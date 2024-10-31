package com.github.stefvanschie.inventoryframework.util.version;

import com.github.stefvanschie.inventoryframework.abstraction.*;
import com.github.stefvanschie.inventoryframework.exception.UnsupportedVersionException;
import com.github.stefvanschie.inventoryframework.nms.v1_19_4.LegacySmithingTableInventoryImpl;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;

/**
 * Utility class containing versioning related methods.
 *
 * @since 0.8.0
 */
public class VersionMatcher {

    /**
     * The different anvil inventories for different versions
     */
    private static final EnumMap<Version, Class<? extends AnvilInventory>> ANVIL_INVENTORIES;

    /**
     * The different beacon inventories for different versions
     */
    private static final EnumMap<Version, Class<? extends BeaconInventory>> BEACON_INVENTORIES;

    /**
     * The different cartography table inventories for different versions
     */
    private static final EnumMap<Version, Class<? extends CartographyTableInventory>> CARTOGRAPHY_TABLE_INVENTORIES;

    /**
     * The different enchanting table inventories for different versions
     */
    private static final EnumMap<Version, Class<? extends EnchantingTableInventory>> ENCHANTING_TABLE_INVENTORIES;

    /**
     * The different grindstone inventories for different versions
     */
    private static final EnumMap<Version, Class<? extends GrindstoneInventory>> GRINDSTONE_INVENTORIES;

    /**
     * The different merchant inventories for different versions
     */
    private static final EnumMap<Version, Class<? extends MerchantInventory>> MERCHANT_INVENTORIES;

    /**
     * The different smithing table inventories for different versions
     */
    private static final EnumMap<Version, Class<? extends SmithingTableInventory>> SMITHING_TABLE_INVENTORIES;

    /**
     * The different legacy smithing table inventories for different versions
     */
    private static final EnumMap<Version, Class<? extends SmithingTableInventory>> LEGACY_SMITHING_TABLE_INVENTORIES;

    /**
     * The different stonecutter inventories for different versions
     */
    private static final EnumMap<Version, Class<? extends StonecutterInventory>> STONECUTTER_INVENTORIES;

    /**
     * Gets a new anvil inventory for the specified version of the specified inventory holder.
     *
     * @param version the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the anvil inventory
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public static AnvilInventory newAnvilInventory(@NotNull Version version, @NotNull InventoryHolder inventoryHolder) {
        try {
            return ANVIL_INVENTORIES.get(version).getConstructor(InventoryHolder.class).newInstance(inventoryHolder);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
            NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Gets a new beacon inventory for the specified version of the specified inventory holder.
     *
     * @param version the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the beacon inventory
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public static BeaconInventory newBeaconInventory(@NotNull Version version,
                                                     @NotNull InventoryHolder inventoryHolder) {
        try {
            return BEACON_INVENTORIES.get(version).getConstructor(InventoryHolder.class).newInstance(inventoryHolder);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
            NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Gets a new cartography table inventory for the specified version of the specified inventory holder.
     *
     * @param version the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the cartography table inventory
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public static CartographyTableInventory newCartographyTableInventory(@NotNull Version version,
                                                                         @NotNull InventoryHolder inventoryHolder) {
        try {
            Class<? extends CartographyTableInventory> clazz = CARTOGRAPHY_TABLE_INVENTORIES.get(version);

            return clazz.getConstructor(InventoryHolder.class).newInstance(inventoryHolder);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
            NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Gets a new enchanting table inventory for the specified version of the specified inventory holder.
     *
     * @param version the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the enchanting table inventory
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public static EnchantingTableInventory newEnchantingTableInventory(@NotNull Version version,
                                                                         @NotNull InventoryHolder inventoryHolder) {
        try {
            Class<? extends EnchantingTableInventory> clazz = ENCHANTING_TABLE_INVENTORIES.get(version);

            return clazz.getConstructor(InventoryHolder.class).newInstance(inventoryHolder);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
            NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Gets a new grindstone inventory for the specified version of the specified inventory holder.
     *
     * @param version the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the grindstone inventory
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public static GrindstoneInventory newGrindstoneInventory(@NotNull Version version,
                                                             @NotNull InventoryHolder inventoryHolder) {
        try {
            Class<? extends GrindstoneInventory> clazz = GRINDSTONE_INVENTORIES.get(version);

            return clazz.getConstructor(InventoryHolder.class).newInstance(inventoryHolder);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
            NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Gets a new merchant inventory for the specified version.
     *
     * @param version the version to get the inventory of
     * @return the merchant inventory
     * @since 0.10.1
     */
    @NotNull
    @Contract(pure = true)
    public static MerchantInventory newMerchantInventory(@NotNull Version version) {
        try {
            return MERCHANT_INVENTORIES.get(version).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Gets a new smithing table inventory for the specified version of the specified inventory holder. If a smithing
     * table is requested for a version that does not have smithing tables, an {@link UnsupportedVersionException} is
     * thrown.
     *
     * @param version the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the smithing table inventory
     * @since 0.10.9
     * @throws UnsupportedVersionException when a smithing table is requested on a version without smithing tables
     */
    @NotNull
    @Contract(pure = true)
    public static SmithingTableInventory newModernSmithingTableInventory(@NotNull Version version,
                                                                         @NotNull InventoryHolder inventoryHolder) {
        if (!version.existsModernSmithingTable() && !version.existsLegacySmithingTable()) {
            throw new UnsupportedVersionException("Smithing tables didn't exist in version " + version);
        }

        if (!version.existsModernSmithingTable()) {
            throw new UnsupportedVersionException("Modern smithing tables didn't exist in version " + version);
        }

        try {
            Class<? extends SmithingTableInventory> clazz = SMITHING_TABLE_INVENTORIES.get(version);

            return clazz.getConstructor(InventoryHolder.class).newInstance(inventoryHolder);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
            NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Gets a new legacy smithing table inventory for the specified version of the specified inventory holder. If a
     * smithing table is requested for a version that does not have smithing tables, an
     * {@link UnsupportedVersionException} is thrown.
     *
     * @param version the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the smithing table inventory
     * @since 0.8.0
     * @throws UnsupportedVersionException when a smithing table is requested on a version without smithing tables
     */
    @NotNull
    @Contract(pure = true)
    public static SmithingTableInventory newSmithingTableInventory(@NotNull Version version,
                                                                   @NotNull InventoryHolder inventoryHolder) {
        if (!version.existsModernSmithingTable() && !version.existsLegacySmithingTable()) {
            throw new UnsupportedVersionException("Smithing tables didn't exist in version " + version);
        }

        if (!version.existsLegacySmithingTable()) {
            throw new UnsupportedVersionException("Legacy smithing tables don't exist in version " + version);
        }

        try {
            Class<? extends SmithingTableInventory> clazz = LEGACY_SMITHING_TABLE_INVENTORIES.get(version);

            return clazz.getConstructor(InventoryHolder.class).newInstance(inventoryHolder);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Gets a new stonecutter inventory for the specified version of the specified inventory holder.
     *
     * @param version the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the stonecutter inventory
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public static StonecutterInventory newStonecutterInventory(@NotNull Version version,
                                                               @NotNull InventoryHolder inventoryHolder) {
        try {
            Class<? extends StonecutterInventory> clazz = STONECUTTER_INVENTORIES.get(version);

            return clazz.getConstructor(InventoryHolder.class).newInstance(inventoryHolder);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
            NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }

    static {
        ANVIL_INVENTORIES = new EnumMap<>(Version.class);
        ANVIL_INVENTORIES.put(Version.V1_14,
            com.github.stefvanschie.inventoryframework.nms.v1_14.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_15,
            com.github.stefvanschie.inventoryframework.nms.v1_15.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_16_1,
            com.github.stefvanschie.inventoryframework.nms.v1_16_1.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_16_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_16_2_3.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_16_4_5,
            com.github.stefvanschie.inventoryframework.nms.v1_16_4_5.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_17_0,
            com.github.stefvanschie.inventoryframework.nms.v1_17_0.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_17_1,
            com.github.stefvanschie.inventoryframework.nms.v1_17_1.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_18_0,
            com.github.stefvanschie.inventoryframework.nms.v1_18_0.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_18_1,
            com.github.stefvanschie.inventoryframework.nms.v1_18_1.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_18_2,
            com.github.stefvanschie.inventoryframework.nms.v1_18_2.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_19_0,
            com.github.stefvanschie.inventoryframework.nms.v1_19_0.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_19_1,
            com.github.stefvanschie.inventoryframework.nms.v1_19_1.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_19_2,
            com.github.stefvanschie.inventoryframework.nms.v1_19_2.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_19_3,
            com.github.stefvanschie.inventoryframework.nms.v1_19_3.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_19_4,
            com.github.stefvanschie.inventoryframework.nms.v1_19_4.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_20_0,
            com.github.stefvanschie.inventoryframework.nms.v1_20_0.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_20_1,
            com.github.stefvanschie.inventoryframework.nms.v1_20_1.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_20_2,
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_20_3_4,
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_20_5,
            com.github.stefvanschie.inventoryframework.nms.v1_20_5.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_20_6,
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_21_0,
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_21_1,
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.AnvilInventoryImpl.class);
        ANVIL_INVENTORIES.put(Version.V1_21_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.AnvilInventoryImpl.class);

        BEACON_INVENTORIES = new EnumMap<>(Version.class);
        BEACON_INVENTORIES.put(Version.V1_14,
            com.github.stefvanschie.inventoryframework.nms.v1_14.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_15,
            com.github.stefvanschie.inventoryframework.nms.v1_15.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_16_1,
            com.github.stefvanschie.inventoryframework.nms.v1_16_1.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_16_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_16_2_3.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_16_4_5,
            com.github.stefvanschie.inventoryframework.nms.v1_16_4_5.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_17_0,
            com.github.stefvanschie.inventoryframework.nms.v1_17_0.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_17_1,
            com.github.stefvanschie.inventoryframework.nms.v1_17_1.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_18_0,
            com.github.stefvanschie.inventoryframework.nms.v1_18_0.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_18_1,
            com.github.stefvanschie.inventoryframework.nms.v1_18_1.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_18_2,
            com.github.stefvanschie.inventoryframework.nms.v1_18_2.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_19_0,
            com.github.stefvanschie.inventoryframework.nms.v1_19_0.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_19_1,
            com.github.stefvanschie.inventoryframework.nms.v1_19_1.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_19_2,
            com.github.stefvanschie.inventoryframework.nms.v1_19_2.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_19_3,
            com.github.stefvanschie.inventoryframework.nms.v1_19_3.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_19_4,
            com.github.stefvanschie.inventoryframework.nms.v1_19_4.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_20_0,
            com.github.stefvanschie.inventoryframework.nms.v1_20_0.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_20_1,
            com.github.stefvanschie.inventoryframework.nms.v1_20_1.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_20_2,
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_20_3_4,
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_20_5,
            com.github.stefvanschie.inventoryframework.nms.v1_20_5.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_20_6,
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_21_0,
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_21_1,
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.BeaconInventoryImpl.class);
        BEACON_INVENTORIES.put(Version.V1_21_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.BeaconInventoryImpl.class);

        CARTOGRAPHY_TABLE_INVENTORIES = new EnumMap<>(Version.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_14,
            com.github.stefvanschie.inventoryframework.nms.v1_14.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_15,
            com.github.stefvanschie.inventoryframework.nms.v1_15.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_16_1,
            com.github.stefvanschie.inventoryframework.nms.v1_16_1.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_16_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_16_2_3.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_16_4_5,
            com.github.stefvanschie.inventoryframework.nms.v1_16_4_5.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_17_0,
            com.github.stefvanschie.inventoryframework.nms.v1_17_0.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_17_1,
            com.github.stefvanschie.inventoryframework.nms.v1_17_1.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_18_0,
            com.github.stefvanschie.inventoryframework.nms.v1_18_0.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_18_1,
            com.github.stefvanschie.inventoryframework.nms.v1_18_1.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_18_2,
            com.github.stefvanschie.inventoryframework.nms.v1_18_2.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_19_0,
            com.github.stefvanschie.inventoryframework.nms.v1_19_0.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_19_1,
            com.github.stefvanschie.inventoryframework.nms.v1_19_1.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_19_2,
            com.github.stefvanschie.inventoryframework.nms.v1_19_2.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_19_3,
            com.github.stefvanschie.inventoryframework.nms.v1_19_3.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_19_4,
            com.github.stefvanschie.inventoryframework.nms.v1_19_4.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_20_0,
            com.github.stefvanschie.inventoryframework.nms.v1_20_0.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_20_1,
            com.github.stefvanschie.inventoryframework.nms.v1_20_1.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_20_2,
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_20_3_4,
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_20_5,
            com.github.stefvanschie.inventoryframework.nms.v1_20_5.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_20_6,
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_21_0,
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_21_1,
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.CartographyTableInventoryImpl.class);
        CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_21_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.CartographyTableInventoryImpl.class);

        ENCHANTING_TABLE_INVENTORIES = new EnumMap<>(Version.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_14,
            com.github.stefvanschie.inventoryframework.nms.v1_14.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_15,
            com.github.stefvanschie.inventoryframework.nms.v1_15.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_16_1,
            com.github.stefvanschie.inventoryframework.nms.v1_16_1.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_16_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_16_2_3.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_16_4_5,
            com.github.stefvanschie.inventoryframework.nms.v1_16_4_5.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_17_0,
            com.github.stefvanschie.inventoryframework.nms.v1_17_0.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_17_1,
            com.github.stefvanschie.inventoryframework.nms.v1_17_1.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_18_0,
            com.github.stefvanschie.inventoryframework.nms.v1_18_0.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_18_1,
            com.github.stefvanschie.inventoryframework.nms.v1_18_1.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_18_2,
            com.github.stefvanschie.inventoryframework.nms.v1_18_2.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_19_0,
            com.github.stefvanschie.inventoryframework.nms.v1_19_0.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_19_1,
            com.github.stefvanschie.inventoryframework.nms.v1_19_1.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_19_2,
            com.github.stefvanschie.inventoryframework.nms.v1_19_2.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_19_3,
            com.github.stefvanschie.inventoryframework.nms.v1_19_3.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_19_4,
            com.github.stefvanschie.inventoryframework.nms.v1_19_4.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_20_0,
            com.github.stefvanschie.inventoryframework.nms.v1_20_0.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_20_1,
            com.github.stefvanschie.inventoryframework.nms.v1_20_1.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_20_2,
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_20_3_4,
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_20_5,
            com.github.stefvanschie.inventoryframework.nms.v1_20_5.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_20_6,
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_21_0,
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_21_1,
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.EnchantingTableInventoryImpl.class);
        ENCHANTING_TABLE_INVENTORIES.put(Version.V1_21_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.EnchantingTableInventoryImpl.class);

        GRINDSTONE_INVENTORIES = new EnumMap<>(Version.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_14,
            com.github.stefvanschie.inventoryframework.nms.v1_14.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_15,
            com.github.stefvanschie.inventoryframework.nms.v1_15.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_16_1,
            com.github.stefvanschie.inventoryframework.nms.v1_16_1.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_16_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_16_2_3.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_16_4_5,
            com.github.stefvanschie.inventoryframework.nms.v1_16_4_5.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_17_0,
            com.github.stefvanschie.inventoryframework.nms.v1_17_0.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_17_1,
            com.github.stefvanschie.inventoryframework.nms.v1_17_1.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_18_0,
            com.github.stefvanschie.inventoryframework.nms.v1_18_0.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_18_1,
            com.github.stefvanschie.inventoryframework.nms.v1_18_1.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_18_2,
            com.github.stefvanschie.inventoryframework.nms.v1_18_2.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_19_0,
            com.github.stefvanschie.inventoryframework.nms.v1_19_0.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_19_1,
            com.github.stefvanschie.inventoryframework.nms.v1_19_1.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_19_2,
            com.github.stefvanschie.inventoryframework.nms.v1_19_2.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_19_3,
            com.github.stefvanschie.inventoryframework.nms.v1_19_3.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_19_4,
            com.github.stefvanschie.inventoryframework.nms.v1_19_4.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_20_0,
            com.github.stefvanschie.inventoryframework.nms.v1_20_0.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_20_1,
            com.github.stefvanschie.inventoryframework.nms.v1_20_1.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_20_2,
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_20_3_4,
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_20_5,
            com.github.stefvanschie.inventoryframework.nms.v1_20_5.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_20_6,
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_21_0,
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_21_1,
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.GrindstoneInventoryImpl.class);
        GRINDSTONE_INVENTORIES.put(Version.V1_21_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.GrindstoneInventoryImpl.class);

        MERCHANT_INVENTORIES = new EnumMap<>(Version.class);
        MERCHANT_INVENTORIES.put(Version.V1_14,
            com.github.stefvanschie.inventoryframework.nms.v1_14.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_15,
            com.github.stefvanschie.inventoryframework.nms.v1_15.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_16_1,
            com.github.stefvanschie.inventoryframework.nms.v1_16_1.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_16_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_16_2_3.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_16_4_5,
            com.github.stefvanschie.inventoryframework.nms.v1_16_4_5.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_17_0,
            com.github.stefvanschie.inventoryframework.nms.v1_17_0.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_17_1,
            com.github.stefvanschie.inventoryframework.nms.v1_17_1.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_18_0,
            com.github.stefvanschie.inventoryframework.nms.v1_18_0.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_18_1,
            com.github.stefvanschie.inventoryframework.nms.v1_18_1.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_18_2,
            com.github.stefvanschie.inventoryframework.nms.v1_18_2.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_19_0,
            com.github.stefvanschie.inventoryframework.nms.v1_19_0.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_19_1,
            com.github.stefvanschie.inventoryframework.nms.v1_19_1.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_19_2,
            com.github.stefvanschie.inventoryframework.nms.v1_19_2.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_19_3,
            com.github.stefvanschie.inventoryframework.nms.v1_19_3.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_19_4,
            com.github.stefvanschie.inventoryframework.nms.v1_19_4.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_20_0,
            com.github.stefvanschie.inventoryframework.nms.v1_20_0.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_20_1,
            com.github.stefvanschie.inventoryframework.nms.v1_20_1.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_20_2,
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_20_3_4,
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_20_5,
            com.github.stefvanschie.inventoryframework.nms.v1_20_5.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_20_6,
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_21_0,
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_21_1,
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.MerchantInventoryImpl.class);
        MERCHANT_INVENTORIES.put(Version.V1_21_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.MerchantInventoryImpl.class);

        SMITHING_TABLE_INVENTORIES = new EnumMap<>(Version.class);
        SMITHING_TABLE_INVENTORIES.put(Version.V1_19_4,
            com.github.stefvanschie.inventoryframework.nms.v1_19_4.SmithingTableInventoryImpl.class);
        SMITHING_TABLE_INVENTORIES.put(Version.V1_20_0,
            com.github.stefvanschie.inventoryframework.nms.v1_20_0.SmithingTableInventoryImpl.class);
        SMITHING_TABLE_INVENTORIES.put(Version.V1_20_1,
            com.github.stefvanschie.inventoryframework.nms.v1_20_1.SmithingTableInventoryImpl.class);
        SMITHING_TABLE_INVENTORIES.put(Version.V1_20_2,
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.SmithingTableInventoryImpl.class);
        SMITHING_TABLE_INVENTORIES.put(Version.V1_20_3_4,
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.SmithingTableInventoryImpl.class);
        SMITHING_TABLE_INVENTORIES.put(Version.V1_20_5,
            com.github.stefvanschie.inventoryframework.nms.v1_20_5.SmithingTableInventoryImpl.class);
        SMITHING_TABLE_INVENTORIES.put(Version.V1_20_6,
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.SmithingTableInventoryImpl.class);
        SMITHING_TABLE_INVENTORIES.put(Version.V1_21_0,
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.SmithingTableInventoryImpl.class);
        SMITHING_TABLE_INVENTORIES.put(Version.V1_21_1,
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.SmithingTableInventoryImpl.class);
        SMITHING_TABLE_INVENTORIES.put(Version.V1_21_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.SmithingTableInventoryImpl.class);

        LEGACY_SMITHING_TABLE_INVENTORIES = new EnumMap<>(Version.class);
        LEGACY_SMITHING_TABLE_INVENTORIES.put(Version.V1_16_1,
            com.github.stefvanschie.inventoryframework.nms.v1_16_1.SmithingTableInventoryImpl.class);
        LEGACY_SMITHING_TABLE_INVENTORIES.put(Version.V1_16_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_16_2_3.SmithingTableInventoryImpl.class);
        LEGACY_SMITHING_TABLE_INVENTORIES.put(Version.V1_16_4_5,
            com.github.stefvanschie.inventoryframework.nms.v1_16_4_5.SmithingTableInventoryImpl.class);
        LEGACY_SMITHING_TABLE_INVENTORIES.put(Version.V1_17_0,
            com.github.stefvanschie.inventoryframework.nms.v1_17_0.SmithingTableInventoryImpl.class);
        LEGACY_SMITHING_TABLE_INVENTORIES.put(Version.V1_17_1,
            com.github.stefvanschie.inventoryframework.nms.v1_17_1.SmithingTableInventoryImpl.class);
        LEGACY_SMITHING_TABLE_INVENTORIES.put(Version.V1_18_0,
            com.github.stefvanschie.inventoryframework.nms.v1_18_0.SmithingTableInventoryImpl.class);
        LEGACY_SMITHING_TABLE_INVENTORIES.put(Version.V1_18_1,
            com.github.stefvanschie.inventoryframework.nms.v1_18_1.SmithingTableInventoryImpl.class);
        LEGACY_SMITHING_TABLE_INVENTORIES.put(Version.V1_18_2,
            com.github.stefvanschie.inventoryframework.nms.v1_18_2.SmithingTableInventoryImpl.class);
        LEGACY_SMITHING_TABLE_INVENTORIES.put(Version.V1_19_0,
            com.github.stefvanschie.inventoryframework.nms.v1_19_0.SmithingTableInventoryImpl.class);
        LEGACY_SMITHING_TABLE_INVENTORIES.put(Version.V1_19_1,
            com.github.stefvanschie.inventoryframework.nms.v1_19_1.SmithingTableInventoryImpl.class);
        LEGACY_SMITHING_TABLE_INVENTORIES.put(Version.V1_19_2,
            com.github.stefvanschie.inventoryframework.nms.v1_19_2.SmithingTableInventoryImpl.class);
        LEGACY_SMITHING_TABLE_INVENTORIES.put(Version.V1_19_3,
            com.github.stefvanschie.inventoryframework.nms.v1_19_3.SmithingTableInventoryImpl.class);
        LEGACY_SMITHING_TABLE_INVENTORIES.put(Version.V1_19_4,
            LegacySmithingTableInventoryImpl.class);

        STONECUTTER_INVENTORIES = new EnumMap<>(Version.class);
        STONECUTTER_INVENTORIES.put(Version.V1_14,
            com.github.stefvanschie.inventoryframework.nms.v1_14.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_15,
            com.github.stefvanschie.inventoryframework.nms.v1_15.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_16_1,
            com.github.stefvanschie.inventoryframework.nms.v1_16_1.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_16_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_16_2_3.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_16_4_5,
            com.github.stefvanschie.inventoryframework.nms.v1_16_4_5.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_17_0,
            com.github.stefvanschie.inventoryframework.nms.v1_17_0.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_17_1,
            com.github.stefvanschie.inventoryframework.nms.v1_17_1.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_18_0,
            com.github.stefvanschie.inventoryframework.nms.v1_18_0.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_18_1,
            com.github.stefvanschie.inventoryframework.nms.v1_18_1.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_18_2,
            com.github.stefvanschie.inventoryframework.nms.v1_18_2.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_19_0,
            com.github.stefvanschie.inventoryframework.nms.v1_19_0.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_19_1,
            com.github.stefvanschie.inventoryframework.nms.v1_19_1.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_19_2,
            com.github.stefvanschie.inventoryframework.nms.v1_19_2.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_19_3,
            com.github.stefvanschie.inventoryframework.nms.v1_19_3.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_19_4,
            com.github.stefvanschie.inventoryframework.nms.v1_19_4.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_20_0,
            com.github.stefvanschie.inventoryframework.nms.v1_20_0.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_20_1,
            com.github.stefvanschie.inventoryframework.nms.v1_20_1.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_20_2,
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_20_3_4,
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_20_5,
            com.github.stefvanschie.inventoryframework.nms.v1_20_5.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_20_6,
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_21_0,
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_21_1,
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.StonecutterInventoryImpl.class);
        STONECUTTER_INVENTORIES.put(Version.V1_21_2_3,
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.StonecutterInventoryImpl.class);
    }
}
