package com.github.stefvanschie.inventoryframework.core.pane;

import com.github.stefvanschie.inventoryframework.core.pane.util.Orientable;

/**
 * This pane holds panes and decides itself where every pane should go. It tries to put every pane in the top left
 * corner and will move rightwards and downwards respectively if the top left corner is already in use. Depending on the
 * order and size of the panes, this may leave empty spaces in certain spots. Do note however that the order of panes
 * isn't always preserved. If there is a gap left in which a pane with a higher index can fit, it will be put there,
 * even if there are panes with a lower index after it. Panes that do not fit will not be displayed.
 *
 * @since 1.0.0
 */
public interface AbstractMasonryPane extends AbstractPane, Orientable {}
