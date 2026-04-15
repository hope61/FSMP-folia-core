package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;

public class BroadcastCommand implements CommandExecutor {
    private final LangManager lang;

    public BroadcastCommand(LangManager lang) {
        this.lang = lang;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.broadcast")) {
            sender.sendMessage(Component.text(lang.getNoPermission(), NamedTextColor.DARK_PURPLE));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text(lang.getBroadcastUsage(), NamedTextColor.DARK_PURPLE));
            return true;
        }
        String message = String.join(" ", args);
        Component broadcast = Component.text()
                .append(Component.text("[", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD))
                .append(Component.text("BROADCAST", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                .append(Component.text("] ", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD))
                .append(Component.text(message, NamedTextColor.WHITE))
                .build();
        Bukkit.broadcast(broadcast);
        return true;
    }
}
