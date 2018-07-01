package com.github.stefvanschie.inventoryframework.util;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class XMLUtil {

    /**
     * Loads an onLocalClick consumer from the given instance and element
     *
     * @param instance the object instance
     * @param element the element
     * @return the consumer to be called on click
     */
    @Nullable
    @Contract(pure = true)
    public static Consumer<InventoryClickEvent> loadOnClickAttribute(Object instance, Element element) {
        for (Method method : instance.getClass().getMethods()) {
            if (!method.getName().equals(element.getAttribute("onLocalClick")))
                continue;

            int parameterCount = method.getParameterCount();

            if (parameterCount == 0) {
                return event -> {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                };
            } else if (parameterCount == 1 &&
                    InventoryClickEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                return event -> {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance, event);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                };
            }
        }

        return null;
    }
}
