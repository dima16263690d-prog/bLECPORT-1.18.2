/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.login;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;

public class ServerboundCustomQueryPacket
implements Packet<ServerLoginPacketListener> {
    private static final int MAX_PAYLOAD_SIZE = 0x100000;
    private final int transactionId;
    @Nullable
    private final FriendlyByteBuf data;

    public ServerboundCustomQueryPacket(int n, @Nullable FriendlyByteBuf friendlyByteBuf) {
        this.transactionId = n;
        this.data = friendlyByteBuf;
    }

    public ServerboundCustomQueryPacket(FriendlyByteBuf friendlyByteBuf) {
        this.transactionId = friendlyByteBuf.readVarInt();
        if (friendlyByteBuf.readBoolean()) {
            int n = friendlyByteBuf.readableBytes();
            if (n < 0 || n > 0x100000) {
                throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
            }
            this.data = new FriendlyByteBuf(friendlyByteBuf.readBytes(n));
        } else {
            this.data = null;
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.transactionId);
        if (this.data != null) {
            friendlyByteBuf.writeBoolean(true);
            friendlyByteBuf.writeBytes(this.data.copy());
        } else {
            friendlyByteBuf.writeBoolean(false);
        }
    }

    @Override
    public void handle(ServerLoginPacketListener serverLoginPacketListener) {
        serverLoginPacketListener.handleCustomQueryPacket(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    @Nullable
    public FriendlyByteBuf getData() {
        return this.data;
    }
}

