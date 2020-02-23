package com.github.stefvanschie.inventoryframework.exception;

import org.jetbrains.annotations.NotNull;

/**
 * An exception indicating that something went wrong while executing reflection that was loaded prior from an XML file.
 *
 * Keep in mind that, while this exception is thrown because of reflection, reflection that is executed while loading
 * from the XML file will throw an {@link XMLLoadException} if something goes wrong. This exception will only occur when
 * the reflection error happens after loading has finished.
 *
 * @since 0.3.1
 */
public class XMLReflectionException extends RuntimeException {

    /**
     * Constructs the exception with a given message
     *
     * @param message the message to show
     * @since 0.3.1
     */
    public XMLReflectionException(@NotNull String message) {
        super(message);
    }

    /**
     * Constructs the exception with a given cause
     *
     * @param cause the cause of this exception
     * @since 0.3.1
     */
    public XMLReflectionException(@NotNull Throwable cause) {
        super(cause);
    }
}
