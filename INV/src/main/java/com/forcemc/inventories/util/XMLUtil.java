package com.forcemc.inventories.util;

import com.forcemc.inventories.exception.XMLLoadException;
import com.forcemc.inventories.exception.XMLReflectionException;
import org.bukkit.event.Event;
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
     * Loads an event consumer from the given instance and element
     *
     * @param instance the object instance
     * @param element the element
     * @param eventType the type of the event
     * @param name the name of the attribute
     * @return the consumer to be called on click
     * @param <T> the type of the event
     */
    @Nullable
    @Contract(pure = true)
    public static <T extends Event> Consumer<T> loadOnEventAttribute(@NotNull Object instance, @NotNull Element element,
                                                                     @NotNull Class<T> eventType, @NotNull String name) {
        String attribute = element.getAttribute(name);
        for (Method method : instance.getClass().getMethods()) {
            if (!method.getName().equals(attribute))
                continue;

            int parameterCount = method.getParameterCount();
            boolean eventParameter;
            if (parameterCount == 0) {
                eventParameter = false;
            } else if (parameterCount == 1 &&
                    eventType.isAssignableFrom(method.getParameterTypes()[0])) {
                eventParameter = true;
            } else {
                continue;
            }

            return event -> {
                try {
                    method.setAccessible(true);
                    if (eventParameter) {
                        method.invoke(instance, event);
                    } else {
                        method.invoke(instance);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new XMLReflectionException(e);
                }
            };
        }

        return null;
    }

    /**
     * Invokes the method by the given name on the given instance with the provided argument. The method should have
     * the exact name specified and the exact parameter as specified. If the method cannot be accessed or found, this
     * will throw an {@link XMLLoadException}.
     *
     * @param instance the instance on which to call the method
     * @param methodName the name of the method to invoke
     * @param argument the argument to provide for the invocation
     * @param parameter the parameter of the method
     * @since 0.10.3
     * @throws XMLLoadException if the method cannot be accessed or found
     */
    public static void invokeMethod(@NotNull Object instance, @NotNull String methodName, @NotNull Object argument,
                                    @NotNull Class<?> parameter) {
        try {
            Method method = instance.getClass().getMethod(methodName, parameter);

            method.setAccessible(true);
            method.invoke(instance, argument);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
            throw new XMLLoadException(exception);
        }
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
            throw new XMLLoadException(e);
        }
    }
}
