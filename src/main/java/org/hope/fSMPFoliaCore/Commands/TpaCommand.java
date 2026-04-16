package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.TpaManager;
import org.hope.fSMPFoliaCore.Managers.VanishManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TpaCommand implements CommandExecutor, TabCompleter {
    private final LangManager lang;
    private final TpaManager tpaManager;
    private final VanishManager vanishManager;

    public TpaCommand(LangManager lang, TpaManager tpaManager, VanishManager vanishManager) {
        this.lang = lang;
        this.tpaManager = tpaManager;
        this.vanishManager = vanishManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getTpaPlayersOnly(), lang.secondary()));
            return true;
        }
        if (!player.hasPermission("fsmp.tpa")) {
            player.sendMessage(Component.text(lang.getNoPermission(), lang.secondary()));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Component.text(lang.getTpaUsage(), lang.secondary()));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || target.equals(player)) {
            if (target != null && target.equals(player)) {
                player.sendMessage(Component.text(lang.getTpaCannotSelf(), lang.secondary()));
            } else {
                player.sendMessage(Component.text(lang.getMsgPlayerNotFound(args[0]), lang.secondary()));
            }
            return true;
        }

        tpaManager.createRequest(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(Component.text(lang.getTpaSent(target.getName()), lang.secondary()));

        // Notify target with clickable buttons
        Component notification = Component.text()
                .append(Component.text(lang.getTpaReceived(player.getName()), lang.primary()))
                .appendNewline()
                .append(Component.text(lang.getTpaAcceptButton(), lang.success())
                        .clickEvent(ClickEvent.runCommand("/tpaccept"))
                        .hoverEvent(HoverEvent.showText(Component.text("/tpaccept", lang.success()))))
                .append(Component.text("  ", lang.white()))
                .append(Component.text(lang.getTpaDenyButton(), lang.error())
                        .clickEvent(ClickEvent.runCommand("/tpadeny"))
                        .hoverEvent(HoverEvent.showText(Component.text("/tpadeny", lang.error()))))
                .build();
        target.sendMessage(notification);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            boolean senderCanSeeVanished = sender.hasPermission("fsmp.vanish");
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.equals(sender))
                    .filter(p -> senderCanSeeVanished || !vanishManager.isVanished(p))
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
