package com.github.stefvanschie.inventoryframework;

import me.ialistannen.mininbt.ItemNBTUtil;
import me.ialistannen.mininbt.NBTWrappers;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * An item for in an inventory
 */
public class GuiItem {

    /**
     * An action for the inventory
     */
    @NotNull
    private final Consumer<InventoryClickEvent> action;

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
     * Internal UUID for keeping track of this item
     */
    @NotNull
    private final UUID uuid = UUID.randomUUID();

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     * @param action the action called whenever an interaction with this item happens
     */
    public GuiItem(@NotNull ItemStack item, @Nullable Consumer<InventoryClickEvent> action) {
        if (action == null) {
            this.action = event -> {};
        } else {
            this.action = action;
        }

        this.visible = true;

        NBTWrappers.NBTTagCompound compound = ItemNBTUtil.getTag(item);

        compound.setString("IF-uuid", uuid.toString());

        this.item = ItemNBTUtil.setNBTTag(compound, item);
    }

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     */
    public GuiItem(@NotNull ItemStack item) {
        this(item, null);
    }

    /**
     * Returns the action for this item
     *
     * @return the action called when clicked on this item
     */
    @NotNull
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
     * Gets the {@link UUID} associated with this {@link GuiItem}. This is for internal use only, and should not be
     * used.
     *
     * @return the {@link UUID} of this item
     * @since 0.5.9
     */
    @NotNull
    @Contract(pure = true)
    public UUID getUUID() {
        return uuid;
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
