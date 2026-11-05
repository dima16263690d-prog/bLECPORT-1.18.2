/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ClientboundExplodePacket
implements Packet<ClientGamePacketListener> {
    private final double x;
    private final double y;
    private final double z;
    private final float power;
    private final List<BlockPos> toBlow;
    private final float knockbackX;
    private final float knockbackY;
    private final float knockbackZ;

    public ClientboundExplodePacket(double d, double d2, double d3, float f, List<BlockPos> list, @Nullable Vec3 vec3) {
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.power = f;
        this.toBlow = Lists.newArrayList(list);
        if (vec3 != null) {
            this.knockbackX = (float)vec3.x;
            this.knockbackY = (float)vec3.y;
            this.knockbackZ = (float)vec3.z;
        } else {
            this.knockbackX = 0.0f;
            this.knockbackY = 0.0f;
            this.knockbackZ = 0.0f;
        }
    }

    public ClientboundExplodePacket(FriendlyByteBuf friendlyByteBuf2) {
        this.x = friendlyByteBuf2.readFloat();
        this.y = friendlyByteBuf2.readFloat();
        this.z = friendlyByteBuf2.readFloat();
        this.power = friendlyByteBuf2.readFloat();
        int n = Mth.floor(this.x);
        int n2 = Mth.floor(this.y);
        int n3 = Mth.floor(this.z);
        this.toBlow = friendlyByteBuf2.readList(friendlyByteBuf -> {
            int n4 = friendlyByteBuf.readByte() + n;
            int n5 = friendlyByteBuf.readByte() + n2;
            int n6 = friendlyByteBuf.readByte() + n3;
            return new BlockPos(n4, n5, n6);
        });
        this.knockbackX = friendlyByteBuf2.readFloat();
        this.knockbackY = friendlyByteBuf2.readFloat();
        this.knockbackZ = friendlyByteBuf2.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeFloat((float)this.x);
        friendlyByteBuf2.writeFloat((float)this.y);
        friendlyByteBuf2.writeFloat((float)this.z);
        friendlyByteBuf2.writeFloat(this.power);
        int n = Mth.floor(this.x);
        int n2 = Mth.floor(this.y);
        int n3 = Mth.floor(this.z);
        friendlyByteBuf2.writeCollection(this.toBlow, (friendlyByteBuf, blockPos) -> {
            int n4 = blockPos.getX() - n;
            int n5 = blockPos.getY() - n2;
            int n6 = blockPos.getZ() - n3;
            friendlyByteBuf.writeByte(n4);
            friendlyByteBuf.writeByte(n5);
            friendlyByteBuf.writeByte(n6);
        });
        friendlyByteBuf2.writeFloat(this.knockbackX);
        friendlyByteBuf2.writeFloat(this.knockbackY);
        friendlyByteBuf2.writeFloat(this.knockbackZ);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleExplosion(this);
    }

    public float getKnockbackX() {
        return this.knockbackX;
    }

    public float getKnockbackY() {
        return this.knockbackY;
    }

    public float getKnockbackZ() {
        return this.knockbackZ;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getPower() {
        return this.power;
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }
}

