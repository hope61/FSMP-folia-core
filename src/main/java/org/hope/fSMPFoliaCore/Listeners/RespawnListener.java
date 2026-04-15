package org.hope.fSMPFoliaCore.Listeners;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.SpawnManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RespawnListener implements Listener {
    private final FSMPFoliaCore plugin;
    private final ConfigManager configManager;
    private final SpawnManager spawnManager;

    private final Map<UUID, ScheduledTask> pendingRespawn = new ConcurrentHashMap<>();

    public RespawnListener(FSMPFoliaCore plugin, ConfigManager configManager, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.spawnManager = spawnManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!configManager.isTeleportOnRespawn()) return;
        if (!spawnManager.hasSpawn()) return;

        Player player = event.getPlayer();
        // Has a bed or respawn anchor — let vanilla handle it
        if (player.getRespawnLocation() != null) return;

        UUID uuid = player.getUniqueId();

        // Cancel any existing poll task for this player (double-death protection)
        ScheduledTask existing = pendingRespawn.remove(uuid);
        if (existing != null) existing.cancel();

        Location spawn = spawnManager.getSpawn();
        if (spawn == null) return;

        // Poll every tick until the player respawns (isDead flips to false).
        ScheduledTask task = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
            Player p = Bukkit.getPlayer(uuid);

            if (p == null || !pendingRespawn.containsKey(uuid)) {
                pendingRespawn.remove(uuid);
                t.cancel();
                return;
            }

            if (!p.isDead()) {
                pendingRespawn.remove(uuid);
                t.cancel();
                p.teleportAsync(spawn);
            }
        }, 1L, 1L);

        pendingRespawn.put(uuid, task);
    }

    public void removePending(UUID playerId) {
        ScheduledTask task = pendingRespawn.remove(playerId);
        if (task != null) task.cancel();
    }
}
