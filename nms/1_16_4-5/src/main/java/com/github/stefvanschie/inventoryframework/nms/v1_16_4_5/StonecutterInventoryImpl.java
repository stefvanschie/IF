package com.github.stefvanschie.inventoryframework.nms.v1_16_4_5;

import com.github.stefvanschie.inventoryframework.abstraction.StonecutterInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_16_4_5.util.TextHolderUtil;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryStonecutter;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal stonecutter inventory for 1.16 R3
 *
 * @since 0.8.0
 */
public class StonecutterInventoryImpl extends StonecutterInventory {

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory(@NotNull TextHolder title) {
        InventorySubcontainer resultSlot = new InventorySubcontainer(1);

        IInventory container = new InventoryViewProvider() {
            @NotNull
            @Contract(pure = true)
            @Override
            public Container createMenu(
                    int containerId,
                    @Nullable PlayerInventory inventory,
                    @NotNull EntityHuman player
            ) {
                return new ContainerStonecutterImpl(containerId, player, this, resultSlot);
            }

            @NotNull
            @Contract(pure = true)
            @Override
            public IChatBaseComponent getScoreboardDisplayName() {
                return TextHolderUtil.toComponent(title);
            }
        };

        return new CraftInventoryStonecutter(container, resultSlot) {
            @NotNull
            @Contract(pure = true)
            @Override
            public InventoryType getType() {
                return InventoryType.STONECUTTER;
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
    private abstract static class InventoryViewProvider extends InventorySubcontainer implements ITileInventory {

        /**
         * Creates a new inventory view provider with three slots.
         *
         * @since 0.11.0
         */
        public InventoryViewProvider() {
            super(1);
        }
    }

    /**
     * A custom container enchanting table
     *
     * @since 0.8.0
     */
    private static class ContainerStonecutterImpl extends ContainerStonecutter {

        /**
         * The human entity viewing this menu.
         */
        @NotNull
        private final HumanEntity humanEntity;

        /**
         * The container for the items slots.
         */
        @NotNull
        private final InventorySubcontainer inputSlot;

        /**
         * The container for the result slot.
         */
        @NotNull
        private final InventorySubcontainer resultSlot;

        /**
         * The corresponding Bukkit view. Will be not null after the first call to {@link #getBukkitView()} and null
         * prior.
         */
        @Nullable
        private CraftInventoryView bukkitEntity;

        /**
         * Creates a new custom stonecutter container for the specified player
         *
         * @param containerId the container id
         * @param player the player
         * @param inputSlot the input slot
         * @param resultSlot the result slot
         * @since 0.11.0
         */
        public ContainerStonecutterImpl(
                int containerId,
                @NotNull EntityHuman player,
                @NotNull InventorySubcontainer inputSlot,
                @NotNull InventorySubcontainer resultSlot
        ) {
            super(containerId, player.inventory, ContainerAccess.at(player.getWorld(), BlockPosition.ZERO));

            this.humanEntity = player.getBukkitEntity();
            this.inputSlot = inputSlot;
            this.resultSlot = resultSlot;

            super.checkReachable = false;

            InventoryLargeChest container = new InventoryLargeChest(inputSlot, resultSlot);

            updateSlot(0, container);
            updateSlot(1, container);
        }

        @NotNull
        @Override
        public CraftInventoryView getBukkitView() {
            if (this.bukkitEntity != null) {
                return this.bukkitEntity;
            }

            CraftInventoryStonecutter inventory = new CraftInventoryStonecutter(this.inputSlot, this.resultSlot);

            this.bukkitEntity = new CraftInventoryView(this.humanEntity, inventory, this);

            return this.bukkitEntity;
        }

        @Contract(pure = true, value = "_ -> true")
        @Override
        public boolean canUse(@Nullable EntityHuman nmsPlayer) {
            return true;
        }

        @Override
        public void a(IInventory container) {}

        @Override
        public void b(EntityHuman nmsPlayer) {}

        @Contract(value = "_, _ -> false", pure = true)
        @Override
        public boolean a(@Nullable EntityHuman player, int index) {
            return false;
        }

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
