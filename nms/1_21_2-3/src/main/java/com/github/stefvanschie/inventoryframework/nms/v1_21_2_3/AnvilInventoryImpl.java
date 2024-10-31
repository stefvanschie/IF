package com.github.stefvanschie.inventoryframework.nms.v1_21_2_3;

import com.github.stefvanschie.inventoryframework.abstraction.AnvilInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.util.CustomInventoryUtil;
import com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.util.TextHolderUtil;
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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_21_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal anvil inventory for 1.21.2
 *
 * @since 0.10.18
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

        ServerPlayer serverPlayer = getServerPlayer(player);

        //ignore deprecation: superseding method is only available on Paper
        //noinspection deprecation
        CraftEventFactory.handleInventoryCloseEvent(serverPlayer);

        serverPlayer.containerMenu = serverPlayer.inventoryMenu;

        Component message = TextHolderUtil.toComponent(title);
        ContainerAnvilImpl containerAnvil = new ContainerAnvilImpl(serverPlayer, message);

        Inventory inventory = containerAnvil.getBukkitView().getTopInventory();

        inventory.setItem(0, items[0]);
        inventory.setItem(1, items[1]);
        inventory.setItem(2, items[2]);

        int containerId = containerAnvil.getContainerId();

        serverPlayer.connection.send(new ClientboundOpenScreenPacket(containerId, MenuType.ANVIL, message));
        serverPlayer.containerMenu = containerAnvil;
        serverPlayer.initMenu(containerAnvil);

        return inventory;
    }

    @Override
    public void sendItems(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack[] items) {
        NonNullList<ItemStack> nmsItems = CustomInventoryUtil.convertToNMSItems(items);
        ServerPlayer serverPlayer = getServerPlayer(player);
        int containerId = getContainerId(serverPlayer);
        int state = serverPlayer.containerMenu.incrementStateId();
        ItemStack cursor = CraftItemStack.asNMSCopy(player.getItemOnCursor());
        ServerPlayerConnection playerConnection = getPlayerConnection(serverPlayer);

        playerConnection.send(new ClientboundContainerSetContentPacket(containerId, state, nmsItems, cursor));
    }

    @Override
    public void sendFirstItem(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        int containerId = getContainerId(serverPlayer);
        ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        int state = serverPlayer.containerMenu.incrementStateId();

        getPlayerConnection(serverPlayer).send(new ClientboundContainerSetSlotPacket(containerId, state, 0, nmsItem));
    }

    @Override
    public void sendSecondItem(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        int containerId = getContainerId(serverPlayer);
        ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        int state = serverPlayer.containerMenu.incrementStateId();

        getPlayerConnection(serverPlayer).send(new ClientboundContainerSetSlotPacket(containerId, state, 1, nmsItem));
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
        ServerPlayer serverPlayer = getServerPlayer(player);
        int state = serverPlayer.containerMenu.incrementStateId();

        getPlayerConnection(serverPlayer).send(new ClientboundContainerSetSlotPacket(-1, state, -1, ItemStack.EMPTY));
    }

    /**
     * Sets the cursor of the given player
     *
     * @param player the player to set the cursor
     * @param item the item to set the cursor to
     * @since 0.10.18
     * @deprecated no longer used internally
     */
    @Deprecated
    private void setCursor(@NotNull Player player, @NotNull ItemStack item) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        int state = serverPlayer.containerMenu.incrementStateId();

        getPlayerConnection(serverPlayer).send(new ClientboundContainerSetSlotPacket(-1, state, -1, item));
    }

    /**
     * Sends the result item to the specified player with the given item
     *
     * @param player the player to send the result item to
     * @param item the result item
     * @since 0.10.18
     * @deprecated no longer used internally
     */
    @Deprecated
    private void sendResultItem(@NotNull Player player, @NotNull ItemStack item) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        int containerId = getContainerId(serverPlayer);
        int state = serverPlayer.containerMenu.incrementStateId();

        getPlayerConnection(serverPlayer).send(new ClientboundContainerSetSlotPacket(containerId, state, 2, item));
    }

    /**
     * Gets the container id for the inventory view the player currently has open
     *
     * @param nmsPlayer the player to get the container id for
     * @return the container id
     * @since 0.10.18
     * @deprecated no longer used internally
     */
    @Contract(pure = true)
    @Deprecated
    private int getContainerId(@NotNull net.minecraft.world.entity.player.Player nmsPlayer) {
        return nmsPlayer.containerMenu.containerId;
    }

    /**
     * Gets the player connection for the specified player
     *
     * @param serverPlayer the player to get the player connection from
     * @return the player connection
     * @since 0.10.18
     * @deprecated no longer used internally
     */
    @NotNull
    @Contract(pure = true)
    @Deprecated
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
     * A custom container anvil for responding to item renaming
     *
     * @since 0.10.18
     */
    private class ContainerAnvilImpl extends AnvilMenu {

        /**
         * Creates a new custom anvil container for the specified player
         *
         * @param serverPlayer the player for whom this anvil container is
         * @param title the title of the inventory
         * @since 0.10.18
         */
        public ContainerAnvilImpl(@NotNull ServerPlayer serverPlayer, @NotNull Component title) {
            super(serverPlayer.nextContainerCounter(), serverPlayer.getInventory(),
                ContainerLevelAccess.create(serverPlayer.getCommandSenderWorld(), new BlockPos(0, 0, 0)));

            this.checkReachable = false;
            this.cost.set(AnvilInventoryImpl.super.cost);

            setTitle(title);

            Slot originalSlot = this.slots.get(2);

            this.slots.set(2, new Slot(originalSlot.container, originalSlot.index, originalSlot.x, originalSlot.y) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return true;
                }

                @Override
                public boolean mayPickup(net.minecraft.world.entity.player.@NotNull Player playerEntity) {
                    return true;
                }

                @Override
                public void onTake(net.minecraft.world.entity.player.@NotNull Player player, @NotNull ItemStack stack) {
                    originalSlot.onTake(player, stack);
                }
            });
        }

        @Override
        public boolean setItemName(@Nullable String name) {
            name = name == null ? "" : name;

            /* Only update if the name is actually different. This may be called even if the name is not different,
               particularly when putting an item in the first slot. */
            if (!name.equals(AnvilInventoryImpl.super.observableText.get())) {
                AnvilInventoryImpl.super.observableText.set(name);
            }

            //the client predicts the output result, so we broadcast the state again to override it
            broadcastFullState();
            return true; //no idea what this is for
        }

        @Override
        public void createResult() {}

        @Override
        public void removed(net.minecraft.world.entity.player.@NotNull Player nmsPlayer) {}

        @Override
        protected void clearContainer(net.minecraft.world.entity.player.@NotNull Player player,
                                      @NotNull Container inventory) {}

        @Override
        protected void onTake(net.minecraft.world.entity.player.@NotNull Player player, @NotNull ItemStack stack) {}

        public int getContainerId() {
            return this.containerId;
        }
    }
}
