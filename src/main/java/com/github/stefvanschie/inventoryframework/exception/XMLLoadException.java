package com.github.stefvanschie.inventoryframework.exception;

import com.github.stefvanschie.inventoryframework.Gui;

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
    public XMLLoadException(String message) {
        super(message);
    }
}
