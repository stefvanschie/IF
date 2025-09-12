package com.github.stefvanschie.inventoryframework.abstraction;

import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * A smithing table inventory
 *
 * @since 0.8.0
 */
public abstract class SmithingTableInventory {

    /**
     * Creates a smithing table inventory.
     *
     * @param title the title of the inventory
     * @return the inventory
     * @since 0.11.0
     */
    @NotNull
    public abstract Inventory createInventory(@NotNull TextHolder title);
}
