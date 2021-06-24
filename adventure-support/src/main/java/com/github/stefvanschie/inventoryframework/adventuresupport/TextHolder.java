package com.github.stefvanschie.inventoryframework.adventuresupport;

import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable wrapper of a text-like value.
 * Support for both Adventure and legacy strings is achieved through this class.
 *
 * @see StringHolder
 * @see ComponentHolder
 */
public abstract class TextHolder {
    
    /**
     * Gets an instance that contains no characters.
     *
     * @return an instance without any characters
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
    
    @NotNull
    @Contract(pure = true)
    public abstract String asLegacyString();
    
    @NotNull
    @Contract(pure = true)
    public abstract Inventory asInventoryTitle(InventoryHolder holder, InventoryType type);
    
    @NotNull
    @Contract(pure = true)
    public abstract Inventory asInventoryTitle(InventoryHolder holder, int size);
    
    public abstract void asItemDisplayName(ItemMeta meta);
    
    public abstract void asItemLoreAtEnd(ItemMeta meta);
}
