/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundKeepAlivePacket
implements Packet<ClientGamePacketListener> {
    private final long id;

    public ClientboundKeepAlivePacket(long l) {
        this.id = l;
    }

    public ClientboundKeepAlivePacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readLong();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeLong(this.id);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleKeepAlive(this);
    }

    public long getId() {
        return this.id;
    }
}

