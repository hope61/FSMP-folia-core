package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.HomeManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EditHomeCommand implements CommandExecutor, TabCompleter {
    private final LangManager lang;
    private final HomeManager homeManager;

    public EditHomeCommand(LangManager lang, HomeManager homeManager) {
        this.lang = lang;
        this.homeManager = homeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getHomePlayersOnly(), NamedTextColor.DARK_AQUA));
            return true;
        }

        if (!player.hasPermission("fsmp.home.edit")) {
            player.sendMessage(Component.text(lang.getHomeNoPermission(), NamedTextColor.DARK_AQUA));
            return true;
        }

        // /edithome <name> rename <newname>
        // /edithome <name> relocate
        if (args.length < 2) {
            player.sendMessage(Component.text(lang.getEdithomeUsage(), NamedTextColor.DARK_AQUA));
            return true;
        }

        String name = args[0];
        String action = args[1].toLowerCase();

        if (!homeManager.hasHome(player.getUniqueId(), name)) {
            player.sendMessage(Component.text(lang.getHomeNotFound(name), NamedTextColor.DARK_AQUA));
            return true;
        }

        switch (action) {
            case "rename" -> {
                if (args.length < 3) {
                    player.sendMessage(Component.text(lang.getEdithomeUsage(), NamedTextColor.DARK_AQUA));
                    return true;
                }
                String newName = args[2];
                if (!homeManager.isValidName(newName)) {
                    player.sendMessage(Component.text(lang.getHomeInvalidName(), NamedTextColor.DARK_AQUA));
                    return true;
                }
                if (homeManager.hasHome(player.getUniqueId(), newName)) {
                    player.sendMessage(Component.text(lang.getHomeAlreadyExistsRename(newName), NamedTextColor.DARK_AQUA));
                    return true;
                }
                homeManager.renameHome(player.getUniqueId(), name, newName);
                player.sendMessage(Component.text(lang.getHomeRenamed(name, newName), NamedTextColor.AQUA));
            }
            case "relocate" -> {
                homeManager.setHome(player.getUniqueId(), name, player.getLocation());
                player.sendMessage(Component.text(lang.getHomeRelocated(name), NamedTextColor.AQUA));
            }
            default -> player.sendMessage(Component.text(lang.getEdithomeUsage(), NamedTextColor.DARK_AQUA));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        if (!player.hasPermission("fsmp.home.edit")) return List.of();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return homeManager.getHomeNames(player.getUniqueId())
                    .stream().filter(h -> h.toLowerCase().startsWith(input)).toList();
        }
        if (args.length == 2) {
            String input = args[1].toLowerCase();
            return List.of("rename", "relocate").stream().filter(a -> a.startsWith(input)).toList();
        }
        return List.of();
    }
}
