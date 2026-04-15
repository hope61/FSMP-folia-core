package org.hope.fSMPFoliaCore.Managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VanishManager {
    private final Plugin plugin;
    private final Set<UUID> vanished = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public VanishManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void vanish(Player player) {
        vanished.add(player.getUniqueId());
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.hasPermission("fsmp.vanish")) {
                other.hidePlayer(plugin, player);
            }
        }
    }

    public void unvanish(Player player) {
        vanished.remove(player.getUniqueId());
        for (Player other : Bukkit.getOnlinePlayers()) {
            other.showPlayer(plugin, player);
        }
    }

    public boolean isVanished(UUID uuid) {
        return vanished.contains(uuid);
    }

    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    /** Called when a player joins — hide vanished players from them (unless they have vanish perm). */
    public void applyVanishToNewViewer(Player viewer) {
        for (UUID uuid : vanished) {
            Player vanishedPlayer = Bukkit.getPlayer(uuid);
            if (vanishedPlayer != null && !viewer.hasPermission("fsmp.vanish")) {
                viewer.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    /** Called when a vanished player quits — clean up state. */
    public void onQuit(UUID uuid) {
        vanished.remove(uuid);
    }
}
