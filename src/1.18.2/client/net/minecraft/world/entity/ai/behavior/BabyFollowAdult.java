/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class BabyFollowAdult<E extends AgeableMob>
extends Behavior<E> {
    private final UniformInt followRange;
    private final Function<LivingEntity, Float> speedModifier;

    public BabyFollowAdult(UniformInt uniformInt, float f) {
        this(uniformInt, livingEntity -> Float.valueOf(f));
    }

    public BabyFollowAdult(UniformInt uniformInt, Function<LivingEntity, Float> function) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)));
        this.followRange = uniformInt;
        this.speedModifier = function;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E e) {
        if (!((AgeableMob)e).isBaby()) {
            return false;
        }
        AgeableMob ageableMob = this.getNearestAdult(e);
        return ((Entity)e).closerThan(ageableMob, this.followRange.getMaxValue() + 1) && !((Entity)e).closerThan(ageableMob, this.followRange.getMinValue());
    }

    @Override
    protected void start(ServerLevel serverLevel, E e, long l) {
        BehaviorUtils.setWalkAndLookTargetMemories(e, this.getNearestAdult(e), this.speedModifier.apply((LivingEntity)e).floatValue(), this.followRange.getMinValue() - 1);
    }

    private AgeableMob getNearestAdult(E e) {
        return ((LivingEntity)e).getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT).get();
    }
}

