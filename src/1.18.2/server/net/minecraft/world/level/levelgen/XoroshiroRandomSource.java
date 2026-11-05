/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Charsets
 *  com.google.common.hash.HashFunction
 *  com.google.common.hash.Hashing
 *  com.google.common.primitives.Longs
 */
package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.MarsagliaPolarGaussian;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.Xoroshiro128PlusPlus;

public class XoroshiroRandomSource
implements RandomSource {
    private static final float FLOAT_UNIT = 5.9604645E-8f;
    private static final double DOUBLE_UNIT = (double)1.110223E-16f;
    private Xoroshiro128PlusPlus randomNumberGenerator;
    private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

    public XoroshiroRandomSource(long l) {
        this.randomNumberGenerator = new Xoroshiro128PlusPlus(RandomSupport.upgradeSeedTo128bit(l));
    }

    public XoroshiroRandomSource(long l, long l2) {
        this.randomNumberGenerator = new Xoroshiro128PlusPlus(l, l2);
    }

    @Override
    public RandomSource fork() {
        return new XoroshiroRandomSource(this.randomNumberGenerator.nextLong(), this.randomNumberGenerator.nextLong());
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return new XoroshiroPositionalRandomFactory(this.randomNumberGenerator.nextLong(), this.randomNumberGenerator.nextLong());
    }

    @Override
    public void setSeed(long l) {
        this.randomNumberGenerator = new Xoroshiro128PlusPlus(RandomSupport.upgradeSeedTo128bit(l));
        this.gaussianSource.reset();
    }

    @Override
    public int nextInt() {
        return (int)this.randomNumberGenerator.nextLong();
    }

    @Override
    public int nextInt(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }
        long l = Integer.toUnsignedLong(this.nextInt());
        long l2 = l * (long)n;
        long l3 = l2 & 0xFFFFFFFFL;
        if (l3 < (long)n) {
            int n2 = Integer.remainderUnsigned(~n + 1, n);
            while (l3 < (long)n2) {
                l = Integer.toUnsignedLong(this.nextInt());
                l2 = l * (long)n;
                l3 = l2 & 0xFFFFFFFFL;
            }
        }
        long l4 = l2 >> 32;
        return (int)l4;
    }

    @Override
    public long nextLong() {
        return this.randomNumberGenerator.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return (this.randomNumberGenerator.nextLong() & 1L) != 0L;
    }

    @Override
    public float nextFloat() {
        return (float)this.nextBits(24) * 5.9604645E-8f;
    }

    @Override
    public double nextDouble() {
        return (double)this.nextBits(53) * (double)1.110223E-16f;
    }

    @Override
    public double nextGaussian() {
        return this.gaussianSource.nextGaussian();
    }

    @Override
    public void consumeCount(int n) {
        for (int i = 0; i < n; ++i) {
            this.randomNumberGenerator.nextLong();
        }
    }

    private long nextBits(int n) {
        return this.randomNumberGenerator.nextLong() >>> 64 - n;
    }

    public static class XoroshiroPositionalRandomFactory
    implements PositionalRandomFactory {
        private static final HashFunction MD5_128 = Hashing.md5();
        private final long seedLo;
        private final long seedHi;

        public XoroshiroPositionalRandomFactory(long l, long l2) {
            this.seedLo = l;
            this.seedHi = l2;
        }

        @Override
        public RandomSource at(int n, int n2, int n3) {
            long l = Mth.getSeed(n, n2, n3);
            long l2 = l ^ this.seedLo;
            return new XoroshiroRandomSource(l2, this.seedHi);
        }

        @Override
        public RandomSource fromHashOf(String string) {
            byte[] byArray = MD5_128.hashString((CharSequence)string, Charsets.UTF_8).asBytes();
            long l = Longs.fromBytes((byte)byArray[0], (byte)byArray[1], (byte)byArray[2], (byte)byArray[3], (byte)byArray[4], (byte)byArray[5], (byte)byArray[6], (byte)byArray[7]);
            long l2 = Longs.fromBytes((byte)byArray[8], (byte)byArray[9], (byte)byArray[10], (byte)byArray[11], (byte)byArray[12], (byte)byArray[13], (byte)byArray[14], (byte)byArray[15]);
            return new XoroshiroRandomSource(l ^ this.seedLo, l2 ^ this.seedHi);
        }

        @Override
        @VisibleForTesting
        public void parityConfigString(StringBuilder stringBuilder) {
            stringBuilder.append("seedLo: ").append(this.seedLo).append(", seedHi: ").append(this.seedHi);
        }
    }
}

