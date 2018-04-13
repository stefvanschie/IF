package com.gmail.stefvanschiedev.inventoryframework;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * An item for in an inventory
 */
public class GuiItem {

    /**
     * An action for the inventory
     */
    @Nullable
    private Consumer<InventoryClickEvent> action;

    /**
     * The items shown
     */
    @NotNull
    private final ItemStack item;

    /**
     * Whether this item is visible or not
     */
    private boolean visible;

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     * @param action the action called whenever an interaction with this item happens
     */
    public GuiItem(@NotNull ItemStack item, @Nullable Consumer<InventoryClickEvent> action) {
        this(item);

        this.action = action;
    }

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     */
    public GuiItem(@NotNull ItemStack item) {
        this.item = item;
        this.visible = true;
    }

    /**
     * Returns the action for this item
     *
     * @return the action called when clicked on this item
     */
    @Nullable
    @Contract(pure = true)
    public Consumer<InventoryClickEvent> getAction() {
        return action;
    }

    /**
     * Returns the item
     *
     * @return the item that belongs to this gui item
     */
    @NotNull
    @Contract(pure = true)
    public ItemStack getItem() {
        return item;
    }

    /**
     * Returns whether or not this item is visible
     *
     * @return true if this item is visible, false otherwise
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the visibility of this item to the new visibility
     *
     * @param visible the new visibility
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
