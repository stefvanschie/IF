package com.github.stefvanschie.inventoryframework.pane.util;

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A mask for {@link PatternPane}s that specifies in which positions the items should be placed. Objects of this class
 * are immutable.
 *
 * @since 0.9.8
 */
public class Pattern {

    /**
     * A two-dimensional array of characters indicating which slot has which character. The characters are stored as
     * integers to properly support characters outside the 16-bit range. This means that, what would be a surrogate pair
     * in a string, is now a single number. This two-dimensional array is constructed in a row-major order fashion.
     */
    private final int @NotNull [] @NotNull [] pattern;

    /**
     * Creates a pattern based on the strings provided. Each string is a row for the pattern and each character is a
     * slot of that row. When multiple strings have a different length an {@link IllegalArgumentException} will be
     * thrown.
     *
     * Surrogate pairs in strings are treated as a single character and not as two. This means that a string with five
     * surrogate pairs, will count as having five characters, not ten.
     *
     * @param pattern a var-arg of strings that represent this pattern
     * @throws IllegalArgumentException when strings have different lengths
     * @since 0.9.8
     */
    public Pattern(@NotNull String @NotNull ... pattern) {
        int rows = pattern.length;
        boolean zeroRows = rows == 0;

        this.pattern = new int[rows][zeroRows ? 0 : pattern[0].codePointCount(0, pattern[0].length())];

        if (zeroRows) {
            return;
        }

        int globalLength = this.pattern[0].length;

        for (int index = 0; index < rows; index++) {
            String row = pattern[index];
            int length = row.codePointCount(0, row.length());

            if (length != globalLength) {
                throw new IllegalArgumentException(
                    "Rows have different lengths, row 1 has " + globalLength + " characters, but row " + index +
                    " has " + length + " characters"
                );
            }

            List<Integer> values = new ArrayList<>();

            row.codePoints().forEach(values::add);

            for (int column = 0; column < values.size(); column++) {
                this.pattern[index][column] = values.get(column);
            }
        }
    }

    /**
     * Creates a pattern based on the two-dimensional int array provided. Each array is a row for the pattern and each
     * index is a cell of that row that indicates a slot for the pattern.
     *
     * @param pattern a two-dimensional int array that represent this pattern
     * @since 0.9.8
     */
    private Pattern(int @NotNull [] @NotNull [] pattern) {
        this.pattern = pattern;
    }

    /**
     * Creates a new pattern with the specified height. If the new height is smaller than the previous height, the
     * excess values will be truncated. If the new height is longer than the previous height, additional values will be
     * added which are the same as the current bottom row. If the height is the same as the previous pattern, this will
     * simply return a new pattern identical to this one.
     *
     * @param height the new height of the pattern
     * @return a new pattern with the specified height
     * @since 0.9.8
     */
    @NotNull
    @Contract(pure = true)
    public Pattern setHeight(int height) {
        int[][] newRows = new int[height][getLength()];

        for (int index = 0; index < Math.min(height, getHeight()); index++) {
            System.arraycopy(pattern[index], 0, newRows[index], 0, pattern[index].length);
        }

        for (int index = Math.min(height, getHeight()); index < height; index++) {
            int[] previousRow = newRows[index - 1];

            newRows[index] = Arrays.copyOf(previousRow, previousRow.length);
        }

        return new Pattern(newRows);
    }

    /**
     * Creates a new pattern with the specified length. If the new length is smaller than the previous length, the excess
     * values will be truncated. If the new length is longer than the previous length, additional values will be added
     * which are the same as the rightmost value on a given row. If the length is the same as the previous pattern, this
     * will simply return a new pattern identical to this one.
     *
     * @param length the new length of the pattern
     * @return a new pattern with the specified length
     * @since 0.9.8
     */
    @NotNull
    @Contract(pure = true)
    public Pattern setLength(int length) {
        int[][] newRows = new int[getHeight()][length];

        for (int index = 0; index < pattern.length; index++) {
            int[] newRow = new int[length];
            int[] row = pattern[index];
            int minLength = Math.min(length, row.length);

            System.arraycopy(row, 0, newRow, 0, minLength);

            for (int column = minLength; column < length; column++) {
                newRow[column] = newRow[minLength - 1];
            }

            newRows[index] = newRow;
        }

        return new Pattern(newRows);
    }

    /**
     * Gets the column of this pattern at the specified index. The values indicate the character of the slots for that
     * slot. The returned array is a copy of the original; modifications to the returned array will not be reflected in
     * the pattern.
     *
     * @param index the column index
     * @return the column of this pattern
     * @throws IllegalArgumentException when the index is outside the pattern's range
     * @since 0.9.8
     */
    @Contract(pure = true)
    public int @NotNull [] getColumn(int index) {
        if (index >= getLength()) {
            throw new IllegalArgumentException("Index " + index + " exceeds pattern length");
        }

        int[] column = new int[pattern[0].length];

        for (int i = 0; i < getHeight(); i++) {
            column[i] = pattern[i][index];
        }

        return column;
    }

    /**
     * Gets whether the provided character is present in this pattern. For checking surrogate pairs, the pair should be
     * combined into one single number.
     *
     * @param character the character to look for
     * @return whether the provided character is present
     * @since 0.9.8
     */
    @Contract(pure = true)
    public boolean contains(int character) {
        for (int[] row : pattern) {
            for (int cell : row) {
                if (cell != character) {
                    continue;
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Gets the row of this mask at the specified index. The values indicate the character of the slots for that
     * slot. The returned array is a copy of the original; modifications to the returned array will not be reflected in
     * the pattern.
     *
     * @param index the row index
     * @return the row of this pattern
     * @throws IllegalArgumentException when the index is outside the pattern's range
     * @since 0.9.8
     */
    @Contract(pure = true)
    public int @NotNull [] getRow(int index) {
        if (index >= getHeight()) {
            throw new IllegalArgumentException("Index " + index + " exceeds pattern height");
        }

        int[] row = pattern[index];

        return Arrays.copyOf(row, row.length);
    }

    /**
     * Gets the character at the specified position. This returns an integer, instead of a character to properly account
     * for characters above the 16-bit range.
     *
     * @param x the x position
     * @param y the y position
     * @return the character at the specified position
     * @throws IllegalArgumentException when the position is out of range
     * @since 0.9.8
     */
    @Contract(pure = true)
    public int getCharacter(int x, int y) {
        if (x < 0 || x >= getLength() || y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Position " + x + ", " + y + " is out of range");
        }

        return this.pattern[y][x];
    }

    /**
     * Gets the length of this pattern
     *
     * @return the length
     * @since 0.9.8
     */
    @Contract(pure = true)
    public int getLength() {
        return pattern[0].length;
    }

    /**
     * Gets the height of this pattern
     *
     * @return the height
     * @since 0.9.8
     */
    @Contract(pure = true)
    public int getHeight() {
        return pattern.length;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Pattern pattern = (Pattern) object;

        return Arrays.deepEquals(this.pattern, pattern.pattern);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(pattern);
    }

    @Override
    public String toString() {
        return "Mask{" +
            "mask=" + Arrays.deepToString(pattern) +
            '}';
    }

    /**
     * Loads a pattern from an xml element.
     *
     * @param element the xml element
     * @return the loaded pattern
     * @since 0.9.8
     */
    @NotNull
    @Contract(pure = true)
    public static Pattern load(@NotNull Element element) {
        ArrayList<String> rows = new ArrayList<>();
        NodeList childNodes = element.getChildNodes();

        for (int itemIndex = 0; itemIndex < childNodes.getLength(); itemIndex++) {
            Node item = childNodes.item(itemIndex);

            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element child = (Element) item;
            String name = child.getNodeName();

            if (!name.equals("row")) {
                throw new XMLLoadException("Pattern contains unknown tag " + name);
            }

            rows.add(child.getTextContent());
        }

        return new Pattern(rows.toArray(new String[0]));
    }
}
