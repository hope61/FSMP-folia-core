package org.hope.fSMPFoliaCore.Managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;

import java.util.List;

public class SpecialToolManager {
    public static final String KEY_EXPIRES = "fsmp_tool_expires";
    public static final String KEY_TYPE    = "fsmp_tool_type";

    private final NamespacedKey expiresKey;
    private final NamespacedKey typeKey;
    private final FSMPFoliaCore plugin;
    private final LangManager lang;

    public SpecialToolManager(FSMPFoliaCore plugin, LangManager lang) {
        this.plugin = plugin;
        this.lang = lang;
        this.expiresKey = new NamespacedKey(plugin, KEY_EXPIRES);
        this.typeKey    = new NamespacedKey(plugin, KEY_TYPE);
    }

    public NamespacedKey getExpiresKey() { return expiresKey; }
    public NamespacedKey getTypeKey()    { return typeKey; }

    public enum ToolType {
        PICKAXE, AXE, SHOVEL
    }

    /** Create a special tool item. Lifetime in hours from config (default 24). */
    public ItemStack createTool(ToolType type, int lifetimeHours) {
        Material mat = switch (type) {
            case PICKAXE -> Material.NETHERITE_PICKAXE;
            case AXE     -> Material.NETHERITE_AXE;
            case SHOVEL  -> Material.NETHERITE_SHOVEL;
        };

        String typeName = switch (type) {
            case PICKAXE -> "Кирка";
            case AXE     -> "Брадва";
            case SHOVEL  -> "Лопата";
        };

        String ability = switch (type) {
            case PICKAXE -> "3×3 разбиване на блокове";
            case AXE     -> "Сеч на цяло дърво";
            case SHOVEL  -> "3×3 разбиване на блокове";
        };

        long expiresAt = System.currentTimeMillis() + lifetimeHours * 3600_000L;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("✦ Специална " + typeName + " ✦")
                .color(lang.warning())
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(List.of(
                Component.empty(),
                Component.text("Способност: ").color(lang.info()).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(ability).color(lang.warning()).decoration(TextDecoration.ITALIC, false)),
                Component.empty(),
                Component.text("Изтича след " + lifetimeHours + "ч").color(lang.error()).decoration(TextDecoration.ITALIC, false)
        ));

        meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addEnchant(Enchantment.FORTUNE, 3, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        if (type == ToolType.AXE) {
            meta.addEnchant(Enchantment.SHARPNESS, 5, true);
        }
        if (type == ToolType.SHOVEL) {
            meta.addEnchant(Enchantment.SILK_TOUCH, 1, true);
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(false);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(expiresKey, PersistentDataType.LONG, expiresAt);
        pdc.set(typeKey, PersistentDataType.STRING, type.name());

        item.setItemMeta(meta);
        return item;
    }

    /** Returns true if the given item is an expired special tool. */
    public boolean isExpired(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        Long expires = pdc.get(expiresKey, PersistentDataType.LONG);
        return expires != null && System.currentTimeMillis() > expires;
    }

    /** Returns true if the item is a special tool (regardless of expiry). */
    public boolean isSpecialTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(expiresKey, PersistentDataType.LONG);
    }

    public ToolType getType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String s = item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        if (s == null) return null;
        try { return ToolType.valueOf(s); } catch (IllegalArgumentException e) { return null; }
    }

    /** Give tool to player (or drop at feet if inventory full). */
    public void giveTool(Player player, ToolType type, int lifetimeHours) {
        ItemStack item = createTool(type, lifetimeHours);
        var leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover.values().iterator().next());
        }
    }
}
