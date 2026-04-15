package org.hope.fSMPFoliaCore.Managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;

import java.io.File;
import java.io.IOException;

public class PortalLockManager {
    private final FSMPFoliaCore plugin;
    private File file;
    private FileConfiguration data;

    public PortalLockManager(FSMPFoliaCore plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "timers.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) {
                plugin.getLogger().severe("Failed to create timers.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        // Initialize start times if absent
        if (!data.contains("nether-start")) {
            data.set("nether-start", System.currentTimeMillis());
        }
        if (!data.contains("end-start")) {
            data.set("end-start", System.currentTimeMillis());
        }
        saveSync();
    }

    private void saveSync() {
        try { data.save(file); } catch (IOException e) {
            plugin.getLogger().severe("Failed to save timers.yml: " + e.getMessage());
        }
    }

    private void saveAsync() {
        YamlConfiguration snapshot = new YamlConfiguration();
        for (String key : data.getKeys(true)) snapshot.set(key, data.get(key));
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            try { snapshot.save(file); } catch (IOException e) {
                plugin.getLogger().severe("Failed to save timers.yml: " + e.getMessage());
            }
        });
    }

    /** Get start time in millis for the given portal type ("nether" or "end"). */
    public long getStartTime(String portal) {
        return data.getLong(portal + "-start", System.currentTimeMillis());
    }

    /** Reset the timer for the given portal type ("nether" or "end"). */
    public void resetTimer(String portal) {
        data.set(portal + "-start", System.currentTimeMillis());
        saveAsync();
    }

    /** Returns how many milliseconds are left before the portal unlocks. 0 if already unlocked. */
    public long getRemainingMillis(String portal, ConfigManager config) {
        long unlockMs = getUnlockDurationMs(portal, config);
        long elapsed = System.currentTimeMillis() - getStartTime(portal);
        return Math.max(0, unlockMs - elapsed);
    }

    public boolean isLocked(String portal, ConfigManager config) {
        return getRemainingMillis(portal, config) > 0;
    }

    private long getUnlockDurationMs(String portal, ConfigManager config) {
        int days    = config.getConfig().getInt("portals." + portal + ".unlock-days", 0);
        int hours   = config.getConfig().getInt("portals." + portal + ".unlock-hours", 0);
        int minutes = config.getConfig().getInt("portals." + portal + ".unlock-minutes", 0);
        return ((days * 86400L) + (hours * 3600L) + (minutes * 60L)) * 1000L;
    }

    public static String formatRemaining(long millis) {
        long secs = millis / 1000;
        long days = secs / 86400;
        long hours = (secs % 86400) / 3600;
        long mins  = (secs % 3600) / 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0)  sb.append(days).append("д ");
        if (hours > 0) sb.append(hours).append("ч ");
        sb.append(mins).append("м");
        return sb.toString().trim();
    }
}
