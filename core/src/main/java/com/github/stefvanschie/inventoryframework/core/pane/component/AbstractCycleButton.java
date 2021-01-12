package com.github.stefvanschie.inventoryframework.core.pane.component;

import com.github.stefvanschie.inventoryframework.core.pane.AbstractPane;

/**
 * A button for cycling between different options
 *
 * @since 1.0.0
 */
public interface AbstractCycleButton extends AbstractPane {

    /**
     * Cycles through one option, making it go to the next one
     *
     * @since 1.0.0
     */
    void cycle();
}
