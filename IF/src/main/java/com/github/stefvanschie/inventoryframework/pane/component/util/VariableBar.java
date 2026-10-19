package com.github.stefvanschie.inventoryframework.pane.component.util;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.Flippable;
import com.github.stefvanschie.inventoryframework.pane.Orientable;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.util.GuiItemContainer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.github.stefvanschie.inventoryframework.gui.GuiClickEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A variable bar for UI elements that require some sort of bar
 *
 * @since 0.5.0
 */
public abstract class VariableBar extends Pane implements Orientable, Flippable  {

    /**
     * The green and the red parts of the slider
     */
    @NotNull
    protected final OutlinePane fillPane, backgroundPane;

    /**
     * The value this slider is at. This is a value between 0 and 1 (both inclusive).
     */
    protected float value;

    /**
     * The orientation of the slider
     */
    @NotNull
    protected Orientation orientation;

    /**
     * Whether the pane is flipped horizontally or vertically
     */
    protected boolean flipHorizontally, flipVertically;

    /**
     * Creates a new variable bar
     *
     * @param length the length of the bar
     * @param height the height of the bar
     * @param plugin the plugin that will be the owner for this variable bar's items
     * @see #VariableBar(int, int)
     * @since 0.10.8
     */
    protected VariableBar(int length, int height, @NotNull Plugin plugin) {
        this(length, height, Priority.NORMAL, plugin);
    }

    /**
     * Creates a new variable bar
     *
     * @param length the length of the bar
     * @param height the height of the bar
     * @param priority the priority of the bar
     * @param plugin the plugin that will be the owner for this variable bar's items
     * @since 0.12.0
     */
    protected VariableBar(int length, int height, @NotNull Priority priority, @NotNull Plugin plugin) {
        super(length, height);

        this.value = 0F;
        this.orientation = Orientation.HORIZONTAL;

        this.fillPane = new OutlinePane(length, height);
        this.backgroundPane = new OutlinePane(length, height);

        this.fillPane.addItem(new GuiItem(new ItemStack(Material.GREEN_STAINED_GLASS_PANE),
                event -> event.getClickEvent().setCancelled(true), plugin));
        this.backgroundPane.addItem(new GuiItem(new ItemStack(Material.RED_STAINED_GLASS_PANE),
                event -> event.getClickEvent().setCancelled(true), plugin));

        this.fillPane.setRepeat(true);
        this.backgroundPane.setRepeat(true);

        this.fillPane.setVisible(false);

        setPriority(priority);
    }

    protected VariableBar(int length, int height) {
        this(length, height, JavaPlugin.getProvidingPlugin(VariableBar.class));
    }

    /**
     * Creates a new variable bar
     *
     * @param length the length of the bar
     * @param height the height of the bar
     * @param priority the priority of the bar
     * @since 0.12.0
     */
    protected VariableBar(int length, int height, @NotNull Priority priority) {
        this(length, height, priority, JavaPlugin.getProvidingPlugin(VariableBar.class));
    }

    /**
     * Sets the value of this bar. The value has to be in (0,1). If not, this method will throw an
     * {@link IllegalArgumentException}.
     *
     * @param value the new value.
     * @throws IllegalArgumentException when the value is out of range
     * @since 0.9.5
     */
    protected void setValue(float value) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("Value is out of range (0,1)");
        }

        this.value = value;

        if (orientation == Orientation.HORIZONTAL) {
            int length = Math.round(getLength() * value);
            boolean positiveLength = length != 0;

            this.fillPane.setVisible(positiveLength);

            if (positiveLength) {
                this.fillPane.setLength(length);
            }
        } else if (orientation == Orientation.VERTICAL) {
            int height = Math.round(getHeight() * value);
            boolean positiveHeight = height != 0;

            this.fillPane.setVisible(positiveHeight);

            if (positiveHeight) {
                this.fillPane.setHeight(height);
            }
        } else {
            throw new UnsupportedOperationException("Unknown orientation");
        }
    }

    @Override
    public void setLength(int length) {
        super.setLength(length);

        boolean isPositive = length != 0;

        this.fillPane.setVisible(isPositive);

        if (isPositive) {
            this.fillPane.setLength(length);
        }

        this.backgroundPane.setLength(length);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);

        boolean isPositive = height != 0;

        this.fillPane.setVisible(isPositive);

        if (isPositive) {
            this.fillPane.setHeight(height);
        }

        this.backgroundPane.setHeight(height);
    }

    /**
     * Applies the contents of this variable bar onto the provided copy of this variable bar. This variable bar will not
     * be modified.
     *
     * @param copy the copy of the variable bar
     * @since 0.6.2
     */
    protected void applyContents(@NotNull VariableBar copy) {
        copy.length = length;
        copy.height = height;
        copy.setPriority(getPriority());

        copy.setVisible(isVisible());
        copy.onClick = onClick;

        copy.setFillItem(fillPane.getItems().get(0).copy());
        copy.setBackgroundItem(backgroundPane.getItems().get(0).copy());

        copy.value = value;
        copy.orientation = orientation;

        copy.flipHorizontally = flipHorizontally;
        copy.flipVertically = flipVertically;

        copy.uuid = uuid;
    }

    @Override
    public void setOrientation(@NotNull Orientation orientation) {
        this.orientation = orientation;

        this.fillPane.setOrientation(orientation);
        this.backgroundPane.setOrientation(orientation);
    }

    @NotNull
    @Override
    public GuiItemContainer display() {
        GuiItemContainer container = new GuiItemContainer(getLength(), getHeight());

        if (this.backgroundPane.isVisible()) {
            container.apply(this.backgroundPane.display(), 0, 0);
        }

        if (this.fillPane.isVisible()) {
            container.apply(this.fillPane.display(), 0, 0);
        }

        return container;
    }

    /**
     * Sets the fill item (foreground)
     *
     * @param item the new item
     * @since 0.5.0
     */
    public void setFillItem(@NotNull GuiItem item) {
        fillPane.clear();

        fillPane.addItem(item);
    }

    /**
     * Sets the background item
     *
     * @param item the new item
     * @since 0.5.0
     */
    public void setBackgroundItem(@NotNull GuiItem item) {
        backgroundPane.clear();

        backgroundPane.addItem(item);
    }

    @NotNull
    @Override
    public Collection<GuiItem> getItems() {
        return getPanes().stream().flatMap(pane -> pane.getItems().stream()).collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public Collection<Pane> getPanes() {
        return Stream.of(this.fillPane, this.backgroundPane).collect(Collectors.toSet());
    }

    @Override
    public void flipHorizontally(boolean flipHorizontally) {
        this.flipHorizontally = flipHorizontally;

        this.fillPane.flipHorizontally(flipHorizontally);
        this.backgroundPane.flipHorizontally(flipHorizontally);
    }

    @Override
    public void flipVertically(boolean flipVertically) {
        this.flipVertically = flipVertically;

        this.fillPane.flipVertically(flipVertically);
        this.backgroundPane.flipVertically(flipVertically);
    }

    @NotNull
    @Override
    public Orientation getOrientation() {
        return orientation;
    }

    @Override
    public boolean isFlippedHorizontally() {
        return flipHorizontally;
    }

    @Override
    public boolean isFlippedVertically() {
        return flipVertically;
    }

    @Override
    public void clear() {}
}
