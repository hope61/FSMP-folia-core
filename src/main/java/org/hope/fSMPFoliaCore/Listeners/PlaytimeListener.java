package org.hope.fSMPFoliaCore.Listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.CrateManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.PlaytimeManager;

public class PlaytimeListener implements Listener {
    private final FSMPFoliaCore plugin;
    private final PlaytimeManager playtimeManager;
    private final CrateManager crateManager;
    private final LangManager lang;

    public PlaytimeListener(FSMPFoliaCore plugin, PlaytimeManager playtimeManager, CrateManager crateManager, LangManager lang) {
        this.plugin = plugin;
        this.playtimeManager = playtimeManager;
        this.crateManager = crateManager;
        this.lang = lang;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playtimeManager.onJoin(player.getUniqueId());

        // Daily login crate — schedule on player's region thread for inventory ops
        if (crateManager.canClaimDaily(player.getUniqueId())) {
            crateManager.recordDailyClaim(player.getUniqueId());
            player.getScheduler().runDelayed(plugin, t -> {
                crateManager.addCrates(player, 1);
                player.sendMessage(Component.text(lang.getCratesDailyLogin(), lang.primary()));
            }, null, 1L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playtimeManager.onQuit(event.getPlayer().getUniqueId());
    }
}
