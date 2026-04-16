package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.HomeManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HomesCommand implements CommandExecutor {
    private final FSMPFoliaCore plugin;
    private final LangManager lang;
    private final HomeManager homeManager;

    public HomesCommand(FSMPFoliaCore plugin, LangManager lang, HomeManager homeManager) {
        this.plugin = plugin;
        this.lang = lang;
        this.homeManager = homeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getHomePlayersOnly(), lang.secondary()));
            return true;
        }

        if (!player.hasPermission("fsmp.home")) {
            player.sendMessage(Component.text(lang.getHomeNoPermission(), lang.secondary()));
            return true;
        }

        List<String> homes = homeManager.getHomeNames(player.getUniqueId());

        if (homes.isEmpty()) {
            player.sendMessage(Component.text(lang.getHomeListEmpty(), lang.secondary()));
            return true;
        }

        // Folia: open inventory on entity scheduler thread
        player.getScheduler().run(plugin, t -> openGui(player, homes), null);
        return true;
    }

    private void openGui(Player player, List<String> homes) {
        int rows = Math.max(1, (int) Math.ceil(homes.size() / 9.0));
        rows = Math.min(rows, 3); // max 3 rows = 27 slots
        Inventory inv = Bukkit.createInventory(null, rows * 9,
                Component.text(lang.getHomesGuiTitle()).color(lang.secondary()).decorate(TextDecoration.BOLD));

        for (int i = 0; i < Math.min(homes.size(), rows * 9); i++) {
            String name = homes.get(i);
            Location loc = homeManager.getHome(player.getUniqueId(), name);

            Material icon = Material.COMPASS;
            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(name).color(lang.primary()).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));

            if (loc != null) {
                meta.lore(List.of(
                        //Component.text(loc.getWorld().getName()).color(lang.info()).decoration(TextDecoration.ITALIC, false),
                        //Component.text(String.format("X: %.1f  Y: %.1f  Z: %.1f", loc.getX(), loc.getY(), loc.getZ()))
                        //        .color(lang.info()).decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text(lang.getHomesGuiClickToTeleport()).color(lang.secondary()).decoration(TextDecoration.ITALIC, false)
                ));
            }

            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        player.openInventory(inv);
    }
}
