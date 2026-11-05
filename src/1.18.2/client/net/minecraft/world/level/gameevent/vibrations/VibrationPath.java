/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;

public class VibrationPath {
    public static final Codec<VibrationPath> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockPos.CODEC.fieldOf("origin").forGetter(vibrationPath -> vibrationPath.origin), (App)PositionSource.CODEC.fieldOf("destination").forGetter(vibrationPath -> vibrationPath.destination), (App)Codec.INT.fieldOf("arrival_in_ticks").forGetter(vibrationPath -> vibrationPath.arrivalInTicks)).apply((Applicative)instance, VibrationPath::new));
    private final BlockPos origin;
    private final PositionSource destination;
    private final int arrivalInTicks;

    public VibrationPath(BlockPos blockPos, PositionSource positionSource, int n) {
        this.origin = blockPos;
        this.destination = positionSource;
        this.arrivalInTicks = n;
    }

    public int getArrivalInTicks() {
        return this.arrivalInTicks;
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public PositionSource getDestination() {
        return this.destination;
    }

    public static VibrationPath read(FriendlyByteBuf friendlyByteBuf) {
        BlockPos blockPos = friendlyByteBuf.readBlockPos();
        PositionSource positionSource = PositionSourceType.fromNetwork(friendlyByteBuf);
        int n = friendlyByteBuf.readVarInt();
        return new VibrationPath(blockPos, positionSource, n);
    }

    public static void write(FriendlyByteBuf friendlyByteBuf, VibrationPath vibrationPath) {
        friendlyByteBuf.writeBlockPos(vibrationPath.origin);
        PositionSourceType.toNetwork(vibrationPath.destination, friendlyByteBuf);
        friendlyByteBuf.writeVarInt(vibrationPath.arrivalInTicks);
    }
}

