/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.Vec3;

public class RamTarget<E extends PathfinderMob>
extends Behavior<E> {
    public static final int TIME_OUT_DURATION = 200;
    public static final float RAM_SPEED_FORCE_FACTOR = 1.65f;
    private final Function<E, UniformInt> getTimeBetweenRams;
    private final TargetingConditions ramTargeting;
    private final float speed;
    private final ToDoubleFunction<E> getKnockbackForce;
    private Vec3 ramDirection;
    private final Function<E, SoundEvent> getImpactSound;

    public RamTarget(Function<E, UniformInt> function, TargetingConditions targetingConditions, float f, ToDoubleFunction<E> toDoubleFunction, Function<E, SoundEvent> function2) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.RAM_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.RAM_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT)), 200);
        this.getTimeBetweenRams = function;
        this.ramTargeting = targetingConditions;
        this.speed = f;
        this.getKnockbackForce = toDoubleFunction;
        this.getImpactSound = function2;
        this.ramDirection = Vec3.ZERO;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        return pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        return pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET);
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        BlockPos blockPos = pathfinderMob.blockPosition();
        Brain<?> brain = pathfinderMob.getBrain();
        Vec3 vec3 = brain.getMemory(MemoryModuleType.RAM_TARGET).get();
        this.ramDirection = new Vec3((double)blockPos.getX() - vec3.x(), 0.0, (double)blockPos.getZ() - vec3.z()).normalize();
        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speed, 0));
    }

    @Override
    protected void tick(ServerLevel serverLevel, E e, long l) {
        List<LivingEntity> list = serverLevel.getNearbyEntities(LivingEntity.class, this.ramTargeting, (LivingEntity)e, ((Entity)e).getBoundingBox());
        Brain<?> brain = ((LivingEntity)e).getBrain();
        if (!list.isEmpty()) {
            LivingEntity livingEntity = list.get(0);
            livingEntity.hurt(DamageSource.mobAttack(e).setNoAggro(), (float)((LivingEntity)e).getAttributeValue(Attributes.ATTACK_DAMAGE));
            int n = ((LivingEntity)e).hasEffect(MobEffects.MOVEMENT_SPEED) ? ((LivingEntity)e).getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() + 1 : 0;
            int n2 = ((LivingEntity)e).hasEffect(MobEffects.MOVEMENT_SLOWDOWN) ? ((LivingEntity)e).getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() + 1 : 0;
            float f = 0.25f * (float)(n - n2);
            float f2 = Mth.clamp(((LivingEntity)e).getSpeed() * 1.65f, 0.2f, 3.0f) + f;
            float f3 = livingEntity.isDamageSourceBlocked(DamageSource.mobAttack(e)) ? 0.5f : 1.0f;
            livingEntity.knockback((double)(f3 * f2) * this.getKnockbackForce.applyAsDouble(e), this.ramDirection.x(), this.ramDirection.z());
            this.finishRam(serverLevel, e);
            serverLevel.playSound(null, (Entity)e, this.getImpactSound.apply(e), SoundSource.HOSTILE, 1.0f, 1.0f);
        } else {
            boolean bl;
            Optional<WalkTarget> optional = brain.getMemory(MemoryModuleType.WALK_TARGET);
            Optional<Vec3> optional2 = brain.getMemory(MemoryModuleType.RAM_TARGET);
            boolean bl2 = bl = !optional.isPresent() || !optional2.isPresent() || optional.get().getTarget().currentPosition().distanceTo(optional2.get()) < 0.25;
            if (bl) {
                this.finishRam(serverLevel, e);
            }
        }
    }

    protected void finishRam(ServerLevel serverLevel, E e) {
        serverLevel.broadcastEntityEvent((Entity)e, (byte)59);
        ((LivingEntity)e).getBrain().setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, this.getTimeBetweenRams.apply(e).sample(serverLevel.random));
        ((LivingEntity)e).getBrain().eraseMemory(MemoryModuleType.RAM_TARGET);
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (E)((PathfinderMob)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (PathfinderMob)livingEntity, l);
    }
}

