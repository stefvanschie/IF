package com.github.stefvanschie.inventoryframework.pane;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import java.util.Locale;

/**
 * An interface for panes that can have different orientations
 *
 * @since 0.3.0
 */
public interface Orientable {

    /**
     * Gets the orientation of this outline pane
     *
     * @return the orientation
     * @since 0.3.0
     */
    @NotNull
    @Contract(pure = true)
    Orientation getOrientation();

    /**
     * Sets the orientation of this outline pane
     *
     * @param orientation the new orientation
     * @since 0.3.0
     */
    void setOrientation(@NotNull Orientation orientation);

    /**
     * Loads all elements regarding a {@link Orientable} {@link Pane} for the specified pane. The mutable pane contains
     * the changes made.
     *
     * @param orientable the orientable pane's elements to be applied
     * @param element the XML element for this pane
     * @since 0.3.0
     */
    static void load(@NotNull Orientable orientable, @NotNull Element element) {
        if (element.hasAttribute("orientation")) {
            orientable.setOrientation(Orientation.valueOf(element.getAttribute("orientation")
                .toUpperCase(Locale.getDefault())));
        }
    }

    /**
     * An orientation for outline panes
     *
     * @since 0.3.0
     */
    enum Orientation {

        /**
         * A horizontal orientation, will outline every item from the top-left corner going to the right and down
         *
         * @since 0.3.0
         */
        HORIZONTAL,

        /**
         * A vertical orientation, will outline every item from the top-left corner going down and to the right
         *
         * @since 0.3.0
         */
        VERTICAL
    }
}
