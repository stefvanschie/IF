package com.github.stefvanschie.inventoryframework.gui;

import com.github.stefvanschie.inventoryframework.pane.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GuiComponentTest {

    @Test
    void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new GuiComponent(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> new GuiComponent(1, -1));
        assertThrows(IllegalArgumentException.class, () -> new GuiComponent(-1, -1));
        assertDoesNotThrow(() -> new GuiComponent(0, 0));
    }

    @Test
    void testAddPane() {
        GuiComponent guiComponent = new GuiComponent(0, 0);

        guiComponent.addPane(new StaticPane(1, 1));

        List<Pane> panes = guiComponent.getPanes();

        assertEquals(1, panes.size());
        assertTrue(panes.get(0) instanceof StaticPane);
    }

    @Test
    void testCopy() {
        GuiComponent original = new GuiComponent(0, 0);

        original.addPane(new StaticPane(1, 1));
        original.addPane(new OutlinePane(1, 1));

        GuiComponent copy = original.copy();

        assertNotSame(original, copy);

        assertEquals(original.getLength(), copy.getLength());
        assertEquals(original.getHeight(), copy.getHeight());
        assertEquals(original.getPanes().size(), copy.getPanes().size());
    }

    @Test
    void testExcludeRowsValid() {
        GuiComponent original = new GuiComponent(0, 6);

        original.addPane(new StaticPane(1, 1));
        original.addPane(new OutlinePane(1, 1));
        original.addPane(new PaginatedPane(1, 1));
        original.addPane(new MasonryPane(1, 1));

        GuiComponent shrunk = original.excludeRows(4, 4);

        assertEquals(5, shrunk.getHeight());
        assertEquals(original.getPanes().size(), shrunk.getPanes().size());

        for (Pane pane : original.getPanes()) {
            assertTrue(shrunk.getPanes().contains(pane));
        }
    }

    @Test
    void testExcludeRowsInvalid() {
        GuiComponent guiComponent = new GuiComponent(0, 5);

        //noinspection ResultOfMethodCallIgnored
        assertThrows(IllegalArgumentException.class, () -> guiComponent.excludeRows(8, 8));
    }

    @Test
    void testGetPanesEmptyWhenNone() {
        assertEquals(0, new GuiComponent(0, 0).getPanes().size());
    }

    @Test
    void testGetPanesSorted() {
        GuiComponent guiComponent = new GuiComponent(0, 0);

        guiComponent.addPane(new StaticPane(0, 0, 1, 1, Pane.Priority.HIGHEST));
        guiComponent.addPane(new OutlinePane(0, 0, 1, 1, Pane.Priority.LOW));
        guiComponent.addPane(new PaginatedPane(0, 0, 1, 1, Pane.Priority.MONITOR));

        List<Pane> panes = guiComponent.getPanes();

        assertEquals(Pane.Priority.LOW, panes.get(0).getPriority());
        assertEquals(Pane.Priority.HIGHEST, panes.get(1).getPriority());
        assertEquals(Pane.Priority.MONITOR, panes.get(2).getPriority());
    }

    @Test
    void testGetSize() {
        assertEquals(30, new GuiComponent(3, 10).getSize());
    }
}
