package com.github.stefvanschie.inventoryframework.gui;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

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
    @Nullable
    private Consumer<InventoryClickEvent> action;
    
    /**
     * List of item's properties
     */
    @NotNull
    private List<Object> properties;

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
    private UUID uuid = UUID.randomUUID();

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     * @param action the action called whenever an interaction with this item happens
     */
    public GuiItem(@NotNull ItemStack item, @Nullable Consumer<InventoryClickEvent> action) {
        this.action = action;
        this.visible = true;
        this.properties = new ArrayList<>();

        //remove this call after the removal of InventoryComponent#setItem(ItemStack, int, int)
        applyUUID();

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
     * Makes a copy of this gui item and returns it. This makes a deep copy of the gui item. This entails that the
     * underlying item will be copied as per their {@link ItemStack#clone()} and miscellaneous data will be copied in
     * such a way that they are identical. The returned gui item will never be reference equal to the current gui item.
     *
     * @return a copy of the gui item
     * @since 0.6.2
     */
    @NotNull
    @Contract(pure = true)
    public GuiItem copy() {
        GuiItem guiItem = new GuiItem(item.clone(), action);

        guiItem.visible = visible;
        guiItem.uuid = uuid;
        guiItem.properties = new ArrayList<>(properties);
        ItemMeta meta = guiItem.item.getItemMeta();

        if (meta == null) {
            throw new IllegalArgumentException("item must be able to have ItemMeta (it mustn't be AIR)");
        }

        meta.getPersistentDataContainer().set(KEY_UUID, UUIDTagType.INSTANCE, guiItem.uuid);
        guiItem.item.setItemMeta(meta);

        return guiItem;
    }

    /**
     * Calls the handler of the {@link InventoryClickEvent}
     * if such a handler was specified in the constructor.
     * Catches and logs all exceptions the handler might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    public void callAction(@NotNull InventoryClickEvent event) {
        if (action == null) {
            return;
        }

        try {
            action.accept(event);
        } catch (Throwable t) {
            Logger logger = JavaPlugin.getProvidingPlugin(getClass()).getLogger();
            logger.log(Level.SEVERE, "Exception while handling click event in inventory '"
                    + event.getView().getTitle() + "', slot=" + event.getSlot() + ", item=" + item.getType(), t);
        }
    }

    /**
     * Sets the internal UUID of this gui item onto the underlying item. Previously set UUID will be overwritten by the
     * current UUID. If the underlying item does not have an item meta, this method will silently do nothing.
     *
     * @since 0.9.3
     */
    public void applyUUID() {
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.getPersistentDataContainer().set(KEY_UUID, UUIDTagType.INSTANCE, uuid);
            item.setItemMeta(meta);
        }
    }

    /**
     * Sets the action to be executed when a human entity clicks on this item.
     *
     * @param action the action of this item
     * @since 0.7.1
     */
    public void setAction(@NotNull Consumer<InventoryClickEvent> action) {
        this.action = action;
    }
    
    /**
     * Returns the list of properties
     *
     * @return the list of properties that belong to this gui item
     * @since 0.7.2
     */
    @NotNull
    @Contract(pure = true)
    public List<Object> getProperties(){
        return properties;
    }
    
    /**
     * Sets the list of properties for this gui item
     *
     * @param properties list of new properties
     * @since 0.7.2
     */
    public void setProperties(@NotNull List<Object> properties){
        this.properties = properties;
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
