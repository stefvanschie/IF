package com.github.stefvanschie.inventoryframework.nms.v1_17_R1.util;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class AdventureSupportUtil {
    
    private AdventureSupportUtil() {
        //private constructor to prevent construction
    }
    
    @NotNull
    @Contract(pure = true)
    public static Component toComponent(@NotNull TextHolder holder) {
        if (holder instanceof StringHolder) {
            return toComponent((StringHolder) holder);
        } else {
            return toComponent((ComponentHolder) holder);
        }
    }
    
    @NotNull
    @Contract(pure = true)
    private static Component toComponent(@NotNull StringHolder holder) {
        return new TextComponent(holder.asLegacyString());
    }
    
    @NotNull
    @Contract(pure = true)
    private static Component toComponent(@NotNull ComponentHolder holder) {
        return Objects.requireNonNull(Component.Serializer.fromJson(holder.asJson()));
    }
}
