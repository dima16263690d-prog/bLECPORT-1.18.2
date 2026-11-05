/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class DarkOakTreeGrower
extends AbstractMegaTreeGrower {
    @Override
    @Nullable
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(Random random, boolean bl) {
        return null;
    }

    @Override
    @Nullable
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredMegaFeature(Random random) {
        return TreeFeatures.DARK_OAK;
    }
}

