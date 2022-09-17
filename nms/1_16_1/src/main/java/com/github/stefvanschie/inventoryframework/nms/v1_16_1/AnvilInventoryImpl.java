package com.github.stefvanschie.inventoryframework.nms.v1_16_1;

import com.github.stefvanschie.inventoryframework.abstraction.AnvilInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_16_1.util.TextHolderUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal anvil inventory for 1.16 R1
 *
 * @since 0.8.0
 */
public class AnvilInventoryImpl extends AnvilInventory {

    public AnvilInventoryImpl(@NotNull InventoryHolder inventoryHolder) {
        super(inventoryHolder);
    }

    @Override
    public Inventory openInventory(@NotNull Player player, @NotNull TextHolder title,
                                   @Nullable org.bukkit.inventory.ItemStack[] items) {
        int itemAmount = items.length;

        if (itemAmount != 3) {
            throw new IllegalArgumentException(
                    "The amount of items for an anvil should be 3, but is '" + itemAmount + "'"
            );
        }

        EntityPlayer entityPlayer = getEntityPlayer(player);

        CraftEventFactory.handleInventoryCloseEvent(entityPlayer, InventoryCloseEvent.Reason.OPEN_NEW);

        entityPlayer.activeContainer = entityPlayer.defaultContainer;

        IChatBaseComponent message = TextHolderUtil.toComponent(title);
        ContainerAnvilImpl containerAnvil = new ContainerAnvilImpl(entityPlayer, message);

        Inventory inventory = containerAnvil.getBukkitView().getTopInventory();

        inventory.setItem(0, items[0]);
        inventory.setItem(1, items[1]);
        inventory.setItem(2, items[2]);

        int containerId = containerAnvil.getContainerId();

        entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, Containers.ANVIL, message));
        entityPlayer.activeContainer = containerAnvil;
        entityPlayer.syncInventory();

        return inventory;
    }

    @Override
    public void sendItems(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack[] items) {
        NonNullList<ItemStack> nmsItems = NonNullList.a(
            ItemStack.b,
            CraftItemStack.asNMSCopy(items[0]),
            CraftItemStack.asNMSCopy(items[1]),
            CraftItemStack.asNMSCopy(items[2])
        );

        EntityPlayer entityPlayer = getEntityPlayer(player);

        getPlayerConnection(entityPlayer).sendPacket(new PacketPlayOutWindowItems(getWindowId(entityPlayer), nmsItems));
    }

    @Override
    public void sendFirstItem(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        EntityPlayer entityPlayer = getEntityPlayer(player);
        ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        getPlayerConnection(entityPlayer).sendPacket(new PacketPlayOutSetSlot(getWindowId(entityPlayer), 0, nmsItem));
    }

    @Override
    public void sendSecondItem(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        EntityPlayer entityPlayer = getEntityPlayer(player);
        ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        getPlayerConnection(entityPlayer).sendPacket(new PacketPlayOutSetSlot(getWindowId(entityPlayer), 1, nmsItem));
    }

    @Override
    public void sendResultItem(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        sendResultItem(player, CraftItemStack.asNMSCopy(item));
    }

    @Override
    public void clearResultItem(@NotNull Player player) {
        sendResultItem(player, ItemStack.b);
    }

    @Override
    public void setCursor(@NotNull Player player, @NotNull org.bukkit.inventory.ItemStack item) {
        setCursor(player, CraftItemStack.asNMSCopy(item));
    }

    @Override
    public void clearCursor(@NotNull Player player) {
        getPlayerConnection(getEntityPlayer(player)).sendPacket(new PacketPlayOutSetSlot(-1, -1, ItemStack.b));
    }

    /**
     * Sets the cursor of the given player
     *
     * @param player the player to set the cursor
     * @param item the item to set the cursor to
     * @since 0.8.0
     * @deprecated no longer used internally
     */
    @Deprecated
    private void setCursor(@NotNull Player player, @NotNull ItemStack item) {
        getPlayerConnection(getEntityPlayer(player)).sendPacket(new PacketPlayOutSetSlot(-1, -1, item));
    }

    /**
     * Sends the result item to the specified player with the given item
     *
     * @param player the player to send the result item to
     * @param item the result item
     * @since 0.8.0
     * @deprecated no longer used internally
     */
    @Deprecated
    private void sendResultItem(@NotNull Player player, @NotNull ItemStack item) {
        EntityPlayer entityPlayer = getEntityPlayer(player);

        getPlayerConnection(entityPlayer).sendPacket(new PacketPlayOutSetSlot(getWindowId(entityPlayer), 2, item));
    }

    /**
     * Gets the window id for the inventory view the player currently has open
     *
     * @param entityPlayer the player to get the window id for
     * @return the window id
     * @since 0.8.0
     * @deprecated no longer used internally
     */
    @Contract(pure = true)
    @Deprecated
    private int getWindowId(@NotNull EntityPlayer entityPlayer) {
        return entityPlayer.activeContainer.windowId;
    }

    /**
     * Gets the player connection for the specified player
     *
     * @param entityPlayer the player to get the player connection from
     * @return the player connection
     * @since 0.8.0
     * @deprecated no longer used internally
     */
    @NotNull
    @Contract(pure = true)
    @Deprecated
    private PlayerConnection getPlayerConnection(@NotNull EntityPlayer entityPlayer) {
        return entityPlayer.playerConnection;
    }

    /**
     * Gets the entity player associated to this player
     *
     * @param player the player to get the entity player from
     * @return the entity player
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    private EntityPlayer getEntityPlayer(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    /**
     * A custom container anvil for responding to item renaming
     *
     * @since 0.8.0
     */
    private class ContainerAnvilImpl extends ContainerAnvil {

        /**
         * The index of the result slot
         */
        private static final int RESULT_SLOT_INDEX = 2;

        /**
         * The player for whom this container is
         */
        @NotNull
        private final EntityPlayer entityPlayer;

        /**
         * Creates a new custom anvil container for the specified player
         *
         * @param entityPlayer the player for whom this anvil container is
         * @param title the title of the inventory
         * @since 0.10.8
         */
        public ContainerAnvilImpl(@NotNull EntityPlayer entityPlayer, @NotNull IChatBaseComponent title) {
            super(entityPlayer.nextContainerCounter(), entityPlayer.inventory,
                    ContainerAccess.at(entityPlayer.getWorld(), new BlockPosition(0, 0, 0)));

            this.entityPlayer = entityPlayer;

            this.checkReachable = false;

            setTitle(title);

            Slot originalSlot = this.slots.get(RESULT_SLOT_INDEX);

            Slot newSlot = new Slot(originalSlot.inventory, originalSlot.index, originalSlot.e, originalSlot.f) {
                @Override
                public boolean isAllowed(ItemStack itemStack) {
                    return true;
                }

                @Override
                public boolean isAllowed(EntityHuman entityHuman) {
                    return true;
                }

                @Override
                public ItemStack a(EntityHuman entityHuman, @NotNull ItemStack itemStack) {
                    return originalSlot.a(entityHuman, itemStack);
                }
            };

            this.slots.set(RESULT_SLOT_INDEX, newSlot);
        }

        @Override
        public void a(@Nullable String name) {
            AnvilInventoryImpl.super.text = name == null ? "" : name;

            //the client predicts the output result, so we broadcast the state again to override it
            this.entityPlayer.updateInventory(entityPlayer.activeContainer);
        }

        @Override
        public void e() {}

        @Override
        public void b(EntityHuman entityHuman) {}

        @Override
        protected void a(EntityHuman entityHuman, World world, @NotNull IInventory inventory) {}

        @Override
        protected ItemStack a(EntityHuman entityHuman, @NotNull ItemStack itemStack) {
            return itemStack;
        }

        @Override
        public ItemStack a(int i, int j, InventoryClickType inventoryclicktype, EntityHuman entityhuman) {
            ItemStack itemStack = super.a(i, j, inventoryclicktype, entityhuman);

            //the client predicts the allowed movement of the item, so we broadcast the state again to override it
            this.entityPlayer.updateInventory(entityPlayer.activeContainer);

            return itemStack;
        }

        public int getContainerId() {
            return this.windowId;
        }
    }
}
