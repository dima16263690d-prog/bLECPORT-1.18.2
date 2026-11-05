/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.StateTestingPredicate;

public class SolidPredicate
extends StateTestingPredicate {
    public static final Codec<SolidPredicate> CODEC = RecordCodecBuilder.create(instance -> SolidPredicate.stateTestingCodec(instance).apply((Applicative)instance, SolidPredicate::new));

    public SolidPredicate(Vec3i vec3i) {
        super(vec3i);
    }

    @Override
    protected boolean test(BlockState blockState) {
        return blockState.getMaterial().isSolid();
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.SOLID;
    }
}

