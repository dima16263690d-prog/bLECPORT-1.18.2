/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class Projectile
extends Entity {
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    private boolean leftOwner;
    private boolean hasBeenShot;

    Projectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public void setOwner(@Nullable Entity entity) {
        if (entity != null) {
            this.ownerUUID = entity.getUUID();
            this.cachedOwner = entity;
        }
    }

    @Nullable
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        }
        if (this.ownerUUID != null && this.level instanceof ServerLevel) {
            this.cachedOwner = ((ServerLevel)this.level).getEntity(this.ownerUUID);
            return this.cachedOwner;
        }
        return null;
    }

    public Entity getEffectSource() {
        return (Entity)MoreObjects.firstNonNull((Object)this.getOwner(), (Object)this);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.ownerUUID != null) {
            compoundTag.putUUID("Owner", this.ownerUUID);
        }
        if (this.leftOwner) {
            compoundTag.putBoolean("LeftOwner", true);
        }
        compoundTag.putBoolean("HasBeenShot", this.hasBeenShot);
    }

    protected boolean ownedBy(Entity entity) {
        return entity.getUUID().equals(this.ownerUUID);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("Owner")) {
            this.ownerUUID = compoundTag.getUUID("Owner");
        }
        this.leftOwner = compoundTag.getBoolean("LeftOwner");
        this.hasBeenShot = compoundTag.getBoolean("HasBeenShot");
    }

    @Override
    public void tick() {
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner(), this.blockPosition());
            this.hasBeenShot = true;
        }
        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }
        super.tick();
    }

    private boolean checkLeftOwner() {
        Entity entity2 = this.getOwner();
        if (entity2 != null) {
            for (Entity entity3 : this.level.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), entity -> !entity.isSpectator() && entity.isPickable())) {
                if (entity3.getRootVehicle() != entity2.getRootVehicle()) continue;
                return false;
            }
        }
        return true;
    }

    public void shoot(double d, double d2, double d3, float f, float f2) {
        Vec3 vec3 = new Vec3(d, d2, d3).normalize().add(this.random.nextGaussian() * (double)0.0075f * (double)f2, this.random.nextGaussian() * (double)0.0075f * (double)f2, this.random.nextGaussian() * (double)0.0075f * (double)f2).scale(f);
        this.setDeltaMovement(vec3);
        double d4 = vec3.horizontalDistance();
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2(vec3.y, d4) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void shootFromRotation(Entity entity, float f, float f2, float f3, float f4, float f5) {
        float f6 = -Mth.sin(f2 * ((float)Math.PI / 180)) * Mth.cos(f * ((float)Math.PI / 180));
        float f7 = -Mth.sin((f + f3) * ((float)Math.PI / 180));
        float f8 = Mth.cos(f2 * ((float)Math.PI / 180)) * Mth.cos(f * ((float)Math.PI / 180));
        this.shoot(f6, f7, f8, f4, f5);
        Vec3 vec3 = entity.getDeltaMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(vec3.x, entity.isOnGround() ? 0.0 : vec3.y, vec3.z));
    }

    protected void onHit(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();
        if (type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)hitResult);
        } else if (type == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult)hitResult);
        }
        if (type != HitResult.Type.MISS) {
            this.gameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());
        }
    }

    protected void onHitEntity(EntityHitResult entityHitResult) {
    }

    protected void onHitBlock(BlockHitResult blockHitResult) {
        BlockState blockState = this.level.getBlockState(blockHitResult.getBlockPos());
        blockState.onProjectileHit(this.level, blockState, blockHitResult, this);
    }

    @Override
    public void lerpMotion(double d, double d2, double d3) {
        this.setDeltaMovement(d, d2, d3);
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            double d4 = Math.sqrt(d * d + d3 * d3);
            this.setXRot((float)(Mth.atan2(d2, d4) * 57.2957763671875));
            this.setYRot((float)(Mth.atan2(d, d3) * 57.2957763671875));
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }
    }

    protected boolean canHitEntity(Entity entity) {
        if (entity.isSpectator() || !entity.isAlive() || !entity.isPickable()) {
            return false;
        }
        Entity entity2 = this.getOwner();
        return entity2 == null || this.leftOwner || !entity2.isPassengerOfSameVehicle(entity);
    }

    protected void updateRotation() {
        Vec3 vec3 = this.getDeltaMovement();
        double d = vec3.horizontalDistance();
        this.setXRot(Projectile.lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, d) * 57.2957763671875)));
        this.setYRot(Projectile.lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875)));
    }

    protected static float lerpRotation(float f, float f2) {
        while (f2 - f < -180.0f) {
            f -= 360.0f;
        }
        while (f2 - f >= 180.0f) {
            f += 360.0f;
        }
        return Mth.lerp(0.2f, f, f2);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        Entity entity = this.getOwner();
        return new ClientboundAddEntityPacket(this, entity == null ? 0 : entity.getId());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        Entity entity = this.level.getEntity(clientboundAddEntityPacket.getData());
        if (entity != null) {
            this.setOwner(entity);
        }
    }

    @Override
    public boolean mayInteract(Level level, BlockPos blockPos) {
        Entity entity = this.getOwner();
        if (entity instanceof Player) {
            return entity.mayInteract(level, blockPos);
        }
        return entity == null || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }
}

