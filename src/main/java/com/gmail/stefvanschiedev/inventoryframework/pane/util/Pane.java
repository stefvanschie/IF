package com.gmail.stefvanschiedev.inventoryframework.pane.util;

import com.gmail.stefvanschiedev.inventoryframework.GuiItem;
import com.gmail.stefvanschiedev.inventoryframework.GuiLocation;
import com.google.common.primitives.Primitives;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The base class for all panes.
 */
public abstract class Pane {

    /**
     * The starting position of this pane
     */
    protected GuiLocation start;

    /**
     * Length is horizontal, height is vertical
     */
    protected int length, height;

    /**
     * The visibility state of the pane
     */
    private boolean visible;

    /**
     * The tag assigned to this pane, null if no tag has been assigned
     */
    private String tag;

    /**
     * A map containing the mappings for attributes for items
     */
    private static final Map<String, Function<String, Object>> ATTRIBUTE_MAPPINGS = new HashMap<>();

    /**
     * Constructs a new default pane
     *
     * @param start the upper left corner of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     */
    protected Pane(@NotNull GuiLocation start, int length, int height) {
        assert start.getX() + length <= 9 : "length longer than maximum size";
        assert start.getY() + height <= 6 : "height longer than maximum size";

        this.start = start;

        this.length = length;
        this.height = height;

        this.visible = true;
    }

    /**
     * Returns the length of this pane
     *
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the height of this pane
     *
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Has to set all the items in the right spot inside the inventory
     *
     * @param inventory the inventory that the items should be displayed in
     */
    public abstract void display(Inventory inventory);

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
     * @param event the event that occurred while clicking on this item
     * @return whether the item was found or not
     */
    public abstract boolean click(@NotNull InventoryClickEvent event);

    /**
     * Returns a gui item by tag
     *
     * @param tag the tag to look for
     * @return the gui item
     */
    @Nullable
    public abstract GuiItem getItem(@NotNull String tag);

    /**
     * Returns the tag that belongs to this item, or null if no tag has been assigned
     *
     * @return the tag or null
     */
    @Nullable
    @Contract(pure = true)
    public String getTag() {
        return tag;
    }

    /**
     * Sets the tag of this item to the new tag or removes it when the parameter is null
     *
     * @param tag the new tag
     */
    public void setTag(@Nullable String tag) {
        this.tag = tag;
    }

    /**
     * Loads an item from an instance and an element
     *
     * @param instance the instance
     * @param element the element
     * @return the gui item
     */
    public static GuiItem loadItem(Object instance, Element element) {
        String id = element.getAttribute("id");
        ItemStack itemStack = new ItemStack(Material.matchMaterial(id.toUpperCase(Locale.getDefault())), 1,
                element.hasAttribute("damage") ? Short.parseShort(element.getAttribute("damage")) : 0);

        if (element.hasAttribute("displayName")) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            itemMeta.setDisplayName(element.getAttribute("displayName"));
            itemStack.setItemMeta(itemMeta);
        }

        if (element.hasAttribute("lores")) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            itemStack.setItemMeta(itemMeta);
        }

        String tag = null;

        if (element.hasAttribute("tag"))
            tag = element.getAttribute("tag");

        List<Object> attributes = new ArrayList<>();

        if (element.hasChildNodes()) {
            NodeList childNodes = element.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);

                if (item.getNodeType() != Node.ELEMENT_NODE || !item.getNodeName().equals("attributes"))
                    continue;

                Element attributeList = (Element) item;

                for (int j = 0; j < attributeList.getChildNodes().getLength(); j++) {
                    Node attributeNode = attributeList.getChildNodes().item(j);

                    if (attributeNode.getNodeType() != Node.ELEMENT_NODE ||
                            !attributeNode.getNodeName().equals("attribute"))
                        continue;

                    Element attribute = (Element) attributeNode;
                    String attributeType;

                    if (!attribute.hasAttribute("type"))
                        attributeType = "string";
                    else
                        attributeType = attribute.getAttribute("type");

                    attributes.add(ATTRIBUTE_MAPPINGS.get(attributeType).apply(attribute.getTextContent()));
                }
            }
        }

        Consumer<InventoryClickEvent> action = null;

        if (element.hasAttribute("onClick")) {
            for (Method method : instance.getClass().getMethods()) {
                if (!method.getName().equals(element.getAttribute("onClick")))
                    continue;

                int parameterCount = method.getParameterCount();
                Class<?>[] parameterTypes = method.getParameterTypes();

                if (parameterCount == 0)
                    action = event -> {
                        try {
                            //because reflection with lambdas is stupid
                            method.setAccessible(true);
                            method.invoke(instance);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    };
                else if (InventoryClickEvent.class.isAssignableFrom(parameterTypes[0]) ||
                    Cancellable.class.isAssignableFrom(parameterTypes[0])) {
                    if (parameterCount == 1)
                        action = event -> {
                            try {
                                //because reflection with lambdas is stupid
                                method.setAccessible(true);
                                method.invoke(instance, event);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        };
                    else if (parameterCount == attributes.size() + 1) {
                        boolean correct = true;

                        for (int i = 0; i < attributes.size(); i++) {
                            Object attribute = attributes.get(i);

                            if (!(parameterTypes[1 + i].isPrimitive() &&
                                    Primitives.unwrap(attribute.getClass()).isAssignableFrom(parameterTypes[1 + i])) &&
                                    !attribute.getClass().isAssignableFrom(parameterTypes[1 + i]))
                                correct = false;
                        }

                        if (correct) {
                            action = event -> {
                                try {
                                    //don't ask me why we need to do this, just roll with it (actually I do know why, but it's stupid)
                                    attributes.add(0, event);

                                    //because reflection with lambdas is stupid
                                    method.setAccessible(true);
                                    method.invoke(instance, attributes.toArray(new Object[0]));
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            };
                        }
                    }
                }

                break;
            }
        }

        GuiItem item = action == null ? new GuiItem(itemStack) : new GuiItem(itemStack, action);
        item.setTag(tag);

        return item;
    }

    /**
     * Returns the attribute mappings used when loading attributes from an XML file.
     *
     * @return the attribute mappings
     */
    @NotNull
    @Contract(pure = true)
    public static Map<String, Function<String, Object>> getAttributeMappings() {
        return ATTRIBUTE_MAPPINGS;
    }

    static {
        ATTRIBUTE_MAPPINGS.put("boolean", Boolean::parseBoolean);
        ATTRIBUTE_MAPPINGS.put("byte", Byte::parseByte);
        ATTRIBUTE_MAPPINGS.put("character", value -> value.charAt(0));
        ATTRIBUTE_MAPPINGS.put("double", Double::parseDouble);
        ATTRIBUTE_MAPPINGS.put("float", Float::parseFloat);
        ATTRIBUTE_MAPPINGS.put("integer", Integer::parseInt);
        ATTRIBUTE_MAPPINGS.put("long", Long::parseLong);
        ATTRIBUTE_MAPPINGS.put("short", Short::parseShort);
        ATTRIBUTE_MAPPINGS.put("string", value -> value);
    }
}