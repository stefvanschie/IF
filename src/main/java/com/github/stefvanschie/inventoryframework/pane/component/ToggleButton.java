package com.github.stefvanschie.inventoryframework.pane.component;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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

    /**
     * {@inheritDoc}
     */
    public ToggleButton(int x, int y, int length, int height, @NotNull Priority priority) {
        this(x, y, length, height);

        setPriority(priority);
    }

    /**
     * {@inheritDoc}
     */
    public ToggleButton(int length, int height) {
        super(length, height);

        this.enabledPane = new OutlinePane(0, 0, length, height);
        this.enabledPane.addItem(new GuiItem(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)));
        this.enabledPane.setRepeat(true);

        this.disabledPane = new OutlinePane(0, 0, length, height);
        this.disabledPane.addItem(new GuiItem(new ItemStack(Material.RED_STAINED_GLASS_PANE)));
        this.disabledPane.setRepeat(true);
    }

    /**
     * {@inheritDoc}
     */
    public ToggleButton(int x, int y, int length, int height) {
        this(length, height);

        setX(x);
        setY(y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void display(@NotNull Gui gui, @NotNull Inventory inventory, @NotNull PlayerInventory playerInventory,
                        int paneOffsetX, int paneOffsetY, int maxLength, int maxHeight) {
        int newX = paneOffsetX + x;
        int newY = paneOffsetY + y;

        int newMaxLength = Math.min(maxLength, length);
        int newMaxHeight = Math.min(maxHeight, height);

        if (enabled) {
            enabledPane.display(gui, inventory, playerInventory, newX, newY, newMaxLength, newMaxHeight);
        } else {
            disabledPane.display(gui, inventory, playerInventory, newX, newY, newMaxLength, newMaxHeight);
        }
    }

    /**
     * {@inheritDoc}
     */
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

        //this isn't our item
        if (x < 0 || x >= length || y < 0 || y >= height) {
            return false;
        }

        if (onClick != null)
            onClick.accept(event);

        int newX = paneOffsetX + x;
        int newY = paneOffsetY + y;

        if (enabled) {
            enabledPane.click(gui, event, newX, newY, length, height);
        } else {
            disabledPane.click(gui, event, newX, newY, length, height);
        }

        toggle();

        gui.update();

        return true;
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

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Collection<GuiItem> getItems() {
        return getPanes().stream().flatMap(pane -> pane.getItems().stream()).collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Collection<Pane> getPanes() {
        return Stream.of(enabledPane, disabledPane).collect(Collectors.toSet());
    }

    /**
     * Toggles between the enabled and disabled states
     *
     * @since 0.5.0
     */
    public void toggle() {
        enabled = !enabled;
    }

    /**
     * {@inheritDoc}
     */
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
    @Contract(value = "_, null -> fail", pure = true)
    public static ToggleButton load(@NotNull Object instance, @NotNull Element element) {
        int length, height;

        try {
            length = Integer.parseInt(element.getAttribute("length"));
            height = Integer.parseInt(element.getAttribute("height"));
        } catch (NumberFormatException exception) {
            throw new XMLLoadException(exception);
        }

        ToggleButton toggleButton = new ToggleButton(length, height);

        Pane.load(toggleButton, instance, element);

        if (element.hasAttribute("enabled") && Boolean.parseBoolean(element.getAttribute("enabled"))) {
            toggleButton.toggle();
        }

        return toggleButton;
    }
}
