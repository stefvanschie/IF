package com.forcemc.inventories.pane.component;

import com.forcemc.inventories.gui.InventoryComponent;
import com.forcemc.inventories.gui.type.util.Gui;
import com.forcemc.inventories.exception.XMLLoadException;
import com.forcemc.inventories.pane.Flippable;
import com.forcemc.inventories.pane.Orientable;
import com.forcemc.inventories.pane.Pane;
import com.forcemc.inventories.pane.component.util.VariableBar;
import com.forcemc.inventories.pane.util.Slot;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
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
     * @param slot the slot of the slider
     * @param length the length of the slider
     * @param height the height of the slider
     * @param priority the priority of the slider
     * @param plugin the plugin that will be the owner of the slider's items
     * @since 0.10.8
     */
    public Slider(@NotNull Slot slot, int length, int height, @NotNull Priority priority, @NotNull Plugin plugin) {
        super(slot, length, height, priority, plugin);
    }

    /**
     * Creates a new slider
     *
     * @param x the x coordinate of the slider
     * @param y the y coordinate of the slier
     * @param length the length of the slider
     * @param height the height of the slider
     * @param priority the priority of the slider
     * @param plugin the plugin that will be the owner of the slider's items
     * @since 0.10.8
     */
    public Slider(int x, int y, int length, int height, @NotNull Priority priority, @NotNull Plugin plugin) {
        super(x, y, length, height, priority, plugin);
    }

    /**
     * Creates a new slider
     *
     * @param slot the slot of the slider
     * @param length the length of the slider
     * @param height the height of the slider
     * @param plugin the plugin that will be the owner of the slider's items
     * @since 0.10.8
     */
    public Slider(@NotNull Slot slot, int length, int height, @NotNull Plugin plugin) {
        super(slot, length, height, plugin);
    }

    /**
     * Creates a new slider
     *
     * @param x the x coordinate of the slider
     * @param y the y coordinate of the slier
     * @param length the length of the slider
     * @param height the height of the slider
     * @param plugin the plugin that will be the owner of the slider's items
     * @since 0.10.8
     */
    public Slider(int x, int y, int length, int height, @NotNull Plugin plugin) {
        super(x, y, length, height, plugin);
    }

    /**
     * Creates a new slider
     *
     * @param length the length of the slider
     * @param height the height of the slider
     * @param plugin the plugin that will be the owner of the slider's items
     * @since 0.10.8
     */
    public Slider(int length, int height, @NotNull Plugin plugin) {
        super(length, height, plugin);
    }

    /**
     * Creates a new slider
     *
     * @param slot the slot of the slider
     * @param length the length of the slider
     * @param height the height of the slider
     * @param priority the priority of the slider
     * @since 0.10.8
     */
    public Slider(@NotNull Slot slot, int length, int height, @NotNull Priority priority) {
        super(slot, length, height, priority);
    }

    public Slider(int x, int y, int length, int height, @NotNull Priority priority) {
        super(x, y, length, height, priority);
    }

    /**
     * Creates a new slider
     *
     * @param slot the slot of the slider
     * @param length the length of the slider
     * @param height the height of the slider
     * @since 0.10.8
     */
    public Slider(@NotNull Slot slot, int length, int height) {
        super(slot, length, height);
    }

    public Slider(int x, int y, int length, int height) {
        super(x, y, length, height);
    }

    public Slider(int length, int height) {
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

        if (x < 0 || x >= length || y < 0 || y >= height) {
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

        int newPaneOffsetX = paneOffsetX + xPosition;
        int newPaneOffsetY = paneOffsetY + yPosition;

        boolean success = this.fillPane.click(
            gui, inventoryComponent, event, slot, newPaneOffsetX, newPaneOffsetY, length, height
        ) || this.backgroundPane.click(
            gui, inventoryComponent, event, slot, newPaneOffsetX, newPaneOffsetY, length, height
        );

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
        Slider slider = new Slider(getSlot(), length, height, getPriority());

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
        int length;
        int height;

        try {
            length = Integer.parseInt(element.getAttribute("length"));
            height = Integer.parseInt(element.getAttribute("height"));
        } catch (NumberFormatException exception) {
            throw new XMLLoadException(exception);
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
                throw new XMLLoadException(exception);
            }
        }

        return slider;
    }

    /**
     * Loads a percentage bar from a given element
     *
     * @param instance the instance class
     * @param element the element
     * @return the percentage bar
     * @deprecated this method is no longer used internally and has been superseded by
     *             {@link #load(Object, Element, Plugin)}
     */
    @NotNull
    @Contract(pure = true)
    @Deprecated
    public static Slider load(@NotNull Object instance, @NotNull Element element) {
        return load(instance, element, JavaPlugin.getProvidingPlugin(Slider.class));
    }
}
