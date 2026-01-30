package com.github.stefvanschie.inventoryframework.pane;

import com.github.stefvanschie.inventoryframework.pane.component.CycleButton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MasonryPaneTest {

    @Test
    void testCopy() {
        MasonryPane original = new MasonryPane(7, 5, 1, 1, Pane.Priority.LOW);
        original.setVisible(false);
        original.setOrientation(Orientable.Orientation.VERTICAL);

        original.addPane(new CycleButton(1, 1));
        original.addPane(new StaticPane(1, 1));
        original.addPane(new PaginatedPane(1, 1));
        original.addPane(new MasonryPane(1, 1));
        original.addPane(new OutlinePane(1, 1));

        MasonryPane copy = original.copy();

        assertNotSame(original, copy);

        assertEquals(original.getSlot(), copy.getSlot());
        assertEquals(original.getLength(), copy.getLength());
        assertEquals(original.getHeight(), copy.getHeight());
        assertEquals(original.getPriority(), copy.getPriority());
        assertEquals(original.isVisible(), copy.isVisible());
        assertEquals(original.getOrientation(), copy.getOrientation());
        assertEquals(original.getPanes().size(), copy.getPanes().size());
        assertEquals(original.getUUID(), copy.getUUID());
    }
}
