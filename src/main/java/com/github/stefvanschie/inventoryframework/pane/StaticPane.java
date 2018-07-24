package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.GuiLocation;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.util.Pane;
import com.github.stefvanschie.inventoryframework.util.GeometryUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A pane for static items and stuff. All items will have to be specified a slot, or will be added in the next position.
 */
public class StaticPane extends Pane {

    /**
     * A set of items inside this pane
     */
    private final Set<Map.Entry<GuiItem, GuiLocation>> items;

    /**
     * The clockwise rotation of this pane in degrees
     */
    private int rotation;

    /**
     * Whether the items should be flipped horizontally and/or vertically
     */
    private boolean flipHorizontally, flipVertically;

    /**
     * Constructs a new default pane
     *
     * @param start the upper left corner of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     */
    public StaticPane(@NotNull GuiLocation start, int length, int height) {
        super(start, length, height);

        this.items = new HashSet<>(length * height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void display(@NotNull Inventory inventory, int paneOffsetX, int paneOffsetY, int maxLength, int maxHeight) {
        int length = Math.min(this.length, maxLength);
        int height = Math.min(this.height, maxHeight);

        items.stream().filter(entry -> {
            GuiItem key = entry.getKey();
            GuiLocation value = entry.getValue();

            return key.isVisible() && value.getX() + paneOffsetX <= 9 && value.getY() + paneOffsetY <= 6;
        }).forEach(entry -> {
            GuiLocation location = entry.getValue();

            int x = location.getX(), y = location.getY();

            if (flipHorizontally)
                x = length - x - 1;

            if (flipVertically)
                y = height - y - 1;

            Map.Entry<Integer, Integer> coordinates = GeometryUtil.processClockwiseRotation(x, y, length, height,
                rotation);

            inventory.setItem((start.getY() + coordinates.getValue() + paneOffsetY) * 9 + (start.getX() + coordinates
                    .getKey() + paneOffsetX), entry.getKey().getItem());
        });
    }

    /**
     * Adds a gui item at the specific spot in the pane
     *
     * @param item the item to set
     * @param location the location of the item
     */
    public void addItem(@NotNull GuiItem item, @NotNull GuiLocation location) {
        items.add(new AbstractMap.SimpleEntry<>(item, location));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean click(@NotNull InventoryClickEvent event, int paneOffsetX, int paneOffsetY, int maxLength,
                         int maxHeight) {
        int length = Math.min(this.length, maxLength);
        int height = Math.min(this.height, maxHeight);

        int slot = event.getSlot();

        //correct coordinates
        int x = (slot % 9) - start.getX() - paneOffsetX;
        int y = (slot / 9) - start.getY() - paneOffsetY;

        //this isn't our item
        if (x < 0 || x > length || y < 0 || y > height)
            return false;

        //first we undo the rotation
        //this is the same as applying a new rotation to match up to 360, so we'll be doing that
        Map.Entry<Integer, Integer> coordinates = GeometryUtil.processCounterClockwiseRotation(x, y, length, height,
                rotation);

        int newX = coordinates.getKey(), newY = coordinates.getValue();

        if (flipHorizontally)
            newX = length - newX - 1;

        if (flipVertically)
            newY = height - newY - 1;

        //find the item on the correct spot
        for (Map.Entry<GuiItem, GuiLocation> entry : items) {
            GuiLocation location = entry.getValue();
            GuiItem item = entry.getKey();

            if (location.getX() != newX || location.getY() != newY || !item.getItem().equals(event.getCurrentItem()))
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
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Collection<GuiItem> getItems() {
        return items.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Contract(pure = true)
    @Override
    public Collection<Pane> getPanes() {
        return new HashSet<>();
    }

    /**
     * Sets whether this pane should flip its items horizontally
     *
     * @param flipHorizontally whether the pane should flip items horizontally
     */
    public void flipHorizontally(boolean flipHorizontally) {
        this.flipHorizontally = flipHorizontally;
    }

    /**
     * Sets whether this pane should flip its items vertically
     *
     * @param flipVertically whether the pane should flip items vertically
     */
    public void flipVertically(boolean flipVertically) {
        this.flipVertically = flipVertically;
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
     * Gets whether this pane's items are flipped horizontally
     *
     * @return true if the items are flipped horizontally, false otherwise
     */
    @Contract(pure = true)
    public boolean isFlippedHorizontally() {
        return flipHorizontally;
    }

    /**
     * Gets whether this pane's items are flipped vertically
     *
     * @return true if the items are flipped vertically, false otherwise
     */
    @Contract(pure = true)
    public boolean isFlippedVertically() {
        return flipVertically;
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
    public static StaticPane load(Object instance, @NotNull Element element) {
        try {
            StaticPane staticPane = new StaticPane(new GuiLocation(
                Integer.parseInt(element.getAttribute("x")),
                Integer.parseInt(element.getAttribute("y"))),
                Integer.parseInt(element.getAttribute("length")),
                Integer.parseInt(element.getAttribute("height"))
            );

            if (element.hasAttribute("rotation"))
                staticPane.setRotation(Integer.parseInt(element.getAttribute("rotation")));

            if (element.hasAttribute("flipHorizontally"))
                staticPane.flipHorizontally(Boolean.parseBoolean(element.getAttribute("flipHorizontally")));

            if (element.hasAttribute("flipVertically"))
                staticPane.flipVertically(Boolean.parseBoolean(element.getAttribute("flipVertically")));

            Pane.load(staticPane, instance, element);

            if (element.hasAttribute("populate"))
                return staticPane;

            NodeList childNodes = element.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);

                if (item.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                Element child = (Element) item;

                staticPane.addItem(Pane.loadItem(instance, child),
                    new GuiLocation(Integer.parseInt(child.getAttribute("x")),
                        Integer.parseInt(child.getAttribute("y"))));
            }

            return staticPane;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return null;
    }
}