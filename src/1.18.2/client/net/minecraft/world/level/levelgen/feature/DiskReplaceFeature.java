/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.levelgen.feature.BaseDiskFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskReplaceFeature
extends BaseDiskFeature {
    public DiskReplaceFeature(Codec<DiskConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DiskConfiguration> featurePlaceContext) {
        if (!featurePlaceContext.level().getFluidState(featurePlaceContext.origin()).is(FluidTags.WATER)) {
            return false;
        }
        return super.place(featurePlaceContext);
    }
}

