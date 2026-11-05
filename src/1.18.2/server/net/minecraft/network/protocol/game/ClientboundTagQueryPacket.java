/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundTagQueryPacket
implements Packet<ClientGamePacketListener> {
    private final int transactionId;
    @Nullable
    private final CompoundTag tag;

    public ClientboundTagQueryPacket(int n, @Nullable CompoundTag compoundTag) {
        this.transactionId = n;
        this.tag = compoundTag;
    }

    public ClientboundTagQueryPacket(FriendlyByteBuf friendlyByteBuf) {
        this.transactionId = friendlyByteBuf.readVarInt();
        this.tag = friendlyByteBuf.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.transactionId);
        friendlyByteBuf.writeNbt(this.tag);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleTagQueryPacket(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    @Nullable
    public CompoundTag getTag() {
        return this.tag;
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}

