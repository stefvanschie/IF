package com.github.stefvanschie.inventoryframework.util;

import com.github.stefvanschie.inventoryframework.util.version.Version;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

/**
 * A utility class for working with skulls
 *
 * @since 0.5.0
 */
public final class SkullUtil {

    private static Field cachedProfileField;
    private static Method cachedSetProfileMethod;

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
        Version ver = Version.getVersion();
        if (!(meta instanceof org.bukkit.inventory.meta.SkullMeta skullMeta)) {
            throw new IllegalArgumentException("ItemMeta is not an instance of SkullMeta");
        }
        if (ver.name().contains("1_20") || ver.name().contains("1_21")) {
            final UUID uuid = UUID.randomUUID();
            final PlayerProfile playerProfile = Bukkit.getServer().createPlayerProfile(uuid, uuid.toString().substring(0, 16));
            PlayerTextures playerTextures = playerProfile.getTextures();
            try {
                playerTextures.setSkin(new URL("http://textures.minecraft.net/texture/" + id));
            }
            catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid URL for skin texture", e);
            }
            playerProfile.setTextures(playerTextures);
            skullMeta.setOwnerProfile(playerProfile);
        }
        else {
            com.mojang.authlib.GameProfile profile = new com.mojang.authlib.GameProfile(UUID.randomUUID(), "");
            byte[] encodedData = java.util.Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}",
                    "http://textures.minecraft.net/texture/" + id).getBytes());
            profile.getProperties().put("textures", new com.mojang.authlib.properties.Property("textures", new String(encodedData)));
            String itemDisplayName = skullMeta.getDisplayName();

            try {
                if (cachedProfileField == null) {
                    cachedProfileField = skullMeta.getClass().getDeclaredField("profile");
                    cachedProfileField.setAccessible(true);
                }
                cachedProfileField.set(skullMeta, profile);

                meta.setDisplayName(itemDisplayName);

                if (cachedSetProfileMethod == null) {
                    cachedSetProfileMethod = skullMeta.getClass().getDeclaredMethod("setProfile", com.mojang.authlib.GameProfile.class);
                    cachedSetProfileMethod.setAccessible(true);
                }
                cachedSetProfileMethod.invoke(skullMeta, profile);
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException ignored) {}
        }
    }
}
