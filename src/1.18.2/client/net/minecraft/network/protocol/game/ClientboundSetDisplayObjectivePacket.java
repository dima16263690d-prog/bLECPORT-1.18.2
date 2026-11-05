/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.scores.Objective;

public class ClientboundSetDisplayObjectivePacket
implements Packet<ClientGamePacketListener> {
    private final int slot;
    private final String objectiveName;

    public ClientboundSetDisplayObjectivePacket(int n, @Nullable Objective objective) {
        this.slot = n;
        this.objectiveName = objective == null ? "" : objective.getName();
    }

    public ClientboundSetDisplayObjectivePacket(FriendlyByteBuf friendlyByteBuf) {
        this.slot = friendlyByteBuf.readByte();
        this.objectiveName = friendlyByteBuf.readUtf();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.slot);
        friendlyByteBuf.writeUtf(this.objectiveName);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetDisplayObjective(this);
    }

    public int getSlot() {
        return this.slot;
    }

    @Nullable
    public String getObjectiveName() {
        return Objects.equals(this.objectiveName, "") ? null : this.objectiveName;
    }
}

