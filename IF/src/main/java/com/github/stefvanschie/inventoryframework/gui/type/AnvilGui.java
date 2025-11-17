package com.github.stefvanschie.inventoryframework.gui.type;

import com.github.stefvanschie.inventoryframework.HumanEntityCache;
import com.github.stefvanschie.inventoryframework.abstraction.AnvilInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent;
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
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
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Represents a gui in the form of an anvil
 *
 * @since 0.8.0
 */
public class AnvilGui extends NamedGui implements InventoryBased {

    /**
     * Called whenever the name input is changed.
     */
    @NotNull
    private Consumer<? super String> onNameInputChanged = (name) -> {};

    /**
     * Represents the inventory component for the first item
     */
    @NotNull
    private InventoryComponent firstItemComponent = new InventoryComponent(1, 1);

    /**
     * Represents the inventory component for the second item
     */
    @NotNull
    private InventoryComponent secondItemComponent = new InventoryComponent(1, 1);

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
     * An internal anvil inventory
     */
    @NotNull
    private final AnvilInventory anvilInventory = VersionMatcher.newAnvilInventory(Version.getVersion());

    /**
     * Constructs a new anvil gui
     *
     * @param title the title/name of this gui.
     * @since 0.8.0
     */
    public AnvilGui(@NotNull String title) {
        super(title);

        this.anvilInventory.subscribeToNameInputChanges(this::callOnRename);
    }

    /**
     * Constructs a new anvil gui
     *
     * @param title the title/name of this gui.
     * @since 0.10.0
     */
    public AnvilGui(@NotNull TextHolder title) {
        super(title);

        this.anvilInventory.subscribeToNameInputChanges(this::callOnRename);
    }

    /**
     * Constructs a new anvil gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #AnvilGui(String)
     * @since 0.10.8
     */
    public AnvilGui(@NotNull String title, @NotNull Plugin plugin) {
        super(title, plugin);

        this.anvilInventory.subscribeToNameInputChanges(this::callOnRename);
    }

    /**
     * Constructs a new anvil gui for the given {@code plugin}.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see #AnvilGui(TextHolder)
     * @since 0.10.8
     */
    public AnvilGui(@NotNull TextHolder title, @NotNull Plugin plugin) {
        super(title, plugin);

        this.anvilInventory.subscribeToNameInputChanges(this::callOnRename);
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

        getFirstItemComponent().display(getInventory(), 0);
        getSecondItemComponent().display(getInventory(), 1);
        getResultComponent().display(getInventory(), 2);

        getPlayerInventoryComponent().display();

        HumanEntityCache humanEntityCache = getHumanEntityCache();

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
    public AnvilGui copy() {
        AnvilGui gui = new AnvilGui(getTitleHolder(), super.plugin);

        gui.firstItemComponent = firstItemComponent.copy();
        gui.secondItemComponent = secondItemComponent.copy();
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
            getFirstItemComponent().click(this, event, 0);
        } else if (rawSlot == 1) {
            getSecondItemComponent().click(this, event, 0);
        } else if (rawSlot == 2) {
            getResultComponent().click(this, event, 0);
        } else {
            getPlayerInventoryComponent().click(this, event, rawSlot - 3);
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

    /**
     * Sets the enchantment level cost for this anvil gui. Taking the item from the result slot will not actually remove
     * these levels. Having a cost specified does not impede a player's ability to take the item in the result item,
     * even if the player does not have the specified amount of levels. The cost must be a non-negative number.
     *
     * @param cost the cost
     * @since 0.10.8
     * @throws IllegalArgumentException when the cost is less than zero
     */
    public void setCost(short cost) {
        if (cost < 0){
            throw new IllegalArgumentException("Cost must be non-negative");
        }

        this.anvilInventory.setCost(cost);
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory() {
        Inventory inventory = this.anvilInventory.createInventory(getTitleHolder());

        addInventory(inventory, this);

        return inventory;
    }

    /**
     * Gets the rename text currently specified in the anvil.
     *
     * @return the rename text
     * @since 0.8.0
     * @see org.bukkit.inventory.AnvilInventory#getRenameText()
     */
    @NotNull
    @Contract(pure = true)
    public String getRenameText() {
        return anvilInventory.getRenameText();
    }

    @Contract(pure = true)
    @Override
    public boolean isPlayerInventoryUsed() {
        return getPlayerInventoryComponent().hasItem();
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
     * Sets the consumer that should be called whenever the name input is changed. The argument is the new input. When
     * this consumer is invoked, the value as returned by {@link #getRenameText()} will not have updated yet, hence
     * allowing you to see the old value via that.
     *
     * @param onNameInputChanged the consumer to call when the rename input is changed
     * @since 0.10.10
     */
    public void setOnNameInputChanged(@NotNull Consumer<? super String> onNameInputChanged) {
        this.onNameInputChanged = onNameInputChanged;
    }

    /**
     * Calls the consumer that was specified using {@link #setOnNameInputChanged(Consumer)}, so the consumer that should
     * be called whenever the rename input is changed. Catches and logs all exceptions the consumer might throw.
     *
     * @param newInput the new rename input
     * @since 0.10.10
     */
    private void callOnRename(@NotNull String newInput) {
        try {
            this.onNameInputChanged.accept(newInput);
        } catch (Throwable throwable) {
            String message = "Exception while handling onRename, newInput='" + newInput + "'";

            this.plugin.getLogger().log(Level.SEVERE, message, throwable);
        }
    }

    /**
     * Gets the inventory component representing the first item
     *
     * @return the first item component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getFirstItemComponent() {
        return firstItemComponent;
    }

    /**
     * Gets the inventory component representing the second item
     *
     * @return the second item component
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getSecondItemComponent() {
        return secondItemComponent;
    }

    /**
     * Gets the inventory component representing the result
     *
     * @return the result component
     * @since 0.8.0
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
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public InventoryComponent getPlayerInventoryComponent() {
        return playerInventoryComponent;
    }

    /**
     * Gets the top items
     *
     * @return the top items
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    private ItemStack[] getTopItems() {
        return new ItemStack[] {
            getFirstItemComponent().getItem(0, 0),
            getSecondItemComponent().getItem(0, 0),
            getResultComponent().getItem(0, 0)
        };
    }

    /**
     * Loads an anvil gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @param plugin the plugin that will be the owner of the created gui
     * @return the loaded anvil gui
     * @see #load(Object, InputStream)
     * @since 0.10.8
     */
    @Nullable
    @Contract(pure = true)
    public static AnvilGui load(@NotNull Object instance, @NotNull InputStream inputStream, @NotNull Plugin plugin) {
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
     * Loads an anvil gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @param plugin the plugin that will own the created gui
     * @return the loaded anvil gui
     * @see #load(Object, Element)
     * @since 0.10.8
     */
    @NotNull
    public static AnvilGui load(@NotNull Object instance, @NotNull Element element, @NotNull Plugin plugin) {
        if (!element.hasAttribute("title")) {
            throw new XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set");
        }

        AnvilGui anvilGui = new AnvilGui(element.getAttribute("title"), plugin);
        anvilGui.initializeOrThrow(instance, element);

        if (element.hasAttribute("populate")) {
            return anvilGui;
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
                case "first-item":
                    component = anvilGui.getFirstItemComponent();
                    break;
                case "second-item":
                    component = anvilGui.getSecondItemComponent();
                    break;
                case "result":
                    component = anvilGui.getResultComponent();
                    break;
                case "player-inventory":
                    component = anvilGui.getPlayerInventoryComponent();
                    break;
                default:
                    throw new XMLLoadException("Unknown component name");
            }

            component.load(instance, componentElement, plugin);
        }

        return anvilGui;
    }

    /**
     * Loads an anvil gui from an XML file.
     *
     * @param instance the instance on which to reference fields and methods
     * @param inputStream the input stream containing the XML data
     * @return the loaded anvil gui
     * @since 0.8.0
     */
    @Nullable
    @Contract(pure = true)
    public static AnvilGui load(@NotNull Object instance, @NotNull InputStream inputStream) {
        return load(instance, inputStream, JavaPlugin.getProvidingPlugin(AnvilGui.class));
    }

    /**
     * Loads an anvil gui from the specified element, applying code references to the provided instance.
     *
     * @param instance the instance on which to reference fields and methods
     * @param element the element to load the gui from
     * @return the loaded anvil gui
     * @since 0.8.0
     */
    @NotNull
    public static AnvilGui load(@NotNull Object instance, @NotNull Element element) {
        return load(instance, element, JavaPlugin.getProvidingPlugin(AnvilGui.class));
    }
}
