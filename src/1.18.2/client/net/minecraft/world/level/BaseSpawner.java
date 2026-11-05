/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public abstract class BaseSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int EVENT_SPAWN = 1;
    private int spawnDelay = 20;
    private SimpleWeightedRandomList<SpawnData> spawnPotentials = SimpleWeightedRandomList.empty();
    private SpawnData nextSpawnData = new SpawnData();
    private double spin;
    private double oSpin;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    @Nullable
    private Entity displayEntity;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;
    private final Random random = new Random();

    public void setEntityId(EntityType<?> entityType) {
        this.nextSpawnData.getEntityToSpawn().putString("id", Registry.ENTITY_TYPE.getKey(entityType).toString());
    }

    private boolean isNearPlayer(Level level, BlockPos blockPos) {
        return level.hasNearbyAlivePlayer((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, this.requiredPlayerRange);
    }

    public void clientTick(Level level, BlockPos blockPos) {
        if (!this.isNearPlayer(level, blockPos)) {
            this.oSpin = this.spin;
        } else {
            double d = (double)blockPos.getX() + level.random.nextDouble();
            double d2 = (double)blockPos.getY() + level.random.nextDouble();
            double d3 = (double)blockPos.getZ() + level.random.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, d, d2, d3, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.FLAME, d, d2, d3, 0.0, 0.0, 0.0);
            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            }
            this.oSpin = this.spin;
            this.spin = (this.spin + (double)(1000.0f / ((float)this.spawnDelay + 200.0f))) % 360.0;
        }
    }

    public void serverTick(ServerLevel serverLevel, BlockPos blockPos) {
        if (!this.isNearPlayer(serverLevel, blockPos)) {
            return;
        }
        if (this.spawnDelay == -1) {
            this.delay(serverLevel, blockPos);
        }
        if (this.spawnDelay > 0) {
            --this.spawnDelay;
            return;
        }
        boolean bl = false;
        for (int i = 0; i < this.spawnCount; ++i) {
            Object object;
            double d;
            CompoundTag compoundTag = this.nextSpawnData.getEntityToSpawn();
            Optional<EntityType<?>> optional = EntityType.by(compoundTag);
            if (optional.isEmpty()) {
                this.delay(serverLevel, blockPos);
                return;
            }
            ListTag listTag = compoundTag.getList("Pos", 6);
            int n = listTag.size();
            double d2 = n >= 1 ? listTag.getDouble(0) : (double)blockPos.getX() + (serverLevel.random.nextDouble() - serverLevel.random.nextDouble()) * (double)this.spawnRange + 0.5;
            double d3 = n >= 2 ? listTag.getDouble(1) : (double)(blockPos.getY() + serverLevel.random.nextInt(3) - 1);
            double d4 = d = n >= 3 ? listTag.getDouble(2) : (double)blockPos.getZ() + (serverLevel.random.nextDouble() - serverLevel.random.nextDouble()) * (double)this.spawnRange + 0.5;
            if (!serverLevel.noCollision(optional.get().getAABB(d2, d3, d))) continue;
            BlockPos blockPos2 = new BlockPos(d2, d3, d);
            if (!this.nextSpawnData.getCustomSpawnRules().isPresent() ? !SpawnPlacements.checkSpawnRules(optional.get(), serverLevel, MobSpawnType.SPAWNER, blockPos2, serverLevel.getRandom()) : !optional.get().getCategory().isFriendly() && serverLevel.getDifficulty() == Difficulty.PEACEFUL || !((SpawnData.CustomSpawnRules)((Object)(object = this.nextSpawnData.getCustomSpawnRules().get()))).blockLightLimit().isValueInRange(serverLevel.getBrightness(LightLayer.BLOCK, blockPos2)) || !((SpawnData.CustomSpawnRules)((Object)object)).skyLightLimit().isValueInRange(serverLevel.getBrightness(LightLayer.SKY, blockPos2))) continue;
            object = EntityType.loadEntityRecursive(compoundTag, serverLevel, entity -> {
                entity.moveTo(d2, d3, d, entity.getYRot(), entity.getXRot());
                return entity;
            });
            if (object == null) {
                this.delay(serverLevel, blockPos);
                return;
            }
            int n2 = serverLevel.getEntitiesOfClass(object.getClass(), new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1).inflate(this.spawnRange)).size();
            if (n2 >= this.maxNearbyEntities) {
                this.delay(serverLevel, blockPos);
                return;
            }
            ((Entity)object).moveTo(((Entity)object).getX(), ((Entity)object).getY(), ((Entity)object).getZ(), serverLevel.random.nextFloat() * 360.0f, 0.0f);
            if (object instanceof Mob) {
                Mob mob = (Mob)object;
                if (this.nextSpawnData.getCustomSpawnRules().isEmpty() && !mob.checkSpawnRules(serverLevel, MobSpawnType.SPAWNER) || !mob.checkSpawnObstruction(serverLevel)) continue;
                if (this.nextSpawnData.getEntityToSpawn().size() == 1 && this.nextSpawnData.getEntityToSpawn().contains("id", 8)) {
                    ((Mob)object).finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(((Entity)object).blockPosition()), MobSpawnType.SPAWNER, null, null);
                }
            }
            if (!serverLevel.tryAddFreshEntityWithPassengers((Entity)object)) {
                this.delay(serverLevel, blockPos);
                return;
            }
            serverLevel.levelEvent(2004, blockPos, 0);
            if (object instanceof Mob) {
                ((Mob)object).spawnAnim();
            }
            bl = true;
        }
        if (bl) {
            this.delay(serverLevel, blockPos);
        }
    }

    private void delay(Level level, BlockPos blockPos) {
        this.spawnDelay = this.maxSpawnDelay <= this.minSpawnDelay ? this.minSpawnDelay : this.minSpawnDelay + this.random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        this.spawnPotentials.getRandom(this.random).ifPresent(wrapper -> this.setNextSpawnData(level, blockPos, (SpawnData)((Object)((Object)wrapper.getData()))));
        this.broadcastEvent(level, blockPos, 1);
    }

    public void load(@Nullable Level level, BlockPos blockPos, CompoundTag compoundTag) {
        this.spawnDelay = compoundTag.getShort("Delay");
        boolean bl = compoundTag.contains("SpawnPotentials", 9);
        boolean bl2 = compoundTag.contains("SpawnData", 10);
        if (!bl) {
            SpawnData spawnData = bl2 ? SpawnData.CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.getCompound("SpawnData")).resultOrPartial(string -> LOGGER.warn("Invalid SpawnData: {}", string)).orElseGet(SpawnData::new) : new SpawnData();
            this.spawnPotentials = SimpleWeightedRandomList.single(spawnData);
            this.setNextSpawnData(level, blockPos, spawnData);
        } else {
            ListTag listTag = compoundTag.getList("SpawnPotentials", 10);
            this.spawnPotentials = SpawnData.LIST_CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)listTag).resultOrPartial(string -> LOGGER.warn("Invalid SpawnPotentials list: {}", string)).orElseGet(SimpleWeightedRandomList::empty);
            if (bl2) {
                SpawnData spawnData = SpawnData.CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.getCompound("SpawnData")).resultOrPartial(string -> LOGGER.warn("Invalid SpawnData: {}", string)).orElseGet(SpawnData::new);
                this.setNextSpawnData(level, blockPos, spawnData);
            } else {
                this.spawnPotentials.getRandom(this.random).ifPresent(wrapper -> this.setNextSpawnData(level, blockPos, (SpawnData)((Object)((Object)wrapper.getData()))));
            }
        }
        if (compoundTag.contains("MinSpawnDelay", 99)) {
            this.minSpawnDelay = compoundTag.getShort("MinSpawnDelay");
            this.maxSpawnDelay = compoundTag.getShort("MaxSpawnDelay");
            this.spawnCount = compoundTag.getShort("SpawnCount");
        }
        if (compoundTag.contains("MaxNearbyEntities", 99)) {
            this.maxNearbyEntities = compoundTag.getShort("MaxNearbyEntities");
            this.requiredPlayerRange = compoundTag.getShort("RequiredPlayerRange");
        }
        if (compoundTag.contains("SpawnRange", 99)) {
            this.spawnRange = compoundTag.getShort("SpawnRange");
        }
        this.displayEntity = null;
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putShort("Delay", (short)this.spawnDelay);
        compoundTag.putShort("MinSpawnDelay", (short)this.minSpawnDelay);
        compoundTag.putShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
        compoundTag.putShort("SpawnCount", (short)this.spawnCount);
        compoundTag.putShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
        compoundTag.putShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
        compoundTag.putShort("SpawnRange", (short)this.spawnRange);
        compoundTag.put("SpawnData", (Tag)SpawnData.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)this.nextSpawnData).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData")));
        compoundTag.put("SpawnPotentials", (Tag)SpawnData.LIST_CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, this.spawnPotentials).result().orElseThrow());
        return compoundTag;
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(Level level) {
        if (this.displayEntity == null) {
            this.displayEntity = EntityType.loadEntityRecursive(this.nextSpawnData.getEntityToSpawn(), level, Function.identity());
            if (this.nextSpawnData.getEntityToSpawn().size() != 1 || !this.nextSpawnData.getEntityToSpawn().contains("id", 8) || this.displayEntity instanceof Mob) {
                // empty if block
            }
        }
        return this.displayEntity;
    }

    public boolean onEventTriggered(Level level, int n) {
        if (n == 1) {
            if (level.isClientSide) {
                this.spawnDelay = this.minSpawnDelay;
            }
            return true;
        }
        return false;
    }

    public void setNextSpawnData(@Nullable Level level, BlockPos blockPos, SpawnData spawnData) {
        this.nextSpawnData = spawnData;
    }

    public abstract void broadcastEvent(Level var1, BlockPos var2, int var3);

    public double getSpin() {
        return this.spin;
    }

    public double getoSpin() {
        return this.oSpin;
    }
}

