package com.github.stefvanschie.inventoryframework.abstraction;

import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An anvil inventory
 *
 * @since 0.8.0
 */
public abstract class AnvilInventory {

    /**
     * The inventory holder
     */
    @NotNull
    protected InventoryHolder inventoryHolder;

    /**
     * The rename text
     */
    @NotNull
    protected String text = "";

    /**
     * Creates a new anvil inventory for the specified inventory holder
     *
     * @param inventoryHolder the inventory holder
     * @since 0.8.0
     */
    public AnvilInventory(@NotNull InventoryHolder inventoryHolder) {
        this.inventoryHolder = inventoryHolder;
    }

    /**
     * Opens the inventory for the specified player
     *
     * @param player the player to open the inventory for
     * @param title the title of the inventory
     * @param items the items to show
     * @since 0.8.0
     */
    public final void openInventory(@NotNull Player player, @NotNull String title, @Nullable ItemStack[] items) {
        openInventory(player, StringHolder.of(title), items);
    }

    public abstract void openInventory(@NotNull Player player, @NotNull TextHolder title, @Nullable ItemStack[] items);

    /**
     * Sends the top items to the inventory for the specified player.
     *
     * @param player the player for which to open the anvil
     * @param items the items to send
     * @since 0.8.0
     */
    public abstract void sendItems(@NotNull Player player, @Nullable ItemStack[] items);

    /**
     * Sends the result item to the specified player
     *
     * @param player the player to send the item to
     * @param item the item to send
     * @since 0.8.0
     */
    public abstract void sendResultItem(@NotNull Player player, @Nullable ItemStack item);

    /**
     * Sends the first item to the specified player
     *
     * @param player the player to send the item to
     * @param item the item to send
     * @since 0.8.0
     */
    public abstract void sendFirstItem(@NotNull Player player, @Nullable ItemStack item);

    /**
     * Sends the second item to the specified player
     *
     * @param player the player to send the item to
     * @param item the item to send
     * @since 0.8.0
     */
    public abstract void sendSecondItem(@NotNull Player player, @Nullable ItemStack item);

    /**
     * Sets the cursor of the given player
     *
     * @param player the player to set the cursor
     * @param item the item to set the cursor to
     * @since 0.8.0
     */
    public abstract void setCursor(@NotNull Player player, @NotNull ItemStack item);

    /**
     * Clears the cursor of the specified player
     *
     * @param player the player to clear the cursor of
     * @since 0.8.0
     */
    public abstract void clearCursor(@NotNull Player player);

    /**
     * Clears the result item for the specified player
     *
     * @param player the player to clear the result item of
     * @since 0.8.0
     */
    public abstract void clearResultItem(@NotNull Player player);

    /**
     * Gets the text shown in the rename slot of the anvil
     *
     * @return the rename text
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public String getRenameText() {
        return text;
    }
}
