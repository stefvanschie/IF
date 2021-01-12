package com.github.stefvanschie.inventoryframework.bukkit.gui.type;

import com.github.stefvanschie.inventoryframework.bukkit.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.bukkit.gui.type.util.NamedGui;
import com.github.stefvanschie.inventoryframework.bukkit.util.version.Version;
import com.github.stefvanschie.inventoryframework.bukkit.util.version.VersionMatcher;
import com.github.stefvanschie.inventoryframework.core.gui.type.AbstractStonecutterGui;
import com.github.stefvanschie.inventoryframework.abstraction.StonecutterInventory;
import com.github.stefvanschie.inventoryframework.core.exception.XMLLoadException;
import org.bukkit.Bukkit;
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
 * Represents a gui in the form of a stonecutter
 *
 * @since 0.8.0
 */
public class StonecutterGui extends NamedGui implements AbstractStonecutterGui {

    /**
     * Represents the inventory component for the input
     */
    @NotNull
    private InventoryComponent inputComponent = new InventoryComponent(1, 1);

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
     * An internal stonecutter inventory
     */
    @NotNull
    private final StonecutterInventory stonecutterInventory = VersionMatcher.newStonecutterInventory(
        Version.getVersion(), this
    );

    /**
     * Constructs a new GUI
     *
     * @param title the title/name of this gui.
     * @since 0.8.0
     */
    public StonecutterGui(@NotNull String title) {
        super(title);
    }

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        if (!(humanEntity instanceof Player)) {
            throw new IllegalArgumentException("Enchanting tables can only be opened by players");
        }

        getInventory().clear();

        getHumanEntityCache().store(humanEntity);

        getInputComponent().display(getInventory(), 0);
        getResultComponent().display(getInventory(), 1);
        getPlayerInventoryComponent().display();

        if (getPlayerInventoryComponent().hasItem()) {
            humanEntity.getInventory().clear();

            getPlayerInventoryComponent().placeItems(humanEntity.getInventory(), 0);
        } else {
            getHumanEntityCache().clearCache(humanEntity);
        }

        stonecutterInventory.openInventory((Player) humanEntity, getTitle(), getTopItems());
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public StonecutterGui copy() {
        StonecutterGui gui = new StonecutterGui(getTitle());

        gui.inputComponent = inputComponent.copy();
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

        if (rawSlot == 0) {
            getInputComponent().click(this, event, 0);
        } else if (rawSlot == 1) {
            getResultComponent().click(this, event, 0);
        } else {
            getPlayerInventoryComponent().click(this, event, rawSlot - 2);
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
    public Inventory createInventory(@NotNull String title) {
        return Bukkit.createInventory(this, InventoryType.STONECUTTER, title);
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

        if (slot >= 2 && slot <= 37) {
            stonecutterInventory.sendItems(player, getTopItems());
        } else if (slot == 0 || slot == 1) {
            stonecutterInventory.sendItems(player, getTopItems());

            if (event.isCancelled()) {
                stonecutterInventory.clearCursor(player);
            }
        }
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public InventoryComponent getInputComponent() {
        return inputComponent;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public InventoryComponent getResultComponent() {
        return resultComponent;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public InventoryComponent getPlayerInventoryComponent() {
        return playerInventoryComponent;
    }

    /**
     * Get the top items
     *
     * @return the top items
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    private ItemStack[] getTopItems() {
        return new ItemStack[] {
            getInputComponent().getItem(0, 0),
            getResultComponent().getItem(0, 0)
        };
    }

    /**
     * Loads a stone cutter gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @return the loaded stone cutter gui
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    public static StonecutterGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
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
     * Loads a stonecutter gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @return the loaded stonecutter gui
     * @since 0.8.0
     */
    @NotNull
    public static StonecutterGui load(@NotNull Object instance, @NotNull Element element) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        StonecutterGui stonecutterGui = new StonecutterGui(element.getAttribute("title"));
        stonecutterGui.initializeOrThrow(instance, element);

        if (element.hasAttribute("populate")) {
            return stonecutterGui;
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
                case "input":
                    component = stonecutterGui.getInputComponent();
                    break;
                case "result":
                    component = stonecutterGui.getResultComponent();
                    break;
                case "player-inventory":
                    component = stonecutterGui.getPlayerInventoryComponent();
                    break;
                default:
                    throw new XMLLoadException("Unknown component name");
            }

            component.load(instance, componentElement);
        }

        return stonecutterGui;
    }
}
