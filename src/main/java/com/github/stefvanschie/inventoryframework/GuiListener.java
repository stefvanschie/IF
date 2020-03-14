package com.github.stefvanschie.inventoryframework;

import com.github.stefvanschie.inventoryframework.pane.Pane;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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
     * The main plugin instance.
     */
    @NotNull
    private final Plugin plugin;

    /**
     * Constructs a new listener
     *
     * @param plugin the main plugin
     * @since 0.5.19
     */
    public GuiListener(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

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
        Consumer<InventoryClickEvent> onOutsideClick = gui.getOnOutsideClick();

        if (onOutsideClick != null && event.getClickedInventory() == null) {
            onOutsideClick.accept(event);
            return;
        }

        Consumer<InventoryClickEvent> onGlobalClick = gui.getOnGlobalClick();

        if (onGlobalClick != null) {
            onGlobalClick.accept(event);
        }

        InventoryView view = event.getView();
        Inventory inventory = Gui.getInventory(view, event.getRawSlot());

        if (inventory == null) {
            return;
        }

        Consumer<InventoryClickEvent> onTopClick = gui.getOnTopClick();

        if (onTopClick != null && inventory.equals(view.getTopInventory())) {
            onTopClick.accept(event);
        }

        Consumer<InventoryClickEvent> onBottomClick = gui.getOnBottomClick();

        if (onBottomClick != null && inventory.equals(view.getBottomInventory())) {
            onBottomClick.accept(event);
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

        Consumer<InventoryCloseEvent> onClose = gui.getOnClose();
        if (!gui.isUpdating() && onClose != null) {
            onClose.accept(event);
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
        if (event.getPlugin() != plugin) {
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
			plugin.getLogger().warning("Unable to close GUIs on plugin disable: they keep getting opened "
					+ "(tried: " + maxCount + " times)");
		}
    }
}
