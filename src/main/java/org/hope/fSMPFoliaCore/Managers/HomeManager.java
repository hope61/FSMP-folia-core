package org.hope.fSMPFoliaCore.Managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class HomeManager {
    private static final int NAME_MAX_LENGTH = 16;
    private static final String NAME_PATTERN = "[a-zA-Z0-9_]+";

    private final FSMPFoliaCore plugin;
    private File file;
    private FileConfiguration data;
    private final ReentrantLock dataLock = new ReentrantLock();

    // Pending confirmations: uuid -> home name they are about to overwrite
    private final Map<UUID, String> overwritePending = new ConcurrentHashMap<>();

    public HomeManager(FSMPFoliaCore plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "homes.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create homes.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    private void save() {
        // Snapshot under lock, then write async
        YamlConfiguration snapshot = new YamlConfiguration();
        dataLock.lock();
        try {
            for (String key : data.getKeys(true)) {
                snapshot.set(key, data.get(key));
            }
        } finally {
            dataLock.unlock();
        }
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                snapshot.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save homes.yml: " + e.getMessage());
            }
        });
    }

    // ── Validation ─────────────────────────────────────────────────

    public boolean isValidName(String name) {
        return name != null
                && !name.isEmpty()
                && name.length() <= NAME_MAX_LENGTH
                && name.matches(NAME_PATTERN);
    }

    // ── Limit ──────────────────────────────────────────────────────

    public int getHomeLimit(org.bukkit.entity.Player player) {
        if (player.hasPermission("fsmp.home.unlimited")) return Integer.MAX_VALUE;
        for (int i = 100; i >= 2; i--) {
            if (player.hasPermission("fsmp.home.limit." + i)) return i;
        }
        return 1; // fsmp.home.limit.1 default
    }

    // ── Homes data ─────────────────────────────────────────────────

    public List<String> getHomeNames(UUID uuid) {
        dataLock.lock();
        try {
            var section = data.getConfigurationSection(uuid.toString());
            if (section == null) return Collections.emptyList();
            return List.copyOf(section.getKeys(false));
        } finally {
            dataLock.unlock();
        }
    }

    public boolean hasHome(UUID uuid, String name) {
        dataLock.lock();
        try {
            return data.contains(uuid + "." + name + ".world");
        } finally {
            dataLock.unlock();
        }
    }

    public Location getHome(UUID uuid, String name) {
        dataLock.lock();
        try {
            String path = uuid + "." + name;
            if (!data.contains(path + ".world")) return null;

            String worldName = data.getString(path + ".world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) return null;

            return new Location(
                    world,
                    data.getDouble(path + ".x"),
                    data.getDouble(path + ".y"),
                    data.getDouble(path + ".z"),
                    (float) data.getDouble(path + ".yaw"),
                    (float) data.getDouble(path + ".pitch")
            );
        } finally {
            dataLock.unlock();
        }
    }

    public void setHome(UUID uuid, String name, Location loc) {
        dataLock.lock();
        try {
            String path = uuid + "." + name;
            data.set(path + ".world", loc.getWorld().getName());
            data.set(path + ".x", loc.getX());
            data.set(path + ".y", loc.getY());
            data.set(path + ".z", loc.getZ());
            data.set(path + ".yaw", (double) loc.getYaw());
            data.set(path + ".pitch", (double) loc.getPitch());
        } finally {
            dataLock.unlock();
        }
        save();
    }

    public void deleteHome(UUID uuid, String name) {
        dataLock.lock();
        try {
            data.set(uuid + "." + name, null);
        } finally {
            dataLock.unlock();
        }
        save();
    }

    public void deleteAllHomes(UUID uuid) {
        dataLock.lock();
        try {
            data.set(uuid.toString(), null);
        } finally {
            dataLock.unlock();
        }
        save();
    }

    public void renameHome(UUID uuid, String oldName, String newName) {
        // Perform both operations under a single lock to avoid double-save race
        dataLock.lock();
        try {
            String oldPath = uuid + "." + oldName;
            if (!data.contains(oldPath + ".world")) return;
            String worldName = data.getString(oldPath + ".world");
            double x = data.getDouble(oldPath + ".x");
            double y = data.getDouble(oldPath + ".y");
            double z = data.getDouble(oldPath + ".z");
            double yaw = data.getDouble(oldPath + ".yaw");
            double pitch = data.getDouble(oldPath + ".pitch");

            data.set(uuid + "." + oldName, null);

            String newPath = uuid + "." + newName;
            data.set(newPath + ".world", worldName);
            data.set(newPath + ".x", x);
            data.set(newPath + ".y", y);
            data.set(newPath + ".z", z);
            data.set(newPath + ".yaw", yaw);
            data.set(newPath + ".pitch", pitch);
        } finally {
            dataLock.unlock();
        }
        save();
    }

    // ── Overwrite confirmation ─────────────────────────────────────

    public void setPendingOverwrite(UUID uuid, String homeName) {
        overwritePending.put(uuid, homeName);
    }

    public String getPendingOverwrite(UUID uuid) {
        return overwritePending.get(uuid);
    }

    public void clearPendingOverwrite(UUID uuid) {
        overwritePending.remove(uuid);
    }
}
