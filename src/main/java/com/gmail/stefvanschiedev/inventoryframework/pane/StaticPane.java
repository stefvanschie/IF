package com.gmail.stefvanschiedev.inventoryframework.pane;

import com.gmail.stefvanschiedev.inventoryframework.GuiItem;
import com.gmail.stefvanschiedev.inventoryframework.GuiLocation;
import com.gmail.stefvanschiedev.inventoryframework.pane.util.Pane;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.function.Consumer;

/**
 * A pane for static items and stuff. All items will have to be specified a slot, or will be added in the next position.
 */
public class StaticPane extends Pane {

    /**
     * A set of items inside this pane
     */
    private final GuiItem[] items;

    /**
     * Constructs a new default pane
     *
     * @param start the upper left corner of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     */
    public StaticPane(@NotNull GuiLocation start, int length, int height) {
        super(start, length, height);

        this.items = new GuiItem[length * height];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void display(@NotNull Inventory inventory, int paneOffsetX, int paneOffsetY) {
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < height; y++) {
                if (items[y * length + x] == null || !items[y * length + x].isVisible())
                    continue;

                ItemStack item = items[y * length + x].getItem();

                if (item.getType() == Material.AIR || x + paneOffsetX > 9 || y + paneOffsetY > 6)
                    continue;

                inventory.setItem((start.getY() + y + paneOffsetY) * 9 + (start.getX() + x + paneOffsetX), item);
            }
        }
    }

    /**
     * Adds a gui item at the specific spot in the pane
     *
     * @param item the item to set
     * @param location the location of the item
     */
    public void addItem(@NotNull GuiItem item, @NotNull GuiLocation location) {
        items[location.getY() * length + location.getX()] = item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean click(@NotNull InventoryClickEvent event) {
        int slot = event.getSlot();

        //correct coordinates
        int x = (slot % 9) - start.getX();
        int y = (slot / 9) - start.getY();

        if (y * length + x < 0 || y * length + x >= items.length)
            return false;

        if (onClick != null)
            onClick.accept(event);

        if (items[y * length + x] == null)
            return false;

        if (items[y * length + x].getItem().equals(event.getCurrentItem())) {
            Consumer<InventoryClickEvent> action = items[y * length + x].getAction();

            if (action != null)
                action.accept(event);

            return true;
        }

        return false;
    }

    /**
     * Loads an outline pane from a given element
     *
     * @param instance the instance class
     * @param element the element
     * @return the outline pane
     */
    @Nullable
    @Contract("_, null -> fail")
    public static StaticPane load(Object instance, @NotNull Element element) {
        try {
            StaticPane staticPane = new StaticPane(new GuiLocation(
                Integer.parseInt(element.getAttribute("x")),
                Integer.parseInt(element.getAttribute("y"))),
                Integer.parseInt(element.getAttribute("length")),
                Integer.parseInt(element.getAttribute("height"))
            );

            Pane.load(staticPane, instance, element);

            if (element.hasAttribute("populate"))
                return staticPane;

            NodeList childNodes = element.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);

                if (item.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                Element child = (Element) item;

                staticPane.addItem(Pane.loadItem(instance, child),
                    new GuiLocation(Integer.parseInt(child.getAttribute("x")),
                        Integer.parseInt(child.getAttribute("y"))));
            }

            return staticPane;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return null;
    }
}