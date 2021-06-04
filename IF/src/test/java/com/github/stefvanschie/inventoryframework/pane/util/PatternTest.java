package com.github.stefvanschie.inventoryframework.pane.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import static org.junit.jupiter.api.Assertions.*;

public class PatternTest {

    @Test
    void testGetCharacter() {
        Pattern pattern  = new Pattern(
            "12",
            "34"
        );

        assertEquals('4', pattern.getCharacter(1, 1));
    }

    @Test
    void testCodePoints() {
        Pattern pattern = new Pattern("\uD800\uDC00"); //surrogate pair for U+10000; Linear B Syllable B008 A

        assertEquals(1, pattern.getLength());
    }

    @Test
    void testConstructorInvalidLengths() {
        assertThrows(IllegalArgumentException.class, () -> new Pattern(
            "0",
            "0000000000"
        ));
    }

    @Test
    void testConstructorAcceptEmpty() {
        assertDoesNotThrow((ThrowingSupplier<Pattern>) Pattern::new);
    }

    @Test
    void testGetColumn() {
        assertArrayEquals(new int[] {'1', '0'}, new Pattern(
            "10",
            "00"
        ).getColumn(0));
    }

    @Test
    void testGetRow() {
        assertArrayEquals(new int[] {'1', '0'}, new Pattern(
            "10",
            "00"
        ).getRow(0));
    }

    @Test
    void testGetLength() {
        assertEquals(2, new Pattern(
            "10",
            "00",
            "01"
        ).getLength());
    }

    @Test
    void testGetHeight() {
        assertEquals(3, new Pattern(
            "10",
            "00",
            "01"
        ).getHeight());
    }

    @Test
    void testSetLengthLonger() {
        assertEquals(new Pattern(
            "100",
            "011"
        ), new Pattern(
            "10",
            "01"
        ).setLength(3));
    }

    @Test
    void testSetLengthShorter() {
        assertEquals(new Pattern(
            "1",
            "0"
        ), new Pattern(
            "10",
            "01"
        ).setLength(1));
    }

    @Test
    void testSetLengthEqual() {
        assertEquals(new Pattern(
            "10",
            "01"
        ), new Pattern(
            "10",
            "01"
        ).setLength(2));
    }

    @Test
    void testSetHeightLonger() {
        assertEquals(new Pattern(
            "10",
            "01",
            "01"
        ), new Pattern(
            "10",
            "01"
        ).setHeight(3));
    }

    @Test
    void testSetHeightSmaller() {
        assertEquals(new Pattern(
            "10"
        ), new Pattern(
            "10",
            "01"
        ).setHeight(1));
    }

    @Test
    void testSetHeightEqual() {
        assertEquals(new Pattern(
            "10",
            "01"
        ), new Pattern(
            "10",
            "01"
        ).setHeight(2));
    }
}
