/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCustomQueryPacket
implements Packet<ClientLoginPacketListener> {
    private static final int MAX_PAYLOAD_SIZE = 0x100000;
    private final int transactionId;
    private final ResourceLocation identifier;
    private final FriendlyByteBuf data;

    public ClientboundCustomQueryPacket(int n, ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        this.transactionId = n;
        this.identifier = resourceLocation;
        this.data = friendlyByteBuf;
    }

    public ClientboundCustomQueryPacket(FriendlyByteBuf friendlyByteBuf) {
        this.transactionId = friendlyByteBuf.readVarInt();
        this.identifier = friendlyByteBuf.readResourceLocation();
        int n = friendlyByteBuf.readableBytes();
        if (n < 0 || n > 0x100000) {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
        this.data = new FriendlyByteBuf(friendlyByteBuf.readBytes(n));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.transactionId);
        friendlyByteBuf.writeResourceLocation(this.identifier);
        friendlyByteBuf.writeBytes(this.data.copy());
    }

    @Override
    public void handle(ClientLoginPacketListener clientLoginPacketListener) {
        clientLoginPacketListener.handleCustomQuery(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public ResourceLocation getIdentifier() {
        return this.identifier;
    }

    public FriendlyByteBuf getData() {
        return this.data;
    }
}

