package com.github.stefvanschie.inventoryframework.pane.component;

import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A button for cycling between different options
 *
 * @since 0.5.0
 */
public class CycleButton extends Pane {

    /**
     * The list of pane used for display
     */
    private final List<Pane> panes = new ArrayList<>();

    /**
     * The current position of the cycle button
     */
    private int position = 0;

    /**
     * Creates a new cycle button
     *
     * @param slot the slot of the button
     * @param length the length of the button
     * @param height the height of the button
     * @param priority the priority of the button
     * @since 0.10.8
     */
    public CycleButton(@NotNull Slot slot, int length, int height, @NotNull Priority priority) {
        super(slot, length, height, priority);
    }

    public CycleButton(int x, int y, int length, int height, @NotNull Priority priority) {
        super(x, y, length, height, priority);
    }

    /**
     * Creates a new cycle button
     *
     * @param slot the slot of the button
     * @param length the length of the button
     * @param height the height of the button
     * @since 0.10.8
     */
    public CycleButton(@NotNull Slot slot, int length, int height) {
        super(slot, length, height);
    }

    public CycleButton(int x, int y, int length, int height) {
        super(x, y, length, height);
    }

    public CycleButton(int length, int height) {
        super(length, height);
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

        int previousPosition = position;

        position++;

        if (position == panes.size()) {
            position = 0;
        }

        callOnClick(event);

        //use the previous position, since that will have the pane we clicked on
        Pane pane = panes.get(previousPosition);
        pane.click(gui, inventoryComponent, event, slot, paneOffsetX + x, paneOffsetY + y,
            length, height);

        gui.update();

        return true;
    }

    @Override
    public void display(@NotNull InventoryComponent inventoryComponent, int paneOffsetX, int paneOffsetY, int maxLength,
                        int maxHeight) {
        Slot slot = getSlot();

        int newX = paneOffsetX + slot.getX(maxLength);
        int newY = paneOffsetY + slot.getY(maxLength);

        int newMaxLength = Math.min(maxLength, length);
        int newMaxHeight = Math.min(maxHeight, height);

        panes.get(position).display(inventoryComponent, newX, newY, newMaxLength, newMaxHeight);
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public CycleButton copy() {
        CycleButton cycleButton = new CycleButton(getSlot(), length, height, getPriority());

        for (Pane pane : panes) {
            cycleButton.addPane(pane);
        }

        cycleButton.setVisible(isVisible());
        cycleButton.onClick = onClick;

        cycleButton.position = position;

        cycleButton.uuid = uuid;

        return cycleButton;
    }

    @NotNull
    @Override
    public Collection<GuiItem> getItems() {
        return getPanes().stream().flatMap(pane -> pane.getItems().stream()).collect(Collectors.toList());
    }

    /**
     * Adds a pane to the current list of options
     *
     * @param index the index to insert the pane at
     * @param pane the pane to add
     * @since 0.5.0
     */
    public void addPane(int index, @NotNull Pane pane) {
        panes.add(index, pane);
    }

    /**
     * Adds a pane to the current list of options
     *
     * @param pane the pane to add
     * @since 0.5.0
     */
    public void addPane(@NotNull Pane pane) {
        panes.add(pane);
    }

    @Override
    public void clear() {
        panes.clear();
    }

    @NotNull
    @Override
    public Collection<Pane> getPanes() {
        return panes;
    }

    /**
     * Cycles through one option, making it go to the next one
     *
     * @since 0.5.0
     */
    public void cycle() {
        position++;
    }

    /**
     * Loads a cycle button from a given element
     *
     * @param instance the instance class
     * @param element the element
     * @param plugin the plugin that will be the owner of the underlying items
     * @return the cycle button
     * @since 0.10.8
     */
    @NotNull
    public static CycleButton load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        int length;
        int height;

        try {
            length = Integer.parseInt(element.getAttribute("length"));
            height = Integer.parseInt(element.getAttribute("height"));
        } catch (NumberFormatException exception) {
            throw new XMLLoadException(exception);
        }

        CycleButton cycleButton = new CycleButton(length, height);

        Pane.load(cycleButton, instance, element);

        if (element.hasAttribute("populate")) {
            return cycleButton;
        }

        NodeList childNodes = element.getChildNodes();

        for (int j = 0; j < childNodes.getLength(); j++) {
            Node pane = childNodes.item(j);

            if (pane.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            cycleButton.addPane(Gui.loadPane(instance, pane, plugin));
        }

        return cycleButton;
    }

    /**
     * Loads a cycle button from a given element
     *
     * @param instance the instance class
     * @param element the element
     * @return the cycle button
     * @since 0.5.0
     * @deprecated this method is no longer used internally and has been superseded by
     *             {@link #load(Object, Element, Plugin)}
     */
    @NotNull
    @Deprecated
    public static CycleButton load(@NotNull Object instance, @NotNull Element element) {
        return load(instance, element, JavaPlugin.getProvidingPlugin(CycleButton.class));
    }
}
