package com.github.stefvanschie.inventoryframework.nms.v1_20_5.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility class for custom inventories
 *
 * @since 0.10.14
 */
public final class CustomInventoryUtil {

    /**
     * A private constructor to prevent construction.
     */
    private CustomInventoryUtil() {}

    /**
     * Converts an array of Bukkit items into a non-null list of NMS items. The returned list is modifiable. If no items
     * were specified, this returns an empty list.
     *
     * @param items the items to convert
     * @return a list of converted items
     * @since 0.10.14
     */
    @NotNull
    @Contract(pure = true)
    public static NonNullList<ItemStack> convertToNMSItems(@Nullable org.bukkit.inventory.ItemStack @NotNull [] items) {
        NonNullList<ItemStack> nmsItems = NonNullList.create();

        for (org.bukkit.inventory.ItemStack item : items) {
            nmsItems.add(CraftItemStack.asNMSCopy(item));
        }

        return nmsItems;
    }
}
