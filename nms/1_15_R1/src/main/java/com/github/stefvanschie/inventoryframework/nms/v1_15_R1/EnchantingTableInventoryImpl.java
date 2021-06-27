package com.github.stefvanschie.inventoryframework.nms.v1_15_R1;

import com.github.stefvanschie.inventoryframework.abstraction.EnchantingTableInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_15_R1.util.TextHolderUtil;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftInventoryEnchanting;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Internal enchanting table inventory for 1.15 R1
 *
 * @since 0.8.0
 */
public class EnchantingTableInventoryImpl extends EnchantingTableInventory {

    public EnchantingTableInventoryImpl(@NotNull InventoryHolder inventoryHolder) {
        super(inventoryHolder);
    }

    @Override
    public void openInventory(@NotNull Player player, @NotNull TextHolder title,
                              @Nullable org.bukkit.inventory.ItemStack[] items) {
        int itemAmount = items.length;

        if (itemAmount != 2) {
            throw new IllegalArgumentException(
                "The amount of items for an enchanting table should be 2, but is '" + itemAmount + "'"
            );
        }

        EntityPlayer entityPlayer = getEntityPlayer(player);
        ContainerEnchantingTableImpl containerEnchantmentTable = new ContainerEnchantingTableImpl(entityPlayer, items);

        entityPlayer.activeContainer = containerEnchantmentTable;

        int id = containerEnchantmentTable.windowId;
        IChatBaseComponent message = TextHolderUtil.toComponent(title);
        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(id, Containers.ENCHANTMENT, message);

        entityPlayer.playerConnection.sendPacket(packet);

        sendItems(player, items);
    }

    @Override
    public void sendItems(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack[] items) {
        NonNullList<ItemStack> nmsItems = NonNullList.a(
            ItemStack.a,
            CraftItemStack.asNMSCopy(items[0]),
            CraftItemStack.asNMSCopy(items[1])
        );

        EntityPlayer entityPlayer = getEntityPlayer(player);

        getPlayerConnection(entityPlayer).sendPacket(new PacketPlayOutWindowItems(getWindowId(entityPlayer), nmsItems));
    }

    @Override
    public void clearCursor(@NotNull Player player) {
        getPlayerConnection(getEntityPlayer(player)).sendPacket(new PacketPlayOutSetSlot(-1, -1, ItemStack.a));
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
     * A custom container enchanting table
     *
     * @since 0.8.0
     */
    private class ContainerEnchantingTableImpl extends ContainerEnchantTable {

        /**
         * The player for this enchanting table container
         */
        @NotNull
        private final Player player;

        /**
         * The internal bukkit entity for this container enchanting table
         */
        @Nullable
        private CraftInventoryView bukkitEntity;

        /**
         * Field for accessing the enchant slots field
         */
        @NotNull
        private final Field enchantSlotsField;

        public ContainerEnchantingTableImpl(@NotNull EntityPlayer entityPlayer,
                                            @Nullable org.bukkit.inventory.ItemStack[] items) {
            super(entityPlayer.nextContainerCounter(), entityPlayer.inventory);

            this.player = entityPlayer.getBukkitEntity();

            try {
                this.enchantSlotsField = ContainerEnchantTable.class.getDeclaredField("enchantSlots");
                this.enchantSlotsField.setAccessible(true);
            } catch (NoSuchFieldException exception) {
                throw new RuntimeException(exception);
            }

            try {
                IInventory input = (IInventory) enchantSlotsField.get(this);

                input.setItem(0, CraftItemStack.asNMSCopy(items[0]));
                input.setItem(1, CraftItemStack.asNMSCopy(items[1]));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        @NotNull
        @Override
        public CraftInventoryView getBukkitView() {
            if (bukkitEntity == null) {
                try {
                    CraftInventory inventory = new CraftInventoryEnchanting((IInventory) enchantSlotsField.get(this)) {
                        @NotNull
                        @Contract(pure = true)
                        @Override
                        public InventoryHolder getHolder() {
                            return inventoryHolder;
                        }
                    };

                    bukkitEntity = new CraftInventoryView(player, inventory, this);
                } catch (IllegalAccessException exception) {
                    exception.printStackTrace();
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
