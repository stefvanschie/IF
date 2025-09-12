package com.github.stefvanschie.inventoryframework.nms.v1_16_2_3;

import com.github.stefvanschie.inventoryframework.abstraction.BeaconInventory;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_16_R2.inventory.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal beacon inventory for 1.16 R2
 *
 * @since 0.8.0
 */
public class BeaconInventoryImpl extends BeaconInventory {

    @NotNull
    @Override
    public Inventory createInventory() {
        IInventory container = new InventoryViewProvider() {
            @Override
            protected Container createContainer(int containerId, PlayerInventory playerInventory) {
                return new ContainerBeaconImpl(containerId, playerInventory.player, this);
            }

            @NotNull
            @Contract(pure = true)
            @Override
            protected IChatBaseComponent getContainerName() {
                return new ChatComponentText("Beacon");
            }
        };

        return new CraftInventoryBeacon(container) {
            @NotNull
            @Contract(pure = true)
            @Override
            public InventoryType getType() {
                return InventoryType.BEACON;
            }

            @Override
            public IInventory getInventory() {
                return container;
            }
        };
    }

    /**
     * This is a nice hack to get CraftBukkit to create custom inventories. By providing a container that is also a menu
     * provider, CraftBukkit will allow us to create a custom menu, rather than picking one of the built-in options.
     * That way, we can provide a menu with custom behaviour.
     *
     * @since 0.11.0
     */
    private abstract static class InventoryViewProvider extends TileEntityContainer {

        /**
         * The size of the container.
         */
        private final int size = 1;

        /**
         * The item in the container.
         */
        public ItemStack item = ItemStack.b;

        /**
         * The human entities viewing this container.
         */
        public List<HumanEntity> transaction = new ArrayList<>();

        /**
         * The maximum stack size.
         */
        private int maxStack = 1;

        protected InventoryViewProvider() {
            super(TileEntityTypes.BEACON);
        }

        @Override
        public void clear() {
            this.item = ItemStack.b;

            update();
        }

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return this.item.isEmpty();
        }

        @Override
        public ItemStack getItem(int index) {
            return index == 0 ? this.item : ItemStack.b;
        }

        @Override
        public ItemStack splitStack(int firstIndex, int secondIndex) {
            ItemStack itemstack = firstIndex == 0 && !this.item.isEmpty() && secondIndex > 0 ?
                    this.item.cloneAndSubtract(secondIndex) : ItemStack.b;

            if (!itemstack.isEmpty()) {
                this.update();
            }

            return itemstack;
        }

        @Override
        public ItemStack splitWithoutUpdate(int index) {
            ItemStack itemstack = getItem(index);

            if (itemstack.isEmpty()) {
                return ItemStack.b;
            }

            this.item = ItemStack.b;

            return itemstack;
        }

        @Override
        public void setItem(int index, ItemStack itemStack) {
           this.item = itemStack;

            if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
                itemStack.setCount(this.getMaxStackSize());
            }

            this.update();
        }

        @Override
        public int getMaxStackSize() {
            return this.maxStack;
        }

        @Override
        public boolean a(EntityHuman entityHuman) {
            return true;
        }

        @Override
        public List<ItemStack> getContents() {
            return NonNullList.a(this.item);
        }

        @Override
        public void onOpen(CraftHumanEntity craftHumanEntity) {
            this.transaction.add(craftHumanEntity);
        }

        @Override
        public void onClose(CraftHumanEntity craftHumanEntity) {
            this.transaction.remove(craftHumanEntity);
        }

        @Override
        public List<HumanEntity> getViewers() {
            return this.transaction;
        }

        @Override
        public void setMaxStackSize(int maxStack) {
            this.maxStack = maxStack;
        }
    }

    /**
     * A custom container beacon
     *
     * @since 0.8.0
     */
    private static class ContainerBeaconImpl extends ContainerBeacon {

        /**
         * The player viewing this menu.
         */
        @NotNull
        private final EntityHuman player;

        /**
         * The container for the input slots.
         */
        @NotNull
        private final IInventory inputSlot;

        /**
         * The corresponding Bukkit view. Will be not null after the first call to {@link #getBukkitView()} and null
         * prior.
         */
        @Nullable
        private CraftInventoryView bukkitEntity;

        /**
         * Creates a new custom beacon container for the specified player.
         *
         * @param containerId the container id
         * @param player the player
         * @param inputSlot the input slot
         * @since 0.11.0
         */
        public ContainerBeaconImpl(
                int containerId,
                @NotNull EntityHuman player,
                @NotNull IInventory inputSlot
        ) {
            super(containerId, player.inventory, new ContainerProperties(3),
                    ContainerAccess.at(player.world, BlockPosition.ZERO));

            this.player = player;
            this.inputSlot = inputSlot;

            super.checkReachable = false;

            Slot slot = super.slots.get(0);

            Slot newSlot = new Slot(inputSlot, slot.index, slot.e, slot.f);
            newSlot.rawSlotIndex = slot.rawSlotIndex;

            super.slots.set(0, newSlot);
        }

        @NotNull
        @Override
        public CraftInventoryView getBukkitView() {
            if (this.bukkitEntity != null) {
                return this.bukkitEntity;
            }

            CraftInventoryBeacon inventory = new CraftInventoryBeacon(this.inputSlot);

            this.bukkitEntity = new CraftInventoryView(this.player.getBukkitEntity(), inventory, this);

            return this.bukkitEntity;
        }

        @Override
        public void b(@Nullable EntityHuman player) {}

    }
}
