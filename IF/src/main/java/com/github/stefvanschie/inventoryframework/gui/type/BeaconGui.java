package com.github.stefvanschie.inventoryframework.gui.type;

import com.github.stefvanschie.inventoryframework.HumanEntityCache;
import com.github.stefvanschie.inventoryframework.abstraction.BeaconInventory;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased;
import com.github.stefvanschie.inventoryframework.util.version.Version;
import com.github.stefvanschie.inventoryframework.util.version.VersionMatcher;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
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
 * Represents a gui in the form of a beacon
 *
 * @since 0.8.0
 */
public class BeaconGui extends Gui implements InventoryBased {

    /**
     * Represents the payment item inventory component
     */
    @NotNull
    private InventoryComponent paymentItemComponent = new InventoryComponent(1, 1);

    /**
     * Represents the player inventory component
     */
    @NotNull
    private InventoryComponent playerInventoryComponent = new InventoryComponent(9, 4);

    /**
     * An internal beacon inventory
     */
    @NotNull
    private final BeaconInventory beaconInventory = VersionMatcher.newBeaconInventory(Version.getVersion());

    /**
     * Constructs a new beacon gui.
     *
     * @since 0.8.0
     */
    public BeaconGui() {
        this(JavaPlugin.getProvidingPlugin(BeaconGui.class));
    }

    /**
     * Constructs a new beacon gui for the given {@code plugin}.
     *
     * @param plugin the owning plugin of this gui
     * @see #BeaconGui()
     * @since 0.10.8
     */
    public BeaconGui(@NotNull Plugin plugin) {
        super(plugin);
    }

    @Override
    public void update() {
        super.updating = true;

        getInventory().clear();

        getPaymentItemComponent().display(getInventory(), 0);
        getPlayerInventoryComponent().display();

        for (HumanEntity viewer : getViewers()) {
            ItemStack cursor = viewer.getItemOnCursor();
            viewer.setItemOnCursor(new ItemStack(Material.AIR));

            populateBottomInventory(viewer);

            viewer.setItemOnCursor(cursor);
        }

        if (!super.updating) {
            throw new AssertionError("Gui#isUpdating became false before Gui#update finished");
        }

        super.updating = false;
    }

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        if (super.inventory == null) {
            update();
        }

        populateBottomInventory(humanEntity);

        humanEntity.openInventory(getInventory());
    }

    /**
     * Populates the inventory of the {@link HumanEntity} if needed.
     *
     * @param humanEntity the human entity
     * @since 0.11.4
     */
    private void populateBottomInventory(@NotNull HumanEntity humanEntity) {
        if (getPlayerInventoryComponent().hasItem()) {
            HumanEntityCache humanEntityCache = getHumanEntityCache();

            if (!humanEntityCache.contains(humanEntity)) {
                humanEntityCache.storeAndClear(humanEntity);
            }

            getPlayerInventoryComponent().placeItems(humanEntity.getInventory(), 0);
        }
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public BeaconGui copy() {
        BeaconGui gui = new BeaconGui(super.plugin);

        gui.paymentItemComponent = paymentItemComponent.copy();
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

        if (rawSlot == 0) {
            getPaymentItemComponent().click(this, event, 0);
        } else {
            getPlayerInventoryComponent().click(this, event, rawSlot - 1);
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
        Inventory inventory = this.beaconInventory.createInventory();

        addInventory(inventory, this);

        return inventory;
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
     * Gets the inventory component representing the payment item
     *
     * @return the payment item component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getPaymentItemComponent() {
        return paymentItemComponent;
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
     * Loads a beacon gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded beacon gui
     * @see #load(Object, InputStream)
     * @since 0.10.8
     */
    @Nullable
    @Contract(pure = true)
    public static BeaconGui load(@NotNull Object instance, @NotNull InputStream inputStream, @NotNull Plugin plugin) {
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
     * Loads a beacon gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded beacon gui
     * @see #load(Object, Element)
     * @since 0.10.8
     */
    @NotNull
    public static BeaconGui load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        BeaconGui beaconGui = new BeaconGui(plugin);
        beaconGui.initializeOrThrow(instance, element);

        if (element.hasAttribute("populate")) {
            return beaconGui;
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
                case "payment-item":
                    component = beaconGui.getPaymentItemComponent();
                    break;
                case "player-inventory":
                    component = beaconGui.getPlayerInventoryComponent();
                    break;
                default:
                    throw new XMLLoadException("Unknown component name");
            }

            component.load(instance, componentElement, plugin);
        }

        return beaconGui;
    }

    /**
     * Loads a beacon gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @return the loaded beacon gui
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    public static BeaconGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
        return load(instance, inputStream, JavaPlugin.getProvidingPlugin(BeaconGui.class));
    }

    /**
     * Loads a beacon gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @return the loaded beacon gui
     * @since 0.8.0
     */
    @NotNull
    public static BeaconGui load(@NotNull Object instance, @NotNull Element element) {
        return load(instance, element, JavaPlugin.getProvidingPlugin(BeaconGui.class));
    }
}
