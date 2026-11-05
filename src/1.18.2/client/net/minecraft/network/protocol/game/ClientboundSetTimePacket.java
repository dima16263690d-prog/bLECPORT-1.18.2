/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundSetTimePacket
implements Packet<ClientGamePacketListener> {
    private final long gameTime;
    private final long dayTime;

    public ClientboundSetTimePacket(long l, long l2, boolean bl) {
        this.gameTime = l;
        long l3 = l2;
        if (!bl && (l3 = -l3) == 0L) {
            l3 = -1L;
        }
        this.dayTime = l3;
    }

    public ClientboundSetTimePacket(FriendlyByteBuf friendlyByteBuf) {
        this.gameTime = friendlyByteBuf.readLong();
        this.dayTime = friendlyByteBuf.readLong();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeLong(this.gameTime);
        friendlyByteBuf.writeLong(this.dayTime);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetTime(this);
    }

    public long getGameTime() {
        return this.gameTime;
    }

    public long getDayTime() {
        return this.dayTime;
    }
}

