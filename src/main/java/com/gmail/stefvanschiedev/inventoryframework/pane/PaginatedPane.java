package com.gmail.stefvanschiedev.inventoryframework.pane;

import com.gmail.stefvanschiedev.inventoryframework.GUILocation;
import com.gmail.stefvanschiedev.inventoryframework.pane.util.Pane;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class PaginatedPane extends Pane {

    /**
     * A set of panes for the different pages
     */
    private Pane[] panes;

    /**
     * The current page
     */
    private int page;

    /**
     * {@inheritDoc}
     */
    public PaginatedPane(@NotNull GUILocation start, int length, int width, int pages) {
        super(start, length, width);

        this.panes = new Pane[pages];
    }

    /**
     * @return the current page
     */
    public int getPage() {
        return page;
    }

    /**
     * @return the amount of pages
     */
    public int getPages() {
        return panes.length;
    }
    /**
     * Assigns a pane to a selected page
     *
     * @param page the page to assign the pane to
     * @param pane the new pane
     */
    public void setPane(int page, Pane pane) {
        this.panes[page] = pane;
    }

    /**
     * Sets the current displayed page
     *
     * @param page the page
     */
    public void setPage(int page) {
        assert page >= 0 && page < panes.length : "page outside range";

        this.page = page;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void display(Inventory inventory) {
        this.panes[page].display(inventory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean click(@NotNull InventoryClickEvent event) {
        return this.panes[page].click(event);
    }
}