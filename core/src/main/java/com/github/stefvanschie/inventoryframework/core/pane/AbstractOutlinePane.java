package com.github.stefvanschie.inventoryframework.core.pane;

import com.github.stefvanschie.inventoryframework.core.pane.util.Flippable;
import com.github.stefvanschie.inventoryframework.core.pane.util.Mask;
import com.github.stefvanschie.inventoryframework.core.pane.util.Orientable;
import com.github.stefvanschie.inventoryframework.core.pane.util.Rotatable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A pane for items that should be outlined
 *
 * @since 1.0.0
 */
public interface AbstractOutlinePane extends AbstractPane, Flippable, Orientable, Rotatable {

    /**
     * Applies a custom mask to this pane. This will throw an {@link IllegalArgumentException} when the mask's dimension
     * differs from this pane's dimension.
     *
     * @param mask the mask to apply to this pane
     * @throws IllegalArgumentException when the mask's dimension is incorrect
     * @since 1.0.0
     */
    void applyMask(@NotNull Mask mask);

    /**
     * Gets the mask applied to this pane.
     *
     * @return the mask
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    Mask getMask();

    /**
     * Sets the gap of the pane
     *
     * @param gap the new gap
     * @since 1.0.0
     */
    void setGap(int gap);

    /**
     * Gets the gap of the pane
     *
     * @return the gap
     * @since 1.0.0
     */
    @Contract(pure = true)
    int getGap();

    /**
     * Sets whether this pane should repeat itself
     *
     * @param repeat whether the pane should repeat
     * @since 1.0.0
     */
    void setRepeat(boolean repeat);

    /**
     * Gets whether this outline pane repeats itself
     *
     * @return true if this pane repeats, false otherwise
     * @since 1.0.0
     */
    @Contract(pure = true)
    boolean doesRepeat();
}
