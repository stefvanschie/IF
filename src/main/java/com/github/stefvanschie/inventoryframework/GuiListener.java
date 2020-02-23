package com.github.stefvanschie.inventoryframework;

import com.github.stefvanschie.inventoryframework.pane.Pane;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * Listens to events for {@link Gui}s. Only one of these is ever initialized per plugin.
 *
 * @since 0.5.4
 */
public class GuiListener implements Listener {

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

        HumanEntityCache humanEntityCache = gui.getHumanEntityCache();
        HumanEntity humanEntity = event.getPlayer();
        humanEntityCache.restore(humanEntity);
        humanEntityCache.clearCache(humanEntity);
    }
}
