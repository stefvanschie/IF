package com.github.stefvanschie.inventoryframework;

import com.github.stefvanschie.inventoryframework.util.UUIDTagType;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
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
     * The {@link NamespacedKey} that specifies the location of the (internal) {@link UUID} in {@link PersistentDataContainer}s.
     * The {@link PersistentDataType} that should be used is {@link UUIDTagType}.
     */
    public static final NamespacedKey KEY_UUID = new NamespacedKey(JavaPlugin.getProvidingPlugin(GuiItem.class), "IF-uuid");

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
        this.action = action == null ? event -> {} : action;
        this.visible = true;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new IllegalArgumentException("item must be able to have ItemMeta (it mustn't be AIR)");
        }

        meta.getPersistentDataContainer().set(KEY_UUID, UUIDTagType.INSTANCE, uuid);
        item.setItemMeta(meta);
        this.item = item;
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
