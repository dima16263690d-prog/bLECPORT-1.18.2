/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.Util;

public final class SerializableUUID {
    public static final Codec<UUID> CODEC = Codec.INT_STREAM.comapFlatMap(intStream -> Util.fixedSize(intStream, 4).map(SerializableUUID::uuidFromIntArray), uUID -> Arrays.stream(SerializableUUID.uuidToIntArray(uUID)));

    private SerializableUUID() {
    }

    public static UUID uuidFromIntArray(int[] nArray) {
        return new UUID((long)nArray[0] << 32 | (long)nArray[1] & 0xFFFFFFFFL, (long)nArray[2] << 32 | (long)nArray[3] & 0xFFFFFFFFL);
    }

    public static int[] uuidToIntArray(UUID uUID) {
        long l = uUID.getMostSignificantBits();
        long l2 = uUID.getLeastSignificantBits();
        return SerializableUUID.leastMostToIntArray(l, l2);
    }

    private static int[] leastMostToIntArray(long l, long l2) {
        return new int[]{(int)(l >> 32), (int)l, (int)(l2 >> 32), (int)l2};
    }

    public static UUID readUUID(Dynamic<?> dynamic) {
        int[] nArray = dynamic.asIntStream().toArray();
        if (nArray.length != 4) {
            throw new IllegalArgumentException("Could not read UUID. Expected int-array of length 4, got " + nArray.length + ".");
        }
        return SerializableUUID.uuidFromIntArray(nArray);
    }
}

