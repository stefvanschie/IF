package com.github.stefvanschie.inventoryframework.core.gui.type;

import com.github.stefvanschie.inventoryframework.core.gui.type.util.AbstractNamedGui;
import com.github.stefvanschie.inventoryframework.core.gui.AbstractInventoryComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a gui in the form of a grindstone
 *
 * @since 1.0.0
 */
public interface AbstractGrindstoneGui extends AbstractNamedGui {

    /**
     * Gets the inventory component representing the items
     *
     * @return the items component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getItemsComponent();

    /**
     * Gets the inventory component representing the result
     *
     * @return the result component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getResultComponent();

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
