package com.github.stefvanschie.inventoryframework.nms.v1_16_2_3;

import com.github.stefvanschie.inventoryframework.abstraction.CartographyTableInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_16_2_3.util.TextHolderUtil;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftInventoryCartography;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal cartography table inventory for 1.16 R2
 *
 * @since 0.8.0
 */
public class CartographyTableInventoryImpl extends CartographyTableInventory {

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory(@NotNull TextHolder title) {
        InventorySubcontainer resultSlot = new InventorySubcontainer(1);

        IInventory container = new InventoryViewProvider() {
            @NotNull
            @Contract(pure = true)
            @Override
            public Container createContainer(
                    int containerId,
                    @NotNull PlayerInventory inventory
            ) {
                return new ContainerCartographyTableImpl(containerId, inventory.player, this, resultSlot);
            }

            @NotNull
            @Contract(pure = true)
            @Override
            public IChatBaseComponent getContainerName() {
                return TextHolderUtil.toComponent(title);
            }
        };

        return new CraftInventoryCartography(container, resultSlot) {
            @NotNull
            @Contract(pure = true)
            @Override
            public InventoryType getType() {
                return InventoryType.CARTOGRAPHY;
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
         * The item in the container.
         */
        private final NonNullList<ItemStack> items = NonNullList.a(2, ItemStack.b);

        /**
         * The human entities viewing this container.
         */
        private final List<HumanEntity> transaction = new ArrayList<>();

        /**
         * The maximum stack size.
         */
        private int maxStack = 64;

        protected InventoryViewProvider() {
            super(TileEntityTypes.FURNACE); //close enough
        }

        @Override
        public void clear() {
            this.items.clear();

            update();
        }

        @Override
        public int getSize() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack itemStack : this.items) {
                if (!itemStack.isEmpty()) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return index >= 0 && index < this.items.size() ? this.items.get(index) : ItemStack.b;
        }

        @Override
        public ItemStack splitStack(int firstIndex, int secondIndex) {
            ItemStack itemstack = ContainerUtil.a(this.items, firstIndex, secondIndex);

            if (!itemstack.isEmpty()) {
                update();
            }

            return itemstack;
        }

        @Override
        public ItemStack splitWithoutUpdate(int index) {
            ItemStack itemStack = this.items.get(index);

            if (itemStack.isEmpty()) {
                return ItemStack.b;
            }

            this.items.set(index, ItemStack.b);

            return itemStack;
        }

        @Override
        public void setItem(int index, ItemStack itemStack) {
            this.items.set(index, itemStack);

            if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
                itemStack.setCount(this.getMaxStackSize());
            }

            update();
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
            return this.items;
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
     * A custom container cartography table
     *
     * @since 0.8.0
     */
    private static class ContainerCartographyTableImpl extends ContainerCartography {

        /**
         * The human entity viewing this menu.
         */
        @NotNull
        private final HumanEntity humanEntity;

        /**
         * The container for the input slots.
         */
        @NotNull
        private final IInventory inputSlots;

        /**
         * The container for the result slot.
         */
        @NotNull
        private final IInventory resultSlot;

        /**
         * The corresponding Bukkit view. Will be not null after the first call to {@link #getBukkitView()} and null
         * prior.
         */
        @Nullable
        private CraftInventoryView bukkitEntity;

        /**
         * Creates a new custom cartography table container for the specified player.
         *
         * @param containerId the container id
         * @param player the player
         * @param inputSlots the input slots
         * @param resultSlot the result slot
         * @since 0.11.0
         */
        public ContainerCartographyTableImpl(
                int containerId,
                @NotNull EntityHuman player,
                @NotNull IInventory inputSlots,
                @NotNull IInventory resultSlot
        ) {
            super(containerId, player.inventory, ContainerAccess.at(player.getWorld(), BlockPosition.ZERO));

            this.humanEntity = player.getBukkitEntity();
            this.inputSlots = inputSlots;
            this.resultSlot = resultSlot;

            super.checkReachable = false;

            InventoryLargeChest container = new InventoryLargeChest(inputSlots, resultSlot);

            updateSlot(0, container);
            updateSlot(1, container);
            updateSlot(2, container);
        }

        @NotNull
        @Override
        public CraftInventoryView getBukkitView() {
            if (this.bukkitEntity != null) {
                return this.bukkitEntity;
            }

            CraftInventoryCartography inventory = new CraftInventoryCartography(this.inputSlots, this.resultSlot);

            this.bukkitEntity = new CraftInventoryView(this.humanEntity, inventory, this);

            return this.bukkitEntity;
        }

        @Override
        public void a(@Nullable IInventory container) {}

        @Override
        public void b(@Nullable EntityHuman player) {}

        @Override
        protected void a(@Nullable EntityHuman player, @Nullable World world, @Nullable IInventory container) {}

        /**
         * Updates the current slot at the specified index to a new slot. The new slot will have the same slot, x, y,
         * and index as the original. The container of the new slot will be set to the value specified.
         *
         * @param slotIndex the slot index to update
         * @param container the container of the new slot
         * @since 0.11.0
         */
        private void updateSlot(int slotIndex, @NotNull IInventory container) {
            Slot slot = super.slots.get(slotIndex);

            Slot newSlot = new Slot(container, slot.index, slot.e, slot.f);
            newSlot.rawSlotIndex = slot.rawSlotIndex;

            super.slots.set(slotIndex, newSlot);
        }

    }
}
