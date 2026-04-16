package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.AfkManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;

public class AfkCommand implements CommandExecutor {
    private final LangManager lang;
    private final AfkManager afkManager;

    public AfkCommand(LangManager lang, AfkManager afkManager) {
        this.lang = lang;
        this.afkManager = afkManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getAfkPlayersOnly(), lang.secondary()));
            return true;
        }
        if (!player.hasPermission("fsmp.afk")) {
            player.sendMessage(Component.text(lang.getNoPermission(), lang.secondary()));
            return true;
        }

        boolean nowAfk = !afkManager.isAfk(player.getUniqueId());
        afkManager.setAfk(player.getUniqueId(), nowAfk);

        if (nowAfk) {
            Bukkit.broadcast(Component.text(
                    lang.getAfkEnter(player.getName()), lang.secondary()));
        } else {
            afkManager.updateActivity(player);
            Bukkit.broadcast(Component.text(
                    lang.getAfkLeave(player.getName()), lang.secondary()));
        }
        return true;
    }
}
