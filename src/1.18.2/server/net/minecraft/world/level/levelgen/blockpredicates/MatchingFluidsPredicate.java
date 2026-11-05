/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.StateTestingPredicate;
import net.minecraft.world.level.material.Fluid;

class MatchingFluidsPredicate
extends StateTestingPredicate {
    private final HolderSet<Fluid> fluids;
    public static final Codec<MatchingFluidsPredicate> CODEC = RecordCodecBuilder.create(instance -> MatchingFluidsPredicate.stateTestingCodec(instance).and((App)RegistryCodecs.homogeneousList(Registry.FLUID_REGISTRY).fieldOf("fluids").forGetter(matchingFluidsPredicate -> matchingFluidsPredicate.fluids)).apply((Applicative)instance, MatchingFluidsPredicate::new));

    public MatchingFluidsPredicate(Vec3i vec3i, HolderSet<Fluid> holderSet) {
        super(vec3i);
        this.fluids = holderSet;
    }

    @Override
    protected boolean test(BlockState blockState) {
        return blockState.getFluidState().is(this.fluids);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_FLUIDS;
    }
}

