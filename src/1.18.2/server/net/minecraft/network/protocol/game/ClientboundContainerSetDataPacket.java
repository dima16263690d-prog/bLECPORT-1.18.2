/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundContainerSetDataPacket
implements Packet<ClientGamePacketListener> {
    private final int containerId;
    private final int id;
    private final int value;

    public ClientboundContainerSetDataPacket(int n, int n2, int n3) {
        this.containerId = n;
        this.id = n2;
        this.value = n3;
    }

    public ClientboundContainerSetDataPacket(FriendlyByteBuf friendlyByteBuf) {
        this.containerId = friendlyByteBuf.readUnsignedByte();
        this.id = friendlyByteBuf.readShort();
        this.value = friendlyByteBuf.readShort();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.containerId);
        friendlyByteBuf.writeShort(this.id);
        friendlyByteBuf.writeShort(this.value);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleContainerSetData(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getId() {
        return this.id;
    }

    public int getValue() {
        return this.value;
    }
}

