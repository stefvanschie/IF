package com.github.stefvanschie.inventoryframework.nms.v1_15_R1.util;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A utility class for adding {@link TextHolder} support.
 *
 * @since $ADVENTURE-SUPPORT-SINCE$
 */
public final class TextHolderUtil {
    
    private TextHolderUtil() {
        //private constructor to prevent construction
    }
    
    /**
     * Converts the specified value to a vanilla component.
     *
     * @param holder the value to convert
     * @return the value as a vanilla component
     * @since $ADVENTURE-SUPPORT-SINCE$
     */
    @NotNull
    @Contract(pure = true)
    public static IChatBaseComponent toComponent(@NotNull TextHolder holder) {
        if (holder instanceof StringHolder) {
            return toComponent((StringHolder) holder);
        } else {
            return toComponent((ComponentHolder) holder);
        }
    }
    
    /**
     * Converts the specified legacy string holder to a vanilla component.
     *
     * @param holder the value to convert
     * @return the value as a vanilla component
     * @since $ADVENTURE-SUPPORT-SINCE$
     */
    @NotNull
    @Contract(pure = true)
    private static IChatBaseComponent toComponent(@NotNull StringHolder holder) {
        return new ChatComponentText(holder.asLegacyString());
    }
    
    /**
     * Converts the specified Adventure component holder to a vanilla component.
     *
     * @param holder the value to convert
     * @return the value as a vanilla component
     * @since $ADVENTURE-SUPPORT-SINCE$
     */
    @NotNull
    @Contract(pure = true)
    private static IChatBaseComponent toComponent(@NotNull ComponentHolder holder) {
        return Objects.requireNonNull(IChatBaseComponent.ChatSerializer.a(holder.asJson()));
    }
}
