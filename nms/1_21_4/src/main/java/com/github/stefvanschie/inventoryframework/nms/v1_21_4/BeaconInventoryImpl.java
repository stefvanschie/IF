package com.github.stefvanschie.inventoryframework.nms.v1_21_4;

import com.github.stefvanschie.inventoryframework.abstraction.BeaconInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftInventoryBeacon;
import org.bukkit.craftbukkit.v1_21_R3.inventory.view.CraftBeaconView;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal beacon inventory for 1.21.3
 *
 * @since 0.10.19
 */
public class BeaconInventoryImpl extends BeaconInventory {

    @NotNull
    @Override
    public Inventory createInventory() {
        Container container = new InventoryViewProvider() {
            @NotNull
            @Contract(pure = true)
            @Override
            public AbstractContainerMenu createMenu(
                    int containerId,
                    @Nullable net.minecraft.world.entity.player.Inventory inventory,
                    @NotNull Player player
            ) {
                return new ContainerBeaconImpl(containerId, player, this);
            }

            @NotNull
            @Contract(pure = true)
            @Override
            public Component getDisplayName() {
                return Component.literal("Beacon");
            }
        };

        container.setMaxStackSize(1); //client limitation

        return new CraftInventoryBeacon(container) {
            @NotNull
            @Contract(pure = true)
            @Override
            public InventoryType getType() {
                return InventoryType.BEACON;
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
         * Creates a new inventory view provider with one slot.
         *
         * @since 0.11.0
         */
        public InventoryViewProvider() {
            super(1);
        }
    }

    /**
     * A custom container beacon
     *
     * @since 0.10.19
     */
    private static class ContainerBeaconImpl extends BeaconMenu {

        /**
         * The player viewing this menu.
         */
        @NotNull
        private final net.minecraft.world.entity.player.Player player;

        /**
         * The container for the input slots.
         */
        @NotNull
        private final SimpleContainer inputSlot;

        /**
         * The corresponding Bukkit view. Will be not null after the first call to {@link #getBukkitView()} and null
         * prior.
         */
        @Nullable
        private CraftBeaconView bukkitEntity;

        /**
         * Creates a new custom beacon container for the specified player.
         *
         * @param containerId the container id
         * @param player the player
         * @param inputSlot the input slot
         * @since 0.11.0
         */
        public ContainerBeaconImpl(int containerId, @NotNull net.minecraft.world.entity.player.Player player, @NotNull SimpleContainer inputSlot) {
            super(containerId, player.getInventory(), new SimpleContainerData(3),
                    ContainerLevelAccess.create(player.level(), BlockPos.ZERO));

            this.player = player;
            this.inputSlot = inputSlot;

            super.checkReachable = false;

            Slot slot = super.slots.get(0);

            Slot newSlot = new Slot(inputSlot, slot.slot, slot.x, slot.y);
            newSlot.index = slot.index;

            super.slots.set(0, newSlot);
        }

        @NotNull
        @Override
        public CraftBeaconView getBukkitView() {
            if (this.bukkitEntity != null) {
                return this.bukkitEntity;
            }

            CraftInventoryBeacon inventory = new CraftInventoryBeacon(this.inputSlot);

            this.bukkitEntity = new CraftBeaconView(this.player.getBukkitEntity(), inventory, this);

            return this.bukkitEntity;
        }

        @Override
        public void removed(@Nullable net.minecraft.world.entity.player.Player player) {}

        @Override
        protected void clearContainer(@Nullable net.minecraft.world.entity.player.Player player, @Nullable Container container) {}

    }
}
