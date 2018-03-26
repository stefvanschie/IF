package com.gmail.stefvanschiedev.inventoryframework;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * An item for in an inventory
 *
 * @since 5.6.0
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
     * The tag assigned to this item, null if no tag has been assigned
     */
    @Nullable
    private String tag;

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
     * @since 5.6.0
     */
    public GuiItem(@NotNull ItemStack item) {
        this.item = item;
        this.visible = true;
    }

    /**
     * Returns the action for this item
     *
     * @return the action called when clicked on this item
     * @since 5.6.0
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
     * @since 5.6.0
     */
    @NotNull
    @Contract(pure = true)
    public ItemStack getItem() {
        return item;
    }

    /**
     * Returns the tag that belongs to this item, or null if no tag has been assigned
     *
     * @return the tag or null
     * @since 5.6.0
     */
    @Nullable
    @Contract(pure = true)
    public String getTag() {
        return tag;
    }

    /**
     * Returns whether or not this item is visible
     *
     * @return true if this item is visible, false otherwise
     * @since 5.6.0
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the tag of this item to the new tag or removes it when the parameter is null
     *
     * @param tag the new tag
     * @since 5.6.0
     */
    public void setTag(@Nullable String tag) {
        this.tag = tag;
    }

    /**
     * Sets the visibility of this item to the new visibility
     *
     * @param visible the new visibility
     * @since 5.6.0
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}