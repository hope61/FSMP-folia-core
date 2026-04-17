package org.hope.fSMPFoliaCore.Shop;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Marks an inventory as belonging to the FSMP shop.
 * Used instead of title matching to prevent title-spoof exploits.
 */
public class ShopInventoryHolder implements InventoryHolder {
    private Inventory inventory;

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
