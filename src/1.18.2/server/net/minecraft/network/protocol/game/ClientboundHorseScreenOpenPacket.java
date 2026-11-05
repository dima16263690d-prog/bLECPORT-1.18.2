/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundHorseScreenOpenPacket
implements Packet<ClientGamePacketListener> {
    private final int containerId;
    private final int size;
    private final int entityId;

    public ClientboundHorseScreenOpenPacket(int n, int n2, int n3) {
        this.containerId = n;
        this.size = n2;
        this.entityId = n3;
    }

    public ClientboundHorseScreenOpenPacket(FriendlyByteBuf friendlyByteBuf) {
        this.containerId = friendlyByteBuf.readUnsignedByte();
        this.size = friendlyByteBuf.readVarInt();
        this.entityId = friendlyByteBuf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.containerId);
        friendlyByteBuf.writeVarInt(this.size);
        friendlyByteBuf.writeInt(this.entityId);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleHorseScreenOpen(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSize() {
        return this.size;
    }

    public int getEntityId() {
        return this.entityId;
    }
}

