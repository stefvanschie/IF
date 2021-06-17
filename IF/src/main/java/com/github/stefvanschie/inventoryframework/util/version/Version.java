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
     * Version 1.14 R1
     *
     * @since 0.8.0
     */
    V1_14_R1,

    /**
     * Version 1.15 R1
     *
     * @since 0.8.0
     */
    V1_15_R1,

    /**
     * Version 1.16 R1
     *
     * @since 0.8.0
     */
    V1_16_R1,

    /**
     * Version 1.16 R2
     *
     * @since 0.8.0
     */
    V1_16_R2,

    /**
     * Version 1.16 R3
     *
     * @since 0.8.0
     */
    V1_16_R3,

    /**
     * Version 1.17 R1
     *
     * @since 0.9.9
     */
    V1_17_R1;

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
        String version = Bukkit.getServer().getClass().getPackage().getName();

        switch (version.substring(version.lastIndexOf('.') + 1)) {
            case "v1_14_R1":
                return V1_14_R1;
            case "v1_15_R1":
                return V1_15_R1;
            case "v1_16_R1":
                return V1_16_R1;
            case "v1_16_R2":
                return V1_16_R2;
            case "v1_16_R3":
                return V1_16_R3;
            case "v1_17_R1":
                return V1_17_R1;
            default:
                throw new UnsupportedVersionException("The server version provided is not supported");
        }
    }
}
