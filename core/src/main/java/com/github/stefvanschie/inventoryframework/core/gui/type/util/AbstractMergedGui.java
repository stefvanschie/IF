package com.github.stefvanschie.inventoryframework.core.gui.type.util;

import com.github.stefvanschie.inventoryframework.core.gui.AbstractGuiItem;
import com.github.stefvanschie.inventoryframework.core.gui.AbstractInventoryComponent;
import com.github.stefvanschie.inventoryframework.core.pane.AbstractPane;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Represents a chest-like gui in which the top and bottom inventories are merged together and only exist of one
 * inventory component.
 *
 * @since 0.8.1
 */
public interface AbstractMergedGui {

    /**
     * Gets all the panes in this gui. This includes child panes from other panes.
     *
     * @return all panes
     * @since 0.8.1
     */
    @NotNull
    @Contract(pure = true)
    List<? extends AbstractPane> getPanes();

    /**
     * Gets all the items in all underlying panes
     *
     * @return all items
     * @since 0.8.1
     */
    @NotNull
    @Contract(pure = true)
    Collection<? extends AbstractGuiItem> getItems();

    /**
     * Gets the inventory component for this gui
     *
     * @return the inventory component
     * @since 0.8.1
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getInventoryComponent();
}
