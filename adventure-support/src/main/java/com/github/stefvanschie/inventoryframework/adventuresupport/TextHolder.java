package com.github.stefvanschie.inventoryframework.adventuresupport;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable wrapper of a text-like value.
 * Support for both Adventure and legacy strings is achieved through this class.
 * To get an instance of this class please refer to either {@link StringHolder#of(String)}
 * or {@link ComponentHolder#of(Component)}.
 * Other methods like {@link #empty()} and {@link #deserialize(String)}
 * also exist, but their use cases are very limited.
 *
 * @see StringHolder
 * @see ComponentHolder
 * @since 0.10.0
 */
public abstract class TextHolder {
    
    /**
     * Gets an instance that contains no characters and no formatting.
     *
     * @return an instance without any characters or formatting
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public static TextHolder empty() {
        return StringHolder.empty();
    }
    
    /**
     * Deserializes the specified {@link String} as a {@link TextHolder}.
     * This method is still WIP and may change drastically in the future:
     * <ul>
     *     <li>Are we going to use MiniMessage if it's present?</li>
     *     <li>Is MiniMessage going to be opt-in? If yes, how do we opt-in?</li>
     * </ul>
     *
     * @param string the raw data to deserialize
     * @return an instance containing the text from the string
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public static TextHolder deserialize(@NotNull String string) {
        return StringHolder.of(ChatColor.translateAlternateColorCodes('&', string));
    }
    
    TextHolder() {
        //package-private constructor to "seal" the class
    }
    
    @NotNull
    @Contract(pure = true)
    @Override
    public abstract String toString();
    
    @Override
    public abstract int hashCode();
    
    @Override
    public abstract boolean equals(Object other);
    
    /**
     * Converts the text wrapped by this class instance to a legacy string,
     * keeping the original formatting.
     *
     * @return the wrapped value represented as a legacy string
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public abstract String asLegacyString();
    
    /**
     * Creates a new inventory with the wrapped value as the inventory's title.
     *
     * @param holder the holder to use for the new inventory
     * @param type the type of inventory to create
     * @return a newly created inventory with the wrapped value as its title
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public abstract Inventory asInventoryTitle(InventoryHolder holder, InventoryType type);
    
    /**
     * Creates a new inventory with the wrapped value as the inventory's title.
     *
     * @param holder the holder to use for the new inventory
     * @param size the count of slots the inventory should have (normal size restrictions apply)
     * @return a newly created inventory with the wrapped value as its title
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public abstract Inventory asInventoryTitle(InventoryHolder holder, int size);

    /**
     * Creates a new merchant with the wrapped value as the merchant's title.
     *
     * @return a newly created inventory with the wrapped value as its title
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public abstract Merchant asMerchantTitle();
    
    /**
     * Modifies the specified meta: sets the display name to the wrapped value.
     *
     * @param meta the meta whose display name to set
     * @since 0.10.0
     */
    public abstract void asItemDisplayName(ItemMeta meta);
    
    /**
     * Modifies the specified meta: adds the wrapped value as a new lore line at the end
     *
     * @param meta the meta whose lore to append to
     * @since 0.10.0
     */
    public abstract void asItemLoreAtEnd(ItemMeta meta);
}
