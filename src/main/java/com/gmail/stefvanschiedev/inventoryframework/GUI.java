package com.gmail.stefvanschiedev.inventoryframework;

import com.gmail.stefvanschiedev.inventoryframework.pane.util.Pane;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * The base class of all GUIs
 */
public class GUI implements Listener {

    /**
     * A set of all panes in this inventory
     */
    private List<Pane> panes;

    /**
     * The inventory of this gui
     */
    private Inventory inventory;

    /**
     * Constructs a new GUI
     *
     * @param rows the amount of rows this gui should contain
     * @param title the title/name of this gui
     */
    public GUI(Plugin plugin, int rows, String title) {
        assert rows >= 1 && rows <= 6 : "amount of rows outside range";

        this.panes = new ArrayList<>();
        this.inventory = Bukkit.createInventory(null, rows * 9, title);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Adds a pane to this gui
     *
     * @param pane the pane to add
     */
    public void addPane(Pane pane) {
        this.panes.add(pane);

        this.panes.sort(Comparator.comparing(Pane::getPriority));
    }

    /**
     * Shows a gui to a player
     *
     * @param humanEntity the human entity to show the gui to
     */
    public void show(HumanEntity humanEntity) {
        inventory.clear();

        //initialize the inventory first
        panes.forEach(pane -> {
            if (pane.isVisible())
                pane.display(inventory);
        });

        humanEntity.openInventory(inventory);
    }

    /**
     * Update the gui for everyone
     */
    public void update() {
        new HashSet<>(inventory.getViewers()).forEach(this::show);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || !event.getClickedInventory().getName().equals(inventory.getName()))
            return;

        //loop through the panes reverse, because the pane with the highest priority (last in list) is most likely to have the correct item
        for (int i = panes.size() - 1; i >= 0; i--) {
            if (panes.get(i).click(event)) {
                event.setCancelled(true);
                break;
            }
        }
    }
}