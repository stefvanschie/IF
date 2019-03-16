package com.github.stefvanschie.inventoryframework.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
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
        ItemMeta itemMeta = item.getItemMeta();

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}",
            "http://textures.minecraft.net/texture/" + id).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));

        try {
            Field profileField = itemMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(itemMeta, profile);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException exception) {
            exception.printStackTrace();
        }

        item.setItemMeta(itemMeta);

        return item;
    }
}
