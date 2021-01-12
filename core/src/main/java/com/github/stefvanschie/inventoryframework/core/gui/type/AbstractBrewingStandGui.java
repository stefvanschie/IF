package com.github.stefvanschie.inventoryframework.core.gui.type;

import com.github.stefvanschie.inventoryframework.core.gui.AbstractInventoryComponent;
import com.github.stefvanschie.inventoryframework.core.gui.type.util.AbstractNamedGui;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a gui in the form of a brewing stand
 *
 * @since 1.0.0
 */
public interface AbstractBrewingStandGui extends AbstractNamedGui {

    /**
     * Gets the inventory component representing the first bottle
     *
     * @return the first bottle component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getFirstBottleComponent();

    /**
     * Gets the inventory component representing the second bottle
     *
     * @return the second bottle component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getSecondBottleComponent();

    /**
     * Gets the inventory component representing the third bottle
     *
     * @return the third bottle component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getThirdBottleComponent();

    /**
     * Gets the inventory component representing the potion ingredient
     *
     * @return the potion ingredient component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getPotionIngredientComponent();

    /**
     * Gets the inventory component representing the blaze powder
     *
     * @return the blaze powder component
     * @since 1.0.0
     */
    @NotNull
    @Contract(pure = true)
    AbstractInventoryComponent getBlazePowderComponent();

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
