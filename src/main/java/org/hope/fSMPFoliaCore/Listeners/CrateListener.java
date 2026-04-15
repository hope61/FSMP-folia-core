package org.hope.fSMPFoliaCore.Listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.CrateManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.SoundManager;
import org.hope.fSMPFoliaCore.Managers.SpecialToolManager;

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!crateManager.isCrateItem(item)) return;

        event.setCancelled(true);

        // Consume one crate from the stack
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }

        giveReward(player);
        soundManager.play(player, "crate-open");
    }

    private void giveReward(Player player) {
        List<Map<?, ?>> rewards = config.getConfig().getMapList("crates.rewards");
        if (rewards.isEmpty()) {
            player.sendMessage(Component.text(lang.getCratesNoRewards(), NamedTextColor.DARK_PURPLE));
            return;
        }

        int totalWeight = rewards.stream().mapToInt(r -> getInt(r, "weight", 1)).sum();
        int roll = random.nextInt(Math.max(1, totalWeight));
        int cum = 0;
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
                player.sendMessage(Component.text(lang.getCratesRewardXp(amount), NamedTextColor.LIGHT_PURPLE));
            }
            case "totem" -> {
                ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
                var leftover = player.getInventory().addItem(totem);
                if (!leftover.isEmpty()) player.getWorld().dropItemNaturally(player.getLocation(), leftover.values().iterator().next());
                player.sendMessage(Component.text(lang.getCratesRewardTotem(), NamedTextColor.LIGHT_PURPLE));
            }
            case "special_pickaxe" -> {
                toolManager.giveTool(player, SpecialToolManager.ToolType.PICKAXE, config.getSpecialToolLifetimeHours());
                player.sendMessage(Component.text(lang.getCratesRewardTool(), NamedTextColor.LIGHT_PURPLE));
            }
            case "special_axe" -> {
                toolManager.giveTool(player, SpecialToolManager.ToolType.AXE, config.getSpecialToolLifetimeHours());
                player.sendMessage(Component.text(lang.getCratesRewardTool(), NamedTextColor.LIGHT_PURPLE));
            }
            case "special_shovel" -> {
                toolManager.giveTool(player, SpecialToolManager.ToolType.SHOVEL, config.getSpecialToolLifetimeHours());
                player.sendMessage(Component.text(lang.getCratesRewardTool(), NamedTextColor.LIGHT_PURPLE));
            }
            case "gold" -> {
                int amount = getInt(chosen, "amount", 8);
                ItemStack gold = new ItemStack(Material.GOLD_INGOT, amount);
                var leftover = player.getInventory().addItem(gold);
                if (!leftover.isEmpty()) player.getWorld().dropItemNaturally(player.getLocation(), leftover.values().iterator().next());
                player.sendMessage(Component.text(lang.getCratesRewardGold(amount), NamedTextColor.LIGHT_PURPLE));
            }
            default -> {
                player.giveExp(25);
                player.sendMessage(Component.text(lang.getCratesRewardXp(25), NamedTextColor.LIGHT_PURPLE));
            }
        }
    }

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
