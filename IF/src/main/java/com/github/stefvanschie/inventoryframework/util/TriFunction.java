package com.github.stefvanschie.inventoryframework.util;

/**
 * A function that takes three arguments and returns a result.
 *
 * @param <A> the type of the first argument
 * @param <B> the type of the second argument
 * @param <C> the type of the third argument
 * @param <R> the type of the result
 * @since 0.10.8
 */
@FunctionalInterface
public interface TriFunction<A, B, C, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param a the first argument
     * @param b the second argument
     * @param c the third argument
     * @return the result value
     * @since 0.10.8
     */
    R apply(A a, B b, C c);
}
