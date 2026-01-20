package com.github.stefvanschie.inventoryframework.gui.type;

import com.github.stefvanschie.inventoryframework.HumanEntityCache;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.gui.GuiComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
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
 * Represents a gui in the form of a brewing stand
 *
 * @since 0.8.0
 */
public class BrewingStandGui extends NamedGui implements InventoryBased {

    /**
     * Represents the gui component for the first bottle
     */
    @NotNull
    private GuiComponent firstBottleComponent = new GuiComponent(1, 1);

    /**
     * Represents the gui component for the second bottle
     */
    @NotNull
    private GuiComponent secondBottleComponent = new GuiComponent(1, 1);

    /**
     * Represents the ui component for the third bottle
     */
    @NotNull
    private GuiComponent thirdBottleComponent = new GuiComponent(1, 1);

    /**
     * Represents the gui component for the potion ingredient
     */
    @NotNull
    private GuiComponent potionIngredientComponent = new GuiComponent(1, 1);

    /**
     * Represents the gui component for the blaze powder
     */
    @NotNull
    private GuiComponent blazePowderComponent = new GuiComponent(1, 1);

    /**
     * Represents the gui component for the player inventory
     */
    @NotNull
    private GuiComponent playerGuiComponent = new GuiComponent(9, 4);

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

    /**
     * Constructs a new brewing stand gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #BrewingStandGui(String)
     * @since 0.10.8
     */
    public BrewingStandGui(@NotNull String title, @NotNull Plugin plugin) {
        super(title, plugin);
    }

    /**
     * Constructs a new brewing stand gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #BrewingStandGui(TextHolder)
     * @since 0.10.8
     */
    public BrewingStandGui(@NotNull TextHolder title, @NotNull Plugin plugin) {
        super(title, plugin);
    }

    @Override
    public void update() {
        if (isDirty()) {
            Inventory oldInventory = this.inventory;
            this.inventory = createInventory();

            if (oldInventory != null) {
                for (HumanEntity viewer : new ArrayList<>(oldInventory.getViewers())) {
                    viewer.openInventory(this.inventory);
                }
            }

            markChanges();
        }

        getInventory().clear();

        getFirstBottleComponent().display(getInventory(), 0);
        getSecondBottleComponent().display(getInventory(), 1);
        getThirdBottleComponent().display(getInventory(), 2);
        getPotionIngredientComponent().display(getInventory(), 3);
        getBlazePowderComponent().display(getInventory(), 4);
        getPlayerGuiComponent().display();

        super.updating = true;

        for (HumanEntity viewer : getViewers()) {
            ItemStack cursor = viewer.getItemOnCursor();
            viewer.setItemOnCursor(new ItemStack(Material.AIR));

            populateBottomInventory(viewer);

            viewer.setItemOnCursor(cursor);
        }

        if (!super.updating)
            throw new AssertionError("Gui#isUpdating became false before Gui#update finished");

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
        if (getPlayerGuiComponent().hasItem()) {
            HumanEntityCache humanEntityCache = getHumanEntityCache();

            if (!humanEntityCache.contains(humanEntity)) {
                humanEntityCache.storeAndClear(humanEntity);
            }

            getPlayerGuiComponent().placeItems(humanEntity.getInventory(), 0);
        }
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public BrewingStandGui copy() {
        BrewingStandGui gui = new BrewingStandGui(getTitleHolder(), super.plugin);

        gui.firstBottleComponent = firstBottleComponent.copy();
        gui.secondBottleComponent = secondBottleComponent.copy();
        gui.thirdBottleComponent = thirdBottleComponent.copy();
        gui.potionIngredientComponent = potionIngredientComponent.copy();
        gui.blazePowderComponent = blazePowderComponent.copy();
        gui.playerGuiComponent = this.playerGuiComponent.copy();

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
            getPlayerGuiComponent().click(this, event, rawSlot - 5);
        }
    }

    @Contract(pure = true)
    @Override
    public boolean isPlayerInventoryUsed() {
        return getPlayerGuiComponent().hasItem();
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
     * Gets the gui component representing the first bottle
     *
     * @return the first bottle component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getFirstBottleComponent() {
        return firstBottleComponent;
    }

    /**
     * Gets the gui component representing the second bottle
     *
     * @return the second bottle component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getSecondBottleComponent() {
        return secondBottleComponent;
    }

    /**
     * Gets the gui component representing the third bottle
     *
     * @return the third bottle component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getThirdBottleComponent() {
        return thirdBottleComponent;
    }

    /**
     * Gets the gui component representing the potion ingredient
     *
     * @return the potion ingredient component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getPotionIngredientComponent() {
        return potionIngredientComponent;
    }

    /**
     * Gets the gui component representing the blaze powder
     *
     * @return the blaze powder component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getBlazePowderComponent() {
        return blazePowderComponent;
    }

    /**
     * Gets the gui component representing the player inventory
     *
     * @return the player gui component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getPlayerGuiComponent() {
        return this.playerGuiComponent;
    }

    /**
     * Loads a brewing stand gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded brewing stand gui
     * @see #load(Object, InputStream)
     * @since 0.10.8
     */
    @Nullable
    @Contract(pure = true)
    public static BrewingStandGui load(@NotNull Object instance, @NotNull InputStream inputStream,
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
     * Loads a brewing stand gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded brewing stand gui
     * @see #load(Object, Element)
     * @since 0.10.8
     */
    @NotNull
    public static BrewingStandGui load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
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

            GuiComponent component;

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
                    component = brewingStandGui.getPlayerGuiComponent();
                    break;
                default:
                    throw new XMLLoadException("Unknown component name");
            }

            component.load(instance, componentElement, plugin);
        }

        return brewingStandGui;
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
        return load(instance, inputStream, JavaPlugin.getProvidingPlugin(BrewingStandGui.class));
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
        return load(instance, element, JavaPlugin.getProvidingPlugin(BrewingStandGui.class));
    }
}
