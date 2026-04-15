package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.PlaytimeManager;
import org.hope.fSMPFoliaCore.Managers.VanishManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PlaytimeCommand implements CommandExecutor, TabCompleter {
    private final LangManager lang;
    private final PlaytimeManager playtimeManager;
    private final VanishManager vanishManager;

    public PlaytimeCommand(LangManager lang, PlaytimeManager playtimeManager, VanishManager vanishManager) {
        this.lang = lang;
        this.playtimeManager = playtimeManager;
        this.vanishManager = vanishManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("fsmp.playtime")) {
            sender.sendMessage(Component.text(lang.getNoPermission(), NamedTextColor.DARK_AQUA));
            return true;
        }

        if (args.length == 0) {
            // Show own playtime
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text(lang.getPlaytimePlayersOnly(), NamedTextColor.DARK_AQUA));
                return true;
            }
            showPlaytime(sender, player.getUniqueId(), player.getName());
        } else {
            // Show another player's playtime (requires fsmp.playtime.other or any op)
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                // Try offline lookup for total only
                sender.sendMessage(Component.text(lang.getMsgPlayerNotFound(args[0]), NamedTextColor.DARK_AQUA));
                return true;
            }
            showPlaytime(sender, target.getUniqueId(), target.getName());
        }
        return true;
    }

    private void showPlaytime(CommandSender sender, UUID uuid, String name) {
        long total = playtimeManager.getTotalSeconds(uuid);
        long session = playtimeManager.getSessionSeconds(uuid);
        long combined = total + session;

        sender.sendMessage(Component.text(
                lang.getPlaytimeInfo(name,
                        PlaytimeManager.format(combined),
                        PlaytimeManager.format(session)),
                NamedTextColor.AQUA));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            boolean canSeeVanished = sender.hasPermission("fsmp.vanish");
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> canSeeVanished || !vanishManager.isVanished(p))
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
