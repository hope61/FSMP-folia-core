package org.hope.fSMPFoliaCore.Managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class SpawnManager {
    private final FSMPFoliaCore plugin;
    private File file;
    private FileConfiguration data;
    private final ReentrantLock dataLock = new ReentrantLock();

    public SpawnManager(FSMPFoliaCore plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "spawn.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create spawn.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public boolean hasSpawn() {
        dataLock.lock();
        try {
            return data.contains("spawn.world");
        } finally {
            dataLock.unlock();
        }
    }

    public Location getSpawn() {
        dataLock.lock();
        try {
            if (!data.contains("spawn.world")) return null;
            String worldName = data.getString("spawn.world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) return null;
            return new Location(
                    world,
                    data.getDouble("spawn.x"),
                    data.getDouble("spawn.y"),
                    data.getDouble("spawn.z"),
                    (float) data.getDouble("spawn.yaw"),
                    (float) data.getDouble("spawn.pitch")
            );
        } finally {
            dataLock.unlock();
        }
    }

    public void setSpawn(Location location) {
        dataLock.lock();
        try {
            data.set("spawn.world", location.getWorld().getName());
            data.set("spawn.x", location.getX());
            data.set("spawn.y", location.getY());
            data.set("spawn.z", location.getZ());
            data.set("spawn.yaw", (double) location.getYaw());
            data.set("spawn.pitch", (double) location.getPitch());
        } finally {
            dataLock.unlock();
        }
        save();
    }

    public void removeSpawn() {
        dataLock.lock();
        try {
            data.set("spawn", null);
        } finally {
            dataLock.unlock();
        }
        save();
    }

    private void save() {
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
                plugin.getLogger().severe("Failed to save spawn.yml: " + e.getMessage());
            }
        });
    }
}
