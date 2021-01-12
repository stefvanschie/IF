package com.github.stefvanschie.inventoryframework.bukkit.gui.type.util;

import com.github.stefvanschie.inventoryframework.core.gui.type.util.AbstractMergedGui;
import com.github.stefvanschie.inventoryframework.bukkit.pane.Pane;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a chest-like gui in which the top and bottom inventories are merged together and only exist of one
 * inventory component.
 *
 * @since 0.8.1
 */
public interface MergedGui extends AbstractMergedGui {

    /**
     * Adds a pane to this gui
     *
     * @param pane the pane to add
     * @since 0.8.1
     */
    void addPane(@NotNull Pane pane);
}
