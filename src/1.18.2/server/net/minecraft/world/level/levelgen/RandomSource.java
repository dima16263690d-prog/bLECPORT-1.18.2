/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public interface RandomSource {
    public RandomSource fork();

    public PositionalRandomFactory forkPositional();

    public void setSeed(long var1);

    public int nextInt();

    public int nextInt(int var1);

    default public int nextIntBetweenInclusive(int n, int n2) {
        return this.nextInt(n2 - n + 1) + n;
    }

    public long nextLong();

    public boolean nextBoolean();

    public float nextFloat();

    public double nextDouble();

    public double nextGaussian();

    default public void consumeCount(int n) {
        for (int i = 0; i < n; ++i) {
            this.nextInt();
        }
    }
}

