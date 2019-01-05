package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.util.GeometryUtil;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A pane for static items and stuff. All items will have to be specified a slot, or will be added in the next position.
 */
public class StaticPane extends Pane implements Flippable, Rotatable {

	/**
	 * A set of items inside this pane and their locations
	 */
	@NotNull
	private final Set<Map.Entry<GuiItem, Map.Entry<Integer, Integer>>> items;

	/**
	 * The clockwise rotation of this pane in degrees
	 */
	private int rotation;

	/**
	 * Whether the items should be flipped horizontally and/or vertically
	 */
	private boolean flipHorizontally, flipVertically;

    /**
     * {@inheritDoc}
     */
    public StaticPane(int x, int y, int length, int height, @NotNull Priority priority) {
        super(x, y, length, height, priority);

        this.items = new HashSet<>(length * height);
    }

	/**
	 * {@inheritDoc}
	 */
	public StaticPane(int x, int y, int length, int height) {
		this(x, y, length, height, Priority.NORMAL);
	}

    /**
     * {@inheritDoc}
     */
    public StaticPane(int length, int height) {
        super(length, height);

        this.items = new HashSet<>(length * height);
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void display(@NotNull Inventory inventory, int paneOffsetX, int paneOffsetY, int maxLength, int maxHeight) {
		int length = Math.min(this.length, maxLength);
		int height = Math.min(this.height, maxHeight);

		items.stream().filter(entry -> {
			GuiItem key = entry.getKey();
			Map.Entry<Integer, Integer> value = entry.getValue();

			return key.isVisible() && value.getKey() + paneOffsetX <= 9 && value.getValue() + paneOffsetY <= 6;
		}).forEach(entry -> {
			Map.Entry<Integer, Integer> location = entry.getValue();

			int x = location.getKey(), y = location.getValue();

			if (flipHorizontally)
				x = length - x - 1;

			if (flipVertically)
				y = height - y - 1;

			Map.Entry<Integer, Integer> coordinates = GeometryUtil.processClockwiseRotation(x, y, length, height,
				rotation);

			inventory.setItem((getY() + coordinates.getValue() + paneOffsetY) * 9 + (getX() + coordinates.getKey() +
                paneOffsetX), entry.getKey().getItem());
		});
	}

	/**
	 * Adds a gui item at the specific spot in the pane
	 *
	 * @param item the item to set
	 * @param x    the x coordinate of the position of the item
     * @param y    the y coordinate of the position of the item
	 */
	public void addItem(@NotNull GuiItem item, int x, int y) {
		items.add(new AbstractMap.SimpleEntry<>(item, new AbstractMap.SimpleEntry<>(x, y)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean click(@NotNull InventoryClickEvent event, int paneOffsetX, int paneOffsetY, int maxLength,
						 int maxHeight) {
		int length = Math.min(this.length, maxLength);
		int height = Math.min(this.height, maxHeight);

		int slot = event.getSlot();

		//correct coordinates
		int x = (slot % 9) - getX() - paneOffsetX;
		int y = (slot / 9) - getY() - paneOffsetY;

		//this isn't our item
		if (x < 0 || x >= length || y < 0 || y >= height)
			return false;

        if (onLocalClick != null)
            onLocalClick.accept(event);

		//first we undo the rotation
		//this is the same as applying a new rotation to match up to 360, so we'll be doing that
		Map.Entry<Integer, Integer> coordinates = GeometryUtil.processCounterClockwiseRotation(x, y, length, height,
			rotation);

		int newX = coordinates.getKey(), newY = coordinates.getValue();

		if (flipHorizontally)
			newX = length - newX - 1;

		if (flipVertically)
			newY = height - newY - 1;

		//find the item on the correct spot
		for (Map.Entry<GuiItem, Map.Entry<Integer, Integer>> entry : items) {
			Map.Entry<Integer, Integer> location = entry.getValue();
			GuiItem item = entry.getKey();

			if (location.getKey() != newX || location.getValue() != newY ||
                !item.getItem().equals(event.getCurrentItem()))
				continue;

			if (!item.isVisible())
				return false;

			Consumer<InventoryClickEvent> action = item.getAction();

			if (action != null)
				action.accept(event);

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
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
	 * Fills all empty space in the pane with the given {@code itemStack}
	 *
	 * @param itemStack The {@link org.bukkit.inventory.ItemStack} to fill the empty space with
	 */
	@Contract("null -> fail")
	public void fillWith(@NotNull ItemStack itemStack) {
		//The non empty spots
		List<Map.Entry<Integer, Integer>> locations = this.items.stream()
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());

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
                    this.addItem(new GuiItem(itemStack), x, y);
                }
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	@Override
	public Collection<GuiItem> getItems() {
		return items.stream().map(Map.Entry::getKey).collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	@Contract(pure = true)
	@Override
	public Collection<Pane> getPanes() {
		return new HashSet<>();
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
	@Contract(pure = true)
    @Override
	public int getRotation() {
		return rotation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Contract(pure = true)
    @Override
	public boolean isFlippedHorizontally() {
		return flipHorizontally;
	}

	/**
	 * {@inheritDoc}
	 */
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
	@Contract("_, null -> fail")
	public static StaticPane load(@NotNull Object instance, @NotNull Element element) {
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

				staticPane.addItem(Pane.loadItem(instance, child), Integer.parseInt(child.getAttribute("x")),
                    Integer.parseInt(child.getAttribute("y")));
			}

			return staticPane;
		} catch (NumberFormatException exception) {
			throw new XMLLoadException(exception);
		}
	}
}