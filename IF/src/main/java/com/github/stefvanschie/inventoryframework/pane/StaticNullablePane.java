package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.github.stefvanschie.inventoryframework.util.GeometryUtil;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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

        GuiItem clickedItem = this.items.get(Slot.fromXY(x, y));
        if (clickedItem == null) {
            return false;
        }

        clickedItem.callAction(event);

        return true;
    }

    /**
     * {@inheritDoc}
     *
     * If there are multiple items in the same position when displaying the items, either one of those items may be
     * shown. In particular, there is no guarantee that a specific item will be shown.
     *
     * @param inventoryComponent {@inheritDoc}
     * @param paneOffsetX {@inheritDoc}
     * @param paneOffsetY {@inheritDoc}
     * @param maxLength {@inheritDoc}
     * @param maxHeight {@inheritDoc}
     */
    @Override
    public void display(@NotNull InventoryComponent inventoryComponent, int paneOffsetX, int paneOffsetY, int maxLength,
                        int maxHeight) {
        int length = Math.min(this.length, maxLength);
        int height = Math.min(this.height, maxHeight);

        items.entrySet().stream().filter(entry -> entry.getValue().isVisible()).forEach(entry -> {
            Slot location = entry.getKey();

            int x = location.getX(getLength());
            int y = location.getY(getLength());

            if (isFlippedHorizontally())
                x = length - x - 1;

            if (isFlippedVertically())
                y = height - y - 1;

            Map.Entry<Integer, Integer> coordinates = GeometryUtil.processClockwiseRotation(x, y, length, height,
                    getRotation());

            x = coordinates.getKey();
            y = coordinates.getValue();

            if (x < 0 || x >= length || y < 0 || y >= height) {
                return;
            }

            GuiItem item = entry.getValue();

            Slot slot = getSlot();
            int finalRow = slot.getY(maxLength) + y + paneOffsetY;
            int finalColumn = slot.getX(maxLength) + x + paneOffsetX;

            inventoryComponent.setItemUnsafe(item, finalColumn, finalRow);
        });
    }

    /**
     * Adds a rectangular area of the same GuiItem to the pane.
     * @param guiItem the item to add
     * @param startingX the starting x coordinate (north-west corner)
     * @param startingY the starting y coordinate (north-west corner)
     * @param xLength the length of the area
     * @param yLength the height of the area
     * @since 0.10.12
     */
    public void addItem(@NotNull GuiItem guiItem, int startingX, int startingY, int xLength, int yLength) {
        for(int i = startingX; i < startingX + xLength; i++) {
            for(int j = startingY; j < startingY + yLength; j++) {
                addItem(guiItem, i, j);
            }
        }
    }

    /**
     * Adds a rectangular area of the same GuiItem to the pane.
     * @param guiItem the item to add
     * @param slot the starting slot (north-west corner)
     * @param xLength the length of the area
     * @param yLength the height of the area
     * @since 0.10.12
     */
    public void addItem(@NotNull GuiItem guiItem, Slot slot,  int xLength, int yLength) {
        addItem(guiItem, slot.getX(getLength()), slot.getY(getLength()), xLength, yLength);
    }

    /**
     * Adds a SQUARED area of the same GuiItem to the pane.
     * @param guiItem the item to add
     * @param startingX the starting x coordinate (north-west corner)
     * @param startingY the starting y coordinate (north-west corner)
     * @param length the length and height of the squared area
     * @since 0.10.12
     */
    public void addItem(@NotNull GuiItem guiItem, int startingX, int startingY, int length) {
        addItem(guiItem, startingX, startingY, length, length);
    }

    /**
     * Adds a SQUARED area of the same GuiItem to the pane.
     * @param guiItem the item to add
     * @param slot the starting slot (north-west corner)
     * @param length the length and height of the squared area
     * @since 0.10.12
     */
    public void addItem(@NotNull GuiItem guiItem, Slot slot, int length) {
        this.addItem(guiItem, slot, length, length);
    }
}
