package com.github.stefvanschie.inventoryframework.annotation.click;

import com.github.stefvanschie.inventoryframework.annotation.exception.InvalidParametersException;
import com.github.stefvanschie.inventoryframework.annotation.exception.MultipleAnnotationsException;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

/**
 * This annotation indicates the method that will be the global click handler for the {@link Gui}. The annotation must
 * be placed on a method declaration. The method must occur in a class which extends a subtype of {@link Gui}; this may
 * not be an enclosing, enclosed, or parent class. The gui will automatically have its global click handler set to this
 * method. This handler may be overwritten at a later moment with {@link Gui#setOnGlobalClick(Consumer)}. If multiple
 * methods within this class have this annotation, a {@link MultipleAnnotationsException} will be thrown.
 * <p>
 * The referenced method may either have no parameters declared, or one parameter declared which equals or is a
 * supertype of {@link InventoryClickEvent}. If it has one parameter declared, the argument upon this method being
 * called will be the event that was fired during the corresponding click.
 * <p>
 * If this method does not have the correct parameters, {@link InvalidParametersException} will be thrown.
 *
 * @since 0.12.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GlobalClickHandler {}
