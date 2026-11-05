/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.levelgen.RandomSupport;

public class Xoroshiro128PlusPlus {
    private long seedLo;
    private long seedHi;

    public Xoroshiro128PlusPlus(RandomSupport.Seed128bit seed128bit) {
        this(seed128bit.seedLo(), seed128bit.seedHi());
    }

    public Xoroshiro128PlusPlus(long l, long l2) {
        this.seedLo = l;
        this.seedHi = l2;
        if ((this.seedLo | this.seedHi) == 0L) {
            this.seedLo = -7046029254386353131L;
            this.seedHi = 7640891576956012809L;
        }
    }

    public long nextLong() {
        long l = this.seedLo;
        long l2 = this.seedHi;
        long l3 = Long.rotateLeft(l + l2, 17) + l;
        this.seedLo = Long.rotateLeft(l, 49) ^ (l2 ^= l) ^ l2 << 21;
        this.seedHi = Long.rotateLeft(l2, 28);
        return l3;
    }
}

