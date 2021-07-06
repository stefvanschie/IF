package com.github.stefvanschie.inventoryframework.gui.type;

import com.github.stefvanschie.inventoryframework.abstraction.GrindstoneInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
import com.github.stefvanschie.inventoryframework.util.version.Version;
import com.github.stefvanschie.inventoryframework.util.version.VersionMatcher;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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

/**
 * Represents a gui in the form of a grindstone
 *
 * @since 0.8.0
 */
public class GrindstoneGui extends NamedGui {

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

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        if (!(humanEntity instanceof Player)) {
            throw new IllegalArgumentException("Grindstones can only be opened by players");
        }

        getInventory().clear();

        getHumanEntityCache().store(humanEntity);

        getItemsComponent().display(getInventory(), 0);
        getResultComponent().display(getInventory(), 2);
        getPlayerInventoryComponent().display();

        if (getPlayerInventoryComponent().hasItem()) {
            humanEntity.getInventory().clear();

            getPlayerInventoryComponent().placeItems(humanEntity.getInventory(), 0);
        } else {
            getHumanEntityCache().clearCache(humanEntity);
        }

        grindstoneInventory.openInventory((Player) humanEntity, getTitle(), getTopItems());
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public GrindstoneGui copy() {
        GrindstoneGui gui = new GrindstoneGui(getTitle());

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

    @Contract(pure = true)
    @Override
    public boolean isPlayerInventoryUsed() {
        return getPlayerInventoryComponent().hasItem();
    }

    @NotNull
    @Contract(pure = true)
    @Override
    protected Inventory createInventory() {
		return getTitleHolder().asInventoryTitle(this, InventoryType.GRINDSTONE);
    }

    /**
     * Handles an incoming inventory click event
     *
     * @param event the event to handle
     * @since 0.8.0
     */
    public void handleClickEvent(@NotNull InventoryClickEvent event) {
        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();

        if (slot >= 3 && slot <= 38) {
            grindstoneInventory.sendItems(player, getTopItems());
        } else if (slot >= 0 && slot <= 2) {
            grindstoneInventory.sendItems(player, getTopItems());

            if (event.isCancelled()) {
                grindstoneInventory.clearCursor(player);
            }
        }
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
     * @return the loaded furnace gui
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    public static GrindstoneGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            Element documentElement = document.getDocumentElement();

            documentElement.normalize();

            return load(instance, documentElement);
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
     * @return the loaded grindstone gui
     * @since 0.8.0
     */
    @NotNull
    public static GrindstoneGui load(@NotNull Object instance, @NotNull Element element) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        GrindstoneGui grindstoneGui = new GrindstoneGui(element.getAttribute("title"));
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

            component.load(instance, componentElement);
        }

        return grindstoneGui;
    }
}
