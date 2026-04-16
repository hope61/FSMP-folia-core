package org.hope.fSMPFoliaCore.Listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;

public class LeaveListener implements Listener {
    private final ConfigManager configManager;
    private final LangManager lang;

    public LeaveListener(ConfigManager configManager, LangManager lang) {
        this.configManager = configManager;
        this.lang = lang;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (!configManager.isLeaveEnabled()) {
            event.quitMessage(null);
            return;
        }

        String[] parts = lang.getLeaveMessage().split("\\{player\\}", 2);
        String before = parts[0];
        String after  = parts.length > 1 ? parts[1] : "";

        Component message = Component.text()
                .append(Component.text("◀ ", lang.secondary()))
                .append(Component.text(before, lang.primary()))
                .append(Component.text(event.getPlayer().getName(), lang.primary()).decorate(TextDecoration.BOLD))
                .append(Component.text(after, lang.primary()))
                .build();

        event.quitMessage(message);
    }
}
