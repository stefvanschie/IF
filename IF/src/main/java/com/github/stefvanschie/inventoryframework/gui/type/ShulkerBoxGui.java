package com.github.stefvanschie.inventoryframework.gui.type;

import com.github.stefvanschie.inventoryframework.HumanEntityCache;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased;
import com.github.stefvanschie.inventoryframework.gui.type.util.MergedGui;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a gui in the form a shulker box
 *
 * @since 0.8.0
 */
public class ShulkerBoxGui extends NamedGui implements MergedGui, InventoryBased {

    /**
     * Represents the inventory component for the entire gui
     */
    @NotNull
    private InventoryComponent inventoryComponent = new InventoryComponent(9, 7);

    /**
     * Constructs a new GUI
     *
     * @param title the title/name of this gui.
     * @since 0.8.0
     */
    public ShulkerBoxGui(@NotNull String title) {
        super(title);
    }

    /**
     * Constructs a new GUI
     *
     * @param title the title/name of this gui.
     * @since 0.10.0
     */
    public ShulkerBoxGui(@NotNull TextHolder title) {
        super(title);
    }

    /**
     * Constructs a new shulker box gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #ShulkerBoxGui(String)
     * @since 0.10.8
     */
    public ShulkerBoxGui(@NotNull String title, @NotNull Plugin plugin) {
        super(title, plugin);
    }

    /**
     * Constructs a new shulker box gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #ShulkerBoxGui(TextHolder)
     * @since 0.10.8
     */
    public ShulkerBoxGui(@NotNull TextHolder title, @NotNull Plugin plugin) {
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

        int height = getInventoryComponent().getHeight();

        getInventoryComponent().display();
        getInventoryComponent().excludeRows(height - 4, height - 1).placeItems(getInventory(), 0);

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
        int height = getInventoryComponent().getHeight();
        InventoryComponent bottomComponent = getInventoryComponent().excludeRows(0, height - 5);

        if (bottomComponent.hasItem()) {
            HumanEntityCache humanEntityCache = getHumanEntityCache();

            if (!humanEntityCache.contains(humanEntity)) {
                humanEntityCache.storeAndClear(humanEntity);
            }

            bottomComponent.placeItems(humanEntity.getInventory(), 0);
        }
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public ShulkerBoxGui copy() {
        ShulkerBoxGui gui = new ShulkerBoxGui(getTitleHolder(), super.plugin);

        gui.inventoryComponent = inventoryComponent.copy();

        gui.setOnTopClick(this.onTopClick);
        gui.setOnBottomClick(this.onBottomClick);
        gui.setOnGlobalClick(this.onGlobalClick);
        gui.setOnOutsideClick(this.onOutsideClick);
        gui.setOnClose(this.onClose);

        return gui;
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
        return getInventoryComponent().excludeRows(0, getInventoryComponent().getHeight() - 5).hasItem();
    }

    @Override
    public void click(@NotNull InventoryClickEvent event) {
        getInventoryComponent().click(this, event, event.getRawSlot());
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
        return getTitleHolder().asInventoryTitle(this, InventoryType.SHULKER_BOX);
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
     * Loads a shulker box gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded shulker box gui
     * @see #load(Object, InputStream)
     * @since 0.10.8
     */
    @Nullable
    @Contract(pure = true)
    public static ShulkerBoxGui load(@NotNull Object instance, @NotNull InputStream inputStream,
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
     * Loads a shulker box gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded shulker box gui
     * @see #load(Object, Element)
     * @since 0.10.8
     */
    @NotNull
    public static ShulkerBoxGui load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        ShulkerBoxGui shulkerBoxGui = new ShulkerBoxGui(element.getAttribute("title"), plugin);
        shulkerBoxGui.initializeOrThrow(instance, element);

        if (element.hasAttribute("populate")) {
            return shulkerBoxGui;
        }

        NodeList childNodes = element.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node item = childNodes.item(index);

            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element componentElement = (Element) item;
            InventoryComponent inventoryComponent = shulkerBoxGui.getInventoryComponent();

            if (componentElement.getTagName().equalsIgnoreCase("component")) {
                inventoryComponent.load(instance, componentElement, plugin);
            } else {
                inventoryComponent.load(instance, element, plugin);
            }

            break;
        }

        return shulkerBoxGui;
    }

    /**
     * Loads a shulker box gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @return the loaded shulker box gui
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    public static ShulkerBoxGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
        return load(instance, inputStream, JavaPlugin.getProvidingPlugin(ShulkerBoxGui.class));
    }

    /**
     * Loads a shulker box gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @return the loaded shulker box gui
     * @since 0.8.0
     */
    @NotNull
    public static ShulkerBoxGui load(@NotNull Object instance, @NotNull Element element) {
        return load(instance, element, JavaPlugin.getProvidingPlugin(ShulkerBoxGui.class));
    }
}
