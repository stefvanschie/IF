package com.github.stefvanschie.inventoryframework.gui;

import com.github.stefvanschie.inventoryframework.HumanEntityCache;
import com.github.stefvanschie.inventoryframework.gui.type.*;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil;
import com.github.stefvanschie.inventoryframework.util.UUIDTagType;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Listens to events for {@link Gui}s. Only one instance of this class gets constructed.
 * (One instance per plugin, but plugins are supposed to shade and relocate IF.)
 *
 * @since 0.5.4
 */
public class GuiListener implements Listener {

    /**
     * The owning plugin of this listener.
     */
    @NotNull
    private final Plugin plugin;

    /**
     * A collection of all {@link Gui} instances that have at least one viewer.
     */
    @NotNull
    private final Set<Gui> activeGuiInstances = new HashSet<>();

    /**
     * Creates a new listener for all guis for the provided {@code plugin}.
     *
     * @param plugin the owning plugin of this listener
     * @since 0.10.8
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
        Gui gui = getGui(event.getInventory());

        if (gui == null) {
            return;
        }

        InventoryView view = event.getView();
        Inventory inventory = InventoryViewUtil.getInstance().getInventory(view, event.getRawSlot());

        if (inventory == null) {
            gui.callOnOutsideClick(event);
            return;
        }

        gui.callOnGlobalClick(event);
        if (inventory.equals(InventoryViewUtil.getInstance().getTopInventory(view))) {
            gui.callOnTopClick(event);
        } else {
            gui.callOnBottomClick(event);
        }

        gui.click(event);

        if (event.isCancelled()) {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                PlayerInventory playerInventory = event.getWhoClicked().getInventory();

                /* due to a client issue off-hand items appear as ghost items, this updates the off-hand correctly
                   client-side */
                playerInventory.setItemInOffHand(playerInventory.getItemInOffHand());
            });
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
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof HumanEntity)) {
            return;
        }

        Gui gui = getGui(InventoryViewUtil.getInstance().getTopInventory(((HumanEntity) entity).getOpenInventory()));

        if (gui == null || !gui.isPlayerInventoryUsed()) {
            return;
        }

        int leftOver = gui.getHumanEntityCache().add((HumanEntity) entity, event.getItem().getItemStack());

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
     * Handles drag events
     *
     * @param event the event fired
     * @since 0.6.1
     */
    @EventHandler
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        Gui gui = getGui(event.getInventory());

        if (gui == null) {
            return;
        }

        InventoryView view = event.getView();
        Set<Integer> inventorySlots = event.getRawSlots();

        if (inventorySlots.size() > 1) {
            boolean top = false, bottom = false;

            for (int inventorySlot : inventorySlots) {
                Inventory inventory = InventoryViewUtil.getInstance().getInventory(view, inventorySlot);

                if (InventoryViewUtil.getInstance().getTopInventory(view).equals(inventory)) {
                    top = true;
                } else if (InventoryViewUtil.getInstance().getBottomInventory(view).equals(inventory)) {
                    bottom = true;
                }

                if (top && bottom) {
                    break;
                }
            }

            gui.callOnGlobalDrag(event);

            if (top) {
                gui.callOnTopDrag(event);
            }

            if (bottom) {
                gui.callOnBottomDrag(event);
            }
        } else {
            int index = inventorySlots.toArray(new Integer[0])[0];
            InventoryType.SlotType slotType = InventoryViewUtil.getInstance().getSlotType(view, index);

            boolean even = event.getType() == DragType.EVEN;

            ClickType clickType = even ? ClickType.LEFT : ClickType.RIGHT;
            InventoryAction inventoryAction = even ? InventoryAction.PLACE_SOME : InventoryAction.PLACE_ONE;

            ItemStack previousViewCursor = InventoryViewUtil.getInstance().getCursor(view);
            // Overwrite getCursor in inventory click event to mimic real event fired by Bukkit.
            InventoryViewUtil.getInstance().setCursor(view, event.getOldCursor());
            //this is a fake click event, firing this may cause other plugins to function incorrectly, so keep it local
            InventoryClickEvent inventoryClickEvent = new InventoryClickEvent(view, slotType, index, clickType,
                inventoryAction);

            onInventoryClick(inventoryClickEvent);
            // Restore previous cursor only if someone has not changed it manually in onInventoryClick.
            if (Objects.equals(InventoryViewUtil.getInstance().getCursor(view), event.getOldCursor())) {
                InventoryViewUtil.getInstance().setCursor(view, previousViewCursor);
            }

            event.setCancelled(inventoryClickEvent.isCancelled());
        }
    }

    /**
     * Handles the selection of trades in merchant guis
     *
     * @param event the event fired
     */
    @EventHandler(ignoreCancelled = true)
    public void onTradeSelect(@NotNull TradeSelectEvent event) {
        Gui gui = getGui(event.getInventory());

        if (!(gui instanceof MerchantGui)) {
            return;
        }

        ((MerchantGui) gui).callOnTradeSelect(event);
    }

    /**
     * Handles closing in inventories
     *
     * @param event the event fired
     * @since 0.5.4
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        Gui gui = getGui(event.getInventory());

        if (gui == null || isNamedGuiUpdatingDirtily(gui)) {
            return;
        }

        HumanEntity humanEntity = event.getPlayer();
        PlayerInventory playerInventory = humanEntity.getInventory();

        //due to a client issue off-hand items appear as ghost items, this updates the off-hand correctly client-side
        playerInventory.setItemInOffHand(playerInventory.getItemInOffHand());

        gui.callOnClose(event);

        HumanEntityCache humanEntityCache = gui.getHumanEntityCache();

        if (humanEntityCache.contains(humanEntity)) {
            humanEntityCache.restoreAndForget(humanEntity);
        } else {
            for (ItemStack itemStack : humanEntity.getInventory()) {
                if (itemStack == null || !itemStack.hasItemMeta()) {
                    continue;
                }

                ItemMeta itemMeta = itemStack.getItemMeta();

                assert itemMeta != null;

                PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();

                for (GuiItem item : gui.getItems()) {
                    NamespacedKey key = item.getKey();

                    if (persistentDataContainer.has(key, UUIDTagType.INSTANCE)) {
                        persistentDataContainer.remove(key);
                        break;
                    }
                }

                itemStack.setItemMeta(itemMeta);
            }
        }

        if (gui.getViewerCount() == 1) {
            activeGuiInstances.remove(gui);
        }

        //Bukkit doesn't like it if you open an inventory while the previous one is being closed
        Bukkit.getScheduler().runTask(this.plugin, () -> gui.navigateToParent(humanEntity));
    }

    /**
     * Handles removing identifiers from gui items when an item is dropped from the gui.
     *
     * @param event the event fired
     * @since 0.12.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
        Gui gui = getGui(event.getPlayer().getOpenInventory().getTopInventory());

        if (gui == null) {
            return;
        }

        ItemStack itemStack = event.getItemDrop().getItemStack();

        if (!itemStack.hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        assert itemMeta != null;

        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();

        for (GuiItem item : gui.getItems()) {
            NamespacedKey key = item.getKey();

            if (persistentDataContainer.has(key, UUIDTagType.INSTANCE)) {
                persistentDataContainer.remove(key);
                break;
            }
        }

        itemStack.setItemMeta(itemMeta);
    }

    /**
     * Registers newly opened inventories
     *
     * @param event the event fired
     * @since 0.5.19
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        Gui gui = getGui(event.getInventory());

        if (gui == null || isNamedGuiUpdatingDirtily(gui)) {
            return;
        }

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
        if (event.getPlugin() != this.plugin) {
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
            Logger logger = this.plugin.getLogger();

            logger.warning(
                "Unable to close GUIs on plugin disable: they keep getting opened (tried: " + maxCount + " times)"
            );
		}
    }

    /**
     * Gets the gui from the inventory or null if the inventory isn't a gui
     *
     * @param inventory the inventory to get the gui from
     * @return the gui or null if the inventory doesn't have a gui
     * @since 0.8.1
     */
    @Nullable
    @Contract(pure = true)
    private Gui getGui(@NotNull Inventory inventory) {
        Gui gui = Gui.getGui(inventory);

        if (gui != null) {
            return gui;
        }

        InventoryHolder holder = inventory.getHolder();

        if (holder instanceof Gui) {
            return (Gui) holder;
        }

        return null;
    }

    private boolean isNamedGuiUpdatingDirtily(@NotNull Gui gui) {
        boolean dirtyTitle = gui instanceof NamedGui && (((NamedGui) gui).isDirty());
        boolean dirtyRows = gui instanceof ChestGui && ((ChestGui) gui).isDirtyRows();
        return gui.isUpdating() && (dirtyTitle || dirtyRows);
    }

}
