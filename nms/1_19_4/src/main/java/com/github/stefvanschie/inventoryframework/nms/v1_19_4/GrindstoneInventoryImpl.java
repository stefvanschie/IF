package com.github.stefvanschie.inventoryframework.nms.v1_19_4;

import com.github.stefvanschie.inventoryframework.abstraction.GrindstoneInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_19_4.util.TextHolderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.Slot;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryGrindstone;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal grindstone inventory for 1.19.4
 *
 * @since 0.10.9
 */
public class GrindstoneInventoryImpl extends GrindstoneInventory {

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
                return new ContainerGrindstoneImpl(containerId, player, this, resultSlot);
            }

            @NotNull
            @Contract(pure = true)
            @Override
            public Component getDisplayName() {
                return TextHolderUtil.toComponent(title);
            }
        };

        return new CraftInventoryGrindstone(container, resultSlot) {
            @NotNull
            @Contract(pure = true)
            @Override
            public InventoryType getType() {
                return InventoryType.GRINDSTONE;
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
     * A custom container grindstone
     *
     * @since 0.10.9
     */
    private static class ContainerGrindstoneImpl extends GrindstoneMenu {

        /**
         * The human entity viewing this menu.
         */
        @NotNull
        private final HumanEntity humanEntity;

        /**
         * The container for the items slots.
         */
        @NotNull
        private final SimpleContainer itemsSlots;

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
        private CraftInventoryView bukkitEntity;

        /**
         * Creates a new custom grindstone container for the specified player.
         *
         * @param containerId the container id
         * @param player the player
         * @param itemsSlots the items slots
         * @param resultSlot the result slot
         * @since 0.11.0
         */
        public ContainerGrindstoneImpl(
                int containerId,
                @NotNull Player player,
                @NotNull SimpleContainer itemsSlots,
                @NotNull SimpleContainer resultSlot
        ) {
            super(containerId, player.getInventory(), ContainerLevelAccess.create(player.level, BlockPos.ZERO));

            this.humanEntity = player.getBukkitEntity();
            this.itemsSlots = itemsSlots;
            this.resultSlot = resultSlot;

            super.checkReachable = false;

            CompoundContainer container = new CompoundContainer(itemsSlots, resultSlot);

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

            CraftInventoryGrindstone inventory = new CraftInventoryGrindstone(this.itemsSlots, this.resultSlot);

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
