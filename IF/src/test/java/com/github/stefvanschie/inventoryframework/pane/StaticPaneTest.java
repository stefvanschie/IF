package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StaticPaneTest {

    @Test
    void testCopy() {
        StaticPane original = new StaticPane(5, 1, 1, 1, Pane.Priority.MONITOR);
        original.setVisible(false);
        original.setRotation(90);
        original.flipHorizontally(false);
        original.flipVertically(true);

        StaticPane copy = original.copy();

        assertNotSame(original, copy);

        assertEquals(original.getSlot(), copy.getSlot());
        assertEquals(original.getLength(), copy.getLength());
        assertEquals(original.getHeight(), copy.getHeight());
        assertEquals(original.getPriority(), copy.getPriority());
        assertEquals(original.isVisible(), copy.isVisible());
        assertEquals(original.getRotation(), copy.getRotation());
        assertEquals(original.isFlippedHorizontally(), copy.isFlippedHorizontally());
        assertEquals(original.isFlippedVertically(), copy.isFlippedVertically());
        assertEquals(original.getUUID(), copy.getUUID());
    }

    @Test
    void testRemoveItemCoordinates() {
        StaticPane pane = new StaticPane(Slot.fromXY(0, 0), 1, 1);

        assertDoesNotThrow(() -> pane.removeItem(0, 0));
    }

    @Test
    void testRemoveItemSlot() {
        StaticPane pane = new StaticPane(Slot.fromXY(0, 0), 1, 1);

        assertDoesNotThrow(() -> pane.removeItem(Slot.fromXY(0, 0)));
    }
}
