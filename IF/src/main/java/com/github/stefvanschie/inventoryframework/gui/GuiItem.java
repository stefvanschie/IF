package com.github.stefvanschie.inventoryframework.gui;

import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.exception.XMLReflectionException;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil;
import com.github.stefvanschie.inventoryframework.util.SkullUtil;
import com.github.stefvanschie.inventoryframework.util.UUIDTagType;
import com.github.stefvanschie.inventoryframework.util.XMLUtil;
import com.google.common.primitives.Primitives;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

/**
 * An item for in an inventory
 */
public class GuiItem {

    /**
     * A map containing the mappings for properties for items
     */
    @NotNull
    private static final Map<String, Function<? super String, ?>> PROPERTY_MAPPINGS = new HashMap<>();
    /**
     * The logger to log errors with
     */
    @NotNull
    private final Logger logger;

    /**
     * The {@link NamespacedKey} that specifies the location of the (internal) {@link UUID} in {@link PersistentDataContainer}s.
     * The {@link PersistentDataType} that should be used is {@link UUIDTagType}.
     */
    @NotNull
    private final NamespacedKey keyUUID;

    /**
     * An action for the inventory
     */
    @Nullable
    private Consumer<? super InventoryClickEvent> action;
    
    /**
     * List of item's properties
     */
    @NotNull
    private List<Object> properties;

    /**
     * The items shown
     */
    @NotNull
    private ItemStack item;

    /**
     * Whether this item is visible or not
     */
    private boolean visible;

    /**
     * Internal UUID for keeping track of this item
     */
    @NotNull
    private UUID uuid = UUID.randomUUID();

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     * @param action the action called whenever an interaction with this item happens
     * @param plugin the owning plugin of this item
     * @see #GuiItem(ItemStack, Consumer)
     * @since 0.10.8
     */
    public GuiItem(@NotNull ItemStack item, @Nullable Consumer<? super InventoryClickEvent> action,
                   @NotNull Plugin plugin) {
        this(item, action, plugin.getLogger(), new NamespacedKey(plugin, "IF-uuid"));
    }

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     * @param plugin the owning plugin of this item
     * @see #GuiItem(ItemStack)
     * @since 0.10.8
     */
    public GuiItem(@NotNull ItemStack item, @NotNull Plugin plugin) {
        this(item, event -> {}, plugin);
    }

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     * @param action the action called whenever an interaction with this item happens
     */
    public GuiItem(@NotNull ItemStack item, @Nullable Consumer<? super InventoryClickEvent> action) {
        this(item, action, JavaPlugin.getProvidingPlugin(GuiItem.class));
    }

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     */
    public GuiItem(@NotNull ItemStack item) {
        this(item, event -> {});
    }

    /**
     * Creates a new gui item based on the given item, action, logger, and key. The logger will be used for logging
     * exceptions and the key is used for identification of this item.
     *
     * @param item the item stack
     * @param action the action called whenever an interaction with this item happens
     * @param logger the logger used for logging exceptions
     * @param key the key to identify this item with
     * @since 0.10.10
     */
    private GuiItem(@NotNull ItemStack item, @Nullable Consumer<? super InventoryClickEvent> action,
                    @NotNull Logger logger, @NotNull NamespacedKey key) {
        this.logger = logger;
        this.keyUUID = key;
        this.action = action;
        this.visible = true;
        this.properties = new ArrayList<>();
        this.item = item;
    }

    /**
     * Makes a copy of this gui item and returns it. This makes a deep copy of the gui item. This entails that the
     * underlying item will be copied as per their {@link ItemStack#clone()} and miscellaneous data will be copied in
     * such a way that they are identical. The returned gui item will never be reference equal to the current gui item.
     *
     * @return a copy of the gui item
     * @since 0.6.2
     */
    @NotNull
    @Contract(pure = true)
    public GuiItem copy() {
        GuiItem guiItem = new GuiItem(item.clone(), action, this.logger, this.keyUUID);

        guiItem.visible = visible;
        guiItem.uuid = uuid;
        guiItem.properties = new ArrayList<>(properties);

        return guiItem;
    }

    /**
     * Calls the handler of the {@link InventoryClickEvent}
     * if such a handler was specified in the constructor.
     * Catches and logs all exceptions the handler might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    public void callAction(@NotNull InventoryClickEvent event) {
        if (action == null) {
            return;
        }

        try {
            action.accept(event);
        } catch (Throwable t) {
            this.logger.log(Level.SEVERE, "Exception while handling click event in inventory '"
                    + InventoryViewUtil.getInstance().getTitle(event.getView()) + "', slot=" + event.getSlot() +
                    ", item=" + item.getType(), t);
        }
    }

    /**
     * Sets the internal UUID of this gui item onto the underlying item. Previously set UUID will be overwritten by the
     * current UUID. If the underlying item does not have an item meta, this method will silently do nothing.
     *
     * @since 0.9.3
     */
    public void applyUUID() {
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.getPersistentDataContainer().set(this.keyUUID, UUIDTagType.INSTANCE, uuid);
            item.setItemMeta(meta);
        }
    }

    /**
     * Overwrites the current item with the provided item.
     *
     * @param item the item to set
     * @since 0.10.8
     */
    public void setItem(@NotNull ItemStack item) {
        this.item = item;
    }

    /**
     * Sets the action to be executed when a human entity clicks on this item.
     *
     * @param action the action of this item
     * @since 0.7.1
     */
    public void setAction(@NotNull Consumer<InventoryClickEvent> action) {
        this.action = action;
    }
    
    /**
     * Returns the list of properties
     *
     * @return the list of properties that belong to this gui item
     * @since 0.7.2
     */
    @NotNull
    @Contract(pure = true)
    public List<Object> getProperties(){
        return properties;
    }
    
    /**
     * Sets the list of properties for this gui item
     *
     * @param properties list of new properties
     * @since 0.7.2
     */
    public void setProperties(@NotNull List<Object> properties){
        this.properties = properties;
    }

    /**
     * Returns the item
     *
     * @return the item that belongs to this gui item
     */
    @NotNull
    @Contract(pure = true)
    public ItemStack getItem() {
        return item;
    }

    /**
     * Gets the namespaced key used for this item.
     *
     * @return the namespaced key
     * @since 0.10.8
     */
    @NotNull
    @Contract(pure = true)
    public NamespacedKey getKey() {
        return keyUUID;
    }

    /**
     * Gets the {@link UUID} associated with this {@link GuiItem}. This is for internal use only, and should not be
     * used.
     *
     * @return the {@link UUID} of this item
     * @since 0.5.9
     */
    @NotNull
    @Contract(pure = true)
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Returns whether or not this item is visible
     *
     * @return true if this item is visible, false otherwise
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the visibility of this item to the new visibility
     *
     * @param visible the new visibility
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Loads an item from an instance and an element
     *
     * @param instance the instance
     * @param element the element
     * @param plugin the plugin that will be the owner of the created item
     * @return the gui item
     * @see #loadItem(Object, Element)
     * @since 0.12.0
     */
    @NotNull
    @Contract(pure = true)
    public static GuiItem loadItem(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        String id = element.getAttribute("id");
        Material material = Material.matchMaterial(id.toUpperCase(Locale.getDefault()));

        if (material == null) {
            throw new XMLLoadException("Can't find material for '" + id + "'");
        }

        boolean hasDamage = element.hasAttribute("damage");
        int amount = 1;

        if (element.hasAttribute("amount")) {
            try {
                amount = Integer.parseInt(element.getAttribute("amount"));
            } catch (NumberFormatException exception) {
                throw new XMLLoadException("Amount attribute is not an integer", exception);
            }
        }

        short damage = 0;

        if (element.hasAttribute("damage")) {
            try {
                amount = Short.parseShort(element.getAttribute("damage"));
            } catch (NumberFormatException exception) {
                throw new XMLLoadException("Damage attribute is not a short", exception);
            }
        }

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

                                Function<? super String, ?> mapping = PROPERTY_MAPPINGS.get(propertyType);

                                if (mapping == null) {
                                    throw new XMLLoadException("Specified property type is not registered");
                                }

                                properties.add(mapping.apply(innerElementChild.getTextContent()));
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

                                if (!innerElementChild.hasAttribute("id")) {
                                    throw new XMLLoadException("Enchantment tag does not have mandatory id attribute");
                                }

                                Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(
                                        innerElementChild.getAttribute("id").toUpperCase(Locale.getDefault())
                                ));

                                if (enchantment == null) {
                                    throw new XMLLoadException("Enchantment cannot be found");
                                }

                                if (!element.hasAttribute("level")) {
                                    throw new XMLLoadException(
                                            "Enchantment tag does not have mandatory level attribute"
                                    );
                                }

                                int level;

                                try {
                                    level = Integer.parseInt(innerElementChild.getAttribute("level"));
                                } catch (NumberFormatException exception) {
                                    throw new XMLLoadException("Level attribute is not an integer", exception);
                                }

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

                    try {
                        itemMeta.setCustomModelData(Integer.parseInt(item.getTextContent()));
                    } catch (NumberFormatException exception) {
                        throw new XMLLoadException("Modeldata tag does not contain an integer", exception);
                    }

                    itemStack.setItemMeta(itemMeta);
                } else if (nodeName.equals("skull") && itemStack.getItemMeta() instanceof SkullMeta) {
                    SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

                    if (elementItem.hasAttribute("owner"))
                        //noinspection deprecation
                        skullMeta.setOwner(elementItem.getAttribute("owner"));
                    else if (elementItem.hasAttribute("id")) {
                        SkullUtil.setSkull(skullMeta, elementItem.getAttribute("id"));
                    } else {
                        throw new XMLLoadException("Skull tag has neither an owner, nor an id attribute");
                    }

                    itemStack.setItemMeta(skullMeta);
                } else {
                    throw new XMLLoadException("Unknown node " + nodeName);
                }
            }
        }

        Consumer<InventoryClickEvent> action = null;

        if (element.hasAttribute("onClick")) {
            String methodName = element.getAttribute("onClick");

            boolean found = false;

            for (Method method : instance.getClass().getMethods()) {
                if (!method.getName().equals(methodName))
                    continue;

                int parameterCount = method.getParameterCount();
                Class<?>[] parameterTypes = method.getParameterTypes();

                if (parameterCount == 0) {
                    action = event -> {
                        try {
                            //because reflection with lambdas is stupid
                            method.setAccessible(true);
                            method.invoke(instance);
                        } catch (IllegalAccessException | InvocationTargetException exception) {
                            throw new XMLReflectionException(exception);
                        }
                    };
                    found = true;
                } else if (parameterTypes[0].isAssignableFrom(InventoryClickEvent.class)) {
                    if (parameterCount == 1) {
                        action = event -> {
                            try {
                                //because reflection with lambdas is stupid
                                method.setAccessible(true);
                                method.invoke(instance, event);
                            } catch (IllegalAccessException | InvocationTargetException exception) {
                                throw new XMLReflectionException(exception);
                            }
                        };
                        found = true;
                    } else if (parameterCount == properties.size() + 1) {
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
                            found = true;
                        }
                    }
                }

                break;
            }

            if (!found) {
                throw new XMLLoadException("Specified method could not be found");
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
     * @since 0.12.0
     */
    @NotNull
    @Contract(pure = true)
    public static GuiItem loadItem(@NotNull Object instance, @NotNull Element element) {
        return loadItem(instance, element, JavaPlugin.getProvidingPlugin(Pane.class));
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
     * @since 0.12.0
     */
    public static void registerProperty(@NotNull String attributeName, @NotNull Function<? super String, ?> function) {
        if (PROPERTY_MAPPINGS.containsKey(attributeName)) {
            throw new IllegalArgumentException("property '" + attributeName + "' is already registered");
        }

        PROPERTY_MAPPINGS.put(attributeName, function);
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
