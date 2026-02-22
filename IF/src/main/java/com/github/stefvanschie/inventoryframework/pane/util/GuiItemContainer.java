package com.github.stefvanschie.inventoryframework.pane.util;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Arrays;

/**
 * A container for storing a grid of {@link GuiItem}s.
 *
 * @since 0.12.0
 */
public class GuiItemContainer {

    /**
     * The items stored in this grid, stored in column-major order. Slots that are empty are represented as
     * {@literal null}.
     */
    @Nullable
    private final GuiItem @NotNull [] @NotNull [] items;
    /**
     * The length and height of this container.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    private final int length, height;

    /**
     * Creates a new container with the provided length and height. The length and height must both be positive numbers.
     *
     * @param length the length of the container
     * @param height the height of the container
     * @since 0.12.0
     */
    public GuiItemContainer(@Range(from = 0, to = Integer.MAX_VALUE) int length,
                            @Range(from = 0, to = Integer.MAX_VALUE) int height) {
        this.length = length;
        this.height = height;
        this.items = new GuiItem[length][height];
    }

    /**
     * Returns a new container, excluding the range of specified rows. The new container will have its size shrunk so
     * only the included rows are present and any items in the excluded rows are discarded. Note that while this does
     * make a new container, it does not make a copy. For example, the items in the new gui component will be the exact
     * same items as in this one and will not be copied. The specified range is 0-indexed: the first row starts at index
     * 0 and the last row ends at height - 1. The range is inclusive on both ends, the row specified at either parameter
     * will also be excluded. When the range specified is invalid - that is, part of the range contains rows that are
     * not included in this container, an {@link IllegalArgumentException} will be thrown.
     *
     * @param from the starting index of the range
     * @param end the ending index of the range
     * @return the new, shrunk container
     * @since 0.12.0
     */
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public GuiItemContainer excludeRows(@Range(from = 0, to = Integer.MAX_VALUE) int from, int end) {
        if (end >= getHeight()) {
            throw new IllegalArgumentException("Specified range includes non-existent rows");
        }

        GuiItemContainer newGuiContainer = new GuiItemContainer(getLength(), getHeight() - (end - from + 1));

        for (int x = 0; x < getLength(); x++) {
            int newY = 0;

            for (int y = 0; y < getHeight(); y++) {
                GuiItem item = getItem(x, y);

                if (y >= from && y <= end) {
                    continue;
                }

                newGuiContainer.items[x][newY] = item;
                newY++;
            }
        }

        return newGuiContainer;
    }

    /**
     * Creates a deep copy of this container. This means that all internal items will be cloned. The returned container
     * is guaranteed to not reference equals this container.
     *
     * @return the new container
     * @since 0.12.0
     */
    @NotNull
    @Contract(value = "-> new", pure = true)
    public GuiItemContainer copy() {
        GuiItemContainer copy = new GuiItemContainer(getLength(), getHeight());

        for (int x = 0; x < getLength(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                GuiItem item = getItem(x, y);

                if (item == null) {
                    continue;
                }

                copy.items[x][y] = item.copy();
            }
        }

        return copy;
    }

    /**
     * Checks whether this container has at least one item. If it does, true is returned; false otherwise.
     *
     * @return {@literal true} if this has an item, {@literal false} otherwise
     * @since 0.12.0
     */
    @Contract(pure = true)
    public boolean hasItem() {
        for (int x = 0; x < getLength(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (hasItem(x, y)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Adds the specified item in the slot at the specified positions. This will override an already set item if it
     * resides in the same position as specified. If the position specified is outside the boundaries set by this
     * container, an {@link IllegalArgumentException} will be thrown.
     *
     * @param guiItem the item to place in this container
     * @param x the x coordinate of the item
     * @param y the y coordinate of the item
     * @since 0.12.0
     * @throws IllegalArgumentException when the coordinates are out of bounds
     */
    public void setItem(@NotNull GuiItem guiItem, @Range(from = 0, to = Integer.MAX_VALUE) int x,
                        @Range(from = 0, to = Integer.MAX_VALUE) int y) {
        if (!isInBounds(x, y)) {
            throw new IllegalArgumentException("Coordinates must be in-bounds: x = " + x + ", y = " + y +
                    "; should be below " + getLength() + " and " + getHeight());
        }

        GuiItem copy = guiItem.copy();
        copy.applyUUID();

        this.items[x][y] = copy;
    }

    /**
     * Gets the item at the specified coordinates, or null if this cell is empty. If the specified coordinates are not
     * within this container, an {@link IllegalArgumentException} will be thrown.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the item or null
     * @since 0.12.0
     * @throws IllegalArgumentException when the coordinates are out of bounds
     */
    @Nullable
    @Contract(pure = true)
    public GuiItem getItem(@Range(from = 0, to = Integer.MAX_VALUE) int x,
                           @Range(from = 0, to = Integer.MAX_VALUE) int y) {
        if (!isInBounds(x, y)) {
            throw new IllegalArgumentException("Coordinates must be in-bounds: x = " + x + ", y = " + y +
                    "; should be below " + getLength() + " and " + getHeight());
        }

        return this.items[x][y];
    }

    /**
     * Clears the items of this container.
     *
     * @since 0.12.0
     */
    public void clearItems() {
        for (GuiItem[] items : this.items) {
            Arrays.fill(items, null);
        }
    }

    /**
     * Checks whether the item at the specified coordinates exists. If the specified coordinates are not within this
     * container, an {@link IllegalArgumentException} will be thrown.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return {@literal true} if an item exists at the given coordinates, {@literal false} otherwise
     * @since 0.12.0
     * @throws IllegalArgumentException when the coordinates are out of bounds
     */
    @Contract(pure = true)
    public boolean hasItem(@Range(from = 0, to = Integer.MAX_VALUE) int x,
                           @Range(from = 0, to = Integer.MAX_VALUE) int y) {
        return getItem(x, y) != null;
    }

    /**
     * Gets the height of this container.
     *
     * @return the height
     * @since 0.12.0
     */
    @Contract(pure = true)
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getHeight() {
        return this.height;
    }

    /**
     * Gets the length of this container.
     *
     * @return the length
     * @since 0.12.0
     */
    @Contract(pure = true)
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getLength() {
        return this.length;
    }

    /**
     * Puts all items from the container in this container. The items will not be copied, nor will their UUID be applied
     * again. The items will be placed starting at the specified x and y coordinates. Any items that are outside the
     * confines of this container will be ignored. The provided container will not be modified. Any items that were
     * already in this container will be overwritten by the items from the provided container. However, currently
     * existing items will not be overwritten with {@literal null}.
     *
     * @param container the container to obtain items from
     * @param startX the starting x coordinate
     * @param startY the starting y coordinate
     * @since 0.12.0
     */
    public void apply(@NotNull GuiItemContainer container, @Range(from = 0, to = Integer.MAX_VALUE) int startX,
                      @Range(from = 0, to = Integer.MAX_VALUE) int startY) {
        for (int x = 0; x < container.getLength(); x++) {
            for (int y = 0; y < container.getHeight(); y++) {
                if (!isInBounds(x + startX, y + startY)) {
                    continue;
                }

                GuiItem item = container.getItem(x, y);

                if (item == null) {
                    continue;
                }

                this.items[x + startX][y + startY] = item;
            }
        }
    }

    /**
     * Returns whether the specified coordinates are inside the boundary of this container. {@literal true} is returned
     * if they are and {@literal false} otherwise.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return {@literal true} if the coordinates are in bounds, {@literal false} otherwise
     * @since 0.12.0
     */
    @Contract(pure = true)
    private boolean isInBounds(@Range(from = 0, to = Integer.MAX_VALUE) int x,
                               @Range(from = 0, to = Integer.MAX_VALUE) int y) {
        boolean xBounds = isInBounds(0, getLength() - 1, x);
        boolean yBounds = isInBounds(0, getHeight() - 1, y);

        return xBounds && yBounds;
    }

    /**
     * Checks whether a number is within the specified number bound (inclusive on both ends).
     *
     * @param lowerBound the lower bound of the range
     * @param upperBound the upper bound of the range
     * @param value the value to check
     * @return {@literal true} if the value is within the bounds, {@literal false} otherwise
     * @since 0.12.0
     */
    @Contract(pure = true)
    private boolean isInBounds(int lowerBound, int upperBound, int value) {
        return lowerBound <= value && value <= upperBound;
    }
}
