package com.github.stefvanschie.inventoryframework.adventuresupport;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class ComponentHolder extends TextHolder {
    
    private static Boolean nativeAdventureSupport;
    
    @NotNull
    @Contract(pure = true)
    public static ComponentHolder of(@NotNull Component value) {
        Validate.notNull(value, "value mustn't be null");
        return isNativeAdventureSupport()
            ? new NativeComponentHolder(value)
            : new ForeignComponentHolder(value);
    }
    
    private static boolean isNativeAdventureSupport() {
        if (nativeAdventureSupport == null) {
            try {
                Component component = Component.text("test");
                NativeComponentHolder holder = new NativeComponentHolder(component);
                
                //If NoSuchMethodError or something is thrown we can assume that
                //Adventure components are not natively supported by the server platform
                
                //noinspection unused
                Object ignored1 = holder.asInventoryTitle(null, 9);
                //noinspection unused
                Object ignored2 = holder.asInventoryTitle(null, InventoryType.HOPPER);
                
                ItemMeta meta = new ItemStack(Material.STONE).getItemMeta();
                holder.asItemDisplayName(meta);
                holder.asItemLoreAtEnd(meta);
                
                nativeAdventureSupport = true;
            } catch (Throwable t) {
                nativeAdventureSupport = false;
            }
        }
        return nativeAdventureSupport;
    }
    
    @NotNull
    protected final Component value;
    
    ComponentHolder(@NotNull Component value) {
        this.value = value;
    }
    
    @NotNull
    @Contract(pure = true)
    public Component getComponent() {
        return value;
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
    @Override
    public String asLegacyString() {
        //TODO this down samples colors to the nearest ChatColor
        // is this a bug or a feature?
        return LegacyComponentSerializer.legacySection().serialize(value);
    }
}
