package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.exception.XMLReflectionException;
import com.github.stefvanschie.inventoryframework.util.SkullUtil;
import com.github.stefvanschie.inventoryframework.util.XMLUtil;
import com.google.common.primitives.Primitives;
import me.ialistannen.mininbt.ItemNBTUtil;
import me.ialistannen.mininbt.NBTWrappers;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
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
     * The starting position of this pane, which is 0 by default
     */
    protected int x = 0, y = 0;

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
     * The consumer that will be called once a players clicks in the gui
     */
    @Nullable
    protected Consumer<InventoryClickEvent> onClick;

    /**
     * A map containing the mappings for properties for items
     */
    @NotNull
    private static final Map<String, Function<String, Object>> PROPERTY_MAPPINGS = new HashMap<>();

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
        this.x = x;
        this.y = y;

        this.length = length;
        this.height = height;

        this.priority = priority;
        this.visible = true;
    }

    /**
     * Constructs a new default pane, with no position
     *
     * @param length the length of the pane
     * @param height the height of the pane
     */
    protected Pane(int length, int height) {
        this.length = length;
        this.height = height;

        this.priority = Priority.NORMAL;
        this.visible = true;
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
     * Set the x coordinate of this pane
     *
     * @param x the new x coordinate
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Set the y coordinate of this pane
     *
     * @param y the new y coordinate
     */
    public void setY(int y) {
        this.y = y;
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
     * Gets the x coordinate of this pane
     *
     * @return the x coordinate
     */
    @Contract(pure = true)
    public int getX() {
        return x;
    }

    /**
     * Gets the y coordinate of this pane
     *
     * @return the y coordinate
     */
    @Contract(pure = true)
    public int getY() {
        return y;
    }

    /**
     * Has to set all the items in the right spot inside the inventory
     *
     * @param gui the gui for which we're rendering
     * @param inventory the inventory that the items should be displayed in
     * @param playerInventory the player's inventory
     * @param paneOffsetX the pane's offset on the x axis
     * @param paneOffsetY the pane's offset on the y axis
     * @param maxLength the maximum length of the pane
     * @param maxHeight the maximum height of the pane
     */
    public abstract void display(@NotNull Gui gui, @NotNull Inventory inventory, @NotNull PlayerInventory playerInventory,
                                 int paneOffsetX, int paneOffsetY, int maxLength, int maxHeight);

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
     * @param gui the gui this event stems from
     * @param event the event that occurred while clicking on this item
     * @param paneOffsetX the pane's offset on the x axis
     * @param paneOffsetY the pane's offset on the y axis
     * @param maxLength the maximum length of the pane
     * @param maxHeight the maximum height of the pane
     * @return whether the item was found or not
     */
    public abstract boolean click(@NotNull Gui gui, @NotNull InventoryClickEvent event, int paneOffsetX,
                                  int paneOffsetY, int maxLength, int maxHeight);

    /**
     * Sets the priority of this pane
     *
     * @param priority the priority
     */
    public void setPriority(@NotNull Priority priority) {
        this.priority = priority;
    }

    /**
     * Loads an item from an instance and an element
     *
     * @param instance the instance
     * @param element the element
     * @return the gui item
     */
    @NotNull
    @Contract(pure = true)
    public static GuiItem loadItem(@NotNull Object instance, @NotNull Element element) {
        String id = element.getAttribute("id");
        Material material = Objects.requireNonNull(Material.matchMaterial(id.toUpperCase(Locale.getDefault())));
        boolean hasAmount = element.hasAttribute("amount");
        boolean hasDamage = element.hasAttribute("damage");
        int amount = hasAmount ? Integer.parseInt(element.getAttribute("amount")) : 1;
        short damage = hasDamage ? Short.parseShort(element.getAttribute("damage")) : 0;

        //noinspection deprecation
        ItemStack itemStack = new ItemStack(material, amount, damage);

        List<Object> properties = new ArrayList<>();

        if (element.hasChildNodes()) {
            NodeList childNodes = element.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);

                if (item.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                Element elementItem = (Element) item;

                String nodeName = item.getNodeName();

                if (nodeName.equals("properties") || nodeName.equals("lore") || nodeName.equals("enchantments")) {
                    Element innerElement = (Element) item;
                    NodeList innerChildNodes = innerElement.getChildNodes();

                    for (int j = 0; j < innerChildNodes.getLength(); j++) {
                        Node innerNode = innerChildNodes.item(j);

                        if (innerNode.getNodeType() != Node.ELEMENT_NODE)
                            continue;

                        Element innerElementChild = (Element) innerNode;
                        ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());

                        switch (nodeName) {
                            case "properties":
                                if (!innerNode.getNodeName().equals("property"))
                                    continue;

                                String propertyType = innerElementChild.hasAttribute("type")
                                        ? innerElementChild.getAttribute("type")
                                        : "string";

                                properties.add(PROPERTY_MAPPINGS.get(propertyType).apply(innerElementChild
                                        .getTextContent()));
                                break;
                            case "lore":
                                if (!innerNode.getNodeName().equals("line"))
                                    continue;

                                boolean hasLore = itemMeta.hasLore();
                                List<String> lore = hasLore ? Objects.requireNonNull(itemMeta.getLore()) : new ArrayList<>();

                                lore.add(ChatColor.translateAlternateColorCodes('&', innerNode
                                        .getTextContent()));
                                itemMeta.setLore(lore);
                                itemStack.setItemMeta(itemMeta);
                                break;
                            case "enchantments":
                                if (!innerNode.getNodeName().equals("enchantment"))
                                    continue;

                                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(
                                    innerElementChild.getAttribute("id").toUpperCase(Locale.getDefault())
                                ));

                                if (enchantment == null) {
                                    throw new XMLLoadException("Enchantment cannot be found");
                                }

                                int level = Integer.parseInt(innerElementChild.getAttribute("level"));

                                itemMeta.addEnchant(enchantment, level, true);
                                itemStack.setItemMeta(itemMeta);
                                break;
                        }
                    }
                } else if (nodeName.equals("displayname")) {
                    ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());

                    itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', item
                            .getTextContent()));

                    itemStack.setItemMeta(itemMeta);
                } else if (nodeName.equals("skull") && itemStack.getItemMeta() instanceof SkullMeta) {
                    SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

                    if (elementItem.hasAttribute("owner"))
                        //noinspection deprecation
                        skullMeta.setOwner(elementItem.getAttribute("owner"));
                    else if (elementItem.hasAttribute("id")) {
                        SkullUtil.setSkull(skullMeta, elementItem.getAttribute("id"));
                    }

                    itemStack.setItemMeta(skullMeta);
                }
            }
        }

        Consumer<InventoryClickEvent> action = null;

        if (element.hasAttribute("onClick")) {
            String methodName = element.getAttribute("onClick");
            for (Method method : instance.getClass().getMethods()) {
                if (!method.getName().equals(methodName))
                    continue;

                int parameterCount = method.getParameterCount();
                Class<?>[] parameterTypes = method.getParameterTypes();

                if (parameterCount == 0)
                    action = event -> {
                        try {
                            //because reflection with lambdas is stupid
                            method.setAccessible(true);
                            method.invoke(instance);
                        } catch (IllegalAccessException | InvocationTargetException exception) {
                            throw new XMLReflectionException(exception);
                        }
                    };
                else if (InventoryClickEvent.class.isAssignableFrom(parameterTypes[0])) {
                    if (parameterCount == 1)
                        action = event -> {
                            try {
                                //because reflection with lambdas is stupid
                                method.setAccessible(true);
                                method.invoke(instance, event);
                            } catch (IllegalAccessException | InvocationTargetException exception) {
                                throw new XMLReflectionException(exception);
                            }
                        };
                    else if (parameterCount == properties.size() + 1) {
                        boolean correct = true;

                        for (int i = 0; i < properties.size(); i++) {
                            Object attribute = properties.get(i);

                            if (!(parameterTypes[1 + i].isPrimitive() &&
                                    Primitives.unwrap(attribute.getClass()).isAssignableFrom(parameterTypes[1 + i])) &&
                                    !attribute.getClass().isAssignableFrom(parameterTypes[1 + i]))
                                correct = false;
                        }

                        if (correct) {
                            action = event -> {
                                try {
                                    //don't ask me why we need to do this, just roll with it (actually I do know why, but it's stupid)
                                    properties.add(0, event);

                                    //because reflection with lambdas is stupid
                                    method.setAccessible(true);
                                    method.invoke(instance, properties.toArray(new Object[0]));

                                    //since we'll append the event to the list next time again, we need to remove it here again
                                    properties.remove(0);
                                } catch (IllegalAccessException | InvocationTargetException exception) {
                                    throw new XMLReflectionException(exception);
                                }
                            };
                        }
                    }
                }

                break;
            }
        }

        GuiItem item = new GuiItem(itemStack, action);

        if (element.hasAttribute("field"))
            XMLUtil.loadFieldAttribute(instance, element, item);

        if (element.hasAttribute("populate")) {
            try {
                MethodUtils.invokeExactMethod(instance, "populate", item, GuiItem.class);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
                throw new XMLLoadException(exception);
            }
        }

        return item;
    }

    public static void load(@NotNull Pane pane, @NotNull Object instance, @NotNull Element element) {
        if (element.hasAttribute("x")) {
            pane.setX(Integer.parseInt(element.getAttribute("x")));
        }

        if (element.hasAttribute("y")) {
            pane.setY(Integer.parseInt(element.getAttribute("y")));
        }

        if (element.hasAttribute("priority"))
            pane.setPriority(Priority.valueOf(element.getAttribute("priority")));

        if (element.hasAttribute("visible"))
            pane.setVisible(Boolean.parseBoolean(element.getAttribute("visible")));

        if (element.hasAttribute("field"))
            XMLUtil.loadFieldAttribute(instance, element, pane);

        if (element.hasAttribute("onClick"))
            pane.setOnClick(XMLUtil.loadOnEventAttribute(instance, element, InventoryClickEvent.class, "onClick"));

        if (element.hasAttribute("populate")) {
            String attribute = element.getAttribute("populate");
            for (Method method: instance.getClass().getMethods()) {
                if (!method.getName().equals(attribute))
                    continue;

                try {
                    method.setAccessible(true);
                    method.invoke(instance, pane);
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    throw new XMLLoadException(exception);
                }
            }
        }
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
        return items.stream().filter(guiItem -> {
            NBTWrappers.NBTTagCompound tag = ItemNBTUtil.getTag(item);

            if (tag == null) {
                return false;
            }

            String stringUUID = tag.getString("IF-uuid");

            return stringUUID != null && guiItem.getUUID().equals(UUID.fromString(stringUUID));
        }).findAny().orElse(null);
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
     * Set the consumer that should be called whenever this gui is clicked in.
     *
     * @param onClick the consumer that gets called
     * @since 0.4.0
     */
    public void setOnClick(@Nullable Consumer<InventoryClickEvent> onClick) {
        this.onClick = onClick;
    }

    /**
     * Set the consumer that should be called whenever this gui is clicked in.
     *
     * @param onLocalClick the consumer that gets called
     * @deprecated see {@link #setOnClick(Consumer)}
     */
    @Deprecated
    public void setOnLocalClick(@Nullable Consumer<InventoryClickEvent> onLocalClick) {
        this.onClick = onLocalClick;
    }

    /**
     * Registers a property that can be used inside an XML file to add additional new properties.
     * The use of {@link Gui#registerProperty(String, Function)} is preferred over this method.
     *
     * @param attributeName the name of the property. This is the same name you'll be using to specify the property
     *                      type in the XML file.
     * @param function how the property should be processed. This converts the raw text input from the XML node value
     *                 into the correct object type.
     * @throws IllegalArgumentException when a property with this name is already registered.
     */
    public static void registerProperty(@NotNull String attributeName, @NotNull Function<String, Object> function) {
        if (PROPERTY_MAPPINGS.containsKey(attributeName)) {
            throw new IllegalArgumentException("property '" + attributeName + "' is already registered");
        }
    
        PROPERTY_MAPPINGS.put(attributeName, function);
    }

    /**
     * Returns the property mappings used when loading properties from an XML file.
     *
     * @deprecated use {@link Gui#registerProperty(String, Function)} instead
     * @return the property mappings
     */
    @NotNull
    @Contract(pure = true)
    @Deprecated
    public static Map<String, Function<String, Object>> getPropertyMappings() {
        return PROPERTY_MAPPINGS;
    }

    /**
     * An enum representing the rendering priorities for the panes. Uses a similar system to Bukkit's
     * {@link org.bukkit.event.EventPriority} system
     */
    public enum Priority {

        /**
         * The lowest priority, will be rendered first
         */
        LOWEST,

        /**
         * A low priority, lower than default
         */
        LOW,

        /**
         * A normal priority, the default
         */
        NORMAL,

        /**
         * A higher priority, higher than default
         */
        HIGH,

        /**
         * The highest priority for production use
         */
        HIGHEST,

        /**
         * The highest priority, will always be called last, should not be used for production code
         */
        MONITOR
    }

    static {
        PROPERTY_MAPPINGS.put("boolean", Boolean::parseBoolean);
        PROPERTY_MAPPINGS.put("byte", Byte::parseByte);
        PROPERTY_MAPPINGS.put("character", value -> value.charAt(0));
        PROPERTY_MAPPINGS.put("double", Double::parseDouble);
        PROPERTY_MAPPINGS.put("float", Float::parseFloat);
        PROPERTY_MAPPINGS.put("integer", Integer::parseInt);
        PROPERTY_MAPPINGS.put("long", Long::parseLong);
        PROPERTY_MAPPINGS.put("short", Short::parseShort);
        PROPERTY_MAPPINGS.put("string", value -> value);
    }
}
