package org.hope.fSMPFoliaCore.Listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;

import java.util.Set;

public class DiamondCraftListener implements Listener {
    private static final Set<Material> DIAMOND_ITEMS = Set.of(
            Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE,
            Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE,
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS
    );

    private final ConfigManager config;
    private final LangManager lang;

    public DiamondCraftListener(ConfigManager config, LangManager lang) {
        this.config = config;
        this.lang = lang;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!config.isDiamondCraftingEnabled()) return;
        ItemStack result = event.getRecipe().getResult();
        if (!DIAMOND_ITEMS.contains(result.getType())) return;

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (player.hasPermission(config.getDiamondCraftingBypassPermission())) return;

        // Block shift-click — Bukkit cannot reliably report how many items were actually
        // crafted after inventory constraints, so we cannot charge the correct XP amount.
        // Players must craft one at a time when an XP cost is in effect.
        if (event.isShiftClick()) {
            event.setCancelled(true);
            player.sendMessage(Component.text(lang.getDiamondCraftNoShiftClick(), lang.secondary()));
            return;
        }

        int xpCost = config.getDiamondCraftingXpCost();

        if (player.getTotalExperience() < xpCost) {
            event.setCancelled(true);
            player.sendMessage(Component.text(
                    lang.getDiamondCraftNoXp(xpCost, player.getTotalExperience()),
                    lang.secondary()));
            return;
        }

        // Deduct XP
        int newTotal = player.getTotalExperience() - xpCost;
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);
        player.giveExp(newTotal);
    }
}
