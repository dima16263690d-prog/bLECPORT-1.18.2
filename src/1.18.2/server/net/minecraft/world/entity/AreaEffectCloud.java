/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;

public class AreaEffectCloud
extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TIME_BETWEEN_APPLICATIONS = 5;
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.PARTICLE);
    private static final float MAX_RADIUS = 32.0f;
    private Potion potion = Potions.EMPTY;
    private final List<MobEffectInstance> effects = Lists.newArrayList();
    private final Map<Entity, Integer> victims = Maps.newHashMap();
    private int duration = 600;
    private int waitTime = 20;
    private int reapplicationDelay = 20;
    private boolean fixedColor;
    private int durationOnUse;
    private float radiusOnUse;
    private float radiusPerTick;
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;

    public AreaEffectCloud(EntityType<? extends AreaEffectCloud> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setRadius(3.0f);
    }

    public AreaEffectCloud(Level level, double d, double d2, double d3) {
        this((EntityType<? extends AreaEffectCloud>)EntityType.AREA_EFFECT_CLOUD, level);
        this.setPos(d, d2, d3);
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_COLOR, 0);
        this.getEntityData().define(DATA_RADIUS, Float.valueOf(0.5f));
        this.getEntityData().define(DATA_WAITING, false);
        this.getEntityData().define(DATA_PARTICLE, ParticleTypes.ENTITY_EFFECT);
    }

    public void setRadius(float f) {
        if (!this.level.isClientSide) {
            this.getEntityData().set(DATA_RADIUS, Float.valueOf(Mth.clamp(f, 0.0f, 32.0f)));
        }
    }

    @Override
    public void refreshDimensions() {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        super.refreshDimensions();
        this.setPos(d, d2, d3);
    }

    public float getRadius() {
        return this.getEntityData().get(DATA_RADIUS).floatValue();
    }

    public void setPotion(Potion potion) {
        this.potion = potion;
        if (!this.fixedColor) {
            this.updateColor();
        }
    }

    private void updateColor() {
        if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
            this.getEntityData().set(DATA_COLOR, 0);
        } else {
            this.getEntityData().set(DATA_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
        }
    }

    public void addEffect(MobEffectInstance mobEffectInstance) {
        this.effects.add(mobEffectInstance);
        if (!this.fixedColor) {
            this.updateColor();
        }
    }

    public int getColor() {
        return this.getEntityData().get(DATA_COLOR);
    }

    public void setFixedColor(int n) {
        this.fixedColor = true;
        this.getEntityData().set(DATA_COLOR, n);
    }

    public ParticleOptions getParticle() {
        return this.getEntityData().get(DATA_PARTICLE);
    }

    public void setParticle(ParticleOptions particleOptions) {
        this.getEntityData().set(DATA_PARTICLE, particleOptions);
    }

    protected void setWaiting(boolean bl) {
        this.getEntityData().set(DATA_WAITING, bl);
    }

    public boolean isWaiting() {
        return this.getEntityData().get(DATA_WAITING);
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int n) {
        this.duration = n;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void tick() {
        block20: {
            ArrayList arrayList;
            float f;
            block21: {
                boolean bl;
                boolean bl2;
                block19: {
                    void object;
                    float f2;
                    int n;
                    super.tick();
                    bl2 = this.isWaiting();
                    f = this.getRadius();
                    if (!this.level.isClientSide) break block19;
                    if (bl2 && this.random.nextBoolean()) {
                        return;
                    }
                    ParticleOptions particleOptions = this.getParticle();
                    if (bl2) {
                        n = 2;
                        f2 = 0.2f;
                    } else {
                        n = Mth.ceil((float)Math.PI * f * f);
                        f2 = f;
                    }
                    boolean i = false;
                    while (object < n) {
                        double d;
                        double d2;
                        double d3;
                        float f3 = this.random.nextFloat() * ((float)Math.PI * 2);
                        float f4 = Mth.sqrt(this.random.nextFloat()) * f2;
                        double d4 = this.getX() + (double)(Mth.cos(f3) * f4);
                        double d5 = this.getY();
                        double d6 = this.getZ() + (double)(Mth.sin(f3) * f4);
                        if (particleOptions.getType() == ParticleTypes.ENTITY_EFFECT) {
                            int n2 = bl2 && this.random.nextBoolean() ? 0xFFFFFF : this.getColor();
                            d3 = (float)(n2 >> 16 & 0xFF) / 255.0f;
                            d2 = (float)(n2 >> 8 & 0xFF) / 255.0f;
                            d = (float)(n2 & 0xFF) / 255.0f;
                        } else if (bl2) {
                            d3 = 0.0;
                            d2 = 0.0;
                            d = 0.0;
                        } else {
                            d3 = (0.5 - this.random.nextDouble()) * 0.15;
                            d2 = 0.01f;
                            d = (0.5 - this.random.nextDouble()) * 0.15;
                        }
                        this.level.addAlwaysVisibleParticle(particleOptions, d4, d5, d6, d3, d2, d);
                        ++object;
                    }
                    break block20;
                }
                if (this.tickCount >= this.waitTime + this.duration) {
                    this.discard();
                    return;
                }
                boolean bl3 = bl = this.tickCount < this.waitTime;
                if (bl2 != bl) {
                    this.setWaiting(bl);
                }
                if (bl) {
                    return;
                }
                if (this.radiusPerTick != 0.0f) {
                    if ((f += this.radiusPerTick) < 0.5f) {
                        this.discard();
                        return;
                    }
                    this.setRadius(f);
                }
                if (this.tickCount % 5 != 0) break block20;
                this.victims.entrySet().removeIf(entry -> this.tickCount >= (Integer)entry.getValue());
                arrayList = Lists.newArrayList();
                for (MobEffectInstance mobEffectInstance : this.potion.getEffects()) {
                    arrayList.add(new MobEffectInstance(mobEffectInstance.getEffect(), mobEffectInstance.getDuration() / 4, mobEffectInstance.getAmplifier(), mobEffectInstance.isAmbient(), mobEffectInstance.isVisible()));
                }
                arrayList.addAll(this.effects);
                if (!arrayList.isEmpty()) break block21;
                this.victims.clear();
                break block20;
            }
            List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
            if (list.isEmpty()) break block20;
            for (LivingEntity livingEntity : list) {
                double d;
                double d7;
                double d8;
                if (this.victims.containsKey(livingEntity) || !livingEntity.isAffectedByPotions() || !((d8 = (d7 = livingEntity.getX() - this.getX()) * d7 + (d = livingEntity.getZ() - this.getZ()) * d) <= (double)(f * f))) continue;
                this.victims.put(livingEntity, this.tickCount + this.reapplicationDelay);
                for (MobEffectInstance mobEffectInstance : arrayList) {
                    if (mobEffectInstance.getEffect().isInstantenous()) {
                        mobEffectInstance.getEffect().applyInstantenousEffect(this, this.getOwner(), livingEntity, mobEffectInstance.getAmplifier(), 0.5);
                        continue;
                    }
                    livingEntity.addEffect(new MobEffectInstance(mobEffectInstance), this);
                }
                if (this.radiusOnUse != 0.0f) {
                    if ((f += this.radiusOnUse) < 0.5f) {
                        this.discard();
                        return;
                    }
                    this.setRadius(f);
                }
                if (this.durationOnUse == 0) continue;
                this.duration += this.durationOnUse;
                if (this.duration > 0) continue;
                this.discard();
                return;
            }
        }
    }

    public float getRadiusOnUse() {
        return this.radiusOnUse;
    }

    public void setRadiusOnUse(float f) {
        this.radiusOnUse = f;
    }

    public float getRadiusPerTick() {
        return this.radiusPerTick;
    }

    public void setRadiusPerTick(float f) {
        this.radiusPerTick = f;
    }

    public int getDurationOnUse() {
        return this.durationOnUse;
    }

    public void setDurationOnUse(int n) {
        this.durationOnUse = n;
    }

    public int getWaitTime() {
        return this.waitTime;
    }

    public void setWaitTime(int n) {
        this.waitTime = n;
    }

    public void setOwner(@Nullable LivingEntity livingEntity) {
        this.owner = livingEntity;
        this.ownerUUID = livingEntity == null ? null : livingEntity.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        Entity entity;
        if (this.owner == null && this.ownerUUID != null && this.level instanceof ServerLevel && (entity = ((ServerLevel)this.level).getEntity(this.ownerUUID)) instanceof LivingEntity) {
            this.owner = (LivingEntity)entity;
        }
        return this.owner;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.tickCount = compoundTag.getInt("Age");
        this.duration = compoundTag.getInt("Duration");
        this.waitTime = compoundTag.getInt("WaitTime");
        this.reapplicationDelay = compoundTag.getInt("ReapplicationDelay");
        this.durationOnUse = compoundTag.getInt("DurationOnUse");
        this.radiusOnUse = compoundTag.getFloat("RadiusOnUse");
        this.radiusPerTick = compoundTag.getFloat("RadiusPerTick");
        this.setRadius(compoundTag.getFloat("Radius"));
        if (compoundTag.hasUUID("Owner")) {
            this.ownerUUID = compoundTag.getUUID("Owner");
        }
        if (compoundTag.contains("Particle", 8)) {
            try {
                this.setParticle(ParticleArgument.readParticle(new StringReader(compoundTag.getString("Particle"))));
            }
            catch (CommandSyntaxException commandSyntaxException) {
                LOGGER.warn("Couldn't load custom particle {}", (Object)compoundTag.getString("Particle"), (Object)commandSyntaxException);
            }
        }
        if (compoundTag.contains("Color", 99)) {
            this.setFixedColor(compoundTag.getInt("Color"));
        }
        if (compoundTag.contains("Potion", 8)) {
            this.setPotion(PotionUtils.getPotion(compoundTag));
        }
        if (compoundTag.contains("Effects", 9)) {
            ListTag listTag = compoundTag.getList("Effects", 10);
            this.effects.clear();
            for (int i = 0; i < listTag.size(); ++i) {
                MobEffectInstance mobEffectInstance = MobEffectInstance.load(listTag.getCompound(i));
                if (mobEffectInstance == null) continue;
                this.addEffect(mobEffectInstance);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("Age", this.tickCount);
        compoundTag.putInt("Duration", this.duration);
        compoundTag.putInt("WaitTime", this.waitTime);
        compoundTag.putInt("ReapplicationDelay", this.reapplicationDelay);
        compoundTag.putInt("DurationOnUse", this.durationOnUse);
        compoundTag.putFloat("RadiusOnUse", this.radiusOnUse);
        compoundTag.putFloat("RadiusPerTick", this.radiusPerTick);
        compoundTag.putFloat("Radius", this.getRadius());
        compoundTag.putString("Particle", this.getParticle().writeToString());
        if (this.ownerUUID != null) {
            compoundTag.putUUID("Owner", this.ownerUUID);
        }
        if (this.fixedColor) {
            compoundTag.putInt("Color", this.getColor());
        }
        if (this.potion != Potions.EMPTY) {
            compoundTag.putString("Potion", Registry.POTION.getKey(this.potion).toString());
        }
        if (!this.effects.isEmpty()) {
            ListTag listTag = new ListTag();
            for (MobEffectInstance mobEffectInstance : this.effects) {
                listTag.add(mobEffectInstance.save(new CompoundTag()));
            }
            compoundTag.put("Effects", listTag);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_RADIUS.equals(entityDataAccessor)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    public Potion getPotion() {
        return this.potion;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0f, 0.5f);
    }
}

