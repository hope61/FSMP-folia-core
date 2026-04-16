package org.hope.fSMPFoliaCore.Listeners;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.AfkManager;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.CrateManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;

public class AfkListener implements Listener {
    private final FSMPFoliaCore plugin;
    private final AfkManager afkManager;
    private final ConfigManager config;
    private final LangManager lang;
    private final CrateManager crateManager;

    private ScheduledTask checkTask;

    // Players currently being kicked (prevents repeated kick attempts)
    private final java.util.Set<java.util.UUID> kickPending =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    public AfkListener(FSMPFoliaCore plugin, AfkManager afkManager, ConfigManager config,
                       LangManager lang, CrateManager crateManager) {
        this.plugin = plugin;
        this.afkManager = afkManager;
        this.config = config;
        this.lang = lang;
        this.crateManager = crateManager;
    }

    public void startTask() {
        if (checkTask != null) checkTask.cancel();
        // Check every 20 seconds
        checkTask = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> checkAfk(), 400L, 400L);
    }

    public void stopTask() {
        if (checkTask != null) { checkTask.cancel(); checkTask = null; }
    }

    private void checkAfk() {
        if (!config.isAfkEnabled()) return;
        long timeoutMs = config.getAfkTimeoutMinutes() * 60_000L;
        long crateAfterMs = config.getAfkCrateAfterMinutes() * 60_000L;
        long kickAfterMs = config.getAfkKickAfterMinutes() * 60_000L;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("fsmp.afk.bypass")) continue;
            long idle = afkManager.getIdleMillis(player.getUniqueId());

            // Auto-AFK detection
            if (!afkManager.isAfk(player.getUniqueId()) && timeoutMs > 0 && idle >= timeoutMs) {
                afkManager.setAfk(player.getUniqueId(), true);
                Bukkit.broadcast(Component.text(lang.getAfkEnter(player.getName()), lang.secondary()));
            }

            // Crate reward for AFK time — inventory ops must run on player's region thread
            if (afkManager.isAfk(player.getUniqueId()) && crateAfterMs > 0 && idle >= crateAfterMs) {
                if (crateManager.canClaimHourly(player.getUniqueId())) {
                    crateManager.recordHourlyClaim(player.getUniqueId());
                    player.getScheduler().run(plugin, t -> {
                        crateManager.addCrates(player, 1);
                        player.sendMessage(Component.text(lang.getCratesReceivedAfk(), lang.primary()));
                    }, null);
                }
            }

            // Auto-kick — only once per player
            if (kickAfterMs > 0 && idle >= kickAfterMs && !kickPending.contains(player.getUniqueId())) {
                kickPending.add(player.getUniqueId());
                player.getScheduler().run(plugin, task -> {
                    kickPending.remove(player.getUniqueId());
                    player.kick(Component.text(lang.getAfkKicked(), lang.secondary()));
                }, () -> kickPending.remove(player.getUniqueId()));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        // Only register actual position change (not just head rotation)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        boolean wasAfk = afkManager.isAfk(player.getUniqueId());
        afkManager.updateActivity(player);

        if (wasAfk) {
            afkManager.setAfk(player.getUniqueId(), false);
            Bukkit.broadcast(Component.text(lang.getAfkLeave(player.getName()), lang.secondary()));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        afkManager.updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        afkManager.onQuit(uuid);
        kickPending.remove(uuid);
    }
}
