/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundSetCarriedItemPacket
implements Packet<ServerGamePacketListener> {
    private final int slot;

    public ServerboundSetCarriedItemPacket(int n) {
        this.slot = n;
    }

    public ServerboundSetCarriedItemPacket(FriendlyByteBuf friendlyByteBuf) {
        this.slot = friendlyByteBuf.readShort();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeShort(this.slot);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleSetCarriedItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}

