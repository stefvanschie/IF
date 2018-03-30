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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A pane for items that should be outlined
 */
public class OutlinePane extends Pane {

    /**
     * A set of items inside this pane
     */
    private final GuiItem[] items;

    /**
     * Constructs a new default pane
     *
     * @param start  the upper left corner of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     */
    public OutlinePane(@NotNull GuiLocation start, int length, int height) {
        super(start, length, height);

        this.items = new GuiItem[length * height];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void display(@NotNull Inventory inventory) {
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < height; y++) {
                if (items[y * length + x] == null || !items[y * length + x].isVisible())
                    continue;

                ItemStack item = items[y * length + x].getItem();

                if (item.getType() == Material.AIR)
                    continue;

                inventory.setItem((start.getY() + y) * 9 + (start.getX() + x), item);
            }
        }
    }

    /**
     * Adds a gui item at the specific spot in the pane
     *
     * @param item the item to set
     */
    public void addItem(@NotNull GuiItem item) {
        int openIndex = -1;

        int length = items.length;
        for (int i = 0; i < length; i++) {
            if (items[i] == null) {
                openIndex = i;
                break;
            }
        }

        if (openIndex == -1)
            return;

        items[openIndex] = item;
    }

    /**
     * Insert an item at the specified index
     *
     * @param item the item to add
     * @param position the position to insert it into
     */
    public void insertItem(@NotNull GuiItem item, int position) {
        if (items[position] != null)
            System.arraycopy(items, position, items, position + 1, (int) Stream.of(items).skip(position)
                .filter(Objects::nonNull).count());

        items[position] = item;
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
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public GuiItem getItem(@NotNull String tag) {
        return Stream.of(items)
                .filter(item -> item != null && tag.equals(item.getTag()))
                .findAny()
                .orElse(null);
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
    public static OutlinePane load(Object instance, @NotNull Element element) {
        try {
            OutlinePane outlinePane = new OutlinePane(new GuiLocation(
                Integer.parseInt(element.getAttribute("x")),
                Integer.parseInt(element.getAttribute("y"))),
                Integer.parseInt(element.getAttribute("length")),
                Integer.parseInt(element.getAttribute("height"))
            );

            if (element.hasAttribute("onClick")) {
                for (Method method : instance.getClass().getMethods()) {
                    if (!method.getName().equals(element.getAttribute("onClick")))
                        continue;

                    int parameterCount = method.getParameterCount();

                    if (parameterCount == 0) {
                        outlinePane.setOnClick(event -> {
                            try {
                                method.setAccessible(true);
                                method.invoke(instance);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        });
                    } else if (parameterCount == 1 &&
                            InventoryClickEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        outlinePane.setOnClick(event -> {
                            try {
                                method.setAccessible(true);
                                method.invoke(instance, event);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }

            if (element.hasAttribute("tag"))
                outlinePane.setTag(element.getAttribute("tag"));

            if (element.hasAttribute("visible"))
                outlinePane.setVisible(Boolean.parseBoolean(element.getAttribute("visible")));

            if (element.hasAttribute("populate")) {
                for (Method method : instance.getClass().getMethods()) {
                    if (!method.getName().equals(element.getAttribute("populate")))
                        continue;

                    if (method.getParameterCount() == 1 &&
                        OutlinePane.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        method.setAccessible(true);
                        method.invoke(instance, outlinePane);
                    }
                }

                return outlinePane;
            }

            NodeList childNodes = element.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);

                if (item.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                if (item.getNodeName().equals("empty"))
                    outlinePane.addItem(new GuiItem(new ItemStack(Material.AIR)));
                else
                    outlinePane.addItem(Pane.loadItem(instance, (Element) item));
            }

            return outlinePane;
        } catch (NumberFormatException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }
}