package com.github.stefvanschie.inventoryframework.nms.v1_16_4_5;

import com.github.stefvanschie.inventoryframework.abstraction.GrindstoneInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_16_4_5.util.TextHolderUtil;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Internal grindstone inventory for 1.16 R3
 *
 * @since 0.8.0
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

        EntityPlayer entityPlayer = getEntityPlayer(player);

        //ignore deprecation: superseding method is only available on Paper
        //noinspection deprecation
        CraftEventFactory.handleInventoryCloseEvent(entityPlayer);

        entityPlayer.activeContainer = entityPlayer.defaultContainer;

        IChatBaseComponent message = TextHolderUtil.toComponent(title);
        ContainerGrindstoneImpl containerGrindstone = new ContainerGrindstoneImpl(entityPlayer);

        Inventory inventory = containerGrindstone.getBukkitView().getTopInventory();

        inventory.setItem(0, items[0]);
        inventory.setItem(1, items[1]);
        inventory.setItem(2, items[2]);

        int windowId = containerGrindstone.getWindowId();

        entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, Containers.GRINDSTONE, message));
        entityPlayer.activeContainer = containerGrindstone;
        entityPlayer.syncInventory();

        return inventory;
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
    private static class ContainerGrindstoneImpl extends ContainerGrindstone {

        /**
         * A unique item
         */
        @NotNull
        private final ItemStack uniqueItem;

        /**
         * The field containing the listeners for this container
         */
        @NotNull
        private final Field listenersField;

        /**
         * Creates a new grindstone container
         *
         * @param entityPlayer the player for whom this container should be opened
         * @since 0.10.8
         */
        public ContainerGrindstoneImpl(@NotNull EntityPlayer entityPlayer) {
            super(entityPlayer.nextContainerCounter(), entityPlayer.inventory);

            try {
                this.listenersField = Container.class.getDeclaredField("listeners");
                this.listenersField.setAccessible(true);
            } catch (NoSuchFieldException exception) {
                throw new RuntimeException("Unable to access field 'listeners'", exception);
            }

            Slot firstSlot = this.slots.get(0);
            Slot secondSlot = this.slots.get(1);
            Slot thirdSlot = this.slots.get(2);

            this.slots.set(0, new Slot(firstSlot.inventory, firstSlot.rawSlotIndex, firstSlot.e, firstSlot.f) {
                @Override
                public boolean isAllowed(ItemStack stack) {
                    return true;
                }
            });

            this.slots.set(1, new Slot(secondSlot.inventory, secondSlot.rawSlotIndex, secondSlot.e, secondSlot.f) {
                @Override
                public boolean isAllowed(ItemStack stack) {
                    return true;
                }
            });

            this.slots.set(2, new Slot(thirdSlot.inventory, thirdSlot.rawSlotIndex, thirdSlot.e, thirdSlot.f) {
                @Override
                public boolean isAllowed(ItemStack stack) {
                    return true;
                }

                @Override
                public ItemStack a(EntityHuman entityHuman, ItemStack itemStack) {
                    return itemStack;
                }
            });

            this.uniqueItem = new ItemStack(Items.COOKIE);

            //to make the item unique, we add a random uuid as nbt to it
            UUID uuid = UUID.randomUUID();
            NBTTagCompound nbtTagCompound = new NBTTagCompound();

            nbtTagCompound.set("uuid", new NBTTagLongArray(new long [] {
                    uuid.getLeastSignificantBits(),
                    uuid.getMostSignificantBits()
            }));

            this.uniqueItem.setTag(nbtTagCompound);
        }

        /**
         * Forcefully updates the client state, sending all items, container properties and the held item.
         *
         * @since 0.10.8
         */
        public void forceUpdate() {
            /*
            The server will not send the items when they haven't changed, so we will overwrite every item with a unique
            item first to ensure the server is going to send our items.
             */
            Collections.fill(this.items, this.uniqueItem);

            notifyListeners();

            List<? extends ICrafting> listeners;

            try {
                //noinspection unchecked
                listeners = (List<? extends ICrafting>) listenersField.get(this);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException("Unable to access field 'listeners'", exception);
            }

            for (ICrafting listener : listeners) {
                if (!(listener instanceof EntityPlayer)) {
                    continue;
                }

                EntityPlayer player = (EntityPlayer) listener;

                player.e = false;
                player.broadcastCarriedItem();
            }
        }

        @Override
        public ItemStack a(int i, int j, InventoryClickType inventoryclicktype, EntityHuman entityhuman) {
            ItemStack itemStack = super.a(i, j, inventoryclicktype, entityhuman);

            //the client predicts the allowed movement of the item, so we broadcast the state again to override it
            forceUpdate();

            return itemStack;
        }

        @Contract(pure = true, value = "_ -> true")
        @Override
        public boolean canUse(@Nullable EntityHuman entityHuman) {
            return true;
        }

        @Override
        public void a(IInventory inventory) {}

        @Override
        public void b(EntityHuman entityHuman) {}

        public int getWindowId() {
            return this.windowId;
        }
    }
}
