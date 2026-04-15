package org.hope.fSMPFoliaCore.Managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlaytimeManager {
    private final FSMPFoliaCore plugin;
    private File file;
    private FileConfiguration data;

    // session start times (in memory only)
    private final Map<UUID, Long> sessionStart = new ConcurrentHashMap<>();
    // total seconds (loaded from file)
    private final Map<UUID, Long> totalSeconds = new ConcurrentHashMap<>();

    public PlaytimeManager(FSMPFoliaCore plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "playtime.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) {
                plugin.getLogger().severe("Failed to create playtime.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(false)) {
            try {
                totalSeconds.put(UUID.fromString(key), data.getLong(key));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private void saveAsync() {
        Map<UUID, Long> snapshot = Map.copyOf(totalSeconds);
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            YamlConfiguration yml = new YamlConfiguration();
            snapshot.forEach((uuid, secs) -> yml.set(uuid.toString(), secs));
            try { yml.save(file); } catch (IOException e) {
                plugin.getLogger().severe("Failed to save playtime.yml: " + e.getMessage());
            }
        });
    }

    public void onJoin(UUID uuid) {
        sessionStart.put(uuid, System.currentTimeMillis());
    }

    public void onQuit(UUID uuid) {
        Long start = sessionStart.remove(uuid);
        if (start == null) return;
        long sessionSecs = (System.currentTimeMillis() - start) / 1000L;
        totalSeconds.merge(uuid, sessionSecs, Long::sum);
        saveAsync();
    }

    /** Total playtime in seconds (excluding current session). */
    public long getTotalSeconds(UUID uuid) {
        return totalSeconds.getOrDefault(uuid, 0L);
    }

    /** Current session seconds. */
    public long getSessionSeconds(UUID uuid) {
        Long start = sessionStart.get(uuid);
        if (start == null) return 0L;
        return (System.currentTimeMillis() - start) / 1000L;
    }

    /** Format seconds as "Xд Xч Xм Xс". */
    public static String format(long totalSecs) {
        long days    = totalSecs / 86400;
        long hours   = (totalSecs % 86400) / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long secs    = totalSecs % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0)    sb.append(days).append("д ");
        if (hours > 0)   sb.append(hours).append("ч ");
        if (minutes > 0) sb.append(minutes).append("м ");
        sb.append(secs).append("с");
        return sb.toString().trim();
    }
}
