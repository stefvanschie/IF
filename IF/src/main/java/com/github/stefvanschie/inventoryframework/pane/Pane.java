package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.exception.XMLReflectionException;
import com.github.stefvanschie.inventoryframework.pane.util.Mask;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil;
import com.github.stefvanschie.inventoryframework.util.SkullUtil;
import com.github.stefvanschie.inventoryframework.util.UUIDTagType;
import com.github.stefvanschie.inventoryframework.util.XMLUtil;
import com.google.common.primitives.Primitives;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.UnsupportedOperationException;
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
    @Deprecated
    protected int x = 0, y = 0;

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
    protected Consumer<InventoryClickEvent> onClick;

    /**
     * A unique identifier for panes to locate them by
     */
    protected UUID uuid;

    /**
     * A map containing the mappings for properties for items
     */
    @NotNull
    private static final Map<String, Function<String, Object>> PROPERTY_MAPPINGS = new HashMap<>();

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

        //the length should be the length of the parent container, but we don't have that, so just use one
        this.x = slot.getX(1);
        this.y = slot.getY(1);
    }

    /**
     * Set the x coordinate of this pane
     *
     * @param x the new x coordinate
     */
    public void setX(int x) {
        this.x = x;

        this.slot = Slot.fromXY(x, getY());
    }

    /**
     * Set the y coordinate of this pane
     *
     * @param y the new y coordinate
     */
    public void setY(int y) {
        this.y = y;

        this.slot = Slot.fromXY(getX(), y);
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
     * Gets the x coordinate of this pane
     *
     * @return the x coordinate
     * @deprecated when the slot was specified as an indexed position, this may return the wrong value;
     *             {@link #getSlot()} should be used instead
     */
    @Contract(pure = true)
    @Deprecated
    public int getX() {
        return x;
    }

    /**
     * Gets the y coordinate of this pane
     *
     * @return the y coordinate
     * @deprecated when the slot was specified as an indexed position, this may return the wrong value;
     *             {@link #getSlot()} should be used instead
     */
    @Contract(pure = true)
    @Deprecated
    public int getY() {
        return y;
    }

    /**
     * Has to set all the items in the right spot inside the inventory
     *
     * @param inventoryComponent the inventory component in which the items should be displayed
     * @param paneOffsetX the pane's offset on the x axis
     * @param paneOffsetY the pane's offset on the y axis
     * @param maxLength the maximum length of the pane
     * @param maxHeight the maximum height of the pane
     */
    public abstract void display(@NotNull InventoryComponent inventoryComponent, int paneOffsetX, int paneOffsetY,
                                 int maxLength, int maxHeight);

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
     * @param inventoryComponent the inventory component in which this pane resides
     * @param event the event that occurred while clicking on this item
     * @param slot the slot that was clicked in
     * @param paneOffsetX the pane's offset on the x axis
     * @param paneOffsetY the pane's offset on the y axis
     * @param maxLength the maximum length of the pane
     * @param maxHeight the maximum height of the pane
     * @return whether the item was found or not
     */
    public abstract boolean click(@NotNull Gui gui, @NotNull InventoryComponent inventoryComponent,
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

    /**
     * Loads an item from an instance and an element
     *
     * @param instance the instance
     * @param element the element
     * @param plugin the plugin that will be the owner of the created item
     * @return the gui item
     * @see #loadItem(Object, Element)
     * @since 0.10.8
     */
    @NotNull
    @Contract(pure = true)
    public static GuiItem loadItem(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        String id = element.getAttribute("id");
        Material material = Material.matchMaterial(id.toUpperCase(Locale.getDefault()));

        if (material == null) {
            throw new XMLLoadException("Can't find material for '" + id + "'");
        }

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

                                TextHolder.deserialize(innerNode.getTextContent())
                                        .asItemLoreAtEnd(itemMeta);
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

                    TextHolder.deserialize(item.getTextContent())
                            .asItemDisplayName(itemMeta);

                    itemStack.setItemMeta(itemMeta);
                } else if (nodeName.equals("modeldata")) {
                    ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());

                    itemMeta.setCustomModelData(Integer.parseInt(item.getTextContent()));

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
                else if (parameterTypes[0].isAssignableFrom(InventoryClickEvent.class)) {
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
                                    parameterTypes[1 + i].isAssignableFrom(Primitives.unwrap(attribute.getClass()))) &&
                                    !parameterTypes[1 + i].isAssignableFrom(attribute.getClass()))
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

        GuiItem item = new GuiItem(itemStack, action, plugin);

        if (element.hasAttribute("field"))
            XMLUtil.loadFieldAttribute(instance, element, item);

        if (element.hasAttribute("populate")) {
            XMLUtil.invokeMethod(instance, element.getAttribute("populate"), item, GuiItem.class);
        }
		
		item.setProperties(properties);

        return item;
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
        return loadItem(instance, element, JavaPlugin.getProvidingPlugin(Pane.class));
    }

    public static void load(@NotNull Pane pane, @NotNull Object instance, @NotNull Element element) {
        pane.setSlot(Slot.deserialize(element));

        if (element.hasAttribute("priority"))
            pane.setPriority(Priority.valueOf(element.getAttribute("priority").toUpperCase()));

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
    public void setOnClick(@Nullable Consumer<InventoryClickEvent> onClick) {
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
                    ", for " + getClass().getSimpleName() + ", x=" + getX() + ", y=" + getY()
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
