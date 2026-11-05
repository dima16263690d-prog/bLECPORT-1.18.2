/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class ClampedNormalInt
extends IntProvider {
    public static final Codec<ClampedNormalInt> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.FLOAT.fieldOf("mean").forGetter(clampedNormalInt -> Float.valueOf(clampedNormalInt.mean)), (App)Codec.FLOAT.fieldOf("deviation").forGetter(clampedNormalInt -> Float.valueOf(clampedNormalInt.deviation)), (App)Codec.INT.fieldOf("min_inclusive").forGetter(clampedNormalInt -> clampedNormalInt.min_inclusive), (App)Codec.INT.fieldOf("max_inclusive").forGetter(clampedNormalInt -> clampedNormalInt.max_inclusive)).apply((Applicative)instance, ClampedNormalInt::new)).comapFlatMap(clampedNormalInt -> {
        if (clampedNormalInt.max_inclusive < clampedNormalInt.min_inclusive) {
            return DataResult.error((String)("Max must be larger than min: [" + clampedNormalInt.min_inclusive + ", " + clampedNormalInt.max_inclusive + "]"));
        }
        return DataResult.success((Object)clampedNormalInt);
    }, Function.identity());
    private float mean;
    private float deviation;
    private int min_inclusive;
    private int max_inclusive;

    public static ClampedNormalInt of(float f, float f2, int n, int n2) {
        return new ClampedNormalInt(f, f2, n, n2);
    }

    private ClampedNormalInt(float f, float f2, int n, int n2) {
        this.mean = f;
        this.deviation = f2;
        this.min_inclusive = n;
        this.max_inclusive = n2;
    }

    @Override
    public int sample(Random random) {
        return ClampedNormalInt.sample(random, this.mean, this.deviation, this.min_inclusive, this.max_inclusive);
    }

    public static int sample(Random random, float f, float f2, float f3, float f4) {
        return (int)Mth.clamp(Mth.normal(random, f, f2), f3, f4);
    }

    @Override
    public int getMinValue() {
        return this.min_inclusive;
    }

    @Override
    public int getMaxValue() {
        return this.max_inclusive;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.CLAMPED_NORMAL;
    }

    public String toString() {
        return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min_inclusive + "-" + this.max_inclusive + "]";
    }
}

