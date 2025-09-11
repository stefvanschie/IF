package com.github.stefvanschie.inventoryframework.nms.v1_16_4_5;

import com.github.stefvanschie.inventoryframework.abstraction.EnchantingTableInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_16_4_5.util.TextHolderUtil;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryEnchanting;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal enchanting table inventory for 1.16 R3
 *
 * @since 0.8.0
 */
public class EnchantingTableInventoryImpl extends EnchantingTableInventory {

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory(@NotNull TextHolder title) {
        IInventory container = new InventoryViewProvider() {
            @NotNull
            @Contract(pure = true)
            @Override
            public Container createMenu(
                    int containerId,
                    @Nullable PlayerInventory inventory,
                    @NotNull EntityHuman player
            ) {
                return new ContainerEnchantingTableImpl(containerId, player, this);
            }

            @NotNull
            @Contract(pure = true)
            @Override
            public IChatBaseComponent getScoreboardDisplayName() {
                return TextHolderUtil.toComponent(title);
            }
        };

        return new CraftInventoryEnchanting(container) {
            @NotNull
            @Contract(pure = true)
            @Override
            public InventoryType getType() {
                return InventoryType.ENCHANTING;
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
         * Creates a new inventory view provider with two slots.
         *
         * @since 0.11.0
         */
        public InventoryViewProvider() {
            super(2);
        }
    }

    /**
     * A custom container enchanting table
     *
     * @since 0.8.0
     */
    private static class ContainerEnchantingTableImpl extends ContainerEnchantTable {

        /**
         * The human entity viewing this menu.
         */
        @NotNull
        private final HumanEntity humanEntity;

        /**
         * The container for the input slots.
         */
        @NotNull
        private final InventorySubcontainer inputSlots;

        /**
         * The corresponding Bukkit view. Will be not null after the first call to {@link #getBukkitView()} and null
         * prior.
         */
        @Nullable
        private CraftInventoryView bukkitEntity;

        /**
         * Creates a new custom enchanting table container for the specified player.
         *
         * @param containerId the container id
         * @param player the player
         * @param inputSlots the input slots
         * @since 0.11.0
         */
        public ContainerEnchantingTableImpl(
                int containerId,
                @NotNull EntityHuman player,
                @NotNull InventorySubcontainer inputSlots
        ) {
            super(containerId, player.inventory, ContainerAccess.at(player.getWorld(), BlockPosition.ZERO));

            this.humanEntity = player.getBukkitEntity();
            this.inputSlots = inputSlots;

            super.checkReachable = false;

            updateSlot(0, inputSlots);
            updateSlot(1, inputSlots);
        }

        @NotNull
        @Override
        public CraftInventoryView getBukkitView() {
            if (this.bukkitEntity != null) {
                return this.bukkitEntity;
            }

            CraftInventoryEnchanting inventory = new CraftInventoryEnchanting(this.inputSlots);

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
