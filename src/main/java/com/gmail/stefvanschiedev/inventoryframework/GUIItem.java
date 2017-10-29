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
public class GUIItem {

    /**
     * An action for the inventory
     */
    @Nullable private Consumer<InventoryClickEvent> action;

    /**
     * The items shown
     */
    @NotNull private final ItemStack item;

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     * @param action the action called whenever an interaction with this item happens
     */
    public GUIItem(@NotNull ItemStack item, @Nullable Consumer<InventoryClickEvent> action) {
        this.item = item;
        this.action = action;
    }

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     */
    public GUIItem(@NotNull ItemStack item) {
        this.item = item;
    }

    /**
     * @return the action called when clicked on this item
     */
    @Nullable
    @Contract(pure = true)
    public Consumer<InventoryClickEvent> getAction() {
        return action;
    }

    /**
     * @return the item that belongs to this gui item
     */
    @NotNull
    @Contract(pure = true)
    public ItemStack getItem() {
        return item;
    }
}