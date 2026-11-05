/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;

public interface PositionSourceType<T extends PositionSource> {
    public static final PositionSourceType<BlockPositionSource> BLOCK = PositionSourceType.register("block", new BlockPositionSource.Type());
    public static final PositionSourceType<EntityPositionSource> ENTITY = PositionSourceType.register("entity", new EntityPositionSource.Type());

    public T read(FriendlyByteBuf var1);

    public void write(FriendlyByteBuf var1, T var2);

    public Codec<T> codec();

    public static <S extends PositionSourceType<T>, T extends PositionSource> S register(String string, S s) {
        return (S)Registry.register(Registry.POSITION_SOURCE_TYPE, string, s);
    }

    public static PositionSource fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
        return Registry.POSITION_SOURCE_TYPE.getOptional(resourceLocation).orElseThrow(() -> new IllegalArgumentException("Unknown position source type " + resourceLocation)).read(friendlyByteBuf);
    }

    public static <T extends PositionSource> void toNetwork(T t, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(Registry.POSITION_SOURCE_TYPE.getKey(t.getType()));
        t.getType().write(friendlyByteBuf, t);
    }
}

