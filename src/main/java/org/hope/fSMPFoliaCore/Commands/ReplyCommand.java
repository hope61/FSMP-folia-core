package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.MessageManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ReplyCommand implements CommandExecutor {
    private final ConfigManager configManager;
    private final LangManager lang;
    private final MessageManager messageManager;
    private final MsgCommand msgCommand;

    public ReplyCommand(ConfigManager configManager, LangManager lang, MessageManager messageManager, MsgCommand msgCommand) {
        this.configManager = configManager;
        this.lang = lang;
        this.messageManager = messageManager;
        this.msgCommand = msgCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getMsgPlayersOnly()));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text(lang.getReplyUsage(), lang.secondary()));
            return true;
        }

        UUID targetUUID = messageManager.getReplyTarget(player.getUniqueId());
        if (targetUUID == null) {
            player.sendMessage(Component.text(lang.getMsgNoReplyTarget(), lang.secondary()));
            return true;
        }

        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null || !target.isOnline()) {
            // Target UUID is known but the player is no longer online — no name available
            player.sendMessage(Component.text(lang.getMsgNoReplyTarget(), lang.secondary()));
            return true;
        }

        msgCommand.send(player, target, String.join(" ", args));
        return true;
    }
}
