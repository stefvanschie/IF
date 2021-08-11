package com.github.stefvanschie.inventoryframework.nms.v1_15;

import com.github.stefvanschie.inventoryframework.abstraction.MerchantInventory;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.MerchantRecipeList;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Internal merchant inventory for 1.15
 *
 * @since 0.10.1
 */
public class MerchantInventoryImpl extends MerchantInventory {

    @Override
    public void sendMerchantOffers(@NotNull Player player, @NotNull List<? extends MerchantRecipe> trades,
                                   int level, int experience) {
        MerchantRecipeList offers = new MerchantRecipeList();

        for (MerchantRecipe recipe : trades) {
            List<ItemStack> ingredients = recipe.getIngredients();

            if (ingredients.size() < 1) {
                throw new IllegalStateException("Merchant recipe has no ingredients");
            }

            ItemStack itemA = ingredients.get(0);
            ItemStack itemB = null;

            if (ingredients.size() >= 2) {
                itemB = ingredients.get(1);
            }

            net.minecraft.server.v1_15_R1.ItemStack nmsItemA = CraftItemStack.asNMSCopy(itemA);
            net.minecraft.server.v1_15_R1.ItemStack nmsItemB = net.minecraft.server.v1_15_R1.ItemStack.a;
            net.minecraft.server.v1_15_R1.ItemStack nmsItemResult = CraftItemStack.asNMSCopy(recipe.getResult());

            if (itemB != null) {
                nmsItemB = CraftItemStack.asNMSCopy(itemB);
            }

            int uses = recipe.getUses();
            int maxUses = recipe.getMaxUses();
            int exp = recipe.getVillagerExperience();
            float multiplier = recipe.getPriceMultiplier();

            net.minecraft.server.v1_15_R1.MerchantRecipe merchantOffer = new net.minecraft.server.v1_15_R1.MerchantRecipe(
                    nmsItemA, nmsItemB, nmsItemResult, uses, maxUses, exp, multiplier
            );

            offers.add(merchantOffer);
        }

        EntityPlayer entityPlayer = getEntityPlayer(player);

        entityPlayer.openTrade(getWindowId(entityPlayer), offers, level, experience, true, false);
    }

    /**
     * Gets the entity player associated to this player
     *
     * @param player the player to get the entity player from
     * @return the entity player
     * @since 0.10.1
     */
    @NotNull
    @Contract(pure = true)
    private EntityPlayer getEntityPlayer(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    /**
     * Gets the window id for the inventory view the player currently has open
     *
     * @param entityPlayer the player to get the window id for
     * @return the window id
     * @since 0.10.1
     */
    @Contract(pure = true)
    private int getWindowId(@NotNull EntityPlayer entityPlayer) {
        return entityPlayer.activeContainer.windowId;
    }
}
