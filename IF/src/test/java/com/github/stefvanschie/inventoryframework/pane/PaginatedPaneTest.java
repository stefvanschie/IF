package com.github.stefvanschie.inventoryframework.pane;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
    void testAddPaneNegative() {
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 1, 1);

        StaticPane staticPane = new StaticPane(0, 0, 1, 1);

        assertThrows(IllegalArgumentException.class, () -> paginatedPane.addPane(-1, staticPane));
    }

    @Test
    void testAddPaneExisting() {
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 1, 1);

        StaticPane staticPane1 = new StaticPane(0, 0, 1, 1);
        StaticPane staticPane2 = new StaticPane(0, 0, 1, 1);

        Set<? super Pane> elements = new HashSet<>();

        elements.add(staticPane1);
        elements.add(staticPane2);

        paginatedPane.addPane(0, staticPane1);

        assertDoesNotThrow(() -> {
            paginatedPane.addPane(0, staticPane2);

            Collection<Pane> panes = paginatedPane.getPanes(0);

            assertEquals(elements.size(), panes.size());
            assertTrue(elements.containsAll(panes));
        });
    }

    @Test
    void testAddPaneAfter() {
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 1, 1);

        StaticPane staticPane1 = new StaticPane(0, 0, 1, 1);
        StaticPane staticPane2 = new StaticPane(0, 0, 1, 1);

        paginatedPane.addPane(0, staticPane1);

        assertDoesNotThrow(() -> {
            paginatedPane.addPane(1, staticPane2);

            Collection<Pane> panes0 = paginatedPane.getPanes(0);

            assertEquals(1, panes0.size());
            assertEquals(staticPane1, panes0.iterator().next());

            Collection<Pane> panes1 = paginatedPane.getPanes(1);

            assertEquals(1, panes1.size());
            assertEquals(staticPane2, panes1.iterator().next());
        });
    }

    @Test
    void testAddPaneBeyond() {
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 1, 1);

        StaticPane staticPane1 = new StaticPane(0, 0, 1, 1);
        StaticPane staticPane2 = new StaticPane(0, 0, 1, 1);

        paginatedPane.addPane(0, staticPane1);

        assertThrows(IllegalArgumentException.class, () -> paginatedPane.addPane(2, staticPane2));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void testSetPageOutside(int index) {
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 1, 1);

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> paginatedPane.setPage(index));
    }

    @Test
    void testSetPage() {
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 1, 1);

        StaticPane staticPane1 = new StaticPane(0, 0, 1, 1);

        paginatedPane.addPage(staticPane1);

        assertDoesNotThrow(() -> paginatedPane.setPage(0));
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

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void testDeletePageNotExists(int index) {
        PaginatedPane pane = new PaginatedPane(0, 0, 1, 1);

        assertDoesNotThrow(() -> pane.deletePage(index));
    }
}
