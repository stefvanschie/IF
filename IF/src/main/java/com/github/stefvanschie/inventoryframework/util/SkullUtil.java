package com.github.stefvanschie.inventoryframework.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * A utility class for working with skulls
 *
 * @since 0.5.0
 */
public final class SkullUtil {

    /**
     * A private constructor to ensure this class isn't instantiated
     *
     * @since 0.5.0
     */
    private SkullUtil() {}

    /**
     * Gets a skull from the specified id. The id is the value from the textures.minecraft.net website after the last
     * '/' character.
     *
     * @param id the skull id
     * @return the skull item
     * @since 0.5.0
     */
    @NotNull
    public static ItemStack getSkull(@NotNull String id) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta itemMeta = Objects.requireNonNull(item.getItemMeta());
        setSkull(itemMeta, id);
        item.setItemMeta(itemMeta);
        return item;
    }

    /**
     * Sets the skull of an existing {@link ItemMeta} from the specified id.
     * The id is the value from the textures.minecraft.net website after the last '/' character.
     *
     * @param meta the meta to change
     * @param id the skull id
     */
    public static void setSkull(@NotNull ItemMeta meta, @NotNull String id) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}",
            "http://textures.minecraft.net/texture/" + id).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        String itemDisplayName = meta.getDisplayName();

        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);

            meta.setDisplayName(itemDisplayName);

            // Sets serializedProfile field on meta
            // If it does throw NoSuchMethodException this stops, and meta is correct.
            // Else it has profile and will set the field.
            Method setProfile = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            setProfile.setAccessible(true);
            setProfile.invoke(meta, profile);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException ignored) {}
    }
}
