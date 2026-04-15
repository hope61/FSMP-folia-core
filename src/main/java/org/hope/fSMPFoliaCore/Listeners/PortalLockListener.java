package org.hope.fSMPFoliaCore.Listeners;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.PortalLockManager;

public class PortalLockListener implements Listener {
    private final FSMPFoliaCore plugin;
    private final ConfigManager config;
    private final LangManager lang;
    private final PortalLockManager portalLockManager;

    private ScheduledTask broadcastTask;

    public PortalLockListener(FSMPFoliaCore plugin, ConfigManager config, LangManager lang,
                               PortalLockManager portalLockManager) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.portalLockManager = portalLockManager;
    }

    public void startBroadcastTask() {
        if (broadcastTask != null) broadcastTask.cancel();
        long intervalTicks = config.getPortalBroadcastIntervalMinutes() * 60L * 20L;
        if (intervalTicks <= 0) return;
        broadcastTask = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
            broadcastStatus("nether");
            broadcastStatus("end");
        }, intervalTicks, intervalTicks);
    }

    public void stopBroadcastTask() {
        if (broadcastTask != null) { broadcastTask.cancel(); broadcastTask = null; }
    }

    private void broadcastStatus(String portal) {
        if (!config.isPortalEnabled(portal)) return;
        if (!portalLockManager.isLocked(portal, config)) return;
        long remaining = portalLockManager.getRemainingMillis(portal, config);
        String msg = portal.equals("nether")
                ? lang.getPortalNetherLocked(PortalLockManager.formatRemaining(remaining))
                : lang.getPortalEndLocked(PortalLockManager.formatRemaining(remaining));
        Bukkit.broadcast(Component.text(msg, NamedTextColor.DARK_AQUA));
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("fsmp.portal.bypass")) return;

        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        String portal = null;

        if (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) portal = "nether";
        else if (cause == PlayerTeleportEvent.TeleportCause.END_PORTAL
                || cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY) portal = "end";

        if (portal == null) return;
        if (!config.isPortalEnabled(portal)) return;

        if (portalLockManager.isLocked(portal, config)) {
            event.setCancelled(true);
            long remaining = portalLockManager.getRemainingMillis(portal, config);
            String msg = portal.equals("nether")
                    ? lang.getPortalNetherDenied(PortalLockManager.formatRemaining(remaining))
                    : lang.getPortalEndDenied(PortalLockManager.formatRemaining(remaining));
            player.sendMessage(Component.text(msg, NamedTextColor.DARK_AQUA));
        }
    }
}
