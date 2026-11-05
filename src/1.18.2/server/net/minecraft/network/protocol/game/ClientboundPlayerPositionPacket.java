/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.EnumSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundPlayerPositionPacket
implements Packet<ClientGamePacketListener> {
    private final double x;
    private final double y;
    private final double z;
    private final float yRot;
    private final float xRot;
    private final Set<RelativeArgument> relativeArguments;
    private final int id;
    private final boolean dismountVehicle;

    public ClientboundPlayerPositionPacket(double d, double d2, double d3, float f, float f2, Set<RelativeArgument> set, int n, boolean bl) {
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.yRot = f;
        this.xRot = f2;
        this.relativeArguments = set;
        this.id = n;
        this.dismountVehicle = bl;
    }

    public ClientboundPlayerPositionPacket(FriendlyByteBuf friendlyByteBuf) {
        this.x = friendlyByteBuf.readDouble();
        this.y = friendlyByteBuf.readDouble();
        this.z = friendlyByteBuf.readDouble();
        this.yRot = friendlyByteBuf.readFloat();
        this.xRot = friendlyByteBuf.readFloat();
        this.relativeArguments = RelativeArgument.unpack(friendlyByteBuf.readUnsignedByte());
        this.id = friendlyByteBuf.readVarInt();
        this.dismountVehicle = friendlyByteBuf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeDouble(this.x);
        friendlyByteBuf.writeDouble(this.y);
        friendlyByteBuf.writeDouble(this.z);
        friendlyByteBuf.writeFloat(this.yRot);
        friendlyByteBuf.writeFloat(this.xRot);
        friendlyByteBuf.writeByte(RelativeArgument.pack(this.relativeArguments));
        friendlyByteBuf.writeVarInt(this.id);
        friendlyByteBuf.writeBoolean(this.dismountVehicle);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleMovePlayer(this);
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

    public float getYRot() {
        return this.yRot;
    }

    public float getXRot() {
        return this.xRot;
    }

    public int getId() {
        return this.id;
    }

    public boolean requestDismountVehicle() {
        return this.dismountVehicle;
    }

    public Set<RelativeArgument> getRelativeArguments() {
        return this.relativeArguments;
    }

    public static enum RelativeArgument {
        X(0),
        Y(1),
        Z(2),
        Y_ROT(3),
        X_ROT(4);

        private final int bit;

        private RelativeArgument(int n2) {
            this.bit = n2;
        }

        private int getMask() {
            return 1 << this.bit;
        }

        private boolean isSet(int n) {
            return (n & this.getMask()) == this.getMask();
        }

        public static Set<RelativeArgument> unpack(int n) {
            EnumSet<RelativeArgument> enumSet = EnumSet.noneOf(RelativeArgument.class);
            for (RelativeArgument relativeArgument : RelativeArgument.values()) {
                if (!relativeArgument.isSet(n)) continue;
                enumSet.add(relativeArgument);
            }
            return enumSet;
        }

        public static int pack(Set<RelativeArgument> set) {
            int n = 0;
            for (RelativeArgument relativeArgument : set) {
                n |= relativeArgument.getMask();
            }
            return n;
        }
    }
}

