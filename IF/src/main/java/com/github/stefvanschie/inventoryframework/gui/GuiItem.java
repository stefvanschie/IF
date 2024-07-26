package com.github.stefvanschie.inventoryframework.gui;

import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil;
import com.github.stefvanschie.inventoryframework.util.UUIDTagType;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
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
     * The logger to log errors with
     */
    @NotNull
    private final Logger logger;

    /**
     * The {@link NamespacedKey} that specifies the location of the (internal) {@link UUID} in {@link PersistentDataContainer}s.
     * The {@link PersistentDataType} that should be used is {@link UUIDTagType}.
     */
    @NotNull
    private final NamespacedKey keyUUID;

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
    private ItemStack item;

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
     * @param plugin the owning plugin of this item
     * @see #GuiItem(ItemStack, Consumer)
     * @since 0.10.8
     */
    public GuiItem(@NotNull ItemStack item, @Nullable Consumer<InventoryClickEvent> action, @NotNull Plugin plugin) {
        this(item, action, plugin.getLogger(), new NamespacedKey(plugin, "IF-uuid"));
    }

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     * @param plugin the owning plugin of this item
     * @see #GuiItem(ItemStack)
     * @since 0.10.8
     */
    public GuiItem(@NotNull ItemStack item, @NotNull Plugin plugin) {
        this(item, event -> {}, plugin);
    }

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     * @param action the action called whenever an interaction with this item happens
     */
    public GuiItem(@NotNull ItemStack item, @Nullable Consumer<InventoryClickEvent> action) {
        this(item, action, JavaPlugin.getProvidingPlugin(GuiItem.class));
    }

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     */
    public GuiItem(@NotNull ItemStack item) {
        this(item, event -> {});
    }

    /**
     * Creates a new gui item based on the given item, action, logger, and key. The logger will be used for logging
     * exceptions and the key is used for identification of this item.
     *
     * @param item the item stack
     * @param action the action called whenever an interaction with this item happens
     * @param logger the logger used for logging exceptions
     * @param key the key to identify this item with
     * @since 0.10.10
     */
    private GuiItem(@NotNull ItemStack item, @Nullable Consumer<InventoryClickEvent> action, @NotNull Logger logger,
                    @NotNull NamespacedKey key) {
        this.logger = logger;
        this.keyUUID = key;
        this.action = action;
        this.visible = true;
        this.properties = new ArrayList<>();
        this.item = item;

        //remove this call after the removal of InventoryComponent#setItem(ItemStack, int, int)
        applyUUID();
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
        GuiItem guiItem = new GuiItem(item.clone(), action, this.logger, this.keyUUID);

        guiItem.visible = visible;
        guiItem.uuid = uuid;
        guiItem.properties = new ArrayList<>(properties);
        ItemMeta meta = guiItem.item.getItemMeta();

        if (meta != null) {
            meta.getPersistentDataContainer().set(keyUUID, UUIDTagType.INSTANCE, guiItem.uuid);
            guiItem.item.setItemMeta(meta);
        }

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
            this.logger.log(Level.SEVERE, "Exception while handling click event in inventory '"
                    + InventoryViewUtil.getInstance().getTitle(event.getView()) + "', slot=" + event.getSlot() +
                    ", item=" + item.getType(), t);
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
            meta.getPersistentDataContainer().set(this.keyUUID, UUIDTagType.INSTANCE, uuid);
            item.setItemMeta(meta);
        }
    }

    /**
     * Overwrites the current item with the provided item.
     *
     * @param item the item to set
     * @since 0.10.8
     */
    public void setItem(@NotNull ItemStack item) {
        this.item = item;
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
     * Gets the namespaced key used for this item.
     *
     * @return the namespaced key
     * @since 0.10.8
     */
    @NotNull
    @Contract(pure = true)
    public NamespacedKey getKey() {
        return keyUUID;
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
