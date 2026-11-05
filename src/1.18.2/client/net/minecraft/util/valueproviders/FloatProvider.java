/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProviderType;

public abstract class FloatProvider {
    private static final Codec<Either<Float, FloatProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either((Codec)Codec.FLOAT, (Codec)Registry.FLOAT_PROVIDER_TYPES.byNameCodec().dispatch(FloatProvider::getType, FloatProviderType::codec));
    public static final Codec<FloatProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(either -> (FloatProvider)either.map(ConstantFloat::of, floatProvider -> floatProvider), floatProvider -> floatProvider.getType() == FloatProviderType.CONSTANT ? Either.left((Object)Float.valueOf(((ConstantFloat)floatProvider).getValue())) : Either.right((Object)floatProvider));

    public static Codec<FloatProvider> codec(float f, float f2) {
        Function<FloatProvider, DataResult> function = floatProvider -> {
            if (floatProvider.getMinValue() < f) {
                return DataResult.error((String)("Value provider too low: " + f + " [" + floatProvider.getMinValue() + "-" + floatProvider.getMaxValue() + "]"));
            }
            if (floatProvider.getMaxValue() > f2) {
                return DataResult.error((String)("Value provider too high: " + f2 + " [" + floatProvider.getMinValue() + "-" + floatProvider.getMaxValue() + "]"));
            }
            return DataResult.success((Object)floatProvider);
        };
        return CODEC.flatXmap(function, function);
    }

    public abstract float sample(Random var1);

    public abstract float getMinValue();

    public abstract float getMaxValue();

    public abstract FloatProviderType<?> getType();
}

