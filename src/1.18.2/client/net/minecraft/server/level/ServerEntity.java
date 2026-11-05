/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TOLERANCE_LEVEL_ROTATION = 1;
    private final ServerLevel level;
    private final Entity entity;
    private final int updateInterval;
    private final boolean trackDelta;
    private final Consumer<Packet<?>> broadcast;
    private long xp;
    private long yp;
    private long zp;
    private int yRotp;
    private int xRotp;
    private int yHeadRotp;
    private Vec3 ap = Vec3.ZERO;
    private int tickCount;
    private int teleportDelay;
    private List<Entity> lastPassengers = Collections.emptyList();
    private boolean wasRiding;
    private boolean wasOnGround;

    public ServerEntity(ServerLevel serverLevel, Entity entity, int n, boolean bl, Consumer<Packet<?>> consumer) {
        this.level = serverLevel;
        this.broadcast = consumer;
        this.entity = entity;
        this.updateInterval = n;
        this.trackDelta = bl;
        this.updateSentPos();
        this.yRotp = Mth.floor(entity.getYRot() * 256.0f / 360.0f);
        this.xRotp = Mth.floor(entity.getXRot() * 256.0f / 360.0f);
        this.yHeadRotp = Mth.floor(entity.getYHeadRot() * 256.0f / 360.0f);
        this.wasOnGround = entity.isOnGround();
    }

    public void sendChanges() {
        Object object;
        List<Entity> list = this.entity.getPassengers();
        if (!list.equals(this.lastPassengers)) {
            this.lastPassengers = list;
            this.broadcast.accept(new ClientboundSetPassengersPacket(this.entity));
        }
        if (this.entity instanceof ItemFrame && this.tickCount % 10 == 0) {
            MapItemSavedData mapItemSavedData;
            ItemFrame itemFrame = (ItemFrame)this.entity;
            ItemStack itemStack = itemFrame.getItem();
            if (itemStack.getItem() instanceof MapItem && (mapItemSavedData = MapItem.getSavedData((Integer)(object = MapItem.getMapId(itemStack)), (Level)this.level)) != null) {
                for (ServerPlayer serverPlayer : this.level.players()) {
                    mapItemSavedData.tickCarriedBy(serverPlayer, itemStack);
                    Packet<?> packet = mapItemSavedData.getUpdatePacket((Integer)object, serverPlayer);
                    if (packet == null) continue;
                    serverPlayer.connection.send(packet);
                }
            }
            this.sendDirtyEntityData();
        }
        if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
            int n;
            if (this.entity.isPassenger()) {
                boolean bl;
                n = Mth.floor(this.entity.getYRot() * 256.0f / 360.0f);
                int n2 = Mth.floor(this.entity.getXRot() * 256.0f / 360.0f);
                boolean bl2 = bl = Math.abs(n - this.yRotp) >= 1 || Math.abs(n2 - this.xRotp) >= 1;
                if (bl) {
                    this.broadcast.accept(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)n, (byte)n2, this.entity.isOnGround()));
                    this.yRotp = n;
                    this.xRotp = n2;
                }
                this.updateSentPos();
                this.sendDirtyEntityData();
                this.wasRiding = true;
            } else {
                Vec3 vec3;
                double d;
                boolean bl;
                ++this.teleportDelay;
                n = Mth.floor(this.entity.getYRot() * 256.0f / 360.0f);
                int n3 = Mth.floor(this.entity.getXRot() * 256.0f / 360.0f);
                object = this.entity.position().subtract(ClientboundMoveEntityPacket.packetToEntity(this.xp, this.yp, this.zp));
                boolean bl3 = ((Vec3)object).lengthSqr() >= 7.62939453125E-6;
                Object object2 = null;
                boolean bl4 = bl3 || this.tickCount % 60 == 0;
                boolean bl5 = bl = Math.abs(n - this.yRotp) >= 1 || Math.abs(n3 - this.xRotp) >= 1;
                if (this.tickCount > 0 || this.entity instanceof AbstractArrow) {
                    boolean bl6;
                    long l = ClientboundMoveEntityPacket.entityToPacket(((Vec3)object).x);
                    long l2 = ClientboundMoveEntityPacket.entityToPacket(((Vec3)object).y);
                    long l3 = ClientboundMoveEntityPacket.entityToPacket(((Vec3)object).z);
                    boolean bl7 = bl6 = l < -32768L || l > 32767L || l2 < -32768L || l2 > 32767L || l3 < -32768L || l3 > 32767L;
                    if (bl6 || this.teleportDelay > 400 || this.wasRiding || this.wasOnGround != this.entity.isOnGround()) {
                        this.wasOnGround = this.entity.isOnGround();
                        this.teleportDelay = 0;
                        object2 = new ClientboundTeleportEntityPacket(this.entity);
                    } else if (bl4 && bl || this.entity instanceof AbstractArrow) {
                        object2 = new ClientboundMoveEntityPacket.PosRot(this.entity.getId(), (short)l, (short)l2, (short)l3, (byte)n, (byte)n3, this.entity.isOnGround());
                    } else if (bl4) {
                        object2 = new ClientboundMoveEntityPacket.Pos(this.entity.getId(), (short)l, (short)l2, (short)l3, this.entity.isOnGround());
                    } else if (bl) {
                        object2 = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)n, (byte)n3, this.entity.isOnGround());
                    }
                }
                if ((this.trackDelta || this.entity.hasImpulse || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && this.tickCount > 0 && ((d = (vec3 = this.entity.getDeltaMovement()).distanceToSqr(this.ap)) > 1.0E-7 || d > 0.0 && vec3.lengthSqr() == 0.0)) {
                    this.ap = vec3;
                    this.broadcast.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
                }
                if (object2 != null) {
                    this.broadcast.accept((Packet<?>)object2);
                }
                this.sendDirtyEntityData();
                if (bl4) {
                    this.updateSentPos();
                }
                if (bl) {
                    this.yRotp = n;
                    this.xRotp = n3;
                }
                this.wasRiding = false;
            }
            n = Mth.floor(this.entity.getYHeadRot() * 256.0f / 360.0f);
            if (Math.abs(n - this.yHeadRotp) >= 1) {
                this.broadcast.accept(new ClientboundRotateHeadPacket(this.entity, (byte)n));
                this.yHeadRotp = n;
            }
            this.entity.hasImpulse = false;
        }
        ++this.tickCount;
        if (this.entity.hurtMarked) {
            this.broadcastAndSend(new ClientboundSetEntityMotionPacket(this.entity));
            this.entity.hurtMarked = false;
        }
    }

    public void removePairing(ServerPlayer serverPlayer) {
        this.entity.stopSeenByPlayer(serverPlayer);
        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(this.entity.getId()));
    }

    public void addPairing(ServerPlayer serverPlayer) {
        this.sendPairingData(serverPlayer.connection::send);
        this.entity.startSeenByPlayer(serverPlayer);
    }

    public void sendPairingData(Consumer<Packet<?>> consumer) {
        Object object;
        if (this.entity.isRemoved()) {
            LOGGER.warn("Fetching packet for removed entity {}", (Object)this.entity);
        }
        Packet<?> packet = this.entity.getAddEntityPacket();
        this.yHeadRotp = Mth.floor(this.entity.getYHeadRot() * 256.0f / 360.0f);
        consumer.accept(packet);
        if (!this.entity.getEntityData().isEmpty()) {
            consumer.accept(new ClientboundSetEntityDataPacket(this.entity.getId(), this.entity.getEntityData(), true));
        }
        boolean bl = this.trackDelta;
        if (this.entity instanceof LivingEntity) {
            object = ((LivingEntity)this.entity).getAttributes().getSyncableAttributes();
            if (!object.isEmpty()) {
                consumer.accept(new ClientboundUpdateAttributesPacket(this.entity.getId(), (Collection<AttributeInstance>)object));
            }
            if (((LivingEntity)this.entity).isFallFlying()) {
                bl = true;
            }
        }
        this.ap = this.entity.getDeltaMovement();
        if (bl && !(packet instanceof ClientboundAddMobPacket)) {
            consumer.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
        }
        if (this.entity instanceof LivingEntity) {
            object = Lists.newArrayList();
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                ItemStack itemStack = ((LivingEntity)this.entity).getItemBySlot(equipmentSlot);
                if (itemStack.isEmpty()) continue;
                object.add(Pair.of((Object)((Object)equipmentSlot), (Object)itemStack.copy()));
            }
            if (!object.isEmpty()) {
                consumer.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), (List<Pair<EquipmentSlot, ItemStack>>)object));
            }
        }
        if (this.entity instanceof LivingEntity) {
            object = (LivingEntity)this.entity;
            for (MobEffectInstance mobEffectInstance : ((LivingEntity)object).getActiveEffects()) {
                consumer.accept(new ClientboundUpdateMobEffectPacket(this.entity.getId(), mobEffectInstance));
            }
        }
        if (!this.entity.getPassengers().isEmpty()) {
            consumer.accept(new ClientboundSetPassengersPacket(this.entity));
        }
        if (this.entity.isPassenger()) {
            consumer.accept(new ClientboundSetPassengersPacket(this.entity.getVehicle()));
        }
        if (this.entity instanceof Mob && ((Mob)(object = (Mob)this.entity)).isLeashed()) {
            consumer.accept(new ClientboundSetEntityLinkPacket((Entity)object, ((Mob)object).getLeashHolder()));
        }
    }

    private void sendDirtyEntityData() {
        SynchedEntityData synchedEntityData = this.entity.getEntityData();
        if (synchedEntityData.isDirty()) {
            this.broadcastAndSend(new ClientboundSetEntityDataPacket(this.entity.getId(), synchedEntityData, false));
        }
        if (this.entity instanceof LivingEntity) {
            Set<AttributeInstance> set = ((LivingEntity)this.entity).getAttributes().getDirtyAttributes();
            if (!set.isEmpty()) {
                this.broadcastAndSend(new ClientboundUpdateAttributesPacket(this.entity.getId(), set));
            }
            set.clear();
        }
    }

    private void updateSentPos() {
        this.xp = ClientboundMoveEntityPacket.entityToPacket(this.entity.getX());
        this.yp = ClientboundMoveEntityPacket.entityToPacket(this.entity.getY());
        this.zp = ClientboundMoveEntityPacket.entityToPacket(this.entity.getZ());
    }

    public Vec3 sentPos() {
        return ClientboundMoveEntityPacket.packetToEntity(this.xp, this.yp, this.zp);
    }

    private void broadcastAndSend(Packet<?> packet) {
        this.broadcast.accept(packet);
        if (this.entity instanceof ServerPlayer) {
            ((ServerPlayer)this.entity).connection.send(packet);
        }
    }
}

