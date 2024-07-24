package com.forcemc.inventories.gui.type;

import com.forcemc.inventories.HumanEntityCache;
import com.forcemc.inventories.abstraction.GrindstoneInventory;
import com.forcemc.inventories.adventuresupport.TextHolder;
import com.forcemc.inventories.exception.XMLLoadException;
import com.forcemc.inventories.gui.InventoryComponent;
import com.forcemc.inventories.gui.type.util.InventoryBased;
import com.forcemc.inventories.gui.type.util.NamedGui;
import com.forcemc.inventories.util.version.Version;
import com.forcemc.inventories.util.version.VersionMatcher;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a gui in the form of a grindstone
 *
 * @since 0.8.0
 */
public class GrindstoneGui extends NamedGui implements InventoryBased {

    /**
     * Represents the inventory component for the items
     */
    @NotNull
    private InventoryComponent itemsComponent = new InventoryComponent(1, 2);

    /**
     * Represents the inventory component for the result
     */
    @NotNull
    private InventoryComponent resultComponent = new InventoryComponent(1, 1);

    /**
     * Represents the inventory component for the player inventory
     */
    @NotNull
    private InventoryComponent playerInventoryComponent = new InventoryComponent(9, 4);

    /**
     * An internal grindstone inventory
     */
    @NotNull
    private final GrindstoneInventory grindstoneInventory = VersionMatcher.newGrindstoneInventory(Version.getVersion(),
        this);

    /**
     * Constructs a new GUI
     *
     * @param title the title/name of this gui.
     * @since 0.8.0
     */
    public GrindstoneGui(@NotNull String title) {
        super(title);
    }

    /**
     * Constructs a new GUI
     *
     * @param title the title/name of this gui.
     * @since 0.10.0
     */
    public GrindstoneGui(@NotNull TextHolder title) {
        super(title);
    }

    /**
     * Constructs a new grindstone gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #GrindstoneGui(String)
     * @since 0.10.8
     */
    public GrindstoneGui(@NotNull String title, @NotNull Plugin plugin) {
        super(title, plugin);
    }

    /**
     * Constructs a new grindstone gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #GrindstoneGui(TextHolder)
     * @since 0.10.8
     */
    public GrindstoneGui(@NotNull TextHolder title, @NotNull Plugin plugin) {
        super(title, plugin);
    }

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        if (!(humanEntity instanceof Player)) {
            throw new IllegalArgumentException("Grindstones can only be opened by players");
        }

        if (isDirty()) {
            this.inventory = createInventory();
            markChanges();
        }

        getInventory().clear();

        getItemsComponent().display(getInventory(), 0);
        getResultComponent().display(getInventory(), 2);
        getPlayerInventoryComponent().display();

        if (getPlayerInventoryComponent().hasItem()) {
            HumanEntityCache humanEntityCache = getHumanEntityCache();

            if (!humanEntityCache.contains(humanEntity)) {
                humanEntityCache.storeAndClear(humanEntity);
            }

            getPlayerInventoryComponent().placeItems(humanEntity.getInventory(), 0);
        }

        Inventory inventory = grindstoneInventory.openInventory((Player) humanEntity, getTitleHolder(), getTopItems());

        addInventory(inventory, this);
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public GrindstoneGui copy() {
        GrindstoneGui gui = new GrindstoneGui(getTitleHolder(), super.plugin);

        gui.itemsComponent = itemsComponent.copy();
        gui.resultComponent = resultComponent.copy();
        gui.playerInventoryComponent = playerInventoryComponent.copy();

        gui.setOnTopClick(this.onTopClick);
        gui.setOnBottomClick(this.onBottomClick);
        gui.setOnGlobalClick(this.onGlobalClick);
        gui.setOnOutsideClick(this.onOutsideClick);
        gui.setOnClose(this.onClose);

        return gui;
    }

    @Override
    public void click(@NotNull InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();

        if (rawSlot >= 0 && rawSlot <= 1) {
            getItemsComponent().click(this, event, rawSlot);
        } else if (rawSlot == 2) {
            getResultComponent().click(this, event, 0);
        } else {
            getPlayerInventoryComponent().click(this, event, rawSlot - 3);
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = createInventory();
        }

        return inventory;
    }

    @Contract(pure = true)
    @Override
    public boolean isPlayerInventoryUsed() {
        return getPlayerInventoryComponent().hasItem();
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory() {
		return getTitleHolder().asInventoryTitle(this, InventoryType.GRINDSTONE);
    }

    /**
     * Handles an incoming inventory click event
     *
     * @param event the event to handle
     * @since 0.8.0
     * @deprecated no longer used internally
     */
    @Deprecated
    public void handleClickEvent(@NotNull InventoryClickEvent event) {
        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();

        if (slot >= 3 && slot <= 38) {
            grindstoneInventory.sendItems(player, getTopItems(), event.getCurrentItem());
        } else if (slot >= 0 && slot <= 2) {
            grindstoneInventory.sendItems(player, getTopItems(), event.getCurrentItem());

            if (event.isCancelled()) {
                grindstoneInventory.clearCursor(player);
            }
        }
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

    /**
     * Gets the inventory component representing the items
     *
     * @return the items component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getItemsComponent() {
        return itemsComponent;
    }

    /**
     * Gets the inventory component representing the result
     *
     * @return the result component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getResultComponent() {
        return resultComponent;
    }

    /**
     * Gets the inventory component representing the player inventory
     *
     * @return the player inventory component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getPlayerInventoryComponent() {
        return playerInventoryComponent;
    }

    /**
     * Gets the top items
     *
     * @return the top items
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    private ItemStack[] getTopItems() {
        return new ItemStack[] {
            getItemsComponent().getItem(0, 0),
            getItemsComponent().getItem(0, 1),
            getResultComponent().getItem(0, 0)
        };
    }

    /**
     * Loads a grindstone gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded furnace gui
     * @see #load(Object, InputStream)
     * @since 0.10.8
     */
    @Nullable
    @Contract(pure = true)
    public static GrindstoneGui load(@NotNull Object instance, @NotNull InputStream inputStream,
                                     @NotNull Plugin plugin) {
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
     * Loads a grindstone gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded grindstone gui
     * @see #load(Object, Element)
     * @since 0.10.8
     */
    @NotNull
    public static GrindstoneGui load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        GrindstoneGui grindstoneGui = new GrindstoneGui(element.getAttribute("title"), plugin);
        grindstoneGui.initializeOrThrow(instance, element);

        if (element.hasAttribute("populate")) {
            return grindstoneGui;
        }

        NodeList childNodes = element.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node item = childNodes.item(index);

            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element componentElement = (Element) item;

            if (!componentElement.getTagName().equalsIgnoreCase("component")) {
                throw new XMLLoadException("Gui element contains non-component tags");
            }

            if (!componentElement.hasAttribute("name")) {
                throw new XMLLoadException("Component tag does not have a name specified");
            }

            InventoryComponent component;

            switch (componentElement.getAttribute("name")) {
                case "items":
                    component = grindstoneGui.getItemsComponent();
                    break;
                case "result":
                    component = grindstoneGui.getResultComponent();
                    break;
                case "player-inventory":
                    component = grindstoneGui.getPlayerInventoryComponent();
                    break;
                default:
                    throw new XMLLoadException("Unknown component name");
            }

            component.load(instance, componentElement, plugin);
        }

        return grindstoneGui;
    }

    /**
     * Loads a grindstone gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @return the loaded furnace gui
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    public static GrindstoneGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
        return load(instance, inputStream, JavaPlugin.getProvidingPlugin(GrindstoneGui.class));
    }

    /**
     * Loads a grindstone gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @return the loaded grindstone gui
     * @since 0.8.0
     */
    @NotNull
    public static GrindstoneGui load(@NotNull Object instance, @NotNull Element element) {
        return load(instance, element, JavaPlugin.getProvidingPlugin(GrindstoneGui.class));
    }
}
