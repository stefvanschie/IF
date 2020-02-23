package com.github.stefvanschie.inventoryframework.pane.component.util;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.Flippable;
import com.github.stefvanschie.inventoryframework.pane.Orientable;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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

    protected VariableBar(int length, int height) {
        super(length, height);

        this.value = 0F;
        this.orientation = Orientation.HORIZONTAL;

        this.fillPane = new OutlinePane(x, y, 0, height);
        this.backgroundPane = new OutlinePane(x, y, length, height);

        this.fillPane.addItem(new GuiItem(new ItemStack(Material.GREEN_STAINED_GLASS_PANE),
            event -> event.setCancelled(true)));
        this.backgroundPane.addItem(new GuiItem(new ItemStack(Material.RED_STAINED_GLASS_PANE),
            event -> event.setCancelled(true)));

        this.fillPane.setRepeat(true);
        this.backgroundPane.setRepeat(true);
    }

    protected VariableBar(int x, int y, int length, int height, @NotNull Priority priority) {
        this(length, height);

        setX(x);
        setY(y);

        setPriority(priority);
    }

    protected VariableBar(int x, int y, int length, int height) {
        this(x, y, length, height, Priority.NORMAL);
    }

    @Override
    public void setLength(int length) {
        super.setLength(length);

        if (orientation == Orientation.HORIZONTAL) {
            this.fillPane.setLength(Math.round(length * value));

            if (flipHorizontally) {
                this.fillPane.setX(getLength() - this.fillPane.getLength());
            }
        } else if (orientation == Orientation.VERTICAL) {
            this.fillPane.setLength(length);
        } else {
            throw new UnsupportedOperationException("Unknown orientation");
        }

        this.backgroundPane.setLength(length);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);

        if (orientation == Orientation.HORIZONTAL) {
            this.fillPane.setHeight(height);
        } else if (orientation == Orientation.VERTICAL) {
            this.fillPane.setHeight(Math.round(height * value));

            if (flipVertically) {
                this.fillPane.setY(getHeight() - this.fillPane.getHeight());
            }
        } else {
            throw new UnsupportedOperationException("Unknown orientation");
        }

        this.backgroundPane.setHeight(height);
    }

    @Override
    public void setOrientation(@NotNull Orientation orientation) {
        this.orientation = orientation;

        if (orientation == Orientation.HORIZONTAL) {
            fillPane.setLength(Math.round(getLength() * value));
            fillPane.setHeight(getHeight());
        } else if (orientation == Orientation.VERTICAL) {
            fillPane.setLength(getLength());
            fillPane.setHeight(Math.round(getHeight() * value));
        } else {
            throw new IllegalArgumentException("Unknown orientation");
        }
    }

    @Override
    public void display(@NotNull Gui gui, @NotNull Inventory inventory, @NotNull PlayerInventory playerInventory,
                        int paneOffsetX, int paneOffsetY, int maxLength, int maxHeight) {
        int newPaneOffsetX = paneOffsetX + getX();
        int newPaneOffsetY = paneOffsetY + getY();
        int newMaxLength = Math.min(maxLength, getLength());
        int newMaxHeight = Math.min(maxHeight, getHeight());

        this.backgroundPane.display(gui, inventory, playerInventory, newPaneOffsetX, newPaneOffsetY, newMaxLength,
            newMaxHeight);
        this.fillPane.display(gui, inventory, playerInventory, newPaneOffsetX, newPaneOffsetY, newMaxLength,
            newMaxHeight);
    }

    @Override
    public void setX(int x) {
        super.setX(x);

        this.fillPane.setX(x);
        this.backgroundPane.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        this.fillPane.setY(y);
        this.backgroundPane.setY(y);
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
    }

    @Override
    public void flipVertically(boolean flipVertically) {
        this.flipVertically = flipVertically;
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
