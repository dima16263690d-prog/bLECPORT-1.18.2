/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundPickItemPacket
implements Packet<ServerGamePacketListener> {
    private final int slot;

    public ServerboundPickItemPacket(int n) {
        this.slot = n;
    }

    public ServerboundPickItemPacket(FriendlyByteBuf friendlyByteBuf) {
        this.slot = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.slot);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handlePickItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}

