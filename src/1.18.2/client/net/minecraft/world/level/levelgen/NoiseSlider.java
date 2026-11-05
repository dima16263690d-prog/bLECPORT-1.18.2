/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public record NoiseSlider(double target, int size, int offset) {
    public static final Codec<NoiseSlider> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.DOUBLE.fieldOf("target").forGetter(noiseSlider -> noiseSlider.target), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("size").forGetter(noiseSlider -> noiseSlider.size), (App)Codec.INT.fieldOf("offset").forGetter(noiseSlider -> noiseSlider.offset)).apply((Applicative)instance, NoiseSlider::new));

    public double applySlide(double d, double d2) {
        if (this.size <= 0) {
            return d;
        }
        double d3 = (d2 - (double)this.offset) / (double)this.size;
        return Mth.clampedLerp(this.target, d, d3);
    }
}

