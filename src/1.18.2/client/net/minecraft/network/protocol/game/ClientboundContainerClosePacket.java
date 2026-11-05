/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundContainerClosePacket
implements Packet<ClientGamePacketListener> {
    private final int containerId;

    public ClientboundContainerClosePacket(int n) {
        this.containerId = n;
    }

    public ClientboundContainerClosePacket(FriendlyByteBuf friendlyByteBuf) {
        this.containerId = friendlyByteBuf.readUnsignedByte();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.containerId);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleContainerClose(this);
    }

    public int getContainerId() {
        return this.containerId;
    }
}

