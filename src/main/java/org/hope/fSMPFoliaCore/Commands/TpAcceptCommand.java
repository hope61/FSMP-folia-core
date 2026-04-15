package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.SoundManager;
import org.hope.fSMPFoliaCore.Managers.TpaManager;
import org.jetbrains.annotations.NotNull;

public class TpAcceptCommand implements CommandExecutor {
    private final LangManager lang;
    private final TpaManager tpaManager;
    private final SoundManager soundManager;

    public TpAcceptCommand(LangManager lang, TpaManager tpaManager, SoundManager soundManager) {
        this.lang = lang;
        this.tpaManager = tpaManager;
        this.soundManager = soundManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player target)) {
            sender.sendMessage(Component.text(lang.getTpaPlayersOnly(), NamedTextColor.DARK_PURPLE));
            return true;
        }
        if (!target.hasPermission("fsmp.tpa")) {
            target.sendMessage(Component.text(lang.getNoPermission(), NamedTextColor.DARK_PURPLE));
            return true;
        }

        TpaManager.Request req = tpaManager.removeRequest(target.getUniqueId());
        if (req == null) {
            target.sendMessage(Component.text(lang.getTpaNoPending(), NamedTextColor.DARK_PURPLE));
            return true;
        }

        Player requester = Bukkit.getPlayer(req.from());
        if (requester == null) {
            target.sendMessage(Component.text(lang.getTpaRequesterOffline(), NamedTextColor.DARK_PURPLE));
            return true;
        }

        // Teleport requester to target
        requester.teleportAsync(target.getLocation()).thenAccept(success -> {
            if (success) {
                requester.sendMessage(Component.text(lang.getTpaAccepted(target.getName()), NamedTextColor.DARK_PURPLE));
                target.sendMessage(Component.text(lang.getTpaAcceptedTarget(requester.getName()), NamedTextColor.DARK_PURPLE));
                soundManager.play(requester, "teleport");
                soundManager.play(target, "tpa-accepted");
            }
        });
        return true;
    }
}
