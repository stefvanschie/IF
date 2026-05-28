package com.github.stefvanschie.inventoryframework.gui.type;

import com.github.stefvanschie.inventoryframework.HumanEntityCache;
import com.github.stefvanschie.inventoryframework.abstraction.LoomInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.gui.GuiComponent;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Represents a gui in the form of a loom.
 *
 * @since 0.12.1
 */
public class LoomGui extends NamedGui implements InventoryBased {

    /**
     * Represents the gui component for the banner.
     */
    @NotNull
    private GuiComponent bannerComponent = new GuiComponent(1, 1);

    /**
     * Represents the gui component for the dye.
     */
    @NotNull
    private GuiComponent dyeComponent = new GuiComponent(1, 1);

    /**
     * Represents the gui component for the pattern.
     */
    @NotNull
    private GuiComponent patternComponent = new GuiComponent(1, 1);

    /**
     * Represents the gui component for the output.
     */
    @NotNull
    private GuiComponent outputComponent = new GuiComponent(1, 1);

    /**
     * Represents the gui component for the player inventory
     */
    @NotNull
    private GuiComponent playerGuiComponent = new GuiComponent(9, 4);

    /**
     * An internal loom table inventory
     */
    @NotNull
    private final LoomInventory loomInventory = VersionMatcher.newLoomInventory(Version.getVersion());

    /**
     * Constructs a new gui.
     *
     * @param title the title/name of this gui.
     * @since 0.12.1
     */
    public LoomGui(@NotNull String title) {
        super(title);
    }

    /**
     * Constructs a new gui.
     *
     * @param title the title/name of this gui.
     * @since 0.12.1
     */
    public LoomGui(@NotNull TextHolder title) {
        super(title);
    }

    /**
     * Constructs a new loom gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #LoomGui(String)
     * @since 0.12.1
     */
    public LoomGui(@NotNull String title, @NotNull Plugin plugin) {
        super(title, plugin);
    }

    /**
     * Constructs a new loom gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #LoomGui(TextHolder)
     * @since 0.12.1
     */
    public LoomGui(@NotNull TextHolder title, @NotNull Plugin plugin) {
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

        getBannerComponent().display(getInventory(), 0);
        getDyeComponent().display(getInventory(), 1);
        getPatternComponent().display(getInventory(), 2);
        getOutputComponent().display(getInventory(), 3);
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

        for (Pane pane : getBannerComponent().getPanes()) {
            items.addAll(pane.getItems());
        }

        for (Pane pane : getDyeComponent().getPanes()) {
            items.addAll(pane.getItems());
        }

        for (Pane pane : getPatternComponent().getPanes()) {
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
        if (isDirty()) {
            update();
        }

        populateBottomInventory(humanEntity);

        humanEntity.openInventory(getInventory());
    }

    /**
     * Populates the inventory of the {@link HumanEntity} if needed.
     *
     * @param humanEntity the human entity
     * @since 0.12.1
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
    public LoomGui copy() {
        LoomGui gui = new LoomGui(getTitleHolder(), super.plugin);

        gui.bannerComponent = this.bannerComponent.copy();
        gui.dyeComponent = this.dyeComponent.copy();
        gui.patternComponent = this.patternComponent.copy();
        gui.outputComponent = this.outputComponent.copy();
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
            getBannerComponent().click(this, event, 0);
        } else if (rawSlot == 1) {
            getDyeComponent().click(this, event, 0);
        } else if (rawSlot == 2) {
            getPatternComponent().click(this, event, 0);
        } else if (rawSlot == 3) {
            getOutputComponent().click(this, event, 0);
        } else {
            getPlayerGuiComponent().click(this, event, rawSlot - 4);
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = createInventory();
        }

        return this.inventory;
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
        Inventory inventory = this.loomInventory.createInventory(getTitleHolder());

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
     * Gets the gui component representing the banner. Note that this component may only contain banner items, otherwise
     * the clients of the viewers will crash. Attempting to set any other item will throw an
     * {@link IllegalArgumentException} when showing or updating the gui to prevent clients from crashing.
     *
     * @return the banner component
     * @since 0.12.1
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getBannerComponent() {
        return this.bannerComponent;
    }

    /**
     * Gets the gui component representing the dye.
     *
     * @return the dye component
     * @since 0.12.1
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getDyeComponent() {
        return this.dyeComponent;
    }

    /**
     * Gets the gui component representing the pattern.
     *
     * @return the pattern component
     * @since 0.12.1
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getPatternComponent() {
        return this.patternComponent;
    }

    /**
     * Gets the gui component representing the output. Note that this component may only contain banner items, otherwise
     * the clients of the viewers will crash. Attempting to set any other item will throw an
     * {@link IllegalArgumentException} when showing or updating the gui to prevent clients from crashing.
     *
     * @return the output component
     * @since 0.12.1
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getOutputComponent() {
        return this.outputComponent;
    }

    /**
     * Gets the gui component representing the player inventory.
     *
     * @return the player gui component
     * @since 0.12.1
     */
    @NotNull
    @Contract(pure = true)
    public GuiComponent getPlayerGuiComponent() {
        return this.playerGuiComponent;
    }

    /**
     * Loads a cartography table gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded cartography table gui
     * @see #load(Object, InputStream)
     * @since 0.12.1
     */
    @Nullable
    @Contract(pure = true)
    public static LoomGui load(@NotNull Object instance, @NotNull InputStream inputStream,
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
     * Loads a loom gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded loom gui
     * @see #load(Object, Element)
     * @since 0.12.1
     */
    @NotNull
    public static LoomGui load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        LoomGui loomGui = new LoomGui(element.getAttribute("title"), plugin);
        loomGui.initializeOrThrow(instance, element);

        if (element.hasAttribute("populate")) {
            return loomGui;
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
                case "banner":
                    component = loomGui.getBannerComponent();
                    break;
                case "dye":
                    component = loomGui.getDyeComponent();
                    break;
                case "pattern":
                    component = loomGui.getPatternComponent();
                    break;
                case "output":
                    component = loomGui.getOutputComponent();
                    break;
                case "player-inventory":
                    component = loomGui.getPlayerGuiComponent();
                    break;
                default:
                    throw new XMLLoadException("Unknown component name");
            }

            component.load(instance, componentElement, plugin);
        }

        return loomGui;
    }

    /**
     * Loads a loom gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @return the loaded loom gui
     * @since 0.12.1
     */
    @Nullable
    @Contract(pure = true)
    public static LoomGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
        return load(instance, inputStream, JavaPlugin.getProvidingPlugin(LoomGui.class));
    }

    /**
     * Loads a loom gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @return the loaded loom gui
     * @since 0.12.1
     */
    @NotNull
    public static LoomGui load(@NotNull Object instance, @NotNull Element element) {
        return load(instance, element, JavaPlugin.getProvidingPlugin(LoomGui.class));
    }
}
