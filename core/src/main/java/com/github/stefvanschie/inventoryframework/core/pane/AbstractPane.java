package com.github.stefvanschie.inventoryframework.core.pane;

import com.github.stefvanschie.inventoryframework.core.gui.AbstractGuiItem;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The base class for all panes.
 */
public interface AbstractPane {

    /**
	 * Makes a copy of this pane and returns it. This makes a deep copy of the pane. This entails that the underlying
	 * panes and/or items will be copied as well. The returned pane will never be reference equal to the current pane.
	 *
	 * @return a copy of this pane
	 * @since 0.6.2
	 */
	@NotNull
	@Contract(pure = true)
    AbstractPane copy();

    /**
     * Set the length of this pane
     *
     * @param length the new length
     */
    void setLength(int length);

    /**
     * Set the height of this pane
     *
     * @param height the new height
     */
    void setHeight(int height);

    /**
     * Set the x coordinate of this pane
     *
     * @param x the new x coordinate
     */
    void setX(int x);

    /**
     * Set the y coordinate of this pane
     *
     * @param y the new y coordinate
     */
    void setY(int y);

    /**
     * Returns the length of this pane
     *
     * @return the length
     */
    @Contract(pure = true)
    int getLength();

    /**
     * Returns the height of this pane
     *
     * @return the height
     */
    @Contract(pure = true)
    int getHeight();

    /**
     * Gets the {@link UUID} associated with this pane.
     *
     * @return the uuid
     * @since 0.7.1
     */
    @NotNull
    @Contract(pure = true)
    UUID getUUID();

    /**
     * Gets the x coordinate of this pane
     *
     * @return the x coordinate
     */
    @Contract(pure = true)
    int getX();

    /**
     * Gets the y coordinate of this pane
     *
     * @return the y coordinate
     */
    @Contract(pure = true)
    int getY();

    /**
     * Returns the pane's visibility state
     *
     * @return the pane's visibility
     */
    @Contract(pure = true)
    boolean isVisible();

    /**
     * Sets whether this pane is visible or not
     *
     * @param visible the pane's visibility
     */
    void setVisible(boolean visible);

    /**
     * Sets the priority of this pane
     *
     * @param priority the priority
     */
    void setPriority(@NotNull Priority priority);

    /**
     * Returns the priority of the pane
     *
     * @return the priority
     */
    @NotNull
    Priority getPriority();

    /**
     * Gets all the items in this pane and all underlying panes.
     * The returned collection is not guaranteed to be mutable or to be a view of the underlying data.
     * (So changes to the gui are not guaranteed to be visible in the returned value.)
     *
     * @return all items
     */
    @NotNull
    @Contract(pure = true)
    Collection<? extends AbstractGuiItem> getItems();

    /**
     * Gets all the panes in this panes, including any child panes from other panes.
     * The returned collection is not guaranteed to be mutable or to be a view of the underlying data.
     * (So changes to the gui are not guaranteed to be visible in the returned value.)
     *
     * @return all panes
     */
    @NotNull
    @Contract(pure = true)
    Collection<? extends AbstractPane> getPanes();

    /**
     * Clears the entire pane of any items/panes. Underlying panes will not be cleared.
     *
     * @since 0.3.2
     */
    void clear();

    /**
     * An enum representing the rendering priorities for the panes.
     */
    enum Priority {

        /**
         * The lowest priority, will be rendered first
         */
        LOWEST {
            @Override
            public boolean isLessThan(@NotNull Priority priority) {
                return priority != this;
            }
        },

        /**
         * A low priority, lower than default
         */
        LOW {
            @Override
            public boolean isLessThan(@NotNull Priority priority) {
                return priority != this && priority != LOWEST;
            }
        },

        /**
         * A normal priority, the default
         */
        NORMAL {
            @Override
            public boolean isLessThan(@NotNull Priority priority) {
                return priority != this && priority != LOW && priority != LOWEST;
            }
        },

        /**
         * A higher priority, higher than default
         */
        HIGH {
            @Override
            public boolean isLessThan(@NotNull Priority priority) {
                return priority == HIGHEST || priority == MONITOR;
            }
        },

        /**
         * The highest priority for production use
         */
        HIGHEST {
            @Override
            public boolean isLessThan(@NotNull Priority priority) {
                return priority == MONITOR;
            }
        },

        /**
         * The highest priority, will always be called last, should not be used for production code
         */
        MONITOR {
            @Override
            public boolean isLessThan(@NotNull Priority priority) {
                return false;
            }
        };

        /**
         * Whether this priority is less than the priority specified.
         *
         * @param priority the priority to compare against
         * @return true if this priority is less than the specified priority, false otherwise
         * @since 0.8.0
         */
        @Contract(pure = true)
        public abstract boolean isLessThan(@NotNull Priority priority);

        /**
         * Whether this priority is greater than the priority specified.
         *
         * @param priority the priority to compare against
         * @return true if this priority is greater than the specified priority, false otherwise
         * @since 0.8.0
         */
        @Contract(pure = true)
        public boolean isGreaterThan(@NotNull Priority priority) {
            return !isLessThan(priority) && this != priority;
        }
    }
}
