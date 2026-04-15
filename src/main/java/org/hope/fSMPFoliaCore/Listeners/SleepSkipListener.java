package org.hope.fSMPFoliaCore.Listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.SoundManager;

public class SleepSkipListener implements Listener {
    private final FSMPFoliaCore plugin;
    private final ConfigManager config;
    private final LangManager lang;
    private final SoundManager soundManager;

    public SleepSkipListener(FSMPFoliaCore plugin, ConfigManager config, LangManager lang, SoundManager soundManager) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.soundManager = soundManager;
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        //noinspection deprecation
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        World world = event.getPlayer().getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) return;

        // Schedule a check one tick later (player needs to be "in bed")
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> checkSleep(world), 1L);
    }

    private void checkSleep(World world) {
        if (!config.isSleepSkipEnabled()) return;
        if (!isNight(world)) return;

        long totalCount = world.getPlayers().size();
        long sleepingCount = world.getPlayers().stream()
                .filter(p -> p.getSleepTicks() > 0)
                .count();

        if (totalCount == 0) return;

        int required = config.getSleepRequiredPlayers();
        double pct = config.getSleepPercentage();

        boolean enough = (required > 0 && sleepingCount >= required)
                || (pct > 0 && (double) sleepingCount / totalCount * 100 >= pct);

        if (config.isSleepBroadcastProgress()) {
            Bukkit.broadcast(Component.text(
                    lang.getSleepProgress(sleepingCount, totalCount), NamedTextColor.DARK_PURPLE));
        }

        if (enough) {
            world.setTime(1000L);
            world.setWeatherDuration(0);
            world.setThunderDuration(0);
            Bukkit.broadcast(Component.text(lang.getSleepSkipped(), NamedTextColor.LIGHT_PURPLE));
            for (Player p : Bukkit.getOnlinePlayers()) {
                soundManager.play(p, "sleep-skip");
            }
        }
    }

    private boolean isNight(World world) {
        long time = world.getTime();
        return time >= 12300 && time <= 23850;
    }
}
