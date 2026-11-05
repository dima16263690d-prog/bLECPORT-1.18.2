/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundEntityTagQuery
implements Packet<ServerGamePacketListener> {
    private final int transactionId;
    private final int entityId;

    public ServerboundEntityTagQuery(int n, int n2) {
        this.transactionId = n;
        this.entityId = n2;
    }

    public ServerboundEntityTagQuery(FriendlyByteBuf friendlyByteBuf) {
        this.transactionId = friendlyByteBuf.readVarInt();
        this.entityId = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.transactionId);
        friendlyByteBuf.writeVarInt(this.entityId);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleEntityTagQuery(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public int getEntityId() {
        return this.entityId;
    }
}

