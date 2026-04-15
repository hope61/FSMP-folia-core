package org.hope.fSMPFoliaCore.Listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.hope.fSMPFoliaCore.Managers.LangManager;

public class SpawnerBreakListener implements Listener {
    private final LangManager lang;

    public SpawnerBreakListener(LangManager lang) {
        this.lang = lang;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.SPAWNER) {
            return;
        }

        Player player = event.getPlayer();

        if (player.hasPermission("fsmp.spawner.bypass")) return;

        event.setCancelled(true);
        player.sendMessage(Component.text(lang.getSpawnerBreakDenied(), NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }
}
