package org.hope.fSMPFoliaCore.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.hope.fSMPFoliaCore.Managers.VanishManager;

public class VanishListener implements Listener {
    private final VanishManager vanishManager;

    public VanishListener(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    /** When a new player joins, hide any currently vanished players from them (unless they have vanish permission). */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        vanishManager.applyVanishToNewViewer(event.getPlayer());
    }

    /** When a vanished player quits, ensure they're cleaned up. */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (vanishManager.isVanished(player)) {
            // Suppress the quit message for vanished players
            event.quitMessage(null);
            vanishManager.onQuit(player.getUniqueId());
        }
    }
}
