package org.hope.fSMPFoliaCore.Commands;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.SoundManager;
import org.hope.fSMPFoliaCore.Managers.TpaManager;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TpAcceptCommand implements CommandExecutor {
    private final FSMPFoliaCore plugin;
    private final ConfigManager configManager;
    private final LangManager lang;
    private final TpaManager tpaManager;
    private final SoundManager soundManager;

    private final Map<UUID, ScheduledTask> warmups = new ConcurrentHashMap<>();

    public TpAcceptCommand(FSMPFoliaCore plugin, ConfigManager configManager,
                           LangManager lang, TpaManager tpaManager, SoundManager soundManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.lang = lang;
        this.tpaManager = tpaManager;
        this.soundManager = soundManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player target)) {
            sender.sendMessage(Component.text(lang.getTpaPlayersOnly(), NamedTextColor.DARK_PURPLE));
            return true;
        }
        if (!target.hasPermission("fsmp.tpa")) {
            target.sendMessage(Component.text(lang.getNoPermission(), NamedTextColor.DARK_PURPLE));
            return true;
        }

        TpaManager.Request req = tpaManager.removeRequest(target.getUniqueId());
        if (req == null) {
            target.sendMessage(Component.text(lang.getTpaNoPending(), NamedTextColor.DARK_PURPLE));
            return true;
        }

        Player requester = Bukkit.getPlayer(req.from());
        if (requester == null) {
            target.sendMessage(Component.text(lang.getTpaRequesterOffline(), NamedTextColor.DARK_PURPLE));
            return true;
        }

        target.sendMessage(Component.text(lang.getTpaAcceptedTarget(requester.getName()), NamedTextColor.DARK_PURPLE));
        soundManager.play(target, "tpa-accepted");

        int warmupSeconds = configManager.getTpaWarmupSeconds();

        if (warmupSeconds <= 0) {
            doTeleport(requester, target);
            return true;
        }

        // Cancel any existing warmup for the requester
        cancelWarmup(requester.getUniqueId());

        Location startLocation = requester.getLocation().clone();
        int[] secondsLeft = { warmupSeconds };

        requester.sendMessage(Component.text(lang.getTpaAccepted(target.getName()), NamedTextColor.DARK_PURPLE));
        requester.sendActionBar(buildCountdownBar(warmupSeconds, warmupSeconds, lang.getTpaStandStill()));
        playTickSound(requester, warmupSeconds, warmupSeconds);

        ScheduledTask task = requester.getScheduler().runAtFixedRate(plugin, t -> {
            if (hasMoved(startLocation, requester.getLocation())) {
                t.cancel();
                warmups.remove(requester.getUniqueId());
                requester.sendActionBar(Component.empty());
                requester.sendMessage(Component.text(lang.getTpaWarmupCancelled(), NamedTextColor.DARK_PURPLE));
                requester.playSound(requester.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                return;
            }

            secondsLeft[0]--;

            if (secondsLeft[0] <= 0) {
                t.cancel();
                warmups.remove(requester.getUniqueId());
                requester.sendActionBar(Component.empty());
                doTeleport(requester, target);
                return;
            }

            requester.sendActionBar(buildCountdownBar(secondsLeft[0], warmupSeconds, lang.getTpaStandStill()));
            playTickSound(requester, secondsLeft[0], warmupSeconds);

        }, () -> warmups.remove(requester.getUniqueId()), 20L, 20L);

        if (task != null) warmups.put(requester.getUniqueId(), task);
        return true;
    }

    private void doTeleport(Player requester, Player target) {
        requester.teleportAsync(target.getLocation()).thenAccept(success -> {
            if (!success) return;
            requester.getScheduler().run(plugin, t -> {
                soundManager.play(requester, "teleport");
            }, null);
        });
    }

    public void cancelWarmup(UUID uuid) {
        ScheduledTask task = warmups.remove(uuid);
        if (task != null) task.cancel();
    }

    private Component buildCountdownBar(int secondsLeft, int totalSeconds, String label) {
        StringBuilder filled = new StringBuilder();
        StringBuilder empty  = new StringBuilder();
        for (int i = 0; i < secondsLeft; i++)              filled.append('█');
        for (int i = 0; i < totalSeconds - secondsLeft; i++) empty.append('█');

        return Component.text()
                .append(Component.text(filled.toString()).color(NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(empty.toString()).color(NamedTextColor.DARK_PURPLE))
                .append(Component.text(" "))
                .append(Component.text(secondsLeft + "с").color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                .append(Component.text(" — " + label).color(NamedTextColor.GRAY))
                .build();
    }

    private void playTickSound(Player player, int secondsLeft, int totalSeconds) {
        float progress = 1.0f - (float) secondsLeft / totalSeconds;
        float pitch = 0.8f + progress * 0.8f;
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, pitch);
    }

    private boolean hasMoved(Location from, Location to) {
        return Math.abs(from.getX() - to.getX()) > 0.01
            || Math.abs(from.getY() - to.getY()) > 0.01
            || Math.abs(from.getZ() - to.getZ()) > 0.01;
    }
}
