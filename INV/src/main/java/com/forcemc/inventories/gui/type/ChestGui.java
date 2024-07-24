package com.forcemc.inventories.gui.type;

import com.forcemc.inventories.HumanEntityCache;
import com.forcemc.inventories.adventuresupport.StringHolder;
import com.forcemc.inventories.adventuresupport.TextHolder;
import com.forcemc.inventories.exception.XMLLoadException;
import com.forcemc.inventories.gui.GuiItem;
import com.forcemc.inventories.gui.InventoryComponent;
import com.forcemc.inventories.gui.type.util.InventoryBased;
import com.forcemc.inventories.gui.type.util.MergedGui;
import com.forcemc.inventories.gui.type.util.NamedGui;
import com.forcemc.inventories.pane.Pane;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a gui in the form of a chest. Unlike traditional chests, this may take on any amount of rows between 1 and
 * 6.
 *
 * @since 0.8.0
 */
public class ChestGui extends NamedGui implements MergedGui, InventoryBased {

    /**
     * Represents the inventory component for the entire gui
     */
    @NotNull
    private InventoryComponent inventoryComponent;

    /**
     * Whether the amount of rows are dirty i.e. has been changed
     */
    private boolean dirtyRows = false;

    /**
     * Constructs a new chest GUI
     *
     * @param rows the amount of rows this gui should contain, in range 1..6.
     * @param title the title/name of this gui.
     * @since 0.8.0
     */
    public ChestGui(int rows, @NotNull String title) {
        this(rows, StringHolder.of(title), JavaPlugin.getProvidingPlugin(ChestGui.class));
    }

    /**
     * Constructs a new chest GUI
     *
     * @param rows the amount of rows this gui should contain, in range 1..6.
     * @param title the title/name of this gui.
     * @since 0.10.0
     */
    public ChestGui(int rows, @NotNull TextHolder title) {
        this(rows, title, JavaPlugin.getProvidingPlugin(ChestGui.class));
    }

    /**
     * Constructs a new chest gui for the given {@code plugin}.
     *
     * @param rows the amount of rows this gui should contain, in range 1..6.
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #ChestGui(int, String)
     * @since 0.10.8
     */
    public ChestGui(int rows, @NotNull String title, @NotNull Plugin plugin) {
        this(rows, StringHolder.of(title), plugin);
    }

    /**
     * Constructs a new chest gui for the given {@code plugin}.
     *
     * @param rows the amount of rows this gui should contain, in range 1..6.
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #ChestGui(int, TextHolder)
     * @since 0.10.8
     */
    public ChestGui(int rows, @NotNull TextHolder title, @NotNull Plugin plugin) {
        super(title, plugin);

        if (!(rows >= 1 && rows <= 6)) {
            throw new IllegalArgumentException("Rows should be between 1 and 6");
        }

        this.inventoryComponent = new InventoryComponent(9, rows + 4);
    }

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        if (isDirty() || dirtyRows) {
            this.inventory = createInventory();
            this.dirtyRows = false;

            markChanges();
        }

        getInventory().clear();

        int height = getInventoryComponent().getHeight();

        getInventoryComponent().display();

        InventoryComponent topComponent = getInventoryComponent().excludeRows(height - 4, height - 1);
        InventoryComponent bottomComponent = getInventoryComponent().excludeRows(0, height - 5);

        topComponent.placeItems(getInventory(), 0);

        if (bottomComponent.hasItem()) {
            HumanEntityCache humanEntityCache = getHumanEntityCache();

            if (!humanEntityCache.contains(humanEntity)) {
                humanEntityCache.storeAndClear(humanEntity);
            }

            bottomComponent.placeItems(humanEntity.getInventory(), 0);
        }

        humanEntity.openInventory(getInventory());
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public ChestGui copy() {
        ChestGui gui = new ChestGui(getRows(), getTitleHolder(), super.plugin);

        gui.inventoryComponent = inventoryComponent.copy();

        gui.setOnTopClick(this.onTopClick);
        gui.setOnBottomClick(this.onBottomClick);
        gui.setOnGlobalClick(this.onGlobalClick);
        gui.setOnOutsideClick(this.onOutsideClick);
        gui.setOnClose(this.onClose);

        return gui;
    }

    @Override
    public void click(@NotNull InventoryClickEvent event) {
        getInventoryComponent().click(this, event, event.getRawSlot());
    }

    @Contract(pure = true)
    @Override
    public boolean isPlayerInventoryUsed() {
        return getInventoryComponent().excludeRows(0, getInventoryComponent().getHeight() - 5).hasItem();
    }

    /**
     * Sets the amount of rows for this inventory.
     * This will (unlike most other methods) directly update itself in order to ensure all viewers will still be viewing
     * the new inventory as well.
     *
     * @param rows the amount of rows in range 1..6.
     * @since 0.8.0
     */
    public void setRows(int rows) {
        if (!(rows >= 1 && rows <= 6)) {
            throw new IllegalArgumentException("Rows should be between 1 and 6");
        }

        InventoryComponent inventoryComponent = new InventoryComponent(9, rows + 4);

        for (Pane pane : this.inventoryComponent.getPanes()) {
            inventoryComponent.addPane(pane);
        }

        this.inventoryComponent = inventoryComponent;
        this.dirtyRows = true;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = createInventory();
        }

        return inventory;
    }

    @Override
    public void addPane(@NotNull Pane pane) {
        this.inventoryComponent.addPane(pane);
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public List<Pane> getPanes() {
        return this.inventoryComponent.getPanes();
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public Collection<GuiItem> getItems() {
        return getPanes().stream().flatMap(pane -> pane.getItems().stream()).collect(Collectors.toSet());
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory() {
        return getTitleHolder().asInventoryTitle(this, getRows() * 9);
    }

    /**
     * Returns the amount of rows this gui currently has
     *
     * @return the amount of rows
     * @since 0.8.0
     */
    @Contract(pure = true)
    public int getRows() {
        return getInventoryComponent().getHeight() - 4;
    }

    @Contract(pure = true)
    @Override
    public int getViewerCount() {
        return getInventory().getViewers().size();
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public List<HumanEntity> getViewers() {
        return new ArrayList<>(getInventory().getViewers());
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public InventoryComponent getInventoryComponent() {
        return inventoryComponent;
    }

    /**
     * Loads a chest gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded chest gui
     * @see #load(Object, InputStream)
     * @since 0.10.8
     */
    @Nullable
    @Contract(pure = true)
    public static ChestGui load(@NotNull Object instance, @NotNull InputStream inputStream, @NotNull Plugin plugin) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            Element documentElement = document.getDocumentElement();

            documentElement.normalize();

            return load(instance, documentElement, plugin);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a chest gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded chest gui
     * @see #load(Object, Element)
     * @since 0.10.8
     */
    @NotNull
    public static ChestGui load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        if (!element.hasAttribute("rows")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory rows attribute set");
        }

        int rows;

        try {
            rows = Integer.parseInt(element.getAttribute("rows"));
        } catch (NumberFormatException exception) {
            throw new XMLLoadException("Rows attribute is not an integer", exception);
        }

        ChestGui chestGui = new ChestGui(rows, element.getAttribute("title"), plugin);
        chestGui.initializeOrThrow(instance, element);

        if (element.hasAttribute("populate")) {
            return chestGui;
        }

        NodeList childNodes = element.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node item = childNodes.item(index);

            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element componentElement = (Element) item;
            InventoryComponent inventoryComponent = chestGui.getInventoryComponent();

            if (componentElement.getTagName().equalsIgnoreCase("component")) {
                inventoryComponent.load(instance, componentElement, plugin);
            } else {
                inventoryComponent.load(instance, element, plugin);
            }

            break;
        }

        return chestGui;
    }

    /**
     * Loads a chest gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @return the loaded chest gui
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    public static ChestGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
        return load(instance, inputStream, JavaPlugin.getProvidingPlugin(ChestGui.class));
    }

    /**
     * Loads a chest gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @return the loaded chest gui
     * @since 0.8.0
     */
    @NotNull
    public static ChestGui load(@NotNull Object instance, @NotNull Element element) {
        return load(instance, element, JavaPlugin.getProvidingPlugin(ChestGui.class));
    }
}
