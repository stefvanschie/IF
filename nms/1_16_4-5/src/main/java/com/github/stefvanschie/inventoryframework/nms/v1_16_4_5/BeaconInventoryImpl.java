package com.github.stefvanschie.inventoryframework.nms.v1_16_4_5;

import com.github.stefvanschie.inventoryframework.abstraction.BeaconInventory;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.Container;
import net.minecraft.server.v1_16_R3.ContainerAccess;
import net.minecraft.server.v1_16_R3.ContainerBeacon;
import net.minecraft.server.v1_16_R3.ContainerProperties;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.IInventory;
import net.minecraft.server.v1_16_R3.ITileInventory;
import net.minecraft.server.v1_16_R3.InventorySubcontainer;
import net.minecraft.server.v1_16_R3.PlayerInventory;
import net.minecraft.server.v1_16_R3.Slot;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryBeacon;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryView;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal beacon inventory for 1.16 R3
 *
 * @since 0.8.0
 */
public class BeaconInventoryImpl extends BeaconInventory {

    @NotNull
    @Override
    public Inventory createInventory() {
        IInventory container = new InventoryViewProvider() {
            @NotNull
            @Contract(pure = true)
            @Override
            public Container createMenu(
                    int containerId,
                    @Nullable PlayerInventory inventory,
                    @NotNull EntityHuman player
            ) {
                return new ContainerBeaconImpl(containerId, player, this);
            }

            @NotNull
            @Contract(pure = true)
            @Override
            public IChatBaseComponent getScoreboardDisplayName() {
                return new ChatComponentText("Beacon");
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
     * @since 0.8.0
     */
    private static class ContainerBeaconImpl extends ContainerBeacon {

        /**
         * The player viewing this menu.
         */
        @NotNull
        private final EntityHuman player;

        /**
         * The container for the input slots.
         */
        @NotNull
        private final InventorySubcontainer inputSlot;

        /**
         * The corresponding Bukkit view. Will be not null after the first call to {@link #getBukkitView()} and null
         * prior.
         */
        @Nullable
        private CraftInventoryView bukkitEntity;

        /**
         * Creates a new custom beacon container for the specified player.
         *
         * @param containerId the container id
         * @param player the player
         * @param inputSlot the input slot
         * @since 0.11.0
         */
        public ContainerBeaconImpl(
                int containerId,
                @NotNull EntityHuman player,
                @NotNull InventorySubcontainer inputSlot
        ) {
            super(containerId, player.inventory, new ContainerProperties(3),
                    ContainerAccess.at(player.world, BlockPosition.ZERO));

            this.player = player;
            this.inputSlot = inputSlot;

            super.checkReachable = false;

            Slot slot = super.slots.get(0);

            Slot newSlot = new Slot(inputSlot, slot.index, slot.e, slot.f);
            newSlot.rawSlotIndex = slot.rawSlotIndex;

            super.slots.set(0, newSlot);
        }

        @NotNull
        @Override
        public CraftInventoryView getBukkitView() {
            if (this.bukkitEntity != null) {
                return this.bukkitEntity;
            }

            CraftInventoryBeacon inventory = new CraftInventoryBeacon(this.inputSlot);

            this.bukkitEntity = new CraftInventoryView(this.player.getBukkitEntity(), inventory, this);

            return this.bukkitEntity;
        }

        @Override
        public void b(@Nullable EntityHuman player) {}

    }
}
