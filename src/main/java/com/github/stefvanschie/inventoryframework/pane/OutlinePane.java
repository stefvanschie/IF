package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.pane.util.Mask;
import com.github.stefvanschie.inventoryframework.util.GeometryUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * A pane for items that should be outlined
 */
public class OutlinePane extends Pane implements Flippable, Orientable, Rotatable {

    /**
     * A set of items inside this pane
     */
    @NotNull
    private final List<GuiItem> items;

    /**
     * The orientation of the items in this pane
     */
    @NotNull
    private Orientation orientation;

    /**
     * The clockwise rotation of this pane in degrees
     */
    private int rotation;

    /**
     * The amount of empty spots in between each item
     */
    private int gap;

    /**
     * Whether the items should be repeated to fill the entire pane
     */
    private boolean repeat;

    /**
     * Whether the items should be flipped horizontally and/or vertically
     */
    private boolean flipHorizontally, flipVertically;

    /**
     * The mask for this pane
     */
    @NotNull
    private Mask mask;

    public OutlinePane(int x, int y, int length, int height, @NotNull Priority priority) {
        super(x, y, length, height, priority);

        this.items = new ArrayList<>(length * height);
        this.orientation = Orientation.HORIZONTAL;

        String[] mask = new String[height];
        StringBuilder maskString = new StringBuilder();

        for (int i = 0; i < length; i++) {
            maskString.append('1');
        }

        Arrays.fill(mask, maskString.toString());

        this.mask = new Mask(mask);
    }

    public OutlinePane(int x, int y, int length, int height) {
        this(x, y, length, height, Priority.NORMAL);
    }

    public OutlinePane(int length, int height) {
        this(0, 0, length, height);
    }

    @Override
    public void display(@NotNull Gui gui, @NotNull Inventory inventory, @NotNull PlayerInventory playerInventory,
                        int paneOffsetX, int paneOffsetY, int maxLength, int maxHeight) {
        int length = Math.min(this.length, maxLength);
        int height = Math.min(this.height, maxHeight);

        int x = 0, y = 0;

        if (orientation == Orientation.HORIZONTAL) {
            outerloop:
            for (int rowIndex = 0; rowIndex < mask.getHeight(); rowIndex++) {
                boolean[] row = mask.getRow(rowIndex);

                for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
                    if (!row[columnIndex]) {
                        continue;
                    }

                    x = columnIndex;
                    y = rowIndex;
                    break outerloop;
                }
            }
        } else if (orientation == Orientation.VERTICAL) {
            outerloop:
            for (int columnIndex = 0; columnIndex < mask.getLength(); columnIndex++) {
                boolean[] column = mask.getColumn(columnIndex);

                for (int rowIndex = 0; rowIndex < column.length; rowIndex++) {
                    if (!column[rowIndex]) {
                        continue;
                    }

                    x = columnIndex;
                    y = rowIndex;
                    break outerloop;
                }
            }
        }

        int itemAmount = items.size();

        outerloop:
        for (int i = 0; i < (doesRepeat() ? mask.amountOfEnabledSlots() : itemAmount); i++) {
            GuiItem item = items.get(i % itemAmount);

            if (!item.isVisible())
                continue;

            int newX = x, newY = y;

            if (flipHorizontally)
                newX = length - x - 1;

            if (flipVertically)
                newY = height - y - 1;

            Map.Entry<Integer, Integer> coordinates = GeometryUtil.processClockwiseRotation(newX, newY, length, height,
                    rotation);

            int finalRow = getY() + coordinates.getValue() + paneOffsetY;
            int finalColumn = getX() + coordinates.getKey() + paneOffsetX;

            if (finalRow >= gui.getRows()) {
                gui.setState(Gui.State.BOTTOM);

                if (finalRow == gui.getRows() + 3) {
                    playerInventory.setItem(finalColumn, item.getItem());
                } else {
                    playerInventory.setItem(((finalRow - gui.getRows()) + 1) * 9 + finalColumn, item.getItem());
                }
            } else {
                inventory.setItem(finalRow * 9 + finalColumn, item.getItem());
            }

            int gapCount = gap;

            do {
                if (orientation == Orientation.HORIZONTAL) {
                    x++;

                    if (x >= length) {
                        y++;
                        x = 0;
                    }
                } else if (orientation == Orientation.VERTICAL) {
                    y++;

                    if (y >= height) {
                        x++;
                        y = 0;
                    }
                }

                //stop the loop when there is no more space in the pane
                if (x >= length || y >= height) {
                    break outerloop;
                }

                if (mask.isEnabled(x, y)) {
                    gapCount--;
                }
            } while (gapCount >= 0);
        }
    }

    @Override
    public boolean click(@NotNull Gui gui, @NotNull InventoryClickEvent event, int paneOffsetX, int paneOffsetY,
                         int maxLength, int maxHeight) {
        int length = Math.min(this.length, maxLength);
        int height = Math.min(this.height, maxHeight);

        int slot = event.getSlot();

        int x, y;

        //noinspection ConstantConditions
        if (Gui.getInventory(event.getView(), event.getRawSlot()).equals(event.getView().getBottomInventory())) {
            x = (slot % 9) - getX() - paneOffsetX;
            y = ((slot / 9) + gui.getRows() - 1) - getY() - paneOffsetY;

            if (slot / 9 == 0) {
                y = (gui.getRows() + 3) - getY() - paneOffsetY;
            }
        } else {
            x = (slot % 9) - getX() - paneOffsetX;
            y = (slot / 9) - getY() - paneOffsetY;
        }

        //this isn't our item
        if (x < 0 || x >= length || y < 0 || y >= height)
            return false;

        if (onClick != null)
            onClick.accept(event);

        ItemStack itemStack = event.getCurrentItem();

        if (itemStack == null) {
            return false;
        }

        GuiItem item = findMatchingItem(items, itemStack);

        if (item == null) {
            return false;
        }

        item.getAction().accept(event);

        return true;
    }

    @Override
    public void setRotation(int rotation) {
        if (length != height) {
            throw new UnsupportedOperationException("length and height are different");
        }

        if (rotation % 90 != 0) {
            throw new IllegalArgumentException("rotation isn't divisible by 90");
        }

        this.rotation = rotation % 360;
    }

    /**
     * Adds a gui item in the specified index
     *
     * @param item the item to add
     * @param index the item's index
     */
    public void insertItem(@NotNull GuiItem item, int index) {
        items.add(index, item);
    }

    /**
     * Adds a gui item at the specific spot in the pane
     *
     * @param item the item to set
     */
    public void addItem(@NotNull GuiItem item) {
        items.add(item);
    }

    /**
     * Removes the specified item from the pane
     *
     * @param item the item to remove
     * @since 0.5.8
     */
    public void removeItem(@NotNull GuiItem item) {
        items.remove(item);
    }

    @Override
    public void clear() {
        items.clear();
    }

    /**
     * Applies a custom mask to this pane. This will throw an {@link IllegalArgumentException} when the mask's dimension
     * differs from this pane's dimension.
     *
     * @param mask the mask to apply to this pane
     * @throws IllegalArgumentException when the mask's dimension is incorrect
     * @since 0.5.16
     */
    public void applyMask(@NotNull Mask mask) {
        if (length != mask.getLength() || height != mask.getHeight()) {
            throw new IllegalArgumentException("Mask's dimension must be the same as the pane's dimension");
        }

        this.mask = mask;
    }

    @Override
    public void flipHorizontally(boolean flipHorizontally) {
        this.flipHorizontally = flipHorizontally;
    }

    @Override
    public void flipVertically(boolean flipVertically) {
        this.flipVertically = flipVertically;
    }

    /**
     * Sets the gap of the pane
     *
     * @param gap the new gap
     */
    public void setGap(int gap) {
        this.gap = gap;
    }

    @Override
    public void setOrientation(@NotNull Orientation orientation) {
        this.orientation = orientation;
    }

    /**
     * Sets whether this pane should repeat itself
     *
     * @param repeat whether the pane should repeat
     */
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public Collection<Pane> getPanes() {
        return new HashSet<>();
    }

    /**
     * Gets whether this outline pane repeats itself
     *
     * @return true if this pane repeats, false otherwise
     */
    @Contract(pure = true)
    public boolean doesRepeat() {
        return repeat;
    }

    /**
     * Gets the gap of the pane
     *
     * @return the gap
     */
    @Contract(pure = true)
    public int getGap() {
        return gap;
    }

    @NotNull
    @Override
    public List<GuiItem> getItems() {
        return items;
    }

    /**
     * Gets the orientation of this outline pane
     *
     * @return the orientation
     */
    @NotNull
    @Contract(pure = true)
    @Override
    public Orientation getOrientation() {
        return orientation;
    }

    @Contract(pure = true)
    @Override
    public int getRotation() {
        return rotation;
    }

    @Contract(pure = true)
    @Override
    public boolean isFlippedHorizontally() {
        return flipHorizontally;
    }

    @Contract(pure = true)
    @Override
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
    @NotNull
    public static OutlinePane load(@NotNull Object instance, @NotNull Element element) {
        try {
            OutlinePane outlinePane = new OutlinePane(
                Integer.parseInt(element.getAttribute("length")),
                Integer.parseInt(element.getAttribute("height"))
            );

            if (element.hasAttribute("gap"))
                outlinePane.setGap(Integer.parseInt(element.getAttribute("gap")));

            if (element.hasAttribute("repeat"))
                outlinePane.setRepeat(Boolean.parseBoolean(element.getAttribute("repeat")));

            Pane.load(outlinePane, instance, element);
            Flippable.load(outlinePane, element);
            Orientable.load(outlinePane, element);
            Rotatable.load(outlinePane, element);

            if (element.hasAttribute("populate"))
                return outlinePane;

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
        } catch (NumberFormatException exception) {
            throw new XMLLoadException(exception);
        }
    }
}