package com.github.stefvanschie.inventoryframework.util;

import com.github.stefvanschie.inventoryframework.util.version.Version;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
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
        if (Version.getVersion().isOlderThan(Version.V1_18_2)) {
            setSkullReflection(meta, id);
        } else {
            setSkullProfile(meta, id);
        }
    }

    /**
     * Sets the skull's texture based on the player profile API introduced in 1.18.2.
     *
     * @param meta the {@link ItemMeta} of the skull to set
     * @param id the ID of the skin URL to apply
     * @since 0.11.6
     */
    private static void setSkullProfile(@NotNull ItemMeta meta, @NotNull String id) {
        if (!(meta instanceof SkullMeta)) {
            throw new IllegalArgumentException("Provided item meta is not of a skull");
        }

        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();

        try {
            textures.setSkin(new URL("http://textures.minecraft.net/texture/" + id));
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Provided ID is invalid", exception);
        }

        profile.setTextures(textures);

        ((SkullMeta) meta).setOwnerProfile(profile);
    }

    /**
     * Sets the skull's texture via reflection.
     *
     * @param meta the {@link ItemMeta} of the skull to set
     * @param id the ID of the skin URL to apply
     * @since 0.11.6
     */
    private static void setSkullReflection(@NotNull ItemMeta meta, @NotNull String id) {
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
