/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetLookAndInteract
extends Behavior<LivingEntity> {
    private final EntityType<?> type;
    private final int interactionRangeSqr;
    private final Predicate<LivingEntity> targetFilter;
    private final Predicate<LivingEntity> selfFilter;

    public SetLookAndInteract(EntityType<?> entityType, int n, Predicate<LivingEntity> predicate, Predicate<LivingEntity> predicate2) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.INTERACTION_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, (Object)((Object)MemoryStatus.VALUE_PRESENT)));
        this.type = entityType;
        this.interactionRangeSqr = n * n;
        this.targetFilter = predicate2;
        this.selfFilter = predicate;
    }

    public SetLookAndInteract(EntityType<?> entityType, int n) {
        this(entityType, n, livingEntity -> true, livingEntity -> true);
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
        return this.selfFilter.test(livingEntity) && this.getVisibleEntities(livingEntity).contains(this::isMatchingTarget);
    }

    @Override
    public void start(ServerLevel serverLevel, LivingEntity livingEntity2, long l) {
        super.start(serverLevel, livingEntity2, l);
        Brain<?> brain = livingEntity2.getBrain();
        brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).flatMap(nearestVisibleLivingEntities -> nearestVisibleLivingEntities.findClosest(livingEntity2 -> livingEntity2.distanceToSqr(livingEntity2) <= (double)this.interactionRangeSqr && this.isMatchingTarget((LivingEntity)livingEntity2))).ifPresent(livingEntity -> {
            brain.setMemory(MemoryModuleType.INTERACTION_TARGET, livingEntity);
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker((Entity)livingEntity, true));
        });
    }

    private boolean isMatchingTarget(LivingEntity livingEntity) {
        return this.type.equals(livingEntity.getType()) && this.targetFilter.test(livingEntity);
    }

    private NearestVisibleLivingEntities getVisibleEntities(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
    }
}

