package com.github.stefvanschie.inventoryframework.nms.v1_16_R1;

import com.github.stefvanschie.inventoryframework.abstraction.AnvilInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_16_R1.util.AdventureSupportUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
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
    public void openInventory(@NotNull Player player, @NotNull TextHolder title,
                              @Nullable org.bukkit.inventory.ItemStack[] items) {
        int itemAmount = items.length;

        if (itemAmount != 3) {
            throw new IllegalArgumentException(
                "The amount of items for an anvil should be 3, but is '" + itemAmount + "'"
            );
        }

        EntityPlayer entityPlayer = getEntityPlayer(player);
        ContainerAnvilImpl containerAnvil = new ContainerAnvilImpl(entityPlayer, items);

        entityPlayer.activeContainer = containerAnvil;

        int id = containerAnvil.windowId;
        IChatBaseComponent message = AdventureSupportUtil.toComponent(title);

        entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(id, Containers.ANVIL, message));

        sendItems(player, items);
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
     */
    private void setCursor(@NotNull Player player, @NotNull ItemStack item) {
        getPlayerConnection(getEntityPlayer(player)).sendPacket(new PacketPlayOutSetSlot(-1, -1, item));
    }

    /**
     * Sends the result item to the specified player with the given item
     *
     * @param player the player to send the result item to
     * @param item the result item
     * @since 0.8.0
     */
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
     */
    @Contract(pure = true)
    private int getWindowId(@NotNull EntityPlayer entityPlayer) {
        return entityPlayer.activeContainer.windowId;
    }

    /**
     * Gets the player connection for the specified player
     *
     * @param entityPlayer the player to get the player connection from
     * @return the player connection
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
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
         * The player for whom this anvil container is
         */
        @NotNull
        private final Player player;

        /**
         * The internal bukkit entity for this container anvil
         */
        @Nullable
        private CraftInventoryView bukkitEntity;

        /**
         * Creates a new custom anvil container for the specified player
         *
         * @param entityPlayer the player for who this anvil container is
         * @since 0.8.0
         */
        public ContainerAnvilImpl(@NotNull EntityPlayer entityPlayer,
                                  @Nullable org.bukkit.inventory.ItemStack[] items) {
            super(entityPlayer.nextContainerCounter(), entityPlayer.inventory,
                ContainerAccess.at(entityPlayer.getWorld(), new BlockPosition(0, 0, 0)));

            this.player = entityPlayer.getBukkitEntity();

            repairInventory.setItem(0, CraftItemStack.asNMSCopy(items[0]));
            repairInventory.setItem(1, CraftItemStack.asNMSCopy(items[1]));
            resultInventory.setItem(0, CraftItemStack.asNMSCopy(items[2]));
        }

        @NotNull
        @Override
        public CraftInventoryView getBukkitView() {
            if (bukkitEntity == null) {
                Location location = containerAccess.getLocation();
                CraftInventory inventory = new CraftInventoryAnvil(location, repairInventory, resultInventory,
                    this) {
                    @NotNull
                    @Contract(pure = true)
                    @Override
                    public InventoryHolder getHolder() {
                        return inventoryHolder;
                    }
                };

                bukkitEntity = new CraftInventoryView(player, inventory, this);
            }

            return bukkitEntity;
        }

        @Override
        public void a(@Nullable String name) {
            text = name == null ? "" : name;

            sendResultItem(player, resultInventory.getItem(0));
        }

        @Contract(pure = true, value = "_ -> true")
        @Override
        public boolean canUse(@Nullable EntityHuman entityhuman) {
            return true;
        }

        @Override
        public void a(IInventory inventory) {}

        @Override
        public void b(EntityHuman entityhuman) {}
    }
}
