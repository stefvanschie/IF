package com.github.stefvanschie.inventoryframework.core.pane;

import com.github.stefvanschie.inventoryframework.core.pane.util.Flippable;
import com.github.stefvanschie.inventoryframework.core.pane.util.Rotatable;

/**
 * A pane for static items and stuff. All items will have to be specified a slot, or will be added in the next position.
 *
 * @since 1.0.0
 */
public interface AbstractStaticPane extends AbstractPane, Flippable, Rotatable {}
