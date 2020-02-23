package com.github.stefvanschie.inventoryframework.pane.util;

import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A mask for {@link OutlinePane}s that specifies in which positions the items should be placed. Objects of this class
 * are immutable.
 *
 * @since 0.5.16
 */
public class Mask {

    /**
     * A two-dimensional array of booleans indicating which slots are 'enabled' and which ones are 'disabled'. This
     * two-dimensional array is constructed in a row-major order fashion.
     */
    private final boolean[][] mask;

    /**
     * Creates a mask based on the strings provided. Each string is a row for the mask and each character is a cell of
     * that row that indicates a slot for the mask. When the character is a 0, the slot will be considered 'disabled';
     * when the character is a 1, the slot will be considered 'enabled'. When there are any other characters in the
     * string, an {@link IllegalArgumentException} will be thrown. When multiple strings have a different length an
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param mask a var-arg of strings that represent this mask
     * @throws IllegalArgumentException when a string contains an illegal character or when strings have different
     * lengths
     * @since 0.5.16
     */
    public Mask(@NotNull String... mask) {
        this.mask = new boolean[mask.length][mask[0].length()];

        for (int row = 0; row < mask.length; row++) {
            int length = mask[row].length();

            if (length != this.mask[row].length) {
                throw new IllegalArgumentException("Lengths of each string should be equal");
            }

            for (int column = 0; column < length; column++) {
                char character = mask[row].charAt(column);

                if (character == '0') {
                    this.mask[row][column] = false;
                } else if (character == '1') {
                    this.mask[row][column] = true;
                } else {
                    throw new IllegalArgumentException("Strings may only contain '0' and '1'");
                }
            }
        }
    }

    /**
     * Returns the amount of slots in this mask that are 'enabled'.
     *
     * @return amount of enabled slots
     * @since 0.5.16
     */
    public int amountOfEnabledSlots() {
        int amount = 0;

        for (boolean[] row : mask) {
            for (boolean cell : row) {
                if (cell) {
                    amount++;
                }
            }
        }

        return amount;
    }

    /**
     * Gets the column of this mask at the specified index. The values indicate the state of the slots for that slot:
     * {@literal true} indicates that the slot is 'enabled'; {@literal false} indicates that the slot is 'disabled'. The
     * returned array is a copy of the original; modifications to the returned array will not be reflected in the mask.
     *
     * @param index the column index
     * @return the column of this mask
     * @since 0.5.16
     */
    public boolean[] getColumn(int index) {
        boolean[] column = new boolean[mask[0].length];

        for (int i = 0; i < getHeight(); i++) {
            column[i] = mask[i][index];
        }

        return column;
    }

    /**
     * Gets the row of this mask at the specified index. The values indicate the state of the slots for that slot:
     * {@literal true} indicates that the slot is 'enabled'; {@literal false} indicates that the slot is 'disabled'. The
     * returned array is a copy of the original; modifications to the returned array will not be reflected in the mask.
     *
     * @param index the row index
     * @return the row of this mask
     * @since 0.5.16
     */
    public boolean[] getRow(int index) {
        boolean[] row = mask[index];

        return Arrays.copyOf(row, row.length);
    }

    /**
     * Gets whether the slot at the specified row and column is 'enabled' or not. This returns {@literal true} if it is
     * 'enabled' and {@literal false} if it is 'disabled'.
     *
     * @param x the x coordinate of the slot
     * @param y the y coordinate of the slot
     * @return whether the slot is enabled or not
     * @since 0.5.16
     */
    public boolean isEnabled(int x, int y) {
        return mask[y][x];
    }

    /**
     * Gets the length of this mask
     *
     * @return the length
     * @since 0.5.16
     */
    public int getLength() {
        return mask[0].length;
    }

    /**
     * Gets the height of this mask
     *
     * @return the height
     * @since 0.5.16
     */
    public int getHeight() {
        return mask.length;
    }
}
