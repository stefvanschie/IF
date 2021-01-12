package com.github.stefvanschie.inventoryframework.core.gui.type.util;

import com.github.stefvanschie.inventoryframework.core.pane.AbstractPane;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * The base class of all GUIs
 */
public interface AbstractGui {

    /**
     * Makes a copy of this gui and returns it. This makes a deep copy of the gui. This entails that the underlying
     * panes will be copied as per their {@link AbstractPane#copy} and miscellaneous data will be copied. The copy of this gui,
     * will however have no viewers even if this gui currently has viewers. With this, cache data for viewers will also
     * be non-existent for the copied gui. The returned gui will never be reference equal to the current gui.
     *
     * @return a copy of the gui
     * @since 0.6.2
     */
    @NotNull
    @Contract(pure = true)
    AbstractGui copy();

    /**
     * Gets whether the player inventory is currently in use. This means whether the player inventory currently has an
     * item in it.
     *
     * @return true if the player inventory is occupied, false otherwise
     * @since 0.8.0
     */
    boolean isPlayerInventoryUsed();

    /**
     * Gets whether this gui is being updated. This returns true if this is the case and false otherwise.
     *
     * @return whether this gui is being updated
     * @since 0.5.15
     */
    @Contract(pure = true)
    boolean isUpdating();
}
