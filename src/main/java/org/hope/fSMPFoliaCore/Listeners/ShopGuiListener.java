package org.hope.fSMPFoliaCore.Listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hope.fSMPFoliaCore.Commands.ShopCommand;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Shop.ConfirmInventoryHolder;
import org.hope.fSMPFoliaCore.Shop.ShopInventoryHolder;
import org.hope.fSMPFoliaCore.Shop.ShopItem;
import ovh.mythmc.banco.api.Banco;
import ovh.mythmc.banco.api.accounts.AccountManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShopGuiListener implements Listener {

    private static final int CONFIRM_SLOT = 11;
    private static final int CANCEL_SLOT  = 15;

    private static final long CLICK_COOLDOWN_MS = 300L;
    private final Map<UUID, Long> lastClick = new ConcurrentHashMap<>();

    private final LangManager lang;
    private final ShopCommand shopCommand;

    public ShopGuiListener(LangManager lang, ShopCommand shopCommand) {
        this.lang = lang;
        this.shopCommand = shopCommand;
    }

    // ── Shop GUI click — opens confirm GUI ────────────────────────

    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof ShopInventoryHolder)) return;

        event.setCancelled(true);
        if (!isValidClick(event)) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 36) return;

        ShopItem shopItem = null;
        for (ShopItem s : ShopCommand.ITEMS) {
            if (s.slot() == slot) { shopItem = s; break; }
        }
        if (shopItem == null) return;
        if (!applyCooldown(player)) return;

        player.openInventory(buildConfirmGui(shopItem));
    }

    // ── Confirm GUI click ─────────────────────────────────────────

    @EventHandler
    public void onConfirmClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof ConfirmInventoryHolder holder)) return;

        event.setCancelled(true);
        if (!isValidClick(event)) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 27) return;
        if (!applyCooldown(player)) return;

        if (slot == CANCEL_SLOT) {
            player.openInventory(buildShopGui());
            return;
        }

        if (slot != CONFIRM_SLOT) return;

        // ── Purchase ─────────────────────────────────────────────
        ShopItem shopItem = holder.getPendingItem();

        Banco banco;
        try {
            banco = Banco.get();
        } catch (Exception | NoClassDefFoundError e) {
            player.sendMessage(Component.text(lang.getShopUnavailable(), lang.error()));
            player.closeInventory();
            return;
        }
        if (banco == null) {
            player.sendMessage(Component.text(lang.getShopUnavailable(), lang.error()));
            player.closeInventory();
            return;
        }

        AccountManager accountManager = banco.getAccountManager();
        BigDecimal price = shopItem.price();

        if (!accountManager.has(player.getUniqueId(), price)) {
            BigDecimal balance = accountManager.amount(player.getUniqueId());
            player.sendMessage(Component.text(
                    lang.getShopNotEnoughGold(price, balance), lang.error()));
            player.closeInventory();
            return;
        }

        accountManager.withdraw(player.getUniqueId(), price);

        ItemStack reward = new ItemStack(shopItem.material(), shopItem.quantity());
        var leftover = player.getInventory().addItem(reward);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(
                    player.getLocation(), leftover.values().iterator().next());
        }

        player.sendMessage(Component.text(
                lang.getShopPurchased(shopItem.displayName(), price), lang.success()));
        player.closeInventory();
    }

    // Block drag events on both GUIs — prevents dragging items out of the inventory
    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getInventory().getHolder() instanceof ShopInventoryHolder
                || event.getInventory().getHolder() instanceof ConfirmInventoryHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastClick.remove(event.getPlayer().getUniqueId());
    }

    // ── Helpers ───────────────────────────────────────────────────

    private boolean isValidClick(InventoryClickEvent event) {
        ClickType click = event.getClick();
        return click != ClickType.DOUBLE_CLICK
                && click != ClickType.SHIFT_LEFT
                && click != ClickType.SHIFT_RIGHT
                && click != ClickType.NUMBER_KEY
                && click != ClickType.DROP
                && click != ClickType.CONTROL_DROP
                && click != ClickType.MIDDLE
                && click != ClickType.CREATIVE;
    }

    private boolean applyCooldown(Player player) {
        long now = System.currentTimeMillis();
        Long last = lastClick.get(player.getUniqueId());
        if (last != null && now - last < CLICK_COOLDOWN_MS) return false;
        lastClick.put(player.getUniqueId(), now);
        return true;
    }

    private Inventory buildShopGui() {
        return shopCommand.buildGui();
    }

    private Inventory buildConfirmGui(ShopItem shopItem) {
        ConfirmInventoryHolder holder = new ConfirmInventoryHolder(shopItem);
        Inventory inv = Bukkit.createInventory(holder, 27,
                Component.text("Потвърди покупка?")
                        .color(lang.primary())
                        .decorate(TextDecoration.BOLD));
        holder.setInventory(inv);

        // Border
        ItemStack border = buildPane(Material.GRAY_STAINED_GLASS_PANE, Component.empty());
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        // Item preview (center)
        ItemStack preview = new ItemStack(shopItem.material(), shopItem.quantity());
        ItemMeta previewMeta = preview.getItemMeta();
        previewMeta.displayName(Component.text(shopItem.displayName())
                .color(lang.primary())
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        previewMeta.lore(List.of(
                Component.empty(),
                Component.text("Цена: ")
                        .color(lang.info())
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(shopItem.price().toPlainString() + " 🪙 Злато")
                                .color(lang.warning())
                                .decorate(TextDecoration.BOLD)
                                .decoration(TextDecoration.ITALIC, false))
        ));
        preview.setItemMeta(previewMeta);
        inv.setItem(13, preview);

        // Confirm button — green
        inv.setItem(CONFIRM_SLOT, buildPane(Material.LIME_STAINED_GLASS_PANE,
                Component.text("✔ Потвърди")
                        .color(lang.success())
                        .decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false)));

        // Cancel button — red
        inv.setItem(CANCEL_SLOT, buildPane(Material.RED_STAINED_GLASS_PANE,
                Component.text("✘ Откажи")
                        .color(lang.error())
                        .decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false)));

        return inv;
    }

    private ItemStack buildPane(Material material, Component name) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(name);
        pane.setItemMeta(meta);
        return pane;
    }
}
