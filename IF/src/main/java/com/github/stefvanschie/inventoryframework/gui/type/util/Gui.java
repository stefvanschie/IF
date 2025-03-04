package com.github.stefvanschie.inventoryframework.gui.type.util;

import com.github.stefvanschie.inventoryframework.HumanEntityCache;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.gui.GuiListener;
import com.github.stefvanschie.inventoryframework.gui.type.*;
import com.github.stefvanschie.inventoryframework.pane.*;
import com.github.stefvanschie.inventoryframework.pane.component.*;
import com.github.stefvanschie.inventoryframework.util.TriFunction;
import com.github.stefvanschie.inventoryframework.util.XMLUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * The base class of all GUIs
 */
public abstract class Gui {

    /**
     * The plugin that owns this gui
     */
    @NotNull
    protected final Plugin plugin;

    /**
     * The inventory of this gui
     */
    protected Inventory inventory;

    /**
     * A player cache for storing player's inventories
     */
    @NotNull
    protected final HumanEntityCache humanEntityCache = new HumanEntityCache();

    /**
     * The consumer that will be called once a players clicks in the top-half of the gui
     */
    @Nullable
    protected Consumer<InventoryClickEvent> onTopClick;

    /**
     * The consumer that will be called once a players clicks in the bottom-half of the gui
     */
    @Nullable
    protected Consumer<InventoryClickEvent> onBottomClick;

    /**
     * The consumer that will be called once a players clicks in the gui or in their inventory
     */
    @Nullable
    protected Consumer<InventoryClickEvent> onGlobalClick;

    /**
     * The consumer that will be called once a player clicks outside of the gui screen
     */
    @Nullable
    protected Consumer<InventoryClickEvent> onOutsideClick;

    /**
     * The consumer that will be called once a player drags in the top-half of the gui
     */
    @Nullable
    protected Consumer<InventoryDragEvent> onTopDrag;

    /**
     * The consumer that will be called once a player drags in the bottom-half of the gui
     */
    @Nullable
    protected Consumer<InventoryDragEvent> onBottomDrag;

    /**
     * The consumer that will be called once a player drags in the gui or their inventory
     */
    @Nullable
    protected Consumer<InventoryDragEvent> onGlobalDrag;

    /**
     * The consumer that will be called once a player closes the gui
     */
    @Nullable
    protected Consumer<InventoryCloseEvent> onClose;

    /**
     * Whether this gui is updating (as invoked by {@link #update()}), true if this is the case, false otherwise. This
     * is used to indicate that inventory close events due to updating should be ignored.
     */
    boolean updating = false;

    /**
     * The parent gui. This gui will be navigated to once a player closes this gui. If this is null, the player will not
     * be redirected to another gui once they close this gui.
     */
    @Nullable
    private Gui parent;

    /**
     * The pane mapping which will allow users to register their own panes to be used in XML files
     */
    @NotNull
    private static final Map<String, TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Pane>>
        PANE_MAPPINGS = new HashMap<>();

    /**
     * The gui mappings which determine which gui type belongs to which identifier
     */
    @NotNull
    private static final Map<String, TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>>
        GUI_MAPPINGS = new HashMap<>();

    /**
     * A map containing the relations between inventories and their respective gui. This is needed because Bukkit and
     * Spigot ignore inventory holders for beacons, brewing stands, dispensers, droppers, furnaces and hoppers. The
     * inventory holder for beacons is already being set properly via NMS, but this contains the other inventory types.
     */
    @NotNull
    private static final Map<Inventory, Gui> GUI_INVENTORIES = new WeakHashMap<>();

    /**
     * Whether listeners have ben registered by some gui
     */
    private static boolean hasRegisteredListeners;

    /**
     * Constructs a new gui with the provided plugin.
     *
     * @param plugin the plugin
     * @since 0.10.8
     */
    public Gui(@NotNull Plugin plugin) {
        this.plugin = plugin;

        if (!hasRegisteredListeners) {
            Bukkit.getPluginManager().registerEvents(new GuiListener(plugin), plugin);

            hasRegisteredListeners = true;
        }
    }

    /**
     * Shows a gui to a player
     *
     * @param humanEntity the human entity to show the gui to
     */
    public abstract void show(@NotNull HumanEntity humanEntity);

    /**
     * Makes a copy of this gui and returns it. This makes a deep copy of the gui. This entails that the underlying
     * panes will be copied as per their {@link Pane#copy} and miscellaneous data will be copied. The copy of this gui,
     * will however have no viewers even if this gui currently has viewers. With this, cache data for viewers will also
     * be non-existent for the copied gui. The original owning plugin of the gui is preserved, but the plugin will not
     * be deeply copied. The returned gui will never be reference equal to the current gui.
     *
     * @return a copy of the gui
     * @since 0.6.2
     */
    @NotNull
    @Contract(pure = true)
    public abstract Gui copy();

    /**
     * This should delegate the provided inventory click event to the right pane, which can then handle this click event
     * further. This should not call any internal click handlers, since those will already have been activated.
     *
     * @param event the event to delegate
     * @since 0.8.0
     */
    public abstract void click(@NotNull InventoryClickEvent event);

    /**
     * Gets whether the player inventory is currently in use. This means whether the player inventory currently has an
     * item in it.
     *
     * @return true if the player inventory is occupied, false otherwise
     * @since 0.8.0
     */
    public abstract boolean isPlayerInventoryUsed();

    /**
     * Gets the count of {@link HumanEntity} instances that are currently viewing this GUI.
     *
     * @return the count of viewers
     * @since 0.5.19
     */
    @Contract(pure = true)
    public abstract int getViewerCount();

    /**
     * Gets a mutable snapshot of the current {@link HumanEntity} viewers of this GUI.
     * This is a snapshot (copy) and not a view, therefore modifications aren't visible.
     *
     * @return a snapshot of the current viewers
     * @see #getViewerCount()
     * @since 0.5.19
     */
    @NotNull
    @Contract(pure = true)
    public abstract List<HumanEntity> getViewers();

    /**
     * Update the gui for everyone
     */
    public void update() {
        updating = true;

        for (HumanEntity viewer : getViewers()) {
            ItemStack cursor = viewer.getItemOnCursor();
            viewer.setItemOnCursor(new ItemStack(Material.AIR));

            show(viewer);

            viewer.setItemOnCursor(cursor);
        }

        if (!updating)
            throw new AssertionError("Gui#isUpdating became false before Gui#update finished");

        updating = false;
    }

    /**
     * Adds the specified inventory and gui, so we can properly intercept clicks.
     *
     * @param inventory the inventory for the specified gui
     * @param gui the gui belonging to the specified inventory
     * @since 0.8.1
     */
    protected void addInventory(@NotNull Inventory inventory, @NotNull Gui gui) {
        GUI_INVENTORIES.put(inventory, gui);
    }

    /**
     * Gets a gui from the specified inventory. Only guis of type beacon, brewing stand, dispenser, dropper, furnace and
     * hopper can be retrieved.
     *
     * @param inventory the inventory to get the gui from
     * @return the gui or null if the inventory doesn't have an accompanying gui
     * @since 0.8.1
     */
    @Nullable
    @Contract(pure = true)
    public static Gui getGui(@NotNull Inventory inventory) {
        return GUI_INVENTORIES.get(inventory);
    }

    /**
     * Gets the human entity cache used for this gui
     *
     * @return the human entity cache
     * @see HumanEntityCache
     * @since 0.5.4
     */
    @NotNull
    @Contract(pure = true)
    public HumanEntityCache getHumanEntityCache() {
        return humanEntityCache;
    }

    /**
     * Loads a Gui from a given input stream.
     *
     * @param instance the class instance for all reflection lookups
     * @param inputStream the file
     * @return the gui or null if the loading failed
     * @throws XMLLoadException if loading could not finish successfully, due to e.g., a malformed file
     * @see #load(Object, InputStream)
     * @since 0.10.8
     */
    @Nullable
    public static Gui load(@NotNull Object instance, @NotNull InputStream inputStream, @NotNull Plugin plugin) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            Element documentElement = document.getDocumentElement();

            documentElement.normalize();

            if (!documentElement.hasAttribute("type")) {
                throw new XMLLoadException("Type attribute must be specified when loading via Gui.load");
            }

            String type = documentElement.getAttribute("type");
            TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui> mapping = GUI_MAPPINGS
                    .get(type);

            if (mapping == null) {
                throw new XMLLoadException("Type attribute '" + type + "' is invalid");
            }

            return mapping.apply(instance, documentElement, plugin);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a Gui from a given input stream.
     * Returns null instead of throwing an exception in case of a failure.
     *
     * @param instance the class instance for all reflection lookups
     * @param inputStream the file
     * @return the gui or null if the loading failed
     * @throws XMLLoadException if loading could not finish successfully, due to e.g., a malformed file
     */
    @Nullable
    public static Gui load(@NotNull Object instance, @NotNull InputStream inputStream) {
        return load(instance, inputStream, JavaPlugin.getProvidingPlugin(Gui.class));
    }

    /**
     * Initializes standard fields from a Gui from a given input stream.
     * Throws a {@link RuntimeException} instead of returning null in case of a failure.
     *
     * @param instance the class instance for all reflection lookups
     * @param element the gui element
     * @see #load(Object, InputStream)
     */
     protected void initializeOrThrow(@NotNull Object instance, @NotNull Element element) {
        if (element.hasAttribute("field"))
            XMLUtil.loadFieldAttribute(instance, element, this);

        if (element.hasAttribute("onTopClick")) {
            setOnTopClick(XMLUtil.loadOnEventAttribute(instance,
                element, InventoryClickEvent.class, "onTopClick"));
        }

        if (element.hasAttribute("onBottomClick")) {
            setOnBottomClick(XMLUtil.loadOnEventAttribute(instance,
                element, InventoryClickEvent.class, "onBottomClick"));
        }

        if (element.hasAttribute("onGlobalClick")) {
            setOnGlobalClick(XMLUtil.loadOnEventAttribute(instance,
                element, InventoryClickEvent.class, "onGlobalClick"));
        }

        if (element.hasAttribute("onOutsideClick")) {
            setOnOutsideClick(XMLUtil.loadOnEventAttribute(instance,
                element, InventoryClickEvent.class, "onOutsideClick"));
        }

         if (element.hasAttribute("onTopDrag")) {
             setOnTopDrag(XMLUtil.loadOnEventAttribute(instance,
                 element, InventoryDragEvent.class, "onTopDrag"));
         }

         if (element.hasAttribute("onBottomDrag")) {
             setOnBottomDrag(XMLUtil.loadOnEventAttribute(instance,
                 element, InventoryDragEvent.class, "onBottomDrag"));
         }

         if (element.hasAttribute("onGlobalDrag")) {
             setOnGlobalDrag(XMLUtil.loadOnEventAttribute(instance,
                 element, InventoryDragEvent.class, "onGlobalDrag"));
         }

        if (element.hasAttribute("onClose")) {
            setOnClose(XMLUtil.loadOnEventAttribute(instance,
                element, InventoryCloseEvent.class, "onClose"));
        }

        if (element.hasAttribute("populate")) {
            XMLUtil.invokeMethod(instance, element.getAttribute("populate"), this, Gui.class);
        }
    }

    /**
     * Set the consumer that should be called whenever this gui is clicked in.
     *
     * @param onTopClick the consumer that gets called
     */
    public void setOnTopClick(@Nullable Consumer<InventoryClickEvent> onTopClick) {
        this.onTopClick = onTopClick;
    }

    /**
     * Calls the consumer (if it's not null) that was specified using {@link #setOnTopClick(Consumer)},
     * so the consumer that should be called whenever this gui is clicked in.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    public void callOnTopClick(@NotNull InventoryClickEvent event) {
        callCallback(onTopClick, event, "onTopClick");
    }

    /**
     * Set the consumer that should be called whenever the inventory is clicked in.
     *
     * @param onBottomClick the consumer that gets called
     */
    public void setOnBottomClick(@Nullable Consumer<InventoryClickEvent> onBottomClick) {
        this.onBottomClick = onBottomClick;
    }

    /**
     * Calls the consumer (if it's not null) that was specified using {@link #setOnBottomClick(Consumer)},
     * so the consumer that should be called whenever the inventory is clicked in.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    public void callOnBottomClick(@NotNull InventoryClickEvent event) {
        callCallback(onBottomClick, event, "onBottomClick");
    }

    /**
     * Set the consumer that should be called whenever this gui or inventory is clicked in.
     *
     * @param onGlobalClick the consumer that gets called
     */
    public void setOnGlobalClick(@Nullable Consumer<InventoryClickEvent> onGlobalClick) {
        this.onGlobalClick = onGlobalClick;
    }

    /**
     * Calls the consumer (if it's not null) that was specified using {@link #setOnGlobalClick(Consumer)},
     * so the consumer that should be called whenever this gui or inventory is clicked in.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    public void callOnGlobalClick(@NotNull InventoryClickEvent event) {
        callCallback(onGlobalClick, event, "onGlobalClick");
    }

    /**
     * Set the consumer that should be called whenever a player clicks outside the gui.
     *
     * @param onOutsideClick the consumer that gets called
     * @since 0.5.7
     */
    public void setOnOutsideClick(@Nullable Consumer<InventoryClickEvent> onOutsideClick) {
        this.onOutsideClick = onOutsideClick;
    }

    /**
     * Calls the consumer (if it's not null) that was specified using {@link #setOnOutsideClick(Consumer)},
     * so the consumer that should be called whenever a player clicks outside the gui.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    public void callOnOutsideClick(@NotNull InventoryClickEvent event) {
        callCallback(onOutsideClick, event, "onOutsideClick");
    }

    /**
     * Set the consumer that should be called whenever this gui's top half is dragged in.
     *
     * @param onTopDrag the consumer that gets called
     * @since 0.9.0
     */
    public void setOnTopDrag(@Nullable Consumer<InventoryDragEvent> onTopDrag) {
        this.onTopDrag = onTopDrag;
    }

    /**
     * Calls the consumer (if it's not null) that was specified using {@link #setOnTopDrag(Consumer)},
     * so the consumer that should be called whenever this gui's top half is dragged in.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.9.0
     */
    public void callOnTopDrag(@NotNull InventoryDragEvent event) {
        callCallback(onTopDrag, event, "onTopDrag");
    }

    /**
     * Set the consumer that should be called whenever the inventory is dragged in.
     *
     * @param onBottomDrag the consumer that gets called
     * @since 0.9.0
     */
    public void setOnBottomDrag(@Nullable Consumer<InventoryDragEvent> onBottomDrag) {
        this.onBottomDrag = onBottomDrag;
    }

    /**
     * Calls the consumer (if it's not null) that was specified using {@link #setOnBottomDrag(Consumer)},
     * so the consumer that should be called whenever the inventory is dragged in.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.9.0
     */
    public void callOnBottomDrag(@NotNull InventoryDragEvent event) {
        callCallback(onBottomDrag, event, "onBottomDrag");
    }

    /**
     * Set the consumer that should be called whenever this gui or inventory is dragged in.
     *
     * @param onGlobalDrag the consumer that gets called
     * @since 0.9.0
     */
    public void setOnGlobalDrag(@Nullable Consumer<InventoryDragEvent> onGlobalDrag) {
        this.onGlobalDrag = onGlobalDrag;
    }

    /**
     * Calls the consumer (if it's not null) that was specified using {@link #setOnGlobalDrag(Consumer)},
     * so the consumer that should be called whenever this gui or inventory is dragged in.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    public void callOnGlobalDrag(@NotNull InventoryDragEvent event) {
        callCallback(onGlobalDrag, event, "onGlobalDrag");
    }

    /**
     * Set the consumer that should be called whenever this gui is closed.
     *
     * @param onClose the consumer that gets called
     */
    public void setOnClose(@Nullable Consumer<InventoryCloseEvent> onClose) {
        this.onClose = onClose;
    }

    /**
     * Calls the consumer (if it's not null) that was specified using {@link #setOnClose(Consumer)},
     * so the consumer that should be called whenever this gui is closed.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    public void callOnClose(@NotNull InventoryCloseEvent event) {
        callCallback(onClose, event, "onClose");
    }

    /**
     * Calls the specified consumer (if it's not null) with the specified parameter,
     * catching and logging all exceptions it might throw.
     *
     * @param callback the consumer to call if it isn't null
     * @param event the value the consumer should accept
     * @param callbackName the name of the action, used for logging
     * @param <T> the type of the value the consumer is accepting
     */
    protected <T extends InventoryEvent> void callCallback(@Nullable Consumer<? super T> callback,
            @NotNull T event, @NotNull String callbackName) {
        if (callback == null) {
            return;
        }

        try {
            callback.accept(event);
        } catch (Throwable t) {
            String message = "Exception while handling " + callbackName;
            if (event instanceof InventoryClickEvent) {
                InventoryClickEvent clickEvent = (InventoryClickEvent) event;
                message += ", slot=" + clickEvent.getSlot();
            }

            this.plugin.getLogger().log(Level.SEVERE, message, t);
        }
    }

    /**
     * The parent gui will be shown to the specified {@link HumanEntity}. If no parent gui is set, then this method will
     * silently do nothing.
     *
     * @param humanEntity the human entity to redirect
     * @since 0.10.14
     */
    public void navigateToParent(@NotNull HumanEntity humanEntity) {
        if (this.parent == null) {
            return;
        }

        this.parent.show(humanEntity);
    }

    /**
     * Sets the parent gui to the provided gui. This is the gui that a player will be navigated to once they close this
     * gui. The navigation will occur after the close event handler, set by {@link #setOnClose(Consumer)}, is called. If
     * there was already a previous parent set, the provided gui will override the previous one.
     *
     * @param gui the new parent gui
     * @since 0.10.14
     */
    public void setParent(@NotNull Gui gui) {
        this.parent = gui;
    }

    /**
     * Gets whether this gui is being updated, as invoked by {@link #update()}. This returns true if this is the case
     * and false otherwise.
     *
     * @return whether this gui is being updated
     * @since 0.5.15
     */
    @Contract(pure = true)
    public boolean isUpdating() {
        return updating;
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
        Pane.registerProperty(attributeName, function);
    }

    /**
     * Registers a name that can be used inside an XML file to add custom panes
     *
     * @param name the name of the pane to be used in the XML file
     * @param triFunction how the pane loading should be processed
     * @throws IllegalArgumentException when a pane with this name is already registered
     * @see #registerPane(String, BiFunction)
     * @since 0.10.8
     */
    public static void registerPane(@NotNull String name,
                                    @NotNull TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Pane> triFunction) {
        if (PANE_MAPPINGS.containsKey(name)) {
            throw new IllegalArgumentException("pane name '" + name + "' is already registered");
        }

        PANE_MAPPINGS.put(name, triFunction);
    }

    /**
     * Registers a name that can be used inside an XML file to add custom panes
     *
     * @param name the name of the pane to be used in the XML file
     * @param biFunction how the pane loading should be processed
     * @throws IllegalArgumentException when a pane with this name is already registered
     */
    public static void registerPane(@NotNull String name, @NotNull BiFunction<Object, Element, Pane> biFunction) {
        registerPane(name, (object, element, plugin) -> biFunction.apply(object, element));
    }

    /**
     * Registers a type that can be used inside an XML file to specify the gui type
     *
     * @param name the name of the type of gui to be used in an XML file
     * @param triFunction how the gui creation should be processed
     * @throws IllegalArgumentException when a gui type with this name is already registered
     * @since 0.10.8
     */
    public static void registerGui(@NotNull String name,
                                   @NotNull TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui> triFunction) {
        if (GUI_MAPPINGS.containsKey(name)) {
            throw new IllegalArgumentException("Gui name '" + name + "' is already registered");
        }

        GUI_MAPPINGS.put(name, triFunction);
    }

    /**
     * Registers a type that can be used inside an XML file to specify the gui type
     *
     * @param name the name of the type of gui to be used in an XML file
     * @param biFunction how the gui creation should be processed
     * @throws IllegalArgumentException when a gui type with this name is already registered
     * @deprecated this method is no longer used internally and has been superseded by
     *             {@link #registerPane(String, TriFunction)}
     */
    @Deprecated
    public static void registerGui(@NotNull String name,
                                   @NotNull BiFunction<? super Object, ? super Element, ? extends Gui> biFunction) {
        registerGui(name, (object, element, plugin) -> biFunction.apply(object, element));
    }

    /**
     * Loads a pane by the given instance and node
     *
     * @param instance the instance
     * @param node the node
     * @param plugin the plugin to load the pane with
     * @return the pane
     * @throws XMLLoadException if the name of the node does not correspond to a valid pane.
     * @since 0.10.8
     */
    @NotNull
    public static Pane loadPane(@NotNull Object instance, @NotNull Node node, @NotNull Plugin plugin) {
        String name = node.getNodeName();
        TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Pane> mapping = PANE_MAPPINGS.get(name);

        if (mapping == null) {
            throw new XMLLoadException("Pane '" + name + "' is not registered or does not exist");
        }

        return mapping.apply(instance, (Element) node, plugin);
    }

    /**
     * Loads a pane by the given instance and node
     *
     * @param instance the instance
     * @param node the node
     * @return the pane
     */
    @NotNull
    public static Pane loadPane(@NotNull Object instance, @NotNull Node node) {
        return loadPane(instance, node, JavaPlugin.getProvidingPlugin(Gui.class));
    }

    static {
        registerPane("masonrypane",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Pane>) MasonryPane::load);
        registerPane("outlinepane",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Pane>) OutlinePane::load);
        registerPane("paginatedpane",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Pane>) PaginatedPane::load);
        registerPane("patternpane",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Pane>) PatternPane::load);
        registerPane("staticpane",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Pane>) StaticPane::load);

        registerPane("cyclebutton",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Pane>) CycleButton::load);
        registerPane("label",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Pane>) Label::load);
        registerPane("pagingbuttons", PagingButtons::load);
        registerPane("percentagebar",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Pane>) PercentageBar::load);
        registerPane("slider",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Pane>) Slider::load);
        registerPane("togglebutton",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Pane>) ToggleButton::load);

        registerGui("anvil",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) AnvilGui::load);
        registerGui("barrel",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) BarrelGui::load);
        registerGui("beacon",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) BeaconGui::load);
        registerGui("blast-furnace",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) BlastFurnaceGui::load);
        registerGui("brewing-stand",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) BrewingStandGui::load);
        registerGui("cartography-table",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) CartographyTableGui::load);
        registerGui("chest",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) ChestGui::load);
        registerGui("crafting-table",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) CraftingTableGui::load);
        registerGui("dispenser",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) DispenserGui::load);
        registerGui("dropper",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) DropperGui::load);
        registerGui("enchanting-table",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) EnchantingTableGui::load);
        registerGui("ender-chest",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) EnderChestGui::load);
        registerGui("furnace",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) FurnaceGui::load);
        registerGui("grindstone",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) GrindstoneGui::load);
        registerGui("hopper",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) HopperGui::load);
        registerGui("merchant",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) MerchantGui::load);
        registerGui("shulker-box",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) ShulkerBoxGui::load);
        registerGui("smithing-table",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) SmithingTableGui::load);
        registerGui("smoker",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) SmokerGui::load);
        registerGui("stonecutter",
                (TriFunction<? super Object, ? super Element, ? super Plugin, ? extends Gui>) StonecutterGui::load);
    }
}
