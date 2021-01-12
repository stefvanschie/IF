package com.github.stefvanschie.inventoryframework.core.gui.type;

import com.github.stefvanschie.inventoryframework.core.gui.type.util.AbstractNamedGui;
import com.github.stefvanschie.inventoryframework.core.gui.AbstractInventoryComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a gui in the form of a furnace
 *
 * @since 1.0.0
 */
public interface AbstractFurnaceGui extends AbstractNamedGui {

    /**
     * Gets the inventory component representing the ingredient
     *
     * @return the ingredient component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getIngredientComponent();

    /**
     * Gets the inventory component representing the fuel
     *
     * @return the fuel component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getFuelComponent();

    /**
     * Gets the inventory component representing the output
     *
     * @return the output component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getOutputComponent();

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
