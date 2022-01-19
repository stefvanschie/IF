package com.github.stefvanschie.inventoryframework.pane.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import static org.junit.jupiter.api.Assertions.*;

public class MaskTest {

    @Test
    void testConstructorIncorrectCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new Mask("missingno."));
    }

    @Test
    void testConstructorInvalidLengths() {
        assertThrows(IllegalArgumentException.class, () -> new Mask(
            "0",
            "0000000000"
        ));
    }

    @Test
    void testConstructorAcceptEmpty() {
        assertDoesNotThrow((ThrowingSupplier<Mask>) Mask::new);
    }

    @Test
    void testAmountOfEnabledSlots() {
        assertEquals(4, new Mask(
            "1001",
            "1001"
        ).amountOfEnabledSlots());
    }

    @Test
    void testGetColumn() {
        assertArrayEquals(new boolean[] {true, false}, new Mask(
            "10",
            "00"
        ).getColumn(0));

        assertArrayEquals(new boolean[] {true, false}, new Mask(
            "1",
            "0"
        ).getColumn(0));
    }

    @Test
    void testGetRow() {
        assertArrayEquals(new boolean[] {true, false}, new Mask(
            "10",
            "00"
        ).getRow(0));
    }

    @Test
    void testIsEnabled() {
        assertTrue(new Mask(
            "10",
            "00"
        ).isEnabled(0, 0));
    }

    @Test
    void testGetLength() {
        assertEquals(2, new Mask(
            "10",
            "00",
            "01"
        ).getLength());
    }

    @Test
    void testGetHeight() {
        assertEquals(3, new Mask(
            "10",
            "00",
            "01"
        ).getHeight());
    }

    @Test
    void testSetLengthLonger() {
        assertEquals(new Mask(
           "101",
           "011"
        ), new Mask(
            "10",
            "01"
        ).setLength(3));
    }

    @Test
    void testSetLengthShorter() {
        assertEquals(new Mask(
            "1",
            "0"
        ), new Mask(
            "10",
            "01"
        ).setLength(1));
    }

    @Test
    void testSetLengthEqual() {
        assertEquals(new Mask(
            "10",
            "01"
        ), new Mask(
            "10",
            "01"
        ).setLength(2));
    }

    @Test
    void testSetHeightLonger() {
        assertEquals(new Mask(
            "10",
            "01",
            "11"
        ), new Mask(
            "10",
            "01"
        ).setHeight(3));
    }

    @Test
    void testSetHeightSmaller() {
        assertEquals(new Mask(
            "10"
        ), new Mask(
            "10",
            "01"
        ).setHeight(1));
    }

    @Test
    void testSetHeightEqual() {
        assertEquals(new Mask(
            "10",
            "01"
        ), new Mask(
            "10",
            "01"
        ).setHeight(2));
    }
}
