package com.github.stefvanschie.inventoryframework.core.pane.component;

import com.github.stefvanschie.inventoryframework.core.pane.component.util.AbstractVariableBar;

/**
 * A percentage bar for a graphical interface into what amount of a whole is set.
 *
 * @since 1.0.0
 */
public interface AbstractPercentageBar extends AbstractVariableBar {

    /**
     * Sets the percentage of this bar. The percentage has to be in (0,1). If not, this method will throw an
     * {@link IllegalArgumentException}.
     *
     * @param percentage the new percentage.
     * @throws IllegalArgumentException when the percentage is out of range
     * @since 1.0.0
     */
    void setPercentage(float percentage);

    /**
     * Gets the percentage as a float in between (0,1) this bar is currently set at.
     *
     * @return the percentage
     * @since 1.0.0
     */
    float getPercentage();
}
