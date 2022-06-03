package com.github.stefvanschie.inventoryframework.gui.type;

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
 * Represents a gui in the form of a dropper
 *
 * @since 0.8.0
 */
public class DropperGui extends NamedGui implements InventoryBased {

    /**
     * Represents the inventory component for the contents
     */
    @NotNull
    private InventoryComponent contentsComponent = new InventoryComponent(3, 3);

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
    public DropperGui(@NotNull String title) {
        super(title);
    }

    /**
     * Constructs a new GUI
     *
     * @param title the title/name of this gui.
     * @since 0.10.0
     */
    public DropperGui(@NotNull TextHolder title) {
        super(title);
    }

    @Override
    protected void show(@NotNull HumanEntity humanEntity, boolean reopen) {
        if (isDirty()) {
            this.inventory = createInventory();
            markChanges();
        }

        getInventory().clear();

        getContentsComponent().display(getInventory(), 0);
        getPlayerInventoryComponent().display();

        if (getPlayerInventoryComponent().hasItem()) {
            getHumanEntityCache().storeAndClear(humanEntity);

            getPlayerInventoryComponent().placeItems(humanEntity.getInventory(), 0);
        }

        if (reopen)
            humanEntity.openInventory(getInventory());
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public DropperGui copy() {
        DropperGui gui = new DropperGui(getTitleHolder());

        gui.contentsComponent = contentsComponent.copy();
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

        if (rawSlot >= 0 && rawSlot <= 8) {
            getContentsComponent().click(this, event, rawSlot);
        } else {
            getPlayerInventoryComponent().click(this, event, rawSlot - 9);
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
        Inventory inventory = getTitleHolder().asInventoryTitle(this, InventoryType.DROPPER);

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
     * Gets the inventory component representing the contents
     *
     * @return the contents component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getContentsComponent() {
        return contentsComponent;
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
     * Loads a dropper gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @return the loaded dropper gui
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    public static DropperGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
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
     * Loads a dropper gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @return the loaded dropper gui
     * @since 0.8.0
     */
    @NotNull
    public static DropperGui load(@NotNull Object instance, @NotNull Element element) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        DropperGui dropperGui = new DropperGui(element.getAttribute("title"));
        dropperGui.initializeOrThrow(instance, element);

        if (element.hasAttribute("populate")) {
            return dropperGui;
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
                case "contents":
                    component = dropperGui.getContentsComponent();
                    break;
                case "player-inventory":
                    component = dropperGui.getPlayerInventoryComponent();
                    break;
                default:
                    throw new XMLLoadException("Unknown component name");
            }

            component.load(instance, componentElement);
        }

        return dropperGui;
    }
}
