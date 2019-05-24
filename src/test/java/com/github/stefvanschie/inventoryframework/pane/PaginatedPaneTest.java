package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.GuiItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PaginatedPaneTest {

    @Test
    public void testPopulateWithItemStacksPageAmount() {
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 9, 6);

        paginatedPane.populateWithItemStacks(Stream.generate(() -> Material.values()[0])
            .limit(paginatedPane.getLength() * paginatedPane.getHeight())
            .map(ItemStack::new)
            .collect(Collectors.toList()));

        assertEquals(1, paginatedPane.getPages());
    }

    @Test
    public void testPopulateWithGuiItemsPageAmount() {
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 9, 6);

        paginatedPane.populateWithGuiItems(Stream.generate(() -> Material.values()[0])
            .limit(paginatedPane.getLength() * paginatedPane.getHeight())
            .map(material -> new GuiItem(new ItemStack(material)))
            .collect(Collectors.toList()));

        assertEquals(1, paginatedPane.getPages());
    }

    @Test
    public void testPopulateWithItemStacksInnerPaneDimension() {
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 9, 6);

        paginatedPane.populateWithItemStacks(Collections.singletonList(new ItemStack(Material.values()[0])));

        paginatedPane.getPanes().forEach(pane -> {
            assertEquals(0, pane.getX());
            assertEquals(0, pane.getY());
            assertEquals(paginatedPane.getLength(), pane.getLength());
            assertEquals(paginatedPane.getHeight(), pane.getHeight());
        });
    }

    @Test
    public void testPopulateWithGuiItemsInnerPaneDimension() {
        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 9, 6);

        paginatedPane.populateWithGuiItems(Collections.singletonList(new GuiItem(new ItemStack(Material.values()[0]))));

        paginatedPane.getPanes().forEach(pane -> {
            assertEquals(0, pane.getX());
            assertEquals(0, pane.getY());
            assertEquals(paginatedPane.getLength(), pane.getLength());
            assertEquals(paginatedPane.getHeight(), pane.getHeight());
        });
    }
}
