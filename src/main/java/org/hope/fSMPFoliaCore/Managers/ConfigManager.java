package org.hope.fSMPFoliaCore.Managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;

public class ConfigManager {
    private final FSMPFoliaCore plugin;

    public ConfigManager(FSMPFoliaCore plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    // ── Discord ──────────────────────────────────────────────────

    public boolean isDiscordEnabled() {
        return getConfig().getBoolean("discord.enabled", true);
    }

    public String getDiscordLink() {
        return getConfig().getString("discord.link", "https://discord.gg/yourlink");
    }

    public long getAnnouncementInterval() {
        return getConfig().getLong("discord.interval-seconds", 300L);
    }

    // ── Voice Chat ────────────────────────────────────────────────

    public boolean isVoicechatAnnouncementEnabled() {
        return getConfig().getBoolean("voicechat.enabled", true);
    }

    public String getVoicechatModLink() {
        return getConfig().getString("voicechat.mod-link", "https://modrinth.com/plugin/simple-voice-chat");
    }

    public long getVoicechatAnnouncementInterval() {
        return getConfig().getLong("voicechat.interval-seconds", 2700L);
    }

    // ── Join ─────────────────────────────────────────────────────

    public boolean isJoinEnabled() {
        return getConfig().getBoolean("join.enabled", true);
    }

    public boolean isJoinTitleEnabled() {
        return getConfig().getBoolean("join.show-title", true);
    }

    // ── Leave ────────────────────────────────────────────────────

    public boolean isLeaveEnabled() {
        return getConfig().getBoolean("leave.enabled", true);
    }

    // ── Spawn ─────────────────────────────────────────────────────

    public boolean isTeleportOnFirstJoin() {
        return getConfig().getBoolean("spawn.teleport-on-first-join", true);
    }

    public boolean isTeleportOnRespawn() {
        return getConfig().getBoolean("spawn.teleport-on-respawn", true);
    }

    public int getSpawnWarmupSeconds() {
        return getConfig().getInt("spawn.warmup-seconds", 5);
    }

    public int getSpawnCooldownSeconds() {
        return getConfig().getInt("spawn.cooldown-seconds", 30);
    }

    // ── Home ─────────────────────────────────────────────────────

    public int getHomeWarmupSeconds() {
        return getConfig().getInt("home.warmup-seconds", 3);
    }

    public int getHomeCooldownSeconds() {
        return getConfig().getInt("home.cooldown-seconds", 0);
    }

    // ── TPA ──────────────────────────────────────────────────────

    public int getTpaWarmupSeconds() {
        return getConfig().getInt("tpa.warmup-seconds", 3);
    }

    // ── AFK ──────────────────────────────────────────────────────

    public boolean isAfkEnabled() {
        return getConfig().getBoolean("afk.enabled", true);
    }

    public int getAfkTimeoutMinutes() {
        return getConfig().getInt("afk.timeout-minutes", 5);
    }

    public int getAfkCrateAfterMinutes() {
        return getConfig().getInt("afk.crate-after-minutes", 60);
    }

    public int getAfkKickAfterMinutes() {
        return getConfig().getInt("afk.kick-after-minutes", 0);
    }

    // ── Sleep ─────────────────────────────────────────────────────

    public boolean isSleepSkipEnabled() {
        return getConfig().getBoolean("sleep.enabled", true);
    }

    public int getSleepRequiredPlayers() {
        return getConfig().getInt("sleep.required-players", 0);
    }

    public double getSleepPercentage() {
        return getConfig().getDouble("sleep.percentage", 50);
    }

    public boolean isSleepBroadcastProgress() {
        return getConfig().getBoolean("sleep.broadcast-progress", true);
    }

    // ── Portal Lock ───────────────────────────────────────────────

    public boolean isPortalEnabled(String portal) {
        return getConfig().getBoolean("portals." + portal + ".enabled", true);
    }

    public int getPortalBroadcastIntervalMinutes(String portal) {
        return getConfig().getInt("portals." + portal + ".broadcast-interval-minutes", 60);
    }

    // ── Diamond Crafting ──────────────────────────────────────────

    public boolean isDiamondCraftingEnabled() {
        return getConfig().getBoolean("diamond-crafting.enabled", true);
    }

    public int getDiamondCraftingXpCost() {
        return getConfig().getInt("diamond-crafting.xp-cost", 187);
    }

    public String getDiamondCraftingBypassPermission() {
        return getConfig().getString("diamond-crafting.bypass-permission", "fsmp.diamond-craft.bypass");
    }

    // ── Vote ──────────────────────────────────────────────────────

    public int getVoteCratesPerVote() {
        return getConfig().getInt("vote.crates-per-vote", 3);
    }

    public java.util.List<String> getVoteLinks() {
        return getConfig().getStringList("vote.links");
    }

    // ── Special Tools ─────────────────────────────────────────────

    public int getSpecialToolLifetimeHours() {
        return getConfig().getInt("special-tools.lifetime-hours", 24);
    }
}
