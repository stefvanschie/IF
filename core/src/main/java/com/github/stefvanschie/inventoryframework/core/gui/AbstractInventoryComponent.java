package com.github.stefvanschie.inventoryframework.core.gui;

import com.github.stefvanschie.inventoryframework.core.pane.AbstractPane;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a component within an inventory that can hold items. This is always in the shape of a rectangular grid.
 *
 * @since 0.8.0
 */
public abstract class AbstractInventoryComponent {

    /**
     * The length and height of this inventory component
     */
    private final int length, height;

    /**
     * Creates a new inventory component with the specified length and width. If either the length or the width is less
     * than zero, an {@link IllegalArgumentException} will be thrown.
     *
     * @param length the length of the component
     * @param height the height of the component
     * @since 0.8.0
     */
    public AbstractInventoryComponent(int length, int height) {
        if (length < 0 || height < 0) {
            throw new IllegalArgumentException("Sizes must be greater or equal to zero");
        }

        this.length = length;
        this.height = height;
    }

    /**
     * This will make each pane in this component render their items in this inventory component. The panes are
     * displayed according to their priority, with the lowest priority rendering first and the highest priority (note:
     * highest priority, not {@link AbstractPane.Priority#HIGHEST} priority) rendering last.
     *
     * @since 0.8.0
     */
    public abstract void display();

    /**
     * Gets a list of panes this inventory component contains. The returned list is modifiable. If this inventory
     * component currently does not have any panes, an empty list is returned. This list is guaranteed to be sorted
     * according to the panes' priorities.
     *
     * @return the panes this component has
     * @since 1.0.0
     */
    public abstract List<? extends AbstractPane> getPanes();

    /**
     * Gets the total size of this inventory component.
     *
     * @return the size
     * @since 0.8.0
     */
    @Contract(pure = true)
    public int getSize() {
        return getLength() * getHeight();
    }

    /**
     * Gets the height of this inventory component.
     *
     * @return the height
     * @since 0.8.0
     */
    @Contract(pure = true)
    public int getHeight() {
        return this.height;
    }

    /**
     * Gets the length of this inventory component.
     *
     * @return the length
     * @since 0.8.0
     */
    @Contract(pure = true)
    public int getLength() {
        return this.length;
    }

    /**
     * Returns whether the specified coordinates are inside the boundary of this inventory component or outside of this
     * inventory component; true is returned for the former case and false for the latter case.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if the coordinates are in bounds, false otherwise
     * @since 0.8.0
     */
    @Contract(pure = true)
    protected boolean isInBounds(int x, int y) {
        boolean xBounds = isInBounds(0, getLength() - 1, x);
        boolean yBounds = isInBounds(0, getHeight() - 1, y);

        return xBounds && yBounds;
    }

    /**
     * Gets the pane at the specified index.
     *
     * @param index the index of the pane
     * @return the pane
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    protected AbstractPane getPane(int index) {
        if (!isInBounds(0, getPanes().size() - 1, index)) {
            throw new IllegalArgumentException("Index not in pane list");
        }

        return getPanes().get(index);
    }

    /**
     * Checks whether a number is within the specified number bound (inclusive on both ends).
     *
     * @param lowerBound the lower bound of the range
     * @param upperBound the upper bound of the range
     * @param value the value to check
     * @return true if the value is within the bounds, false otherwise
     * @since 0.8.0
     */
    @Contract(pure = true)
    private boolean isInBounds(int lowerBound, int upperBound, int value) {
        return lowerBound <= value && value <= upperBound;
    }
}
