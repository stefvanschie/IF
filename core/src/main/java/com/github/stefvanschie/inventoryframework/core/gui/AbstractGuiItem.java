package com.github.stefvanschie.inventoryframework.core.gui;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An item for in an inventory
 */
public class AbstractGuiItem {

    /**
     * List of item's properties
     */
    protected List<Object> properties;

    /**
     * Whether this item is visible or not
     */
    protected boolean visible;
    
    /**
     * Returns the list of properties
     *
     * @return the list of properties that belong to this gui item
     * @since 0.7.2
     */
    @Contract(pure = true)
    public List<Object> getProperties(){
        return properties;
    }
    
    /**
     * Sets the list of properties for this gui item
     *
     * @param properties list of new properties
     * @since 0.7.2
     */
    public void setProperties(@NotNull List<Object> properties){
        this.properties = properties;
    }

    /**
     * Returns whether or not this item is visible
     *
     * @return true if this item is visible, false otherwise
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the visibility of this item to the new visibility
     *
     * @param visible the new visibility
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
