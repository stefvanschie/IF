package com.github.stefvanschie.inventoryframework.pane;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

/**
 * An interface for panes that are rotatable
 *
 * @since 0.3.0
 */
public interface Rotatable {

    /**
     * Sets the rotation of this pane. The rotation is in degrees and can only be in increments of 90. Anything higher
     * than 360, will be lowered to a value in between [0, 360) while maintaining the same rotational value. E.g. 450
     * degrees becomes 90 degrees, 1080 degrees becomes 0, etc.
     *
     * This method fails for any pane that has a length and height which are unequal.
     *
     * @param rotation the rotation of this pane, must be divisible by 90.
     * @throws UnsupportedOperationException when the length and height of the pane are not the same
     * @throws IllegalArgumentException when the rotation isn't a multiple of 90
     * @since 0.3.0
     */
    void setRotation(int rotation);

    /**
     * Gets the rotation specified to this pane. If no rotation has been set, or if this pane is not capable of having a
     * rotation, 0 is returned.
     *
     * @return the rotation for this pane
     * @since 0.3.0
     */
    @Contract(pure = true)
    int getRotation();

    /**
     * Loads all elements regarding a {@link Rotatable} {@link Pane} for the specified pane. The mutable pane contains
     * the changes made.
     *
     * @param rotatable the rotatable pane's elements to be applied
     * @param element the XML element for this pane
     * @since 0.3.0
     */
    static void load(@NotNull Rotatable rotatable, @NotNull Element element) {
        if (element.hasAttribute("rotation")) {
            rotatable.setRotation(Integer.parseInt(element.getAttribute("rotation")));
        }
    }
}
