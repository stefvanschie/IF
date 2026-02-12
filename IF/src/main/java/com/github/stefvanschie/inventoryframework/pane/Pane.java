package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.gui.GuiComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.pane.util.Mask;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil;
import com.github.stefvanschie.inventoryframework.util.UUIDTagType;
import com.github.stefvanschie.inventoryframework.util.XMLUtil;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.lang.UnsupportedOperationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/**
 * The base class for all panes.
 */
public abstract class Pane {

    /**
     * The position of this pane, which is (0,0) by default
     */
    @NotNull
    protected Slot slot = Slot.fromXY(0, 0);

    /**
     * Length is horizontal, height is vertical
     */
    protected int length, height;

    /**
     * The visibility state of the pane
     */
    private boolean visible;

    /**
     * The priority of the pane, determines when it will be rendered
     */
    @NotNull
    private Priority priority;

    /**
     * The consumer that will be called once a players clicks in this pane
     */
    @Nullable
    protected Consumer<? super InventoryClickEvent> onClick;

    /**
     * A unique identifier for panes to locate them by
     */
    protected UUID uuid;

    /**
     * Constructs a new default pane
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     * @since 0.10.8
     */
    protected Pane(@NotNull Slot slot, int length, int height, @NotNull Priority priority) {
        if (length == 0 || height == 0) {
            throw new IllegalArgumentException("Length and height of pane must be greater than zero");
        }

        setSlot(slot);

        this.length = length;
        this.height = height;

        this.priority = priority;
        this.visible = true;

        this.uuid = UUID.randomUUID();
    }

    /**
     * Constructs a new default pane
     *
     * @param x the upper left x coordinate of the pane
     * @param y the upper left y coordinate of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     */
    protected Pane(int x, int y, int length, int height, @NotNull Priority priority) {
        this(Slot.fromXY(x, y), length, height, priority);
    }

    /**
     * Constructs a new default pane, with no position
     *
     * @param length the length of the pane
     * @param height the height of the pane
     */
    protected Pane(int length, int height) {
        if (length == 0 || height == 0) {
            throw new IllegalArgumentException("Length and height of pane must be greater than zero");
        }

        this.length = length;
        this.height = height;

        this.priority = Priority.NORMAL;
        this.visible = true;

        this.uuid = UUID.randomUUID();
    }

    /**
     * Constructs a new default pane
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     */
    protected Pane(Slot slot, int length, int height) {
        this(slot, length, height, Priority.NORMAL);
    }

    /**
     * Constructs a new default pane
     *
     * @param x the upper left x coordinate of the pane
     * @param y the upper left y coordinate of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     */
    protected Pane(int x, int y, int length, int height) {
        this(x, y, length, height, Priority.NORMAL);
    }

    /**
	 * Makes a copy of this pane and returns it. This makes a deep copy of the pane. This entails that the underlying
	 * panes and/or items will be copied as well. The returned pane will never be reference equal to the current pane.
	 *
	 * @return a copy of this pane
	 * @since 0.6.2
	 */
	@NotNull
	@Contract(pure = true)
    public Pane copy() {
		throw new UnsupportedOperationException("The implementing pane hasn't overridden the copy method");
	}

    /**
     * Set the length of this pane
     *
     * @param length the new length
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Set the height of this pane
     *
     * @param height the new height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Sets the slot of this pane.
     *
     * @param slot the slot
     * @since 0.10.8
     */
    public void setSlot(@NotNull Slot slot) {
        this.slot = slot;
    }

    /**
     * Returns the length of this pane
     *
     * @return the length
     */
    @Contract(pure = true)
    public int getLength() {
        return length;
    }

    /**
     * Returns the height of this pane
     *
     * @return the height
     */
    @Contract(pure = true)
    public int getHeight() {
        return height;
    }

    /**
     * Gets the {@link UUID} associated with this pane.
     *
     * @return the uuid
     * @since 0.7.1
     */
    @NotNull
    @Contract(pure = true)
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Gets the slot of the position of this pane
     *
     * @return the slot
     * @since 0.10.8
     */
    @NotNull
    @Contract(pure = true)
    public Slot getSlot() {
        return this.slot;
    }

    /**
     * Has to set all the items in the right spot inside the inventory
     *
     * @param guiComponent the gui component in which the items should be displayed
     * @param paneOffsetX the pane's offset on the x axis
     * @param paneOffsetY the pane's offset on the y axis
     * @param maxLength the maximum length of the pane
     * @param maxHeight the maximum height of the pane
     */
    public abstract void display(@NotNull GuiComponent guiComponent, int paneOffsetX, int paneOffsetY, int maxLength,
                                 int maxHeight);

    /**
     * Returns the pane's visibility state
     *
     * @return the pane's visibility
     */
    @Contract(pure = true)
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets whether this pane is visible or not
     *
     * @param visible the pane's visibility
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Called whenever there is being clicked on this pane
     *
     * @param gui the gui in which was clicked
     * @param guiComponent the gui component in which this pane resides
     * @param event the event that occurred while clicking on this item
     * @param slot the slot that was clicked in
     * @param paneOffsetX the pane's offset on the x axis
     * @param paneOffsetY the pane's offset on the y axis
     * @param maxLength the maximum length of the pane
     * @param maxHeight the maximum height of the pane
     * @return whether the item was found or not
     */
    public abstract boolean click(@NotNull Gui gui, @NotNull GuiComponent guiComponent,
                                  @NotNull InventoryClickEvent event, int slot, int paneOffsetX, int paneOffsetY,
                                  int maxLength, int maxHeight);

    /**
     * Sets the priority of this pane
     *
     * @param priority the priority
     */
    public void setPriority(@NotNull Priority priority) {
        this.priority = priority;
    }

    public static void load(@NotNull Pane pane, @NotNull Object instance, @NotNull Element element) {
        pane.setSlot(Slot.deserialize(element));

        if (element.hasAttribute("priority")) {
            try {
                pane.setPriority(Priority.valueOf(element.getAttribute("priority").toUpperCase()));
            } catch (IllegalArgumentException exception) {
                throw new XMLLoadException("Priority attribute is not a proper value", exception);
            }
        }

        if (element.hasAttribute("visible"))
            pane.setVisible(Boolean.parseBoolean(element.getAttribute("visible")));

        if (element.hasAttribute("field"))
            XMLUtil.loadFieldAttribute(instance, element, pane);

        if (element.hasAttribute("onClick"))
            pane.setOnClick(XMLUtil.loadOnEventAttribute(instance, element, InventoryClickEvent.class, "onClick"));

        if (element.hasAttribute("populate")) {
            String attribute = element.getAttribute("populate");
            boolean found = false;

            for (Method method: instance.getClass().getMethods()) {
                if (!method.getName().equals(attribute))
                    continue;

                try {
                    method.setAccessible(true);
                    method.invoke(instance, pane);
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    throw new XMLLoadException(exception);
                }

                found = true;
            }

            if (!found) {
                throw new XMLLoadException("Specified method could not be found");
            }
        }
    }

    /**
     * Checks whether a {@link GuiItem} is the same item as the given {@link ItemStack}. The item will be compared using
     * internal data. When the item does not have this data, this method will return false. If the item does have such
     * data, but its value does not match, false is also returned. This method will not mutate any of the provided
     * arguments.
     *
     * @param guiItem the gui item to check
     * @param item the item which the gui item should be checked against
     * @return true if the {@link GuiItem} matches the {@link ItemStack}, false otherwise
     * @since 0.10.14
     */
    @Contract(pure = true)
    protected static boolean matchesItem(@NotNull GuiItem guiItem, @NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }

        return guiItem.getUUID().equals(meta.getPersistentDataContainer().get(guiItem.getKey(), UUIDTagType.INSTANCE));
    }

    /**
     * Finds a type of {@link GuiItem} from the provided collection of items based on the provided {@link ItemStack}.
     * The items will be compared using internal data. When the item does not have this data, this method will return
     * null. If the item does have such data, but its value cannot be found in the provided list, null is also returned.
     * This method will not mutate any of the provided arguments, nor any of the contents inside of the arguments. The
     * provided collection may be unmodifiable if preferred. This method will always return a type of {@link GuiItem}
     * that is in the provided collection - when the returned result is not null - such that an element E inside the
     * provided collection reference equals the returned type of {@link GuiItem}.
     *
     * @param items a collection of items in which will be searched
     * @param item the item for which an {@link GuiItem} should be found
     * @param <T> a type of GuiItem, which will be used in the provided collection and as return type
     * @return the found type of {@link GuiItem} or null if none was found
     * @since 0.5.14
     */
    @Nullable
    @Contract(pure = true)
    protected static <T extends GuiItem> T findMatchingItem(@NotNull Collection<T> items, @NotNull ItemStack item) {
        for (T guiItem : items) {
            if (matchesItem(guiItem, item)) {
                return guiItem;
            }
        }

        return null;
    }

    /**
     * Returns the priority of the pane
     *
     * @return the priority
     */
    @NotNull
    public Priority getPriority() {
        return priority;
    }

    /**
     * Gets all the items in this pane and all underlying panes.
     * The returned collection is not guaranteed to be mutable or to be a view of the underlying data.
     * (So changes to the gui are not guaranteed to be visible in the returned value.)
     *
     * @return all items
     */
    @NotNull
    @Contract(pure = true)
    public abstract Collection<GuiItem> getItems();

    /**
     * Gets all the panes in this panes, including any child panes from other panes.
     * The returned collection is not guaranteed to be mutable or to be a view of the underlying data.
     * (So changes to the gui are not guaranteed to be visible in the returned value.)
     *
     * @return all panes
     */
    @NotNull
    @Contract(pure = true)
    public abstract Collection<Pane> getPanes();

    /**
     * Clears the entire pane of any items/panes. Underlying panes will not be cleared.
     *
     * @since 0.3.2
     */
    public abstract void clear();

    /**
     * Set the consumer that should be called whenever this pane is clicked in.
     *
     * @param onClick the consumer that gets called
     * @since 0.4.0
     */
    public void setOnClick(@Nullable Consumer<? super InventoryClickEvent> onClick) {
        this.onClick = onClick;
    }
    
    /**
     * Calls the consumer (if it's not null) that was specified using {@link #setOnClick(Consumer)},
     * so the consumer that should be called whenever this pane is clicked in.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    protected void callOnClick(@NotNull InventoryClickEvent event) {
        if (onClick == null) {
            return;
        }


        try {
            onClick.accept(event);
        } catch (Throwable t) {
            throw new RuntimeException(
                    "Exception while handling click event in inventory '"
                    + InventoryViewUtil.getInstance().getTitle(event.getView()) + "', slot=" + event.getSlot() +
                    ", for " + getClass().getSimpleName() + ", slot=" + getSlot()
                    + ", length=" + length + ", height=" + height,
                    t
            );
        }
    }

    /**
     * Creates a pane which displays as a border around the outside of the pane consisting of the provided item. The
     * slot, length and height parameters are used for the respective properties of the pane. If either the length or
     * height is negative an {@link IllegalArgumentException} will be thrown.
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param item the item of which the border is made
     * @return the created pane which displays a border
     * @since 0.10.8
     * @throws IllegalArgumentException if length or height is negative
     */
    @NotNull
    @Contract(pure = true)
    public static Pane createBorder(Slot slot, int length, int height, @NotNull GuiItem item) {
        if (length < 0) {
            throw new IllegalArgumentException("Length should be non-negative");
        }

        if (height < 0) {
            throw new IllegalArgumentException("Height should be non-negative");
        }

        String[] mask = new String[height];

        if (height > 0) {
            mask[0] = createLine(length);
        }

        if (height > 1) {
            mask[height - 1] = createLine(length);
        }

        for (int yIndex = 1; yIndex < height - 1; yIndex++) {
            StringBuilder builder = new StringBuilder("1");

            for (int i = 0; i < length - 2; i++) {
                builder.append('0');
            }

            mask[yIndex] = builder.append('1').toString();
        }

        OutlinePane pane = new OutlinePane(slot, length, height);
        pane.applyMask(new Mask(mask));
        pane.addItem(item);
        pane.setRepeat(true);

        return pane;
    }

    /**
     * Creates a pane which displays as a border around the outside of the pane consisting of the provided item. The x,
     * y, length and height parameters are used for the respective properties of the pane. If either the length or
     * height is negative an {@link IllegalArgumentException} will be thrown.
     *
     * @param x the x coordinate of the pane
     * @param y the y coordinate of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param item the item of which the border is made
     * @return the created pane which displays a border
     * @since 0.10.7
     * @throws IllegalArgumentException if length or height is negative
     */
    @NotNull
    @Contract(pure = true)
    public static Pane createBorder(int x, int y, int length, int height, @NotNull GuiItem item) {
        return createBorder(Slot.fromXY(x, y), length, height, item);
    }

    /**
     * Creates a string containing the character '1' repeated length amount of times. If the provided length is negative
     * an {@link IllegalArgumentException} will be thrown.
     *
     * @param length the length of the string
     * @return the string containing '1's
     * @since 0.10.7
     * @throws IllegalArgumentException if length is negative
     */
    @NotNull
    @Contract(pure = true)
    private static String createLine(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length should be non-negative");
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            builder.append('1');
        }

        return builder.toString();
    }

    /**
     * An enum representing the rendering priorities for the panes. Uses a similar system to Bukkit's
     * {@link org.bukkit.event.EventPriority} system
     */
    public enum Priority {

        /**
         * The lowest priority, will be rendered first
         */
        LOWEST {
            @Override
            public boolean isLessThan(@NotNull Priority priority) {
                return priority != this;
            }
        },

        /**
         * A low priority, lower than default
         */
        LOW {
            @Override
            public boolean isLessThan(@NotNull Priority priority) {
                return priority != this && priority != LOWEST;
            }
        },

        /**
         * A normal priority, the default
         */
        NORMAL {
            @Override
            public boolean isLessThan(@NotNull Priority priority) {
                return priority != this && priority != LOW && priority != LOWEST;
            }
        },

        /**
         * A higher priority, higher than default
         */
        HIGH {
            @Override
            public boolean isLessThan(@NotNull Priority priority) {
                return priority == HIGHEST || priority == MONITOR;
            }
        },

        /**
         * The highest priority for production use
         */
        HIGHEST {
            @Override
            public boolean isLessThan(@NotNull Priority priority) {
                return priority == MONITOR;
            }
        },

        /**
         * The highest priority, will always be called last, should not be used for production code
         */
        MONITOR {
            @Override
            public boolean isLessThan(@NotNull Priority priority) {
                return false;
            }
        };

        /**
         * Whether this priority is less than the priority specified.
         *
         * @param priority the priority to compare against
         * @return true if this priority is less than the specified priority, false otherwise
         * @since 0.8.0
         */
        @Contract(pure = true)
        public abstract boolean isLessThan(@NotNull Priority priority);

        /**
         * Whether this priority is greater than the priority specified.
         *
         * @param priority the priority to compare against
         * @return true if this priority is greater than the specified priority, false otherwise
         * @since 0.8.0
         */
        @Contract(pure = true)
        public boolean isGreaterThan(@NotNull Priority priority) {
            return !isLessThan(priority) && this != priority;
        }
    }
}
