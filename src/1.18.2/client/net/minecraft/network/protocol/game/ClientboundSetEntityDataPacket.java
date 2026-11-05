/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;

public class ClientboundSetEntityDataPacket
implements Packet<ClientGamePacketListener> {
    private final int id;
    @Nullable
    private final List<SynchedEntityData.DataItem<?>> packedItems;

    public ClientboundSetEntityDataPacket(int n, SynchedEntityData synchedEntityData, boolean bl) {
        this.id = n;
        if (bl) {
            this.packedItems = synchedEntityData.getAll();
            synchedEntityData.clearDirty();
        } else {
            this.packedItems = synchedEntityData.packDirty();
        }
    }

    public ClientboundSetEntityDataPacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readVarInt();
        this.packedItems = SynchedEntityData.unpack(friendlyByteBuf);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.id);
        SynchedEntityData.pack(this.packedItems, friendlyByteBuf);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetEntityData(this);
    }

    @Nullable
    public List<SynchedEntityData.DataItem<?>> getUnpackedData() {
        return this.packedItems;
    }

    public int getId() {
        return this.id;
    }
}

