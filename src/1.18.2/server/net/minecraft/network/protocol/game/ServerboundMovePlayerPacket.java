/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public abstract class ServerboundMovePlayerPacket
implements Packet<ServerGamePacketListener> {
    protected final double x;
    protected final double y;
    protected final double z;
    protected final float yRot;
    protected final float xRot;
    protected final boolean onGround;
    protected final boolean hasPos;
    protected final boolean hasRot;

    protected ServerboundMovePlayerPacket(double d, double d2, double d3, float f, float f2, boolean bl, boolean bl2, boolean bl3) {
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.yRot = f;
        this.xRot = f2;
        this.onGround = bl;
        this.hasPos = bl2;
        this.hasRot = bl3;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleMovePlayer(this);
    }

    public double getX(double d) {
        return this.hasPos ? this.x : d;
    }

    public double getY(double d) {
        return this.hasPos ? this.y : d;
    }

    public double getZ(double d) {
        return this.hasPos ? this.z : d;
    }

    public float getYRot(float f) {
        return this.hasRot ? this.yRot : f;
    }

    public float getXRot(float f) {
        return this.hasRot ? this.xRot : f;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public boolean hasPosition() {
        return this.hasPos;
    }

    public boolean hasRotation() {
        return this.hasRot;
    }

    public static class StatusOnly
    extends ServerboundMovePlayerPacket {
        public StatusOnly(boolean bl) {
            super(0.0, 0.0, 0.0, 0.0f, 0.0f, bl, false, false);
        }

        public static StatusOnly read(FriendlyByteBuf friendlyByteBuf) {
            boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
            return new StatusOnly(bl);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
        }
    }

    public static class Rot
    extends ServerboundMovePlayerPacket {
        public Rot(float f, float f2, boolean bl) {
            super(0.0, 0.0, 0.0, f, f2, bl, false, true);
        }

        public static Rot read(FriendlyByteBuf friendlyByteBuf) {
            float f = friendlyByteBuf.readFloat();
            float f2 = friendlyByteBuf.readFloat();
            boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
            return new Rot(f, f2, bl);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeFloat(this.yRot);
            friendlyByteBuf.writeFloat(this.xRot);
            friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
        }
    }

    public static class Pos
    extends ServerboundMovePlayerPacket {
        public Pos(double d, double d2, double d3, boolean bl) {
            super(d, d2, d3, 0.0f, 0.0f, bl, true, false);
        }

        public static Pos read(FriendlyByteBuf friendlyByteBuf) {
            double d = friendlyByteBuf.readDouble();
            double d2 = friendlyByteBuf.readDouble();
            double d3 = friendlyByteBuf.readDouble();
            boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
            return new Pos(d, d2, d3, bl);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeDouble(this.x);
            friendlyByteBuf.writeDouble(this.y);
            friendlyByteBuf.writeDouble(this.z);
            friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
        }
    }

    public static class PosRot
    extends ServerboundMovePlayerPacket {
        public PosRot(double d, double d2, double d3, float f, float f2, boolean bl) {
            super(d, d2, d3, f, f2, bl, true, true);
        }

        public static PosRot read(FriendlyByteBuf friendlyByteBuf) {
            double d = friendlyByteBuf.readDouble();
            double d2 = friendlyByteBuf.readDouble();
            double d3 = friendlyByteBuf.readDouble();
            float f = friendlyByteBuf.readFloat();
            float f2 = friendlyByteBuf.readFloat();
            boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
            return new PosRot(d, d2, d3, f, f2, bl);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeDouble(this.x);
            friendlyByteBuf.writeDouble(this.y);
            friendlyByteBuf.writeDouble(this.z);
            friendlyByteBuf.writeFloat(this.yRot);
            friendlyByteBuf.writeFloat(this.xRot);
            friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
        }
    }
}

