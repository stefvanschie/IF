package com.github.stefvanschie.inventoryframework.nms.v1_21_0;

import com.github.stefvanschie.inventoryframework.abstraction.AnvilInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_21_0.util.TextHolderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_21_R1.inventory.view.CraftAnvilView;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal anvil inventory for 1.21.0
 *
 * @since 0.10.18
 */
public class AnvilInventoryImpl extends AnvilInventory {

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory(@NotNull TextHolder title) {
        SimpleContainer inputSlots = new SimpleContainer(2);
        SimpleContainer resultSlot = new SimpleContainer(1);

        return new CraftInventoryAnvil(null, inputSlots, resultSlot) {
            @NotNull
            @Contract(pure = true)
            @Override
            public InventoryType getType() {
                return InventoryType.ANVIL;
            }

            @Override
            public Container getInventory() {
                return new InventoryViewProvider() {
                    @NotNull
                    @Contract(pure = true)
                    @Override
                    public AbstractContainerMenu createMenu(
                            int containerId,
                            @Nullable net.minecraft.world.entity.player.Inventory inventory,
                            @NotNull Player player
                    ) {
                        return new ContainerAnvilImpl(containerId, player, inputSlots, resultSlot);
                    }

                    @NotNull
                    @Contract(pure = true)
                    @Override
                    public Component getDisplayName() {
                        return TextHolderUtil.toComponent(title);
                    }
                };
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
    private abstract static class InventoryViewProvider extends SimpleContainer implements MenuProvider {}

    /**
     * A custom container anvil for responding to item renaming
     *
     * @since 0.10.18
     */
    private class ContainerAnvilImpl extends AnvilMenu {

        /**
         * The container for the input slots.
         */
        @NotNull
        private final SimpleContainer inputSlots;

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
        private CraftAnvilView bukkitEntity;

        /**
         * Creates a new custom anvil container for the specified player.
         *
         * @param containerId the container id
         * @param player the player
         * @param inputSlots the input slots
         * @param resultSlot the result slot
         * @since 0.11.0
         */
        public ContainerAnvilImpl(
                int containerId,
                @NotNull Player player,
                @NotNull SimpleContainer inputSlots,
                @NotNull SimpleContainer resultSlot
        ) {
            super(containerId, player.getInventory(), ContainerLevelAccess.create(player.level(), BlockPos.ZERO));

            this.inputSlots = inputSlots;
            this.resultSlot = resultSlot;

            this.checkReachable = false;
            this.cost.set(AnvilInventoryImpl.super.cost);

            CompoundContainer compoundContainer = new CompoundContainer(inputSlots, resultSlot);

            updateSlot(0, compoundContainer);
            updateSlot(1, compoundContainer);
            updateSlot(2, compoundContainer);
        }

        @Override
        public CraftAnvilView getBukkitView() {
            if (this.bukkitEntity != null) {
                return this.bukkitEntity;
            }

            CraftInventoryAnvil inventory = new CraftInventoryAnvil(
                    this.access.getLocation(),
                    this.inputSlots,
                    this.resultSlot
            );

            this.bukkitEntity = new CraftAnvilView(this.player.getBukkitEntity(), inventory, this);
            this.bukkitEntity.updateFromLegacy(inventory);

            return this.bukkitEntity;
        }

        @Override
        public void broadcastChanges() {
            if (super.cost.checkAndClearUpdateFlag()) {
                broadcastFullState();
            } else {
                for (int index = 0; index < super.slots.size(); index++) {
                    if (!ItemStack.matches(super.remoteSlots.get(index), super.slots.get(index).getItem())) {
                        broadcastFullState();
                        return;
                    }
                }
            }
        }

        @Override
        public boolean setItemName(@Nullable String name) {
            name = name == null ? "" : name;

            /* Only update if the name is actually different. This may be called even if the name is not different,
               particularly when putting an item in the first slot. */
            if (!name.equals(AnvilInventoryImpl.super.observableText.get())) {
                AnvilInventoryImpl.super.observableText.set(name);
            }

            //the client predicts the output result, so we broadcast the state again to override it
            broadcastFullState();
            return true; //no idea what this is for
        }

        @Override
        public void slotsChanged(Container container) {
            broadcastChanges();
        }

        @Override
        public void clicked(int index, int dragData, ClickType clickType, Player player) {
            super.clicked(index, dragData, clickType, player);

            //client predicts first slot, so send data to override
            broadcastFullState();
        }

        @Override
        public void createResult() {}

        @Override
        public void removed(@NotNull Player nmsPlayer) {}

        @Override
        protected void clearContainer(@NotNull Player player, @NotNull Container inventory) {}

        @Override
        protected void onTake(@NotNull Player player, @NotNull ItemStack stack) {}

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
