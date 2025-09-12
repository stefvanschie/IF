package com.github.stefvanschie.inventoryframework.nms.v1_20_1;

import com.github.stefvanschie.inventoryframework.abstraction.MerchantInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_20_1.util.TextHolderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryMerchant;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Internal merchant inventory for 1.20.1
 *
 * @since 0.10.14
 */
public class MerchantInventoryImpl extends MerchantInventory {

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory(@NotNull TextHolder title) {
        Merchant merchant = new ClientSideMerchant(null);

        MerchantContainer container = new InventoryViewProvider(merchant) {
            @NotNull
            @Contract(pure = true)
            @Override
            public AbstractContainerMenu createMenu(
                    int containerId,
                    @Nullable net.minecraft.world.entity.player.Inventory inventory,
                    @NotNull net.minecraft.world.entity.player.Player player
            ) {
                return new ContainerMerchantImpl(containerId, player, this, merchant);
            }

            @NotNull
            @Contract(pure = true)
            @Override
            public Component getDisplayName() {
                return TextHolderUtil.toComponent(title);
            }
        };

        return new CraftInventoryMerchant(merchant, container) {
            @NotNull
            @Contract(pure = true)
            @Override
            public InventoryType getType() {
                return InventoryType.MERCHANT;
            }

            @Override
            public MerchantContainer getInventory() {
                return container;
            }
        };
    }

    @Override
    public void sendMerchantOffers(@NotNull Player player,
                                   @NotNull List<? extends Map.Entry<? extends MerchantRecipe, ? extends Integer>> trades,
                                   int level, int experience) {
        MerchantOffers offers = new MerchantOffers();

        for (Map.Entry<? extends MerchantRecipe, ? extends Integer> entry : trades) {
            MerchantRecipe recipe = entry.getKey();
            List<ItemStack> ingredients = recipe.getIngredients();

            if (ingredients.size() < 1) {
                throw new IllegalStateException("Merchant recipe has no ingredients");
            }

            ItemStack itemA = ingredients.get(0);
            ItemStack itemB = null;

            if (ingredients.size() >= 2) {
                itemB = ingredients.get(1);
            }

            net.minecraft.world.item.ItemStack nmsItemA = CraftItemStack.asNMSCopy(itemA);
            net.minecraft.world.item.ItemStack nmsItemB = net.minecraft.world.item.ItemStack.EMPTY;
            net.minecraft.world.item.ItemStack nmsItemResult = CraftItemStack.asNMSCopy(recipe.getResult());

            if (itemB != null) {
                nmsItemB = CraftItemStack.asNMSCopy(itemB);
            }

            int uses = recipe.getUses();
            int maxUses = recipe.getMaxUses();
            int exp = recipe.getVillagerExperience();
            float multiplier = recipe.getPriceMultiplier();

            MerchantOffer merchantOffer = new MerchantOffer(
                    nmsItemA, nmsItemB, nmsItemResult, uses, maxUses, exp, multiplier
            );
            merchantOffer.setSpecialPriceDiff(entry.getValue());

            offers.add(merchantOffer);
        }

        ServerPlayer serverPlayer = getServerPlayer(player);
        int containerId = getContainerId(serverPlayer);

        serverPlayer.sendMerchantOffers(containerId, offers, level, experience, true, false);
    }

    /**
     * Gets the server player associated to this player
     *
     * @param player the player to get the server player from
     * @return the server player
     * @since 0.10.14
     */
    @NotNull
    @Contract(pure = true)
    private ServerPlayer getServerPlayer(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    /**
     * Gets the containerId id for the inventory view the player currently has open
     *
     * @param nmsPlayer the player to get the containerId id for
     * @return the containerId id
     * @since 0.10.14
     */
    @Contract(pure = true)
    private int getContainerId(@NotNull net.minecraft.world.entity.player.Player nmsPlayer) {
        return nmsPlayer.containerMenu.containerId;
    }

    /**
     * This is a nice hack to get CraftBukkit to create custom inventories. By providing a container that is also a menu
     * provider, CraftBukkit will allow us to create a custom menu, rather than picking one of the built-in options.
     * That way, we can provide a menu with custom behaviour.
     *
     * @since 0.11.0
     */
    private abstract static class InventoryViewProvider extends MerchantContainer implements MenuProvider {

        /**
         * Creates a new inventory view provider with two slots.
         *
         * @param merchant the merchant
         * @since 0.11.0
         */
        public InventoryViewProvider(@NotNull Merchant merchant) {
            super(merchant);
        }
    }

    /**
     * A custom container merchant
     *
     * @since 0.11.0
     */
    private static class ContainerMerchantImpl extends MerchantMenu {

        /**
         * The human entity viewing this menu.
         */
        @NotNull
        private final HumanEntity humanEntity;

        /**
         * The container for the items slots.
         */
        @NotNull
        private final MerchantContainer container;

        /**
         * The merchant.
         */
        @NotNull
        private final Merchant merchant;

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
         * @param player      the player
         * @param container   the items slots
         * @param merchant the merchant
         * @since 0.11.0
         */
        public ContainerMerchantImpl(
                int containerId,
                @NotNull net.minecraft.world.entity.player.Player player,
                @NotNull MerchantContainer container,
                @NotNull Merchant merchant
        ) {
            super(containerId, player.getInventory(), merchant);

            this.humanEntity = player.getBukkitEntity();
            this.container = container;
            this.merchant = merchant;

            super.checkReachable = false;

            updateSlot(0, container);
            updateSlot(1, container);

            Slot slot = super.slots.get(2);

            Slot newSlot = new Slot(container, slot.slot, slot.x, slot.y) {
                @Contract(value = "_ -> false", pure = true)
                @Override
                public boolean mayPickup(@Nullable net.minecraft.world.entity.player.Player player) {
                    return false;
                }

                @Contract(value = "_ -> false", pure = true)
                @Override
                public boolean mayPlace(@Nullable net.minecraft.world.item.ItemStack itemStack) {
                    return false;
                }
            };
            newSlot.index = slot.index;

            super.slots.set(2, newSlot);
        }

        @NotNull
        @Override
        public CraftInventoryView getBukkitView() {
            if (this.bukkitEntity != null) {
                return this.bukkitEntity;
            }

            CraftInventoryMerchant inventory = new CraftInventoryMerchant(this.merchant, this.container);

            this.bukkitEntity = new CraftInventoryView(this.humanEntity, inventory, this);

            return this.bukkitEntity;
        }

        @Override
        public void slotsChanged(@Nullable Container container) {}

        @Override
        public void removed(@Nullable net.minecraft.world.entity.player.Player player) {}

        @Override
        protected void clearContainer(@Nullable net.minecraft.world.entity.player.Player player, @Nullable Container container) {}

        @Override
        public void setSelectionHint(int i) {}

        @Override
        public void tryMoveItems(int i) {}

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
