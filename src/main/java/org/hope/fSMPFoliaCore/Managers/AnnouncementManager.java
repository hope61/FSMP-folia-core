package org.hope.fSMPFoliaCore.Managers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;

public class AnnouncementManager {
    private final FSMPFoliaCore plugin;
    private final ConfigManager configManager;
    private final LangManager lang;
    private AfkManager afkManager; // optional, set after construction

    private static final String BAR     = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    private static final String PADDING = "              ";

    private ScheduledTask currentTask;

    public AnnouncementManager(FSMPFoliaCore plugin, ConfigManager configManager, LangManager lang) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.lang = lang;
    }

    public void start() {
        stop();
        long intervalTicks = configManager.getAnnouncementInterval() * 20L;
        currentTask = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(
                plugin,
                task -> sendAnnouncement(),
                1L,
                intervalTicks
        );
    }

    public void stop() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
    }

    public void setAfkManager(AfkManager afkManager) {
        this.afkManager = afkManager;
    }

    private void sendAnnouncement() {
        if (!configManager.isDiscordEnabled()) {
            return;
        }
        if (plugin.getServer().getOnlinePlayers().isEmpty()) {
            return;
        }
        // Suppress announcement if all online players are AFK
        if (afkManager != null && afkManager.allAfk(plugin.getServer().getOnlinePlayers())) {
            return;
        }

        plugin.getLogger().info("Sending announcement: " + lang.getDiscordMessage());
        Bukkit.broadcast(buildAnnouncement());
    }

    private Component buildAnnouncement() {
        Component bar = Component.text(BAR)
                .color(configManager.getBarColor())
                .decorate(TextDecoration.BOLD);

        Component title = Component.text()
                .append(Component.text("◆ ", configManager.getBarColor()))
                .append(Component.text(lang.getDiscordTitle(), configManager.getButtonColor()).decorate(TextDecoration.BOLD))
                .append(Component.text(" ◆", configManager.getBarColor()))
                .build();

        Component message = Component.text(lang.getDiscordMessage())
                .color(configManager.getMessageColor());

        Component button = Component.text()
                .append(Component.text("➤ ", configManager.getBarColor()))
                .append(Component.text(lang.getDiscordButtonText(), configManager.getButtonColor()).decorate(TextDecoration.BOLD))
                .append(Component.text(" ◀", configManager.getBarColor()))
                .clickEvent(ClickEvent.openUrl(configManager.getDiscordLink()))
                .hoverEvent(HoverEvent.showText(
                        Component.text(lang.getDiscordHoverText())
                                .color(configManager.getButtonHoverColor())))
                .build();

        return Component.text()
                .append(bar)
                .appendNewline()
                .append(Component.text(PADDING)).append(title)
                .appendNewline()
                .append(Component.text(PADDING)).append(message)
                .appendNewline()
                .append(Component.text(PADDING)).append(button)
                .appendNewline()
                .append(bar)
                .build();
    }
}
