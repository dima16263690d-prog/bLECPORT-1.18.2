/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundSetChunkCacheRadiusPacket
implements Packet<ClientGamePacketListener> {
    private final int radius;

    public ClientboundSetChunkCacheRadiusPacket(int n) {
        this.radius = n;
    }

    public ClientboundSetChunkCacheRadiusPacket(FriendlyByteBuf friendlyByteBuf) {
        this.radius = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.radius);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetChunkCacheRadius(this);
    }

    public int getRadius() {
        return this.radius;
    }
}

