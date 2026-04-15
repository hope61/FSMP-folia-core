package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.VanishManager;
import org.jetbrains.annotations.NotNull;

public class VanishCommand implements CommandExecutor {
    private final LangManager lang;
    private final VanishManager vanishManager;

    public VanishCommand(LangManager lang, VanishManager vanishManager) {
        this.lang = lang;
        this.vanishManager = vanishManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getVanishPlayersOnly(), NamedTextColor.DARK_AQUA));
            return true;
        }
        if (!player.hasPermission("fsmp.vanish")) {
            player.sendMessage(Component.text(lang.getNoPermission(), NamedTextColor.DARK_AQUA));
            return true;
        }

        if (vanishManager.isVanished(player)) {
            vanishManager.unvanish(player);
            player.sendMessage(Component.text(lang.getVanishOff(), NamedTextColor.DARK_AQUA));
        } else {
            vanishManager.vanish(player);
            player.sendMessage(Component.text(lang.getVanishOn(), NamedTextColor.DARK_AQUA));
        }
        return true;
    }
}
