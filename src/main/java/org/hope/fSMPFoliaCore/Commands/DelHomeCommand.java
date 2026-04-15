package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hope.fSMPFoliaCore.Managers.HomeManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DelHomeCommand implements CommandExecutor, TabCompleter {
    private final LangManager lang;
    private final HomeManager homeManager;

    // Pending "delhome all" confirmations
    private final Map<UUID, Long> deleteAllPending = new ConcurrentHashMap<>();
    private static final long CONFIRM_EXPIRY_MS = 30_000L;

    public DelHomeCommand(LangManager lang, HomeManager homeManager) {
        this.lang = lang;
        this.homeManager = homeManager;
    }

    public void clearPending(UUID uuid) {
        deleteAllPending.remove(uuid);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getHomePlayersOnly(), NamedTextColor.DARK_PURPLE));
            return true;
        }

        if (!player.hasPermission("fsmp.home")) {
            player.sendMessage(Component.text(lang.getHomeNoPermission(), NamedTextColor.DARK_PURPLE));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text(lang.getDelhomeUsage(), NamedTextColor.DARK_PURPLE));
            return true;
        }

        String name = args[0];

        // /delhome all — first use asks confirmation, second use within 30s confirms
        if ("all".equalsIgnoreCase(name)) {
            Long pending = deleteAllPending.get(player.getUniqueId());
            if (pending != null && System.currentTimeMillis() - pending < CONFIRM_EXPIRY_MS) {
                deleteAllPending.remove(player.getUniqueId());
                homeManager.deleteAllHomes(player.getUniqueId());
                player.sendMessage(Component.text(lang.getHomeAllDeleted(), NamedTextColor.LIGHT_PURPLE));
            } else {
                deleteAllPending.put(player.getUniqueId(), System.currentTimeMillis());
                player.sendMessage(Component.text(lang.getDelhomeAllConfirm(), NamedTextColor.GOLD));
            }
            return true;
        }

        if (!homeManager.hasHome(player.getUniqueId(), name)) {
            player.sendMessage(Component.text(lang.getHomeNotFound(name), NamedTextColor.DARK_PURPLE));
            return true;
        }

        homeManager.deleteHome(player.getUniqueId(), name);
        player.sendMessage(Component.text(lang.getHomeDeleted(name), NamedTextColor.LIGHT_PURPLE));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        if (!player.hasPermission("fsmp.home")) return List.of();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> names = new java.util.ArrayList<>(homeManager.getHomeNames(player.getUniqueId()));
            names.add("all");
            return names.stream().filter(h -> h.toLowerCase().startsWith(input)).toList();
        }
        return List.of();
    }
}
