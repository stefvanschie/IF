package com.gmail.stefvanschiedev.inventoryframework.pane;

import com.gmail.stefvanschiedev.inventoryframework.GuiItem;
import com.gmail.stefvanschiedev.inventoryframework.GuiLocation;
import com.gmail.stefvanschiedev.inventoryframework.pane.util.Pane;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A pane for items that should be outlined
 */
public class OutlinePane extends Pane {

    /**
     * A set of items inside this pane
     */
    private final Set<Map.Entry<GuiItem, GuiLocation>> items;

    /**
     * The clockwise rotation of this pane in degrees
     */
    private int rotation;

    /**
     * Constructs a new default pane
     *
     * @param start  the upper left corner of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     */
    public OutlinePane(@NotNull GuiLocation start, int length, int height) {
        super(start, length, height);

        this.items = new HashSet<>(length * height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void display(@NotNull Inventory inventory, int paneOffsetX, int paneOffsetY) {
        items.stream().filter(entry -> {
            GuiItem key = entry.getKey();
            GuiLocation value = entry.getValue();

            return key.isVisible() && key.getItem().getType() != Material.AIR &&
                    value.getX() + paneOffsetX <= 9 && value.getY() + paneOffsetY <= 6;
        }).forEach(entry -> {
            GuiLocation location = entry.getValue();
            int x = location.getX(), y = location.getY();
            int newX = x, newY = y;

            //apply rotations
            if (rotation == 90) {
                newX = height - 1 - y;
                //noinspection SuspiciousNameCombination
                newY = x;
            } else if (rotation == 180) {
                newX = length - 1 - x;
                newY = height - 1 - y;
            } else if (rotation == 270) {
                //noinspection SuspiciousNameCombination
                newX = y;
                newY = length - 1 - x;
            }

            inventory.setItem((start.getY() + newY + paneOffsetY) * 9 + (start.getX() + newX + paneOffsetX),
                    entry.getKey().getItem());
        });
    }

    /**
     * Adds a gui item at the specific spot in the pane
     *
     * @param item the item to set
     */
    public void addItem(@NotNull GuiItem item) {
        int highestPos = -1;

        for (Map.Entry<GuiItem, GuiLocation> entry : items) {
            int pos = entry.getValue().getY() * length + entry.getValue().getX();

            if (pos <= highestPos)
                continue;

            highestPos = pos;
        }

        if (highestPos == length * height - 1)
            return;

        int newPos = highestPos + 1;

        items.add(new AbstractMap.SimpleEntry<>(item, new GuiLocation(newPos % length, newPos / height)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean click(@NotNull InventoryClickEvent event, int paneOffsetX, int paneOffsetY) {
        int slot = event.getSlot();

        //correct coordinates
        int x = (slot % 9) - start.getX() - paneOffsetX;
        int y = (slot / 9) - start.getY() - paneOffsetY;

        //this isn't our item
        if (x < 0 || x > length || y < 0 || y > height)
            return false;

        //first we undo the rotation
        //this is the same as applying a new rotation to match up to 360, so we'll be doing that
        int newX = x;
        int newY = y;

        if (rotation == 90) {
            //noinspection SuspiciousNameCombination
            newX = y;
            newY = length - 1 - x;
        } else if (rotation == 180) {
            newX = length - 1 - x;
            newY = height - 1 - y;
        } else if (rotation == 270) {
            newX = height - 1 - y;
            //noinspection SuspiciousNameCombination
            newY = x;
        }

        //find the item on the correct spot
        for (Map.Entry<GuiItem, GuiLocation> entry : items) {
            GuiLocation location = entry.getValue();
            GuiItem item = entry.getKey();

            if (location.getX() != newX || location.getY() != newY ||
                    !item.getItem().equals(event.getCurrentItem()))
                continue;

            if (!item.isVisible())
                return false;

            Consumer<InventoryClickEvent> action = item.getAction();

            if (action != null)
                action.accept(event);

            return true;
        }

        return false;
    }

    /**
     * Sets the rotation of this pane. The rotation is in degrees and can only be in increments of 90. Anything higher
     * than 360, will be lowered to a value in between [0, 360) while maintaining the same rotational value. E.g. 450
     * degrees becomes 90 degrees, 1080 degrees becomes 0, etc.
     *
     * This method fails for any pane that has a length and height which are unequal.
     *
     * @param rotation the rotation of this pane
     * @throws AssertionError when the length and height of the pane are not the same
     */
    public void setRotation(int rotation) {
        assert length == height : "length and height are different";
        assert rotation % 90 == 0 : "rotation isn't divisible by 90";

        this.rotation = rotation % 360;
    }

    /**
     * Gets the rotation specified to this pane. If no rotation has been set, or if this pane is not capable of having a
     * rotation, 0 is returned.
     *
     * @return the rotation for this pane
     */
    @Contract(pure = true)
    public int getRotation() {
        return rotation;
    }

    /**
     * Loads an outline pane from a given element
     *
     * @param instance the instance class
     * @param element the element
     * @return the outline pane
     */
    @Nullable
    @Contract("_, null -> fail")
    public static OutlinePane load(Object instance, @NotNull Element element) {
        try {
            OutlinePane outlinePane = new OutlinePane(new GuiLocation(
                Integer.parseInt(element.getAttribute("x")),
                Integer.parseInt(element.getAttribute("y"))),
                Integer.parseInt(element.getAttribute("length")),
                Integer.parseInt(element.getAttribute("height"))
            );

            Pane.load(outlinePane, instance, element);

            if (element.hasAttribute("populate"))
                return outlinePane;

            if (element.hasAttribute("rotation"))
                outlinePane.setRotation(Integer.parseInt(element.getAttribute("rotation")));

            NodeList childNodes = element.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);

                if (item.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                if (item.getNodeName().equals("empty"))
                    outlinePane.addItem(new GuiItem(new ItemStack(Material.AIR)));
                else
                    outlinePane.addItem(Pane.loadItem(instance, (Element) item));
            }

            return outlinePane;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return null;
    }
}