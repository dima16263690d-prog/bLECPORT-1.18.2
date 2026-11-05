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
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StopAttackingIfTargetInvalid<E extends Mob>
extends Behavior<E> {
    private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;
    private final Predicate<LivingEntity> stopAttackingWhen;
    private final Consumer<E> onTargetErased;

    public StopAttackingIfTargetInvalid(Predicate<LivingEntity> predicate, Consumer<E> consumer) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, (Object)((Object)MemoryStatus.REGISTERED)));
        this.stopAttackingWhen = predicate;
        this.onTargetErased = consumer;
    }

    public StopAttackingIfTargetInvalid(Predicate<LivingEntity> predicate) {
        this(predicate, mob -> {});
    }

    public StopAttackingIfTargetInvalid(Consumer<E> consumer) {
        this((LivingEntity livingEntity) -> false, consumer);
    }

    public StopAttackingIfTargetInvalid() {
        this((LivingEntity livingEntity) -> false, mob -> {});
    }

    @Override
    protected void start(ServerLevel serverLevel, E e, long l) {
        LivingEntity livingEntity = this.getAttackTarget(e);
        if (!((LivingEntity)e).canAttack(livingEntity)) {
            this.clearAttackTarget(e);
            return;
        }
        if (StopAttackingIfTargetInvalid.isTiredOfTryingToReachTarget(e)) {
            this.clearAttackTarget(e);
            return;
        }
        if (this.isCurrentTargetDeadOrRemoved(e)) {
            this.clearAttackTarget(e);
            return;
        }
        if (this.isCurrentTargetInDifferentLevel(e)) {
            this.clearAttackTarget(e);
            return;
        }
        if (this.stopAttackingWhen.test(this.getAttackTarget(e))) {
            this.clearAttackTarget(e);
            return;
        }
    }

    private boolean isCurrentTargetInDifferentLevel(E e) {
        return this.getAttackTarget(e).level != ((Mob)e).level;
    }

    private LivingEntity getAttackTarget(E e) {
        return ((LivingEntity)e).getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    private static <E extends LivingEntity> boolean isTiredOfTryingToReachTarget(E e) {
        Optional<Long> optional = e.getBrain().getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        return optional.isPresent() && e.level.getGameTime() - optional.get() > 200L;
    }

    private boolean isCurrentTargetDeadOrRemoved(E e) {
        Optional<LivingEntity> optional = ((LivingEntity)e).getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        return optional.isPresent() && !optional.get().isAlive();
    }

    protected void clearAttackTarget(E e) {
        this.onTargetErased.accept(e);
        ((LivingEntity)e).getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }
}

