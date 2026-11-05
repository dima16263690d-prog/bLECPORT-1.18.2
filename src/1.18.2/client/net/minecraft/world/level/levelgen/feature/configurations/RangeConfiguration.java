/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class RangeConfiguration
implements FeatureConfiguration {
    public static final Codec<RangeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)HeightProvider.CODEC.fieldOf("height").forGetter(rangeConfiguration -> rangeConfiguration.height)).apply((Applicative)instance, RangeConfiguration::new));
    public final HeightProvider height;

    public RangeConfiguration(HeightProvider heightProvider) {
        this.height = heightProvider;
    }
}

