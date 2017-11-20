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
 * A pane for items that should be outlined in a specific pattern. E.g. left, center or right outline
 */
public class OutlinePane extends Pane {

    /**
     * A set of items inside this pane
     */
    private final GUIItem[] items;

    /**
     * Constructs a new default pane
     *
     * @param start  the upper left corner of the pane
     * @param length the length of the pane
     * @param width  the width of the pane
     */
    public OutlinePane(@NotNull GUILocation start, int length, int width) {
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
     */
    public boolean addItem(@NotNull GUIItem item) {
        int openIndex = -1;

        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                openIndex = i;
                break;
            }
        }

        if (openIndex == -1)
            return false;

        items[openIndex] = item;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean click(@NotNull InventoryClickEvent event) {
        if (listener != null)
            listener.accept(event);

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