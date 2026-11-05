/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public class CaveWorldCarver
extends WorldCarver<CaveCarverConfiguration> {
    public CaveWorldCarver(Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration caveCarverConfiguration, Random random) {
        return random.nextFloat() <= caveCarverConfiguration.probability;
    }

    @Override
    public boolean carve(CarvingContext carvingContext2, CaveCarverConfiguration caveCarverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Holder<Biome>> function, Random random, Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {
        int n2 = SectionPos.sectionToBlockCoord(this.getRange() * 2 - 1);
        int n3 = random.nextInt(random.nextInt(random.nextInt(this.getCaveBound()) + 1) + 1);
        for (int i = 0; i < n3; ++i) {
            float f;
            double d = chunkPos.getBlockX(random.nextInt(16));
            double d5 = caveCarverConfiguration.y.sample(random, carvingContext2);
            double d6 = chunkPos.getBlockZ(random.nextInt(16));
            double d7 = caveCarverConfiguration.horizontalRadiusMultiplier.sample(random);
            double d8 = caveCarverConfiguration.verticalRadiusMultiplier.sample(random);
            double d9 = caveCarverConfiguration.floorLevel.sample(random);
            WorldCarver.CarveSkipChecker carveSkipChecker = (carvingContext, d2, d3, d4, n) -> CaveWorldCarver.shouldSkip(d2, d3, d4, d9);
            int n4 = 1;
            if (random.nextInt(4) == 0) {
                double d10 = caveCarverConfiguration.yScale.sample(random);
                f = 1.0f + random.nextFloat() * 6.0f;
                this.createRoom(carvingContext2, caveCarverConfiguration, chunkAccess, function, aquifer, d, d5, d6, f, d10, carvingMask, carveSkipChecker);
                n4 += random.nextInt(4);
            }
            for (int j = 0; j < n4; ++j) {
                float f2 = random.nextFloat() * ((float)Math.PI * 2);
                f = (random.nextFloat() - 0.5f) / 4.0f;
                float f3 = this.getThickness(random);
                int n5 = n2 - random.nextInt(n2 / 4);
                boolean bl = false;
                this.createTunnel(carvingContext2, caveCarverConfiguration, chunkAccess, function, random.nextLong(), aquifer, d, d5, d6, d7, d8, f3, f2, f, 0, n5, this.getYScale(), carvingMask, carveSkipChecker);
            }
        }
        return true;
    }

    protected int getCaveBound() {
        return 15;
    }

    protected float getThickness(Random random) {
        float f = random.nextFloat() * 2.0f + random.nextFloat();
        if (random.nextInt(10) == 0) {
            f *= random.nextFloat() * random.nextFloat() * 3.0f + 1.0f;
        }
        return f;
    }

    protected double getYScale() {
        return 1.0;
    }

    protected void createRoom(CarvingContext carvingContext, CaveCarverConfiguration caveCarverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Holder<Biome>> function, Aquifer aquifer, double d, double d2, double d3, float f, double d4, CarvingMask carvingMask, WorldCarver.CarveSkipChecker carveSkipChecker) {
        double d5 = 1.5 + (double)(Mth.sin(1.5707964f) * f);
        double d6 = d5 * d4;
        this.carveEllipsoid(carvingContext, caveCarverConfiguration, chunkAccess, function, aquifer, d + 1.0, d2, d3, d5, d6, carvingMask, carveSkipChecker);
    }

    protected void createTunnel(CarvingContext carvingContext, CaveCarverConfiguration caveCarverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Holder<Biome>> function, long l, Aquifer aquifer, double d, double d2, double d3, double d4, double d5, float f, float f2, float f3, int n, int n2, double d6, CarvingMask carvingMask, WorldCarver.CarveSkipChecker carveSkipChecker) {
        Random random = new Random(l);
        int n3 = random.nextInt(n2 / 2) + n2 / 4;
        boolean bl = random.nextInt(6) == 0;
        float f4 = 0.0f;
        float f5 = 0.0f;
        for (int i = n; i < n2; ++i) {
            double d7 = 1.5 + (double)(Mth.sin((float)Math.PI * (float)i / (float)n2) * f);
            double d8 = d7 * d6;
            float f6 = Mth.cos(f3);
            d += (double)(Mth.cos(f2) * f6);
            d2 += (double)Mth.sin(f3);
            d3 += (double)(Mth.sin(f2) * f6);
            f3 *= bl ? 0.92f : 0.7f;
            f3 += f5 * 0.1f;
            f2 += f4 * 0.1f;
            f5 *= 0.9f;
            f4 *= 0.75f;
            f5 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f;
            f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f;
            if (i == n3 && f > 1.0f) {
                this.createTunnel(carvingContext, caveCarverConfiguration, chunkAccess, function, random.nextLong(), aquifer, d, d2, d3, d4, d5, random.nextFloat() * 0.5f + 0.5f, f2 - 1.5707964f, f3 / 3.0f, i, n2, 1.0, carvingMask, carveSkipChecker);
                this.createTunnel(carvingContext, caveCarverConfiguration, chunkAccess, function, random.nextLong(), aquifer, d, d2, d3, d4, d5, random.nextFloat() * 0.5f + 0.5f, f2 + 1.5707964f, f3 / 3.0f, i, n2, 1.0, carvingMask, carveSkipChecker);
                return;
            }
            if (random.nextInt(4) == 0) continue;
            if (!CaveWorldCarver.canReach(chunkAccess.getPos(), d, d3, i, n2, f)) {
                return;
            }
            this.carveEllipsoid(carvingContext, caveCarverConfiguration, chunkAccess, function, aquifer, d, d2, d3, d7 * d4, d8 * d5, carvingMask, carveSkipChecker);
        }
    }

    private static boolean shouldSkip(double d, double d2, double d3, double d4) {
        if (d2 <= d4) {
            return true;
        }
        return d * d + d2 * d2 + d3 * d3 >= 1.0;
    }
}

