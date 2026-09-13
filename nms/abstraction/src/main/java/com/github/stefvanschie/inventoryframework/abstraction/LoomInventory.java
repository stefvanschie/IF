package com.github.stefvanschie.inventoryframework.abstraction;

import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * A loom inventory.
 *
 * @since 0.8.0
 */
public abstract class LoomInventory {

    /**
     * Creates a loom inventory.
     *
     * @param title the title of the inventory
     * @return the inventory
     * @since 0.12.1
     */
    @NotNull
    public abstract Inventory createInventory(@NotNull TextHolder title);
}
