package com.gmail.stefvanschiedev.inventoryframework;

import com.gmail.stefvanschiedev.inventoryframework.pane.OutlinePane;
import com.gmail.stefvanschiedev.inventoryframework.pane.PaginatedPane;
import com.gmail.stefvanschiedev.inventoryframework.pane.StaticPane;
import com.gmail.stefvanschiedev.inventoryframework.pane.util.Pane;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
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
import java.util.*;

/**
 * The base class of all GUIs
 *
 * @since 5.6.0
 */
public class Gui implements Listener, InventoryHolder {

    /**
     * A set of all panes in this inventory
     */
    private final List<Pane> panes;

    /**
     * The inventory of this gui
     */
    private final Inventory inventory;

    /**
     * Constructs a new GUI
     *
     * @param rows the amount of rows this gui should contain
     * @param title the title/name of this gui
     */
    protected Gui(Plugin plugin, int rows, String title) {
        assert rows >= 1 && rows <= 6 : "amount of rows outside range";

        this.panes = new ArrayList<>();
        this.inventory = Bukkit.createInventory(this, rows * 9, title);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Adds a pane to this gui
     *
     * @param pane the pane to add
     * @since 5.6.0
     */
    protected void addPane(Pane pane) {
        this.panes.add(pane);
    }

    /**
     * Returns a gui item by tag
     *
     * @param tag the tag to look for
     * @return the gui item
     * @since 5.6.0
     */
    @Nullable
    @Contract(pure = true)
    public GuiItem getItem(@NotNull String tag) {
        return panes.stream().map(pane -> pane.getItem(tag)).filter(Objects::nonNull).findAny().orElse(null);
    }

    /**
     * Returns a pane by tag
     *
     * @param tag the tag to look for
     * @return the pane
     * @since 5.6.0
     */
    @Nullable
    @Contract(pure = true)
    public Pane getPane(@NotNull String tag) {
        return panes.stream().filter(pane -> tag.equals(pane.getTag())).findAny().orElse(null);
    }

    /**
     * Shows a gui to a player
     *
     * @param humanEntity the human entity to show the gui to
     * @since 5.6.0
     */
    public void show(HumanEntity humanEntity) {
        inventory.clear();

        //initialize the inventory first
        panes.stream().filter(Pane::isVisible).forEach(pane -> pane.display(inventory));

        humanEntity.openInventory(inventory);
    }

    /**
     * Update the gui for everyone
     *
     * @since 5.6.0
     */
    public void update() {
        new HashSet<>(inventory.getViewers()).forEach(this::show);
    }

    /**
     * {@inheritDoc}
     * 
     * @since 5.6.0
     */
    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Loads a Gui from a given input stream
     *
     * @param inputStream the file
     * @return the gui
     * @since 5.6.0
     */
    @Nullable
    @Contract("_, _, null -> fail")
    public static Gui load(Object instance, Plugin plugin, InputStream inputStream) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            Element documentElement = document.getDocumentElement();

            documentElement.normalize();

            String title = documentElement.getAttribute("title");

            Gui gui = new Gui(plugin, Integer.parseInt(documentElement.getAttribute("rows")), title);

            NodeList childNodes = documentElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);

                if (item.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                gui.addPane(loadPane(instance, item));
            }

            return gui;
        } catch (ParserConfigurationException | SAXException | IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Loads a pane by the given instance and node
     *
     * @param instance the instance
     * @param node the node
     * @return the pane
     * @since 5.6.0
     */
    @Nullable
    @Contract("_, null -> fail")
    public static Pane loadPane(Object instance, @NotNull Node node) {
        switch (node.getNodeName()) {
            case "outlinepane":
                return OutlinePane.load(instance, (Element) node);
            case "paginatedpane":
                return PaginatedPane.load(instance, (Element) node);
            case "staticpane":
                return StaticPane.load(instance, (Element) node);
        }

        return null;
    }

    /**
     * Handles clicks in inventories
     * 
     * @param event the event fired
     * @since 5.6.0
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || !this.equals(event.getClickedInventory().getHolder()))
            return;

        //loop through the panes reverse, because the pane with the highest priority (last in list) is most likely to have the correct item
        for (int i = panes.size() - 1; i >= 0; i--) {
            if (panes.get(i).click(event))
                break;
        }
    }
}
