package com.github.stefvanschie.inventoryframework.gui.type;

import com.github.stefvanschie.inventoryframework.HumanEntityCache;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.gui.GuiComponent;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Represents a gui in the form of a smoker
 *
 * @since 0.8.0
 */
public class SmokerGui extends NamedGui implements InventoryBased {

    /**
     * Represents the gui component for the ingredient
     */
    @NotNull
    private GuiComponent ingredientComponent = new GuiComponent(1, 1);

    /**
     * Represents the gui component for the fuel
     */
    @NotNull
    private GuiComponent fuelComponent = new GuiComponent(1, 1);

    /**
     * Represents the gui component for the output
     */
    @NotNull
    private GuiComponent outputComponent = new GuiComponent(1, 1);

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
    public SmokerGui(@NotNull String title) {
        super(title);
    }

    /**
     * Constructs a new GUI
     *
     * @param title the title/name of this gui.
     * @since 0.10.0
     */
    public SmokerGui(@NotNull TextHolder title) {
        super(title);
    }

    /**
     * Constructs a new smoker gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #SmokerGui(String)
     * @since 0.10.8
     */
    public SmokerGui(@NotNull String title, @NotNull Plugin plugin) {
        super(title, plugin);
    }

    /**
     * Constructs a new smoker gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #SmokerGui(TextHolder)
     * @since 0.10.8
     */
    public SmokerGui(@NotNull TextHolder title, @NotNull Plugin plugin) {
        super(title, plugin);
    }

    @Override
    public void update() {
        super.updating = true;

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

        getIngredientComponent().display(getInventory(), 0);
        getFuelComponent().display(getInventory(), 1);
        getOutputComponent().display(getInventory(), 2);
        getPlayerGuiComponent().display();

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

    @NotNull
    @Contract(pure = true)
    @Override
    public Iterable<? extends GuiItem> getItems() {
        Collection<@NotNull GuiItem> items = new HashSet<>();

        for (Pane pane : getIngredientComponent().getPanes()) {
            items.addAll(pane.getItems());
        }

        for (Pane pane : getFuelComponent().getPanes()) {
            items.addAll(pane.getItems());
        }

        for (Pane pane : getOutputComponent().getPanes()) {
            items.addAll(pane.getItems());
        }

        for (Pane pane : getPlayerGuiComponent().getPanes()) {
            items.addAll(pane.getItems());
        }

        return items;
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
    public SmokerGui copy() {
        SmokerGui gui = new SmokerGui(getTitleHolder(), super.plugin);

        gui.ingredientComponent = ingredientComponent.copy();
        gui.fuelComponent = fuelComponent.copy();
        gui.outputComponent = outputComponent.copy();
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
            getIngredientComponent().click(this, event, 0);
        } else if (rawSlot == 1) {
            getFuelComponent().click(this, event, 0);
        } else if (rawSlot == 2) {
            getOutputComponent().click(this, event, 0);
        } else {
            getPlayerGuiComponent().click(this, event, rawSlot - 3);
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
        return getPlayerGuiComponent().hasItem();
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory() {
        return getTitleHolder().asInventoryTitle(this, InventoryType.SMOKER);
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
     * Gets the gui component representing the ingredient
     *
     * @return the ingredient component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getIngredientComponent() {
        return ingredientComponent;
    }

    /**
     * Gets the gui component representing the fuel
     *
     * @return the fuel component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getFuelComponent() {
        return fuelComponent;
    }

    /**
     * Gets the gui component representing the output
     *
     * @return the output component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getOutputComponent() {
        return outputComponent;
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
     * Loads a smoker gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded smoker gui
     * @see #load(Object, InputStream)
     * @since 0.10.8
     */
    @Nullable
    @Contract(pure = true)
    public static SmokerGui load(@NotNull Object instance, @NotNull InputStream inputStream, @NotNull Plugin plugin) {
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
     * Loads a smoker gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded smoker gui
     * @see #load(Object, Element)
     * @since 0.10.8
     */
    @NotNull
    public static SmokerGui load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        SmokerGui smokerGui = new SmokerGui(element.getAttribute("title"), plugin);
        smokerGui.initializeOrThrow(instance, element);

        if (element.hasAttribute("populate")) {
            return smokerGui;
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
                case "ingredient":
                    component = smokerGui.getIngredientComponent();
                    break;
                case "fuel":
                    component = smokerGui.getFuelComponent();
                    break;
                case "output":
                    component = smokerGui.getOutputComponent();
                    break;
                case "player-inventory":
                    component = smokerGui.getPlayerGuiComponent();
                    break;
                default:
                    throw new XMLLoadException("Unknown component name");
            }

            component.load(instance, componentElement, plugin);
        }

        return smokerGui;
    }

    /**
     * Loads a smoker gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @return the loaded smoker gui
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    public static SmokerGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
        return load(instance, inputStream, JavaPlugin.getProvidingPlugin(SmokerGui.class));
    }

    /**
     * Loads a smoker gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @return the loaded smoker gui
     * @since 0.8.0
     */
    @NotNull
    public static SmokerGui load(@NotNull Object instance, @NotNull Element element) {
        return load(instance, element, JavaPlugin.getProvidingPlugin(SmokerGui.class));
    }
}
