package com.github.stefvanschie.inventoryframework.gui.type;

import com.github.stefvanschie.inventoryframework.HumanEntityCache;
import com.github.stefvanschie.inventoryframework.abstraction.SmithingTableInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
import com.github.stefvanschie.inventoryframework.util.version.Version;
import com.github.stefvanschie.inventoryframework.util.version.VersionMatcher;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
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
 * Represents a gui in the form of a smithing table. This is the modern variant with three input slots, available in
 * from Minecraft 1.19.4.
 *
 * @since 0.10.9
 */
public class ModernSmithingTableGui extends NamedGui implements InventoryBased {

    /**
     * Represents the inventory component for the input
     */
    @NotNull
    private InventoryComponent inputComponent = new InventoryComponent(3, 1);

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
     * An internal smithing inventory
     */
    @NotNull
    private final SmithingTableInventory smithingTableInventory = VersionMatcher.newModernSmithingTableInventory(
            Version.getVersion()
    );

    /**
     * Constructs a new GUI.
     *
     * @param title the title/name of this gui.
     * @since 0.10.9
     */
    public ModernSmithingTableGui(@NotNull String title) {
        super(title);
    }

    /**
     * Constructs a new GUI.
     *
     * @param title the title/name of this gui.
     * @since 0.10.9
     */
    public ModernSmithingTableGui(@NotNull TextHolder title) {
        super(title);
    }

    /**
     * Constructs a new smithing table gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #ModernSmithingTableGui(String)
     * @since 0.10.9
     */
    public ModernSmithingTableGui(@NotNull String title, @NotNull Plugin plugin) {
        super(title, plugin);
    }

    /**
     * Constructs a new smithing table gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #ModernSmithingTableGui(TextHolder)
     * @since 0.10.9
     */
    public ModernSmithingTableGui(@NotNull TextHolder title, @NotNull Plugin plugin) {
        super(title, plugin);
    }

    @Override
    public void show(@NotNull HumanEntity humanEntity) {
        if (!(humanEntity instanceof Player)) {
            throw new IllegalArgumentException("Smithing tables can only be opened by players");
        }

        if (isDirty()) {
            this.inventory = createInventory();
            markChanges();
        }

        getInventory().clear();

        getInputComponent().display(getInventory(), 0);
        getResultComponent().display(getInventory(), 3);
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
    public ModernSmithingTableGui copy() {
        ModernSmithingTableGui gui = new ModernSmithingTableGui(getTitleHolder(), super.plugin);

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

        if (rawSlot >= 0 && rawSlot <= 2) {
            getInputComponent().click(this, event, rawSlot);
        } else if (rawSlot == 3) {
            getResultComponent().click(this, event, 0);
        } else {
            getPlayerInventoryComponent().click(this, event, rawSlot - 4);
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
        Inventory inventory = this.smithingTableInventory.createInventory(getTitleHolder());

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
     * Gets the inventory component representing the input items
     *
     * @return the input component
     * @since 0.10.9
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getInputComponent() {
        return inputComponent;
    }

    /**
     * Gets the inventory component representing the result
     *
     * @return the result component
     * @since 0.10.9
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
     * @since 0.10.9
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getPlayerInventoryComponent() {
        return playerInventoryComponent;
    }

    /**
     * Loads a smithing table gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded smithing table gui
     * @see #load(Object, InputStream)
     * @since 0.10.9
     */
    @Nullable
    @Contract(pure = true)
    public static ModernSmithingTableGui load(@NotNull Object instance, @NotNull InputStream inputStream,
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
     * Loads a smithing table gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded smithing table gui
     * @since 0.10.9
     */
    @NotNull
    public static ModernSmithingTableGui load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        ModernSmithingTableGui smithingTableGui = new ModernSmithingTableGui(element.getAttribute("title"), plugin);
        smithingTableGui.initializeOrThrow(instance, element);

        if (element.hasAttribute("populate")) {
            return smithingTableGui;
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
                    component = smithingTableGui.getInputComponent();
                    break;
                case "result":
                    component = smithingTableGui.getResultComponent();
                    break;
                case "player-inventory":
                    component = smithingTableGui.getPlayerInventoryComponent();
                    break;
                default:
                    throw new XMLLoadException("Unknown component name");
            }

            component.load(instance, componentElement, plugin);
        }

        return smithingTableGui;
    }

    /**
     * Loads a smithing table gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @return the loaded smithing table gui
     * @since 0.10.9
     */
    @Nullable
    @Contract(pure = true)
    public static ModernSmithingTableGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
        return load(instance, inputStream, JavaPlugin.getProvidingPlugin(ModernSmithingTableGui.class));
    }

    /**
     * Loads a smithing table gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @return the loaded smithing table gui
     * @since 0.10.9
     */
    @NotNull
    public static ModernSmithingTableGui load(@NotNull Object instance, @NotNull Element element) {
        return load(instance, element, JavaPlugin.getProvidingPlugin(ModernSmithingTableGui.class));
    }
}
