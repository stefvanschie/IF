package com.github.stefvanschie.inventoryframework.pane.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SlotTest {

    @Test
    void testEquals() {
        assertEquals(Slot.fromXY(0, 0), Slot.fromXY(0, 0));
        assertNotEquals(Slot.fromXY(0, 1), Slot.fromXY(1, 0));

        assertEquals(Slot.fromIndex(0), Slot.fromIndex(0));
        assertNotEquals(Slot.fromIndex(0), Slot.fromIndex(1));
    }

    @Test
    void testHashCode() {
        assertEquals(Slot.fromXY(0, 0).hashCode(), Slot.fromXY(0, 0).hashCode());
        assertEquals(Slot.fromIndex(0).hashCode(), Slot.fromIndex(0).hashCode());
    }
}
