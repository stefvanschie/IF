package com.forcemc.inventories.nms.v1_21.util;

import com.forcemc.inventories.adventuresupport.ComponentHolder;
import com.forcemc.inventories.adventuresupport.StringHolder;
import com.forcemc.inventories.adventuresupport.TextHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * A utility class for adding {@link TextHolder} support.
 *
 * @since 0.10.15
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
     * @since 0.10.15
     */
    @NotNull
    @Contract(pure = true)
    public static Component toComponent(@NotNull TextHolder holder) {
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
     * @since 0.10.15
     */
    @NotNull
    @Contract(pure = true)
    private static Component toComponent(@NotNull StringHolder holder) {
        return Component.literal(holder.asLegacyString());
    }
    
    /**
     * Converts the specified Adventure component holder to a vanilla component.
     *
     * @param holder the value to convert
     * @return the value as a vanilla component
     * @since 0.10.15
     */
    @NotNull
    @Contract(pure = true)
    private static Component toComponent(@NotNull ComponentHolder holder) {
        return Objects.requireNonNull(Component.Serializer.fromJson(holder.asJson(), HolderLookup.Provider.create(Stream.empty())));
    }
}
