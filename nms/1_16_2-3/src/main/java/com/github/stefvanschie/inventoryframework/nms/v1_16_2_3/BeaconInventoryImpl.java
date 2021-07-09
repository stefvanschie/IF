package com.github.stefvanschie.inventoryframework.nms.v1_16_2_3;

import com.github.stefvanschie.inventoryframework.abstraction.BeaconInventory;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.inventory.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Internal beacon inventory for 1.16 R2
 *
 * @since 0.8.0
 */
public class BeaconInventoryImpl extends BeaconInventory {

    public BeaconInventoryImpl(@NotNull InventoryHolder inventoryHolder) {
        super(inventoryHolder);
    }

    @Override
    public void openInventory(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        EntityPlayer entityPlayer = getEntityPlayer(player);
        ContainerBeaconImpl containerBeacon = new ContainerBeaconImpl(entityPlayer, item);

        entityPlayer.activeContainer = containerBeacon;

        int id = containerBeacon.windowId;
        ChatMessage message = new ChatMessage("Beacon");

        entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(id, Containers.BEACON, message));

        sendItem(player, item);
    }

    @Override
    public void sendItem(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        NonNullList<ItemStack> items = NonNullList.a(
            ItemStack.b, //the first item doesn't count for some reason, so send a dummy item
            CraftItemStack.asNMSCopy(item)
        );

        EntityPlayer entityPlayer = getEntityPlayer(player);

        getPlayerConnection(entityPlayer).sendPacket(new PacketPlayOutWindowItems(getWindowId(entityPlayer), items));
    }

    @Override
    public void clearCursor(@NotNull Player player) {
        getPlayerConnection(getEntityPlayer(player)).sendPacket(new PacketPlayOutSetSlot(-1, -1, ItemStack.b));
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
     * A custom container beacon
     *
     * @since 0.8.0
     */
    private class ContainerBeaconImpl extends ContainerBeacon {

        /**
         * The player for this beacon container
         */
        @NotNull
        private final Player player;

        /**
         * The internal bukkit entity for this container beacon
         */
        @Nullable
        private CraftInventoryView bukkitEntity;

        /**
         * Field for accessing the beacon field
         */
        @NotNull
        private final Field beaconField;

        public ContainerBeaconImpl(@NotNull EntityPlayer entityPlayer, @Nullable org.bukkit.inventory.ItemStack item) {
            super(entityPlayer.nextContainerCounter(), entityPlayer.inventory);

            this.player = entityPlayer.getBukkitEntity();

            try {
                this.beaconField = ContainerBeacon.class.getDeclaredField("beacon");
                this.beaconField.setAccessible(true);
            } catch (NoSuchFieldException exception) {
                throw new RuntimeException(exception);
            }

            try {
                ItemStack itemStack = CraftItemStack.asNMSCopy(item);

                ((IInventory) beaconField.get(this)).setItem(0, itemStack);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }

        @NotNull
        @Override
        public CraftInventoryView getBukkitView() {
            if (bukkitEntity == null) {
                try {
                    CraftInventory inventory = new CraftInventoryBeacon((IInventory) beaconField.get(this)) {
                        @NotNull
                        @Contract(pure = true)
                        @Override
                        public InventoryHolder getHolder() {
                            return inventoryHolder;
                        }
                    };

                    bukkitEntity = new CraftInventoryView(player, inventory, this);
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException(exception);
                }
            }

            return bukkitEntity;
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
