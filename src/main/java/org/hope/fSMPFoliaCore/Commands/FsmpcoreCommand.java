package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.SpecialToolManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FsmpcoreCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("giveitem");

    private final LangManager lang;
    private final GiveItemCommand giveItemCommand;

    public FsmpcoreCommand(LangManager lang, ConfigManager config, SpecialToolManager toolManager) {
        this.lang = lang;
        this.giveItemCommand = new GiveItemCommand(lang, toolManager, config);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.admin")) {
            sender.sendMessage(Component.text(lang.getNoPermission(), NamedTextColor.DARK_PURPLE));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text(lang.getFsmpcoreUsage(), NamedTextColor.DARK_PURPLE));
            return true;
        }
        if (args[0].equalsIgnoreCase("giveitem")) {
            return giveItemCommand.onCommand(sender, command, label, args);
        }
        sender.sendMessage(Component.text(lang.getFsmpcoreUsage(), NamedTextColor.DARK_PURPLE));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.admin")) return List.of();
        if (args.length == 1) return SUBCOMMANDS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        if (args.length >= 2 && args[0].equalsIgnoreCase("giveitem")) {
            return giveItemCommand.onTabComplete(sender, command, label, args);
        }
        return List.of();
    }
}
