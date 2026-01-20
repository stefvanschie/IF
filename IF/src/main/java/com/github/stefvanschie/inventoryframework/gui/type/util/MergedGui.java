package com.github.stefvanschie.inventoryframework.gui.type.util;

import com.github.stefvanschie.inventoryframework.gui.GuiComponent;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.Pane;
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
public interface MergedGui {

    /**
     * Adds a pane to this gui
     *
     * @param pane the pane to add
     * @since 0.8.1
     */
    void addPane(@NotNull Pane pane);

    /**
     * Gets all the panes in this gui. This includes child panes from other panes.
     *
     * @return all panes
     * @since 0.8.1
     */
    @NotNull
    @Contract(pure = true)
    List<Pane> getPanes();

    /**
     * Gets all the items in all underlying panes
     *
     * @return all items
     * @since 0.8.1
     */
    @NotNull
    @Contract(pure = true)
    Collection<GuiItem> getItems();

    /**
     * Gets the gui component for this gui
     *
     * @return the gui component
     * @since 0.8.1
     */
    @NotNull
    @Contract(pure = true)
    GuiComponent getGuiComponent();
}
