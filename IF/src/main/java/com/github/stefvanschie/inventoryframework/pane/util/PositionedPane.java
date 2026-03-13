package com.github.stefvanschie.inventoryframework.pane.util;

import com.github.stefvanschie.inventoryframework.pane.Pane;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * An object for representing a pane with its slot.
 *
 * @since 0.12.0
 */
public class PositionedPane {

    /**
     * The slot of the pane.
     */
    @NotNull
    private final Slot slot;

    /**
     * The pane.
     */
    @NotNull
    private final Pane pane;

    /**
     * Creates a new positioned pane at the provided position.
     *
     * @param slot the slot of the pane
     * @param pane the pane
     * @since 0.12.0
     */
    public PositionedPane(@NotNull Slot slot, @NotNull Pane pane) {
        this.slot = slot;
        this.pane = pane;
    }

    @NotNull
    @Contract(pure = true)
    public Slot getSlot() {
        return this.slot;
    }

    @NotNull
    @Contract(pure = true)
    public Pane getPane() {
        return this.pane;
    }
}
