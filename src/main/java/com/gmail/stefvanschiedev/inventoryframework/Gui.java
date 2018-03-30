package com.gmail.stefvanschiedev.inventoryframework;

import com.gmail.stefvanschiedev.inventoryframework.pane.OutlinePane;
import com.gmail.stefvanschiedev.inventoryframework.pane.PaginatedPane;
import com.gmail.stefvanschiedev.inventoryframework.pane.StaticPane;
import com.gmail.stefvanschiedev.inventoryframework.pane.util.Pane;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The base class of all GUIs
 *
 * @since 5.6.0
 */
public class Gui implements Listener, InventoryHolder {

    /**
     * A set of all panes in this inventory
     */
    private final List<Pane> panes;

    /**
     * The inventory of this gui
     */
    private final Inventory inventory;

    /**
     * The consumer that will be called once a players clicks in the gui
     */
    private Consumer<InventoryClickEvent> onClick;

    /**
     * The consumer that will be called once a player closes the gui
     */
    private Consumer<InventoryCloseEvent> onClose;

    /**
     * Constructs a new GUI
     *
     * @param rows the amount of rows this gui should contain
     * @param title the title/name of this gui
     */
    protected Gui(Plugin plugin, int rows, String title) {
        assert rows >= 1 && rows <= 6 : "amount of rows outside range";

        this.panes = new ArrayList<>();
        this.inventory = Bukkit.createInventory(this, rows * 9, title);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Adds a pane to this gui
     *
     * @param pane the pane to add
     * @since 5.6.0
     */
    protected void addPane(Pane pane) {
        this.panes.add(pane);
    }

    /**
     * Returns a gui item by tag
     *
     * @param tag the tag to look for
     * @return the gui item
     * @since 5.6.0
     */
    @Nullable
    @Contract(pure = true)
    public GuiItem getItem(@NotNull String tag) {
        return panes.stream().map(pane -> pane.getItem(tag)).filter(Objects::nonNull).findAny().orElse(null);
    }

    /**
     * Returns a pane by tag
     *
     * @param tag the tag to look for
     * @return the pane
     * @since 5.6.0
     */
    @Nullable
    @Contract(pure = true)
    public Pane getPane(@NotNull String tag) {
        return panes.stream().filter(pane -> tag.equals(pane.getTag())).findAny().orElse(null);
    }

    /**
     * Shows a gui to a player
     *
     * @param humanEntity the human entity to show the gui to
     * @since 5.6.0
     */
    public void show(HumanEntity humanEntity) {
        inventory.clear();

        //initialize the inventory first
        panes.stream().filter(Pane::isVisible).forEach(pane -> pane.display(inventory));

        humanEntity.openInventory(inventory);
    }

    /**
     * Update the gui for everyone
     *
     * @since 5.6.0
     */
    public void update() {
        new HashSet<>(inventory.getViewers()).forEach(this::show);
    }

    /**
     * Set the consumer that should be called whenever this gui is clicked in.
     *
     * @param onClick the consumer that gets called
     */
    public void setOnClick(Consumer<InventoryClickEvent> onClick) {
        this.onClick = onClick;
    }

    /**
     * Set the consumer that should be called whenever this gui is closed.
     *
     * @param onClose the consumer that gets called
     */
    public void setOnClose(Consumer<InventoryCloseEvent> onClose) {
        this.onClose = onClose;
    }

    /**
     * {@inheritDoc}
     * 
     * @since 5.6.0
     */
    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Loads a Gui from a given input stream
     *
     * @param inputStream the file
     * @return the gui
     * @since 5.6.0
     */
    @Nullable
    @Contract("_, _, null -> fail")
    public static Gui load(Plugin plugin, Object instance, InputStream inputStream) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            Element documentElement = document.getDocumentElement();

            documentElement.normalize();

            Gui gui = new Gui(plugin, Integer.parseInt(documentElement.getAttribute("rows")),
                    documentElement.getAttribute("title"));

            if (documentElement.hasAttribute("onClick")) {
                for (Method method : instance.getClass().getMethods()) {
                    if (!method.getName().equals(documentElement.getAttribute("onClick")))
                        continue;

                    int parameterCount = method.getParameterCount();

                    if (parameterCount == 0) {
                        gui.setOnClick(event -> {
                            try {
                                method.setAccessible(true);
                                method.invoke(instance);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        });
                    } else if (parameterCount == 1 &&
                            InventoryClickEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        gui.setOnClick(event -> {
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

            if (documentElement.hasAttribute("onClose")) {
                for (Method method : instance.getClass().getMethods()) {
                    if (!method.getName().equals(documentElement.getAttribute("onClose")))
                        continue;

                    int parameterCount = method.getParameterCount();

                    if (parameterCount == 0) {
                        gui.setOnClose(event -> {
                            try {
                                method.setAccessible(true);
                                method.invoke(instance);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        });
                    } else if (parameterCount == 1 &&
                            InventoryCloseEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        gui.setOnClose(event -> {
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

            NodeList childNodes = documentElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);

                if (item.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                gui.addPane(loadPane(instance, item));
            }

            return gui;
        } catch (ParserConfigurationException | SAXException | IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Loads a pane by the given instance and node
     *
     * @param instance the instance
     * @param node the node
     * @return the pane
     * @since 5.6.0
     */
    @Nullable
    @Contract("_, null -> fail")
    public static Pane loadPane(Object instance, @NotNull Node node) {
        switch (node.getNodeName()) {
            case "outlinepane":
                return OutlinePane.load(instance, (Element) node);
            case "paginatedpane":
                return PaginatedPane.load(instance, (Element) node);
            case "staticpane":
                return StaticPane.load(instance, (Element) node);
        }

        return null;
    }

    /**
     * Registers a property that can be used inside an XML file to add additional new properties.
     *
     * @param attributeName the name of the property. This is the same name you'll be using to specify the property
     *                      type in the XML file.
     * @param function how the property should be processed. This converts the raw text input from the XML node value
     *                 into the correct object type.
     * @throws AssertionError when a property with this name is already registered.
     */
    public static void registerProperty(String attributeName, Function<String, Object> function) {
        assert !Pane.getPropertyMappings().containsKey(attributeName) : "property is already registered";

        Pane.getPropertyMappings().put(attributeName, function);
    }

    /**
     * Handles clicks in inventories
     * 
     * @param event the event fired
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || !this.equals(event.getClickedInventory().getHolder()))
            return;

        if (onClick != null)
            onClick.accept(event);

        //loop through the panes reverse, because the pane with the highest priority (last in list) is most likely to have the correct item
        for (int i = panes.size() - 1; i >= 0; i--) {
            if (panes.get(i).click(event))
                break;
        }
    }

    /**
     * Handles closing in inventories
     *
     * @param event the event fired
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (onClose == null)
            return;

        onClose.accept(event);
    }
}
