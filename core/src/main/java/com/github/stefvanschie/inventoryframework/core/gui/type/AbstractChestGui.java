package com.github.stefvanschie.inventoryframework.core.gui.type;

import com.github.stefvanschie.inventoryframework.core.gui.type.util.AbstractMergedGui;
import com.github.stefvanschie.inventoryframework.core.gui.type.util.AbstractNamedGui;
import org.jetbrains.annotations.Contract;

/**
 * Represents a gui in the form of a chest. Unlike traditional chests, this may take on any amount of rows between 1 and
 * 6.
 *
 * @since 1.0.0
 */
public interface AbstractChestGui extends AbstractMergedGui, AbstractNamedGui {

    /**
     * Sets the amount of rows for this inventory.
     * This will (unlike most other methods) directly update itself in order to ensure all viewers will still be viewing
     * the new inventory as well.
     *
     * @param rows the amount of rows in range 1..6.
     * @since 1.0.0
     */
    void setRows(int rows);

    /**
     * Returns the amount of rows this gui currently has
     *
     * @return the amount of rows
     * @since 1.0.0
     */
    @Contract(pure = true)
    int getRows();
}
