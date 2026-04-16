package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GamemodeCommand implements CommandExecutor, TabCompleter {
    private final LangManager lang;

    public GamemodeCommand(LangManager lang) {
        this.lang = lang;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getGamemodePlayersOnly(), lang.secondary()));
            return true;
        }

        if (!player.hasPermission("fsmp.gamemode")) {
            player.sendMessage(Component.text(lang.getGamemodeNoPermission(), lang.secondary()));
            return true;
        }

        GameMode mode = resolveGamemode(label, args);

        if (mode == null) {
            player.sendMessage(Component.text(lang.getGamemodeUsage(), lang.secondary()));
            return true;
        }

        player.setGameMode(mode);
        player.sendMessage(Component.text(lang.getGamemodeChanged(lang.getGamemodeName(mode)), lang.primary()));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.gamemode")) return List.of();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return List.of("survival", "creative", "adventure", "spectator")
                    .stream().filter(s -> s.startsWith(input)).toList();
        }
        return List.of();
    }

    private GameMode resolveGamemode(String label, String[] args) {
        switch (label.toLowerCase()) {
            case "gms":  return GameMode.SURVIVAL;
            case "gmc":  return GameMode.CREATIVE;
            case "gma":  return GameMode.ADVENTURE;
            case "gmsp": return GameMode.SPECTATOR;
        }

        if (args.length == 0) return null;

        switch (args[0].toLowerCase()) {
            case "survival":  case "s":  case "0": return GameMode.SURVIVAL;
            case "creative":  case "c":  case "1": return GameMode.CREATIVE;
            case "adventure": case "a":  case "2": return GameMode.ADVENTURE;
            case "spectator": case "sp": case "3": return GameMode.SPECTATOR;
            default: return null;
        }
    }
}
