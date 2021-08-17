package com.github.stefvanschie.inventoryframework.nms.v1_17_1;

import com.github.stefvanschie.inventoryframework.abstraction.GrindstoneInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_17_1.util.CustomInventoryUtil;
import com.github.stefvanschie.inventoryframework.nms.v1_17_1.util.TextHolderUtil;
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
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryGrindstone;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Internal grindstone inventory for 1.17 R1
 *
 * @since 0.10.0
 */
public class GrindstoneInventoryImpl extends GrindstoneInventory {

    public GrindstoneInventoryImpl(@NotNull InventoryHolder inventoryHolder) {
        super(inventoryHolder);
    }

    @Override
    public void openInventory(@NotNull Player player, @NotNull TextHolder title,
                              @Nullable org.bukkit.inventory.ItemStack[] items) {
        int itemAmount = items.length;

        if (itemAmount != 3) {
            throw new IllegalArgumentException(
                "The amount of items for a grindstone should be 3, but is '" + itemAmount + "'"
            );
        }

        ServerPlayer serverPlayer = getServerPlayer(player);
        ContainerGrindstoneImpl containerGrindstone = new ContainerGrindstoneImpl(serverPlayer, items);

        serverPlayer.containerMenu = containerGrindstone;

        int id = containerGrindstone.containerId;
        Component message = TextHolderUtil.toComponent(title);

        serverPlayer.connection.send(new ClientboundOpenScreenPacket(id, MenuType.GRINDSTONE, message));

        sendItems(player, items, player.getItemOnCursor());
    }

    @Override
    public void sendItems(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack[] items,
                          @Nullable org.bukkit.inventory.ItemStack cursor) {
        if (cursor == null) {
            throw new IllegalArgumentException("Cursor may not be null on version 1.17.1");
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
     * @since 0.10.0
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
     * @since 0.10.0
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
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    private ServerPlayer getServerPlayer(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    /**
     * A custom container grindstone
     *
     * @since 0.10.0
     */
    private class ContainerGrindstoneImpl extends GrindstoneMenu {

        /**
         * The player for this grindstone container
         */
        @NotNull
        private final Player player;

        /**
         * The internal bukkit entity for this container grindstone
         */
        @Nullable
        private CraftInventoryView bukkitEntity;

        /**
         * Field for accessing the craft inventory field
         */
        @NotNull
        private final Field repairSlotsField;

        /**
         * Field for accessing the result inventory field
         */
        @NotNull
        private final Field resultSlotsField;

        public ContainerGrindstoneImpl(@NotNull ServerPlayer serverPlayer,
                                       @Nullable org.bukkit.inventory.ItemStack[] items) {
            super(serverPlayer.nextContainerCounter(), serverPlayer.getInventory());

            this.player = serverPlayer.getBukkitEntity();

            try {
                //noinspection JavaReflectionMemberAccess
                this.repairSlotsField = GrindstoneMenu.class.getDeclaredField("t"); //repairSlots
                this.repairSlotsField.setAccessible(true);

                //noinspection JavaReflectionMemberAccess
                this.resultSlotsField = GrindstoneMenu.class.getDeclaredField("s"); //resultSlots
                this.resultSlotsField.setAccessible(true);
            } catch (NoSuchFieldException exception) {
                throw new RuntimeException(exception);
            }

            getCraftInventory().setItem(0, CraftItemStack.asNMSCopy(items[0]));
            getCraftInventory().setItem(1, CraftItemStack.asNMSCopy(items[1]));

            getResultInventory().setItem(2, CraftItemStack.asNMSCopy(items[2]));
        }

        @NotNull
        @Override
        public CraftInventoryView getBukkitView() {
            if (bukkitEntity == null) {
                CraftInventory inventory = new CraftInventoryGrindstone(getCraftInventory(), getResultInventory()) {
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
         * Gets the craft inventory
         *
         * @return the craft inventory
         * @since 0.10.0
         */
        @NotNull
        @Contract(pure = true)
        private Container getCraftInventory() {
            try {
                return (Container) repairSlotsField.get(this);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }

        /**
         * Gets the result inventory
         *
         * @return the result inventory
         * @since 0.10.0
         */
        @NotNull
        @Contract(pure = true)
        private Container getResultInventory() {
            try {
                return (Container) resultSlotsField.get(this);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
