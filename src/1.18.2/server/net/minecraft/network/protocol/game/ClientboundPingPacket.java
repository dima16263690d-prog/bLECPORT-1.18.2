/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundPingPacket
implements Packet<ClientGamePacketListener> {
    private final int id;

    public ClientboundPingPacket(int n) {
        this.id = n;
    }

    public ClientboundPingPacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.id);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handlePing(this);
    }

    public int getId() {
        return this.id;
    }
}

