/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundPongPacket
implements Packet<ServerGamePacketListener> {
    private final int id;

    public ServerboundPongPacket(int n) {
        this.id = n;
    }

    public ServerboundPongPacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.id);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handlePong(this);
    }

    public int getId() {
        return this.id;
    }
}

