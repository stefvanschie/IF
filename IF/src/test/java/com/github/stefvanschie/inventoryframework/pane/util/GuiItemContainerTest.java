package com.github.stefvanschie.inventoryframework.pane.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GuiItemContainerTest {

    @Test
    void testExcludeRowsValid() {
        GuiItemContainer container = new GuiItemContainer(8, 4);
        GuiItemContainer shrunk = container.excludeRows(0, 2);

        assertEquals(1, shrunk.getHeight());
        assertNotSame(shrunk, container);
    }

    @Test
    void testExcludeRowsInvalid() {
        GuiItemContainer container = new GuiItemContainer(9, 1);

        assertThrows(IllegalArgumentException.class, () -> container.excludeRows(4, 6));
    }

    @Test
    void testCopy() {
        GuiItemContainer original = new GuiItemContainer(6, 7);

        GuiItemContainer copy = original.copy();

        assertNotSame(original, copy);

        assertEquals(original.getLength(), copy.getLength());
        assertEquals(original.getHeight(), copy.getHeight());
    }

    @Test
    void testHasItem() {
        assertFalse(new GuiItemContainer(8, 5).hasItem());
    }

    @Test
    void testGetItemInside() {
        assertNull(new GuiItemContainer(8, 1).getItem(3, 0));
    }

    @Test
    void testGetItemOutside() {
        GuiItemContainer container = new GuiItemContainer(8, 1);

        assertThrows(IllegalArgumentException.class, () -> container.getItem(7, 4));
    }

    @Test
    void testHasItemInside() {
        assertFalse(new GuiItemContainer(2, 7).hasItem(1, 4));
    }

    @Test
    void testHasItemOutside() {
        GuiItemContainer container = new GuiItemContainer(2, 5);

        assertThrows(IllegalArgumentException.class, () -> container.hasItem(8, 3));
    }
}
