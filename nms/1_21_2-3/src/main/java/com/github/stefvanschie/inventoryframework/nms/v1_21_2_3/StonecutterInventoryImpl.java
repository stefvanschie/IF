package com.github.stefvanschie.inventoryframework.nms.v1_21_2_3;

import com.github.stefvanschie.inventoryframework.abstraction.StonecutterInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.util.TextHolderUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R2.inventory.CraftInventoryStonecutter;
import org.bukkit.craftbukkit.v1_21_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R2.inventory.view.CraftStonecutterView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Internal stonecutter inventory for 1.21.2
 *
 * @since 0.10.18
 */
public class StonecutterInventoryImpl extends StonecutterInventory {

    public StonecutterInventoryImpl(@NotNull InventoryHolder inventoryHolder) {
        super(inventoryHolder);
    }

    @Override
    public void openInventory(@NotNull Player player, @NotNull TextHolder title,
                              @Nullable org.bukkit.inventory.ItemStack[] items) {
        int itemAmount = items.length;

        if (itemAmount != 2) {
            throw new IllegalArgumentException(
                "The amount of items for a stonecutter should be 2, but is '" + itemAmount + "'"
            );
        }

        ServerPlayer serverPlayer = getServerPlayer(player);
        Component message = TextHolderUtil.toComponent(title);
        ContainerStonecutterImpl containerEnchantmentTable = new ContainerStonecutterImpl(serverPlayer, items, message);

        serverPlayer.containerMenu = containerEnchantmentTable;

        int id = containerEnchantmentTable.containerId;
        ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(id, MenuType.STONECUTTER, message);

        serverPlayer.connection.send(packet);

        sendItems(player, items);
    }

    @Override
    public void sendItems(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack[] items) {
        NonNullList<ItemStack> nmsItems = NonNullList.of(
            ItemStack.EMPTY,
            CraftItemStack.asNMSCopy(items[0]),
            CraftItemStack.asNMSCopy(items[1])
        );

        ServerPlayer serverPlayer = getServerPlayer(player);
        int containerId = getContainerId(serverPlayer);
        int state = serverPlayer.containerMenu.incrementStateId();
        ItemStack cursor = CraftItemStack.asNMSCopy(player.getItemOnCursor());
        ServerPlayerConnection playerConnection = getPlayerConnection(serverPlayer);

        playerConnection.send(new ClientboundContainerSetContentPacket(containerId, state, nmsItems, cursor));
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
     * A custom container enchanting table
     *
     * @since 0.10.18
     */
    private class ContainerStonecutterImpl extends StonecutterMenu {

        /**
         * The player for this enchanting table container
         */
        @NotNull
        private final Player player;

        /**
         * The internal bukkit entity for this container enchanting table
         */
        @Nullable
        private CraftStonecutterView bukkitEntity;

        /**
         * Field for accessing the result inventory field
         */
        @NotNull
        private final Field resultContainerField;

        public ContainerStonecutterImpl(@NotNull ServerPlayer entityPlayer,
                                        @Nullable org.bukkit.inventory.ItemStack[] items, @NotNull Component title) {
            super(entityPlayer.nextContainerCounter(), entityPlayer.getInventory());

            this.player = entityPlayer.getBukkitEntity();

            setTitle(title);

            try {
                //noinspection JavaReflectionMemberAccess
                this.resultContainerField = StonecutterMenu.class.getDeclaredField("C"); //resultContainer
                this.resultContainerField.setAccessible(true);
            } catch (NoSuchFieldException exception) {
                throw new RuntimeException(exception);
            }

            container.setItem(0, CraftItemStack.asNMSCopy(items[0]));
            getResultInventory().setItem(0, CraftItemStack.asNMSCopy(items[1]));
        }

        @NotNull
        @Override
        public CraftStonecutterView getBukkitView() {
            if (bukkitEntity == null) {
                org.bukkit.inventory.StonecutterInventory inventory = new CraftInventoryStonecutter(
                    this.container, getResultInventory()
                ) {
                    @NotNull
                    @Contract(pure = true)
                    @Override
                    public InventoryHolder getHolder() {
                        return inventoryHolder;
                    }
                };

                this.bukkitEntity = new CraftStonecutterView(player, inventory, this);
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

        /**
         * Gets the result inventory
         *
         * @return the result inventory
         * @since 0.10.18
         */
        @NotNull
        @Contract(pure = true)
        public Container getResultInventory() {
            try {
                return (Container) resultContainerField.get(this);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
