package com.github.stefvanschie.inventoryframework.nms.v1_17_R1;

import com.github.stefvanschie.inventoryframework.abstraction.AnvilInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_17_R1.util.TextHolderUtil;
import com.github.stefvanschie.inventoryframework.nms.v1_17_R1.util.CustomInventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal anvil inventory for 1.17 R1
 *
 * @since 0.9.9
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

        ServerPlayer serverPlayer = getServerPlayer(player);
        ContainerAnvilImpl containerAnvil = new ContainerAnvilImpl(serverPlayer, items);

        serverPlayer.containerMenu = containerAnvil;

        int id = containerAnvil.containerId;
        Component message = TextHolderUtil.toComponent(title);

        serverPlayer.connection.send(new ClientboundOpenScreenPacket(id, MenuType.ANVIL, message));

        sendItems(player, items);
    }

    @Override
    public void sendItems(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack[] items) {
        NonNullList<ItemStack> nmsItems = CustomInventoryUtil.convertToNMSItems(items);
        ServerPlayer serverPlayer = getServerPlayer(player);
        int containerId = getContainerId(serverPlayer);

        getPlayerConnection(serverPlayer).send(new ClientboundContainerSetContentPacket(containerId, nmsItems));
    }

    @Override
    public void sendFirstItem(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        int containerId = getContainerId(serverPlayer);
        ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        getPlayerConnection(serverPlayer).send(new ClientboundContainerSetSlotPacket(containerId, 0, nmsItem));
    }

    @Override
    public void sendSecondItem(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        int containerId = getContainerId(serverPlayer);
        ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        getPlayerConnection(serverPlayer).send(new ClientboundContainerSetSlotPacket(containerId, 1, nmsItem));
    }

    @Override
    public void sendResultItem(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        sendResultItem(player, CraftItemStack.asNMSCopy(item));
    }

    @Override
    public void clearResultItem(@NotNull Player player) {
        sendResultItem(player, ItemStack.EMPTY);
    }

    @Override
    public void setCursor(@NotNull Player player, @NotNull org.bukkit.inventory.ItemStack item) {
        setCursor(player, CraftItemStack.asNMSCopy(item));
    }

    @Override
    public void clearCursor(@NotNull Player player) {
        ClientboundContainerSetSlotPacket packet = new ClientboundContainerSetSlotPacket(-1, -1, ItemStack.EMPTY);

        getPlayerConnection(getServerPlayer(player)).send(packet);
    }

    /**
     * Sets the cursor of the given player
     *
     * @param player the player to set the cursor
     * @param item the item to set the cursor to
     * @since 0.9.9
     */
    private void setCursor(@NotNull Player player, @NotNull ItemStack item) {
        getPlayerConnection(getServerPlayer(player)).send(new ClientboundContainerSetSlotPacket(-1, -1, item));
    }

    /**
     * Sends the result item to the specified player with the given item
     *
     * @param player the player to send the result item to
     * @param item the result item
     * @since 0.9.9
     */
    private void sendResultItem(@NotNull Player player, @NotNull ItemStack item) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        int containerId = getContainerId(serverPlayer);

        getPlayerConnection(serverPlayer).send(new ClientboundContainerSetSlotPacket(containerId, 2, item));
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
     * A custom container anvil for responding to item renaming
     *
     * @since 0.9.9
     */
    private class ContainerAnvilImpl extends AnvilMenu {

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
         * @param serverPlayer the player for who this anvil container is
         * @since 0.9.9
         */
        public ContainerAnvilImpl(@NotNull ServerPlayer serverPlayer,
                                  @Nullable org.bukkit.inventory.ItemStack[] items) {
            super(serverPlayer.nextContainerCounter(), serverPlayer.getInventory(),
                ContainerLevelAccess.create(serverPlayer.getCommandSenderWorld(), new BlockPos(0, 0, 0)));

            this.player = serverPlayer.getBukkitEntity();

            inputSlots.setItem(0, CraftItemStack.asNMSCopy(items[0]));
            inputSlots.setItem(1, CraftItemStack.asNMSCopy(items[1]));
            inputSlots.setItem(0, CraftItemStack.asNMSCopy(items[2]));
        }

        @NotNull
        @Override
        public CraftInventoryView getBukkitView() {
            if (bukkitEntity == null) {
                Location location = access.getLocation();
                CraftInventory inventory = new CraftInventoryAnvil(location, inputSlots, resultSlots,
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
        public void setItemName(@Nullable String name) {
            text = name == null ? "" : name;

            sendResultItem(player, resultSlots.getItem(0));
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
