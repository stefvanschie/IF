package com.github.stefvanschie.inventoryframework.nms.v1_18_0;

import com.github.stefvanschie.inventoryframework.abstraction.EnchantingTableInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_18_0.util.TextHolderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftInventoryEnchanting;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal enchanting table inventory for 1.18.0
 *
 * @since 0.10.4
 */
public class EnchantingTableInventoryImpl extends EnchantingTableInventory {

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory(@NotNull TextHolder title) {
        Container container = new InventoryViewProvider() {
            @NotNull
            @Contract(pure = true)
            @Override
            public AbstractContainerMenu createMenu(
                    int containerId,
                    @Nullable net.minecraft.world.entity.player.Inventory inventory,
                    @NotNull Player player
            ) {
                return new ContainerEnchantingTableImpl(containerId, player, this);
            }

            @NotNull
            @Contract(pure = true)
            @Override
            public Component getDisplayName() {
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
            public Container getInventory() {
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
    private abstract static class InventoryViewProvider extends SimpleContainer implements MenuProvider {

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
     * @since 0.10.4
     */
    private static class ContainerEnchantingTableImpl extends EnchantmentMenu {

        /**
         * The human entity viewing this menu.
         */
        @NotNull
        private final HumanEntity humanEntity;

        /**
         * The container for the input slots.
         */
        @NotNull
        private final SimpleContainer inputSlots;

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
                @NotNull Player player,
                @NotNull SimpleContainer inputSlots
        ) {
            super(containerId, player.getInventory(), ContainerLevelAccess.create(player.level, BlockPos.ZERO));

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
        public void slotsChanged(@Nullable Container container) {}

        @Override
        public void removed(@Nullable Player player) {}

        @Override
        protected void clearContainer(@Nullable Player player, @Nullable Container container) {}

        /**
         * Updates the current slot at the specified index to a new slot. The new slot will have the same slot, x, y,
         * and index as the original. The container of the new slot will be set to the value specified.
         *
         * @param slotIndex the slot index to update
         * @param container the container of the new slot
         * @since 0.11.0
         */
        private void updateSlot(int slotIndex, @NotNull Container container) {
            Slot slot = super.slots.get(slotIndex);

            Slot newSlot = new Slot(container, slot.slot, slot.x, slot.y);
            newSlot.index = slot.index;

            super.slots.set(slotIndex, newSlot);
        }
    }
}
