package com.github.stefvanschie.inventoryframework.nms.v1_21_9.util;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * A utility class for adding {@link TextHolder} support.
 *
 * @since 0.11.4
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
     * @since 0.11.4
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
     * @since 0.11.4
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
     * @since 0.11.4
     */
    @NotNull
    @Contract(pure = true)
    private static Component toComponent(@NotNull ComponentHolder holder) {
        Codec<? extends Component> codec = ComponentSerialization.CODEC;
        HolderLookup.Provider provider = HolderLookup.Provider.create(Stream.empty());
        RegistryOps<? super JsonElement> serializationContext = provider.createSerializationContext(JsonOps.INSTANCE);

        return codec.parse(serializationContext, holder.asJson()).getOrThrow(JsonParseException::new);
    }
}
