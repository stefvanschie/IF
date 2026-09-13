package com.github.stefvanschie.inventoryframework.annotation.exception;

/**
 * An exception thrown when a class definition contains multiple annotations where only one is allowed.
 *
 * @since 0.12.1
 */
public class MultipleAnnotationsException extends RuntimeException {

    /**
     * Constructs a new multiple annotations exception with the specified detail message.
     *
     * @param message the detail message
     * @since 0.12.1
     */
    public MultipleAnnotationsException(String message) {
        super(message);
    }
}
