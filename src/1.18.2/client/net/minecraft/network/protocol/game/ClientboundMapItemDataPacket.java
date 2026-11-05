/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class ClientboundMapItemDataPacket
implements Packet<ClientGamePacketListener> {
    private final int mapId;
    private final byte scale;
    private final boolean locked;
    @Nullable
    private final List<MapDecoration> decorations;
    @Nullable
    private final MapItemSavedData.MapPatch colorPatch;

    public ClientboundMapItemDataPacket(int n, byte by, boolean bl, @Nullable Collection<MapDecoration> collection, @Nullable MapItemSavedData.MapPatch mapPatch) {
        this.mapId = n;
        this.scale = by;
        this.locked = bl;
        this.decorations = collection != null ? Lists.newArrayList(collection) : null;
        this.colorPatch = mapPatch;
    }

    public ClientboundMapItemDataPacket(FriendlyByteBuf friendlyByteBuf2) {
        this.mapId = friendlyByteBuf2.readVarInt();
        this.scale = friendlyByteBuf2.readByte();
        this.locked = friendlyByteBuf2.readBoolean();
        this.decorations = friendlyByteBuf2.readBoolean() ? friendlyByteBuf2.readList(friendlyByteBuf -> {
            MapDecoration.Type type = friendlyByteBuf.readEnum(MapDecoration.Type.class);
            return new MapDecoration(type, friendlyByteBuf.readByte(), friendlyByteBuf.readByte(), (byte)(friendlyByteBuf.readByte() & 0xF), friendlyByteBuf.readBoolean() ? friendlyByteBuf.readComponent() : null);
        }) : null;
        short s = friendlyByteBuf2.readUnsignedByte();
        if (s > 0) {
            short s2 = friendlyByteBuf2.readUnsignedByte();
            short s3 = friendlyByteBuf2.readUnsignedByte();
            short s4 = friendlyByteBuf2.readUnsignedByte();
            byte[] byArray = friendlyByteBuf2.readByteArray();
            this.colorPatch = new MapItemSavedData.MapPatch(s3, s4, s, s2, byArray);
        } else {
            this.colorPatch = null;
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeVarInt(this.mapId);
        friendlyByteBuf2.writeByte(this.scale);
        friendlyByteBuf2.writeBoolean(this.locked);
        if (this.decorations != null) {
            friendlyByteBuf2.writeBoolean(true);
            friendlyByteBuf2.writeCollection(this.decorations, (friendlyByteBuf, mapDecoration) -> {
                friendlyByteBuf.writeEnum(mapDecoration.getType());
                friendlyByteBuf.writeByte(mapDecoration.getX());
                friendlyByteBuf.writeByte(mapDecoration.getY());
                friendlyByteBuf.writeByte(mapDecoration.getRot() & 0xF);
                if (mapDecoration.getName() != null) {
                    friendlyByteBuf.writeBoolean(true);
                    friendlyByteBuf.writeComponent(mapDecoration.getName());
                } else {
                    friendlyByteBuf.writeBoolean(false);
                }
            });
        } else {
            friendlyByteBuf2.writeBoolean(false);
        }
        if (this.colorPatch != null) {
            friendlyByteBuf2.writeByte(this.colorPatch.width);
            friendlyByteBuf2.writeByte(this.colorPatch.height);
            friendlyByteBuf2.writeByte(this.colorPatch.startX);
            friendlyByteBuf2.writeByte(this.colorPatch.startY);
            friendlyByteBuf2.writeByteArray(this.colorPatch.mapColors);
        } else {
            friendlyByteBuf2.writeByte(0);
        }
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleMapItemData(this);
    }

    public int getMapId() {
        return this.mapId;
    }

    public void applyToMap(MapItemSavedData mapItemSavedData) {
        if (this.decorations != null) {
            mapItemSavedData.addClientSideDecorations(this.decorations);
        }
        if (this.colorPatch != null) {
            this.colorPatch.applyToMap(mapItemSavedData);
        }
    }

    public byte getScale() {
        return this.scale;
    }

    public boolean isLocked() {
        return this.locked;
    }
}

