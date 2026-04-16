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
    private ScheduledTask voicechatTask;

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

    public void startVoicechat() {
        stopVoicechat();
        if (!configManager.isVoicechatAnnouncementEnabled()) return;
        long intervalTicks = configManager.getVoicechatAnnouncementInterval() * 20L;
        if (intervalTicks <= 0) return;
        // Initial delay = half the voicechat interval so it never fires at the same time as the discord announcement
        long initialDelay = Math.max(1L, intervalTicks / 2);
        voicechatTask = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(
                plugin,
                task -> sendVoicechatAnnouncement(),
                initialDelay,
                intervalTicks
        );
    }

    public void stopVoicechat() {
        if (voicechatTask != null) {
            voicechatTask.cancel();
            voicechatTask = null;
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

    private void sendVoicechatAnnouncement() {
        if (!configManager.isVoicechatAnnouncementEnabled()) return;
        if (plugin.getServer().getOnlinePlayers().isEmpty()) return;
        if (afkManager != null && afkManager.allAfk(plugin.getServer().getOnlinePlayers())) return;
        Bukkit.broadcast(buildVoicechatAnnouncement());
    }

    private Component buildVoicechatAnnouncement() {
        Component bar = Component.text(BAR)
                .color(lang.secondary())
                .decorate(TextDecoration.BOLD);

        Component title = Component.text()
                .append(Component.text("◆ ", lang.secondary()))
                .append(Component.text(lang.getVoicechatTitle(), lang.primary()).decorate(TextDecoration.BOLD))
                .append(Component.text(" ◆", lang.secondary()))
                .build();

        Component message = Component.text(lang.getVoicechatMessage())
                .color(lang.primary());

        Component button = Component.text()
                .append(Component.text("➤ ", lang.secondary()))
                .append(Component.text(lang.getVoicechatButtonText(), lang.primary()).decorate(TextDecoration.BOLD))
                .append(Component.text(" ◀", lang.secondary()))
                .clickEvent(ClickEvent.openUrl(configManager.getVoicechatModLink()))
                .hoverEvent(HoverEvent.showText(
                        Component.text(lang.getVoicechatHoverText())
                                .color(lang.secondary())))
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

    private Component buildAnnouncement() {
        Component bar = Component.text(BAR)
                .color(lang.secondary())
                .decorate(TextDecoration.BOLD);

        Component title = Component.text()
                .append(Component.text("◆ ", lang.secondary()))
                .append(Component.text(lang.getDiscordTitle(), lang.primary()).decorate(TextDecoration.BOLD))
                .append(Component.text(" ◆", lang.secondary()))
                .build();

        Component message = Component.text(lang.getDiscordMessage())
                .color(lang.primary());

        Component button = Component.text()
                .append(Component.text("➤ ", lang.secondary()))
                .append(Component.text(lang.getDiscordButtonText(), lang.primary()).decorate(TextDecoration.BOLD))
                .append(Component.text(" ◀", lang.secondary()))
                .clickEvent(ClickEvent.openUrl(configManager.getDiscordLink()))
                .hoverEvent(HoverEvent.showText(
                        Component.text(lang.getDiscordHoverText())
                                .color(lang.secondary())))
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
