/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Keyable
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;

public interface StringRepresentable {
    public String getSerializedName();

    public static <E extends Enum<E>> Codec<E> fromEnum(Supplier<E[]> supplier, Function<String, E> function) {
        Enum[] enumArray = (Enum[])supplier.get();
        return ExtraCodecs.orCompressed(ExtraCodecs.stringResolverCodec(object -> ((StringRepresentable)object).getSerializedName(), function), ExtraCodecs.idResolverCodec(object -> ((Enum)object).ordinal(), n -> n >= 0 && n < enumArray.length ? enumArray[n] : null, -1));
    }

    public static Keyable keys(final StringRepresentable[] stringRepresentableArray) {
        return new Keyable(){

            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return Arrays.stream(stringRepresentableArray).map(StringRepresentable::getSerializedName).map(arg_0 -> dynamicOps.createString(arg_0));
            }
        };
    }
}

