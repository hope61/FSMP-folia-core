package org.hope.fSMPFoliaCore.Listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hope.fSMPFoliaCore.Crates.CratePreviewHolder;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.CrateManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.SoundManager;
import org.hope.fSMPFoliaCore.Managers.SpecialToolManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CrateListener implements Listener {
    private final CrateManager crateManager;
    private final ConfigManager config;
    private final LangManager lang;
    private final SpecialToolManager toolManager;
    private final SoundManager soundManager;
    private final Random random = new Random();

    public CrateListener(CrateManager crateManager, ConfigManager config, LangManager lang,
                         SpecialToolManager toolManager, SoundManager soundManager) {
        this.crateManager = crateManager;
        this.config = config;
        this.lang = lang;
        this.toolManager = toolManager;
        this.soundManager = soundManager;
    }

    // ── Hotbar hint — show action bar when crate is selected ─────

    @EventHandler
    public void onHeldChange(PlayerItemHeldEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if (!crateManager.isCrateItem(item)) return;
        event.getPlayer().sendActionBar(
                Component.text("✦ FSMP Кутия  ", lang.primary()).decorate(TextDecoration.BOLD)
                        .append(Component.text("| ", lang.secondary()))
                        .append(Component.text("[Десен Клик]", lang.success()).decorate(TextDecoration.BOLD))
                        .append(Component.text(" Отвори  ", lang.white()))
                        .append(Component.text("| ", lang.secondary()))
                        .append(Component.text("[Ляв Клик]", lang.info()).decorate(TextDecoration.BOLD))
                        .append(Component.text(" Преглед", lang.white()))
        );
    }

    // ── Right click — open crate ──────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!crateManager.isCrateItem(item)) return;

        event.setCancelled(true);

        // Consume one crate
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }

        giveReward(player);
        soundManager.play(player, "crate-open");
    }

    // ── Left click — open preview GUI ────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeftClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!crateManager.isCrateItem(item)) return;

        event.setCancelled(true);
        player.openInventory(buildPreviewGui());
    }

    // ── Preview GUI — block all interaction ──────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreviewClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CratePreviewHolder)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreviewDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof CratePreviewHolder)) return;
        event.setCancelled(true);
    }

    // ── Preview GUI builder (fully dynamic from config) ──────────

    private Inventory buildPreviewGui() {
        List<Map<?, ?>> rewards = config.getConfig().getMapList("crates.rewards");

        // Compute total weight for chance calculation
        int totalWeight = rewards.stream().mapToInt(r -> getInt(r, "weight", 1)).sum();

        // Size: top border + content rows + bottom border, max 6 rows
        int contentRows = (int) Math.ceil(rewards.size() / 9.0);
        int totalRows   = Math.min(6, Math.max(3, contentRows + 2));
        int size        = totalRows * 9;

        CratePreviewHolder holder = new CratePreviewHolder();
        Inventory inv = Bukkit.createInventory(holder, size,
                Component.text("✦ Съдържание на Кутия ✦")
                        .color(lang.primary())
                        .decorate(TextDecoration.BOLD));
        holder.setInventory(inv);

        // Border — top row and bottom row
        ItemStack border = buildPane(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, border);                          // top
        for (int i = size - 9; i < size; i++) inv.setItem(i, border);                // bottom
        // Side borders for middle rows
        for (int row = 1; row < totalRows - 1; row++) {
            inv.setItem(row * 9, border);
            inv.setItem(row * 9 + 8, border);
        }

        // Place reward items starting at slot 10 (first inner slot after top border + left border)
        // Fill inner slots: rows 1..(totalRows-2), columns 1..7
        List<Integer> innerSlots = new ArrayList<>();
        for (int row = 1; row < totalRows - 1; row++) {
            for (int col = 1; col <= 7; col++) {
                innerSlots.add(row * 9 + col);
            }
        }

        for (int i = 0; i < rewards.size() && i < innerSlots.size(); i++) {
            Map<?, ?> reward  = rewards.get(i);
            String type       = getString(reward, "type", "xp");
            int weight        = getInt(reward, "weight", 1);
            double chance     = totalWeight > 0 ? (weight * 100.0 / totalWeight) : 0;

            inv.setItem(innerSlots.get(i), buildRewardItem(reward, type, chance));
        }

        return inv;
    }

    private ItemStack buildRewardItem(Map<?, ?> reward, String type, double chance) {
        Material mat = rewardMaterial(type);
        String name  = rewardDisplayName(reward, type);

        ItemStack item = new ItemStack(mat);
        ItemMeta meta  = item.getItemMeta();

        meta.displayName(Component.text(name)
                .color(lang.primary())
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        String chanceStr = String.format("%.1f", chance) + "%";
        meta.lore(List.of(
                Component.empty(),
                Component.text("Шанс: ")
                        .color(lang.info())
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(chanceStr)
                                .color(lang.warning())
                                .decorate(TextDecoration.BOLD)
                                .decoration(TextDecoration.ITALIC, false))
        ));

        item.setItemMeta(meta);
        return item;
    }

    private Material rewardMaterial(String type) {
        return switch (type) {
            case "xp"              -> Material.EXPERIENCE_BOTTLE;
            case "gold"            -> Material.GOLD_INGOT;
            case "totem"           -> Material.TOTEM_OF_UNDYING;
            case "special_pickaxe" -> Material.NETHERITE_PICKAXE;
            case "special_axe"     -> Material.NETHERITE_AXE;
            case "special_shovel"  -> Material.NETHERITE_SHOVEL;
            default                -> Material.PAPER;
        };
    }

    private String rewardDisplayName(Map<?, ?> reward, String type) {
        return switch (type) {
            case "xp"              -> getInt(reward, "amount", 25) + " XP";
            case "gold"            -> getInt(reward, "amount", 8) + "x Злато";
            case "totem"           -> "Totem of Undying";
            case "special_pickaxe" -> "Специална Кирка";
            case "special_axe"     -> "Специална Брадва";
            case "special_shovel"  -> "Специална Лопата";
            default                -> type;
        };
    }

    private ItemStack buildPane(Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta  = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    // ── Reward giving ─────────────────────────────────────────────

    private void giveReward(Player player) {
        List<Map<?, ?>> rewards = config.getConfig().getMapList("crates.rewards");
        if (rewards.isEmpty()) {
            player.sendMessage(Component.text(lang.getCratesNoRewards(), lang.secondary()));
            return;
        }

        int totalWeight = rewards.stream().mapToInt(r -> getInt(r, "weight", 1)).sum();
        int roll = random.nextInt(Math.max(1, totalWeight));
        int cum  = 0;
        Map<?, ?> chosen = rewards.get(rewards.size() - 1);
        for (Map<?, ?> r : rewards) {
            cum += getInt(r, "weight", 1);
            if (roll < cum) { chosen = r; break; }
        }

        String type = getString(chosen, "type", "xp");
        switch (type) {
            case "xp" -> {
                int amount = getInt(chosen, "amount", 25);
                player.giveExp(amount);
                player.sendMessage(Component.text(lang.getCratesRewardXp(amount), lang.primary()));
            }
            case "totem" -> {
                ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
                var leftover = player.getInventory().addItem(totem);
                if (!leftover.isEmpty()) player.getWorld().dropItemNaturally(player.getLocation(), leftover.values().iterator().next());
                player.sendMessage(Component.text(lang.getCratesRewardTotem(), lang.primary()));
            }
            case "special_pickaxe" -> {
                toolManager.giveTool(player, SpecialToolManager.ToolType.PICKAXE, config.getSpecialToolLifetimeHours());
                player.sendMessage(Component.text(lang.getCratesRewardTool(), lang.primary()));
            }
            case "special_axe" -> {
                toolManager.giveTool(player, SpecialToolManager.ToolType.AXE, config.getSpecialToolLifetimeHours());
                player.sendMessage(Component.text(lang.getCratesRewardTool(), lang.primary()));
            }
            case "special_shovel" -> {
                toolManager.giveTool(player, SpecialToolManager.ToolType.SHOVEL, config.getSpecialToolLifetimeHours());
                player.sendMessage(Component.text(lang.getCratesRewardTool(), lang.primary()));
            }
            case "gold" -> {
                int amount = getInt(chosen, "amount", 8);
                ItemStack gold = new ItemStack(Material.GOLD_INGOT, amount);
                var leftover = player.getInventory().addItem(gold);
                if (!leftover.isEmpty()) player.getWorld().dropItemNaturally(player.getLocation(), leftover.values().iterator().next());
                player.sendMessage(Component.text(lang.getCratesRewardGold(amount), lang.primary()));
            }
            default -> {
                player.giveExp(25);
                player.sendMessage(Component.text(lang.getCratesRewardXp(25), lang.primary()));
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private int getInt(Map<?, ?> map, String key, int def) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.intValue();
        return def;
    }

    private String getString(Map<?, ?> map, String key, String def) {
        Object v = map.get(key);
        if (v instanceof String s) return s;
        return def;
    }
}
