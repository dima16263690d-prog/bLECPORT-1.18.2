/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseSamplingSettings;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class BlendedNoise
implements DensityFunction.SimpleFunction {
    public static final BlendedNoise UNSEEDED = new BlendedNoise(new XoroshiroRandomSource(0L), new NoiseSamplingSettings(1.0, 1.0, 80.0, 160.0), 4, 8);
    public static final Codec<BlendedNoise> CODEC = Codec.unit((Object)UNSEEDED);
    private final PerlinNoise minLimitNoise;
    private final PerlinNoise maxLimitNoise;
    private final PerlinNoise mainNoise;
    private final double xzScale;
    private final double yScale;
    private final double xzMainScale;
    private final double yMainScale;
    private final int cellWidth;
    private final int cellHeight;
    private final double maxValue;

    private BlendedNoise(PerlinNoise perlinNoise, PerlinNoise perlinNoise2, PerlinNoise perlinNoise3, NoiseSamplingSettings noiseSamplingSettings, int n, int n2) {
        this.minLimitNoise = perlinNoise;
        this.maxLimitNoise = perlinNoise2;
        this.mainNoise = perlinNoise3;
        this.xzScale = 684.412 * noiseSamplingSettings.xzScale();
        this.yScale = 684.412 * noiseSamplingSettings.yScale();
        this.xzMainScale = this.xzScale / noiseSamplingSettings.xzFactor();
        this.yMainScale = this.yScale / noiseSamplingSettings.yFactor();
        this.cellWidth = n;
        this.cellHeight = n2;
        this.maxValue = perlinNoise.maxBrokenValue(this.yScale);
    }

    public BlendedNoise(RandomSource randomSource, NoiseSamplingSettings noiseSamplingSettings, int n, int n2) {
        this(PerlinNoise.createLegacyForBlendedNoise(randomSource, IntStream.rangeClosed(-15, 0)), PerlinNoise.createLegacyForBlendedNoise(randomSource, IntStream.rangeClosed(-15, 0)), PerlinNoise.createLegacyForBlendedNoise(randomSource, IntStream.rangeClosed(-7, 0)), noiseSamplingSettings, n, n2);
    }

    @Override
    public double compute(DensityFunction.FunctionContext functionContext) {
        int n = Math.floorDiv(functionContext.blockX(), this.cellWidth);
        int n2 = Math.floorDiv(functionContext.blockY(), this.cellHeight);
        int n3 = Math.floorDiv(functionContext.blockZ(), this.cellWidth);
        double d = 0.0;
        double d2 = 0.0;
        double d3 = 0.0;
        boolean bl = true;
        double d4 = 1.0;
        for (int i = 0; i < 8; ++i) {
            ImprovedNoise improvedNoise = this.mainNoise.getOctaveNoise(i);
            if (improvedNoise != null) {
                d3 += improvedNoise.noise(PerlinNoise.wrap((double)n * this.xzMainScale * d4), PerlinNoise.wrap((double)n2 * this.yMainScale * d4), PerlinNoise.wrap((double)n3 * this.xzMainScale * d4), this.yMainScale * d4, (double)n2 * this.yMainScale * d4) / d4;
            }
            d4 /= 2.0;
        }
        double d5 = (d3 / 10.0 + 1.0) / 2.0;
        boolean bl2 = d5 >= 1.0;
        boolean bl3 = d5 <= 0.0;
        d4 = 1.0;
        for (int i = 0; i < 16; ++i) {
            ImprovedNoise improvedNoise;
            double d6 = PerlinNoise.wrap((double)n * this.xzScale * d4);
            double d7 = PerlinNoise.wrap((double)n2 * this.yScale * d4);
            double d8 = PerlinNoise.wrap((double)n3 * this.xzScale * d4);
            double d9 = this.yScale * d4;
            if (!bl2 && (improvedNoise = this.minLimitNoise.getOctaveNoise(i)) != null) {
                d += improvedNoise.noise(d6, d7, d8, d9, (double)n2 * d9) / d4;
            }
            if (!bl3 && (improvedNoise = this.maxLimitNoise.getOctaveNoise(i)) != null) {
                d2 += improvedNoise.noise(d6, d7, d8, d9, (double)n2 * d9) / d4;
            }
            d4 /= 2.0;
        }
        return Mth.clampedLerp(d / 512.0, d2 / 512.0, d5) / 128.0;
    }

    @Override
    public double minValue() {
        return -this.maxValue();
    }

    @Override
    public double maxValue() {
        return this.maxValue;
    }

    @VisibleForTesting
    public void parityConfigString(StringBuilder stringBuilder) {
        stringBuilder.append("BlendedNoise{minLimitNoise=");
        this.minLimitNoise.parityConfigString(stringBuilder);
        stringBuilder.append(", maxLimitNoise=");
        this.maxLimitNoise.parityConfigString(stringBuilder);
        stringBuilder.append(", mainNoise=");
        this.mainNoise.parityConfigString(stringBuilder);
        stringBuilder.append(String.format(", xzScale=%.3f, yScale=%.3f, xzMainScale=%.3f, yMainScale=%.3f, cellWidth=%d, cellHeight=%d", this.xzScale, this.yScale, this.xzMainScale, this.yMainScale, this.cellWidth, this.cellHeight)).append('}');
    }

    @Override
    public Codec<? extends DensityFunction> codec() {
        return CODEC;
    }
}

