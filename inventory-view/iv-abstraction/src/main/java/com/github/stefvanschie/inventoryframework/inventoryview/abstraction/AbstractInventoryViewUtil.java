package com.github.stefvanschie.inventoryframework.inventoryview.abstraction;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper for {@link InventoryView} methods that apply when {@link InventoryView} was an abstract class.
 *
 * @since 0.10.16
 */
public interface AbstractInventoryViewUtil {

    /**
     * Behaves according to {@link InventoryView#getBottomInventory()}.
     *
     * @param view the {@link InventoryView} to invoke {@link InventoryView#getBottomInventory()} on
     * @return the result of invoking {@link InventoryView#getBottomInventory()}
     * @since 0.10.16
     */
    @NotNull
    Inventory getBottomInventory(@NotNull InventoryView view);

    /**
     * Behaves according to {@link InventoryView#getCursor()}.
     *
     * @param view the {@link InventoryView} to invoke {@link InventoryView#getCursor()} on
     * @return the result of invoking {@link InventoryView#getCursor()}
     * @since 0.10.16
     */
    @Nullable
    ItemStack getCursor(@NotNull InventoryView view);

    /**
     * Behaves according to {@link InventoryView#setCursor(ItemStack)}.
     *
     * @param view the {@link InventoryView} to invoke {@link InventoryView#setCursor(ItemStack)} on
     * @param item the {@link ItemStack} to apply when invoking {@link InventoryView#setCursor(ItemStack)}
     * @since 0.10.16
     */
    void setCursor(@NotNull InventoryView view, @Nullable ItemStack item);

    /**
     * Behaves according to {@link InventoryView#getInventory(int)}.
     *
     * @param view the {@link InventoryView} to invoke {@link InventoryView#getInventory(int)} on
     * @param slot the slot to apply when invoking {@link InventoryView#getInventory(int)}
     * @return the result of invoking {@link InventoryView#getInventory(int)}
     * @since 0.10.16
     */
    @Nullable
    Inventory getInventory(@NotNull InventoryView view, int slot);

    /**
     * Behaves according to {@link InventoryView#getSlotType(int)}.
     *
     * @param view the {@link InventoryView} to invoke {@link InventoryView#getSlotType(int)} on
     * @param slot the slot to apply when invoking {@link InventoryView#getSlotType(int)}
     * @return the result of invoking {@link InventoryView#getSlotType(int)}
     * @since 0.10.16
     */
    @NotNull
    InventoryType.SlotType getSlotType(@NotNull InventoryView view, int slot);

    /**
     * Behaves according to {@link InventoryView#getTitle()}.
     *
     * @param view the {@link InventoryView} to invoke {@link InventoryView#getTitle()} on
     * @return the result of invoking {@link InventoryView#getTitle()}
     * @since 0.10.16
     */
    @NotNull
    String getTitle(@NotNull InventoryView view);

    /**
     * Behaves according to {@link InventoryView#getTopInventory()}.
     *
     * @param view the {@link InventoryView} to invoke {@link InventoryView#getTopInventory()} on
     * @return the result of invoking {@link InventoryView#getTopInventory()}
     * @since 0.10.16
     */
    @NotNull
    Inventory getTopInventory(@NotNull InventoryView view);
}
