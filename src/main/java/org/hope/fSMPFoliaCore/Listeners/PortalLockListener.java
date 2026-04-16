package org.hope.fSMPFoliaCore.Listeners;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
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

    private ScheduledTask netherTask;
    private ScheduledTask endTask;

    // Track previous lock state so we can detect the moment each portal unlocks
    private boolean netherWasLocked = true;
    private boolean endWasLocked    = true;

    public PortalLockListener(FSMPFoliaCore plugin, ConfigManager config, LangManager lang,
                               PortalLockManager portalLockManager) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.portalLockManager = portalLockManager;
    }

    public void startBroadcastTask() {
        stopBroadcastTask();

        // Seed initial state so we don't false-fire on first tick
        netherWasLocked = portalLockManager.isLocked("nether", config);
        endWasLocked    = portalLockManager.isLocked("end", config);

        netherTask = startPortalTask("nether");
        endTask    = startPortalTask("end");
    }

    private ScheduledTask startPortalTask(String portal) {
        if (!config.isPortalEnabled(portal)) return null;
        long intervalTicks = config.getPortalBroadcastIntervalMinutes(portal) * 60L * 20L;
        if (intervalTicks <= 0) return null;
        return plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(
                plugin, t -> checkAndAnnounce(portal), intervalTicks, intervalTicks);
    }

    public void stopBroadcastTask() {
        if (netherTask != null) { netherTask.cancel(); netherTask = null; }
        if (endTask    != null) { endTask.cancel();    endTask    = null; }
    }

    private void checkAndAnnounce(String portal) {
        if (!config.isPortalEnabled(portal)) return;

        boolean locked = portalLockManager.isLocked(portal, config);
        boolean wasLocked = portal.equals("nether") ? netherWasLocked : endWasLocked;

        if (wasLocked && !locked) {
            // Portal just unlocked — broadcast the opening announcement
            String msg = portal.equals("nether")
                    ? lang.getPortalNetherOpened()
                    : lang.getPortalEndOpened();
            broadcastOpenAnnouncement(msg);
        } else if (locked) {
            // Still locked — broadcast remaining time reminder
            long remaining = portalLockManager.getRemainingMillis(portal, config);
            String msg = portal.equals("nether")
                    ? lang.getPortalNetherLocked(PortalLockManager.formatRemaining(remaining))
                    : lang.getPortalEndLocked(PortalLockManager.formatRemaining(remaining));
            Bukkit.broadcast(Component.text(msg, lang.secondary()));
        }

        // Update state
        if (portal.equals("nether")) netherWasLocked = locked;
        else endWasLocked = locked;
    }

    private void broadcastOpenAnnouncement(String msg) {
        String bar = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
        Component announcement = Component.text()
                .append(Component.text(bar, lang.secondary()).decorate(TextDecoration.BOLD))
                .appendNewline()
                .append(Component.text("  " + msg, lang.primary()).decorate(TextDecoration.BOLD))
                .appendNewline()
                .append(Component.text(bar, lang.secondary()).decorate(TextDecoration.BOLD))
                .build();
        Bukkit.broadcast(announcement);
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
            player.sendMessage(Component.text(msg, lang.secondary()));
        }
    }
}
