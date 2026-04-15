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
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.HomeManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HomeCommand implements CommandExecutor, TabCompleter {
    private final FSMPFoliaCore plugin;
    private final ConfigManager configManager;
    private final LangManager lang;
    private final HomeManager homeManager;

    private final Map<UUID, ScheduledTask> warmups = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public HomeCommand(FSMPFoliaCore plugin, ConfigManager configManager, LangManager lang, HomeManager homeManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.lang = lang;
        this.homeManager = homeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getHomePlayersOnly(), NamedTextColor.DARK_AQUA));
            return true;
        }

        if (!player.hasPermission("fsmp.home")) {
            player.sendMessage(Component.text(lang.getHomeNoPermission(), NamedTextColor.DARK_AQUA));
            return true;
        }

        String name = args.length > 0 ? args[0] : "home";

        if (!homeManager.hasHome(player.getUniqueId(), name)) {
            player.sendMessage(Component.text(lang.getHomeNotFound(name), NamedTextColor.DARK_AQUA));
            return true;
        }

        Location dest = homeManager.getHome(player.getUniqueId(), name);
        if (dest == null) {
            player.sendMessage(Component.text(lang.getHomeNotFound(name), NamedTextColor.DARK_AQUA));
            return true;
        }

        // Cooldown check
        int cooldownSeconds = configManager.getHomeCooldownSeconds();
        if (cooldownSeconds > 0 && !player.hasPermission("fsmp.home.bypass-cooldown")) {
            Long last = cooldowns.get(player.getUniqueId());
            if (last != null) {
                long remaining = cooldownSeconds - (System.currentTimeMillis() - last) / 1000L;
                if (remaining > 0) {
                    player.sendActionBar(Component.text(lang.getHomeCooldown(remaining), NamedTextColor.RED));
                    return true;
                }
            }
        }

        cancelWarmup(player.getUniqueId());

        int warmupSeconds = configManager.getHomeWarmupSeconds();
        if (warmupSeconds <= 0) {
            teleportToHome(player, dest, name);
            return true;
        }

        Location startLoc = player.getLocation().clone();
        int[] left = { warmupSeconds };

        player.sendActionBar(buildCountdownBar(warmupSeconds, warmupSeconds, lang.getHomeStandStill()));
        playTickSound(player, warmupSeconds, warmupSeconds);

        ScheduledTask task = player.getScheduler().runAtFixedRate(plugin, t -> {
            if (hasMoved(startLoc, player.getLocation())) {
                t.cancel();
                warmups.remove(player.getUniqueId());
                player.sendActionBar(Component.empty());
                player.sendMessage(Component.text(lang.getHomeWarmupCancelled(), NamedTextColor.DARK_AQUA));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                return;
            }

            left[0]--;

            if (left[0] <= 0) {
                t.cancel();
                warmups.remove(player.getUniqueId());
                player.sendActionBar(Component.empty());
                teleportToHome(player, dest, name);
                return;
            }

            player.sendActionBar(buildCountdownBar(left[0], warmupSeconds, lang.getHomeStandStill()));
            playTickSound(player, left[0], warmupSeconds);
        }, () -> warmups.remove(player.getUniqueId()), 20L, 20L);

        if (task != null) warmups.put(player.getUniqueId(), task);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        if (!player.hasPermission("fsmp.home")) return List.of();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return homeManager.getHomeNames(player.getUniqueId())
                    .stream().filter(h -> h.toLowerCase().startsWith(input)).toList();
        }
        return List.of();
    }

    private void teleportToHome(Player player, Location dest, String name) {
        player.teleportAsync(dest).thenAccept(success -> {
            if (!success) return;
            int cooldownSeconds = configManager.getHomeCooldownSeconds();
            if (cooldownSeconds > 0) cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            player.getScheduler().run(plugin, t -> {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.2f);
                player.showTitle(Title.title(
                        Component.text(lang.getHomeTitleMain(name))
                                .color(NamedTextColor.AQUA)
                                .decorate(TextDecoration.BOLD),
                        Component.text(lang.getHomeTeleported(name))
                                .color(NamedTextColor.DARK_AQUA),
                        Title.Times.times(
                                Duration.ofMillis(200),
                                Duration.ofMillis(1800),
                                Duration.ofMillis(400))
                ));
            }, null);
        });
    }

    private Component buildCountdownBar(int secondsLeft, int totalSeconds, String standStillText) {
        StringBuilder filled = new StringBuilder();
        StringBuilder empty = new StringBuilder();
        for (int i = 0; i < secondsLeft; i++) filled.append('█');
        for (int i = 0; i < totalSeconds - secondsLeft; i++) empty.append('█');

        return Component.text()
                .append(Component.text(filled.toString()).color(NamedTextColor.AQUA))
                .append(Component.text(empty.toString()).color(NamedTextColor.DARK_GRAY))
                .append(Component.text(" "))
                .append(Component.text(secondsLeft + "с").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                .append(Component.text(" — " + standStillText).color(NamedTextColor.GRAY))
                .build();
    }

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
