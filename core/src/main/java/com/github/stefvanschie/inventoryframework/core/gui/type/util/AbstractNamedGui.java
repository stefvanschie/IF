package com.github.stefvanschie.inventoryframework.core.gui.type.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface AbstractNamedGui extends AbstractGui {

    /**
     * Returns the title of this gui
     *
     * @return the title
     * @since 0.8.0
     */
    @NotNull
    @Contract(pure = true)
    String getTitle();
}
