/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class RarityFilter
extends PlacementFilter {
    public static final Codec<RarityFilter> CODEC = ExtraCodecs.POSITIVE_INT.fieldOf("chance").xmap(RarityFilter::new, rarityFilter -> rarityFilter.chance).codec();
    private final int chance;

    private RarityFilter(int n) {
        this.chance = n;
    }

    public static RarityFilter onAverageOnceEvery(int n) {
        return new RarityFilter(n);
    }

    @Override
    protected boolean shouldPlace(PlacementContext placementContext, Random random, BlockPos blockPos) {
        return random.nextFloat() < 1.0f / (float)this.chance;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.RARITY_FILTER;
    }
}

