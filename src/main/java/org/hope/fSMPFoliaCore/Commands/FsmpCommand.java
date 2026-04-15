package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.hope.fSMPFoliaCore.Managers.AnnouncementManager;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FsmpCommand implements CommandExecutor, TabCompleter {
    private final ConfigManager configManager;
    private final LangManager lang;
    private final AnnouncementManager announcementManager;

    public FsmpCommand(ConfigManager configManager, LangManager lang, AnnouncementManager announcementManager) {
        this.configManager = configManager;
        this.lang = lang;
        this.announcementManager = announcementManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.reload")) {
            sender.sendMessage(Component.text(lang.getFsmpNoPermission(), NamedTextColor.DARK_AQUA));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(Component.text(lang.getFsmpUsage(), NamedTextColor.DARK_AQUA));
            return true;
        }

        configManager.reload();
        lang.reload();
        announcementManager.stop();
        announcementManager.start();
        sender.sendMessage(Component.text(lang.getFsmpReloadSuccess(), NamedTextColor.AQUA));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.reload")) return List.of();
        if (args.length == 1) {
            return "reload".startsWith(args[0].toLowerCase()) ? List.of("reload") : List.of();
        }
        return List.of();
    }
}
