package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TpAdminCommand implements CommandExecutor, TabCompleter {
    private final LangManager lang;

    public TpAdminCommand(LangManager lang) {
        this.lang = lang;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getTpaPlayersOnly(), NamedTextColor.DARK_PURPLE));
            return true;
        }
        if (!player.hasPermission("fsmp.tp.admin")) {
            player.sendMessage(Component.text(lang.getNoPermission(), NamedTextColor.DARK_PURPLE));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Component.text(lang.getTpAdminUsage(), NamedTextColor.DARK_PURPLE));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(Component.text(lang.getMsgPlayerNotFound(args[0]), NamedTextColor.DARK_PURPLE));
            return true;
        }
        player.teleportAsync(target.getLocation()).thenAccept(success -> {
            if (success) {
                player.sendMessage(Component.text(lang.getTpAdminTeleported(target.getName()), NamedTextColor.DARK_PURPLE));
            }
        });
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.tp.admin")) return List.of();
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
