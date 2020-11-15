package com.github.stefvanschie.inventoryframework.abstraction;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A grindstone inventory
 *
 * @since 0.8.0
 */
public abstract class GrindstoneInventory {

    /**
     * The inventory holder
     */
    @NotNull
    protected InventoryHolder inventoryHolder;

    /**
     * Creates a new grindstone inventory for the specified inventory holder
     *
     * @param inventoryHolder the inventory holder
     * @since 0.8.0
     */
    public GrindstoneInventory(@NotNull InventoryHolder inventoryHolder) {
        this.inventoryHolder = inventoryHolder;
    }

    /**
     * Opens the inventory for the specified player
     *
     * @param player the player to open the inventory for
     * @param title the title of the inventory
     * @param items the top items
     * @since 0.8.0
     */
    public abstract void openInventory(@NotNull Player player, @NotNull String title, @Nullable ItemStack[] items);

    /**
     * Sends the top items to the inventory for the specified player.
     *
     * @param player the player for which to open the grindstone
     * @param items the items to send
     * @since 0.8.0
     */
    public abstract void sendItems(@NotNull Player player, @Nullable ItemStack[] items);

    /**
     * Clears the cursor of the specified player
     *
     * @param player the player to clear the cursor of
     * @since 0.8.0
     */
    public abstract void clearCursor(@NotNull Player player);
}
