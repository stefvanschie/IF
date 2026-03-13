package com.github.stefvanschie.inventoryframework.pane.component;

import com.github.stefvanschie.inventoryframework.gui.GuiComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.pane.Flippable;
import com.github.stefvanschie.inventoryframework.pane.Orientable;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.component.util.VariableBar;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

/**
 * A percentage bar for a graphical interface into what amount of a whole is set.
 *
 * @since 0.5.0
 */
public class PercentageBar extends VariableBar {

    /**
     * Creates a new percentage bar
     *
     * @param length the length of the bar
     * @param height the height of the bar
     * @param priority the priority of the bar
     * @param plugin the plugin that will be the owner for this percentage bar's items
     * @since 0.12.0
     */
    public PercentageBar(int length, int height, @NotNull Priority priority, @NotNull Plugin plugin) {
        super(length, height, priority, plugin);
    }

    /**
     * Creates a new percentage bar
     *
     * @param length the length of the bar
     * @param height the height of the bar
     * @param plugin the plugin that will be the owner for this percentage bar's items
     * @since 0.12.0
     */
    public PercentageBar(int length, int height, @NotNull Plugin plugin) {
        super(length, height, plugin);
    }

    /**
     * Creates a new percentage bar
     *
     * @param length the length of the bar
     * @param height the height of the bar
     * @param priority the priority of the bar
     * @since 0.12.0
     */
    public PercentageBar(int length, int height, @NotNull Priority priority) {
        super(length, height, priority);
    }

    /**
     * Creates a new percentage bar
     *
     * @param length the length of the bar
     * @param height the height of the bar
     * @since 0.12.0
     */
    public PercentageBar(int length, int height) {
        super(length, height);
    }

    @Override
    public boolean click(@NotNull Gui gui, @NotNull GuiComponent guiComponent, @NotNull InventoryClickEvent event,
                         @NotNull Slot slot) {
        int x = slot.getX(getLength());
        int y = slot.getY(getLength());

        if (x < 0 || x >= getLength() || y < 0 || y >= getHeight()) {
            return false;
        }

        callOnClick(event);

        event.setCancelled(true);

        if (this.fillPane.click(gui, guiComponent, event, slot)) {
            return true;
        }

        return this.backgroundPane.click(gui, guiComponent, event, slot);
    }

    /**
     * Sets the percentage of this bar. The percentage has to be in (0,1). If not, this method will throw an
     * {@link IllegalArgumentException}.
     *
     * @param percentage the new percentage.
     * @throws IllegalArgumentException when the percentage is out of range
     * @since 0.5.0
     * @see VariableBar#setValue(float) the implementation
     */
    public void setPercentage(float percentage) {
        super.setValue(percentage);
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public PercentageBar copy() {
        PercentageBar percentageBar = new PercentageBar(getLength(), getHeight(), getPriority());

        applyContents(percentageBar);

        return percentageBar;
    }

    /**
     * Gets the percentage as a float in between (0,1) this bar is currently set at.
     *
     * @return the percentage
     * @since 0.5.0
     */
    public float getPercentage() {
        return value;
    }

    /**
     * Loads a percentage bar from a given element
     *
     * @param instance the instance class
     * @param element the element
     * @param plugin the plugin that will be the owner of the underlying items
     * @return the percentage bar
     * @since 0.10.8
     */
    @NotNull
    @Contract(pure = true)
    public static PercentageBar load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        if (!element.hasAttribute("length")) {
            throw new XMLLoadException("Percentage bar XML tag does not have the mandatory length attribute");
        }

        if (!element.hasAttribute("height")) {
            throw new XMLLoadException("Percentage bar XML tag does not have the mandatory height attribute");
        }

        int length;
        int height;

        try {
            length = Integer.parseInt(element.getAttribute("length"));
        } catch (NumberFormatException exception) {
            throw new XMLLoadException("Length attribute is not an integer", exception);
        }

        try {
            height = Integer.parseInt(element.getAttribute("height"));
        } catch (NumberFormatException exception) {
            throw new XMLLoadException("Height attribute is not an integer", exception);
        }

        PercentageBar percentageBar = new PercentageBar(length, height, plugin);

        Pane.load(percentageBar, instance, element);
        Orientable.load(percentageBar, element);
        Flippable.load(percentageBar, element);

        if (element.hasAttribute("populate")) {
            return percentageBar;
        }

        if (element.hasAttribute("percentage")) {
            try {
                percentageBar.setPercentage(Float.parseFloat(element.getAttribute("percentage")));
            } catch (IllegalArgumentException exception) {
                throw new XMLLoadException("Percentage attribute is not a float", exception);
            }
        }

        return percentageBar;
    }
}
