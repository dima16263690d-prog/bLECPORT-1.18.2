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
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProviderType;

public abstract class IntProvider {
    private static final Codec<Either<Integer, IntProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either((Codec)Codec.INT, (Codec)Registry.INT_PROVIDER_TYPES.byNameCodec().dispatch(IntProvider::getType, IntProviderType::codec));
    public static final Codec<IntProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(either -> (IntProvider)either.map(ConstantInt::of, intProvider -> intProvider), intProvider -> intProvider.getType() == IntProviderType.CONSTANT ? Either.left((Object)((ConstantInt)intProvider).getValue()) : Either.right((Object)intProvider));
    public static final Codec<IntProvider> NON_NEGATIVE_CODEC = IntProvider.codec(0, Integer.MAX_VALUE);
    public static final Codec<IntProvider> POSITIVE_CODEC = IntProvider.codec(1, Integer.MAX_VALUE);

    public static Codec<IntProvider> codec(int n, int n2) {
        Function<IntProvider, DataResult> function = intProvider -> {
            if (intProvider.getMinValue() < n) {
                return DataResult.error((String)("Value provider too low: " + n + " [" + intProvider.getMinValue() + "-" + intProvider.getMaxValue() + "]"));
            }
            if (intProvider.getMaxValue() > n2) {
                return DataResult.error((String)("Value provider too high: " + n2 + " [" + intProvider.getMinValue() + "-" + intProvider.getMaxValue() + "]"));
            }
            return DataResult.success((Object)intProvider);
        };
        return CODEC.flatXmap(function, function);
    }

    public abstract int sample(Random var1);

    public abstract int getMinValue();

    public abstract int getMaxValue();

    public abstract IntProviderType<?> getType();
}

