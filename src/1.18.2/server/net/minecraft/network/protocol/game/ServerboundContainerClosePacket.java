/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundContainerClosePacket
implements Packet<ServerGamePacketListener> {
    private final int containerId;

    public ServerboundContainerClosePacket(int n) {
        this.containerId = n;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleContainerClose(this);
    }

    public ServerboundContainerClosePacket(FriendlyByteBuf friendlyByteBuf) {
        this.containerId = friendlyByteBuf.readByte();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.containerId);
    }

    public int getContainerId() {
        return this.containerId;
    }
}

