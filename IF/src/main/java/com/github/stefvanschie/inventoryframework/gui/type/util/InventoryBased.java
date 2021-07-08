package com.github.stefvanschie.inventoryframework.gui.type.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface InventoryBased extends InventoryHolder {

    /**
     * Creates a new inventory of the type of the implementing class.
     *
     * @return the new inventory
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    Inventory createInventory();

}
