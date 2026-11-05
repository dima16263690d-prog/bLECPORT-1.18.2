/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  it.unimi.dsi.fastutil.objects.ObjectListIterator
 */
package net.minecraft.world.level.levelgen;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.feature.NoiseEffect;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Beardifier
implements DensityFunctions.BeardifierOrMarker {
    public static final int BEARD_KERNEL_RADIUS = 12;
    private static final int BEARD_KERNEL_SIZE = 24;
    private static final float[] BEARD_KERNEL = Util.make(new float[13824], fArray -> {
        for (int i = 0; i < 24; ++i) {
            for (int j = 0; j < 24; ++j) {
                for (int k = 0; k < 24; ++k) {
                    fArray[i * 24 * 24 + j * 24 + k] = (float)Beardifier.computeBeardContribution(j - 12, k - 12, i - 12);
                }
            }
        }
    });
    private final ObjectList<StructurePiece> rigids;
    private final ObjectList<JigsawJunction> junctions;
    private final ObjectListIterator<StructurePiece> pieceIterator;
    private final ObjectListIterator<JigsawJunction> junctionIterator;

    protected Beardifier(StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        ChunkPos chunkPos = chunkAccess.getPos();
        int n = chunkPos.getMinBlockX();
        int n2 = chunkPos.getMinBlockZ();
        this.junctions = new ObjectArrayList(32);
        this.rigids = new ObjectArrayList(10);
        structureFeatureManager.startsForFeature(SectionPos.bottomOf(chunkAccess), configuredStructureFeature -> configuredStructureFeature.adaptNoise).forEach(structureStart -> {
            for (StructurePiece structurePiece : structureStart.getPieces()) {
                if (!structurePiece.isCloseToChunk(chunkPos, 12)) continue;
                if (structurePiece instanceof PoolElementStructurePiece) {
                    PoolElementStructurePiece poolElementStructurePiece = (PoolElementStructurePiece)structurePiece;
                    StructureTemplatePool.Projection projection = poolElementStructurePiece.getElement().getProjection();
                    if (projection == StructureTemplatePool.Projection.RIGID) {
                        this.rigids.add((Object)poolElementStructurePiece);
                    }
                    for (JigsawJunction jigsawJunction : poolElementStructurePiece.getJunctions()) {
                        int n3 = jigsawJunction.getSourceX();
                        int n4 = jigsawJunction.getSourceZ();
                        if (n3 <= n - 12 || n4 <= n2 - 12 || n3 >= n + 15 + 12 || n4 >= n2 + 15 + 12) continue;
                        this.junctions.add((Object)jigsawJunction);
                    }
                    continue;
                }
                this.rigids.add((Object)structurePiece);
            }
        });
        this.pieceIterator = this.rigids.iterator();
        this.junctionIterator = this.junctions.iterator();
    }

    @Override
    public double compute(DensityFunction.FunctionContext functionContext) {
        int n;
        int n2;
        Object object;
        int n3 = functionContext.blockX();
        int n4 = functionContext.blockY();
        int n5 = functionContext.blockZ();
        double d = 0.0;
        while (this.pieceIterator.hasNext()) {
            object = (StructurePiece)this.pieceIterator.next();
            BoundingBox boundingBox = ((StructurePiece)object).getBoundingBox();
            n2 = Math.max(0, Math.max(boundingBox.minX() - n3, n3 - boundingBox.maxX()));
            n = n4 - (boundingBox.minY() + (object instanceof PoolElementStructurePiece ? ((PoolElementStructurePiece)object).getGroundLevelDelta() : 0));
            int n6 = Math.max(0, Math.max(boundingBox.minZ() - n5, n5 - boundingBox.maxZ()));
            NoiseEffect noiseEffect = ((StructurePiece)object).getNoiseEffect();
            if (noiseEffect == NoiseEffect.BURY) {
                d += Beardifier.getBuryContribution(n2, n, n6);
                continue;
            }
            if (noiseEffect != NoiseEffect.BEARD) continue;
            d += Beardifier.getBeardContribution(n2, n, n6) * 0.8;
        }
        this.pieceIterator.back(this.rigids.size());
        while (this.junctionIterator.hasNext()) {
            object = (JigsawJunction)this.junctionIterator.next();
            int n7 = n3 - ((JigsawJunction)object).getSourceX();
            n2 = n4 - ((JigsawJunction)object).getSourceGroundY();
            n = n5 - ((JigsawJunction)object).getSourceZ();
            d += Beardifier.getBeardContribution(n7, n2, n) * 0.4;
        }
        this.junctionIterator.back(this.junctions.size());
        return d;
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    private static double getBuryContribution(int n, int n2, int n3) {
        double d = Mth.length(n, (double)n2 / 2.0, n3);
        return Mth.clampedMap(d, 0.0, 6.0, 1.0, 0.0);
    }

    private static double getBeardContribution(int n, int n2, int n3) {
        int n4 = n + 12;
        int n5 = n2 + 12;
        int n6 = n3 + 12;
        if (n4 < 0 || n4 >= 24) {
            return 0.0;
        }
        if (n5 < 0 || n5 >= 24) {
            return 0.0;
        }
        if (n6 < 0 || n6 >= 24) {
            return 0.0;
        }
        return BEARD_KERNEL[n6 * 24 * 24 + n4 * 24 + n5];
    }

    private static double computeBeardContribution(int n, int n2, int n3) {
        double d = n * n + n3 * n3;
        double d2 = (double)n2 + 0.5;
        double d3 = d2 * d2;
        double d4 = Math.pow(Math.E, -(d3 / 16.0 + d / 16.0));
        double d5 = -d2 * Mth.fastInvSqrt(d3 / 2.0 + d / 2.0) / 2.0;
        return d5 * d4;
    }
}

