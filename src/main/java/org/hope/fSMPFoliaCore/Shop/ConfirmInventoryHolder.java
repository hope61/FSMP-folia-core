package org.hope.fSMPFoliaCore.Shop;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Holds the pending ShopItem so the confirm listener knows what was selected.
 */
public class ConfirmInventoryHolder implements InventoryHolder {
    private final ShopItem pendingItem;
    private Inventory inventory;

    public ConfirmInventoryHolder(ShopItem pendingItem) {
        this.pendingItem = pendingItem;
    }

    public ShopItem getPendingItem() {
        return pendingItem;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
