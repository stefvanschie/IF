package com.gmail.stefvanschiedev.inventoryframework.pane;

import com.gmail.stefvanschiedev.inventoryframework.GUIItem;
import com.gmail.stefvanschiedev.inventoryframework.GUILocation;
import com.gmail.stefvanschiedev.inventoryframework.pane.util.Pane;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A pane for static items and stuff. All items will have to be specified a slot, or will be added in the next position.
 */
public class StaticPane extends Pane {

    /**
     * A set of items inside this pane
     */
    private GUIItem[] items;

    /**
     * Constructs a new default pane
     *
     * @param start  the upper left corner of the pane
     * @param length the length of the pane
     * @param width  the width of the pane
     */
    public StaticPane(@NotNull GUILocation start, int length, int width) {
        super(start, length, width);

        this.items = new GUIItem[length * width];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void display(@NotNull Inventory inventory) {
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < width; y++) {
                if (items[y * length + x] == null)
                    continue;

                ItemStack item = items[y * length + x].getItem();

                if (item.getType() == Material.AIR)
                    continue;

                inventory.setItem((start.getY() + y) * 9 + (start.getX() + x), item);
            }
        }
    }

    /**
     * Adds a gui item at the specific spot in the pane
     *
     * @param item the item to set
     * @param location the location of the item
     */
    public void addItem(@NotNull GUIItem item, @NotNull GUILocation location) {
        items[location.getY() * length + location.getX()] = item;
    }

    /**
     * Sets all the items to one specific item
     *
     * @param item the item to change everything to
     */
    public void fill(@NotNull GUIItem item) {
        for (int i = 0; i < items.length; i++)
            items[i] = item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean click(@NotNull InventoryClickEvent event) {
        int slot = event.getSlot();

        //correct coordinates
        int x = (slot % 9) - start.getX();
        int y = (slot / 9) - start.getY();

        if (y * length + x < 0 || y * length + x >= items.length || items[y * length + x] == null)
            return false;

        if (items[y * length + x].getItem().equals(event.getCurrentItem())) {
            Consumer<InventoryClickEvent> action = items[y * length + x].getAction();

            if (action != null)
                action.accept(event);

            return true;
        }

        return false;
    }
}