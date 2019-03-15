package com.github.stefvanschie.inventoryframework.pane.component;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.pane.Flippable;
import com.github.stefvanschie.inventoryframework.pane.Orientable;
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
 * A percentage bar for a graphical interface into what amount of a whole is set.
 *
 * @since 0.5.0
 */
public class PercentageBar extends Pane implements Orientable, Flippable {

    /**
     * The green and the red parts of the percentage bar
     */
    @NotNull
    private final OutlinePane fillPane, backgroundPane;

    /**
     * The percentage this bar is at
     */
    private float percentage;

    /**
     * The orientation of the percentage bar
     */
    @NotNull
    private Orientation orientation;

    /**
     * Whether the pane is flipped horizontally or vertically
     */
    private boolean flipHorizontally, flipVertically;

    /**
     * {@inheritDoc}
     */
    private PercentageBar(int x, int y, int length, int height, @NotNull Priority priority) {
        this(length, height);

        this.x = x;
        this.y = y;

        setPriority(priority);
    }

    /**
     * {@inheritDoc}
     */
    public PercentageBar(int x, int y, int length, int height) {
        this(x, y, length, height, Priority.NORMAL);
    }

    /**
     * {@inheritDoc}
     */
    private PercentageBar(int length, int height) {
        super(length, height);

        this.percentage = 0F;
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

        if (event.getView().getInventory(event.getRawSlot()).equals(event.getView().getBottomInventory())) {
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

        event.setCancelled(true);

        int newPaneOffsetX = paneOffsetX + getX();
        int newPaneOffsetY = paneOffsetY + getY();

        return this.fillPane.click(gui, event, newPaneOffsetX, newPaneOffsetY, length, height) ||
            this.backgroundPane.click(gui, event, newPaneOffsetX, newPaneOffsetY, length, height);
    }

    /**
     * Sets the percentage of this bar. The percentage has to be in (0,1). If not, this method will throw an
     * {@link IllegalArgumentException}.
     *
     * @param percentage the new percentage.
     * @throws IllegalArgumentException when the percentage is out of range
     * @since 0.5.0
     */
    public void setPercentage(float percentage) {
        if (percentage < 0 || percentage > 1) {
            throw new IllegalArgumentException("Percentage is out of range (0,1)");
        }

        this.percentage = percentage;

        if (orientation == Orientation.HORIZONTAL) {
            this.fillPane.setLength(Math.round(getLength() * percentage));

            if (flipHorizontally) {
                this.fillPane.setX(getLength() - this.fillPane.getLength());
            }
        } else if (orientation == Orientation.VERTICAL) {
            this.fillPane.setHeight(Math.round(getHeight() * percentage));

            if (flipVertically) {
                this.fillPane.setY(getHeight() - this.fillPane.getHeight());
            }
        } else {
            throw new UnsupportedOperationException("Unknown orientation");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLength(int length) {
        super.setLength(length);

        if (orientation == Orientation.HORIZONTAL) {
            this.fillPane.setLength(Math.round(length * percentage));

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHeight(int height) {
        super.setHeight(height);

        if (orientation == Orientation.HORIZONTAL) {
            this.fillPane.setHeight(height);
        } else if (orientation == Orientation.VERTICAL) {
            this.fillPane.setHeight(Math.round(height * percentage));

            if (flipVertically) {
                this.fillPane.setY(getHeight() - this.fillPane.getHeight());
            }
        } else {
            throw new UnsupportedOperationException("Unknown orientation");
        }

        this.backgroundPane.setHeight(height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOrientation(@NotNull Orientation orientation) {
        this.orientation = orientation;

        if (orientation == Orientation.HORIZONTAL) {
            fillPane.setLength(Math.round(getLength() * percentage));
            fillPane.setHeight(getHeight());
        } else if (orientation == Orientation.VERTICAL) {
            fillPane.setLength(getLength());
            fillPane.setHeight(Math.round(getHeight() * percentage));
        } else {
            throw new IllegalArgumentException("Unknown orientation");
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void setX(int x) {
        super.setX(x);

        this.fillPane.setX(x);
        this.backgroundPane.setX(x);
    }

    /**
     * {@inheritDoc}
     */
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
        return Stream.of(this.fillPane, this.backgroundPane).collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flipHorizontally(boolean flipHorizontally) {
        this.flipHorizontally = flipHorizontally;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flipVertically(boolean flipVertically) {
        this.flipVertically = flipVertically;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Gets the percentage as a float in between (0,1) this bar is currently set at.
     *
     * @return the percentage
     * Since 0.5.0
     */
    public float getPercentage() {
        return percentage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFlippedHorizontally() {
        return flipHorizontally;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFlippedVertically() {
        return flipVertically;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {}

    /**
     * Loads a percentage bar from a given element
     *
     * @param instance the instance class
     * @param element  the element
     * @return the percentage bar
     */
    @NotNull
    @Contract(value = "_, null -> fail", pure = true)
    public static PercentageBar load(@NotNull Object instance, @NotNull Element element) {
        int length;
        int height;

        try {
            length = Integer.parseInt(element.getAttribute("length"));
            height = Integer.parseInt(element.getAttribute("height"));
        } catch (NumberFormatException exception) {
            throw new XMLLoadException(exception);
        }

        PercentageBar percentageBar = new PercentageBar(length, height);

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
                throw new XMLLoadException(exception);
            }
        }

        return percentageBar;
    }
}
