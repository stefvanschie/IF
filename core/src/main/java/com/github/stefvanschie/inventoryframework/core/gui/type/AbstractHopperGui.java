package com.github.stefvanschie.inventoryframework.core.gui.type;

import com.github.stefvanschie.inventoryframework.core.gui.type.util.AbstractNamedGui;
import com.github.stefvanschie.inventoryframework.core.gui.AbstractInventoryComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a gui in the form of a hopper
 *
 * @since 1.0.0
 */
public interface AbstractHopperGui extends AbstractNamedGui {

    /**
     * Gets the inventory component for the slots
     *
     * @return the slots component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getSlotsComponent();

    /**
     * Gets the inventory component for the player inventory
     *
     * @return the player inventory component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getPlayerInventoryComponent();
}
