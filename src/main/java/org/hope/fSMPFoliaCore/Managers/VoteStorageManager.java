package org.hope.fSMPFoliaCore.Managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;

import java.io.File;
import java.io.IOException;

/**
 * Persists pending vote crates for players who were offline when the vote arrived.
 * Data is stored in plugins/FSMP-folia-core/pending_votes.yml as:
 *   <lowercaseUsername>: <pendingCrateCount>
 */
public class VoteStorageManager {

    private final FSMPFoliaCore plugin;
    private final File file;
    private FileConfiguration data;

    public VoteStorageManager(FSMPFoliaCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "pending_votes.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) {
                plugin.getLogger().warning("Could not create pending_votes.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    private void save() {
        try { data.save(file); } catch (IOException e) {
            plugin.getLogger().warning("Could not save pending_votes.yml: " + e.getMessage());
        }
    }

    /** Add pending crates for an offline player (case-insensitive username key). */
    public synchronized void addPending(String username, int crates) {
        String key = username.toLowerCase();
        int current = data.getInt(key, 0);
        data.set(key, current + crates);
        save();
    }

    /**
     * Claim and remove all pending crates for a player.
     * Returns 0 if nothing is pending.
     */
    public synchronized int claimPending(String username) {
        String key = username.toLowerCase();
        int pending = data.getInt(key, 0);
        if (pending > 0) {
            data.set(key, null);
            save();
        }
        return pending;
    }

    public synchronized int getPending(String username) {
        return data.getInt(username.toLowerCase(), 0);
    }
}
