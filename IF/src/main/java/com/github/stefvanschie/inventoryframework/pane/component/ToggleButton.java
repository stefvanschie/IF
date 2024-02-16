package com.github.stefvanschie.inventoryframework.pane.component;

import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
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
    private boolean enabled;

    /**
     * Whether this button can be toggled by a player
     */
    private boolean allowToggle = true;

    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param enabled whether the button should start in its enabled or disabled state
     * @param plugin the plugin that will be the owner of this button's items
     * @see #ToggleButton(int, int, int, int, Priority, boolean)
     * @since 0.10.8
     */
    public ToggleButton(@NotNull Slot slot, int length, int height, @NotNull Priority priority, boolean enabled,
                        @NotNull Plugin plugin) {
        super(slot, length, height, priority);

        this.enabled = enabled;

        this.enabledPane = new OutlinePane(length, height);
        this.enabledPane.addItem(new GuiItem(new ItemStack(Material.GREEN_STAINED_GLASS_PANE), plugin));
        this.enabledPane.setRepeat(true);

        this.disabledPane = new OutlinePane(length, height);
        this.disabledPane.addItem(new GuiItem(new ItemStack(Material.RED_STAINED_GLASS_PANE), plugin));
        this.disabledPane.setRepeat(true);
    }

    /**
     * Creates a new toggle button
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param enabled whether the button should start in its enabled or disabled state
     * @param plugin the plugin that will be the owner of this button's items
     * @see #ToggleButton(int, int, int, int, Priority, boolean)
     * @since 0.10.8
     */
    public ToggleButton(int x, int y, int length, int height, @NotNull Priority priority, boolean enabled,
                        @NotNull Plugin plugin) {
        this(Slot.fromXY(x, y), length, height, priority, enabled, plugin);
    }

    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param plugin the plugin that will be the owner of this button's items
     * @see #ToggleButton(Slot, int, int, Priority)
     * @since 0.10.8
     */
    public ToggleButton(@NotNull Slot slot, int length, int height, @NotNull Priority priority,
                        @NotNull Plugin plugin) {
        this(slot, length, height, priority, false, plugin);
    }

    /**
     * Creates a new toggle button
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param plugin the plugin that will be the owner of this button's items
     * @see #ToggleButton(int, int, int, int, Priority)
     * @since 0.10.8
     */
    public ToggleButton(int x, int y, int length, int height, @NotNull Priority priority, @NotNull Plugin plugin) {
        this(x, y, length, height, priority, false, plugin);
    }

    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param enabled whether the button should start in its enabled or disabled state
     * @param plugin the plugin that will be the owner of this button's items
     * @see #ToggleButton(Slot, int, int, boolean)
     * @since 0.10.8
     */
    public ToggleButton(@NotNull Slot slot, int length, int height, boolean enabled, @NotNull Plugin plugin) {
        this(slot, length, height, Priority.NORMAL, enabled, plugin);
    }

    /**
     * Creates a new toggle button
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the height
     * @param enabled whether the button should start in its enabled or disabled state
     * @param plugin the plugin that will be the owner of this button's items
     * @see #ToggleButton(int, int, int, int, boolean)
     * @since 0.10.8
     */
    public ToggleButton(int x, int y, int length, int height, boolean enabled, @NotNull Plugin plugin) {
        this(x, y, length, height, Priority.NORMAL, enabled, plugin);
    }

    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param plugin the plugin that will be the owner of this button's items
     * @see #ToggleButton(Slot, int, int)
     * @since 0.10.8
     */
    public ToggleButton(@NotNull Slot slot, int length, int height, @NotNull Plugin plugin) {
        this(slot, length, height, false, plugin);
    }

    /**
     * Creates a new toggle button
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the height
     * @param plugin the plugin that will be the owner of this button's items
     * @see #ToggleButton(int, int, int, int)
     * @since 0.10.8
     */
    public ToggleButton(int x, int y, int length, int height, @NotNull Plugin plugin) {
        this(x, y, length, height, false, plugin);
    }

    /**
     * Creates a new toggle button
     *
     * @param length the length
     * @param height the height
     * @param enabled whether the button should start in its enabled or disabled state
     * @param plugin the plugin that will be the owner of this button's items
     * @see #ToggleButton(int, int, boolean)
     * @since 0.10.8
     */
    public ToggleButton(int length, int height, boolean enabled, @NotNull Plugin plugin) {
        this(0, 0, length, height, enabled);
    }

    /**
     * Creates a new toggle button
     *
     * @param length the length
     * @param height the height
     * @param plugin the plugin that will be the owner of this button's items
     * @see #ToggleButton(int, int)
     * @since 0.10.8
     */
    public ToggleButton(int length, int height, @NotNull Plugin plugin) {
        this(length, height, false);
    }

    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param enabled whether the button should start in its enabled or disabled state
     * @since 0.10.8
     */
    public ToggleButton(@NotNull Slot slot, int length, int height, @NotNull Priority priority, boolean enabled) {
        this(slot, length, height, priority, enabled, JavaPlugin.getProvidingPlugin(ToggleButton.class));
    }

    public ToggleButton(int x, int y, int length, int height, @NotNull Priority priority, boolean enabled) {
        this(x, y, length, height, priority, enabled, JavaPlugin.getProvidingPlugin(ToggleButton.class));
    }

    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @since 0.10.8
     */
    public ToggleButton(@NotNull Slot slot, int length, int height, @NotNull Priority priority) {
        this(slot, length, height, priority, false);
    }

    public ToggleButton(int x, int y, int length, int height, @NotNull Priority priority) {
        this(x, y, length, height, priority, false);
    }

    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param enabled whether the button should start in its enabled or disabled state
     * @since 0.10.8
     */
    public ToggleButton(@NotNull Slot slot, int length, int height, boolean enabled) {
        this(slot, length, height, Priority.NORMAL, enabled);
    }

    public ToggleButton(int x, int y, int length, int height, boolean enabled) {
        this(x, y, length, height, Priority.NORMAL, enabled);
    }

    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @since 0.10.8
     */
    public ToggleButton(@NotNull Slot slot, int length, int height) {
        this(slot, length, height, false);
    }

    public ToggleButton(int x, int y, int length, int height) {
        this(x, y, length, height, false);
    }

    public ToggleButton(int length, int height, boolean enabled) {
        this(0, 0, length, height, enabled);
    }

    public ToggleButton(int length, int height) {
        this(length, height, false);
    }

    @Override
    public void display(@NotNull InventoryComponent inventoryComponent, int paneOffsetX, int paneOffsetY, int maxLength,
                        int maxHeight) {
        int newMaxLength = Math.min(maxLength, length);
        int newMaxHeight = Math.min(maxHeight, height);

        int newPaneOffsetX = this.slot.getX(newMaxLength) + paneOffsetX;
        int newPaneOffsetY = this.slot.getY(newMaxHeight) + paneOffsetY;

        if (enabled) {
            this.enabledPane.display(inventoryComponent, newPaneOffsetX, newPaneOffsetY, newMaxLength, newMaxHeight);
        } else {
            this.disabledPane.display(inventoryComponent, newPaneOffsetX, newPaneOffsetY, newMaxLength, newMaxHeight);
        }
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

        if (this.allowToggle) {
            toggle();
        }

        callOnClick(event);

        int newX = paneOffsetX + xPosition;
        int newY = paneOffsetY + yPosition;

        /*
        Since we've toggled before, the click for the panes should be swapped around. If we haven't toggled due to
        allowToggle being false, then we should click the pane corresponding to the current state. An XOR achieves this.
         */
        if (enabled == this.allowToggle) {
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
        ToggleButton toggleButton = new ToggleButton(getSlot(), length, height, getPriority(), enabled);

        toggleButton.allowToggle = this.allowToggle;

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
     * Sets whether this toggle button can be toggled. This only prevents players from toggling the button and does not
     * prevent toggling the button programmatically with methods such as {@link #toggle()}.
     *
     * @param allowToggle whether this button can be toggled
     * @since 0.10.8
     */
    public void allowToggle(boolean allowToggle) {
        this.allowToggle = allowToggle;
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
     * @param plugin the plugin that will be the owner of the underlying items
     * @return the toggle button
     * @since 0.10.8
     */
    @NotNull
    @Contract(pure = true)
    public static ToggleButton load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        int length, height;

        try {
            length = Integer.parseInt(element.getAttribute("length"));
            height = Integer.parseInt(element.getAttribute("height"));
        } catch (NumberFormatException exception) {
            throw new XMLLoadException(exception);
        }

        boolean enabled = element.hasAttribute("enabled") && Boolean.parseBoolean(element.getAttribute("enabled"));
        ToggleButton toggleButton = new ToggleButton(length, height, enabled, plugin);

        Pane.load(toggleButton, instance, element);

        return toggleButton;
    }

    /**
     * Loads a toggle button from an XML element
     *
     * @param instance the instance class
     * @param element the element
     * @return the toggle button
     * @since 0.5.0
     * @deprecated this method is no longer used internally and has been superseded by
     *             {@link #load(Object, Element, Plugin)}
     */
    @NotNull
    @Contract(pure = true)
    @Deprecated
    public static ToggleButton load(@NotNull Object instance, @NotNull Element element) {
        return load(instance, element, JavaPlugin.getProvidingPlugin(ToggleButton.class));
    }
}
