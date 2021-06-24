package com.github.stefvanschie.inventoryframework.adventuresupport;

import net.kyori.adventure.text.Component;
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

class NativeComponentHolder extends ComponentHolder {
    
    NativeComponentHolder(@NotNull Component value) {
        super(value);
    }
    
    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory asInventoryTitle(InventoryHolder holder, InventoryType type) {
        return Bukkit.createInventory(holder, type, value);
    }
    
    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory asInventoryTitle(InventoryHolder holder, int size) {
        return Bukkit.createInventory(holder, size, value);
    }
    
    @Override
    public void asItemDisplayName(ItemMeta meta) {
        meta.displayName(value);
    }
    
    @Override
    public void asItemLoreAtEnd(ItemMeta meta) {
        List<Component> lore = meta.hasLore()
            ? Objects.requireNonNull(meta.lore())
            : new ArrayList<>();
        lore.add(value);
        meta.lore(lore);
    }
}
