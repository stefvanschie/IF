package com.github.stefvanschie.inventoryframework.nms.v1_16_4_5;

import com.github.stefvanschie.inventoryframework.abstraction.MerchantInventory;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.nms.v1_16_4_5.util.TextHolderUtil;
import net.minecraft.server.v1_16_R3.Container;
import net.minecraft.server.v1_16_R3.ContainerMerchant;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.IInventory;
import net.minecraft.server.v1_16_R3.IMerchant;
import net.minecraft.server.v1_16_R3.ITileInventory;
import net.minecraft.server.v1_16_R3.InventoryMerchant;
import net.minecraft.server.v1_16_R3.MerchantRecipeList;
import net.minecraft.server.v1_16_R3.MerchantWrapper;
import net.minecraft.server.v1_16_R3.PlayerInventory;
import net.minecraft.server.v1_16_R3.Slot;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryMerchant;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
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
 * Internal merchant inventory for 1.16.4 - 1.16.5
 *
 * @since 0.10.1
 */
public class MerchantInventoryImpl extends MerchantInventory {

    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory createInventory(@NotNull TextHolder title) {
        IMerchant merchant = new MerchantWrapper(null);

        InventoryMerchant container = new InventoryViewProvider(merchant) {
            @NotNull
            @Contract(pure = true)
            @Override
            public Container createMenu(
                    int containerId,
                    @Nullable PlayerInventory inventory,
                    @NotNull EntityHuman player
            ) {
                return new ContainerMerchantImpl(containerId, player, this, merchant);
            }

            @NotNull
            @Contract(pure = true)
            @Override
            public IChatBaseComponent getScoreboardDisplayName() {
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
            public InventoryMerchant getInventory() {
                return container;
            }
        };
    }

    @Override
    public void sendMerchantOffers(@NotNull Player player,
                                   @NotNull List<? extends Map.Entry<? extends MerchantRecipe, ? extends Integer>> trades,
                                   int level, int experience) {
        MerchantRecipeList offers = new MerchantRecipeList();

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

            net.minecraft.server.v1_16_R3.ItemStack nmsItemA = CraftItemStack.asNMSCopy(itemA);
            net.minecraft.server.v1_16_R3.ItemStack nmsItemB = net.minecraft.server.v1_16_R3.ItemStack.b;
            net.minecraft.server.v1_16_R3.ItemStack nmsItemResult = CraftItemStack.asNMSCopy(recipe.getResult());

            if (itemB != null) {
                nmsItemB = CraftItemStack.asNMSCopy(itemB);
            }

            int uses = recipe.getUses();
            int maxUses = recipe.getMaxUses();
            int exp = recipe.getVillagerExperience();
            float multiplier = recipe.getPriceMultiplier();

            net.minecraft.server.v1_16_R3.MerchantRecipe merchantOffer = new net.minecraft.server.v1_16_R3.MerchantRecipe(
                    nmsItemA, nmsItemB, nmsItemResult, uses, maxUses, exp, multiplier
            );
            merchantOffer.setSpecialPrice(entry.getValue());

            offers.add(merchantOffer);
        }

        EntityPlayer entityPlayer = getEntityPlayer(player);

        entityPlayer.openTrade(getWindowId(entityPlayer), offers, level, experience, true, false);
    }

    /**
     * Gets the entity player associated to this player
     *
     * @param player the player to get the entity player from
     * @return the entity player
     * @since 0.10.1
     */
    @NotNull
    @Contract(pure = true)
    private EntityPlayer getEntityPlayer(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    /**
     * Gets the window id for the inventory view the player currently has open
     *
     * @param entityPlayer the player to get the window id for
     * @return the window id
     * @since 0.10.1
     */
    @Contract(pure = true)
    private int getWindowId(@NotNull EntityPlayer entityPlayer) {
        return entityPlayer.activeContainer.windowId;
    }

    /**
     * This is a nice hack to get CraftBukkit to create custom inventories. By providing a container that is also a menu
     * provider, CraftBukkit will allow us to create a custom menu, rather than picking one of the built-in options.
     * That way, we can provide a menu with custom behaviour.
     *
     * @since 0.11.0
     */
    private abstract static class InventoryViewProvider extends InventoryMerchant implements ITileInventory {

        /**
         * Creates a new inventory view provider with two slots.
         *
         * @param merchant the merchant
         * @since 0.11.0
         */
        public InventoryViewProvider(@NotNull IMerchant merchant) {
            super(merchant);
        }
    }

    /**
     * A custom container merchant
     *
     * @since 0.11.0
     */
    private static class ContainerMerchantImpl extends ContainerMerchant {

        /**
         * The human entity viewing this menu.
         */
        @NotNull
        private final HumanEntity humanEntity;

        /**
         * The container for the items slots.
         */
        @NotNull
        private final InventoryMerchant container;

        /**
         * The merchant.
         */
        @NotNull
        private final IMerchant merchant;

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
                @NotNull EntityHuman player,
                @NotNull InventoryMerchant container,
                @NotNull IMerchant merchant
        ) {
            super(containerId, player.inventory, merchant);

            this.humanEntity = player.getBukkitEntity();
            this.container = container;
            this.merchant = merchant;

            super.checkReachable = false;

            updateSlot(0, container);
            updateSlot(1, container);

            Slot slot = super.slots.get(2);

            Slot newSlot = new Slot(container, slot.index, slot.e, slot.f) {
                @Contract(value = "_ -> false", pure = true)
                @Override
                public boolean isAllowed(@Nullable EntityHuman player) {
                    return false;
                }

                @Contract(value = "_ -> false", pure = true)
                @Override
                public boolean isAllowed(@Nullable net.minecraft.server.v1_16_R3.ItemStack itemStack) {
                    return false;
                }
            };
            newSlot.rawSlotIndex = slot.rawSlotIndex;

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
        public void a(@Nullable IInventory container) {}

        @Override
        public void b(@Nullable EntityHuman player) {}

        @Override
        protected void a(@Nullable EntityHuman player, @Nullable World world, @Nullable IInventory container) {}

        @Override
        public void d(int i) {}

        @Override
        public void g(int i) {}

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
