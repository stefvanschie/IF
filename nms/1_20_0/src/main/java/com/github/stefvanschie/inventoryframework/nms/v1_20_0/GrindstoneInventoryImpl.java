package com.github.stefvanschie.inventoryframework.nms.v1_20_0;

import com.github.stefvanschie.inventoryframework.abstraction.GrindstoneInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_20_0.util.CustomInventoryUtil;
import com.github.stefvanschie.inventoryframework.nms.v1_20_0.util.TextHolderUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal grindstone inventory for 1.20.0
 *
 * @since 0.10.14
 */
public class GrindstoneInventoryImpl extends GrindstoneInventory {

    public GrindstoneInventoryImpl(@NotNull InventoryHolder inventoryHolder) {
        super(inventoryHolder);
    }

    @Override
    public Inventory openInventory(@NotNull Player player, @NotNull TextHolder title,
                              @Nullable org.bukkit.inventory.ItemStack[] items) {
        int itemAmount = items.length;

        if (itemAmount != 3) {
            throw new IllegalArgumentException(
                "The amount of items for a grindstone should be 3, but is '" + itemAmount + "'"
            );
        }

        ServerPlayer serverPlayer = getServerPlayer(player);

        //ignore deprecation: superseding method is only available on Paper
        //noinspection deprecation
        CraftEventFactory.handleInventoryCloseEvent(serverPlayer);

        serverPlayer.containerMenu = serverPlayer.inventoryMenu;

        Component message = TextHolderUtil.toComponent(title);
        ContainerGrindstoneImpl containerGrindstone = new ContainerGrindstoneImpl(serverPlayer, message);

        Inventory inventory = containerGrindstone.getBukkitView().getTopInventory();

        inventory.setItem(0, items[0]);
        inventory.setItem(1, items[1]);
        inventory.setItem(2, items[2]);

        int containerId = containerGrindstone.getContainerId();

        serverPlayer.connection.send(new ClientboundOpenScreenPacket(containerId, MenuType.GRINDSTONE, message));
        serverPlayer.containerMenu = containerGrindstone;
        serverPlayer.initMenu(containerGrindstone);

        return inventory;
    }

    @Override
    public void sendItems(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack[] items,
                          @Nullable org.bukkit.inventory.ItemStack cursor) {
        if (cursor == null) {
            throw new IllegalArgumentException("Cursor may not be null on version 1.19.2");
        }

        NonNullList<ItemStack> nmsItems = CustomInventoryUtil.convertToNMSItems(items);
        ServerPlayer serverPlayer = getServerPlayer(player);
        int containerId = getContainerId(serverPlayer);
        int state = serverPlayer.containerMenu.incrementStateId();
        ItemStack nmsCursor = CraftItemStack.asNMSCopy(cursor);
        ServerPlayerConnection playerConnection = getPlayerConnection(serverPlayer);

        playerConnection.send(new ClientboundContainerSetContentPacket(containerId, state, nmsItems, nmsCursor));
    }

    @Override
    public void clearCursor(@NotNull Player player) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        int state = serverPlayer.containerMenu.incrementStateId();

        getPlayerConnection(serverPlayer).send(new ClientboundContainerSetSlotPacket(-1, state, -1, ItemStack.EMPTY));
    }

    /**
     * Gets the containerId id for the inventory view the player currently has open
     *
     * @param nmsPlayer the player to get the containerId id for
     * @return the containerId id
     * @since 0.10.14
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
     * @since 0.10.14
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
     * @since 0.10.14
     */
    @NotNull
    @Contract(pure = true)
    private ServerPlayer getServerPlayer(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    /**
     * A custom container grindstone
     *
     * @since 0.10.14
     */
    private static class ContainerGrindstoneImpl extends GrindstoneMenu {

        /**
         * Creates a new grindstone container
         *
         * @param serverPlayer the player for whom this container should be opened
         * @param title the title of the gui
         * @since 0.10.14
         */
        public ContainerGrindstoneImpl(@NotNull ServerPlayer serverPlayer, @NotNull Component title) {
            super(serverPlayer.nextContainerCounter(), serverPlayer.getInventory());

            setTitle(title);

            Slot firstSlot = this.slots.get(0);
            Slot secondSlot = this.slots.get(1);
            Slot thirdSlot = this.slots.get(2);

            this.slots.set(0, new Slot(firstSlot.container, firstSlot.index, firstSlot.x, firstSlot.y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return true;
                }
            });

            this.slots.set(1, new Slot(secondSlot.container, secondSlot.index, secondSlot.x, secondSlot.y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return true;
                }
            });

            this.slots.set(2, new Slot(thirdSlot.container, thirdSlot.index, thirdSlot.x, thirdSlot.y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return true;
                }

                @Override
                public void onTake(net.minecraft.world.entity.player.Player player, ItemStack stack) {}
            });
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

        public int getContainerId() {
            return this.containerId;
        }
    }
}
