/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundPlayerCombatEnterPacket
implements Packet<ClientGamePacketListener> {
    public ClientboundPlayerCombatEnterPacket() {
    }

    public ClientboundPlayerCombatEnterPacket(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handlePlayerCombatEnter(this);
    }
}

