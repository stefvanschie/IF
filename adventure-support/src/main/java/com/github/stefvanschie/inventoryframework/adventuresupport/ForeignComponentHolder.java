package com.github.stefvanschie.inventoryframework.adventuresupport;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ComponentHolder} implementation for platforms where Adventure isn't natively supported.
 * Adventure components are converted to legacy Strings before passed to the Bukkit API.
 *
 * @see NativeComponentHolder
 * @since 0.10.0
 */
class ForeignComponentHolder extends ComponentHolder {
    
    /**
     * A {@link StringHolder} wrapping {@link #asLegacyString()}.
     * This class depends on {@link StringHolder} to reduce code duplication.
     */
    @NotNull
    private final StringHolder legacy;
    
    /**
     * Creates and initializes a new instance.
     *
     * @param value the Adventure component this instance should wrap
     * @since 0.10.0
     */
    ForeignComponentHolder(@NotNull Component value) {
        super(value);
        legacy = StringHolder.of(asLegacyString());
    }
    
    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory asInventoryTitle(InventoryHolder holder, InventoryType type) {
        return legacy.asInventoryTitle(holder, type);
    }
    
    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory asInventoryTitle(InventoryHolder holder, int size) {
        return legacy.asInventoryTitle(holder, size);
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public Merchant asMerchantTitle() {
        return legacy.asMerchantTitle();
    }
    
    @Override
    public void asItemDisplayName(ItemMeta meta) {
        legacy.asItemDisplayName(meta);
    }
    
    @Override
    public void asItemLoreAtEnd(ItemMeta meta) {
        legacy.asItemLoreAtEnd(meta);
    }
}
