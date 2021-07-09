package com.github.stefvanschie.inventoryframework.nms.v1_17_0;

import com.github.stefvanschie.inventoryframework.abstraction.BeaconInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryBeacon;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Internal beacon inventory for 1.17 R1
 *
 * @since 0.9.9
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
        TranslatableComponent message = new TranslatableComponent("Beacon");

        serverPlayer.connection.send(new ClientboundOpenScreenPacket(id, MenuType.BEACON, message));

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

        getPlayerConnection(serverPlayer).send(new ClientboundContainerSetContentPacket(containerId, items));
    }

    @Override
    public void clearCursor(@NotNull Player player) {
        ClientboundContainerSetSlotPacket packet = new ClientboundContainerSetSlotPacket(-1, -1, ItemStack.EMPTY);

        getPlayerConnection(getServerPlayer(player)).send(packet);
    }

    /**
     * Gets the container id for the inventory view the player currently has open
     *
     * @param nmsPlayer the player to get the container id for
     * @return the container id
     * @since 0.9.9
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
     * @since 0.9.9
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
     * @since 0.9.9
     */
    @NotNull
    @Contract(pure = true)
    private ServerPlayer getServerPlayer(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    /**
     * A custom container beacon
     *
     * @since 0.9.9
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
        private CraftInventoryView bukkitEntity;

        /**
         * Field for accessing the beacon field
         */
        @NotNull
        private final Field beaconField;

        public ContainerBeaconImpl(@NotNull ServerPlayer serverPlayer, @Nullable org.bukkit.inventory.ItemStack item) {
            super(serverPlayer.nextContainerCounter(), serverPlayer.getInventory());

            this.player = serverPlayer.getBukkitEntity();

            try {
                //noinspection JavaReflectionMemberAccess
                this.beaconField = BeaconMenu.class.getDeclaredField("r"); //beacon
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
        public CraftInventoryView getBukkitView() {
            if (bukkitEntity == null) {
                try {
                    CraftInventory inventory = new CraftInventoryBeacon((Container) beaconField.get(this)) {
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
        public boolean stillValid(@Nullable net.minecraft.world.entity.player.Player nmsPlayer) {
            return true;
        }

        @Override
        public void slotsChanged(Container container) {}

        @Override
        public void removed(net.minecraft.world.entity.player.Player nmsPlayer) {}

    }
}
