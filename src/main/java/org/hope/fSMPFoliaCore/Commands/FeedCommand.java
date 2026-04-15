package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;

public class FeedCommand implements CommandExecutor {
    private final LangManager lang;

    public FeedCommand(LangManager lang) {
        this.lang = lang;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getFeedPlayersOnly(), NamedTextColor.DARK_PURPLE));
            return true;
        }

        if (!player.hasPermission("fsmp.feed")) {
            player.sendMessage(Component.text(lang.getFeedNoPermission(), NamedTextColor.DARK_PURPLE));
            return true;
        }

        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.sendMessage(Component.text(lang.getFeedSuccess(), NamedTextColor.LIGHT_PURPLE));
        return true;
    }
}
