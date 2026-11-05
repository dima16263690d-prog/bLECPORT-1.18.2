/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class ClientboundUpdateMobEffectPacket
implements Packet<ClientGamePacketListener> {
    private static final int FLAG_AMBIENT = 1;
    private static final int FLAG_VISIBLE = 2;
    private static final int FLAG_SHOW_ICON = 4;
    private final int entityId;
    private final int effectId;
    private final byte effectAmplifier;
    private final int effectDurationTicks;
    private final byte flags;

    public ClientboundUpdateMobEffectPacket(int n, MobEffectInstance mobEffectInstance) {
        this.entityId = n;
        this.effectId = MobEffect.getId(mobEffectInstance.getEffect());
        this.effectAmplifier = (byte)(mobEffectInstance.getAmplifier() & 0xFF);
        this.effectDurationTicks = mobEffectInstance.getDuration() > Short.MAX_VALUE ? Short.MAX_VALUE : mobEffectInstance.getDuration();
        byte by = 0;
        if (mobEffectInstance.isAmbient()) {
            by = (byte)(by | 1);
        }
        if (mobEffectInstance.isVisible()) {
            by = (byte)(by | 2);
        }
        if (mobEffectInstance.showIcon()) {
            by = (byte)(by | 4);
        }
        this.flags = by;
    }

    public ClientboundUpdateMobEffectPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entityId = friendlyByteBuf.readVarInt();
        this.effectId = friendlyByteBuf.readVarInt();
        this.effectAmplifier = friendlyByteBuf.readByte();
        this.effectDurationTicks = friendlyByteBuf.readVarInt();
        this.flags = friendlyByteBuf.readByte();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.entityId);
        friendlyByteBuf.writeVarInt(this.effectId);
        friendlyByteBuf.writeByte(this.effectAmplifier);
        friendlyByteBuf.writeVarInt(this.effectDurationTicks);
        friendlyByteBuf.writeByte(this.flags);
    }

    public boolean isSuperLongDuration() {
        return this.effectDurationTicks == Short.MAX_VALUE;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleUpdateMobEffect(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public int getEffectId() {
        return this.effectId;
    }

    public byte getEffectAmplifier() {
        return this.effectAmplifier;
    }

    public int getEffectDurationTicks() {
        return this.effectDurationTicks;
    }

    public boolean isEffectVisible() {
        return (this.flags & 2) == 2;
    }

    public boolean isEffectAmbient() {
        return (this.flags & 1) == 1;
    }

    public boolean effectShowsIcon() {
        return (this.flags & 4) == 4;
    }
}

