package com.github.stefvanschie.inventoryframework.adventuresupport;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StringHolder extends TextHolder {
    
    @NotNull
    private static final StringHolder EMPTY = StringHolder.of("");
    
    @NotNull
    @Contract(pure = true)
    public static StringHolder of(@NotNull String value) {
        Validate.notNull(value, "value mustn't be null");
        return new StringHolder(value);
    }
    
    /**
     * Gets an instance that contains no characters.
     *
     * @return an instance without any characters
     */
    @NotNull
    @Contract(pure = true)
    public static StringHolder empty() {
        return EMPTY;
    }
    
    @NotNull
    private final String value;
    
    private StringHolder(@NotNull String value) {
        this.value = value;
    }
    
    @NotNull
    @Contract(pure = true)
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + value + "}";
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        return other != null && getClass() == other.getClass()
            && Objects.equals(value, ((StringHolder) other).value);
    }
    
    @NotNull
    @Contract(pure = true)
    @Override
    public String asLegacyString() {
        return value;
    }
    
    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory asInventoryTitle(InventoryHolder holder, InventoryType type) {
        //noinspection deprecation
        return Bukkit.createInventory(holder, type, value);
    }
    
    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory asInventoryTitle(InventoryHolder holder, int size) {
        //noinspection deprecation
        return Bukkit.createInventory(holder, size, value);
    }
    
    @Override
    public void asItemDisplayName(ItemMeta meta) {
        //noinspection deprecation
        meta.setDisplayName(value);
    }
    
    @Override
    public void asItemLoreAtEnd(ItemMeta meta) {
        //noinspection deprecation
        List<String> lore = meta.hasLore()
            ? Objects.requireNonNull(meta.getLore())
            : new ArrayList<>();
        lore.add(value);
        //noinspection deprecation
        meta.setLore(lore);
    }
}
