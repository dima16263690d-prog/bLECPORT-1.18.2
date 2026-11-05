/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CarverConfiguration
extends ProbabilityFeatureConfiguration {
    public static final MapCodec<CarverConfiguration> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").forGetter(carverConfiguration -> Float.valueOf(carverConfiguration.probability)), (App)HeightProvider.CODEC.fieldOf("y").forGetter(carverConfiguration -> carverConfiguration.y), (App)FloatProvider.CODEC.fieldOf("yScale").forGetter(carverConfiguration -> carverConfiguration.yScale), (App)VerticalAnchor.CODEC.fieldOf("lava_level").forGetter(carverConfiguration -> carverConfiguration.lavaLevel), (App)CarverDebugSettings.CODEC.optionalFieldOf("debug_settings", (Object)CarverDebugSettings.DEFAULT).forGetter(carverConfiguration -> carverConfiguration.debugSettings)).apply((Applicative)instance, CarverConfiguration::new));
    public final HeightProvider y;
    public final FloatProvider yScale;
    public final VerticalAnchor lavaLevel;
    public final CarverDebugSettings debugSettings;

    public CarverConfiguration(float f, HeightProvider heightProvider, FloatProvider floatProvider, VerticalAnchor verticalAnchor, CarverDebugSettings carverDebugSettings) {
        super(f);
        this.y = heightProvider;
        this.yScale = floatProvider;
        this.lavaLevel = verticalAnchor;
        this.debugSettings = carverDebugSettings;
    }
}

