/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;

public class ClientboundLoginCompressionPacket
implements Packet<ClientLoginPacketListener> {
    private final int compressionThreshold;

    public ClientboundLoginCompressionPacket(int n) {
        this.compressionThreshold = n;
    }

    public ClientboundLoginCompressionPacket(FriendlyByteBuf friendlyByteBuf) {
        this.compressionThreshold = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.compressionThreshold);
    }

    @Override
    public void handle(ClientLoginPacketListener clientLoginPacketListener) {
        clientLoginPacketListener.handleCompression(this);
    }

    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }
}

