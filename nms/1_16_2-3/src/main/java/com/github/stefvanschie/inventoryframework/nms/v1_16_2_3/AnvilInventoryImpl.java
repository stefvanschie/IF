package com.github.stefvanschie.inventoryframework.nms.v1_16_2_3;

import com.github.stefvanschie.inventoryframework.abstraction.AnvilInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_16_2_3.util.TextHolderUtil;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.Container;
import net.minecraft.server.v1_16_R2.ContainerAccess;
import net.minecraft.server.v1_16_R2.ContainerAnvil;
import net.minecraft.server.v1_16_R2.ContainerProperty;
import net.minecraft.server.v1_16_R2.EntityHuman;
import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.IChatBaseComponent;
import net.minecraft.server.v1_16_R2.ICrafting;
import net.minecraft.server.v1_16_R2.IInventory;
import net.minecraft.server.v1_16_R2.InventoryClickType;
import net.minecraft.server.v1_16_R2.InventoryLargeChest;
import net.minecraft.server.v1_16_R2.InventorySubcontainer;
import net.minecraft.server.v1_16_R2.ItemStack;
import net.minecraft.server.v1_16_R2.PlayerInventory;
import net.minecraft.server.v1_16_R2.Slot;
import net.minecraft.server.v1_16_R2.TileEntityContainer;
import net.minecraft.server.v1_16_R2.TileEntityTypes;
import net.minecraft.server.v1_16_R2.World;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Internal anvil inventory for 1.16 R2
 *
 * @since 0.8.0
 */
public class AnvilInventoryImpl extends AnvilInventory {

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory(@NotNull TextHolder title) {
        InventorySubcontainer inputSlots = new InventorySubcontainer(2);
        InventorySubcontainer resultSlot = new InventorySubcontainer(1);

        return new CraftInventoryAnvil(null, inputSlots, resultSlot, null) {
            @NotNull
            @Contract(pure = true)
            @Override
            public InventoryType getType() {
                return InventoryType.ANVIL;
            }

            @Override
            public IInventory getInventory() {
                return new InventoryViewProvider() {
                    @NotNull
                    @Contract(pure = true)
                    @Override
                    public Container createContainer(
                            int containerId,
                            PlayerInventory inventory
                    ) {
                        return new ContainerAnvilImpl(containerId, inventory.player, inputSlots, resultSlot);
                    }

                    @NotNull
                    @Contract(pure = true)
                    @Override
                    public IChatBaseComponent getContainerName() {
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
    private abstract static class InventoryViewProvider extends TileEntityContainer {
        protected InventoryViewProvider() {
            super(TileEntityTypes.FURNACE); //close enough
        }

        @Override
        public void clear() {}

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ItemStack getItem(int index) {
            return null;
        }

        @Override
        public ItemStack splitStack(int firstIndex, int secondIndex) {
            return null;
        }

        @Override
        public ItemStack splitWithoutUpdate(int index) {
            return null;
        }

        @Override
        public void setItem(int index, ItemStack itemStack) {}

        @Override
        public int getMaxStackSize() {
            return 0;
        }

        @Override
        public boolean a(EntityHuman entityHuman) {
            return false;
        }

        @Override
        public List<ItemStack> getContents() {
            return null;
        }

        @Override
        public void onOpen(CraftHumanEntity craftHumanEntity) {}

        @Override
        public void onClose(CraftHumanEntity craftHumanEntity) {}

        @Override
        public List<HumanEntity> getViewers() {
            return null;
        }

        @Override
        public void setMaxStackSize(int index) {}
    }

    /**
     * A custom container anvil for responding to item renaming
     *
     * @since 0.8.0
     */
    private class ContainerAnvilImpl extends ContainerAnvil {

        /**
         * The container for the input slots.
         */
        @NotNull
        private final InventorySubcontainer inputSlots;

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
                @NotNull EntityHuman player,
                @NotNull InventorySubcontainer inputSlots,
                @NotNull InventorySubcontainer resultSlot
        ) {
            super(containerId, player.inventory, ContainerAccess.at(player.world, BlockPosition.ZERO));

            this.inputSlots = inputSlots;
            this.resultSlot = resultSlot;

            this.checkReachable = false;
            this.levelCost.set(AnvilInventoryImpl.super.cost);

            InventoryLargeChest compoundContainer = new InventoryLargeChest(inputSlots, resultSlot);

            updateSlot(0, compoundContainer);
            updateSlot(1, compoundContainer);
            updateSlot(2, compoundContainer);

            try {
                this.propertiesField = Container.class.getDeclaredField("d");
                this.propertiesField.setAccessible(true);

                this.listenersField = Container.class.getDeclaredField("listeners");
                this.listenersField.setAccessible(true);
            } catch (NoSuchFieldException exception) {
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
                    this.containerAccess.getLocation(),
                    this.inputSlots,
                    this.resultSlot,
                    this
            );

            this.bukkitEntity = new CraftInventoryView(this.player.getBukkitEntity(), inventory, this);

            return this.bukkitEntity;
        }

        @Override
        public void c() {
            if (super.levelCost.c()) {
                broadcastFullState();
            } else {
                for (int index = 0; index < super.slots.size(); index++) {
                    if (!ItemStack.matches(super.items.get(index), super.slots.get(index).getItem())) {
                        broadcastFullState();
                        return;
                    }
                }
            }
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
            broadcastFullState();
        }

        @Override
        public ItemStack a(int index, int dragData, @NotNull InventoryClickType clickType, @NotNull EntityHuman player) {
            ItemStack item = super.a(index, dragData, clickType, player);

            //client predicts first slot, so send data to override
            broadcastFullState();

            return item;
        }

        @Override
        protected ItemStack a(@NotNull EntityHuman player, @NotNull ItemStack stack) {
            return stack;
        }

        @Override
        public void a(@NotNull IInventory container) {
            c();
        }

        @Override
        public void e() {}

        @Override
        public void b(@NotNull EntityHuman nmsPlayer) {}

        @Override
        protected void a(@NotNull EntityHuman player, @NotNull World world, @NotNull IInventory inventory) {}

        /**
         * Broadcasts the full menu state to the client.
         *
         * @since 0.11.0
         */
        private void broadcastFullState() {
            List<ContainerProperty> properties;
            try {
                //noinspection unchecked
                properties = (List<ContainerProperty>) this.propertiesField.get(this);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(exception);
            }

            for (int index = 0; index < properties.size(); index++) {
                ContainerProperty property = properties.get(index);

                if (property.c()) {
                    try {
                        //noinspection unchecked
                        for (ICrafting listener : (List<ICrafting>) this.listenersField.get(this)) {
                            listener.setContainerData(this, index, property.get());
                        }
                    } catch (IllegalAccessException exception) {
                        throw new IllegalStateException(exception);
                    }
                }
            }

            if (super.player instanceof EntityPlayer) {
                ((EntityPlayer) super.player).updateInventory(this);
            }
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
