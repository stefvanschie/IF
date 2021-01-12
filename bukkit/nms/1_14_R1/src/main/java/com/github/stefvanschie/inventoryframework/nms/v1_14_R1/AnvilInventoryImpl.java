package com.github.stefvanschie.inventoryframework.nms.v1_14_R1;

import com.github.stefvanschie.inventoryframework.abstraction.AnvilInventory;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Internal anvil inventory for 1.14 R1
 *
 * @since 0.8.0
 */
public class AnvilInventoryImpl extends AnvilInventory {

    public AnvilInventoryImpl(@NotNull InventoryHolder inventoryHolder) {
        super(inventoryHolder);
    }

    @Override
    public void openInventory(@NotNull Player player, @NotNull String title,
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
        ChatMessage message = new ChatMessage(title);

        entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(id, Containers.ANVIL, message));

        sendItems(player, items);
    }

    @Override
    public void sendItems(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack[] items) {
        NonNullList<ItemStack> nmsItems = NonNullList.a(
            ItemStack.a,
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
        sendResultItem(player, ItemStack.a);
    }

    @Override
    public void setCursor(@NotNull Player player, @NotNull org.bukkit.inventory.ItemStack item) {
        setCursor(player, CraftItemStack.asNMSCopy(item));
    }

    @Override
    public void clearCursor(@NotNull Player player) {
        getPlayerConnection(getEntityPlayer(player)).sendPacket(new PacketPlayOutSetSlot(-1, -1, ItemStack.a));
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
         * Field for accessing the repair inventory field
         */
        @NotNull
        private final Field repairInventoryField;

        /**
         * Field for accessing the result inventory field
         */
        @NotNull
        private final Field resultInventoryField;

        /**
         * Field for accessing the container access field
         */
        @NotNull
        private final Field containerAccessField;

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

            try {
                repairInventoryField = ContainerAnvil.class.getDeclaredField("repairInventory");
                repairInventoryField.setAccessible(true);

                resultInventoryField = ContainerAnvil.class.getDeclaredField("resultInventory");
                resultInventoryField.setAccessible(true);

                containerAccessField = ContainerAnvil.class.getDeclaredField("containerAccess");
                containerAccessField.setAccessible(true);
            } catch (NoSuchFieldException exception) {
                throw new RuntimeException(exception);
            }

            getRepairInventory().setItem(0, CraftItemStack.asNMSCopy(items[0]));
            getRepairInventory().setItem(1, CraftItemStack.asNMSCopy(items[1]));
            getResultInventory().setItem(0, CraftItemStack.asNMSCopy(items[2]));
        }

        @NotNull
        @Override
        public CraftInventoryView getBukkitView() {
            if (bukkitEntity == null) {
                Location location = getContainerAccess().getLocation();
                CraftInventory inventory = new CraftInventoryAnvil(location, getRepairInventory(), getResultInventory(),
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

            sendResultItem(player, getResultInventory().getItem(0));
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

        @NotNull
        @Contract(pure = true)
        private IInventory getRepairInventory() {
            try {
                return (IInventory) repairInventoryField.get(this);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }

        @NotNull
        @Contract(pure = true)
        private IInventory getResultInventory() {
            try {
                return (IInventory) resultInventoryField.get(this);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }

        @NotNull
        @Contract(pure = true)
        private ContainerAccess getContainerAccess() {
            try {
                return (ContainerAccess) containerAccessField.get(this);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
