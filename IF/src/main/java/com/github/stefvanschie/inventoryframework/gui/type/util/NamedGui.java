package com.github.stefvanschie.inventoryframework.gui.type.util;

import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class NamedGui extends Gui {

    /**
     * The title of this gui
     */
    @NotNull
    private TextHolder title;

    /**
     * Whether the title is dirty i.e., has changed
     */
    private boolean dirty = false;

    /**
     * Constructs a new gui with a title
     *
     * @param title the title/name of this gui
     * @since 0.8.0
     */
    public NamedGui(@NotNull String title) {
        this(StringHolder.of(title));
    }

    /**
     * Constructs a new gui with a title
     *
     * @param title the title/name of this gui
     * @since 0.10.0
     */
    public NamedGui(@NotNull TextHolder title) {
        this.title = title;
    }

    /**
     * Sets the title for this inventory.
     *
     * @param title the title
     */
    public void setTitle(@NotNull String title) {
        setTitle(StringHolder.of(title));
    }

    /**
     * Sets the title for this inventory.
     *
     * @param title the title
     * @since 0.10.0
     */
    public void setTitle(@NotNull TextHolder title) {
        this.title = title;
        this.dirty = true;
    }

    /**
     * Returns the title of this gui as a legacy string.
     *
     * @return the title
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    public String getTitle() {
        return title.asLegacyString();
    }

    /**
     * Update the gui for everyone if the inventory is considered dirty (the name has changed)
     * then it will be reopened, otherwise its content will simply be updated
     *
     */
    public void update() {
        updating = true;

        if (dirty)
            for (HumanEntity viewer : getViewers()) {
                ItemStack cursor = viewer.getItemOnCursor();

                viewer.setItemOnCursor(new ItemStack(Material.AIR));

                show(viewer, true);

                viewer.setItemOnCursor(cursor);
            }
        else
            for (HumanEntity viewer : getViewers())
                show(viewer, false);

        if (!updating)
            throw new AssertionError("Gui#isUpdating became false before Gui#update finished");

        updating = false;
    }

    /**
     * Returns the title of this GUI in a wrapped form.
     *
     * @return the title
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public TextHolder getTitleHolder() {
        return title;
    }

    /**
     * Gets whether this title is dirty or not i.e. whether the title has changed.
     *
     * @return whether the title is dirty
     * @since 0.10.0
     */
    @Contract(pure = true)
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Marks that the changes present here have been accepted. This sets dirty to false. If dirty was already false,
     * this will do nothing.
     *
     * @since 0.10.0
     */
    public void markChanges() {
        this.dirty = false;
    }
}
