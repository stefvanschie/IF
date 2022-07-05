package com.github.stefvanschie.inventoryframework.pane.component;

import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A button that toggles between an enabled and disabled state.
 *
 * @since 0.5.0
 */
public class ToggleButton extends Pane {

    /**
     * The panes used for showing the enabled and disabled states
     */
    private final OutlinePane enabledPane, disabledPane;

    /**
     * Whether the button is enabled or disabled
     */
    private boolean enabled = false;

    public ToggleButton(int x, int y, int length, int height, @NotNull Priority priority) {
        this(x, y, length, height);

        setPriority(priority);
    }

    public ToggleButton(int x, int y, int length, int height, @NotNull Priority priority, boolean enabled) {
        this(x, y, length, height, priority);

        this.enabled = enabled;
    }

    public ToggleButton(int length, int height) {
        super(length, height);

        this.enabledPane = new OutlinePane(this.x, this.y, length, height);
        this.enabledPane.addItem(new GuiItem(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)));
        this.enabledPane.setRepeat(true);

        this.disabledPane = new OutlinePane(this.x, this.y, length, height);
        this.disabledPane.addItem(new GuiItem(new ItemStack(Material.RED_STAINED_GLASS_PANE)));
        this.disabledPane.setRepeat(true);
    }

    public ToggleButton(int length, int height, boolean enabled) {
        this(length, height);

        this.enabled = enabled;
    }

    public ToggleButton(int x, int y, int length, int height) {
        this(length, height);

        setX(x);
        setY(y);
    }

    public ToggleButton(int x, int y, int length, int height, boolean enabled) {
        this(x, y, length, height);

        this.enabled = enabled;
    }

    @Override
    public void display(@NotNull InventoryComponent inventoryComponent, int paneOffsetX, int paneOffsetY, int maxLength,
                        int maxHeight) {
        int newMaxLength = Math.min(maxLength, length);
        int newMaxHeight = Math.min(maxHeight, height);

        if (enabled) {
            this.enabledPane.display(inventoryComponent, paneOffsetX, paneOffsetY, newMaxLength, newMaxHeight);
        } else {
            this.disabledPane.display(inventoryComponent, paneOffsetX, paneOffsetY, newMaxLength, newMaxHeight);
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

        toggle();

        callOnClick(event);

        int newX = paneOffsetX + x;
        int newY = paneOffsetY + y;

        //swap panes, since we will already have toggled the enabled state here
        if (enabled) {
            disabledPane.click(gui, inventoryComponent, event, slot, newX, newY, length, height);
        } else {
            enabledPane.click(gui, inventoryComponent, event, slot, newX, newY, length, height);
        }

        gui.update();

        return true;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public ToggleButton copy() {
        ToggleButton toggleButton = new ToggleButton(x, y, length, height, getPriority(), enabled);

        toggleButton.setVisible(isVisible());
        toggleButton.onClick = onClick;

        toggleButton.uuid = uuid;

        toggleButton.setEnabledItem(enabledPane.getItems().get(0).copy());
        toggleButton.setDisabledItem(disabledPane.getItems().get(0).copy());

        return toggleButton;
    }

    @Override
    public void setLength(int length) {
        super.setLength(length);

        this.disabledPane.setLength(length);
        this.enabledPane.setLength(length);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);

        this.disabledPane.setHeight(height);
        this.enabledPane.setHeight(height);
    }

    @Override
    public void setX(int x) {
        super.setX(x);

        this.disabledPane.setX(x);
        this.enabledPane.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        this.disabledPane.setY(y);
        this.enabledPane.setY(y);
    }

    /**
     * Sets the item to use when the button is set to disabled
     *
     * @param item the disabled item
     * @since 0.5.0
     */
    public void setDisabledItem(@NotNull GuiItem item) {
        disabledPane.clear();

        disabledPane.addItem(item);
    }

    /**
     * Sets the item to use when the button is set to enabled
     *
     * @param item the enabled item
     * @since 0.5.0
     */
    public void setEnabledItem(@NotNull GuiItem item) {
        enabledPane.clear();

        enabledPane.addItem(item);
    }

    @NotNull
    @Override
    public Collection<GuiItem> getItems() {
        return getPanes().stream().flatMap(pane -> pane.getItems().stream()).collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public Collection<Pane> getPanes() {
        return Stream.of(enabledPane, disabledPane).collect(Collectors.toSet());
    }

    /**
     * Gets whether this toggle button is currently enabled or disabled.
     *
     * @return whether the button is enabled or disabled
     * @since 0.9.6
     */
    @Contract(pure = true)
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Toggles between the enabled and disabled states
     *
     * @since 0.5.0
     */
    public void toggle() {
        enabled = !enabled;
    }

    @Override
    public void clear() {}

    /**
     * Loads a toggle button from an XML element
     *
     * @param instance the instance class
     * @param element the element
     * @return the toggle button
     * @since 0.5.0
     */
    @NotNull
    @Contract(pure = true)
    public static ToggleButton load(@NotNull Object instance, @NotNull Element element) {
        int length, height;

        try {
            length = Integer.parseInt(element.getAttribute("length"));
            height = Integer.parseInt(element.getAttribute("height"));
        } catch (NumberFormatException exception) {
            throw new XMLLoadException(exception);
        }

        boolean enabled = element.hasAttribute("enabled") && Boolean.parseBoolean(element.getAttribute("enabled"));
        ToggleButton toggleButton = new ToggleButton(length, height, enabled);

        Pane.load(toggleButton, instance, element);

        return toggleButton;
    }
}
