/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundSetCarriedItemPacket
implements Packet<ClientGamePacketListener> {
    private final int slot;

    public ClientboundSetCarriedItemPacket(int n) {
        this.slot = n;
    }

    public ClientboundSetCarriedItemPacket(FriendlyByteBuf friendlyByteBuf) {
        this.slot = friendlyByteBuf.readByte();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.slot);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetCarriedItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}

