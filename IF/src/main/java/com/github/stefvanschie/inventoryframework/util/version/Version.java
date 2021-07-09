package com.github.stefvanschie.inventoryframework.util.version;

import com.github.stefvanschie.inventoryframework.exception.UnsupportedVersionException;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
    V1_17_1;

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
            default:
                throw new UnsupportedVersionException("The server version provided is not supported");
        }
    }
}
