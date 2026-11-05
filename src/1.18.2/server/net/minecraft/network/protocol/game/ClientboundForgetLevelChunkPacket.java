/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundForgetLevelChunkPacket
implements Packet<ClientGamePacketListener> {
    private final int x;
    private final int z;

    public ClientboundForgetLevelChunkPacket(int n, int n2) {
        this.x = n;
        this.z = n2;
    }

    public ClientboundForgetLevelChunkPacket(FriendlyByteBuf friendlyByteBuf) {
        this.x = friendlyByteBuf.readInt();
        this.z = friendlyByteBuf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.x);
        friendlyByteBuf.writeInt(this.z);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleForgetLevelChunk(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }
}

