package com.github.stefvanschie.inventoryframework.annotation.exception;

import org.jetbrains.annotations.Nullable;

/**
 * An exception thrown when a method's parameters do not match the required format.
 *
 * @since 0.12.1
 */
public class InvalidParametersException extends RuntimeException {

    /**
     * Constructs a new invalid parameters exception with the specified detail message.
     *
     * @param message the detail message
     * @since 0.12.1
     */
    public InvalidParametersException(@Nullable String message) {
        super(message);
    }
}
