/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
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
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractWith<E extends LivingEntity, T extends LivingEntity>
extends Behavior<E> {
    private final int maxDist;
    private final float speedModifier;
    private final EntityType<? extends T> type;
    private final int interactionRangeSqr;
    private final Predicate<T> targetFilter;
    private final Predicate<E> selfFilter;
    private final MemoryModuleType<T> memory;

    public InteractWith(EntityType<? extends T> entityType, int n, Predicate<E> predicate, Predicate<T> predicate2, MemoryModuleType<T> memoryModuleType, float f, int n2) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, (Object)((Object)MemoryStatus.VALUE_PRESENT)));
        this.type = entityType;
        this.speedModifier = f;
        this.interactionRangeSqr = n * n;
        this.maxDist = n2;
        this.targetFilter = predicate2;
        this.selfFilter = predicate;
        this.memory = memoryModuleType;
    }

    public static <T extends LivingEntity> InteractWith<LivingEntity, T> of(EntityType<? extends T> entityType, int n, MemoryModuleType<T> memoryModuleType, float f, int n2) {
        return new InteractWith<LivingEntity, LivingEntity>(entityType, n, livingEntity -> true, livingEntity -> true, memoryModuleType, f, n2);
    }

    public static <T extends LivingEntity> InteractWith<LivingEntity, T> of(EntityType<? extends T> entityType, int n, Predicate<T> predicate, MemoryModuleType<T> memoryModuleType, float f, int n2) {
        return new InteractWith<LivingEntity, T>(entityType, n, livingEntity -> true, predicate, memoryModuleType, f, n2);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E e) {
        return this.selfFilter.test(e) && this.seesAtLeastOneValidTarget(e);
    }

    private boolean seesAtLeastOneValidTarget(E e) {
        NearestVisibleLivingEntities nearestVisibleLivingEntities = ((LivingEntity)e).getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
        return nearestVisibleLivingEntities.contains(this::isTargetValid);
    }

    private boolean isTargetValid(LivingEntity livingEntity) {
        return this.type.equals(livingEntity.getType()) && this.targetFilter.test(livingEntity);
    }

    @Override
    protected void start(ServerLevel serverLevel, E e, long l) {
        Brain<?> brain = ((LivingEntity)e).getBrain();
        Optional<NearestVisibleLivingEntities> optional = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        if (optional.isEmpty()) {
            return;
        }
        NearestVisibleLivingEntities nearestVisibleLivingEntities = optional.get();
        nearestVisibleLivingEntities.findClosest(livingEntity2 -> this.canInteract(e, (LivingEntity)livingEntity2)).ifPresent(livingEntity -> {
            brain.setMemory(this.memory, livingEntity);
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker((Entity)livingEntity, true));
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker((Entity)livingEntity, false), this.speedModifier, this.maxDist));
        });
    }

    private boolean canInteract(E e, LivingEntity livingEntity) {
        return this.type.equals(livingEntity.getType()) && livingEntity.distanceToSqr((Entity)e) <= (double)this.interactionRangeSqr && this.targetFilter.test(livingEntity);
    }
}

