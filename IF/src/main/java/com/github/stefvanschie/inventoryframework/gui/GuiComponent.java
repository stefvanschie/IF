package com.github.stefvanschie.inventoryframework.gui;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.util.GuiItemContainer;
import com.github.stefvanschie.inventoryframework.pane.util.PositionedPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * Represents a component within a gui that can hold items. This is always in the shape of a rectangular grid.
 *
 * @since 0.8.0
 */
public class GuiComponent {

    /**
     * A set of all panes in this inventory. This is guaranteed to be sorted in order of the pane's priorities, from the
     * lowest priority to the highest priority. The order of panes with the same priority is unspecified.
     */
    @NotNull
    protected final List<PositionedPane> panes = new ArrayList<>();

    /**
     * A container for all the items in this component.
     */
    @NotNull
    private GuiItemContainer container;

    /**
     * Creates a new gui component with the specified length and width. If either the length or the width is less than
     * zero, an {@link IllegalArgumentException} will be thrown.
     *
     * @param length the length of the component
     * @param height the height of the component
     * @since 0.8.0
     */
    public GuiComponent(int length, int height) {
        if (length < 0 || height < 0) {
            throw new IllegalArgumentException("Sizes must be greater or equal to zero");
        }

        this.container = new GuiItemContainer(length, height);
    }

    /**
     * Adds a pane to the current collection of panes.
     *
     * @param slot the position of the pane
     * @param pane the pane to add
     * @since 0.8.0
     */
    public void addPane(@NotNull Slot slot, @NotNull Pane pane) {
        PositionedPane positionedPane = new PositionedPane(slot, pane);

        int size = this.panes.size();

        if (size == 0) {
            this.panes.add(positionedPane);

            return;
        }

        Pane.Priority priority = pane.getPriority();

        int left = 0;
        int right = size - 1;

        while (left <= right) {
            int middle = (left + right) / 2;

            Pane.Priority middlePriority = getPane(middle).getPriority();

            if (middlePriority == priority) {
                this.panes.add(middle, positionedPane);

                return;
            }

            if (middlePriority.isLessThan(priority)) {
                left = middle + 1;
            } else if (middlePriority.isGreaterThan(priority)) {
                right = middle - 1;
            }
        }

        this.panes.add(right + 1, positionedPane);
    }

    /**
     * This will make each pane in this component render their items in this inventory component. The panes are
     * displayed according to their priority, with the lowest priority rendering first and the highest priority (note:
     * highest priority, not {@link Pane.Priority#HIGHEST} priority) rendering last. The items displayed in this
     * gui component will be put into the specified inventory. The slots will start at the given offset up to this
     * component's size + the offset specified.
     *
     * @param inventory the inventory to place the items in
     * @param offset the offset from which to start counting the slots
     * @since 0.8.0
     * @see #display(PlayerInventory, int)
     */
    public void display(@NotNull Inventory inventory, int offset) {
        display();

        placeItems(inventory, offset);
    }

    /**
     * This will make each pane in this component render their items in this gui component. The panes are displayed
     * according to their priority, with the lowest priority rendering first and the highest priority (note: highest
     * priority, not {@link Pane.Priority#HIGHEST} priority) rendering last. The items displayed in this gui component
     * will be put into the inventory found in {@link InventoryBased#getInventory()}. The slots will be placed from the
     * top-right to the bottom-left, continuing from left-to-right, top-to-bottom plus the specified offset. This
     * ordering is different from the normal ordering of the indices of a {@link PlayerInventory}. See for the normal
     * ordering of a {@link PlayerInventory}'s slots its documentation.
     *
     * @param inventory the inventory to place the items in
     * @param offset the offset from which to start counting the slots
     * @since 0.8.0
     * @see #display(Inventory, int)
     */
    public void display(@NotNull PlayerInventory inventory, int offset) {
        display();

        placeItems(inventory, offset);
    }

    /**
     * This places the items currently existing in this gui component into the specified player inventory. The slots
     * will be placed from the top-right to the bottom-left, continuing from left-to-right, top-to-bottom plus the
     * specified offset. This ordering is different from the normal ordering of the indices of a
     * {@link PlayerInventory}. See for the normal ordering of a {@link PlayerInventory}'s slots its documentation. In
     * contrast to {@link #display(PlayerInventory, int)} this does not render the panes of this component.
     *
     * @param inventory the inventory to place the items in
     * @param offset the offset from which to start counting the slots
     * @since 0.8.0
     * @see #placeItems(Inventory, int)
     */
    public void placeItems(@NotNull PlayerInventory inventory, int offset) {
        for (int x = 0; x < getLength(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int slot;

                if (y == getHeight() - 1) {
                    slot = x + offset;
                } else {
                    slot = (y + 1) * getLength() + x + offset;
                }

                GuiItem item = this.container.getItem(x, y);

                if (item == null) {
                    continue;
                }

                inventory.setItem(slot, item.getItem());
            }
        }
    }

    /**
     * This places the items currently existing in this gui component into the specified inventory. The slots will start
     * at the given offset up to this component's size + the offset specified. In contrast to
     * {@link #display(Inventory, int)} this does not render the panes of this component.
     *
     * @param inventory the inventory to place the items in
     * @param offset the offset from which to start counting the slots
     * @since 0.8.0
     * @see #placeItems(PlayerInventory, int)
     */
    public void placeItems(@NotNull Inventory inventory, int offset) {
        for (int x = 0; x < getLength(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                GuiItem item = this.container.getItem(x, y);

                if (item == null) {
                    continue;
                }

                inventory.setItem(y * getLength() + x + offset, item.getItem());
            }
        }
    }

    /**
     * Delegates the handling of the specified click event to the panes of this component. This will call
     * {@link Pane#click(Gui, GuiComponent, InventoryClickEvent, Slot)} on each pane until the
     * right item has been found.
     *
     * @param gui the gui this inventory component belongs to
     * @param event the event to delegate
     * @param index the slot that was clicked
     * @since 0.8.0
     */
    public void click(@NotNull Gui gui, @NotNull InventoryClickEvent event, int index) {
        int x = index % getLength();
        int y = index / getLength();

        //loop panes in reverse, because the highest priority pane (last in list) is most likely to have the right item
        for (int i = this.panes.size() - 1; i >= 0; i--) {
            PositionedPane positionedPane = this.panes.get(i);
            Slot paneSlot = positionedPane.getSlot();
            Slot innerSlot = Slot.fromXY(x - paneSlot.getX(getLength()), y - paneSlot.getY(getLength()));

            if (positionedPane.getPane().click(gui, this, event, innerSlot)) {
                break;
            }
        }
    }

    /**
     * Creates a deep copy of this gui component. This means that all internal items will be cloned and all panes will
     * be copied as per their own {@link ItemStack#clone()} and {@link Pane#copy()} methods. The returned gui component
     * is guaranteed to not reference equals this gui component.
     *
     * @return the new gui component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent copy() {
        GuiComponent guiComponent = new GuiComponent(getLength(), getHeight());

        for (PositionedPane positionedPane : this.panes) {
            guiComponent.addPane(positionedPane.getSlot(), positionedPane.getPane().copy());
        }

        guiComponent.container = this.container.copy();

        return guiComponent;
    }

    /**
     * Returns a new gui component, excluding the range of specified rows. The new gui component will have its size
     * shrunk so only the included rows are present and any items in the excluded rows are discarded. All panes will
     * stay present. Note that while this does make a new gui component, it does not make a copy. For example, the panes
     * in the new gui component will be the exact same panes as in this one and will not be copied. This is also true
     * for any retained items. The specified range is 0-indexed: the first row starts at index 0 and the last row ends
     * at height - 1. The range is inclusive on both ends, the row specified at either parameter will also be excluded.
     * When the range specified is invalid - that is, part of the range contains rows that are not included in this gui
     * component, and {@link IllegalArgumentException} will be thrown.
     *
     * @param from the starting index of the range
     * @param end the ending index of the range
     * @return the new, shrunk gui component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent excludeRows(int from, int end) {
        if (from < 0 || end >= getHeight()) {
            throw new IllegalArgumentException("Specified range includes non-existent rows");
        }

        int newHeight = getHeight() - (end - from + 1);

        GuiComponent newGuiComponent = new GuiComponent(getLength(), newHeight);

        for (PositionedPane positionedPane : this.panes) {
            newGuiComponent.addPane(positionedPane.getSlot(), positionedPane.getPane());
        }

        newGuiComponent.container = this.container.excludeRows(from, end);

        return newGuiComponent;
    }

    /**
     * Loads the provided element's child panes onto this component. If the element contains any child panes, this will
     * mutate this component.
     *
     * @param instance the instance to apply field and method references on
     * @param element the element to load
     * @param plugin the plugin to load the panes with
     * @since 0.10.12
     */
    public void load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        NodeList childNodes = element.getChildNodes();

        for (int innerIndex = 0; innerIndex < childNodes.getLength(); innerIndex++) {
            Node innerItem = childNodes.item(innerIndex);

            if (innerItem.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            addPane(Slot.deserialize((Element) innerItem), Gui.loadPane(instance, innerItem, plugin));
        }
    }

    /**
     * Checks whether this component has at least one item. If it does, true is returned; false otherwise.
     *
     * @return true if this has an item, false otherwise
     * @since 0.8.0
     */
    @Contract(pure = true)
    public boolean hasItem() {
        for (int x = 0; x < getLength(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (this.container.getItem(x, y) != null) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * This will make each pane in this component render their items in this gui component. The panes are displayed
     * according to their priority, with the lowest priority rendering first and the highest priority (note: highest
     * priority, not {@link Pane.Priority#HIGHEST} priority) rendering last.
     *
     * @since 0.8.0
     * @see #display(Inventory, int)
     */
    public void display() {
        this.container.clearItems();

        for (PositionedPane positionedPane : this.panes) {
            Pane pane = positionedPane.getPane();

            if (!pane.isVisible()) {
                continue;
            }

            Slot slot = positionedPane.getSlot();

            this.container.apply(pane.display(), slot.getX(getLength()), slot.getY(getLength()));
        }
    }

    /**
     * Gets a list of panes with their positions this gui component contains. The returned list is unmodifiable. If this
     * gui component currently does not have any panes, an empty list is returned. This list is guaranteed to be sorted
     * according to the panes' priorities. The returned collection is not synchronized and no guarantees should be made
     * as to the safety of concurrently accessing the returned collection. If synchronized behaviour should be allowed,
     * the returned collection must be synchronized externally.
     *
     * @return the panes this component has
     * @since 0.12.0
     */
    @NotNull
    @Contract(pure = true)
    public List<? extends PositionedPane> getPositionedPanes() {
        return this.panes;
    }

    /**
     * Gets a list of panes this gui component contains. The returned list is modifiable. If this gui component
     * currently does not have any panes, an empty list is returned. This list is guaranteed to be sorted according to
     * the panes' priorities.
     *
     * @return the panes this component has
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public List<Pane> getPanes() {
        List<Pane> panes = new ArrayList<>(getPositionedPanes().size());

        for (PositionedPane positionedPane : getPositionedPanes()) {
            panes.add(positionedPane.getPane());
        }

        return panes;
    }


    /**
     * Gets the height of this gui component.
     *
     * @return the height
     * @since 0.8.0
     */
    @Contract(pure = true)
    public int getHeight() {
        return this.container.getHeight();
    }

    /**
     * Gets the length of this gui component.
     *
     * @return the length
     * @since 0.8.0
     */
    @Contract(pure = true)
    public int getLength() {
        return this.container.getLength();
    }

    /**
     * Gets the pane at the specified index.
     *
     * @param index the index of the pane
     * @return the pane
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    private Pane getPane(int index) {
        if (!isInBounds(0, this.panes.size() - 1, index)) {
            throw new IllegalArgumentException("Index not in pane list");
        }

        return this.panes.get(index).getPane();
    }

    /**
     * Checks whether a number is within the specified number bound (inclusive on both ends).
     *
     * @param lowerBound the lower bound of the range
     * @param upperBound the upper bound of the range
     * @param value the value to check
     * @return true if the value is within the bounds, false otherwise
     * @since 0.8.0
     */
    @Contract(pure = true)
    private boolean isInBounds(int lowerBound, int upperBound, int value) {
        return lowerBound <= value && value <= upperBound;
    }
}
