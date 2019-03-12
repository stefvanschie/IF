package com.github.stefvanschie.inventoryframework.pane.component;

import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.font.util.Font;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A label for displaying text.
 *
 * @since 0.5.0
 */
public class Label extends OutlinePane {

    /**
     * The character set used for displaying the characters in this label
     */
    @NotNull
    private Font font;

    /**
     * The text to be displayed
     */
    @NotNull
    private String text;

    /**
     * Creates a new label
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the width
     * @param priority the priority
     * @param font the character set
     * @since 0.5.0
     */
    public Label(int x, int y, int length, int height, @NotNull Priority priority, @NotNull Font font) {
        this(x, y, length, height, font);

        setPriority(priority);
    }

    /**
     * Creates a new label
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the width
     * @param font the character set
     * @since 0.5.0
     */
    public Label(int x, int y, int length, int height, @NotNull Font font) {
        this(length, height, font);

        this.x = x;
        this.y = y;
    }

    /**
     * Creates a new label
     *
     * @param length the length
     * @param height the width
     * @param font the character set
     * @since 0.5.0
     */
    public Label(int length, int height, @NotNull Font font) {
        super(length, height);

        this.font = font;
        this.text = "";
    }

    /**
     * Sets the text to be displayed in this label
     *
     * @param text the new text
     * @since 0.5.0
     */
    public void setText(@NotNull String text) {
        this.text = text;

        clear();

        for (char character : text.toCharArray()) {
            ItemStack item = font.toItem(character);

            if (item == null) {
                item = font.toItem(Character.toUpperCase(character));
            }

            if (item == null) {
                item = font.toItem(Character.toLowerCase(character));
            }

            if (item == null) {
                item = font.getDefaultItem();
            }

            addItem(new GuiItem(item));
        }
    }

    /**
     * Gets the text currently displayed in this label
     *
     * @return the text in this label
     * @since 0.5.0
     */
    @Contract(pure = true)
    @NotNull
    public String getText() {
        return text;
    }

    /**
     * Gets the character set currently used for the text in this label
     *
     * @return the character set
     * @since 0.5.0
     */
    @Contract(pure = true)
    @NotNull
    public Font getFont() {
        return font;
    }
}
