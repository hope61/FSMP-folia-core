package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.HomeManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;

public class SetHomeCommand implements CommandExecutor {
    private final LangManager lang;
    private final HomeManager homeManager;

    public SetHomeCommand(LangManager lang, HomeManager homeManager) {
        this.lang = lang;
        this.homeManager = homeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getHomePlayersOnly(), lang.secondary()));
            return true;
        }

        if (!player.hasPermission("fsmp.home")) {
            player.sendMessage(Component.text(lang.getHomeNoPermission(), lang.secondary()));
            return true;
        }

        String name = args.length > 0 ? args[0] : "home";

        if (!homeManager.isValidName(name)) {
            player.sendMessage(Component.text(lang.getHomeInvalidName(), lang.secondary()));
            return true;
        }

        // Check overwrite confirmation
        if (homeManager.hasHome(player.getUniqueId(), name)) {
            String pending = homeManager.getPendingOverwrite(player.getUniqueId());
            if (name.equals(pending)) {
                // Confirmed — overwrite
                homeManager.clearPendingOverwrite(player.getUniqueId());
                homeManager.setHome(player.getUniqueId(), name, player.getLocation());
                player.sendMessage(Component.text(lang.getHomeSet(name), lang.primary()));
            } else {
                // Ask for confirmation
                homeManager.setPendingOverwrite(player.getUniqueId(), name);
                player.sendMessage(Component.text(lang.getHomeAlreadyExistsConfirm(name), lang.warning()));
            }
            return true;
        }

        // Check home limit
        int limit = homeManager.getHomeLimit(player);
        int current = homeManager.getHomeNames(player.getUniqueId()).size();
        if (current >= limit) {
            player.sendMessage(Component.text(lang.getHomeLimitReached(limit), lang.secondary()));
            return true;
        }

        homeManager.clearPendingOverwrite(player.getUniqueId());
        homeManager.setHome(player.getUniqueId(), name, player.getLocation());
        player.sendMessage(Component.text(lang.getHomeSet(name), lang.primary()));
        return true;
    }
}
