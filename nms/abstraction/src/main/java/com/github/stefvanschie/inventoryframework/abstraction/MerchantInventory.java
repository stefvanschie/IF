package com.github.stefvanschie.inventoryframework.abstraction;

import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * A merchant inventory
 *
 * @since 0.10.1
 */
public abstract class MerchantInventory {

    /**
     * Creates a merchant inventory.
     *
     * @param title the title of the inventory
     * @return the inventory
     * @since 0.11.0
     */
    @NotNull
    public abstract Inventory createInventory(@NotNull TextHolder title);

    /**
     * Sends the merchant offers to the player, combined with the merchants level and experience.
     *
     * @param player the player to send this to
     * @param trades the trades to send
     * @param level the level of the merchant
     * @param experience the experience of the merchant
     * @since 0.10.1
     */
    public abstract void sendMerchantOffers(
            @NotNull Player player,
            @NotNull List<? extends Map.Entry<? extends MerchantRecipe, ? extends Integer>> trades,
            int level,
            int experience
    );
}
