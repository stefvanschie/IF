package com.github.stefvanschie.inventoryframework.bukkit.pane;

import com.github.stefvanschie.inventoryframework.bukkit.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.bukkit.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.core.pane.AbstractStaticPane;
import com.github.stefvanschie.inventoryframework.bukkit.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.core.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.core.pane.util.Flippable;
import com.github.stefvanschie.inventoryframework.core.pane.util.Rotatable;
import com.github.stefvanschie.inventoryframework.core.util.GeometryUtil;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
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
 */
public class StaticPane extends Pane implements AbstractStaticPane {

	/**
	 * A map of locations inside this pane and their item. The locations are stored in a way where the x coordinate is
     * the key and the y coordinate is the value.
	 */
	@NotNull
	private final Map<Map.Entry<Integer, Integer>, GuiItem> items;

	/**
	 * The clockwise rotation of this pane in degrees
	 */
	private int rotation;

	/**
	 * Whether the items should be flipped horizontally and/or vertically
	 */
	private boolean flipHorizontally, flipVertically;

    public StaticPane(int x, int y, int length, int height, @NotNull Priority priority) {
        super(x, y, length, height, priority);

        this.items = new HashMap<>(length * height);
    }

	public StaticPane(int x, int y, int length, int height) {
		this(x, y, length, height, Priority.NORMAL);
	}

    public StaticPane(int length, int height) {
        this(0, 0, length, height);
    }

	@Override
	public void display(@NotNull InventoryComponent inventoryComponent, int paneOffsetX, int paneOffsetY, int maxLength,
                        int maxHeight) {
		int length = Math.min(this.length, maxLength);
		int height = Math.min(this.height, maxHeight);

		items.entrySet().stream().filter(entry -> entry.getValue().isVisible()).forEach(entry -> {
			Map.Entry<Integer, Integer> location = entry.getKey();

			int x = location.getKey(), y = location.getValue();

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

			int finalRow = getY() + y + paneOffsetY;
			int finalColumn = getX() + x + paneOffsetX;

			inventoryComponent.setItem(item, finalColumn, finalRow);
		});
	}

	/**
	 * Adds a gui item at the specific spot in the pane. If the coordinates as specified by the x and y parameters is
     * already occupied, that item will be replaced by the item parameter.
	 *
	 * @param item the item to set
	 * @param x    the x coordinate of the position of the item
     * @param y    the y coordinate of the position of the item
	 */
	public void addItem(@NotNull GuiItem item, int x, int y) {
	    items.keySet().removeIf(entry -> entry.getKey() == x && entry.getValue() == y);

		items.put(new AbstractMap.SimpleEntry<>(x, y), item);
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
        StaticPane staticPane = new StaticPane(x, y, length, height, getPriority());

        for (Map.Entry<Map.Entry<Integer, Integer>, GuiItem> entry : items.entrySet()) {
            Map.Entry<Integer, Integer> coordinates = entry.getKey();

            staticPane.addItem(entry.getValue().copy(), coordinates.getKey(), coordinates.getValue());
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
	 * @param action    The action called whenever an interaction with the item happens
     * @since 0.5.9
	 */
	public void fillWith(@NotNull ItemStack itemStack, @Nullable Consumer<InventoryClickEvent> action) {
		//The non empty spots
		Set<Map.Entry<Integer, Integer>> locations = this.items.keySet();

		for (int y = 0; y < this.getHeight(); y++) {
			for (int x = 0; x < this.getLength(); x++) {
				boolean found = false;

				for (Map.Entry<Integer, Integer> location : locations) {
					if (location.getKey() == x && location.getValue() == y) {
						found = true;
						break;
					}
				}

				if (!found) {
					this.addItem(new GuiItem(itemStack, action), x, y);
				}
			}
		}
	}

	/**
	 * Fills all empty space in the pane with the given {@code itemStack}
	 *
	 * @param itemStack The {@link ItemStack} to fill the empty space with
     * @since 0.2.4
	 */
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
	 * @param element  the element
	 * @return the outline pane
	 */
	@NotNull
	public static StaticPane load(@NotNull Object instance, @NotNull Element element) {
		try {
			StaticPane staticPane = new StaticPane(
				Integer.parseInt(element.getAttribute("length")),
				Integer.parseInt(element.getAttribute("height"))
            );

			load(staticPane, instance, element);
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

				staticPane.addItem(loadItem(instance, child), Integer.parseInt(child.getAttribute("x")),
                    Integer.parseInt(child.getAttribute("y")));
			}

			return staticPane;
		} catch (NumberFormatException exception) {
			throw new XMLLoadException(exception);
		}
	}
}
