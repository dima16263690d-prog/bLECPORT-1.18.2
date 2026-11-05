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
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromLookTarget
extends Behavior<LivingEntity> {
    private final Function<LivingEntity, Float> speedModifier;
    private final int closeEnoughDistance;
    private final Predicate<LivingEntity> canSetWalkTargetPredicate;

    public SetWalkTargetFromLookTarget(float f, int n) {
        this((LivingEntity livingEntity) -> true, livingEntity -> Float.valueOf(f), n);
    }

    public SetWalkTargetFromLookTarget(Predicate<LivingEntity> predicate, Function<LivingEntity, Float> function, int n) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT)));
        this.speedModifier = function;
        this.closeEnoughDistance = n;
        this.canSetWalkTargetPredicate = predicate;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
        return this.canSetWalkTargetPredicate.test(livingEntity);
    }

    @Override
    protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        Brain<?> brain = livingEntity.getBrain();
        PositionTracker positionTracker = brain.getMemory(MemoryModuleType.LOOK_TARGET).get();
        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(positionTracker, this.speedModifier.apply(livingEntity).floatValue(), this.closeEnoughDistance));
    }
}

