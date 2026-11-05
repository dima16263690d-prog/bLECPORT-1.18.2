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

public record NoiseSamplingSettings(double xzScale, double yScale, double xzFactor, double yFactor) {
    private static final Codec<Double> SCALE_RANGE = Codec.doubleRange((double)0.001, (double)1000.0);
    public static final Codec<NoiseSamplingSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)SCALE_RANGE.fieldOf("xz_scale").forGetter(NoiseSamplingSettings::xzScale), (App)SCALE_RANGE.fieldOf("y_scale").forGetter(NoiseSamplingSettings::yScale), (App)SCALE_RANGE.fieldOf("xz_factor").forGetter(NoiseSamplingSettings::xzFactor), (App)SCALE_RANGE.fieldOf("y_factor").forGetter(NoiseSamplingSettings::yFactor)).apply((Applicative)instance, NoiseSamplingSettings::new));
}

