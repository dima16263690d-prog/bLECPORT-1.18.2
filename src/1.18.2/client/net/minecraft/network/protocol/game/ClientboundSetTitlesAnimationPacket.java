/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundSetTitlesAnimationPacket
implements Packet<ClientGamePacketListener> {
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public ClientboundSetTitlesAnimationPacket(int n, int n2, int n3) {
        this.fadeIn = n;
        this.stay = n2;
        this.fadeOut = n3;
    }

    public ClientboundSetTitlesAnimationPacket(FriendlyByteBuf friendlyByteBuf) {
        this.fadeIn = friendlyByteBuf.readInt();
        this.stay = friendlyByteBuf.readInt();
        this.fadeOut = friendlyByteBuf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.fadeIn);
        friendlyByteBuf.writeInt(this.stay);
        friendlyByteBuf.writeInt(this.fadeOut);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.setTitlesAnimation(this);
    }

    public int getFadeIn() {
        return this.fadeIn;
    }

    public int getStay() {
        return this.stay;
    }

    public int getFadeOut() {
        return this.fadeOut;
    }
}

