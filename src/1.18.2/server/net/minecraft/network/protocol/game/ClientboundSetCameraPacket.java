/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundSetCameraPacket
implements Packet<ClientGamePacketListener> {
    private final int cameraId;

    public ClientboundSetCameraPacket(Entity entity) {
        this.cameraId = entity.getId();
    }

    public ClientboundSetCameraPacket(FriendlyByteBuf friendlyByteBuf) {
        this.cameraId = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.cameraId);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetCamera(this);
    }

    @Nullable
    public Entity getEntity(Level level) {
        return level.getEntity(this.cameraId);
    }
}

