package com.github.stefvanschie.inventoryframework.gui.type.util;

import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class NamedGui extends Gui {

    /**
     * The title of this gui
     */
    @NotNull
    private TextHolder title;

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
     * @since $ADVENTURE-SUPPORT-SINCE$
     */
    public NamedGui(@NotNull TextHolder title) {
        this.title = title;
    }

    /**
     * Sets the title for this inventory. This will (unlike most other methods) directly update itself in order
     * to ensure all viewers will still be viewing the new inventory as well.
     *
     * @param title the title
     */
    public void setTitle(@NotNull String title) {
        setTitle(StringHolder.of(title));
    }

    /**
     * Sets the title for this inventory. This will (unlike most other methods) directly update itself in order
     * to ensure all viewers will still be viewing the new inventory as well.
     *
     * @param title the title
     * @since $ADVENTURE-SUPPORT-SINCE$
     */
    public void setTitle(@NotNull TextHolder title) {
        //copy the viewers
        List<HumanEntity> viewers = getViewers();

        this.title = title;
        this.inventory = createInventory();

        updating = true;

        for (HumanEntity viewer : viewers) {
            show(viewer);
        }

        updating = false;
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
     * Returns the title of this GUI in a wrapped form.
     *
     * @return the title
     * @since $ADVENTURE-SUPPORT-SINCE$
     */
    @NotNull
    @Contract(pure = true)
    public TextHolder getTitleHolder() {
        return title;
    }
}
