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
 * A slider for a graphical interface into what amount of a whole is set.
 *
 * @since 0.5.0
 */
public class Slider extends VariableBar {

    /**
     * Creates a new slider
     *
     * @param length the length of the slider
     * @param height the height of the slider
     * @param priority the priority of the slider
     * @param plugin the plugin that will be the owner of the slider's items
     * @since 0.12.0
     */
    public Slider(int length, int height, @NotNull Priority priority, @NotNull Plugin plugin) {
        super(length, height, priority, plugin);
    }

    /**
     * Creates a new slider
     *
     * @param length the length of the slider
     * @param height the height of the slider
     * @param plugin the plugin that will be the owner of the slider's items
     * @since 0.12.0
     */
    public Slider(int length, int height, @NotNull Plugin plugin) {
        super(length, height, plugin);
    }

    /**
     * Creates a new slider
     *
     * @param length the length of the slider
     * @param height the height of the slider
     * @param priority the priority of the slider
     * @since 0.12.0
     */
    public Slider(int length, int height, @NotNull Priority priority) {
        super(length, height, priority);
    }

    /**
     * Creates a new slider
     *
     * @param length the length of the slider
     * @param height the height of the slider
     * @since 0.12.0
     */
    public Slider(int length, int height) {
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

        if (orientation == Orientation.HORIZONTAL) {
            setValue((float) (x + 1) / length);
        } else if (orientation == Orientation.VERTICAL) {
            setValue((float) (y + 1) / height);
        } else {
            throw new UnsupportedOperationException("Unknown orientation");
        }

        callOnClick(event);

        boolean success = this.fillPane.click(gui, guiComponent, event, slot);

        if (!success) {
            success = this.backgroundPane.click(gui, guiComponent, event, slot);
        }

        gui.update();

        return success;
    }

    /**
     * Sets the value of this bar. The value has to be in (0,1). If not, this method will throw an
     * {@link IllegalArgumentException}.
     *
     * @param value the new value.
     * @throws IllegalArgumentException when the value is out of range
     * @since 0.5.0
     * @see VariableBar#setValue(float) the implementation
     */
    public void setValue(float value) {
        super.setValue(value);
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public Slider copy() {
        Slider slider = new Slider(getLength(), getHeight(), getPriority());

        applyContents(slider);

        return slider;
    }

    /**
     * Gets the value as a float in between (0,1) this bar is currently set at.
     *
     * @return the value
     * @since 0.5.0
     */
    public float getValue() {
        return value;
    }

    /**
     * Loads a percentage bar from a given element
     *
     * @param instance the instance class
     * @param element the element
     * @param plugin the plugin that will be the owner of the udnerlying items
     * @return the percentage bar
     * @since 0.10.8
     */
    @NotNull
    @Contract(pure = true)
    public static Slider load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        if (!element.hasAttribute("length")) {
            throw new XMLLoadException("Slider XML tag does not have the mandatory length attribute");
        }

        if (!element.hasAttribute("height")) {
            throw new XMLLoadException("Slider XML tag does not have the mandatory height attribute");
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

        Slider slider = new Slider(length, height, plugin);

        Pane.load(slider, instance, element);
        Orientable.load(slider, element);
        Flippable.load(slider, element);

        if (element.hasAttribute("populate")) {
            return slider;
        }

        if (element.hasAttribute("value")) {
            try {
                slider.setValue(Float.parseFloat(element.getAttribute("value")));
            } catch (IllegalArgumentException exception) {
                throw new XMLLoadException("Value attribute is not a float", exception);
            }
        }

        return slider;
    }
}
