package com.github.stefvanschie.inventoryframework.abstraction;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * A beacon inventory
 *
 * @since 0.8.0
 */
public abstract class BeaconInventory {

    /**
     * Creates a beacon inventory.
     *
     * @return the inventory
     * @since 0.11.0
     */
    @NotNull
    public abstract Inventory createInventory();
}
