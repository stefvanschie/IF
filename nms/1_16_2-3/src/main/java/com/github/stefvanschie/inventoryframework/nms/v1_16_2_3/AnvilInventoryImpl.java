package com.github.stefvanschie.inventoryframework.nms.v1_16_2_3;

import com.github.stefvanschie.inventoryframework.abstraction.AnvilInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_16_2_3.util.TextHolderUtil;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
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
 * Internal anvil inventory for 1.16 R2
 *
 * @since 0.8.0
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

        EntityPlayer entityPlayer = getEntityPlayer(player);

        //ignore deprecation: superseding method is only available on Paper
        //noinspection deprecation
        CraftEventFactory.handleInventoryCloseEvent(entityPlayer);

        entityPlayer.activeContainer = entityPlayer.defaultContainer;

        IChatBaseComponent message = TextHolderUtil.toComponent(title);
        ContainerAnvilImpl containerAnvil = new ContainerAnvilImpl(entityPlayer, message);

        Inventory inventory = containerAnvil.getBukkitView().getTopInventory();

        inventory.setItem(0, items[0]);
        inventory.setItem(1, items[1]);
        inventory.setItem(2, items[2]);

        int containerId = containerAnvil.getContainerId();

        entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, Containers.ANVIL, message));
        entityPlayer.activeContainer = containerAnvil;
        entityPlayer.syncInventory();

        return inventory;
    }

    @Override
    public void sendItems(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack[] items) {
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
    public void sendFirstItem(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        EntityPlayer entityPlayer = getEntityPlayer(player);
        ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        getPlayerConnection(entityPlayer).sendPacket(new PacketPlayOutSetSlot(getWindowId(entityPlayer), 0, nmsItem));
    }

    @Override
    public void sendSecondItem(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        EntityPlayer entityPlayer = getEntityPlayer(player);
        ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        getPlayerConnection(entityPlayer).sendPacket(new PacketPlayOutSetSlot(getWindowId(entityPlayer), 1, nmsItem));
    }

    @Override
    public void sendResultItem(@NotNull Player player, @Nullable org.bukkit.inventory.ItemStack item) {
        sendResultItem(player, CraftItemStack.asNMSCopy(item));
    }

    @Override
    public void clearResultItem(@NotNull Player player) {
        sendResultItem(player, ItemStack.b);
    }

    @Override
    public void setCursor(@NotNull Player player, @NotNull org.bukkit.inventory.ItemStack item) {
        setCursor(player, CraftItemStack.asNMSCopy(item));
    }

    @Override
    public void clearCursor(@NotNull Player player) {
        getPlayerConnection(getEntityPlayer(player)).sendPacket(new PacketPlayOutSetSlot(-1, -1, ItemStack.b));
    }

    /**
     * Sets the cursor of the given player
     *
     * @param player the player to set the cursor
     * @param item the item to set the cursor to
     * @since 0.8.0
     * @deprecated no longer used internally
     */
    @Deprecated
    private void setCursor(@NotNull Player player, @NotNull ItemStack item) {
        getPlayerConnection(getEntityPlayer(player)).sendPacket(new PacketPlayOutSetSlot(-1, -1, item));
    }

    /**
     * Sends the result item to the specified player with the given item
     *
     * @param player the player to send the result item to
     * @param item the result item
     * @since 0.8.0
     * @deprecated no longer used internally
     */
    @Deprecated
    private void sendResultItem(@NotNull Player player, @NotNull ItemStack item) {
        EntityPlayer entityPlayer = getEntityPlayer(player);

        getPlayerConnection(entityPlayer).sendPacket(new PacketPlayOutSetSlot(getWindowId(entityPlayer), 2, item));
    }

    /**
     * Gets the window id for the inventory view the player currently has open
     *
     * @param entityPlayer the player to get the window id for
     * @return the window id
     * @since 0.8.0
     * @deprecated no longer used internally
     */
    @Contract(pure = true)
    @Deprecated
    private int getWindowId(@NotNull EntityPlayer entityPlayer) {
        return entityPlayer.activeContainer.windowId;
    }

    /**
     * Gets the player connection for the specified player
     *
     * @param entityPlayer the player to get the player connection from
     * @return the player connection
     * @since 0.8.0
     * @deprecated no longer used internally
     */
    @NotNull
    @Contract(pure = true)
    @Deprecated
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
     * A custom container anvil for responding to item renaming
     *
     * @since 0.8.0
     */
    private class ContainerAnvilImpl extends ContainerAnvil {

        /**
         * The index of the result slot
         */
        private static final int RESULT_SLOT_INDEX = 2;

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
         * Creates a new custom anvil container for the specified player
         *
         * @param entityPlayer the player for whom this anvil container is
         * @param title the title of the inventory
         * @since 0.10.8
         */
        public ContainerAnvilImpl(@NotNull EntityPlayer entityPlayer, @NotNull IChatBaseComponent title) {
            super(entityPlayer.nextContainerCounter(), entityPlayer.inventory,
                    ContainerAccess.at(entityPlayer.getWorld(), new BlockPosition(0, 0, 0)));

            this.checkReachable = false;

            try {
                //stores all the registered container properties
                Field dField = Container.class.getDeclaredField("d");
                dField.setAccessible(true);

                //get rid of the level cost property
                ((List<?>) dField.get(this)).clear();
            } catch (NoSuchFieldException | IllegalAccessException exception) {
                throw new RuntimeException("Unable to access field 'd'", exception);
            }

            try {
                this.listenersField = Container.class.getDeclaredField("listeners");
                this.listenersField.setAccessible(true);
            } catch (NoSuchFieldException exception) {
                throw new RuntimeException("Unable to access field 'listeners'", exception);
            }

            //register a new property for the level cost
            ContainerProperty levelCost = a(new ContainerProperty() {
                private int value;

                @Override
                public int get() {
                    return value;
                }

                @Override
                public void set(int value) {
                    this.value = value;
                }

                /*
                This checks whether there have been any changes, but we want to override the client prediction. This
                means the server should be sending the data to the client, even if it didn't change server-side. To
                force this, we tell the server the data has always changed.
                 */
                @Override
                public boolean c() {
                    return true;
                }
            });

            levelCost.set(AnvilInventoryImpl.super.cost);

            setTitle(title);

            Slot originalSlot = this.slots.get(RESULT_SLOT_INDEX);

            Slot newSlot = new Slot(originalSlot.inventory, originalSlot.index, originalSlot.e, originalSlot.f) {
                @Override
                public boolean isAllowed(ItemStack itemStack) {
                    return true;
                }

                @Override
                public boolean isAllowed(EntityHuman entityHuman) {
                    return true;
                }

                @Override
                public ItemStack a(EntityHuman entityHuman, @NotNull ItemStack itemStack) {
                    return originalSlot.a(entityHuman, itemStack);
                }
            };

            this.slots.set(RESULT_SLOT_INDEX, newSlot);

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

        @Override
        public void a(@Nullable String name) {
            name = name == null ? "" : name;

            /* Only update if the name is actually different. This may be called even if the name is not different,
               particularly when putting an item in the first slot. */
            if (!name.equals(AnvilInventoryImpl.super.observableText.get())) {
                AnvilInventoryImpl.super.observableText.set(name);
            }

            //the client predicts the output result, so we broadcast the state again to override it
            forceUpdate();
        }

        @Override
        public void e() {}

        @Override
        public void b(EntityHuman entityHuman) {}

        @Override
        protected void a(EntityHuman entityHuman, World world, @NotNull IInventory inventory) {}

        @Override
        protected ItemStack a(EntityHuman entityHuman, @NotNull ItemStack itemStack) {
            return itemStack;
        }

        @Override
        public ItemStack a(int i, int j, InventoryClickType inventoryclicktype, EntityHuman entityhuman) {
            ItemStack itemStack = super.a(i, j, inventoryclicktype, entityhuman);

            //the client predicts the allowed movement of the item, so we broadcast the state again to override it
            forceUpdate();

            return itemStack;
        }

        public int getContainerId() {
            return this.windowId;
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

            c();

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
    }
}
