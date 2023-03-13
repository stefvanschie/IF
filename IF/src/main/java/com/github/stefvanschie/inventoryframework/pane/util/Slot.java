package com.github.stefvanschie.inventoryframework.pane.util;

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.Objects;

/**
 * A slot represents a position in some type of container. Implementors of this class represent slots in different ways.
 *
 * @since 0.10.8
 */
public interface Slot {

    /**
     * Gets the x coordinate of this slot.
     *
     * @param length the length of the parent container
     * @return the x coordinate of this slot
     * @since 0.10.8
     */
    @Contract(pure = true)
    int getX(int length);

    /**
     * Gets the y coordinate of this slot.
     *
     * @param length the length of the parent container
     * @return the y coordinate of this slot
     * @since 0.10.8
     */
    @Contract(pure = true)
    int getY(int length);

    /**
     * Deserializes the slot from an element. The slot may either be provided as an (x, y) coordinate pair via the "x"
     * and "y" attributes; or as an index via the "index" attribute. If both forms are present, an
     * {@link XMLLoadException} will be thrown. If only the "x" or the "y" attribute is present, but not both, an
     * {@link XMLLoadException} will be thrown. If none of the aforementioned attributes appear, an
     * {@link XMLLoadException} will be thrown. If any of these attributes contain a value that is not an integer, an
     * {@link XMLLoadException} will be thrown. Otherwise, this will return a slot based on the present attributes.
     *
     * @param element the element from which to retrieve the attributes for the slot
     * @return the deserialized slot
     * @throws XMLLoadException if "x", "y", and "index" attributes are present; if only an "x" attribute is present; if
     *                          only a "y" attribute is present; if no "x", "y", or "index" attribute is present; or if
     *                          the "x", "y", or "index" attribute contain a value that is not an integer.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    static Slot deserialize(@NotNull Element element) {
        boolean hasX = element.hasAttribute("x");
        boolean hasY = element.hasAttribute("y");
        boolean hasIndex = element.hasAttribute("index");

        if (hasX && hasY && !hasIndex) {
            int x, y;

            try {
                x = Integer.parseInt(element.getAttribute("x"));
                y = Integer.parseInt(element.getAttribute("y"));
            } catch (NumberFormatException exception) {
                throw new XMLLoadException("The x or y attribute does not have an integer as value");
            }

            return Slot.fromXY(x, y);
        }

        if (hasIndex && !hasX && !hasY) {
            int index;

            try {
                index = Integer.parseInt(element.getAttribute("index"));
            } catch (NumberFormatException exception) {
                throw new XMLLoadException("The index attribute does not have an integer as value");
            }

            return Slot.fromIndex(index);
        }

        throw new XMLLoadException("The combination of x, y and index attributes is invalid");
    }

    /**
     * Creates a new slot based on an (x, y) coordinate pair.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the slot representing this position
     * @since 0.10.8
     */
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    static Slot fromXY(int x, int y) {
        return new XY(x, y);
    }

    /**
     * Creates a new slot based on an index. This index is relative to the parent container this slot will be used in.
     *
     * @param index the index
     * @return the slot representing this relative position
     * @since 0.10.8
     */
    @NotNull
    @Contract("_ -> new")
    static Slot fromIndex(int index) {
        return new Indexed(index);
    }

    /**
     * A class representing a slot based on an (x, y) coordinate pair.
     *
     * @since 0.10.8
     */
    class XY implements Slot {

        /**
         * The (x, y) coordinate pair
         */
        private final int x, y;

        /**
         * Creates a new slot based on an (x, y) coordinate pair.
         *
         * @param x the x coordinate
         * @param y the y coordinate
         * @since 0.10.8
         */
        private XY(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int getX(int length) {
            return this.x;
        }

        @Override
        public int getY(int length) {
            return this.y;
        }

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) {
                return true;
            }

            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            XY xy = (XY) object;

            return x == xy.x && y == xy.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    /**
     * A class representing a slot based on an index.
     *
     * @since 0.10.8
     */
    class Indexed implements Slot {

        /**
         * The index of this slot.
         */
        private final int index;

        /**
         * Creates a new slot based on an index.
         *
         * @param index the index of this slot
         * @since 0.10.8
         */
        private Indexed(int index) {
            this.index = index;
        }

        /**
         * {@inheritDoc}
         *
         * If {@code length} is zero, this will throw an {@link IllegalArgumentException}.
         *
         * @param length {@inheritDoc}
         * @return {@inheritDoc}
         * @throws IllegalArgumentException when {@code length} is zero
         */
        @Override
        @Contract(pure = true)
        public int getX(int length) {
            if (length == 0) {
                throw new IllegalArgumentException("Length may not be zero");
            }

            return this.index % length;
        }

        /**
         * {@inheritDoc}
         *
         * If {@code length} is zero, this will throw an {@link IllegalArgumentException}.
         *
         * @param length {@inheritDoc}
         * @return {@inheritDoc}
         * @throws IllegalArgumentException when {@code length} is zero
         */
        @Override
        @Contract(pure = true)
        public int getY(int length) {
            if (length == 0) {
                throw new IllegalArgumentException("Length may not be zero");
            }

            return this.index / length;
        }

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) {
                return true;
            }

            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            Indexed indexed = (Indexed) object;

            return index == indexed.index;
        }

        @Override
        public int hashCode() {
            return index;
        }
    }
}
