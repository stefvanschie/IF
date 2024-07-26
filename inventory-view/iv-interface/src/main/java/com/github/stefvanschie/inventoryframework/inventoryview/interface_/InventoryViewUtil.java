package com.github.stefvanschie.inventoryframework.inventoryview.interface_;

import com.github.stefvanschie.inventoryframework.inventoryview.abstraction.AbstractInventoryViewUtil;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper for {@link InventoryView} methods that apply when {@link InventoryView} was an abstract class.
 *
 * @since 0.10.16
 */
public class InventoryViewUtil implements AbstractInventoryViewUtil {

    /**
     * Instance of this singleton class.
     */
    @NotNull
    private static final InventoryViewUtil INSTANCE = new InventoryViewUtil();

    @NotNull
    @Override
    public Inventory getBottomInventory(@NotNull InventoryView view) {
        return view.getBottomInventory();
    }

    @Nullable
    @Override
    public ItemStack getCursor(@NotNull InventoryView view) {
        return view.getCursor();
    }

    @Override
    public void setCursor(@NotNull InventoryView view, @Nullable ItemStack item) {
        view.setCursor(item);
    }

    @Nullable
    @Override
    public Inventory getInventory(@NotNull InventoryView view, int slot) {
        return view.getInventory(slot);
    }

    @NotNull
    @Override
    public InventoryType.SlotType getSlotType(@NotNull InventoryView view, int slot) {
        return view.getSlotType(slot);
    }

    @NotNull
    @Override
    public String getTitle(@NotNull InventoryView view) {
        return view.getTitle();
    }

    @NotNull
    @Override
    public Inventory getTopInventory(@NotNull InventoryView view) {
        return view.getTopInventory();
    }

    /**
     * Gets the singleton instance of this class.
     *
     * @return the instance of this class
     * @since 0.10.16
     */
    @NotNull
    @Contract(pure = true)
    public static InventoryViewUtil getInstance() {
        return INSTANCE;
    }
}
