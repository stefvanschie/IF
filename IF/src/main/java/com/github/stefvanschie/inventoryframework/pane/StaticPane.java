package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.github.stefvanschie.inventoryframework.util.GeometryUtil;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.function.Consumer;

/**
 * A pane for static items and stuff. All items will have to be specified a slot, or will be added in the next position.
 * <p>
 * This pane allows you to specify the positions of the items either in the form of an x and y coordinate pair or as an
 * index, in which case the indexing starts from the top left and continues to the right and bottom, with the horizontal
 * axis taking priority. There are nuances at play with regard to mixing these two types of positioning systems within
 * the same pane. It's recommended to only use one of these systems per pane and to not mix them.
 * </p>
 */
public class StaticPane extends Pane implements Flippable, Rotatable {

	/**
	 * A map of locations inside this pane and their item. The locations are stored in a way where the x coordinate is
     * the key and the y coordinate is the value.
	 */
	@NotNull
	private final Map<Slot, GuiItem> items;

	/**
	 * The clockwise rotation of this pane in degrees
	 */
	private int rotation;

	/**
	 * Whether the items should be flipped horizontally and/or vertically
	 */
	private boolean flipHorizontally, flipVertically;

    /**
     * Creates a new static pane.
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     * @since 0.10.8
     */
    public StaticPane(Slot slot, int length, int height, @NotNull Priority priority) {
        super(slot, length, height, priority);

        this.items = new HashMap<>(length * height);
    }

    public StaticPane(int x, int y, int length, int height, @NotNull Priority priority) {
        this(Slot.fromXY(x, y), length, height, priority);
    }

    /**
     * Creates a new static pane.
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @since 0.10.8
     */
    public StaticPane(Slot slot, int length, int height) {
        this(slot, length, height, Priority.NORMAL);
    }

	public StaticPane(int x, int y, int length, int height) {
		this(x, y, length, height, Priority.NORMAL);
	}

    public StaticPane(int length, int height) {
        this(0, 0, length, height);
    }

    /**
     * {@inheritDoc}
     *
     * If there are multiple items in the same position when displaying the items, either one of those items may be
     * shown. In particular, there is no guarantee that a specific item will be shown.
     *
     * @param inventoryComponent {@inheritDoc}
     * @param paneOffsetX {@inheritDoc}
     * @param paneOffsetY {@inheritDoc}
     * @param maxLength {@inheritDoc}
     * @param maxHeight {@inheritDoc}
     */
	@Override
	public void display(@NotNull InventoryComponent inventoryComponent, int paneOffsetX, int paneOffsetY, int maxLength,
                        int maxHeight) {
		int length = Math.min(this.length, maxLength);
		int height = Math.min(this.height, maxHeight);

		items.entrySet().stream().filter(entry -> entry.getValue().isVisible()).forEach(entry -> {
			Slot location = entry.getKey();

			int x = location.getX(getLength());
            int y = location.getY(getLength());

			if (flipHorizontally)
				x = length - x - 1;

			if (flipVertically)
				y = height - y - 1;

			Map.Entry<Integer, Integer> coordinates = GeometryUtil.processClockwiseRotation(x, y, length, height,
				rotation);

			x = coordinates.getKey();
			y = coordinates.getValue();

			if (x < 0 || x >= length || y < 0 || y >= height) {
			    return;
            }

			GuiItem item = entry.getValue();

            Slot slot = getSlot();
            int finalRow = slot.getY(maxLength) + y + paneOffsetY;
			int finalColumn = slot.getX(maxLength) + x + paneOffsetX;

			inventoryComponent.setItem(item, finalColumn, finalRow);
		});
	}

	/**
	 * Adds a gui item at the specific spot in the pane. If there is another item specified in terms of x and y
     * coordinates that are equal to the coordinates of this item, the old item will be overwritten by this item.
	 *
	 * @param item the item to set
	 * @param x    the x coordinate of the position of the item
     * @param y    the y coordinate of the position of the item
	 */
	public void addItem(@NotNull GuiItem item, int x, int y) {
		addItem(item, Slot.fromXY(x, y));
	}

    /**
     * Adds a gui item at the specific spot in the pane. If the slot is specified in terms of an x and y coordinate pair
     * and this pane contains another item whose position is specified as such and these positions are equal, the old
     * item will be overwritten by this item. If the slot is specified in terms of an index and this pane contains
     * another item whose position is specified as such and these positions are equal, the old item will be overwritten
     * by this item.
     *
     * @param item the item to set
     * @param slot the position of the item
     * @since 0.10.8
     */
    public void addItem(@NotNull GuiItem item, Slot slot) {
        this.items.put(slot, item);
    }

    /**
     * Removes the specified item from the pane
     *
     * @param item the item to remove
     * @since 0.5.8
     */
    public void removeItem(@NotNull GuiItem item) {
        items.values().removeIf(guiItem -> guiItem.equals(item));
    }

    /**
     * Removes the specified item from the pane. This will only remove items whose slot was specified in terms of an x
     * and y coordinate pair which matches the coordinate specified.
     *
     * @param x the x coordinate of the item to remove
     * @param y the y coordinate of the item to remove
     * @since 0.10.0
     */
    public void removeItem(int x, int y) {
        this.items.remove(Slot.fromXY(x, y));
    }

    /**
     * Removes the specified item from the pane. This will only remove items whose slot was specified in the same way as
     * the original slot and whose slot positions match.
     *
     * @param slot the slot of the item to remove
     * @since 0.10.8
     */
    public void removeItem(@NotNull Slot slot) {
        this.items.remove(slot);
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

		callOnClick(event);

        ItemStack itemStack = event.getCurrentItem();

        if (itemStack == null) {
            return false;
        }

        GuiItem clickedItem = findMatchingItem(items.values(), itemStack);

        if (clickedItem == null) {
            return false;
        }

        clickedItem.callAction(event);

        return true;
	}

    @NotNull
    @Contract(pure = true)
	@Override
    public StaticPane copy() {
        StaticPane staticPane = new StaticPane(getSlot(), length, height, getPriority());

        for (Map.Entry<Slot, GuiItem> entry : items.entrySet()) {
            staticPane.addItem(entry.getValue().copy(), entry.getKey());
        }

        staticPane.setVisible(isVisible());
        staticPane.onClick = onClick;

        staticPane.uuid = uuid;

        staticPane.rotation = rotation;
        staticPane.flipHorizontally = flipHorizontally;
        staticPane.flipVertically = flipVertically;

        return staticPane;
    }

    @Override
	public void setRotation(int rotation) {
		if (length != height) {
			throw new UnsupportedOperationException("length and height are different");
		}
		if (rotation % 90 != 0) {
			throw new IllegalArgumentException("rotation isn't divisible by 90");
		}

		this.rotation = rotation % 360;
	}

	/**
	 * Fills all empty space in the pane with the given {@code itemStack} and adds the given action
	 *
	 * @param itemStack The {@link ItemStack} to fill the empty space with
	 * @param action The action called whenever an interaction with the item happens
     * @param plugin the plugin that will be the owner of the created items
     * @see #fillWith(ItemStack, Consumer)
     * @since 0.10.8
	 */
	public void fillWith(@NotNull ItemStack itemStack, @Nullable Consumer<InventoryClickEvent> action,
                         @NotNull Plugin plugin) {
		//The non empty spots
		Set<Slot> locations = this.items.keySet();

		for (int y = 0; y < this.getHeight(); y++) {
			for (int x = 0; x < this.getLength(); x++) {
				boolean found = false;

				for (Slot location : locations) {
					if (location.getX(getLength()) == x && location.getY(getLength()) == y) {
						found = true;
						break;
					}
				}

				if (!found) {
					this.addItem(new GuiItem(itemStack, action, plugin), x, y);
				}
			}
		}
	}

    /**
     * Fills all empty space in the pane with the given {@code itemStack} and adds the given action
     *
     * @param itemStack The {@link ItemStack} to fill the empty space with
     * @param action The action called whenever an interaction with the item happens
     * @since 0.5.9
     */
    public void fillWith(@NotNull ItemStack itemStack, @Nullable Consumer<InventoryClickEvent> action) {
        fillWith(itemStack, action, JavaPlugin.getProvidingPlugin(StaticPane.class));
    }

	/**
	 * Fills all empty space in the pane with the given {@code itemStack}
	 *
	 * @param itemStack The {@link ItemStack} to fill the empty space with
     * @since 0.2.4
	 */
	@Contract("null -> fail")
	public void fillWith(@NotNull ItemStack itemStack) {
		this.fillWith(itemStack, null);
	}

	@NotNull
	@Override
	public Collection<GuiItem> getItems() {
		return items.values();
	}

    @Override
    public void clear() {
        items.clear();
    }

	@NotNull
	@Contract(pure = true)
	@Override
	public Collection<Pane> getPanes() {
		return new HashSet<>();
	}

	@Override
	public void flipHorizontally(boolean flipHorizontally) {
		this.flipHorizontally = flipHorizontally;
	}

	@Override
	public void flipVertically(boolean flipVertically) {
		this.flipVertically = flipVertically;
	}

	@Contract(pure = true)
    @Override
	public int getRotation() {
		return rotation;
	}

	@Contract(pure = true)
    @Override
	public boolean isFlippedHorizontally() {
		return flipHorizontally;
	}

	@Contract(pure = true)
    @Override
	public boolean isFlippedVertically() {
		return flipVertically;
	}

	/**
	 * Loads an outline pane from a given element
	 *
	 * @param instance the instance class
	 * @param element the element
     * @param plugin the plugin that will be the owner of the udnerlying items
	 * @return the outline pane
     * @since 0.10.8
	 */
	@NotNull
	public static StaticPane load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
		try {
			StaticPane staticPane = new StaticPane(
				Integer.parseInt(element.getAttribute("length")),
				Integer.parseInt(element.getAttribute("height"))
            );

			Pane.load(staticPane, instance, element);
			Flippable.load(staticPane, element);
			Rotatable.load(staticPane, element);

			if (element.hasAttribute("populate"))
				return staticPane;

			NodeList childNodes = element.getChildNodes();

			for (int i = 0; i < childNodes.getLength(); i++) {
				Node item = childNodes.item(i);

				if (item.getNodeType() != Node.ELEMENT_NODE)
					continue;

				Element child = (Element) item;

				staticPane.addItem(Pane.loadItem(instance, child, plugin), Slot.deserialize(child));
			}

			return staticPane;
		} catch (NumberFormatException exception) {
			throw new XMLLoadException(exception);
		}
	}

    /**
     * Loads an outline pane from a given element
     *
     * @param instance the instance class
     * @param element  the element
     * @return the outline pane
     * @deprecated this method is no longer used internally and has been superseded by
     *             {@link #load(Object, Element, Plugin)}
     */
    @NotNull
    @Deprecated
    public static StaticPane load(@NotNull Object instance, @NotNull Element element) {
        return load(instance, element, JavaPlugin.getProvidingPlugin(StaticPane.class));
    }
}
