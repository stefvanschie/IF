package com.forcemc.inventories.pane;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class PaginatedPaneTest {

    @Test
    void testAddPageEmpty() {
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 1, 1);

        StaticPane staticPane = new StaticPane(0, 0, 1, 1);

        assertDoesNotThrow(() -> {
            paginatedPane.addPage(staticPane);

            Collection<Pane> panes = paginatedPane.getPanes(0);

            assertEquals(1, panes.size());
            assertSame(staticPane, panes.iterator().next());
        });
    }

    @Test
    void testAddPageNotEmpty() {
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 1, 1);

        StaticPane staticPane1 = new StaticPane(0, 0, 1, 1);
        StaticPane staticPane2 = new StaticPane(0, 0, 1, 1);

        paginatedPane.addPane(0, staticPane1);

        assertDoesNotThrow(() -> {
            paginatedPane.addPage(staticPane2);

            Collection<Pane> panes = paginatedPane.getPanes(1);

            assertEquals(1, panes.size());
            assertSame(staticPane2, panes.iterator().next());
        });
    }

    @Test
    void testAddPageException() {
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 1, 1);

        StaticPane staticPane1 = new StaticPane(0, 0, 1, 1);
        StaticPane staticPane2 = new StaticPane(0, 0, 1, 1);

        paginatedPane.addPane(Integer.MAX_VALUE, staticPane1);

        assertThrows(ArithmeticException.class, () -> paginatedPane.addPage(staticPane2));
    }

    @Test
    void testCopy() {
        PaginatedPane original = new PaginatedPane(5, 5, 4, 1, Pane.Priority.NORMAL);
        original.setVisible(false);

        original.addPane(0, new OutlinePane(1, 1));
        original.addPane(1, new OutlinePane(1, 1));
        original.addPane(2, new PaginatedPane(1, 1));
        original.addPane(3, new PaginatedPane(1, 1));
        original.addPane(4, new OutlinePane(1, 1));

        original.setPage(4);

        PaginatedPane copy = original.copy();

        assertNotSame(original, copy);

        assertEquals(original.getX(), copy.getX());
        assertEquals(original.getY(), copy.getY());
        assertEquals(original.getLength(), copy.getLength());
        assertEquals(original.getHeight(), copy.getHeight());
        assertEquals(original.getPriority(), copy.getPriority());
        assertEquals(original.isVisible(), copy.isVisible());
        assertEquals(original.getPage(), copy.getPage());
        assertEquals(original.getPages(), copy.getPages());
        assertEquals(original.getUUID(), copy.getUUID());
    }

    @Test
    void testDeletePageExists() {
        PaginatedPane pane = new PaginatedPane(0, 0, 1, 1);

        StaticPane staticPane = new StaticPane(0, 0, 1, 1);

        pane.addPane(0, new StaticPane(0, 0, 1, 1));
        pane.addPane(1, staticPane);

        pane.deletePage(0);

        assertEquals(1, pane.getPages());
        assertEquals(1, pane.getPanes(0).size());
        assertSame(staticPane, pane.getPanes(0).toArray(new Pane[0])[0]);
    }

    @Test
    void testDeletePageNotExists() {
        PaginatedPane pane = new PaginatedPane(0, 0, 1, 1);

        StaticPane staticPane1 = new StaticPane(0, 0, 1, 1);
        StaticPane staticPane2 = new StaticPane(0, 0, 1, 1);

        pane.addPane(0, staticPane1);
        pane.addPane(1, staticPane2);

        pane.deletePage(2);

        assertEquals(2, pane.getPages());
        assertSame(staticPane1, pane.getPanes(0).toArray(new Pane[0])[0]);
        assertSame(staticPane2, pane.getPanes(1).toArray(new Pane[0])[0]);
    }
}
