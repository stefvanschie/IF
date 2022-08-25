package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.pane.util.Mask;
import com.github.stefvanschie.inventoryframework.util.GeometryUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
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
     * The alignment of this pane
     */
    @NotNull
    private Alignment alignment = Alignment.BEGIN;

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
    public void display(@NotNull InventoryComponent inventoryComponent, int paneOffsetX, int paneOffsetY, int maxLength,
                        int maxHeight) {
        int length = Math.min(this.length, maxLength);
        int height = Math.min(this.height, maxHeight);

        int itemIndex = 0;
        int gapCount = 0;

        int size;

        if (getOrientation() == Orientation.HORIZONTAL) {
            size = height;
        } else if (getOrientation() == Orientation.VERTICAL) {
            size = length;
        } else {
            throw new IllegalStateException("Unknown orientation '" + getOrientation() + "'");
        }

        for (int vectorIndex = 0; vectorIndex < size && getItems().size() > itemIndex; vectorIndex++) {
            boolean[] maskLine;

            if (getOrientation() == Orientation.HORIZONTAL) {
                maskLine = mask.getRow(vectorIndex);
            } else if (getOrientation() == Orientation.VERTICAL) {
                maskLine = mask.getColumn(vectorIndex);
            } else {
                throw new IllegalStateException("Unknown orientation '" + getOrientation() + "'");
            }

            int enabled = 0;

            for (boolean bool : maskLine) {
                if (bool) {
                    enabled++;
                }
            }

            GuiItem[] items;

            if (doesRepeat()) {
                items = new GuiItem[enabled];
            } else {
                int remainingPositions = gapCount + (getItems().size() - itemIndex - 1) * (getGap() + 1) + 1;

                items = new GuiItem[Math.min(enabled, remainingPositions)];
            }

            for (int index = 0; index < items.length; index++) {
                if (gapCount == 0) {
                    items[index] = getItems().get(itemIndex);

                    itemIndex++;

                    if (doesRepeat() && itemIndex >= getItems().size()) {
                        itemIndex = 0;
                    }

                    gapCount = getGap();
                } else {
                    items[index] = null;

                    gapCount--;
                }
            }

            int index;

            if (getAlignment() == Alignment.BEGIN) {
                index = 0;
            } else if (getAlignment() == Alignment.CENTER) {
                index = -((enabled - items.length) / 2);
            } else {
                throw new IllegalStateException("Unknown alignment '" + getAlignment() + "'");
            }

            for (int opposingVectorIndex = 0; opposingVectorIndex < maskLine.length; opposingVectorIndex++) {
                if (!maskLine[opposingVectorIndex]) {
                    continue;
                }

                if (index >= 0 && index < items.length && items[index] != null) {
                    int x, y;

                    if (getOrientation() == Orientation.HORIZONTAL) {
                        x = opposingVectorIndex;
                        y = vectorIndex;
                    } else if (getOrientation() == Orientation.VERTICAL) {
                        x = vectorIndex;
                        y = opposingVectorIndex;
                    } else {
                        throw new IllegalStateException("Unknown orientation '" + getOrientation() + "'");
                    }

                    if (flipHorizontally) {
                        x = length - x - 1;
                    }

                    if (flipVertically) {
                        y = height - y - 1;
                    }

                    Map.Entry<Integer, Integer> coordinates = GeometryUtil.processClockwiseRotation(x, y,
                            length, height, rotation);

                    x = coordinates.getKey();
                    y = coordinates.getValue();

                    if (x >= 0 && x < length && y >= 0 && y < height) {
                        int finalRow = getY() + y + paneOffsetY;
                        int finalColumn = getX() + x + paneOffsetX;

                        GuiItem item = items[index];
                        if (!item.isVisible()) {
                            continue;
                        }
                        inventoryComponent.setItem(item, finalColumn, finalRow);
                    }
                }

                index++;
            }
        }
    }

    @Override
    public boolean click(@NotNull Gui gui, @NotNull InventoryComponent inventoryComponent,
                         @NotNull InventoryClickEvent event, int slot, int paneOffsetX, int paneOffsetY, int maxLength,
                         int maxHeight) {
        int length = Math.min(this.length, maxLength);
        int height = Math.min(this.height, maxHeight);

        int adjustedSlot = slot - (getX() + paneOffsetX) - inventoryComponent.getLength() * (getY() + paneOffsetY);

        int x = adjustedSlot % inventoryComponent.getLength();
        int y = adjustedSlot / inventoryComponent.getLength();

        //this isn't our item
        if (x < 0 || x >= length || y < 0 || y >= height) {
            return false;
        }

        callOnClick(event);

        ItemStack itemStack = event.getCurrentItem();

        if (itemStack == null) {
            return false;
        }

        GuiItem item = findMatchingItem(items, itemStack);

        if (item == null) {
            return false;
        }

        item.callAction(event);

        return true;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public OutlinePane copy() {
        OutlinePane outlinePane = new OutlinePane(x, y, length, height, getPriority());

        for (GuiItem item : items) {
            outlinePane.addItem(item.copy());
        }

        outlinePane.setVisible(isVisible());
        outlinePane.onClick = onClick;

        outlinePane.uuid = uuid;

        outlinePane.orientation = orientation;
        outlinePane.rotation = rotation;
        outlinePane.gap = gap;
        outlinePane.repeat = repeat;
        outlinePane.flipHorizontally = flipHorizontally;
        outlinePane.flipVertically = flipVertically;
        outlinePane.mask = mask;
        outlinePane.alignment = alignment;

        return outlinePane;
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
    public void setLength(int length) {
        super.setLength(length);

        applyMask(getMask().setLength(length));
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);

        applyMask(getMask().setHeight(height));
    }

    /**
     * Aligns the pane in the way specified by the provided alignment.
     *
     * @param alignment the new alignment
     * @since 0.10.1
     */
    public void align(@NotNull Alignment alignment) {
        this.alignment = alignment;
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
     * Gets the alignment set on this pane.
     *
     * @return the alignment
     * @since 0.10.1
     */
    @NotNull
    @Contract(pure = true)
    public Alignment getAlignment() {
        return this.alignment;
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
     * Gets the mask applied to this pane.
     *
     * @return the mask
     * @since 0.6.2
     */
    @NotNull
    @Contract(pure = true)
    public Mask getMask() {
        return mask;
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

            if (element.hasAttribute("alignment")) {
                outlinePane.align(Alignment.valueOf(element.getAttribute("alignment").toUpperCase()));
            }

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

    /**
     * An enum containing different alignments that can be used on the outline pane.
     *
     * @since 0.10.1
     */
    public enum Alignment {

        /**
         * Aligns the items at the start of the pane.
         *
         * @since 0.10.1
         */
        BEGIN,

        /**
         * Aligns the items in the center of the pane. If there is no exact center, this will preference the left (for a
         * horizontal orientation) or the top (for a vertical orientation).
         *
         * @since 0.10.1
         */
        CENTER
    }
}
