package com.github.stefvanschie.inventoryframework.gui;

import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class GuiClickEvent {

    @NotNull
    private final InventoryClickEvent event;

    /**
     * The slot that was clicked, expressed relative to the top-left corner of the pane that received the click.
     * For example, if a pane starts at column 3, row 1 of the inventory and the player clicks column 3, row 1,
     * this slot will be (0, 0).
     */
    @NotNull
    private final Slot relativeSlot;

    public GuiClickEvent(@NotNull InventoryClickEvent event, @NotNull Slot relativeSlot) {
        this.event = event;
        this.relativeSlot = relativeSlot;
    }

    @NotNull
    public InventoryClickEvent getClickEvent() {
        return event;
    }

    @NotNull
    public Slot getRelativeSlot() {
        return relativeSlot;
    }
}
