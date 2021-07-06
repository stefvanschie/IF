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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Wrapper of an Adventure {@link Component}.
 *
 * @since 0.10.0
 */
public abstract class ComponentHolder extends TextHolder {
    
    /**
     * Whether the server platform natively supports Adventure.
     * A null value indicates that we don't yet know this: it hasn't been determined yet.
     * This field should not be used directly, use {@link #isNativeAdventureSupport()} instead.
     */
    @Nullable
    private static Boolean nativeAdventureSupport;
    
    /**
     * The serializer to use when converting wrapped values to legacy strings.
     * A null value indicates that we haven't created the serializer yet.
     * This field should not be used directly, use {@link #getLegacySerializer()} instead.
     */
    @Nullable
    private static LegacyComponentSerializer legacySerializer;
    
    /**
     * Wraps the specified Adventure component.
     *
     * @param value the value to wrap
     * @return an instance that wraps the specified value
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public static ComponentHolder of(@NotNull Component value) {
        Validate.notNull(value, "value mustn't be null");
        return isNativeAdventureSupport()
                ? new NativeComponentHolder(value)
                : new ForeignComponentHolder(value);
    }
    
    /**
     * Gets whether the server platform natively supports Adventure.
     * Native Adventure support means that eg. {@link ItemMeta#displayName(Component)}
     * is a valid method.
     *
     * @return whether the server platform natively supports Adventure
     * @since 0.10.0
     */
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
    
    /**
     * Gets the serializer to use when converting wrapped values to legacy strings.
     * Main use case being the implementation of {@link #asLegacyString()}.
     *
     * @return a serializer for converting wrapped values to legacy strings
     * @since 0.10.0
     */
    private static LegacyComponentSerializer getLegacySerializer() {
        if (legacySerializer == null) {
            LegacyComponentSerializer.Builder builder = LegacyComponentSerializer.builder()
                    .character(LegacyComponentSerializer.SECTION_CHAR);
            if (!net.md_5.bungee.api.ChatColor.class.isEnum()) {
                //1.16+ Spigot (or Paper), hex colors are supported, no need to down sample them
                builder.hexColors()
                        .useUnusualXRepeatedCharacterHexFormat();
            }
            legacySerializer = builder.build();
        }
        return legacySerializer;
    }
    
    /**
     * The Adventure component this instance wraps.
     */
    @NotNull
    protected final Component value;
    
    /**
     * Creates and initializes a new instance.
     *
     * @param value the Adventure component this instance should wrap
     * @since 0.10.0
     */
    ComponentHolder(@NotNull Component value) {
        this.value = value;
    }
    
    /**
     * Gets the Adventure component this instance wraps.
     *
     * @return the contained Adventure component
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public Component getComponent() {
        return value;
    }
    
    /**
     * Gets the wrapped Adventure component in a JSON representation.
     *
     * @return the contained Adventure component as JSON
     * @since 0.10.0
     */
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
        return getLegacySerializer().serialize(value);
    }
}
