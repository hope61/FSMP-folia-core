package org.hope.fSMPFoliaCore.Commands;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.SpawnManager;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnCommand implements CommandExecutor {
    private final FSMPFoliaCore plugin;
    private final ConfigManager configManager;
    private final LangManager lang;
    private final SpawnManager spawnManager;

    private final Map<UUID, ScheduledTask> warmups = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public SpawnCommand(FSMPFoliaCore plugin, ConfigManager configManager, LangManager lang, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.lang = lang;
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getSpawnPlayersOnly(), NamedTextColor.DARK_AQUA));
            return true;
        }

        if (!player.hasPermission("fsmp.spawn")) {
            player.sendMessage(Component.text(lang.getSpawnNoPermission(), NamedTextColor.DARK_AQUA));
            return true;
        }

        Location spawn = spawnManager.getSpawn();
        if (spawn == null) {
            player.sendMessage(Component.text(lang.getSpawnNotSet(), NamedTextColor.DARK_AQUA));
            return true;
        }

        // Cooldown check
        int cooldownSeconds = configManager.getSpawnCooldownSeconds();
        if (cooldownSeconds > 0 && !player.hasPermission("fsmp.spawn.bypass-cooldown")) {
            Long lastUse = cooldowns.get(player.getUniqueId());
            if (lastUse != null) {
                long elapsed = (System.currentTimeMillis() - lastUse) / 1000L;
                long remaining = cooldownSeconds - elapsed;
                if (remaining > 0) {
                    player.sendActionBar(Component.text(
                            lang.getSpawnCooldown(remaining), NamedTextColor.RED));
                    return true;
                }
            }
        }

        int warmupSeconds = configManager.getSpawnWarmupSeconds();

        if (warmupSeconds <= 0) {
            teleportToSpawn(player, spawn);
            return true;
        }

        // Cancel any existing warmup before starting a new one
        cancelWarmup(player.getUniqueId());

        Location startLocation = player.getLocation().clone();
        int[] secondsLeft = { warmupSeconds };

        // Show the initial action bar and play the first tick immediately
        player.sendActionBar(buildCountdownBar(warmupSeconds, warmupSeconds, lang.getSpawnStandStill()));
        playTickSound(player, warmupSeconds, warmupSeconds);

        // player.getScheduler() guarantees the task runs on the region that owns this
        // player, so getLocation(), sendMessage(), playSound() etc. are all region-safe.
        ScheduledTask task = player.getScheduler().runAtFixedRate(plugin, t -> {
            if (hasMoved(startLocation, player.getLocation())) {
                t.cancel();
                warmups.remove(player.getUniqueId());
                player.sendActionBar(Component.empty());
                player.sendMessage(Component.text(lang.getSpawnWarmupCancelled(), NamedTextColor.DARK_AQUA));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                return;
            }

            secondsLeft[0]--;

            if (secondsLeft[0] <= 0) {
                t.cancel();
                warmups.remove(player.getUniqueId());
                player.sendActionBar(Component.empty());
                teleportToSpawn(player, spawn);
                return;
            }

            player.sendActionBar(buildCountdownBar(secondsLeft[0], warmupSeconds, lang.getSpawnStandStill()));
            playTickSound(player, secondsLeft[0], warmupSeconds);

        }, () -> warmups.remove(player.getUniqueId()), 20L, 20L);

        if (task != null) {
            warmups.put(player.getUniqueId(), task);
        }
        return true;
    }

    private void teleportToSpawn(Player player, Location spawn) {
        player.teleportAsync(spawn).thenAccept(success -> {
            if (!success) return;
            int cooldownSeconds = configManager.getSpawnCooldownSeconds();
            if (cooldownSeconds > 0) {
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            }
            // Schedule back onto the player's region thread after the async teleport
            player.getScheduler().run(plugin, t -> {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.2f);
                player.showTitle(Title.title(
                        Component.text(lang.getSpawnTitle())
                                .color(NamedTextColor.AQUA)
                                .decorate(TextDecoration.BOLD),
                        Component.text(lang.getSpawnTeleported())
                                .color(NamedTextColor.DARK_AQUA),
                        Title.Times.times(
                                Duration.ofMillis(200),
                                Duration.ofMillis(1800),
                                Duration.ofMillis(400))
                ));
            }, null);
        });
    }

    /** Builds an action bar like: █████░░ 4с — Не се движи! */
    private Component buildCountdownBar(int secondsLeft, int totalSeconds, String standStillText) {
        int barLength = totalSeconds;
        int filled = secondsLeft;

        StringBuilder filledStr = new StringBuilder();
        StringBuilder emptyStr  = new StringBuilder();
        for (int i = 0; i < filled; i++)               filledStr.append('█');
        for (int i = 0; i < barLength - filled; i++)   emptyStr.append('█');

        return Component.text()
                .append(Component.text(filledStr.toString()).color(NamedTextColor.AQUA))
                .append(Component.text(emptyStr.toString()).color(NamedTextColor.DARK_GRAY))
                .append(Component.text(" "))
                .append(Component.text(secondsLeft + "с").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                .append(Component.text(" — " + standStillText).color(NamedTextColor.GRAY))
                .build();
    }

    /** Pitch rises from 0.8 → 1.6 as the countdown nears zero. */
    private void playTickSound(Player player, int secondsLeft, int totalSeconds) {
        float progress = 1.0f - (float) secondsLeft / totalSeconds;
        float pitch = 0.8f + progress * 0.8f;
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, pitch);
    }

    public void cancelWarmup(UUID playerId) {
        ScheduledTask task = warmups.remove(playerId);
        if (task != null) task.cancel();
    }

    private boolean hasMoved(Location from, Location to) {
        return Math.abs(from.getX() - to.getX()) > 0.01
            || Math.abs(from.getY() - to.getY()) > 0.01
            || Math.abs(from.getZ() - to.getZ()) > 0.01;
    }
}
