package com.github.stefvanschie.inventoryframework.exception;

import com.github.stefvanschie.inventoryframework.Gui;
import org.jetbrains.annotations.NotNull;

/**
 * An exception indicating that something went wrong while trying to load a {@link Gui} from an XML file.
 *
 * @since 0.3.0
 */
public class XMLLoadException extends RuntimeException {

    /**
     * Constructs the exception with a given message
     *
     * @param message the message to show
     * @since 0.3.0
     */
    public XMLLoadException(@NotNull String message) {
        super(message);
    }

    /**
     * Constructs the exception with a given cause
     *
     * @param cause the cause of this exception
     * @since 0.3.1
     */
    public XMLLoadException(@NotNull Throwable cause) {
        super(cause);
    }

    /**
     * Constructs the exception with a given message and cause
     *
     * @param message the message to show
     * @param cause the cause of this exception
     */
    public XMLLoadException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
