package org.hope.fSMPFoliaCore.Crates;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/** Marks an inventory as the crate preview GUI — prevents title spoofing and item theft. */
public class CratePreviewHolder implements InventoryHolder {
    private Inventory inventory;

    @Override
    public @NotNull Inventory getInventory() { return inventory; }

    public void setInventory(Inventory inventory) { this.inventory = inventory; }
}
