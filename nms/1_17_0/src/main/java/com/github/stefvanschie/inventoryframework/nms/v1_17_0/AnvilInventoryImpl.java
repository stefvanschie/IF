package com.github.stefvanschie.inventoryframework.nms.v1_17_0;

import com.github.stefvanschie.inventoryframework.abstraction.AnvilInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_17_0.util.TextHolderUtil;
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
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryView;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Internal anvil inventory for 1.17 R1
 *
 * @since 0.9.9
 */
public class AnvilInventoryImpl extends AnvilInventory {

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory(@NotNull TextHolder title) {
        SimpleContainer inputSlots = new SimpleContainer(2);
        SimpleContainer resultSlot = new SimpleContainer(1);

        return new CraftInventoryAnvil(null, inputSlots, resultSlot, null) {
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
     * @since 0.9.9
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
        private CraftInventoryView bukkitEntity;

        /**
         * The method to update the tracked slot.
         */
        @NotNull
        private final Method updateTrackedSlotMethod;

        /**
         * The field containing the properties.
         */
        @NotNull
        private final Field propertiesField;

        /**
         * The field containing the listeners.
         */
        @NotNull
        private final Field listenersField;

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
                @NotNull net.minecraft.world.entity.player.Player player,
                @NotNull SimpleContainer inputSlots,
                @NotNull SimpleContainer resultSlot
        ) {
            super(containerId, player.getInventory(), ContainerLevelAccess.create(player.level, BlockPos.ZERO));

            this.inputSlots = inputSlots;
            this.resultSlot = resultSlot;

            this.checkReachable = false;
            this.cost.set(AnvilInventoryImpl.super.cost);

            CompoundContainer compoundContainer = new CompoundContainer(inputSlots, resultSlot);

            updateSlot(0, compoundContainer);
            updateSlot(1, compoundContainer);
            updateSlot(2, compoundContainer);

            try {
                //noinspection JavaReflectionMemberAccess
                this.updateTrackedSlotMethod = AbstractContainerMenu.class.getDeclaredMethod(
                        "a",
                        int.class,
                        ItemStack.class,
                        Supplier.class
                );
                this.updateTrackedSlotMethod.setAccessible(true);

                //noinspection JavaReflectionMemberAccess
                this.propertiesField = AbstractContainerMenu.class.getDeclaredField("l");
                this.propertiesField.setAccessible(true);

                //noinspection JavaReflectionMemberAccess
                this.listenersField = AbstractContainerMenu.class.getDeclaredField("u");
                this.listenersField.setAccessible(true);
            } catch (NoSuchMethodException | NoSuchFieldException exception) {
                throw new IllegalStateException(exception);
            }
        }

        @NotNull
        @Override
        public CraftInventoryView getBukkitView() {
            if (this.bukkitEntity != null) {
                return this.bukkitEntity;
            }

            CraftInventoryAnvil inventory = new CraftInventoryAnvil(
                    this.access.getLocation(),
                    this.inputSlots,
                    this.resultSlot,
                    this
            );

            this.bukkitEntity = new CraftInventoryView(this.player.getBukkitEntity(), inventory, this);

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
        public void setItemName(@Nullable String name) {
            name = name == null ? "" : name;

            /* Only update if the name is actually different. This may be called even if the name is not different,
               particularly when putting an item in the first slot. */
            if (!name.equals(AnvilInventoryImpl.super.observableText.get())) {
                AnvilInventoryImpl.super.observableText.set(name);
            }

            //the client predicts the output result, so we broadcast the state again to override it
            broadcastFullState();
        }

        @Override
        public void slotsChanged(@NotNull Container container) {
            broadcastChanges();
        }

        @Override
        public void clicked(int index, int dragData, @NotNull ClickType clickType, @NotNull net.minecraft.world.entity.player.Player player) {
            super.clicked(index, dragData, clickType, player);

            //client predicts first slot, so send data to override
            broadcastFullState();
        }

        @Override
        public void createResult() {}

        @Override
        public void removed(@NotNull net.minecraft.world.entity.player.Player nmsPlayer) {}

        @Override
        protected void clearContainer(@NotNull net.minecraft.world.entity.player.Player player, @NotNull Container inventory) {}

        @Override
        protected void onTake(@NotNull net.minecraft.world.entity.player.Player player, @NotNull ItemStack stack) {}

        /**
         * Broadcasts the full menu state to the client.
         *
         * @since 0.11.0
         */
        private void broadcastFullState() {
            for (int index = 0; index < this.slots.size(); index++) {
                ItemStack itemstack = this.slots.get(index).getItem();

                Objects.requireNonNull(itemstack);

                Supplier<ItemStack> supplier = itemstack::copy;
                try {
                    this.updateTrackedSlotMethod.invoke(this, index, itemstack, supplier);
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    throw new IllegalStateException(exception);
                }
            }

            List<DataSlot> properties;
            try {
                //noinspection unchecked
                properties = (List<DataSlot>) this.propertiesField.get(this);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(exception);
            }

            for (int index = 0; index < properties.size(); index++) {
                DataSlot property = properties.get(index);

                if (property.checkAndClearUpdateFlag()) {
                    try {
                        //noinspection unchecked
                        for (ContainerListener listener : (List<ContainerListener>) this.listenersField.get(this)) {
                            listener.dataChanged(this, index, property.get());
                        }
                    } catch (IllegalAccessException exception) {
                        throw new IllegalStateException(exception);
                    }
                }
            }

            this.sendAllDataToRemote();
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
