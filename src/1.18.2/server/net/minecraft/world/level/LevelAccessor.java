/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.LevelTimeAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

public interface LevelAccessor
extends CommonLevelAccessor,
LevelTimeAccess {
    @Override
    default public long dayTime() {
        return this.getLevelData().getDayTime();
    }

    public long nextSubTickCount();

    public LevelTickAccess<Block> getBlockTicks();

    private <T> ScheduledTick<T> createTick(BlockPos blockPos, T t, int n, TickPriority tickPriority) {
        return new ScheduledTick<T>(t, blockPos, this.getLevelData().getGameTime() + (long)n, tickPriority, this.nextSubTickCount());
    }

    private <T> ScheduledTick<T> createTick(BlockPos blockPos, T t, int n) {
        return new ScheduledTick<T>(t, blockPos, this.getLevelData().getGameTime() + (long)n, this.nextSubTickCount());
    }

    default public void scheduleTick(BlockPos blockPos, Block block, int n, TickPriority tickPriority) {
        this.getBlockTicks().schedule(this.createTick(blockPos, block, n, tickPriority));
    }

    default public void scheduleTick(BlockPos blockPos, Block block, int n) {
        this.getBlockTicks().schedule(this.createTick(blockPos, block, n));
    }

    public LevelTickAccess<Fluid> getFluidTicks();

    default public void scheduleTick(BlockPos blockPos, Fluid fluid, int n, TickPriority tickPriority) {
        this.getFluidTicks().schedule(this.createTick(blockPos, fluid, n, tickPriority));
    }

    default public void scheduleTick(BlockPos blockPos, Fluid fluid, int n) {
        this.getFluidTicks().schedule(this.createTick(blockPos, fluid, n));
    }

    public LevelData getLevelData();

    public DifficultyInstance getCurrentDifficultyAt(BlockPos var1);

    @Nullable
    public MinecraftServer getServer();

    default public Difficulty getDifficulty() {
        return this.getLevelData().getDifficulty();
    }

    public ChunkSource getChunkSource();

    @Override
    default public boolean hasChunk(int n, int n2) {
        return this.getChunkSource().hasChunk(n, n2);
    }

    public Random getRandom();

    default public void blockUpdated(BlockPos blockPos, Block block) {
    }

    public void playSound(@Nullable Player var1, BlockPos var2, SoundEvent var3, SoundSource var4, float var5, float var6);

    public void addParticle(ParticleOptions var1, double var2, double var4, double var6, double var8, double var10, double var12);

    public void levelEvent(@Nullable Player var1, int var2, BlockPos var3, int var4);

    default public void levelEvent(int n, BlockPos blockPos, int n2) {
        this.levelEvent(null, n, blockPos, n2);
    }

    public void gameEvent(@Nullable Entity var1, GameEvent var2, BlockPos var3);

    default public void gameEvent(GameEvent gameEvent, BlockPos blockPos) {
        this.gameEvent(null, gameEvent, blockPos);
    }

    default public void gameEvent(GameEvent gameEvent, Entity entity) {
        this.gameEvent(null, gameEvent, entity.blockPosition());
    }

    default public void gameEvent(@Nullable Entity entity, GameEvent gameEvent, Entity entity2) {
        this.gameEvent(entity, gameEvent, entity2.blockPosition());
    }
}

