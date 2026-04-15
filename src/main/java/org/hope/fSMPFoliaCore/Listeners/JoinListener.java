package org.hope.fSMPFoliaCore.Listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

import java.time.Duration;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.CrateManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.SpawnManager;
import org.hope.fSMPFoliaCore.Managers.VoteStorageManager;

public class JoinListener implements Listener {
    private final FSMPFoliaCore plugin;
    private final ConfigManager configManager;
    private final LangManager lang;
    private final SpawnManager spawnManager;
    private final CrateManager crateManager;
    private final VoteStorageManager voteStorage;

    public JoinListener(FSMPFoliaCore plugin, ConfigManager configManager, LangManager lang,
                        SpawnManager spawnManager, CrateManager crateManager, VoteStorageManager voteStorage) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.lang = lang;
        this.spawnManager = spawnManager;
        this.crateManager = crateManager;
        this.voteStorage = voteStorage;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!configManager.isJoinEnabled()) {
            event.joinMessage(null);
        } else {
            String[] parts = lang.getJoinMessage().split("\\{player\\}", 2);
            String before = parts[0];
            String after  = parts.length > 1 ? parts[1] : "";

            event.joinMessage(Component.text()
                    .append(Component.text("➤ ", configManager.getJoinArrowColor()))
                    .append(Component.text(before, configManager.getJoinTextColor()))
                    .append(Component.text(event.getPlayer().getName(), configManager.getJoinNameColor()).decorate(TextDecoration.BOLD))
                    .append(Component.text(after, configManager.getJoinTextColor()))
                    .build());
        }

        // MOTD title shown to all joining players
        if (configManager.isJoinTitleEnabled()) {
            event.getPlayer().getScheduler().runDelayed(plugin, task -> {
                event.getPlayer().showTitle(Title.title(
                        Component.text(lang.getJoinTitleMain())
                                .color(NamedTextColor.AQUA)
                                .decorate(TextDecoration.BOLD),
                        Component.text(lang.getJoinTitleSub())
                                .color(NamedTextColor.DARK_AQUA),
                        Title.Times.times(
                                Duration.ofMillis(500),
                                Duration.ofMillis(3000),
                                Duration.ofMillis(500))
                ));
            }, null, 10L);
        }

        // First-join welcome guide
        if (!event.getPlayer().hasPlayedBefore()) {
            event.getPlayer().getScheduler().runDelayed(plugin, task ->
                    sendWelcomeGuide(event.getPlayer()), null, 40L);
        }

        if (configManager.isTeleportOnFirstJoin()
                && !event.getPlayer().hasPlayedBefore()
                && spawnManager.hasSpawn()) {
            Location spawn = spawnManager.getSpawn();
            if (spawn != null) {
                // Delay by 1 tick to ensure the player is fully initialised before teleporting
                event.getPlayer().getScheduler().runDelayed(plugin, task ->
                        event.getPlayer().teleportAsync(spawn), null, 1L);
            }
        }

        // Deliver any vote crates that arrived while player was offline
        String name = event.getPlayer().getName();
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            int pending = voteStorage.claimPending(name);
            if (pending > 0) {
                int finalPending = pending;
                event.getPlayer().getScheduler().runDelayed(plugin, task -> {
                    crateManager.addCrates(event.getPlayer(), finalPending);
                    event.getPlayer().sendMessage(
                            Component.text(lang.getVotePending(finalPending), NamedTextColor.AQUA));
                }, null, 20L); // slight delay so player is fully loaded
            }
        });
    }

    private void sendWelcomeGuide(org.bukkit.entity.Player player) {
        // ── border ──
        Component border = Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_AQUA);

        // ── header ──
        Component header = Component.text()
                .append(Component.text("   ✦ ", NamedTextColor.DARK_AQUA))
                .append(Component.text("FriendlySMP Revive", NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                .append(Component.text(" ✦", NamedTextColor.DARK_AQUA))
                .build();

        // ── rows ──
        List<Component> lines = List.of(
            row("💰", "Валута", "Златни кюлчета — физически в инвентара"),
            row("", "", "/balance  /pay <играч> <сума>  /balancetop"),
            row("", "", "/balanceconvert — конвертира монети в кюлчета"),
            Component.empty(),
            row("⚔", "Диаманти", "Крафтът изисква XP — пести нивата си"),
            Component.empty(),
            row("🌋", "Нетер & Краят", "Заключени при старт, отварят се"),
            row("", "", "след няколко дни — следи чата"),
            Component.empty(),
            row("📦", "Кутии", "AFK 60мин → кутия  |  /vote → кутии"),
            row("", "", "Дневен логин → кутия (веднъж на ~20ч)"),
            Component.empty(),
            row("⭐", "Спец. инструменти", "3x3 кирка/лопата, брадва за"),
            row("", "", "цяло дърво — траят 24ч, от кутии")
        );

        player.sendMessage(border);
        player.sendMessage(header);
        player.sendMessage(border);
        for (Component line : lines) player.sendMessage(line);
        player.sendMessage(border);
        player.sendMessage(Component.text("  /discord за въпроси и помощ", NamedTextColor.DARK_AQUA));
        player.sendMessage(border);
    }

    private static Component row(String icon, String label, String value) {
        String prefix = icon.isEmpty() ? "    " : " " + icon + " ";
        if (!label.isEmpty()) {
            return Component.text(prefix, NamedTextColor.DARK_AQUA)
                    .append(Component.text(label + ": ", NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                    .append(Component.text(value, NamedTextColor.AQUA));
        } else {
            return Component.text(prefix, NamedTextColor.DARK_AQUA)
                    .append(Component.text(value, NamedTextColor.AQUA));
        }
    }
}
