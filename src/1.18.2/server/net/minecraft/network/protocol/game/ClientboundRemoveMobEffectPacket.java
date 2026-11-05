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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundRemoveMobEffectPacket
implements Packet<ClientGamePacketListener> {
    private final int entityId;
    private final MobEffect effect;

    public ClientboundRemoveMobEffectPacket(int n, MobEffect mobEffect) {
        this.entityId = n;
        this.effect = mobEffect;
    }

    public ClientboundRemoveMobEffectPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entityId = friendlyByteBuf.readVarInt();
        this.effect = MobEffect.byId(friendlyByteBuf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.entityId);
        friendlyByteBuf.writeVarInt(MobEffect.getId(this.effect));
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleRemoveMobEffect(this);
    }

    @Nullable
    public Entity getEntity(Level level) {
        return level.getEntity(this.entityId);
    }

    @Nullable
    public MobEffect getEffect() {
        return this.effect;
    }
}

