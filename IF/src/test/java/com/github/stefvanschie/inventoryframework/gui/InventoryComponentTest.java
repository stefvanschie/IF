package com.github.stefvanschie.inventoryframework.gui;

import com.github.stefvanschie.inventoryframework.pane.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryComponentTest {

    @Test
    void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryComponent(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> new InventoryComponent(1, -1));
        assertThrows(IllegalArgumentException.class, () -> new InventoryComponent(-1, -1));
        assertDoesNotThrow(() -> new InventoryComponent(0, 0));
    }

    @Test
    void testAddPane() {
        InventoryComponent inventoryComponent = new InventoryComponent(0, 0);

        inventoryComponent.addPane(new StaticPane(0, 0));

        List<Pane> panes = inventoryComponent.getPanes();

        assertEquals(1, panes.size());
        assertTrue(panes.get(0) instanceof StaticPane);
    }

    @Test
    void testCopy() {
        InventoryComponent original = new InventoryComponent(0, 0);

        original.addPane(new StaticPane(0, 0));
        original.addPane(new OutlinePane(0, 0));

        InventoryComponent copy = original.copy();

        assertNotSame(original, copy);

        assertEquals(original.getLength(), copy.getLength());
        assertEquals(original.getHeight(), copy.getHeight());
        assertEquals(original.getPanes().size(), copy.getPanes().size());
    }

    @Test
    void testExcludeRowsValid() {
        InventoryComponent original = new InventoryComponent(0, 6);

        original.addPane(new StaticPane(0, 0));
        original.addPane(new OutlinePane(0, 0));
        original.addPane(new PaginatedPane(0, 0));
        original.addPane(new MasonryPane(0, 0));

        InventoryComponent shrunk = original.excludeRows(4, 4);

        assertEquals(5, shrunk.getHeight());
        assertEquals(original.getPanes().size(), shrunk.getPanes().size());

        for (Pane pane : original.getPanes()) {
            assertTrue(shrunk.getPanes().contains(pane));
        }
    }

    @Test
    void testExcludeRowsInvalid() {
        InventoryComponent inventoryComponent = new InventoryComponent(0, 5);

        //noinspection ResultOfMethodCallIgnored
        assertThrows(IllegalArgumentException.class, () -> inventoryComponent.excludeRows(8, 8));
    }

    @Test
    void testGetPanesEmptyWhenNone() {
        assertEquals(0, new InventoryComponent(0, 0).getPanes().size());
    }

    @Test
    void testGetPanesSorted() {
        InventoryComponent inventoryComponent = new InventoryComponent(0, 0);

        inventoryComponent.addPane(new StaticPane(0, 0, 0, 0, Pane.Priority.HIGHEST));
        inventoryComponent.addPane(new OutlinePane(0, 0, 0, 0, Pane.Priority.LOW));
        inventoryComponent.addPane(new PaginatedPane(0, 0, 0, 0, Pane.Priority.MONITOR));

        List<Pane> panes = inventoryComponent.getPanes();

        assertEquals(Pane.Priority.LOW, panes.get(0).getPriority());
        assertEquals(Pane.Priority.HIGHEST, panes.get(1).getPriority());
        assertEquals(Pane.Priority.MONITOR, panes.get(2).getPriority());
    }

    @Test
    void testGetSize() {
        assertEquals(30, new InventoryComponent(3, 10).getSize());
    }
}
