package org.hope.fSMPFoliaCore.Listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;
import org.hope.fSMPFoliaCore.Managers.HomeManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;

public class HomesGuiListener implements Listener {
    private final FSMPFoliaCore plugin;
    private final LangManager lang;
    private final HomeManager homeManager;

    public HomesGuiListener(FSMPFoliaCore plugin, LangManager lang, HomeManager homeManager) {
        this.plugin = plugin;
        this.lang = lang;
        this.homeManager = homeManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if this is the homes GUI by matching title
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());
        if (!plainTitle.equals(lang.getHomesGuiTitle())) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta.displayName() == null) return;

        String homeName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(meta.displayName());

        if (homeName.isBlank()) return;

        // Close the inventory and trigger /home <name>
        player.closeInventory();

        // Use the entity scheduler to dispatch the home command (warmup/cooldown logic lives there)
        player.getScheduler().run(plugin, t -> {
            player.performCommand("home " + homeName);
        }, null);
    }
}
