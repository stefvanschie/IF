package com.github.stefvanschie.inventoryframework.gui.type.util;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class NamedGui extends Gui {

    /**
     * The title of this gui
     */
    @NotNull
    private String title;

    /**
     * Constructs a new gui with a title
     *
     * @param title the title/name of this gui
     * @since 0.8.0
     */
    public NamedGui(@NotNull String title) {
        this.title = title;
    }

    /**
     * Creates a new inventory of the type of the implementing class with the provided title.
     *
     * @param title the title for the new inventory
     * @return the new inventory
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public abstract Inventory createInventory(@NotNull String title);

    @NotNull
    @Override
    public Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = createInventory(getTitle());
        }

        return inventory;
    }

    @NotNull
    @Override
    public Inventory createInventory() {
        return createInventory(title);
    }

    /**
     * Sets the title for this inventory. This will (unlike most other methods) directly update itself in order
     * to ensure all viewers will still be viewing the new inventory as well.
     *
     * @param title the title
     */
    public void setTitle(@NotNull String title) {
        //copy the viewers
        List<HumanEntity> viewers = getViewers();

        this.inventory = createInventory(title);
        this.title = title;

        viewers.forEach(humanEntity -> humanEntity.openInventory(getInventory()));
    }

    /**
     * Returns the title of this gui
     *
     * @return the title
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public String getTitle() {
        return title;
    }
}
