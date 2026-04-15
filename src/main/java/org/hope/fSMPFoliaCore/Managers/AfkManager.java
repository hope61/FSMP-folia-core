package org.hope.fSMPFoliaCore.Managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class AfkManager {
    // UUID -> last movement time (millis)
    private final Map<UUID, Long> lastMove = new ConcurrentHashMap<>();
    // UUID -> last known location (for move detection)
    private final Map<UUID, Location> lastLocation = new ConcurrentHashMap<>();
    // set of currently AFK players
    private final Set<UUID> afk = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void updateActivity(Player player) {
        lastMove.put(player.getUniqueId(), System.currentTimeMillis());
        lastLocation.put(player.getUniqueId(), player.getLocation().clone());
    }

    public void setAfk(UUID uuid, boolean isAfk) {
        if (isAfk) afk.add(uuid);
        else afk.remove(uuid);
    }

    public boolean isAfk(UUID uuid) {
        return afk.contains(uuid);
    }

    public long getIdleMillis(UUID uuid) {
        Long last = lastMove.get(uuid);
        if (last == null) return 0L;
        return System.currentTimeMillis() - last;
    }

    public Location getLastLocation(UUID uuid) {
        return lastLocation.get(uuid);
    }

    public void onQuit(UUID uuid) {
        lastMove.remove(uuid);
        lastLocation.remove(uuid);
        afk.remove(uuid);
    }

    /** Returns true if all online players are AFK. */
    public boolean allAfk(Iterable<? extends Player> online) {
        for (Player p : online) {
            if (!isAfk(p.getUniqueId())) return false;
        }
        return true;
    }
}
