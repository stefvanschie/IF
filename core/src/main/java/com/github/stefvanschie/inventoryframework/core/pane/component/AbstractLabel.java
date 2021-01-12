package com.github.stefvanschie.inventoryframework.core.pane.component;

import com.github.stefvanschie.inventoryframework.core.font.AbstractFont;
import com.github.stefvanschie.inventoryframework.core.pane.AbstractOutlinePane;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A label for displaying text.
 *
 * @since 1.0.0
 */
public interface AbstractLabel extends AbstractOutlinePane {

    /**
     * Sets the text to be displayed in this label
     *
     * @param text the new text
     * @since 1.0.0
     */
    void setText(@NotNull String text);

    /**
     * Gets the text currently displayed in this label
     *
     * @return the text in this label
     * @since 1.0.0
     */
    @Contract(pure = true)
    @NotNull
    String getText();

    /**
     * Gets the character set currently used for the text in this label
     *
     * @return the character set
     * @since 1.0.0
     */
    @Contract(pure = true)
    @NotNull
    AbstractFont getFont();
}
