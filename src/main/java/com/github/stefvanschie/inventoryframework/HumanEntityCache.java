package com.github.stefvanschie.inventoryframework;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for containing players and their inventory state for later use
 *
 * @since 0.4.0
 */
public class HumanEntityCache {

    /**
     * A map containing the player's and their inventory contents. The ItemStack[] contains only the hotbar and
     * inventory contents. 0-8 is the hotbar, with 9-35 being the inventory both starting in the top-left corner and
     * continuing in reading order.
     */
    private final Map<HumanEntity, ItemStack[]> inventories = new HashMap<>();

    /**
     * Stores this player's inventory in the cache. If the player was already stored, their cache will be overwritten.
     * Clears the player's inventory afterwards.
     *
     * @param humanEntity the human entity to keep in the cache
     */
    public void storeAndClear(@NotNull HumanEntity humanEntity) {
        store(humanEntity);

        Inventory inventory = humanEntity.getInventory();
        for (int i = 0; i < 36; i++) {
            inventory.clear(i);
        }
    }

    /**
     * Restores the contents of the specified human entity, clearing the cache afterwards.
     * This method will fail silently if no cache is available.
     *
     * @param humanEntity the human entity to restore its cache for
     * @since 0.5.19
     */
    public void restoreAndForget(@NotNull HumanEntity humanEntity) {
        restore(humanEntity);
        clearCache(humanEntity);
    }

    /**
     * Restores all players' contents into their inventory, clearing the cache afterwards.
     *
     * @since 0.5.19
     */
    public void restoreAndForgetAll() {
        restoreAll();
        clearCache();
    }

    /**
     * Adds the given item stack to the human entity's cached inventory. The returned amount is the amount of items of
     * the provided item stack that could not be put into the cached inventory. This number will always be equal or
     * less than the amount of items in the provided item stack, but no less than zero. The item stack provided will not
     * be updated with this leftover amount. If the human entity provided is not in the human entity cache, this method
     * will return an {@link IllegalStateException}. The items will be added to the inventory in the same way as the
     * items are stored. The items may be added to an already existing item stack, but the item stack's amount will
     * never go over the maximum stack size.
     *
     * @param humanEntity the human entity to add the item to
     * @param item the item to add to the cached inventory
     * @return the amount of leftover items that couldn't be fit in the cached inventory
     * @throws IllegalStateException if the human entity's inventory is not cached
     * @since 0.6.1
     */
    protected int add(@NotNull HumanEntity humanEntity, @NotNull ItemStack item) {
        ItemStack[] items = inventories.get(humanEntity);

        if (items == null) {
            throw new IllegalStateException("The human entity '" + humanEntity.getUniqueId().toString() +
                "' does not have a cached inventory");
        }

        int amountPutIn = 0;

        for (int i = 0; i < items.length; i++) {
            ItemStack itemStack = items[i];

            if (itemStack == null) {
                items[i] = item.clone();
                items[i].setAmount(item.getAmount() - amountPutIn);
                amountPutIn = item.getAmount();
                break;
            }

            if (!itemStack.isSimilar(item)) {
                continue;
            }

            int additionalAmount = Math.min(itemStack.getMaxStackSize() - itemStack.getAmount(), item.getAmount());

            itemStack.setAmount(itemStack.getAmount() + additionalAmount);
            amountPutIn += additionalAmount;

            if (amountPutIn == item.getAmount()) {
                break;
            }
        }

        return item.getAmount() - amountPutIn;
    }

    /**
     * Stores this player's inventory in the cache. If the player was already stored, their cache will be overwritten.
     *
     * @param humanEntity the human entity to keep in the cache
     * @since 0.4.0
     */
    private void store(@NotNull HumanEntity humanEntity) {
        ItemStack[] items = new ItemStack[36];

        for (int i = 0 ; i < 36; i++) {
            items[i] = humanEntity.getInventory().getItem(i);
        }

        inventories.put(humanEntity, items);
    }

    /**
     * Restores the contents of the specified human entity. This method will fail silently if no cache is available. The
     * cache will not be cleared.
     *
     * @param humanEntity the human entity to restore its cache for
     * @since 0.4.0
     */
    private void restore(@NotNull HumanEntity humanEntity) {
        ItemStack[] items = inventories.get(humanEntity);

        if (items == null) {
            return;
        }

        for (int i = 0; i < items.length; i++) {
            humanEntity.getInventory().setItem(i, items[i]);
        }
    }

    /**
     * Restores all players' contents into their inventory. The cache will not be cleared.
     *
     * @since 0.4.0
     */
    private void restoreAll() {
        inventories.keySet().forEach(this::restore);
    }

    /**
     * Clear the cache for the specified human entity
     *
     * @param humanEntity the human entity to clear the cache for
     * @since 0.4.0
     */
    private void clearCache(@NotNull HumanEntity humanEntity) {
        inventories.remove(humanEntity);
    }

    /**
     * This clears the cache.
     *
     * @since 0.4.0
     */
    private void clearCache() {
        inventories.clear();
    }
}
