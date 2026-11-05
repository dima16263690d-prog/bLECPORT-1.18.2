/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundSetHealthPacket
implements Packet<ClientGamePacketListener> {
    private final float health;
    private final int food;
    private final float saturation;

    public ClientboundSetHealthPacket(float f, int n, float f2) {
        this.health = f;
        this.food = n;
        this.saturation = f2;
    }

    public ClientboundSetHealthPacket(FriendlyByteBuf friendlyByteBuf) {
        this.health = friendlyByteBuf.readFloat();
        this.food = friendlyByteBuf.readVarInt();
        this.saturation = friendlyByteBuf.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeFloat(this.health);
        friendlyByteBuf.writeVarInt(this.food);
        friendlyByteBuf.writeFloat(this.saturation);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetHealth(this);
    }

    public float getHealth() {
        return this.health;
    }

    public int getFood() {
        return this.food;
    }

    public float getSaturation() {
        return this.saturation;
    }
}

