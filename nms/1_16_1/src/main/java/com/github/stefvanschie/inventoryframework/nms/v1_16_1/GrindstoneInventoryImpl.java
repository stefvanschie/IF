package com.github.stefvanschie.inventoryframework.nms.v1_16_1;

import com.github.stefvanschie.inventoryframework.abstraction.GrindstoneInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_16_1.util.TextHolderUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventoryGrindstone;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Internal grindstone inventory for 1.16 R1
 *
 * @since 0.8.0
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

        EntityPlayer entityPlayer = getEntityPlayer(player);
        ContainerGrindstoneImpl containerGrindstone = new ContainerGrindstoneImpl(entityPlayer, items);

        entityPlayer.activeContainer = containerGrindstone;

        int id = containerGrindstone.windowId;
        IChatBaseComponent message = TextHolderUtil.toComponent(title);
        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(id, Containers.GRINDSTONE, message);

        entityPlayer.playerConnection.sendPacket(packet);

        sendItems(player, items, null);
    }

    @Override
    public void sendItems(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack[] items,
                          @Nullable org.bukkit.inventory.ItemStack cursor) {
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
     * A custom container grindstone
     *
     * @since 0.8.0
     */
    private class ContainerGrindstoneImpl extends ContainerGrindstone {

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
        private final Field craftInventoryField;

        /**
         * Field for accessing the result inventory field
         */
        @NotNull
        private final Field resultInventoryField;

        public ContainerGrindstoneImpl(@NotNull EntityPlayer entityPlayer,
                                       @Nullable org.bukkit.inventory.ItemStack[] items) {
            super(entityPlayer.nextContainerCounter(), entityPlayer.inventory);

            this.player = entityPlayer.getBukkitEntity();

            try {
                this.craftInventoryField = ContainerGrindstone.class.getDeclaredField("craftInventory");
                this.craftInventoryField.setAccessible(true);

                this.resultInventoryField = ContainerGrindstone.class.getDeclaredField("resultInventory");
                this.resultInventoryField.setAccessible(true);
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
        public boolean canUse(@Nullable EntityHuman entityhuman) {
            return true;
        }

        @Override
        public void a(IInventory inventory) {}

        @Override
        public void b(EntityHuman entityhuman) {}

        /**
         * Gets the craft inventory
         *
         * @return the craft inventory
         * @since 0.8.0
         */
        @NotNull
        @Contract(pure = true)
        private IInventory getCraftInventory() {
            try {
                return (IInventory) craftInventoryField.get(this);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }

        /**
         * Gets the result inventory
         *
         * @return the result inventory
         * @since 0.8.0
         */
        @NotNull
        @Contract(pure = true)
        private IInventory getResultInventory() {
            try {
                return (IInventory) resultInventoryField.get(this);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
