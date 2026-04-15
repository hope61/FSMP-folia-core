package org.hope.fSMPFoliaCore.Managers;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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

    public TextColor getBarColor() {
        return parseColor(getConfig().getString("discord.colors.bar"), NamedTextColor.DARK_PURPLE);
    }

    public TextColor getMessageColor() {
        return parseColor(getConfig().getString("discord.colors.message"), NamedTextColor.LIGHT_PURPLE);
    }

    public TextColor getButtonColor() {
        return parseColor(getConfig().getString("discord.colors.button"), NamedTextColor.LIGHT_PURPLE);
    }

    public TextColor getButtonHoverColor() {
        return parseColor(getConfig().getString("discord.colors.button-hover"), NamedTextColor.DARK_PURPLE);
    }

    // ── Join ─────────────────────────────────────────────────────

    public boolean isJoinEnabled() {
        return getConfig().getBoolean("join.enabled", true);
    }

    public TextColor getJoinArrowColor() {
        return parseColor(getConfig().getString("join.colors.arrow"), NamedTextColor.DARK_PURPLE);
    }

    public TextColor getJoinNameColor() {
        return parseColor(getConfig().getString("join.colors.name"), NamedTextColor.LIGHT_PURPLE);
    }

    public TextColor getJoinTextColor() {
        return parseColor(getConfig().getString("join.colors.text"), NamedTextColor.LIGHT_PURPLE);
    }

    // ── Leave ────────────────────────────────────────────────────

    public boolean isLeaveEnabled() {
        return getConfig().getBoolean("leave.enabled", true);
    }

    public TextColor getLeaveArrowColor() {
        return parseColor(getConfig().getString("leave.colors.arrow"), NamedTextColor.DARK_PURPLE);
    }

    public TextColor getLeaveNameColor() {
        return parseColor(getConfig().getString("leave.colors.name"), NamedTextColor.LIGHT_PURPLE);
    }

    public TextColor getLeaveTextColor() {
        return parseColor(getConfig().getString("leave.colors.text"), NamedTextColor.GRAY);
    }

    // ── Chat ─────────────────────────────────────────────────────

    public TextColor getChatNameColor() {
        return parseColor(getConfig().getString("chat.colors.name"), NamedTextColor.LIGHT_PURPLE);
    }

    public TextColor getChatSeparatorColor() {
        return parseColor(getConfig().getString("chat.colors.separator"), NamedTextColor.DARK_PURPLE);
    }

    public TextColor getChatMessageColor() {
        return parseColor(getConfig().getString("chat.colors.message"), NamedTextColor.WHITE);
    }

    // ── Msg ──────────────────────────────────────────────────────

    public TextColor getMsgArrowColor() {
        return parseColor(getConfig().getString("msg.colors.arrow"), NamedTextColor.DARK_PURPLE);
    }

    public TextColor getMsgSeparatorColor() {
        return parseColor(getConfig().getString("msg.colors.separator"), NamedTextColor.DARK_PURPLE);
    }

    public TextColor getMsgMessageColor() {
        return parseColor(getConfig().getString("msg.colors.message"), NamedTextColor.WHITE);
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

    // ── Join ─────────────────────────────────────────────────────

    public boolean isJoinTitleEnabled() {
        return getConfig().getBoolean("join.show-title", true);
    }

    // ── Home ─────────────────────────────────────────────────────

    public int getHomeWarmupSeconds() {
        return getConfig().getInt("home.warmup-seconds", 3);
    }

    public int getHomeCooldownSeconds() {
        return getConfig().getInt("home.cooldown-seconds", 0);
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

    public int getPortalBroadcastIntervalMinutes() {
        // Use the nether interval as global (they share same value)
        return getConfig().getInt("portals.nether.broadcast-interval-minutes", 60);
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

    // ── Util ─────────────────────────────────────────────────────

    private TextColor parseColor(String value, TextColor fallback) {
        if (value == null || value.isEmpty()) return fallback;
        if (value.startsWith("#")) {
            TextColor hex = TextColor.fromHexString(value);
            return hex != null ? hex : fallback;
        }
        TextColor named = NamedTextColor.NAMES.value(value.toLowerCase());
        return named != null ? named : fallback;
    }
}
