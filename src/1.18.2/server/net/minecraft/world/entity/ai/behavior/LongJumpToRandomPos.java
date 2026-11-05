/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LongJumpToRandomPos<E extends Mob>
extends Behavior<E> {
    private static final int FIND_JUMP_TRIES = 20;
    private static final int PREPARE_JUMP_DURATION = 40;
    private static final int MIN_PATHFIND_DISTANCE_TO_VALID_JUMP = 8;
    public static final int TIME_OUT_DURATION = 200;
    private final UniformInt timeBetweenLongJumps;
    private final int maxLongJumpHeight;
    private final int maxLongJumpWidth;
    private final float maxJumpVelocity;
    private final List<PossibleJump> jumpCandidates = new ArrayList<PossibleJump>();
    private Optional<Vec3> initialPosition = Optional.empty();
    private Optional<PossibleJump> chosenJump = Optional.empty();
    private int findJumpTries;
    private long prepareJumpStart;
    private Function<E, SoundEvent> getJumpSound;

    public LongJumpToRandomPos(UniformInt uniformInt, int n, int n2, float f, Function<E, SoundEvent> function) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.LONG_JUMP_MID_JUMP, (Object)((Object)MemoryStatus.VALUE_ABSENT)), 200);
        this.timeBetweenLongJumps = uniformInt;
        this.maxLongJumpHeight = n;
        this.maxLongJumpWidth = n2;
        this.maxJumpVelocity = f;
        this.getJumpSound = function;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Mob mob) {
        return mob.isOnGround() && !serverLevel.getBlockState(mob.blockPosition()).is(Blocks.HONEY_BLOCK);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
        boolean bl;
        boolean bl2 = bl = this.initialPosition.isPresent() && this.initialPosition.get().equals(mob.position()) && this.findJumpTries > 0 && (this.chosenJump.isPresent() || !this.jumpCandidates.isEmpty());
        if (!bl && !mob.getBrain().getMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isPresent()) {
            mob.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(serverLevel.random) / 2);
        }
        return bl;
    }

    @Override
    protected void start(ServerLevel serverLevel, Mob mob, long l) {
        this.chosenJump = Optional.empty();
        this.findJumpTries = 20;
        this.jumpCandidates.clear();
        this.initialPosition = Optional.of(mob.position());
        BlockPos blockPos = mob.blockPosition();
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        Iterable<BlockPos> iterable = BlockPos.betweenClosed(n - this.maxLongJumpWidth, n2 - this.maxLongJumpHeight, n3 - this.maxLongJumpWidth, n + this.maxLongJumpWidth, n2 + this.maxLongJumpHeight, n3 + this.maxLongJumpWidth);
        PathNavigation pathNavigation = mob.getNavigation();
        for (BlockPos blockPos2 : iterable) {
            double d = blockPos2.distSqr(blockPos);
            if (n == blockPos2.getX() && n3 == blockPos2.getZ() || !pathNavigation.isStableDestination(blockPos2) || mob.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(mob.level, blockPos2.mutable())) != 0.0f) continue;
            Optional<Vec3> optional = this.calculateOptimalJumpVector(mob, Vec3.atCenterOf(blockPos2));
            optional.ifPresent(vec3 -> this.jumpCandidates.add(new PossibleJump(new BlockPos(blockPos2), (Vec3)vec3, Mth.ceil(d))));
        }
    }

    @Override
    protected void tick(ServerLevel serverLevel, E e, long l) {
        if (this.chosenJump.isPresent()) {
            if (l - this.prepareJumpStart >= 40L) {
                ((Entity)e).setYRot(((Mob)e).yBodyRot);
                ((LivingEntity)e).setDiscardFriction(true);
                Vec3 vec3 = this.chosenJump.get().getJumpVector();
                double d = vec3.length();
                double d2 = d + ((LivingEntity)e).getJumpBoostPower();
                ((Entity)e).setDeltaMovement(vec3.scale(d2 / d));
                ((LivingEntity)e).getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
                serverLevel.playSound(null, (Entity)e, this.getJumpSound.apply(e), SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
        } else {
            --this.findJumpTries;
            Optional<PossibleJump> optional = WeightedRandom.getRandomItem(serverLevel.random, this.jumpCandidates);
            if (optional.isPresent()) {
                this.jumpCandidates.remove(optional.get());
                ((LivingEntity)e).getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(optional.get().getJumpTarget()));
                PathNavigation pathNavigation = ((Mob)e).getNavigation();
                Path path = pathNavigation.createPath(optional.get().getJumpTarget(), 0, 8);
                if (path == null || !path.canReach()) {
                    this.chosenJump = optional;
                    this.prepareJumpStart = l;
                }
            }
        }
    }

    private Optional<Vec3> calculateOptimalJumpVector(Mob mob, Vec3 vec3) {
        Optional<Vec3> optional = Optional.empty();
        for (int i = 65; i < 85; i += 5) {
            Optional<Vec3> optional2 = this.calculateJumpVectorForAngle(mob, vec3, i);
            if (optional.isPresent() && (!optional2.isPresent() || !(optional2.get().lengthSqr() < optional.get().lengthSqr()))) continue;
            optional = optional2;
        }
        return optional;
    }

    private Optional<Vec3> calculateJumpVectorForAngle(Mob mob, Vec3 vec3, int n) {
        Vec3 vec32 = mob.position();
        Vec3 vec33 = new Vec3(vec3.x - vec32.x, 0.0, vec3.z - vec32.z).normalize().scale(0.5);
        vec3 = vec3.subtract(vec33);
        Vec3 vec34 = vec3.subtract(vec32);
        float f = (float)n * (float)Math.PI / 180.0f;
        double d = Math.atan2(vec34.z, vec34.x);
        double d2 = vec34.subtract(0.0, vec34.y, 0.0).lengthSqr();
        double d3 = Math.sqrt(d2);
        double d4 = vec34.y;
        double d5 = Math.sin(2.0f * f);
        double d6 = 0.08;
        double d7 = Math.pow(Math.cos(f), 2.0);
        double d8 = Math.sin(f);
        double d9 = Math.cos(f);
        double d10 = Math.sin(d);
        double d11 = Math.cos(d);
        double d12 = d2 * 0.08 / (d3 * d5 - 2.0 * d4 * d7);
        if (d12 < 0.0) {
            return Optional.empty();
        }
        double d13 = Math.sqrt(d12);
        if (d13 > (double)this.maxJumpVelocity) {
            return Optional.empty();
        }
        double d14 = d13 * d9;
        double d15 = d13 * d8;
        int n2 = Mth.ceil(d3 / d14) * 2;
        double d16 = 0.0;
        Vec3 vec35 = null;
        for (int i = 0; i < n2 - 1; ++i) {
            double d17 = d8 / d9 * (d16 += d3 / (double)n2) - Math.pow(d16, 2.0) * 0.08 / (2.0 * d12 * Math.pow(d9, 2.0));
            double d18 = d16 * d11;
            double d19 = d16 * d10;
            Vec3 vec36 = new Vec3(vec32.x + d18, vec32.y + d17, vec32.z + d19);
            if (vec35 != null && !this.isClearTransition(mob, vec35, vec36)) {
                return Optional.empty();
            }
            vec35 = vec36;
        }
        return Optional.of(new Vec3(d14 * d11, d15, d14 * d10).scale(0.95f));
    }

    private boolean isClearTransition(Mob mob, Vec3 vec3, Vec3 vec32) {
        EntityDimensions entityDimensions = mob.getDimensions(Pose.LONG_JUMPING);
        Vec3 vec33 = vec32.subtract(vec3);
        double d = Math.min(entityDimensions.width, entityDimensions.height);
        int n = Mth.ceil(vec33.length() / d);
        Vec3 vec34 = vec33.normalize();
        Vec3 vec35 = vec3;
        for (int i = 0; i < n; ++i) {
            vec35 = i == n - 1 ? vec32 : vec35.add(vec34.scale(d * (double)0.9f));
            AABB aABB = entityDimensions.makeBoundingBox(vec35);
            if (mob.level.noCollision(mob, aABB)) continue;
            return false;
        }
        return true;
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Mob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (E)((Mob)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Mob)livingEntity, l);
    }

    public static class PossibleJump
    extends WeightedEntry.IntrusiveBase {
        private final BlockPos jumpTarget;
        private final Vec3 jumpVector;

        public PossibleJump(BlockPos blockPos, Vec3 vec3, int n) {
            super(n);
            this.jumpTarget = blockPos;
            this.jumpVector = vec3;
        }

        public BlockPos getJumpTarget() {
            return this.jumpTarget;
        }

        public Vec3 getJumpVector() {
            return this.jumpVector;
        }
    }
}

