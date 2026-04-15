package org.hope.fSMPFoliaCore.Listeners;

import com.vexsoftware.votifier.model.VotifierEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.CrateManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.VoteStorageManager;

public class VoteListener implements Listener {
    private final FSMPFoliaCore plugin;
    private final ConfigManager config;
    private final LangManager lang;
    private final CrateManager crateManager;
    private final VoteStorageManager voteStorage;

    public VoteListener(FSMPFoliaCore plugin, ConfigManager config, LangManager lang,
                        CrateManager crateManager, VoteStorageManager voteStorage) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.crateManager = crateManager;
        this.voteStorage = voteStorage;
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        // VotifierEvent fires async — schedule everything on the global region thread
        String username = event.getVote().getUsername();
        int crates = config.getVoteCratesPerVote();

        plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> {
            Bukkit.broadcast(Component.text(lang.getVoteBroadcast(username, crates), NamedTextColor.AQUA));

            Player player = Bukkit.getPlayerExact(username);
            if (player != null) {
                // Online — give crates on player's region thread
                player.getScheduler().run(plugin, t -> {
                    crateManager.addCrates(player, crates);
                    player.sendMessage(Component.text(lang.getVoteReceived(crates), NamedTextColor.AQUA));
                }, null);
            } else {
                // Offline — persist so they get crates on next login
                plugin.getServer().getAsyncScheduler().runNow(plugin, t ->
                        voteStorage.addPending(username, crates));
            }
        });
    }
}
