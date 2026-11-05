/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class ClientboundAddEntityPacket
implements Packet<ClientGamePacketListener> {
    public static final double MAGICAL_QUANTIZATION = 8000.0;
    private final int id;
    private final UUID uuid;
    private final double x;
    private final double y;
    private final double z;
    private final int xa;
    private final int ya;
    private final int za;
    private final int xRot;
    private final int yRot;
    private final EntityType<?> type;
    private final int data;
    public static final double LIMIT = 3.9;

    public ClientboundAddEntityPacket(int n, UUID uUID, double d, double d2, double d3, float f, float f2, EntityType<?> entityType, int n2, Vec3 vec3) {
        this.id = n;
        this.uuid = uUID;
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.xRot = Mth.floor(f * 256.0f / 360.0f);
        this.yRot = Mth.floor(f2 * 256.0f / 360.0f);
        this.type = entityType;
        this.data = n2;
        this.xa = (int)(Mth.clamp(vec3.x, -3.9, 3.9) * 8000.0);
        this.ya = (int)(Mth.clamp(vec3.y, -3.9, 3.9) * 8000.0);
        this.za = (int)(Mth.clamp(vec3.z, -3.9, 3.9) * 8000.0);
    }

    public ClientboundAddEntityPacket(Entity entity) {
        this(entity, 0);
    }

    public ClientboundAddEntityPacket(Entity entity, int n) {
        this(entity.getId(), entity.getUUID(), entity.getX(), entity.getY(), entity.getZ(), entity.getXRot(), entity.getYRot(), entity.getType(), n, entity.getDeltaMovement());
    }

    public ClientboundAddEntityPacket(Entity entity, EntityType<?> entityType, int n, BlockPos blockPos) {
        this(entity.getId(), entity.getUUID(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), entity.getXRot(), entity.getYRot(), entityType, n, entity.getDeltaMovement());
    }

    public ClientboundAddEntityPacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readVarInt();
        this.uuid = friendlyByteBuf.readUUID();
        this.type = Registry.ENTITY_TYPE.byId(friendlyByteBuf.readVarInt());
        this.x = friendlyByteBuf.readDouble();
        this.y = friendlyByteBuf.readDouble();
        this.z = friendlyByteBuf.readDouble();
        this.xRot = friendlyByteBuf.readByte();
        this.yRot = friendlyByteBuf.readByte();
        this.data = friendlyByteBuf.readInt();
        this.xa = friendlyByteBuf.readShort();
        this.ya = friendlyByteBuf.readShort();
        this.za = friendlyByteBuf.readShort();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.id);
        friendlyByteBuf.writeUUID(this.uuid);
        friendlyByteBuf.writeVarInt(Registry.ENTITY_TYPE.getId(this.type));
        friendlyByteBuf.writeDouble(this.x);
        friendlyByteBuf.writeDouble(this.y);
        friendlyByteBuf.writeDouble(this.z);
        friendlyByteBuf.writeByte(this.xRot);
        friendlyByteBuf.writeByte(this.yRot);
        friendlyByteBuf.writeInt(this.data);
        friendlyByteBuf.writeShort(this.xa);
        friendlyByteBuf.writeShort(this.ya);
        friendlyByteBuf.writeShort(this.za);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleAddEntity(this);
    }

    public int getId() {
        return this.id;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public double getXa() {
        return (double)this.xa / 8000.0;
    }

    public double getYa() {
        return (double)this.ya / 8000.0;
    }

    public double getZa() {
        return (double)this.za / 8000.0;
    }

    public int getxRot() {
        return this.xRot;
    }

    public int getyRot() {
        return this.yRot;
    }

    public EntityType<?> getType() {
        return this.type;
    }

    public int getData() {
        return this.data;
    }
}

