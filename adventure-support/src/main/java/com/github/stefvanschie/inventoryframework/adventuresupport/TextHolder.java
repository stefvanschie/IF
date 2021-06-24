package com.github.stefvanschie.inventoryframework.adventuresupport;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

/**
 * Immutable wrapper of a text-like value.
 * Support for both Adventure-based Paper
 * and non-Adventure Spigot is achieved through this class.
 * <p>
 * This class is an implementation detail.
 * It must not be exposed to the users of the IF library.
 * </p>
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
     * Deserializes the specified node's {@link Node#getTextContent()}
     * as a {@link TextHolder}.
     * This method is still WIP and may change drastically in the future:
     * <ul>
     *     <li>Should it take a {@link Node} or a {@link String} as parameter?</li>
     *     <li>Are we ever gonna use anything other than {@link Node#getTextContent()}?</li>
     *     <li>Are we going to use minimessage if it's present?</li>
     *     <li>Is minimessage going to be opt-in? If yes, how do we opt-in?</li>
     * </ul>
     *
     * @param node the node whose text content to deserialize
     * @return an instance containing the text from the node
     */
    @NotNull
    @Contract(pure = true)
    public static TextHolder fromNodeTextContent(@NotNull Node node) {
        String legacy = ChatColor.translateAlternateColorCodes('&', node.getTextContent());
        return StringHolder.of(legacy);
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
    public abstract Component asComponent();
    
    @NotNull
    @Contract(pure = true)
    public abstract Inventory asInventoryTitle(InventoryHolder holder, InventoryType type);
    
    @NotNull
    @Contract(pure = true)
    public abstract Inventory asInventoryTitle(InventoryHolder holder, int size);
    
    public abstract void asItemDisplayName(ItemMeta meta);
    
    public abstract void asItemLoreAtEnd(ItemMeta meta);
}
