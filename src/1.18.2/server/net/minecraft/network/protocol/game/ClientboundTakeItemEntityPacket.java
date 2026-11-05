/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundTakeItemEntityPacket
implements Packet<ClientGamePacketListener> {
    private final int itemId;
    private final int playerId;
    private final int amount;

    public ClientboundTakeItemEntityPacket(int n, int n2, int n3) {
        this.itemId = n;
        this.playerId = n2;
        this.amount = n3;
    }

    public ClientboundTakeItemEntityPacket(FriendlyByteBuf friendlyByteBuf) {
        this.itemId = friendlyByteBuf.readVarInt();
        this.playerId = friendlyByteBuf.readVarInt();
        this.amount = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.itemId);
        friendlyByteBuf.writeVarInt(this.playerId);
        friendlyByteBuf.writeVarInt(this.amount);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleTakeItemEntity(this);
    }

    public int getItemId() {
        return this.itemId;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public int getAmount() {
        return this.amount;
    }
}

