/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviderType;

public class ConstantFloat
extends FloatProvider {
    public static final ConstantFloat ZERO = new ConstantFloat(0.0f);
    public static final Codec<ConstantFloat> CODEC = Codec.either((Codec)Codec.FLOAT, (Codec)RecordCodecBuilder.create(instance -> instance.group((App)Codec.FLOAT.fieldOf("value").forGetter(constantFloat -> Float.valueOf(constantFloat.value))).apply((Applicative)instance, ConstantFloat::new))).xmap(either -> (ConstantFloat)either.map(ConstantFloat::of, constantFloat -> constantFloat), constantFloat -> Either.left((Object)Float.valueOf(constantFloat.value)));
    private final float value;

    public static ConstantFloat of(float f) {
        if (f == 0.0f) {
            return ZERO;
        }
        return new ConstantFloat(f);
    }

    private ConstantFloat(float f) {
        this.value = f;
    }

    public float getValue() {
        return this.value;
    }

    @Override
    public float sample(Random random) {
        return this.value;
    }

    @Override
    public float getMinValue() {
        return this.value;
    }

    @Override
    public float getMaxValue() {
        return this.value + 1.0f;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.CONSTANT;
    }

    public String toString() {
        return Float.toString(this.value);
    }
}

