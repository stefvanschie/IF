package com.github.stefvanschie.inventoryframework.abstraction.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * A value whose modifications can be observed.
 *
 * @param <T> the type of value to store
 * @since 0.10.10
 */
public class ObservableValue<T> {

    /**
     * A collection of subscribers that should be notified on updates.
     */
    @NotNull
    private final Collection<Consumer<? super T>> subscribers = new HashSet<>();

    /**
     * The current value
     */
    @Nullable
    private T value;

    /**
     * Creates a new observable value with the given default value.
     *
     * @param defaultValue the default value
     * @since 0.10.10
     */
    public ObservableValue(@Nullable T defaultValue) {
        this.value = defaultValue;
    }

    /**
     * Updates the old value to the given new value. This will notify all the subscribers before updating the new value.
     * Subscribers may observe the old value by using {@link #get()}. This will always notify the subscribers, even if
     * the new value is the same as the old value.
     *
     * @param newValue the new value
     * @since 0.10.10
     */
    public void set(T newValue) {
        for (Consumer<? super T> subscriber : this.subscribers) {
            subscriber.accept(newValue);
        }

        this.value = newValue;
    }

    /**
     * Subscribes to modifications of this value. The provided consumer will be called every time this value changes.
     *
     * @param consumer the consumer to call upon updates of this value
     * @since 0.10.10
     */
    public void subscribe(@NotNull Consumer<? super T> consumer) {
        this.subscribers.add(consumer);
    }

    /**
     * Gets the current value of this item. If this is called from within a subscriber, then this is the value from
     * before the current in-progress update.
     *
     * @return the current value
     * @since 0.10.10
     */
    @Nullable
    @Contract(pure = true)
    public T get() {
        return this.value;
    }

}
