/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.entity.player.Abilities;

public class ServerboundPlayerAbilitiesPacket
implements Packet<ServerGamePacketListener> {
    private static final int FLAG_FLYING = 2;
    private final boolean isFlying;

    public ServerboundPlayerAbilitiesPacket(Abilities abilities) {
        this.isFlying = abilities.flying;
    }

    public ServerboundPlayerAbilitiesPacket(FriendlyByteBuf friendlyByteBuf) {
        byte by = friendlyByteBuf.readByte();
        this.isFlying = (by & 2) != 0;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        int n = 0;
        if (this.isFlying) {
            n = (byte)(n | 2);
        }
        friendlyByteBuf.writeByte(n);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handlePlayerAbilities(this);
    }

    public boolean isFlying() {
        return this.isFlying;
    }
}

