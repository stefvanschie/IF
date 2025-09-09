package com.github.stefvanschie.inventoryframework.nms.v1_21_4;

import com.github.stefvanschie.inventoryframework.abstraction.StonecutterInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_21_4.util.TextHolderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftInventoryStonecutter;
import org.bukkit.craftbukkit.v1_21_R3.inventory.view.CraftStonecutterView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal stonecutter inventory for 1.21.4
 *
 * @since 0.10.19
 */
public class StonecutterInventoryImpl extends StonecutterInventory {

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory(@NotNull TextHolder title) {
        SimpleContainer resultSlot = new SimpleContainer(1);

        Container container = new InventoryViewProvider() {
            @NotNull
            @Contract(pure = true)
            @Override
            public AbstractContainerMenu createMenu(
                    int containerId,
                    @Nullable net.minecraft.world.entity.player.Inventory inventory,
                    @NotNull Player player
            ) {
                return new ContainerStonecutterImpl(containerId, player, this, resultSlot);
            }

            @NotNull
            @Contract(pure = true)
            @Override
            public Component getDisplayName() {
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
     * @since 0.10.19
     */
    private static class ContainerStonecutterImpl extends StonecutterMenu {

        /**
         * The human entity viewing this menu.
         */
        @NotNull
        private final HumanEntity humanEntity;

        /**
         * The container for the items slots.
         */
        @NotNull
        private final SimpleContainer inputSlot;

        /**
         * The container for the result slot.
         */
        @NotNull
        private final SimpleContainer resultSlot;

        /**
         * The corresponding Bukkit view. Will be not null after the first call to {@link #getBukkitView()} and null
         * prior.
         */
        @Nullable
        private CraftStonecutterView bukkitEntity;

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
                @NotNull net.minecraft.world.entity.player.Player player,
                @NotNull SimpleContainer inputSlot,
                @NotNull SimpleContainer resultSlot
        ) {
            super(containerId, player.getInventory(), ContainerLevelAccess.create(player.level(), BlockPos.ZERO));

            this.humanEntity = player.getBukkitEntity();
            this.inputSlot = inputSlot;
            this.resultSlot = resultSlot;

            super.checkReachable = false;

            CompoundContainer container = new CompoundContainer(inputSlot, resultSlot);

            updateSlot(0, container);
            updateSlot(1, container);
        }

        @NotNull
        @Override
        public CraftStonecutterView getBukkitView() {
            if (this.bukkitEntity != null) {
                return this.bukkitEntity;
            }

            CraftInventoryStonecutter inventory = new CraftInventoryStonecutter(this.inputSlot, this.resultSlot);

            this.bukkitEntity = new CraftStonecutterView(this.humanEntity, inventory, this);

            return this.bukkitEntity;
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

        @Contract(value = "_, _ -> false", pure = true)
        @Override
        public boolean clickMenuButton(@Nullable net.minecraft.world.entity.player.Player player, int index) {
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
        private void updateSlot(int slotIndex, @NotNull Container container) {
            Slot slot = super.slots.get(slotIndex);

            Slot newSlot = new Slot(container, slot.slot, slot.x, slot.y);
            newSlot.index = slot.index;

            super.slots.set(slotIndex, newSlot);
        }
    }
}
