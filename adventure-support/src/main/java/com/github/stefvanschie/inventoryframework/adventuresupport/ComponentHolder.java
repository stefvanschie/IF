package com.github.stefvanschie.inventoryframework.adventuresupport;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

public final class ComponentHolder extends TextHolder {
    
    @NotNull
    @Contract(pure = true)
    public static ComponentHolder of(@NotNull Component value) {
        Validate.notNull(value, "value mustn't be null");
        return new ComponentHolder(value);
    }
    
    @NotNull
    private final Component value;
    
    private ComponentHolder(@NotNull Component value) {
        this.value = value;
    }
    
    @NotNull
    @Contract(pure = true)
    public JsonElement asJson() {
        return GsonComponentSerializer.gson().serializeToTree(value);
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
            && Objects.equals(value, ((ComponentHolder) other).value);
    }
    
    @NotNull
    @Contract(pure = true)
    public String asLegacyString() {
        return LegacyComponentSerializer.legacySection().serialize(value);
    }
    
    @NotNull
    @Contract(pure = true)
    public Component asComponent() {
        return value;
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
