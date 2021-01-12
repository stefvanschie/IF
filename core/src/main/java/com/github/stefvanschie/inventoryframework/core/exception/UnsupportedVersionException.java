package com.github.stefvanschie.inventoryframework.core.exception;

import org.jetbrains.annotations.NotNull;

/**
 * An exception indicating that the provided version is not supported.
 *
 * @since 0.8.0
 */
public class UnsupportedVersionException extends RuntimeException {

    /**
     * Constructs the exception with a given message
     *
     * @param message the message to show
     * @since 0.8.0
     */
    public UnsupportedVersionException(@NotNull String message) {
        super(message);
    }
}
