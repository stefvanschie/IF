package com.github.stefvanschie.inventoryframework.core.gui.type;

import com.github.stefvanschie.inventoryframework.core.gui.AbstractInventoryComponent;
import com.github.stefvanschie.inventoryframework.core.gui.type.util.AbstractNamedGui;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a gui in the form of an enchanting table
 *
 * @since 1.0.0
 */
public interface AbstractEnchantingTableGui extends AbstractNamedGui {

    /**
     * Gets the inventory component representing the input
     *
     * @return the input component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getInputComponent();

    /**
     * Gets the inventory component representing the player inventory
     *
     * @return the player inventory component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getPlayerInventoryComponent();
}
