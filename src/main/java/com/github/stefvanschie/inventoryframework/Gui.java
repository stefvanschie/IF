package com.github.stefvanschie.inventoryframework;

import com.github.stefvanschie.inventoryframework.pane.*;
import com.github.stefvanschie.inventoryframework.pane.component.Label;
import com.github.stefvanschie.inventoryframework.pane.component.PercentageBar;
import com.github.stefvanschie.inventoryframework.util.XMLUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The base class of all GUIs
 */
public class Gui implements Listener, InventoryHolder {

    /**
     * A set of all panes in this inventory
     */
    @NotNull
    private final List<Pane> panes;

    /**
     * The inventory of this gui
     */
    @NotNull
    private Inventory inventory;

    /**
     * The state of this gui
     */
    @NotNull
    private State state = State.TOP;

    /**
     * A player cache for storing player's inventories
     */
    @NotNull
    private HumanEntityCache humanEntityCache = new HumanEntityCache();

    /**
     * The consumer that will be called once a players clicks in the top-half of the gui
     */
    @Nullable
    private Consumer<InventoryClickEvent> onTopClick;

    /**
     * The consumer that will be called once a players clicks in the bottom-half of the gui
     */
    @Nullable
    private Consumer<InventoryClickEvent> onBottomClick;

    /**
     * The consumer that will be called once a players clicks in the gui or in their inventory
     */
    @Nullable
    private Consumer<InventoryClickEvent> onGlobalClick;


    /**
     * The consumer that will be called once a player closes the gui
     */
    @Nullable
    private Consumer<InventoryCloseEvent> onClose;

    /**
     * The pane mapping which will allow users to register their own panes to be used in XML files
     */
    @NotNull
    private static final Map<String, BiFunction<Object, Element, Pane>> PANE_MAPPINGS = new HashMap<>();

    /**
     * Constructs a new GUI
     *
     * @param plugin the main plugin.
     * @param rows the amount of rows this gui should contain, in range 1..6.
     * @param title the title/name of this gui.
     */
    public Gui(@NotNull Plugin plugin, int rows, @NotNull String title) {
        if (!(rows >= 1 && rows <= 6)) {
            throw new IllegalArgumentException("Rows should be between 1 and 6");
        }

        this.panes = new ArrayList<>();
        this.inventory = Bukkit.createInventory(this, rows * 9, title);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Adds a pane to this gui
     *
     * @param pane the pane to add
     */
    public void addPane(@NotNull Pane pane) {
        this.panes.add(pane);

        this.panes.sort(Comparator.comparing(Pane::getPriority));
    }

    /**
     * Shows a gui to a player
     *
     * @param humanEntity the human entity to show the gui to
     */
    public void show(@NotNull HumanEntity humanEntity) {
        inventory.clear();

        //set the state to the top, so in case there are no longer any bottom part panes, their inventory will be shown again
        setState(State.TOP);

        humanEntityCache.store(humanEntity);

        for (int i = 0; i < 36; i++) {
            humanEntity.getInventory().clear(i);
        }

        //initialize the inventory first
        panes.stream().filter(Pane::isVisible).forEach(pane -> pane.display(this, inventory,
            humanEntity.getInventory(), 0, 0, 9, getRows() + 4));

        //ensure that the inventory is cached before being overwritten and restore it if we end up not needing the bottom part after all
        if (state == State.TOP) {
            humanEntityCache.restore(humanEntity);
            humanEntityCache.clearCache(humanEntity);
        }

        humanEntity.openInventory(inventory);
    }

    /**
     * Sets the amount of rows for this inventory.
     * This will (unlike most other methods) directly update itself in order to ensure all viewers will still be viewing the new inventory as well.
     *
     * @param rows the amount of rows in range 1..6.
     */
    public void setRows(int rows) {
        if (!(rows >= 1 && rows <= 6)) {
            throw new IllegalArgumentException("Rows should be between 1 and 6");
        }

        //copy the viewers
        List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());

        this.inventory = Bukkit.createInventory(this, rows * 9, this.inventory.getTitle());

        viewers.forEach(humanEntity -> humanEntity.openInventory(inventory));
    }

    /**
     * Gets all the panes in this gui, this includes child panes from other panes
     *
     * @return all panes
     */
    @NotNull
    @Contract(pure = true)
    public Collection<Pane> getPanes() {
        Collection<Pane> panes = new HashSet<>();

        this.panes.forEach(pane -> panes.addAll(pane.getPanes()));
        panes.addAll(this.panes);

        return panes;
    }

    /**
     * Sets the title for this inventory. This will (unlike most other methods) directly update itself in order
     * to ensure all viewers will still be viewing the new inventory as well.
     *
     * @param title the title
     */
    public void setTitle(@NotNull String title) {
        //copy the viewers
        List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());

        this.inventory = Bukkit.createInventory(this, this.inventory.getSize(), title);

        viewers.forEach(humanEntity -> humanEntity.openInventory(inventory));
    }

    /**
     * Gets all the items in all underlying panes
     *
     * @return all items
     */
    @NotNull
    @Contract(pure = true)
    public Collection<GuiItem> getItems() {
        return getPanes().stream().flatMap(pane -> pane.getItems().stream()).collect(Collectors.toSet());
    }

    /**
     * Update the gui for everyone
     */
    public void update() {
        new HashSet<>(inventory.getViewers()).forEach(this::show);
    }

    /**
     * Calling this method will set the state of this gui. If this state is set to top state, it will restore all the
     * stored inventories of the players and will assume no pane extends into the bottom inventory part. If the state is
     * set to bottom state it will assume one or more panes overflow into the bottom half of the inventory and will
     * store all players' inventories and clear those.
     *
     * Do not call this method if you just want the player's inventory to be cleared.
     *
     * @param state the new gui state
     * @since 0.4.0
     */
    public void setState(@NotNull State state) {
        this.state = state;

        if (state == State.TOP) {
            humanEntityCache.restoreAll();
            humanEntityCache.clearCache();
        } else if (state == State.BOTTOM) {
            inventory.getViewers().forEach(humanEntity -> {
                humanEntityCache.store(humanEntity);

                for (int i = 0; i < 36; i++) {
                    humanEntity.getInventory().clear(i);
                }
            });
        }
    }

    /**
     * Loads a Gui from a given input stream
     *
     * @param plugin the main plugin
     * @param instance the class instance for all reflection lookups
     * @param inputStream the file
     * @return the gui
     */
    @Nullable
    @Contract("_, _, null -> fail")
    public static Gui load(@NotNull Plugin plugin, @NotNull Object instance, @NotNull InputStream inputStream) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            Element documentElement = document.getDocumentElement();

            documentElement.normalize();

            Gui gui = new Gui(plugin, Integer.parseInt(documentElement.getAttribute("rows")), ChatColor
                    .translateAlternateColorCodes('&', documentElement.getAttribute("title")));

            if (documentElement.hasAttribute("field"))
                XMLUtil.loadFieldAttribute(instance, documentElement, gui);

            if (documentElement.hasAttribute("onTopClick")) {
                Consumer<InventoryClickEvent> onTopClickAttribute = XMLUtil.loadOnClickAttribute(instance,
                    documentElement, "onTopClick");

                if (onTopClickAttribute != null) {
                    gui.setOnTopClick(onTopClickAttribute);
                }
            }

            if (documentElement.hasAttribute("onBottomClick")) {
                Consumer<InventoryClickEvent> onBottomClickAttribute = XMLUtil.loadOnClickAttribute(instance,
                    documentElement, "onBottomClick");

                if (onBottomClickAttribute != null) {
                    gui.setOnBottomClick(onBottomClickAttribute);
                }
            }
            
            if (documentElement.hasAttribute("onGlobalClick")) {
                Consumer<InventoryClickEvent> onGlobalClickAttribute = XMLUtil.loadOnClickAttribute(instance,
                    documentElement, "onGlobalClick");

                if (onGlobalClickAttribute != null) {
                    gui.setOnGlobalClick(onGlobalClickAttribute);
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

            if (documentElement.hasAttribute("populate")) {
                try {
                    Method method = instance.getClass().getMethod("populate", Gui.class);

                    method.setAccessible(true);
                    method.invoke(instance, gui);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }

                return gui;
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
     * Set the consumer that should be called whenever this gui is clicked in.
     *
     * @param onTopClick the consumer that gets called
     */
    public void setOnTopClick(@NotNull Consumer<InventoryClickEvent> onTopClick) {
        this.onTopClick = onTopClick;
    }

    /**
     * Set the consumer that should be called whenever the inventory is clicked in.
     *
     * @param onBottomClick the consumer that gets called
     */
    public void setOnBottomClick(@NotNull Consumer<InventoryClickEvent> onBottomClick) {
        this.onBottomClick = onBottomClick;
    }

    /**
     * Set the consumer that should be called whenever this gui or inventory is clicked in.
     *
     * @param onGlobalClick the consumer that gets called
     */
    public void setOnGlobalClick(@NotNull Consumer<InventoryClickEvent> onGlobalClick) {
        this.onGlobalClick = onGlobalClick;
    }

    /**
     * Set the consumer that should be called whenever this gui is clicked in.
     *
     * @param onLocalClick the consumer that gets called
     * @deprecated see {@link #setOnTopClick(Consumer)}
     */
    @Deprecated
    public void setOnLocalClick(@NotNull Consumer<InventoryClickEvent> onLocalClick) {
        this.onTopClick = onLocalClick;
    }

    /**
     * Set the consumer that should be called whenever this gui is closed.
     *
     * @param onClose the consumer that gets called
     */
    public void setOnClose(@NotNull Consumer<InventoryCloseEvent> onClose) {
        this.onClose = onClose;
    }

    /**
     * Returns the amount of rows this gui currently has
     *
     * @return the amount of rows
     */
    public int getRows() {
        return inventory.getSize() / 9;
    }

    /**
     * Returns the title of this gui
     *
     * @return the title
     */
    public String getTitle() {
        return inventory.getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Registers a property that can be used inside an XML file to add additional new properties.
     *
     * @param attributeName the name of the property. This is the same name you'll be using to specify the property
     *                      type in the XML file.
     * @param function how the property should be processed. This converts the raw text input from the XML node value
     *                 into the correct object type.
     * @throws IllegalArgumentException when a property with this name is already registered.
     */
    public static void registerProperty(@NotNull String attributeName, @NotNull Function<String, Object> function) {
        if (Pane.getPropertyMappings().containsKey(attributeName)) {
            throw new IllegalArgumentException("property '" + attributeName + "' is already registered");
        }

        Pane.getPropertyMappings().put(attributeName, function);
    }

    /**
     * Registers a name that can be used inside an XML file to add custom panes
     *
     * @param name the name of the pane to be used in the XML file
     * @param biFunction how the pane loading should be processed
     * @throws IllegalArgumentException when a pane with this name is already registered
     */
    public static void registerPane(@NotNull String name, @NotNull BiFunction<Object, Element, Pane> biFunction) {
        if (PANE_MAPPINGS.containsKey(name)) {
            throw new IllegalArgumentException("pane name '" + name + "' is already registered");
        }

        PANE_MAPPINGS.put(name, biFunction);
    }

    /**
     * Loads a pane by the given instance and node
     *
     * @param instance the instance
     * @param node the node
     * @return the pane
     */
    @NotNull
    @Contract("_, null -> fail")
    public static Pane loadPane(@NotNull Object instance, @NotNull Node node) {
        return PANE_MAPPINGS.get(node.getNodeName()).apply(instance, (Element) node);
    }

    /**
     * Handles clicks in inventories
     * 
     * @param event the event fired
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!this.equals(event.getInventory().getHolder())) {
            return;
        }

        if (onGlobalClick != null) {
            onGlobalClick.accept(event);
        }

        if (onTopClick != null &&
            event.getView().getInventory(event.getRawSlot()).equals(event.getView().getTopInventory())) {
            onTopClick.accept(event);
        }

        if (onBottomClick != null &&
            event.getView().getInventory(event.getRawSlot()).equals(event.getView().getBottomInventory())) {
            onBottomClick.accept(event);
        }

        if ((event.getView().getInventory(event.getRawSlot()).equals(event.getView().getBottomInventory()) &&
            state == State.TOP) || event.getCurrentItem() == null) {
            return;
        }

        //loop through the panes reverse, because the pane with the highest priority (last in list) is most likely to have the correct item
        for (int i = panes.size() - 1; i >= 0; i--) {
            if (panes.get(i).click(this, event, 0, 0, 9, getRows() + 4))
                break;
        }
    }

    /**
     * Handles closing in inventories
     *
     * @param event the event fired
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        HumanEntity humanEntity = event.getPlayer();

        humanEntityCache.restore(humanEntity);
        humanEntityCache.clearCache(humanEntity);

        if (onClose == null)
            return;

        onClose.accept(event);
    }

    /**
     * Handles player's leaving
     *
     * @param event the event fired
     * @since 0.4.0
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();

        humanEntityCache.restore(player);
        humanEntityCache.clearCache(player);
    }

    /**
     * The gui state
     *
     * @since 0.4.0
     */
    public enum State {

        /**
         * This signals that only the top-half of the Gui is in use and the player's inventory will stay like it is
         *
         * @since 0.4.0
         */
        TOP,

        /**
         * This singals that the bottom-hal of the Gui is in use and the player's inventory will be cleared and stored
         *
         * @since 0.4.0
         */
        BOTTOM
    }

    static {
        registerPane("masonrypane", MasonryPane::load);
        registerPane("outlinepane", OutlinePane::load);
        registerPane("paginatedpane", PaginatedPane::load);
        registerPane("staticpane", StaticPane::load);

        registerPane("label", Label::load);
        registerPane("percentagebar", PercentageBar::load);
    }
}
