package com.github.stefvanschie.inventoryframework.util;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.lang.reflect.Field;
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

    /**
     * Sets a field from the given instance and element to the specified value
     *
     * @param instance the class instance the field is located in
     * @param element the element from which the field is specified
     * @param value the field's new value
     */
    public static void loadFieldAttribute(@NotNull Object instance, @NotNull Element element, @Nullable Object value) {
        try {
            Field field = instance.getClass().getField(element.getAttribute("field"));

            field.setAccessible(true);
            field.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
