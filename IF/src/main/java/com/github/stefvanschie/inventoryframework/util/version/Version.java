package com.github.stefvanschie.inventoryframework.util.version;

import com.github.stefvanschie.inventoryframework.exception.UnsupportedVersionException;
import org.bukkit.Bukkit;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;

/**
 * The different supported NMS versions
 *
 * @since 0.8.0
 */
public enum Version {

    /**
     * Version 1.14
     *
     * @since 0.10.0
     */
    V1_14,

    /**
     * Version 1.15
     *
     * @since 0.10.0
     */
    V1_15,

    /**
     * Version 1.16.1
     *
     * @since 0.10.0
     */
    V1_16_1,

    /**
     * Version 1.16.2 - 1.16.3
     *
     * @since 0.10.0
     */
    V1_16_2_3,

    /**
     * Version 1.16.4 - 1.16.5
     *
     * @since 0.10.0
     */
    V1_16_4_5,

    /**
     * Version 1.17
     *
     * @since 0.10.0
     */
    V1_17_0,

    /**
     * Version 1.17.1
     *
     * @since 0.10.0
     */
    V1_17_1,

    /**
     * Version 1.18.0
     *
     * @since 0.10.4
     */
    V1_18_0,

    /**
     * Version 1.18.1
     *
     * @since 0.10.4
     */
    V1_18_1,

    /**
     * Version 1.18.2
     *
     * @since 0.10.5
     */
    V1_18_2,

    /**
     * Version 1.19.0
     *
     * @since 0.10.6
     */
    V1_19_0,

    /**
     * Version 1.19.1
     *
     * @since 0.10.7
     */
    V1_19_1,

    /**
     * Version 1.19.2
     *
     * @since 0.10.7
     */
    V1_19_2,

    /**
     * Version 1.19.3
     *
     * @since 0.10.8
     */
    V1_19_3,

    /**
     * Version 1.19.4
     *
     * @since 0.10.9
     */
    V1_19_4,

    /**
     * Version 1.20.0
     *
     * @since 0.10.14
     */
    V1_20_0,

    /**
     * Version 1.20.1
     *
     * @since 0.10.14
     */
    V1_20_1,

    /**
     * Version 1.20.2
     *
     * @since 0.10.12
     */
    V1_20_2,

    /**
     * Version 1.20.3 - 1.20.4
     *
     * @since 0.10.13
     */
    V1_20_3_4,

    /**
     * Version 1.20.5
     *
     * @since 0.10.14
     */
    V1_20_5,

    /**
     * Version 1.20.6
     *
     * @since 0.10.14
     */
    V1_20_6,

    /**
     * Version 1.21.0
     *
     * @since 0.10.18
     */
    V1_21_0,

    /**
     * Version 1.21.1
     *
     * @since 0.10.18
     */
    V1_21_1,

    /**
     * Version 1.21.2 - 1.21.3
     *
     * @since 0.10.18
     */
    V1_21_2_3;

    /**
     * A collection of versions on which modern smithing tables are available.
     */
    private static final Collection<Version> MODERN_SMITHING_TABLE_VERSIONS = EnumSet.of(
            V1_19_4,
            V1_20_0, V1_20_1, V1_20_2, V1_20_3_4, V1_20_5, V1_20_6,
            V1_21_0, V1_21_1, V1_21_2_3
    );

    /**
     * A collection of versions on which legacy smithing tables ae available.
     */
    @NotNull
    private static final Collection<@NotNull Version> LEGACY_SMITHING_TABLE_VERSIONS = EnumSet.of(
            V1_14,
            V1_15,
            V1_16_1, V1_16_2_3, V1_16_4_5,
            V1_17_0, V1_17_1,
            V1_18_0, V1_18_1, V1_18_2,
            V1_19_0, V1_19_1, V1_19_2, V1_19_3, V1_19_4
    );

    /**
     * A collection of versions on which {@link InventoryView} is an interface.
     */
    @NotNull
    private static final Collection<@NotNull Version> INTERFACE_INVENTORY_VIEW = EnumSet.of(
            V1_21_0, V1_21_1, V1_21_2_3
    );

    /**
     * Checks whether the {@link InventoryView} class is an interface on this version.
     *
     * @return true if the class is an interface, false otherwise
     * @since 0.10.16
     */
    @Contract(pure = true)
    public boolean isInventoryViewInterface() {
        return INTERFACE_INVENTORY_VIEW.contains(this);
    }

    /**
     * Checks whether modern smithing tables exist on this version. Returns true if they do, otherwise false.
     *
     * @return true if modern smithing tables are available
     * @since 0.10.10
     */
    boolean existsModernSmithingTable() {
        return MODERN_SMITHING_TABLE_VERSIONS.contains(this);
    }

    /**
     * Checks whether legacy smithing tables exist on this version. Returns true if they do, otherwise false.
     *
     * @return true if legacy smithing tables are available
     * @since 0.10.10
     */
    @Contract(pure = true)
    boolean existsLegacySmithingTable() {
        return LEGACY_SMITHING_TABLE_VERSIONS.contains(this);
    }

    /**
     * Gets the version currently being used. If the used version is not supported, an
     * {@link UnsupportedVersionException} will be thrown.
     *
     * @return the version of the current instance
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public static Version getVersion() {
        String version = Bukkit.getBukkitVersion().split("-")[0];

        switch (version) {
            case "1.14":
            case "1.14.1":
            case "1.14.2":
            case "1.14.3":
            case "1.14.4":
                return V1_14;
            case "1.15":
            case "1.15.1":
            case "1.15.2":
                return V1_15;
            case "1.16.1":
                return V1_16_1;
            case "1.16.2":
            case "1.16.3":
                return V1_16_2_3;
            case "1.16.4":
            case "1.16.5":
                return V1_16_4_5;
            case "1.17":
                return V1_17_0;
            case "1.17.1":
                return V1_17_1;
            case "1.18":
                return V1_18_0;
            case "1.18.1":
                return V1_18_1;
            case "1.18.2":
                return V1_18_2;
            case "1.19":
                return V1_19_0;
            case "1.19.1":
                return V1_19_1;
            case "1.19.2":
                return V1_19_2;
            case "1.19.3":
                return V1_19_3;
            case "1.19.4":
                return V1_19_4;
            case "1.20":
                return V1_20_0;
            case "1.20.1":
                return V1_20_1;
            case "1.20.2":
                return V1_20_2;
            case "1.20.3":
            case "1.20.4":
                return V1_20_3_4;
            case "1.20.5":
                return V1_20_5;
            case "1.20.6":
                return V1_20_6;
            case "1.21":
                return V1_21_0;
            case "1.21.1":
                return V1_21_1;
            case "1.21.2":
            case "1.21.3":
                return V1_21_2_3;
            default:
                throw new UnsupportedVersionException("The server version provided is not supported");
        }
    }
}
