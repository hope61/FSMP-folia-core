package org.hope.fSMPFoliaCore.Shop;

import org.bukkit.Material;

import java.math.BigDecimal;

public record ShopItem(
        int slot,
        Material material,
        int quantity,
        String displayName,
        BigDecimal price
) {}
