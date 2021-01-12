package com.github.stefvanschie.inventoryframework.abstraction;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A beacon inventory
 *
 * @since 0.8.0
 */
public abstract class BeaconInventory {

    /**
     * The inventory holder
     */
    @NotNull
    protected InventoryHolder inventoryHolder;

    /**
     * Creates a new beacon inventory for the specified inventory holder
     *
     * @param inventoryHolder the inventory holder
     * @since 0.8.0
     */
    public BeaconInventory(@NotNull InventoryHolder inventoryHolder) {
        this.inventoryHolder = inventoryHolder;
    }

    /**
     * Opens the inventory for the specified player
     *
     * @param player the player to open the inventory for
     * @param item the item to send
     * @since 0.8.0
     */
    public abstract void openInventory(@NotNull Player player, @Nullable ItemStack item);

    /**
     * Sends the top item to the inventory for the specified player.
     *
     * @param player the player for which to open the beacon
     * @param item the item to send
     * @since 0.8.0
     */
    public abstract void sendItem(@NotNull Player player, @Nullable ItemStack item);

    /**
     * Clears the cursor of the specified player
     *
     * @param player the player to clear the cursor of
     * @since 0.8.0
     */
    public abstract void clearCursor(@NotNull Player player);
}
