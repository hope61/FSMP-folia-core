package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.SoundManager;
import org.hope.fSMPFoliaCore.Managers.TpaManager;
import org.jetbrains.annotations.NotNull;

public class TpDenyCommand implements CommandExecutor {
    private final LangManager lang;
    private final TpaManager tpaManager;
    private final SoundManager soundManager;

    public TpDenyCommand(LangManager lang, TpaManager tpaManager, SoundManager soundManager) {
        this.lang = lang;
        this.tpaManager = tpaManager;
        this.soundManager = soundManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player target)) {
            sender.sendMessage(Component.text(lang.getTpaPlayersOnly(), lang.secondary()));
            return true;
        }
        if (!target.hasPermission("fsmp.tpa")) {
            target.sendMessage(Component.text(lang.getNoPermission(), lang.secondary()));
            return true;
        }

        TpaManager.Request req = tpaManager.removeRequest(target.getUniqueId());
        if (req == null) {
            target.sendMessage(Component.text(lang.getTpaNoPending(), lang.secondary()));
            return true;
        }

        Player requester = Bukkit.getPlayer(req.from());
        target.sendMessage(Component.text(lang.getTpaDeniedTarget(
                requester != null ? requester.getName() : lang.getTpaUnknownPlayer()), lang.secondary()));

        if (requester != null) {
            requester.sendMessage(Component.text(lang.getTpaDenied(target.getName()), lang.secondary()));
            soundManager.play(requester, "tpa-denied");
        }
        return true;
    }
}
