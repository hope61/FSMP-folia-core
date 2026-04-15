package org.hope.fSMPFoliaCore.Managers;

import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LangManager {
    private final FSMPFoliaCore plugin;
    private FileConfiguration lang;

    public LangManager(FSMPFoliaCore plugin) {
        this.plugin = plugin;
        load();
    }

    public void reload() {
        load();
    }

    private void load() {
        File file = new File(plugin.getDataFolder(), "lang.yml");

        // Write file if absent
        if (!file.exists()) {
            plugin.saveResource("lang.yml", false);
        }

        lang = YamlConfiguration.loadConfiguration(file);

        // Copy any missing keys from the bundled default lang.yml
        InputStream defaults = plugin.getResource("lang.yml");
        if (defaults != null) {
            YamlConfiguration bundled = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaults, StandardCharsets.UTF_8));
            lang.setDefaults(bundled);
            lang.options().copyDefaults(true);
            try {
                lang.save(file);
                // Re-read from disk so the in-memory map has the newly merged keys
                lang = YamlConfiguration.loadConfiguration(file);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save lang.yml after merging defaults: " + e.getMessage());
            }
        }
    }

    private String get(String key) {
        return lang.getString(key, "?" + key);
    }

    // ── Discord ──────────────────────────────────────────────────

    public String getDiscordTitle() {
        return get("discord.title");
    }

    public String getDiscordMessage() {
        return get("discord.message");
    }

    public String getDiscordButtonText() {
        return get("discord.button-text");
    }

    public String getDiscordHoverText() {
        return get("discord.hover-text");
    }

    // ── Join / Leave ─────────────────────────────────────────────

    public String getJoinMessage() {
        return get("join.message");
    }

    public String getLeaveMessage() {
        return get("leave.message");
    }

    // ── Gamemode ─────────────────────────────────────────────────

    public String getGamemodePlayersOnly() {
        return get("gamemode.players-only");
    }

    public String getGamemodeUsage() {
        return get("gamemode.usage");
    }

    public String getGamemodeChanged(String gamemodeName) {
        return get("gamemode.changed").replace("{gamemode}", gamemodeName);
    }

    public String getGamemodeName(GameMode mode) {
        return switch (mode) {
            case SURVIVAL  -> get("gamemode.modes.survival");
            case CREATIVE  -> get("gamemode.modes.creative");
            case ADVENTURE -> get("gamemode.modes.adventure");
            case SPECTATOR -> get("gamemode.modes.spectator");
        };
    }

    // ── Chat ─────────────────────────────────────────────────────

    public String getChatSeparator() {
        return get("chat.separator");
    }

    // ── Msg ──────────────────────────────────────────────────────

    public String getMsgPlayersOnly() {
        return get("msg.players-only");
    }

    public String getMsgUsage() {
        return get("msg.usage");
    }

    public String getReplyUsage() {
        return get("msg.reply-usage");
    }

    public String getMsgPlayerNotFound(String player) {
        return get("msg.player-not-found").replace("{player}", player);
    }

    public String getMsgCannotSelf() {
        return get("msg.cannot-self");
    }

    public String getMsgNoReplyTarget() {
        return get("msg.no-reply-target");
    }

    public String getMsgFormatSent(String player) {
        return get("msg.format-sent").replace("{player}", player);
    }

    public String getMsgFormatReceived(String player) {
        return get("msg.format-received").replace("{player}", player);
    }

    // ── Spawner ───────────────────────────────────────────────────

    public String getSpawnerBreakDenied() {
        return get("spawner.break-denied");
    }

    // ── Gamemode ─────────────────────────────────────────────────

    public String getGamemodeNoPermission() {
        return get("gamemode.no-permission");
    }

    // ── Spawn ─────────────────────────────────────────────────────

    public String getSpawnPlayersOnly() {
        return get("spawn.players-only");
    }

    public String getSpawnNoPermission() {
        return get("spawn.no-permission");
    }

    public String getSpawnNotSet() {
        return get("spawn.not-set");
    }

    public String getSpawnTeleported() {
        return get("spawn.teleported");
    }

    public String getSpawnSet() {
        return get("spawn.set");
    }

    public String getSpawnTitle() {
        return get("spawn.title");
    }

    public String getSpawnStandStill() {
        return get("spawn.stand-still");
    }

    public String getSpawnWarmupCancelled() {
        return get("spawn.warmup-cancelled");
    }

    public String getSpawnCooldown(long seconds) {
        return get("spawn.cooldown").replace("{seconds}", String.valueOf(seconds));
    }

    // ── Join ──────────────────────────────────────────────────────

    public String getJoinTitleMain() {
        return get("join.title-main");
    }

    public String getJoinTitleSub() {
        return get("join.title-sub");
    }

    // ── Heal ──────────────────────────────────────────────────────

    public String getHealPlayersOnly() {
        return get("heal.players-only");
    }

    public String getHealNoPermission() {
        return get("heal.no-permission");
    }

    public String getHealSuccess() {
        return get("heal.success");
    }

    // ── Feed ──────────────────────────────────────────────────────

    public String getFeedPlayersOnly() {
        return get("feed.players-only");
    }

    public String getFeedNoPermission() {
        return get("feed.no-permission");
    }

    public String getFeedSuccess() {
        return get("feed.success");
    }

    // ── Home ──────────────────────────────────────────────────────

    public String getHomePlayersOnly() {
        return get("home.players-only");
    }

    public String getHomeNoPermission() {
        return get("home.no-permission");
    }

    public String getHomeNotFound(String name) {
        return get("home.not-found").replace("{name}", name);
    }

    public String getHomeTeleported(String name) {
        return get("home.teleported").replace("{name}", name);
    }

    public String getHomeSet(String name) {
        return get("home.set").replace("{name}", name);
    }

    public String getHomeDeleted(String name) {
        return get("home.deleted").replace("{name}", name);
    }

    public String getHomeAllDeleted() {
        return get("home.all-deleted");
    }

    public String getHomeLimitReached(int limit) {
        return get("home.limit-reached").replace("{limit}", String.valueOf(limit));
    }

    public String getHomeInvalidName() {
        return get("home.invalid-name");
    }

    public String getHomeAlreadyExistsConfirm(String name) {
        return get("home.already-exists-confirm").replace("{name}", name);
    }

    public String getHomeAlreadyExistsRename(String name) {
        return get("home.already-exists-rename").replace("{name}", name);
    }

    public String getHomeListEmpty() {
        return get("home.list-empty");
    }

    public String getHomeTitleMain(String name) {
        return get("home.title-main").replace("{name}", name);
    }

    public String getHomeStandStill() {
        return get("home.stand-still");
    }

    public String getHomeWarmupCancelled() {
        return get("home.warmup-cancelled");
    }

    public String getHomeCooldown(long seconds) {
        return get("home.cooldown").replace("{seconds}", String.valueOf(seconds));
    }

    public String getHomeRenamed(String oldName, String newName) {
        return get("home.renamed").replace("{old}", oldName).replace("{new}", newName);
    }

    public String getHomeRelocated(String name) {
        return get("home.relocated").replace("{name}", name);
    }

    public String getHomesGuiTitle() {
        return get("home.gui-title");
    }

    public String getHomesGuiClickToTeleport() {
        return get("home.gui-click");
    }

    public String getDelhomeUsage() {
        return get("home.delhome-usage");
    }

    public String getDelhomeAllConfirm() {
        return get("home.delhome-all-confirm");
    }

    public String getEdithomeUsage() {
        return get("home.edithome-usage");
    }

    // ── General ───────────────────────────────────────────────────

    public String getNoPermission() {
        return get("no-permission");
    }

    // ── Time ──────────────────────────────────────────────────────

    public String getTimePlayersOnly() {
        return get("time.players-only");
    }

    public String getTimeNoPermission() {
        return get("time.no-permission");
    }

    public String getTimeDay() {
        return get("time.day");
    }

    public String getTimeNight() {
        return get("time.night");
    }

    public String getTimeSun() {
        return get("time.sun");
    }

    // ── Spawn Removed ─────────────────────────────────────────────

    public String getSpawnRemoved() {
        return get("spawn.removed");
    }

    // ── TPA ───────────────────────────────────────────────────────

    public String getTpaPlayersOnly() {
        return get("tpa.players-only");
    }

    public String getTpaUsage() {
        return get("tpa.usage");
    }

    public String getTpaCannotSelf() {
        return get("tpa.cannot-self");
    }

    public String getTpaSent(String player) {
        return get("tpa.sent").replace("{player}", player);
    }

    public String getTpaReceived(String player) {
        return get("tpa.received").replace("{player}", player);
    }

    public String getTpaAcceptButton() {
        return get("tpa.accept-button");
    }

    public String getTpaDenyButton() {
        return get("tpa.deny-button");
    }

    public String getTpaAccepted(String player) {
        return get("tpa.accepted").replace("{player}", player);
    }

    public String getTpaAcceptedTarget(String player) {
        return get("tpa.accepted-target").replace("{player}", player);
    }

    public String getTpaDenied(String player) {
        return get("tpa.denied").replace("{player}", player);
    }

    public String getTpaDeniedTarget(String player) {
        return get("tpa.denied-target").replace("{player}", player);
    }

    public String getTpaNoPending() {
        return get("tpa.no-pending");
    }

    public String getTpaRequesterOffline() {
        return get("tpa.requester-offline");
    }

    public String getTpaUnknownPlayer() {
        return get("tpa.unknown-player");
    }

    public String getTpaStandStill() {
        return get("tpa.stand-still");
    }

    public String getTpaWarmupCancelled() {
        return get("tpa.warmup-cancelled");
    }

    // ── TP Admin ──────────────────────────────────────────────────

    public String getTpAdminUsage() {
        return get("tp.admin-usage");
    }

    public String getTpAdminTeleported(String player) {
        return get("tp.admin-teleported").replace("{player}", player);
    }

    // ── Vanish ────────────────────────────────────────────────────

    public String getVanishPlayersOnly() {
        return get("vanish.players-only");
    }

    public String getVanishOn() {
        return get("vanish.enabled");
    }

    public String getVanishOff() {
        return get("vanish.disabled");
    }

    // ── Broadcast ─────────────────────────────────────────────────

    public String getBroadcastUsage() {
        return get("broadcast.usage");
    }

    // ── AFK ───────────────────────────────────────────────────────

    public String getAfkPlayersOnly() {
        return get("afk.players-only");
    }

    public String getAfkEnter(String player) {
        return get("afk.enter").replace("{player}", player);
    }

    public String getAfkLeave(String player) {
        return get("afk.leave").replace("{player}", player);
    }

    public String getAfkKicked() {
        return get("afk.kicked");
    }

    // ── Playtime ──────────────────────────────────────────────────

    public String getPlaytimePlayersOnly() {
        return get("playtime.players-only");
    }

    public String getPlaytimeInfo(String player, String total, String session) {
        return get("playtime.info")
                .replace("{player}", player)
                .replace("{total}", total)
                .replace("{session}", session);
    }

    // ── Vote ──────────────────────────────────────────────────────

    public String getVoteHeader() {
        return get("vote.header");
    }

    public String getVoteNoLinks() {
        return get("vote.no-links");
    }

    public String getVoteSite(int number) {
        return get("vote.site").replace("{number}", String.valueOf(number));
    }

    public String getVoteBroadcast(String player, int crates) {
        return get("vote.broadcast").replace("{player}", player).replace("{crates}", String.valueOf(crates));
    }

    public String getVoteReceived(int crates) {
        return get("vote.received").replace("{crates}", String.valueOf(crates));
    }

    public String getVotePending(int crates) {
        return get("vote.pending").replace("{crates}", String.valueOf(crates));
    }

    // ── Crates ────────────────────────────────────────────────────

    public String getCratesPlayersOnly() {
        return get("crates.players-only");
    }

    public String getCratesNone() {
        return get("crates.none");
    }

    public String getCratesNoRewards() {
        return get("crates.no-rewards");
    }

    public String getCratesUsage() {
        return get("crates.usage");
    }

    public String getCratesGiven(String player, int amount) {
        return get("crates.given").replace("{player}", player).replace("{amount}", String.valueOf(amount));
    }

    public String getCratesGivenAll(int amount) {
        return get("crates.given-all").replace("{amount}", String.valueOf(amount));
    }

    public String getCratesReceived(int amount) {
        return get("crates.received").replace("{amount}", String.valueOf(amount));
    }

    public String getCratesInvalidAmount() {
        return get("crates.invalid-amount");
    }

    public String getCratesDailyLogin() {
        return get("crates.daily-login");
    }

    public String getCratesReceivedAfk() {
        return get("crates.received-afk");
    }

    public String getCratesRewardXp(int amount) {
        return get("crates.reward-xp").replace("{amount}", String.valueOf(amount));
    }

    public String getCratesRewardTotem() {
        return get("crates.reward-totem");
    }

    public String getCratesRewardTool() {
        return get("crates.reward-tool");
    }

    public String getCratesRewardGold(int amount) {
        return get("crates.reward-gold").replace("{amount}", String.valueOf(amount));
    }

    // ── XP ────────────────────────────────────────────────────────

    public String getXpUsage() {
        return get("xp.usage");
    }

    public String getXpInfo(String player, int xp, int level) {
        return get("xp.info")
                .replace("{player}", player)
                .replace("{xp}", String.valueOf(xp))
                .replace("{level}", String.valueOf(level));
    }

    public String getXpReset(String player) {
        return get("xp.reset").replace("{player}", player);
    }

    public String getXpAdd(String player, int amount) {
        return get("xp.add").replace("{player}", player).replace("{amount}", String.valueOf(amount));
    }

    public String getXpRemove(String player, int amount) {
        return get("xp.remove").replace("{player}", player).replace("{amount}", String.valueOf(amount));
    }

    public String getXpInvalidAmount() {
        return get("xp.invalid-amount");
    }

    // ── Portal Lock ───────────────────────────────────────────────

    public String getPortalNetherLocked(String time) {
        return get("portal.nether-locked").replace("{time}", time);
    }

    public String getPortalNetherDenied(String time) {
        return get("portal.nether-denied").replace("{time}", time);
    }

    public String getPortalEndLocked(String time) {
        return get("portal.end-locked").replace("{time}", time);
    }

    public String getPortalEndDenied(String time) {
        return get("portal.end-denied").replace("{time}", time);
    }

    // ── Reset Time ────────────────────────────────────────────────

    public String getResetTimeUsage() {
        return get("resettime.usage");
    }

    public String getResetTimeSuccess(String portal) {
        return get("resettime.success").replace("{portal}", portal);
    }

    // ── Diamond Craft ─────────────────────────────────────────────

    public String getDiamondCraftNoXp(int cost, int current) {
        return get("diamond-craft.no-xp")
                .replace("{cost}", String.valueOf(cost))
                .replace("{current}", String.valueOf(current));
    }

    public String getDiamondCraftNoShiftClick() {
        return get("diamond-craft.no-shift-click");
    }

    // ── Sleep ─────────────────────────────────────────────────────

    public String getSleepProgress(long sleeping, long total) {
        return get("sleep.progress")
                .replace("{sleeping}", String.valueOf(sleeping))
                .replace("{total}", String.valueOf(total));
    }

    public String getSleepSkipped() {
        return get("sleep.skipped");
    }

    // ── Special Tool ──────────────────────────────────────────────

    public String getSpecialToolExpired() {
        return get("special-tool.expired");
    }

    // ── GiveItem ──────────────────────────────────────────────────

    public String getGiveItemUsage() {
        return get("giveitem.usage");
    }

    public String getGiveItemSuccess(String type, String player, int amount) {
        return get("giveitem.success")
                .replace("{type}", type)
                .replace("{player}", player)
                .replace("{amount}", String.valueOf(amount));
    }

    // ── FSMPCore ──────────────────────────────────────────────────

    public String getFsmpcoreUsage() {
        return get("fsmpcore.usage");
    }

    // ── FSMP ──────────────────────────────────────────────────────

    public String getFsmpReloadSuccess() {
        return get("fsmp.reload-success");
    }

    public String getFsmpNoPermission() {
        return get("fsmp.no-permission");
    }

    public String getFsmpUsage() {
        return get("fsmp.usage");
    }
}
