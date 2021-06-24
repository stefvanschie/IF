package com.github.stefvanschie.inventoryframework.adventuresupport;

import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

class ForeignComponentHolder extends ComponentHolder {
    
    private final StringHolder legacy;
    
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
    
    @Override
    public void asItemDisplayName(ItemMeta meta) {
        legacy.asItemDisplayName(meta);
    }
    
    @Override
    public void asItemLoreAtEnd(ItemMeta meta) {
        legacy.asItemLoreAtEnd(meta);
    }
}
