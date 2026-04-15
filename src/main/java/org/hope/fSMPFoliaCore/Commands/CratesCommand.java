package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.CrateManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CratesCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("give", "giveall");
    private final LangManager lang;
    private final CrateManager crateManager;

    public CratesCommand(LangManager lang, CrateManager crateManager) {
        this.lang = lang;
        this.crateManager = crateManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.crates.admin")) {
            sender.sendMessage(Component.text(lang.getNoPermission(), NamedTextColor.DARK_AQUA));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text(lang.getCratesUsage(), NamedTextColor.DARK_AQUA));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "give" -> {
                if (args.length < 2) { sender.sendMessage(Component.text(lang.getCratesUsage(), NamedTextColor.DARK_AQUA)); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { sender.sendMessage(Component.text(lang.getMsgPlayerNotFound(args[1]), NamedTextColor.DARK_AQUA)); return true; }
                int amount = parseAmount(sender, args.length >= 3 ? args[2] : "1");
                if (amount < 0) return true;
                crateManager.addCrates(target, amount);
                sender.sendMessage(Component.text(lang.getCratesGiven(target.getName(), amount), NamedTextColor.DARK_AQUA));
                target.sendMessage(Component.text(lang.getCratesReceived(amount), NamedTextColor.AQUA));
            }
            case "giveall" -> {
                int amount = parseAmount(sender, args.length >= 2 ? args[1] : "1");
                if (amount < 0) return true;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    crateManager.addCrates(p, amount);
                    p.sendMessage(Component.text(lang.getCratesReceived(amount), NamedTextColor.AQUA));
                }
                sender.sendMessage(Component.text(lang.getCratesGivenAll(amount), NamedTextColor.DARK_AQUA));
            }
            default -> sender.sendMessage(Component.text(lang.getCratesUsage(), NamedTextColor.DARK_AQUA));
        }
        return true;
    }

    private int parseAmount(CommandSender sender, String s) {
        try {
            int v = Integer.parseInt(s);
            if (v < 1 || v > 64) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text(lang.getCratesInvalidAmount(), NamedTextColor.DARK_AQUA));
            return -1;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.crates.admin")) return List.of();
        if (args.length == 1) return SUBCOMMANDS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).toList();
        }
        return List.of();
    }
}
