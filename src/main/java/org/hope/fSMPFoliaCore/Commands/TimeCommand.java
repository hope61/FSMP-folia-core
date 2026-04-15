package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;

public class TimeCommand implements CommandExecutor {
    public enum Mode { DAY, NIGHT, SUN }

    private final FSMPFoliaCore plugin;
    private final LangManager lang;
    private final Mode mode;

    public TimeCommand(FSMPFoliaCore plugin, LangManager lang, Mode mode) {
        this.plugin = plugin;
        this.lang = lang;
        this.mode = mode;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getTimePlayersOnly(), NamedTextColor.DARK_AQUA));
            return true;
        }
        if (!player.hasPermission("fsmp.time")) {
            player.sendMessage(Component.text(lang.getTimeNoPermission(), NamedTextColor.DARK_AQUA));
            return true;
        }

        // World.setTime / setStorm must run on the global region scheduler (world-level op)
        plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> {
            switch (mode) {
                case DAY -> {
                    player.getWorld().setTime(1000L);
                    player.sendMessage(Component.text(lang.getTimeDay(), NamedTextColor.AQUA));
                }
                case NIGHT -> {
                    player.getWorld().setTime(13000L);
                    player.sendMessage(Component.text(lang.getTimeNight(), NamedTextColor.DARK_AQUA));
                }
                case SUN -> {
                    player.getWorld().setWeatherDuration(0);
                    player.getWorld().setThunderDuration(0);
                    player.sendMessage(Component.text(lang.getTimeSun(), NamedTextColor.AQUA));
                }
            }
        });
        return true;
    }
}
