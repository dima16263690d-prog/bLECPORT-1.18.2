/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundRemoveEntitiesPacket
implements Packet<ClientGamePacketListener> {
    private final IntList entityIds;

    public ClientboundRemoveEntitiesPacket(IntList intList) {
        this.entityIds = new IntArrayList(intList);
    }

    public ClientboundRemoveEntitiesPacket(int ... nArray) {
        this.entityIds = new IntArrayList(nArray);
    }

    public ClientboundRemoveEntitiesPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entityIds = friendlyByteBuf.readIntIdList();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeIntIdList(this.entityIds);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleRemoveEntities(this);
    }

    public IntList getEntityIds() {
        return this.entityIds;
    }
}

