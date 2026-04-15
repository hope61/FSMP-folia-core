package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.SpecialToolManager;
import org.hope.fSMPFoliaCore.Managers.SpecialToolManager.ToolType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GiveItemCommand implements CommandExecutor, TabCompleter {
    private static final List<String> TOOL_TYPES = List.of("pickaxe", "axe", "shovel");

    private final LangManager lang;
    private final SpecialToolManager toolManager;
    private final ConfigManager config;

    public GiveItemCommand(LangManager lang, SpecialToolManager toolManager, ConfigManager config) {
        this.lang = lang;
        this.toolManager = toolManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.admin")) {
            sender.sendMessage(Component.text(lang.getNoPermission(), NamedTextColor.DARK_AQUA));
            return true;
        }
        // /fsmpcore giveitem <axe|pickaxe|shovel> <player> [amount]
        if (args.length < 3) {
            sender.sendMessage(Component.text(lang.getGiveItemUsage(), NamedTextColor.DARK_AQUA));
            return true;
        }

        String typeName = args[1].toLowerCase();
        ToolType type = switch (typeName) {
            case "pickaxe" -> ToolType.PICKAXE;
            case "axe"     -> ToolType.AXE;
            case "shovel"  -> ToolType.SHOVEL;
            default -> null;
        };
        if (type == null) {
            sender.sendMessage(Component.text(lang.getGiveItemUsage(), NamedTextColor.DARK_AQUA));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            sender.sendMessage(Component.text(lang.getMsgPlayerNotFound(args[2]), NamedTextColor.DARK_AQUA));
            return true;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount < 1 || amount > 64) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text(lang.getCratesInvalidAmount(), NamedTextColor.DARK_AQUA));
                return true;
            }
        }

        int lifetimeHours = config.getSpecialToolLifetimeHours();
        for (int i = 0; i < amount; i++) {
            toolManager.giveTool(target, type, lifetimeHours);
        }
        sender.sendMessage(Component.text(lang.getGiveItemSuccess(typeName, target.getName(), amount), NamedTextColor.DARK_AQUA));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        // args[0] = "giveitem" (handled by parent command), but we get the sub-args here
        if (args.length == 2) return TOOL_TYPES.stream().filter(s -> s.startsWith(args[1].toLowerCase())).toList();
        if (args.length == 3) return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase())).toList();
        return List.of();
    }
}
