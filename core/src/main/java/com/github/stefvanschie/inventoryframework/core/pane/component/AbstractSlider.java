package com.github.stefvanschie.inventoryframework.core.pane.component;

import com.github.stefvanschie.inventoryframework.core.pane.component.util.AbstractVariableBar;

/**
 * A slider for a graphical interface into what amount of a whole is set.
 *
 * @since 1.0.0
 */
public interface AbstractSlider extends AbstractVariableBar {

    /**
     * Sets the value of this bar. The value has to be in (0,1). If not, this method will throw an
     * {@link IllegalArgumentException}.
     *
     * @param value the new value.
     * @throws IllegalArgumentException when the value is out of range
     * @since 1.0.0
     */
    void setValue(float value);

    /**
     * Gets the value as a float in between (0,1) this bar is currently set at.
     *
     * @return the value
     * @since 1.0.0
     */
    float getValue();
}
