package com.github.stefvanschie.inventoryframework.gui.type;

import com.github.stefvanschie.inventoryframework.HumanEntityCache;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a gui in the form of a brewing stand
 *
 * @since 0.8.0
 */
public class BrewingStandGui extends NamedGui implements InventoryBased {

    /**
     * Represents the inventory component for the first bottle
     */
    @NotNull
    private InventoryComponent firstBottleComponent = new InventoryComponent(1, 1);

    /**
     * Represents the inventory component for the second bottle
     */
    @NotNull
    private InventoryComponent secondBottleComponent = new InventoryComponent(1, 1);

    /**
     * Represents the inventory component for the third bottle
     */
    @NotNull
    private InventoryComponent thirdBottleComponent = new InventoryComponent(1, 1);

    /**
     * Represents the inventory component for the potion ingredient
     */
    @NotNull
    private InventoryComponent potionIngredientComponent = new InventoryComponent(1, 1);

    /**
     * Represents the inventory component for the blaze powder
     */
    @NotNull
    private InventoryComponent blazePowderComponent = new InventoryComponent(1, 1);

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
    public BrewingStandGui(@NotNull String title) {
        super(title);
    }

    /**
     * Constructs a new GUI
     *
     * @param title the title/name of this gui.
     * @since 0.10.0
     */
    public BrewingStandGui(@NotNull TextHolder title) {
        super(title);
    }

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        if (isDirty()) {
            this.inventory = createInventory();
            markChanges();
        }

        getInventory().clear();

        getFirstBottleComponent().display(getInventory(), 0);
        getSecondBottleComponent().display(getInventory(), 1);
        getThirdBottleComponent().display(getInventory(), 2);
        getPotionIngredientComponent().display(getInventory(), 3);
        getBlazePowderComponent().display(getInventory(), 4);
        getPlayerInventoryComponent().display();

        if (getPlayerInventoryComponent().hasItem()) {
            HumanEntityCache humanEntityCache = getHumanEntityCache();

            if (!humanEntityCache.contains(humanEntity)) {
                humanEntityCache.storeAndClear(humanEntity);
            }

            getPlayerInventoryComponent().placeItems(humanEntity.getInventory(), 0);
        }

        humanEntity.openInventory(getInventory());
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public BrewingStandGui copy() {
        BrewingStandGui gui = new BrewingStandGui(getTitleHolder());

        gui.firstBottleComponent = firstBottleComponent.copy();
        gui.secondBottleComponent = secondBottleComponent.copy();
        gui.thirdBottleComponent = thirdBottleComponent.copy();
        gui.potionIngredientComponent = potionIngredientComponent.copy();
        gui.blazePowderComponent = blazePowderComponent.copy();
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
            getFirstBottleComponent().click(this, event, 0);
        } else if (rawSlot == 1) {
            getSecondBottleComponent().click(this, event, 0);
        } else if (rawSlot == 2) {
            getThirdBottleComponent().click(this, event, 0);
        } else if (rawSlot == 3) {
            getPotionIngredientComponent().click(this, event, 0);
        } else if (rawSlot == 4) {
            getBlazePowderComponent().click(this, event, 0);
        } else {
            getPlayerInventoryComponent().click(this, event, rawSlot - 5);
        }
    }

    @Contract(pure = true)
    @Override
    public boolean isPlayerInventoryUsed() {
        return getPlayerInventoryComponent().hasItem();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = createInventory();
        }

        return inventory;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory() {
        Inventory inventory = getTitleHolder().asInventoryTitle(this, InventoryType.BREWING);

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
     * Gets the inventory component representing the first bottle
     *
     * @return the first bottle component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getFirstBottleComponent() {
        return firstBottleComponent;
    }

    /**
     * Gets the inventory component representing the second bottle
     *
     * @return the second bottle component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getSecondBottleComponent() {
        return secondBottleComponent;
    }

    /**
     * Gets the inventory component representing the third bottle
     *
     * @return the third bottle component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getThirdBottleComponent() {
        return thirdBottleComponent;
    }

    /**
     * Gets the inventory component representing the potion ingredient
     *
     * @return the potion ingredient component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getPotionIngredientComponent() {
        return potionIngredientComponent;
    }

    /**
     * Gets the inventory component representing the blaze powder
     *
     * @return the blaze powder component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getBlazePowderComponent() {
        return blazePowderComponent;
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
     * Loads a brewing stand gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @return the loaded brewing stand gui
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    public static BrewingStandGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
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
     * Loads a brewing stand gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @return the loaded brewing stand gui
     * @since 0.8.0
     */
    @NotNull
    public static BrewingStandGui load(@NotNull Object instance, @NotNull Element element) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        BrewingStandGui brewingStandGui = new BrewingStandGui(element.getAttribute("title"));
        brewingStandGui.initializeOrThrow(instance, element);

        if (element.hasAttribute("populate")) {
            return brewingStandGui;
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
                case "first-bottle":
                    component = brewingStandGui.getFirstBottleComponent();
                    break;
                case "second-bottle":
                    component = brewingStandGui.getSecondBottleComponent();
                    break;
                case "third-bottle":
                    component = brewingStandGui.getThirdBottleComponent();
                    break;
                case "potion-ingredient":
                    component = brewingStandGui.getPotionIngredientComponent();
                    break;
                case "blaze-powder":
                    component = brewingStandGui.getBlazePowderComponent();
                    break;
                case "player-inventory":
                    component = brewingStandGui.getPlayerInventoryComponent();
                    break;
                default:
                    throw new XMLLoadException("Unknown component name");
            }

            component.load(instance, componentElement);
        }

        return brewingStandGui;
    }
}
