package org.hope.fSMPFoliaCore.Listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.Managers.SpecialToolManager;
import org.hope.fSMPFoliaCore.Managers.SpecialToolManager.ToolType;

import java.util.ArrayList;
import java.util.List;

public class SpecialToolListener implements Listener {
    private final FSMPFoliaCore plugin;
    private final SpecialToolManager toolManager;
    private final LangManager lang;

    public SpecialToolListener(FSMPFoliaCore plugin, SpecialToolManager toolManager, LangManager lang) {
        this.plugin = plugin;
        this.toolManager = toolManager;
        this.lang = lang;
    }

    /** Prevent using expired tools. */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (!toolManager.isSpecialTool(item)) return;
        if (toolManager.isExpired(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text(lang.getSpecialToolExpired(), NamedTextColor.DARK_AQUA));
            // Remove only the held item slot, not all matching items in inventory
            event.getPlayer().getInventory().setItemInMainHand(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!toolManager.isSpecialTool(item)) return;

        if (toolManager.isExpired(item)) {
            event.setCancelled(true);
            player.sendMessage(Component.text(lang.getSpecialToolExpired(), NamedTextColor.DARK_AQUA));
            player.getInventory().setItemInMainHand(null);
            return;
        }

        ToolType type = toolManager.getType(item);
        if (type == null) return;

        Block broken = event.getBlock();

        switch (type) {
            case PICKAXE, SHOVEL -> break3x3(player, broken, item);
            case AXE -> fellTree(player, broken, item);
        }
    }

    private void break3x3(Player player, Block center, ItemStack tool) {
        // Break a 3×3 area around the broken block on the face the player hit
        List<Block> toBreak = get3x3Blocks(center, player);
        for (Block b : toBreak) {
            if (b.equals(center)) continue; // vanilla handles center
            if (b.getType().isAir() || !b.getType().isSolid()) continue;
            if (!player.hasPermission("worldguard.bypass") && !canBreak(player, b)) continue;

            // Folia: each block may be in a different region
            plugin.getServer().getRegionScheduler().run(plugin, b.getLocation(), task -> {
                if (!b.getType().isAir()) {
                    b.breakNaturally(tool);
                }
            });
        }
    }

    private List<Block> get3x3Blocks(Block center, Player player) {
        List<Block> blocks = new ArrayList<>();
        // Determine facing axis to know which plane to extend in
        org.bukkit.util.Vector dir = player.getLocation().getDirection().normalize();
        double ax = Math.abs(dir.getX()), ay = Math.abs(dir.getY()), az = Math.abs(dir.getZ());

        int[] dx, dy, dz;
        if (ay > ax && ay > az) {
            // Looking mostly up/down — break horizontal 3×3
            dx = new int[]{-1,0,1}; dy = new int[]{0}; dz = new int[]{-1,0,1};
        } else if (ax > az) {
            // Facing X axis
            dx = new int[]{0}; dy = new int[]{-1,0,1}; dz = new int[]{-1,0,1};
        } else {
            // Facing Z axis
            dx = new int[]{-1,0,1}; dy = new int[]{-1,0,1}; dz = new int[]{0};
        }

        for (int x : dx) for (int y : dy) for (int z : dz) {
            blocks.add(center.getRelative(x, y, z));
        }
        return blocks;
    }

    private void fellTree(Player player, Block start, ItemStack tool) {
        Material log = start.getType();
        if (!isLog(log)) return;

        List<Block> logs = new ArrayList<>();
        collectLogs(start, log, logs, 0, 150);

        for (Block b : logs) {
            if (b.equals(start)) continue;
            plugin.getServer().getRegionScheduler().run(plugin, b.getLocation(), task -> {
                if (b.getType() == log) {
                    b.breakNaturally(tool);
                }
            });
        }
    }

    private void collectLogs(Block start, Material log, List<Block> found, int ignored, int max) {
        java.util.ArrayDeque<Block> queue = new java.util.ArrayDeque<>();
        java.util.Set<Block> visited = new java.util.HashSet<>();
        queue.add(start);
        while (!queue.isEmpty() && found.size() < max) {
            Block block = queue.poll();
            if (visited.contains(block)) continue;
            visited.add(block);
            if (block.getType() != log) continue;
            found.add(block);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block neighbor = block.getRelative(dx, dy, dz);
                        if (!visited.contains(neighbor) && neighbor.getType() == log) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
    }

    private boolean isLog(Material mat) {
        String name = mat.name();
        return name.endsWith("_LOG") || name.endsWith("_WOOD");
    }

    private boolean canBreak(Player player, Block block) {
        // Basic check — could integrate with WorldGuard if needed
        return true;
    }
}
