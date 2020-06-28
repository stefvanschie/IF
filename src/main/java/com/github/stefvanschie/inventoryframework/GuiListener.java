package com.github.stefvanschie.inventoryframework;

import com.github.stefvanschie.inventoryframework.pane.Pane;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Listens to events for {@link Gui}s. Only one instance of this class gets constructed.
 * (One instance per plugin, but plugins are supposed to shade and relocate IF.)
 *
 * @since 0.5.4
 */
public class GuiListener implements Listener {

    /**
     * A collection of all {@link Gui} instances that have at least one viewer.
     */
    @NotNull
    private final Set<Gui> activeGuiInstances = new HashSet<>();

    /**
     * Handles clicks in inventories
     *
     * @param event the event fired
     * @since 0.5.4
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Gui)) {
            return;
        }

        Gui gui = (Gui) event.getInventory().getHolder();
        InventoryView view = event.getView();
        Inventory inventory = view.getInventory(event.getRawSlot());

        if (inventory == null) {
            gui.callOnOutsideClick(event);
            return;
        }

        gui.callOnGlobalClick(event);
        if (inventory.equals(view.getTopInventory())) {
            gui.callOnTopClick(event);
        } else {
            gui.callOnBottomClick(event);
        }

        if ((inventory.equals(view.getBottomInventory()) && gui.getState() == Gui.State.TOP) ||
            event.getCurrentItem() == null) {
            return;
        }

        List<Pane> panes = gui.getPanes();

        //loop through the panes reverse, because the pane with the highest priority (last in list) is most likely to have the correct item
        for (int i = panes.size() - 1; i >= 0; i--) {
            if (panes.get(i).click(gui, event, 0, 0, 9, gui.getRows() + 4))
                break;
        }
    }

    /**
     * Handles users picking up items while their bottom inventory is in use.
     *
     * @param event the event fired when an entity picks up an item
     * @since 0.6.1
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityPickupItem(@NotNull EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof HumanEntity)) {
            return;
        }

        InventoryHolder holder = ((HumanEntity) event.getEntity()).getOpenInventory().getTopInventory().getHolder();

        if (!(holder instanceof Gui)) {
            return;
        }

        Gui gui = (Gui) holder;

        if (gui.getState() != Gui.State.BOTTOM) {
            return;
        }

        int leftOver = gui.getHumanEntityCache().add((HumanEntity) event.getEntity(), event.getItem().getItemStack());

        if (leftOver == 0) {
            event.getItem().remove();
        } else {
            ItemStack itemStack = event.getItem().getItemStack();

            itemStack.setAmount(leftOver);

            event.getItem().setItemStack(itemStack);
        }

        event.setCancelled(true);
    }

    /**
     * Handles small drag events which are likely clicks instead. These small drags will be interpreted as clicks and
     * will fire a click event.
     *
     * @param event the event fired
     * @since 0.6.1
     */
    @EventHandler
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof Gui)) {
            return;
        }

        Set<Integer> inventorySlots = event.getInventorySlots();

        if (inventorySlots.size() > 1) {
            return;
        }

        InventoryView view = event.getView();
        int index = inventorySlots.toArray(new Integer[0])[0];
        InventoryType.SlotType slotType = view.getSlotType(index);

        boolean even = event.getType() == DragType.EVEN;

        ClickType clickType = even ? ClickType.LEFT : ClickType.RIGHT;
        InventoryAction inventoryAction = even ? InventoryAction.PLACE_SOME : InventoryAction.PLACE_ONE;

        //this is a fake click event, firing this may cause other plugins to function incorrectly, so keep it local
        InventoryClickEvent inventoryClickEvent = new InventoryClickEvent(view, slotType, index, clickType,
            inventoryAction);

        onInventoryClick(inventoryClickEvent);

        event.setCancelled(inventoryClickEvent.isCancelled());
    }

    /**
     * Handles closing in inventories
     *
     * @param event the event fired
     * @since 0.5.4
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Gui)) {
            return;
        }

        Gui gui = (Gui) event.getInventory().getHolder();

        if (!gui.isUpdating()) {
            gui.callOnClose(event);
        }

        gui.getHumanEntityCache().restoreAndForget(event.getPlayer());

        if (gui.getViewerCount() == 1) {
            activeGuiInstances.remove(gui);
        }
    }

    /**
     * Registers newly opened inventories
     *
     * @param event the event fired
     * @since 0.5.19
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        if (!(event.getInventory().getHolder() instanceof Gui)) {
            return;
        }

        Gui gui = (Gui) event.getInventory().getHolder();
        activeGuiInstances.add(gui);
    }

    /**
     * Handles the disabling of the plugin
     *
     * @param event the event fired
     * @since 0.5.19
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPluginDisable(@NotNull PluginDisableEvent event) {
        Plugin thisPlugin = JavaPlugin.getProvidingPlugin(getClass());
        if (event.getPlugin() != thisPlugin) {
            return;
        }

        int counter = 0; //callbacks might open GUIs, eg. in nested menus
		int maxCount = 10;
        while (!activeGuiInstances.isEmpty() && counter++ < maxCount) {
            for (Gui gui : new ArrayList<>(activeGuiInstances)) {
                for (HumanEntity viewer : gui.getViewers()) {
                    viewer.closeInventory();
                }
            }
        }

        if (counter == maxCount) {
			thisPlugin.getLogger().warning("Unable to close GUIs on plugin disable: they keep getting opened "
					+ "(tried: " + maxCount + " times)");
		}
    }
}
