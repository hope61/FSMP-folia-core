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

public class XpCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("reset", "add", "remove", "info");
    private final LangManager lang;

    public XpCommand(LangManager lang) {
        this.lang = lang;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.xp")) {
            sender.sendMessage(Component.text(lang.getNoPermission(), NamedTextColor.DARK_PURPLE));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text(lang.getXpUsage(), NamedTextColor.DARK_PURPLE));
            return true;
        }

        String sub = args[0].toLowerCase();
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text(lang.getMsgPlayerNotFound(args[1]), NamedTextColor.DARK_PURPLE));
            return true;
        }

        switch (sub) {
            case "info" -> {
                sender.sendMessage(Component.text(
                        lang.getXpInfo(target.getName(), target.getTotalExperience(), target.getLevel()),
                        NamedTextColor.LIGHT_PURPLE));
            }
            case "reset" -> {
                target.setTotalExperience(0);
                target.setLevel(0);
                target.setExp(0);
                sender.sendMessage(Component.text(lang.getXpReset(target.getName()), NamedTextColor.DARK_PURPLE));
            }
            case "add" -> {
                if (args.length < 3) { sender.sendMessage(Component.text(lang.getXpUsage(), NamedTextColor.DARK_PURPLE)); return true; }
                int amount = parseAmount(sender, args[2]);
                if (amount < 0) return true;
                target.giveExp(amount);
                sender.sendMessage(Component.text(lang.getXpAdd(target.getName(), amount), NamedTextColor.DARK_PURPLE));
            }
            case "remove" -> {
                if (args.length < 3) { sender.sendMessage(Component.text(lang.getXpUsage(), NamedTextColor.DARK_PURPLE)); return true; }
                int amount = parseAmount(sender, args[2]);
                if (amount < 0) return true;
                int newTotal = Math.max(0, target.getTotalExperience() - amount);
                target.setLevel(0); target.setExp(0); target.giveExp(newTotal);
                sender.sendMessage(Component.text(lang.getXpRemove(target.getName(), amount), NamedTextColor.DARK_PURPLE));
            }
            default -> sender.sendMessage(Component.text(lang.getXpUsage(), NamedTextColor.DARK_PURPLE));
        }
        return true;
    }

    private int parseAmount(CommandSender sender, String s) {
        try {
            int v = Integer.parseInt(s);
            if (v < 0) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text(lang.getXpInvalidAmount(), NamedTextColor.DARK_PURPLE));
            return -1;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.xp")) return List.of();
        if (args.length == 1) return SUBCOMMANDS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        if (args.length == 2) return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                .toList();
        return List.of();
    }
}
