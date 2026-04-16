package org.hope.fSMPFoliaCore;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import org.hope.fSMPFoliaCore.Commands.*;
import org.hope.fSMPFoliaCore.Listeners.*;
import org.hope.fSMPFoliaCore.Managers.*;
import org.hope.fSMPFoliaCore.hooks.VaultHook;

public final class FSMPFoliaCore extends JavaPlugin {

    private ConfigManager configManager;
    private LangManager langManager;
    private MessageManager messageManager;
    private SpawnManager spawnManager;
    private HomeManager homeManager;
    private AnnouncementManager announcementManager;
    private PlaytimeManager playtimeManager;
    private VanishManager vanishManager;
    private AfkManager afkManager;
    private TpaManager tpaManager;
    private PortalLockManager portalLockManager;
    private CrateManager crateManager;
    private SpecialToolManager specialToolManager;
    private SoundManager soundManager;
    private VoteStorageManager voteStorageManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        langManager = new LangManager(this);
        messageManager = new MessageManager();
        spawnManager = new SpawnManager(this);
        homeManager = new HomeManager(this);
        playtimeManager = new PlaytimeManager(this);
        vanishManager = new VanishManager(this);
        afkManager = new AfkManager();
        tpaManager = new TpaManager();
        portalLockManager = new PortalLockManager(this);
        crateManager = new CrateManager(this, langManager);
        specialToolManager = new SpecialToolManager(this, langManager);
        soundManager = new SoundManager(configManager);
        voteStorageManager = new VoteStorageManager(this);

        // Announcement manager
        announcementManager = new AnnouncementManager(this, configManager, langManager);
        announcementManager.setAfkManager(afkManager);
        announcementManager.start();
        announcementManager.startVoicechat();

// ── Commands ─────────────────────────────────────────────────
        MsgCommand msgCommand = new MsgCommand(configManager, langManager, messageManager);
        SpawnCommand spawnCommand = new SpawnCommand(this, configManager, langManager, spawnManager);
        HomeCommand homeCommand = new HomeCommand(this, configManager, langManager, homeManager);
        GamemodeCommand gamemodeCommand = new GamemodeCommand(langManager);
        FsmpCommand fsmpCommand = new FsmpCommand(configManager, langManager, announcementManager);

        getCommand("discord").setExecutor(new DiscordCommand(configManager, langManager));
        getCommand("gamemode").setExecutor(gamemodeCommand);
        getCommand("gamemode").setTabCompleter(gamemodeCommand);
        for (String alias : new String[]{"gm", "gms", "gmc", "gma", "gmsp"}) {
            getCommand(alias).setExecutor(gamemodeCommand);
            getCommand(alias).setTabCompleter(gamemodeCommand);
        }
        getCommand("msg").setExecutor(msgCommand);
        getCommand("r").setExecutor(new ReplyCommand(configManager, langManager, messageManager, msgCommand));
        getCommand("spawn").setExecutor(spawnCommand);
        getCommand("setspawn").setExecutor(new SetSpawnCommand(langManager, spawnManager));
        getCommand("removespawn").setExecutor(new RemoveSpawnCommand(langManager, spawnManager));
        getCommand("heal").setExecutor(new HealCommand(langManager));
        getCommand("feed").setExecutor(new FeedCommand(langManager));
        getCommand("home").setExecutor(homeCommand);
        getCommand("home").setTabCompleter(homeCommand);
        getCommand("sethome").setExecutor(new SetHomeCommand(langManager, homeManager));

        DelHomeCommand delHomeCommand = new DelHomeCommand(langManager, homeManager);
        getCommand("delhome").setExecutor(delHomeCommand);
        getCommand("delhome").setTabCompleter(delHomeCommand);

        EditHomeCommand editHomeCommand = new EditHomeCommand(langManager, homeManager);
        getCommand("edithome").setExecutor(editHomeCommand);
        getCommand("edithome").setTabCompleter(editHomeCommand);

        getCommand("homes").setExecutor(new HomesCommand(this, langManager, homeManager));
        getCommand("fsmp").setExecutor(fsmpCommand);
        getCommand("fsmp").setTabCompleter(fsmpCommand);

        // Time / Weather
        getCommand("day").setExecutor(new TimeCommand(this, langManager, TimeCommand.Mode.DAY));
        getCommand("night").setExecutor(new TimeCommand(this, langManager, TimeCommand.Mode.NIGHT));
        getCommand("sun").setExecutor(new TimeCommand(this, langManager, TimeCommand.Mode.SUN));

        // TPA
        TpaCommand tpaCommand = new TpaCommand(langManager, tpaManager, vanishManager);
        getCommand("tpa").setExecutor(tpaCommand);
        getCommand("tpa").setTabCompleter(tpaCommand);
        TpAcceptCommand tpAcceptCommand = new TpAcceptCommand(this, configManager, langManager, tpaManager, soundManager);
        getCommand("tpaccept").setExecutor(tpAcceptCommand);
        getCommand("tpadeny").setExecutor(new TpDenyCommand(langManager, tpaManager, soundManager));

        TpAdminCommand tpAdminCommand = new TpAdminCommand(langManager);
        getCommand("tp").setExecutor(tpAdminCommand);
        getCommand("tp").setTabCompleter(tpAdminCommand);

        // Vanish
        getCommand("vanish").setExecutor(new VanishCommand(langManager, vanishManager));

        // Broadcast
        getCommand("bc").setExecutor(new BroadcastCommand(langManager));

        // AFK
        getCommand("afk").setExecutor(new AfkCommand(langManager, afkManager));

        // Playtime
        PlaytimeCommand playtimeCommand = new PlaytimeCommand(langManager, playtimeManager, vanishManager);
        getCommand("playtime").setExecutor(playtimeCommand);
        getCommand("playtime").setTabCompleter(playtimeCommand);

        // Vote
        getCommand("vote").setExecutor(new VoteCommand(langManager, configManager));

        // Crates
        CratesCommand cratesCommand = new CratesCommand(langManager, crateManager);
        getCommand("crates").setExecutor(cratesCommand);
        getCommand("crates").setTabCompleter(cratesCommand);

        // Reset Time
        ResetTimeCommand resetTimeCommand = new ResetTimeCommand(langManager, portalLockManager);
        getCommand("resettime").setExecutor(resetTimeCommand);
        getCommand("resettime").setTabCompleter(resetTimeCommand);

        // XP
        XpCommand xpCommand = new XpCommand(langManager);
        getCommand("xp").setExecutor(xpCommand);
        getCommand("xp").setTabCompleter(xpCommand);

        // FSMPCore (special tools giveitem)
        FsmpcoreCommand fsmpcoreCommand = new FsmpcoreCommand(langManager, configManager, specialToolManager);
        getCommand("fsmpcore").setExecutor(fsmpcoreCommand);
        getCommand("fsmpcore").setTabCompleter(fsmpcoreCommand);

        // ── Listeners ────────────────────────────────────────────────
        getServer().getPluginManager().registerEvents(new CommandHideListener(), this);
        getServer().getPluginManager().registerEvents(
                new JoinListener(this, configManager, langManager, spawnManager, crateManager, voteStorageManager), this);
        getServer().getPluginManager().registerEvents(
                new LeaveListener(configManager, langManager), this);
        RespawnListener respawnListener = new RespawnListener(this, configManager, spawnManager);
        getServer().getPluginManager().registerEvents(respawnListener, this);
        getServer().getPluginManager().registerEvents(new SpawnerBreakListener(langManager), this);
        getServer().getPluginManager().registerEvents(
                new HomesGuiListener(this, langManager, homeManager), this);

        // Vanish
        getServer().getPluginManager().registerEvents(new VanishListener(vanishManager), this);

        // AFK
        AfkListener afkListener = new AfkListener(this, afkManager, configManager, langManager, crateManager);
        getServer().getPluginManager().registerEvents(afkListener, this);
        afkListener.startTask();

        // Portal Lock
        PortalLockListener portalLockListener = new PortalLockListener(
                this, configManager, langManager, portalLockManager);
        getServer().getPluginManager().registerEvents(portalLockListener, this);
        portalLockListener.startBroadcastTask();

        // Sleep Skip
        getServer().getPluginManager().registerEvents(
                new SleepSkipListener(this, configManager, langManager, soundManager), this);

        // Diamond Crafting XP cost
        getServer().getPluginManager().registerEvents(
                new DiamondCraftListener(configManager, langManager), this);

        // Special Tools
        getServer().getPluginManager().registerEvents(
                new SpecialToolListener(this, specialToolManager, langManager), this);

        // Crates (right-click to open)
        getServer().getPluginManager().registerEvents(
                new CrateListener(crateManager, configManager, langManager, specialToolManager, soundManager), this);

        // Playtime + daily crate
        getServer().getPluginManager().registerEvents(
                new PlaytimeListener(this, playtimeManager, crateManager, langManager), this);

        // Clean up on disconnect / death
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                UUID uuid = event.getPlayer().getUniqueId();
                messageManager.removePlayer(uuid);
                spawnCommand.cancelWarmup(uuid);
                homeCommand.cancelWarmup(uuid);
                respawnListener.removePending(uuid);
                homeManager.clearPendingOverwrite(uuid);
                delHomeCommand.clearPending(uuid);
                tpaManager.cancelOutgoing(uuid);
                tpaManager.cancelIncoming(uuid);
                tpAcceptCommand.cancelWarmup(uuid);
            }

            @EventHandler
            public void onDeath(PlayerDeathEvent event) {
                UUID uuid = event.getPlayer().getUniqueId();
                spawnCommand.cancelWarmup(uuid);
                homeCommand.cancelWarmup(uuid);
                tpAcceptCommand.cancelWarmup(uuid);
            }
        }, this);

        getServer().getGlobalRegionScheduler().runDelayed(this, task -> {
            VaultHook vault = getServer().getPluginManager().isPluginEnabled("Vault")
                    ? new VaultHook()
                    : null;

            if (vault != null && vault.isAvailable()) {
                getLogger().info("Vault Chat provider found — prefixes and suffixes enabled.");
            } else {
                getLogger().warning("Vault Chat provider not found — chat will show without prefix/suffix.");
            }

            getServer().getPluginManager().registerEvents(
                    new ChatListener(langManager, vault), this);

            // Vote (soft-depend — Votifier enables after us, so check here)
            if (getServer().getPluginManager().isPluginEnabled("NuVotifier")
                    || getServer().getPluginManager().isPluginEnabled("Votifier")) {
                try {
                    getServer().getPluginManager().registerEvents(
                            new VoteListener(this, configManager, langManager, crateManager, voteStorageManager), this);
                    getLogger().info("Votifier found — vote rewards enabled.");
                } catch (NoClassDefFoundError e) {
                    getLogger().warning("Votifier classes not found — vote rewards disabled.");
                }
            } else {
                getLogger().warning("Votifier not found — vote rewards disabled.");
            }
        }, 1L);
    }

    @Override
    public void onDisable() {
        if (announcementManager != null) {
            announcementManager.stop();
            announcementManager.stopVoicechat();
        }
    }
}
