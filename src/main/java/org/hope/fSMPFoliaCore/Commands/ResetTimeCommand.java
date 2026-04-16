package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.PortalLockManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResetTimeCommand implements CommandExecutor, TabCompleter {
    private final LangManager lang;
    private final PortalLockManager portalLockManager;

    public ResetTimeCommand(LangManager lang, PortalLockManager portalLockManager) {
        this.lang = lang;
        this.portalLockManager = portalLockManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.resettime")) {
            sender.sendMessage(Component.text(lang.getNoPermission(), lang.secondary()));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text(lang.getResetTimeUsage(), lang.secondary()));
            return true;
        }
        String portal = args[0].toLowerCase();
        if (!portal.equals("nether") && !portal.equals("end")) {
            sender.sendMessage(Component.text(lang.getResetTimeUsage(), lang.secondary()));
            return true;
        }
        portalLockManager.resetTimer(portal);
        sender.sendMessage(Component.text(lang.getResetTimeSuccess(portal), lang.secondary()));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.resettime")) return List.of();
        if (args.length == 1) return List.of("nether", "end").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        return List.of();
    }
}
