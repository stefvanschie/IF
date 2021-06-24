package com.github.stefvanschie.inventoryframework.nms.v1_16_R2.util;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import net.minecraft.server.v1_16_R2.ChatComponentText;
import net.minecraft.server.v1_16_R2.IChatBaseComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class AdventureSupportUtil {
    
    private AdventureSupportUtil() {
        //private constructor to prevent construction
    }
    
    @NotNull
    @Contract(pure = true)
    public static IChatBaseComponent toComponent(@NotNull TextHolder holder) {
        if (holder instanceof StringHolder) {
            return toComponent((StringHolder) holder);
        } else {
            return toComponent((ComponentHolder) holder);
        }
    }
    
    @NotNull
    @Contract(pure = true)
    private static IChatBaseComponent toComponent(@NotNull StringHolder holder) {
        return new ChatComponentText(holder.asLegacyString());
    }
    
    @NotNull
    @Contract(pure = true)
    private static IChatBaseComponent toComponent(@NotNull ComponentHolder holder) {
        return Objects.requireNonNull(IChatBaseComponent.ChatSerializer.a(holder.asJson()));
    }
}
