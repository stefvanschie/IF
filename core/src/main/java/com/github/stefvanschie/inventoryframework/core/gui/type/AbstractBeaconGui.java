package com.github.stefvanschie.inventoryframework.core.gui.type;

import com.github.stefvanschie.inventoryframework.core.gui.AbstractInventoryComponent;
import com.github.stefvanschie.inventoryframework.core.gui.type.util.AbstractGui;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a gui in the form of a beacon
 *
 * @since 1.0.0
 */
public interface AbstractBeaconGui extends AbstractGui {

    /**
     * Gets the inventory component representing the payment item
     *
     * @return the payment item component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getPaymentItemComponent();

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
