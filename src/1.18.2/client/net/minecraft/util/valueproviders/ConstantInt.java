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
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class ConstantInt
extends IntProvider {
    public static final ConstantInt ZERO = new ConstantInt(0);
    public static final Codec<ConstantInt> CODEC = Codec.either((Codec)Codec.INT, (Codec)RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("value").forGetter(constantInt -> constantInt.value)).apply((Applicative)instance, ConstantInt::new))).xmap(either -> (ConstantInt)either.map(ConstantInt::of, constantInt -> constantInt), constantInt -> Either.left((Object)constantInt.value));
    private final int value;

    public static ConstantInt of(int n) {
        if (n == 0) {
            return ZERO;
        }
        return new ConstantInt(n);
    }

    private ConstantInt(int n) {
        this.value = n;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public int sample(Random random) {
        return this.value;
    }

    @Override
    public int getMinValue() {
        return this.value;
    }

    @Override
    public int getMaxValue() {
        return this.value;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.CONSTANT;
    }

    public String toString() {
        return Integer.toString(this.value);
    }
}

