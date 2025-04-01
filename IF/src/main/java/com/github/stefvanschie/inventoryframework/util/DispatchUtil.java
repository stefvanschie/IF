package com.github.stefvanschie.inventoryframework.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class DispatchUtil {
    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /*
     * Schedules a task to run for a given entity.
     *
     * For non-Folia servers, runs on Bukkit scheduler.
     * For Folia servers, runs on the entity's scheduler.
     */
    @SuppressWarnings("deprecation")
    public static void runTaskFor(Entity entity, Plugin plugin, Runnable task) {
        if (isFolia()) {
            entity.getScheduler().run(plugin, e -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
}
