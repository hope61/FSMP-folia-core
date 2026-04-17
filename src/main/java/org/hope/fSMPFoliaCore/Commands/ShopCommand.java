package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Shop.ShopInventoryHolder;
import org.hope.fSMPFoliaCore.Shop.ShopItem;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class ShopCommand implements CommandExecutor {

    public static final String GUI_TITLE = "✦ FSMP Магазин ✦";

    // Crystal PvP shop items — prices in gold ingots
    public static final List<ShopItem> ITEMS = List.of(
        new ShopItem(10, Material.TOTEM_OF_UNDYING,       1,  "Totem of Undying",       BigDecimal.valueOf(32)),
        new ShopItem(11, Material.END_CRYSTAL,            4,  "End Crystals ×4",        BigDecimal.valueOf(20)),
        new ShopItem(12, Material.RESPAWN_ANCHOR,         1,  "Respawn Anchor",         BigDecimal.valueOf(28)),
        new ShopItem(13, Material.OBSIDIAN,              32,  "Obsidian ×32",           BigDecimal.valueOf(15)),
        new ShopItem(14, Material.CRYING_OBSIDIAN,        8,  "Crying Obsidian ×8",     BigDecimal.valueOf(12)),
        new ShopItem(15, Material.GLOWSTONE,             16,  "Glowstone ×16",          BigDecimal.valueOf(8)),
        new ShopItem(16, Material.ENDER_PEARL,            8,  "Ender Pearls ×8",        BigDecimal.valueOf(15)),
        new ShopItem(19, Material.GOLDEN_APPLE,           4,  "Golden Apples ×4",       BigDecimal.valueOf(18)),
        new ShopItem(20, Material.ENCHANTED_GOLDEN_APPLE, 1,  "Enchanted Golden Apple", BigDecimal.valueOf(64)),
        new ShopItem(21, Material.CHORUS_FRUIT,          16,  "Chorus Fruit ×16",       BigDecimal.valueOf(6))
    );

    private final LangManager lang;

    public ShopCommand(LangManager lang) {
        this.lang = lang;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(lang.getShopPlayersOnly(), lang.secondary()));
            return true;
        }
        player.openInventory(buildGui());
        return true;
    }

    public Inventory buildGui() {
        ShopInventoryHolder holder = new ShopInventoryHolder();
        Inventory inv = Bukkit.createInventory(holder, 36, Component.text(GUI_TITLE)
                .color(lang.primary())
                .decorate(TextDecoration.BOLD));
        holder.setInventory(inv);

        // Border — magenta stained glass panes
        ItemStack border = buildBorder();
        for (int i = 0; i < 9; i++)  inv.setItem(i, border);       // top row
        for (int i = 27; i < 36; i++) inv.setItem(i, border);      // bottom row
        inv.setItem(9,  border); inv.setItem(17, border);           // left/right row 1
        inv.setItem(18, border); inv.setItem(26, border);           // left/right row 2

        // Shop items
        for (ShopItem shopItem : ITEMS) {
            inv.setItem(shopItem.slot(), buildItem(shopItem));
        }

        return inv;
    }

    private ItemStack buildBorder() {
        ItemStack pane = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    private ItemStack buildItem(ShopItem shopItem) {
        ItemStack item = new ItemStack(shopItem.material(), shopItem.quantity());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(shopItem.displayName())
                .color(lang.primary())
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.empty(),
                Component.text("Цена: ")
                        .color(lang.info())
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(shopItem.price().toPlainString() + " 🪙 Злато")
                                .color(lang.warning())
                                .decorate(TextDecoration.BOLD)
                                .decoration(TextDecoration.ITALIC, false)),
                Component.empty(),
                Component.text("Кликни за да купиш!")
                        .color(lang.secondary())
                        .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }
}
