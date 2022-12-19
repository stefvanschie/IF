package com.github.stefvanschie.inventoryframework.abstraction;

import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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
    public final Inventory openInventory(@NotNull Player player, @NotNull String title, @Nullable ItemStack[] items) {
        return openInventory(player, StringHolder.of(title), items);
    }

    public abstract Inventory openInventory(@NotNull Player player, @NotNull TextHolder title, @Nullable ItemStack[] items);

    /**
     * Sends the top items to the inventory for the specified player.
     *
     * @param player the player for which to open the grindstone
     * @param items the items to send
     * @param cursor the cursor item, this may be null on versions prior to 1.17.1
     * @since 0.8.0
     * @deprecated no longer used internally
     */
    @Deprecated
    public abstract void sendItems(@NotNull Player player, @Nullable ItemStack[] items, @Nullable ItemStack cursor);

    /**
     * Clears the cursor of the specified player
     *
     * @param player the player to clear the cursor of
     * @since 0.8.0
     * @deprecated no longer used internally
     */
    @Deprecated
    public abstract void clearCursor(@NotNull Player player);
}
