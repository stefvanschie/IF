package com.github.stefvanschie.inventoryframework.pane.component;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.pane.Flippable;
import com.github.stefvanschie.inventoryframework.pane.Orientable;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.component.util.VariableBar;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

/**
 * A slider for a graphical interface into what amount of a whole is set.
 *
 * @since 0.5.0
 */
public class Slider extends VariableBar {

    public Slider(int x, int y, int length, int height, @NotNull Priority priority) {
        super(x, y, length, height, priority);
    }

    public Slider(int x, int y, int length, int height) {
        super(x, y, length, height);
    }

    public Slider(int length, int height) {
        super(length, height);
    }

    @Override
    public boolean click(@NotNull Gui gui, @NotNull InventoryClickEvent event, int paneOffsetX, int paneOffsetY,
                         int maxLength, int maxHeight) {
        int length = Math.min(this.length, maxLength);
        int height = Math.min(this.height, maxHeight);

        int slot = event.getSlot();

        int x, y;

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

        if (x < 0 || x >= length || y < 0 || y >= height) {
            return false;
        }

        if (onClick != null) {
            onClick.accept(event);
        }

        int newPaneOffsetX = paneOffsetX + getX();
        int newPaneOffsetY = paneOffsetY + getY();

        boolean success = this.fillPane.click(gui, event, newPaneOffsetX, newPaneOffsetY, length, height) ||
            this.backgroundPane.click(gui, event, newPaneOffsetX, newPaneOffsetY, length, height);

        if (orientation == Orientation.HORIZONTAL) {
            setValue((float) (x + 1) / length);
        } else if (orientation == Orientation.VERTICAL) {
            setValue((float) (y + 1) / height);
        } else {
            throw new UnsupportedOperationException("Unknown orientation");
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
     */
    public void setValue(float value) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("Value is out of range (0,1)");
        }

        this.value = value;

        if (orientation == Orientation.HORIZONTAL) {
            this.fillPane.setLength(Math.round(getLength() * value));

            if (flipHorizontally) {
                this.fillPane.setX(getLength() - this.fillPane.getLength());
            }
        } else if (orientation == Orientation.VERTICAL) {
            this.fillPane.setHeight(Math.round(getHeight() * value));

            if (flipVertically) {
                this.fillPane.setY(getHeight() - this.fillPane.getHeight());
            }
        } else {
            throw new UnsupportedOperationException("Unknown orientation");
        }
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
     * @param element  the element
     * @return the percentage bar
     */
    @NotNull
    @Contract(pure = true)
    public static Slider load(@NotNull Object instance, @NotNull Element element) {
        int length;
        int height;

        try {
            length = Integer.parseInt(element.getAttribute("length"));
            height = Integer.parseInt(element.getAttribute("height"));
        } catch (NumberFormatException exception) {
            throw new XMLLoadException(exception);
        }

        Slider slider = new Slider(length, height);

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
                throw new XMLLoadException(exception);
            }
        }

        return slider;
    }
}
