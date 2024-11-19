package com.github.stefvanschie.inventoryframework.nms.v1_21_1;

import com.github.stefvanschie.inventoryframework.abstraction.BeaconInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventoryBeacon;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R1.inventory.view.CraftBeaconView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Internal beacon inventory for 1.21.1
 *
 * @since 0.10.18
 */
public class BeaconInventoryImpl extends BeaconInventory {

    public BeaconInventoryImpl(@NotNull InventoryHolder inventoryHolder) {
        super(inventoryHolder);
    }

    @Override
    public void openInventory(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        ContainerBeaconImpl containerBeacon = new ContainerBeaconImpl(serverPlayer, item);

        serverPlayer.containerMenu = containerBeacon;

        int id = containerBeacon.containerId;
        Component beacon = Component.literal("Beacon");

        serverPlayer.connection.send(new ClientboundOpenScreenPacket(id, MenuType.BEACON, beacon));

        sendItem(player, item);
    }

    @Override
    public void sendItem(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        NonNullList<ItemStack> items = NonNullList.of(
            ItemStack.EMPTY, //the first item doesn't count for some reason, so send a dummy item
            CraftItemStack.asNMSCopy(item)
        );

        ServerPlayer serverPlayer = getServerPlayer(player);
        int containerId = getContainerId(serverPlayer);
        int state = serverPlayer.containerMenu.incrementStateId();
        ItemStack cursor = CraftItemStack.asNMSCopy(player.getItemOnCursor());
        ServerPlayerConnection playerConnection = getPlayerConnection(serverPlayer);

        playerConnection.send(new ClientboundContainerSetContentPacket(containerId, state, items, cursor));
    }

    @Override
    public void clearCursor(@NotNull Player player) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        int state = serverPlayer.containerMenu.incrementStateId();

        getPlayerConnection(serverPlayer).send(new ClientboundContainerSetSlotPacket(-1, state, -1, ItemStack.EMPTY));
    }

    /**
     * Gets the container id for the inventory view the player currently has open
     *
     * @param nmsPlayer the player to get the container id for
     * @return the container id
     * @since 0.10.18
     */
    @Contract(pure = true)
    private int getContainerId(@NotNull net.minecraft.world.entity.player.Player nmsPlayer) {
        return nmsPlayer.containerMenu.containerId;
    }

    /**
     * Gets the player connection for the specified player
     *
     * @param serverPlayer the player to get the player connection from
     * @return the player connection
     * @since 0.10.18
     */
    @NotNull
    @Contract(pure = true)
    private ServerPlayerConnection getPlayerConnection(@NotNull ServerPlayer serverPlayer) {
        return serverPlayer.connection;
    }

    /**
     * Gets the server player associated to this player
     *
     * @param player the player to get the server player from
     * @return the server player
     * @since 0.10.18
     */
    @NotNull
    @Contract(pure = true)
    private ServerPlayer getServerPlayer(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    /**
     * A custom container beacon
     *
     * @since 0.10.18
     */
    private class ContainerBeaconImpl extends BeaconMenu {

        /**
         * The player for this beacon container
         */
        @NotNull
        private final Player player;

        /**
         * The internal bukkit entity for this container beacon
         */
        @Nullable
        private CraftBeaconView bukkitEntity;

        /**
         * Field for accessing the beacon field
         */
        @NotNull
        private final Field beaconField;

        public ContainerBeaconImpl(@NotNull ServerPlayer serverPlayer, @Nullable org.bukkit.inventory.ItemStack item) {
            super(serverPlayer.nextContainerCounter(), serverPlayer.getInventory());

            this.player = serverPlayer.getBukkitEntity();
            setTitle(Component.empty());

            try {
                //noinspection JavaReflectionMemberAccess
                this.beaconField = BeaconMenu.class.getDeclaredField("s"); //beacon
                this.beaconField.setAccessible(true);
            } catch (NoSuchFieldException exception) {
                throw new RuntimeException(exception);
            }

            try {
                ItemStack itemStack = CraftItemStack.asNMSCopy(item);

                ((Container) beaconField.get(this)).setItem(0, itemStack);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }

        @NotNull
        @Override
        public CraftBeaconView getBukkitView() {
            if (bukkitEntity == null) {
                try {
                    Container container = (Container) beaconField.get(this);

                    org.bukkit.inventory.BeaconInventory inventory = new CraftInventoryBeacon(container) {
                        @NotNull
                        @Contract(pure = true)
                        @Override
                        public InventoryHolder getHolder() {
                            return inventoryHolder;
                        }
                    };

                    this.bukkitEntity = new CraftBeaconView(player, inventory, this);
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException(exception);
                }
            }

            return bukkitEntity;
        }

        @Contract(pure = true, value = "_ -> true")
        @Override
        public boolean stillValid(@Nullable net.minecraft.world.entity.player.Player nmsPlayer) {
            return true;
        }

        @Override
        public void slotsChanged(Container container) {}

        @Override
        public void removed(net.minecraft.world.entity.player.Player nmsPlayer) {}

    }
}
