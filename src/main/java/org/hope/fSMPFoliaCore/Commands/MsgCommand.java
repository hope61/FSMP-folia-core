package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.MessageManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class MsgCommand implements CommandExecutor {
    private final ConfigManager configManager;
    private final LangManager lang;
    private final MessageManager messageManager;

    public MsgCommand(ConfigManager configManager, LangManager lang, MessageManager messageManager) {
        this.configManager = configManager;
        this.lang = lang;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getMsgPlayersOnly()));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text(lang.getMsgUsage(), lang.secondary()));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(Component.text(lang.getMsgPlayerNotFound(args[0]), lang.secondary()));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(Component.text(lang.getMsgCannotSelf(), lang.secondary()));
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        send(player, target, message);
        return true;
    }

    public void send(Player sender, Player target, String message) {
        messageManager.setReplyTarget(sender.getUniqueId(), target.getUniqueId());

        Component toSender = Component.text()
                .append(Component.text(lang.getMsgFormatSent(target.getName()), lang.secondary()).decorate(TextDecoration.ITALIC))
                .append(Component.text(lang.getChatSeparator(), lang.secondary()))
                .append(Component.text(message, lang.white()))
                .build();

        Component toTarget = Component.text()
                .append(Component.text(lang.getMsgFormatReceived(sender.getName()), lang.secondary()).decorate(TextDecoration.ITALIC))
                .append(Component.text(lang.getChatSeparator(), lang.secondary()))
                .append(Component.text(message, lang.white()))
                .build();

        sender.sendMessage(toSender);
        target.sendMessage(toTarget);
    }
}
