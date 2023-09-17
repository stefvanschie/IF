package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A pane for static items and stuff. All items will have to be specified a slot, or will be added in the next position.
 * <p>
 * This pane allows you to specify the positions of the items either in the form of an x and y coordinate pair or as an
 * index, in which case the indexing starts from the top left and continues to the right and bottom, with the horizontal
 * axis taking priority. There are nuances at play with regard to mixing these two types of positioning systems within
 * the same pane. It's recommended to only use one of these systems per pane and to not mix them.
 * </p>
 */
public class StaticNullablePane extends StaticPane {

    /**
     * Creates a new static pane.
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     * @since 0.10.12
     */
    public StaticNullablePane(Slot slot, int length, int height, @NotNull Priority priority) {
        super(slot, length, height, priority);
    }

    public StaticNullablePane(int x, int y, int length, int height, @NotNull Priority priority) {
        this(Slot.fromXY(x, y), length, height, priority);
    }

    /**
     * Creates a new static nullable pane.
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @since 0.10.12
     */
    public StaticNullablePane(Slot slot, int length, int height) {
        this(slot, length, height, Priority.NORMAL);
    }

    public StaticNullablePane(int x, int y, int length, int height) {
        this(x, y, length, height, Priority.NORMAL);
    }

    public StaticNullablePane(int length, int height) {
        this(0, 0, length, height);
    }

    @Override
    public boolean click(@NotNull Gui gui, @NotNull InventoryComponent inventoryComponent,
                         @NotNull InventoryClickEvent event, int slot, int paneOffsetX, int paneOffsetY, int maxLength,
                         int maxHeight) {
        int length = Math.min(this.length, maxLength);
        int height = Math.min(this.height, maxHeight);

        Slot paneSlot = getSlot();

        int xPosition = paneSlot.getX(maxLength);
        int yPosition = paneSlot.getY(maxLength);

        int totalLength = inventoryComponent.getLength();

        int adjustedSlot = slot - (xPosition + paneOffsetX) - totalLength * (yPosition + paneOffsetY);

        int x = adjustedSlot % totalLength;
        int y = adjustedSlot / totalLength;

        //this isn't our item
        if (x < 0 || x >= length || y < 0 || y >= height) {
            return false;
        }

        callOnClick(event);

        GuiItem clickedItem = this.items.get(Slot.fromIndex(adjustedSlot));

        if (clickedItem == null) {
            return false;
        }

        clickedItem.callAction(event);

        return true;
    }
}
