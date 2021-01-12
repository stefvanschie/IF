package com.github.stefvanschie.inventoryframework.core.pane;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A pane for panes that should be spread out over multiple pages
 *
 * @since 1.0.0
 */
public interface AbstractPaginatedPane extends AbstractPane {

    /**
     * Returns the current page
     *
     * @return the current page
     * @since 1.0.0
     */
    int getPage();

    /**
     * Returns the amount of pages
     *
     * @return the amount of pages
     * @since 1.0.0
     */
    int getPages();

    /**
     * Sets the current displayed page
     *
     * @param page the page
     * @since 1.0.0
     */
    void setPage(int page);

    /**
     * Gets all the panes from inside the specified page of this pane. If the specified page is not existent, this
     * method will throw an {@link IllegalArgumentException}. If the specified page is existent, but doesn't
     * have any panes, the returned collection will be empty. The returned collection is unmodifiable. The returned
     * collection is not synchronized and no guarantees should be made as to the safety of concurrently accessing the
     * returned collection. If synchronized behaviour should be allowed, the returned collection must be synchronized
     * externally.
     *
     * @param page the panes of this page will be returned
     * @return a collection of panes belonging to the specified page
     * @since 0.5.13
     * @throws IllegalArgumentException if the page does not exist
     */
    @NotNull
    @Contract(pure = true)
    Collection<? extends AbstractPane> getPanes(int page);
}
