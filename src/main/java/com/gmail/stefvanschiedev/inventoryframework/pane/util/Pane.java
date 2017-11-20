package com.gmail.stefvanschiedev.inventoryframework.pane.util;

import com.gmail.stefvanschiedev.inventoryframework.GUILocation;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The base class for all panes.
 */
public abstract class Pane {

    /**
     * The starting position of this pane
     */
    protected GUILocation start;

    /**
     * Length is horizontal, width is vertical
     */
    protected int length, width;

    /**
     * The render priority of this pane
     */
    @NotNull private Priority priority;

    /**
     * The visibility state of the pane
     */
    private boolean visible;

    /**
     * An additional click listener
     */
    @Nullable
    protected Consumer<InventoryClickEvent> listener;

    /**
     * Constructs a new default pane
     *
     * @param start the upper left corner of the pane
     * @param length the length of the pane
     * @param width the width of the pane
     */
    protected Pane(@NotNull GUILocation start, int length, int width) {
        assert start.getX() + length <= 9 : "length longer than maximum size";
        assert start.getY() + width <= 6 : "width longer than maximum size";

        this.start = start;

        this.length = length;
        this.width = width;

        this.priority = Priority.NORMAL;
        this.visible = true;
    }

    /**
     * Has to set all the items in the right spot inside the inventory
     *
     * @param inventory the inventory that the items should be displayed in
     */
    public abstract void display(Inventory inventory);

    /**
     * @return the location fo this pane
     */
    @NotNull
    public GUILocation getLocation() {
        return start;
    }

    /**
     * @return the length of this pane
     */
    public int getLength() {
        return length;
    }

    /**
     * @return the length of this pane
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the render priority for this pane
     */
    @NotNull
    @Contract(pure = true)
    public Priority getPriority() {
        return priority;
    }

    /**
     * Sets the rendering priority of this pane
     *
     * @param priority the new priority
     */
    public void setPriority(@NotNull Priority priority) {
        this.priority = priority;
    }

    /**
     * @return the pane's visibility
     */
    @Contract(pure = true)
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets whether this pane is visible or not
     *
     * @param visible the pane's visibility
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Sets a listener to listen for clicks in this pane
     *
     * @param listener the listener to attach
     */
    public void setClickListener(@NotNull Consumer<InventoryClickEvent> listener) {
        this.listener = listener;
    }

    /**
     * Called whenever there is being clicked on this pane
     *
     * @param event the event that occurred while clicking on this item
     * @return whether the item was found or not
     */
    public abstract boolean click(@NotNull InventoryClickEvent event);

    /**
     * An enum representing the rendering priorities for the panes
     */
    public enum Priority {
        LOWEST,
        LOW,
        NORMAL,
        HIGH,
        HIGHEST,
        MONITOR
    }
}