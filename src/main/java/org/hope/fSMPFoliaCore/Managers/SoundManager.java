package org.hope.fSMPFoliaCore.Managers;

import org.bukkit.entity.Player;

public class SoundManager {
    private final ConfigManager config;

    public SoundManager(ConfigManager config) {
        this.config = config;
    }

    /** Play a configurable sound to a player. eventKey = e.g. "teleport", "msg-receive" */
    public void play(Player player, String eventKey) {
        String path = "sounds." + eventKey;
        if (!config.getConfig().getBoolean(path + ".enabled", true)) return;

        String soundName = config.getConfig().getString(path + ".sound", "minecraft:entity.enderman.teleport");
        float volume = (float) config.getConfig().getDouble(path + ".volume", 1.0);
        float pitch  = (float) config.getConfig().getDouble(path + ".pitch", 1.0);

        // Use the key-based API to avoid deprecated Sound.valueOf
        try {
            player.playSound(player.getLocation(), soundName, volume, pitch);
        } catch (Exception e) {
            // Bad sound name in config — skip silently
        }
    }
}
