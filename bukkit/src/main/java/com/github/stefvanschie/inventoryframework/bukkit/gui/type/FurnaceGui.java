package com.github.stefvanschie.inventoryframework.bukkit.gui.type;

import com.github.stefvanschie.inventoryframework.core.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.core.gui.type.AbstractFurnaceGui;
import com.github.stefvanschie.inventoryframework.bukkit.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.bukkit.gui.type.util.NamedGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
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
 * Represents a gui in the form of a furnace
 *
 * @since 0.8.0
 */
public class FurnaceGui extends NamedGui implements AbstractFurnaceGui {

    /**
     * Represents the inventory component for the ingredient
     */
    @NotNull
    private InventoryComponent ingredientComponent = new InventoryComponent(1, 1);

    /**
     * Represents the inventory component for the fuel
     */
    @NotNull
    private InventoryComponent fuelComponent = new InventoryComponent(1, 1);

    /**
     * Represents the inventory component for the output
     */
    @NotNull
    private InventoryComponent outputComponent = new InventoryComponent(1, 1);

    /**
     * Represents the inventory component for the player inventory
     */
    @NotNull
    private InventoryComponent playerInventoryComponent = new InventoryComponent(9, 4);

    /**
     * Constructs a new GUI
     *
     * @param title the title/name of this gui.
     * @since 0.8.0
     */
    public FurnaceGui(@NotNull String title) {
        super(title);
    }

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        getInventory().clear();

        getHumanEntityCache().store(humanEntity);

        getIngredientComponent().display(getInventory(), 0);
        getFuelComponent().display(getInventory(), 1);
        getOutputComponent().display(getInventory(), 2);
        getPlayerInventoryComponent().display();

        if (getPlayerInventoryComponent().hasItem()) {
            humanEntity.getInventory().clear();

            getPlayerInventoryComponent().placeItems(humanEntity.getInventory(), 0);
        } else {
            getHumanEntityCache().clearCache(humanEntity);
        }

        humanEntity.openInventory(getInventory());
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public FurnaceGui copy() {
        FurnaceGui gui = new FurnaceGui(getTitle());

        gui.ingredientComponent = ingredientComponent.copy();
        gui.fuelComponent = fuelComponent.copy();
        gui.outputComponent = outputComponent.copy();
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
            getIngredientComponent().click(this, event, 0);
        } else if (rawSlot == 1) {
            getFuelComponent().click(this, event, 0);
        } else if (rawSlot == 2) {
            getOutputComponent().click(this, event, 0);
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
    public Inventory createInventory(@NotNull String title) {
        Inventory inventory = Bukkit.createInventory(this, InventoryType.FURNACE, title);

        addInventory(inventory, this);

        return inventory;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public InventoryComponent getIngredientComponent() {
        return ingredientComponent;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public InventoryComponent getFuelComponent() {
        return fuelComponent;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public InventoryComponent getOutputComponent() {
        return outputComponent;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public InventoryComponent getPlayerInventoryComponent() {
        return playerInventoryComponent;
    }

    /**
     * Loads a furnace gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @return the loaded furnace gui
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    public static FurnaceGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
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
     * Loads a furnace gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @return the loaded furnace gui
     * @since 0.8.0
     */
    @NotNull
    public static FurnaceGui load(@NotNull Object instance, @NotNull Element element) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        FurnaceGui furnaceGui = new FurnaceGui(element.getAttribute("title"));
        furnaceGui.initializeOrThrow(instance, element);

        if (element.hasAttribute("populate")) {
            return furnaceGui;
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
                case "ingredient":
                    component = furnaceGui.getIngredientComponent();
                    break;
                case "fuel":
                    component = furnaceGui.getFuelComponent();
                    break;
                case "output":
                    component = furnaceGui.getOutputComponent();
                    break;
                case "player-inventory":
                    component = furnaceGui.getPlayerInventoryComponent();
                    break;
                default:
                    throw new XMLLoadException("Unknown component name");
            }

            component.load(instance, componentElement);
        }

        return furnaceGui;
    }
}
