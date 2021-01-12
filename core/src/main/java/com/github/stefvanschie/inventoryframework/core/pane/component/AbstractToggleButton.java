package com.github.stefvanschie.inventoryframework.core.pane.component;

import com.github.stefvanschie.inventoryframework.core.pane.AbstractPane;

/**
 * A button that toggles between an enabled and disabled state.
 *
 * @since 1.0.0
 */
public interface AbstractToggleButton extends AbstractPane {

    /**
     * Toggles between the enabled and disabled states
     *
     * @since 1.0.0
     */
    void toggle();
}
