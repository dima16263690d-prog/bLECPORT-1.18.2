/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundSetChunkCacheCenterPacket
implements Packet<ClientGamePacketListener> {
    private final int x;
    private final int z;

    public ClientboundSetChunkCacheCenterPacket(int n, int n2) {
        this.x = n;
        this.z = n2;
    }

    public ClientboundSetChunkCacheCenterPacket(FriendlyByteBuf friendlyByteBuf) {
        this.x = friendlyByteBuf.readVarInt();
        this.z = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.x);
        friendlyByteBuf.writeVarInt(this.z);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetChunkCacheCenter(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }
}

