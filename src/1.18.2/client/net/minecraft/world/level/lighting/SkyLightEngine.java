/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.lighting.SkyLightSectionStorage;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableInt;

public final class SkyLightEngine
extends LayerLightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public SkyLightEngine(LightChunkGetter lightChunkGetter) {
        super(lightChunkGetter, LightLayer.SKY, new SkyLightSectionStorage(lightChunkGetter));
    }

    @Override
    protected int computeLevelFromNeighbor(long l, long l2, int n) {
        boolean bl;
        VoxelShape voxelShape;
        int n2;
        int n3;
        if (l2 == Long.MAX_VALUE || l == Long.MAX_VALUE) {
            return 15;
        }
        if (n >= 15) {
            return n;
        }
        MutableInt mutableInt = new MutableInt();
        BlockState blockState = this.getStateAndOpacity(l2, mutableInt);
        if (mutableInt.getValue() >= 15) {
            return 15;
        }
        int n4 = BlockPos.getX(l);
        int n5 = BlockPos.getY(l);
        int n6 = BlockPos.getZ(l);
        int n7 = BlockPos.getX(l2);
        int n8 = BlockPos.getY(l2);
        int n9 = BlockPos.getZ(l2);
        int n10 = Integer.signum(n7 - n4);
        Direction direction = Direction.fromNormal(n10, n3 = Integer.signum(n8 - n5), n2 = Integer.signum(n9 - n6));
        if (direction == null) {
            throw new IllegalStateException(String.format("Light was spread in illegal direction %d, %d, %d", n10, n3, n2));
        }
        BlockState blockState2 = this.getStateAndOpacity(l, null);
        VoxelShape voxelShape2 = this.getShape(blockState2, l, direction);
        if (Shapes.faceShapeOccludes(voxelShape2, voxelShape = this.getShape(blockState, l2, direction.getOpposite()))) {
            return 15;
        }
        boolean bl2 = n4 == n7 && n6 == n9;
        boolean bl3 = bl = bl2 && n5 > n8;
        if (bl && n == 0 && mutableInt.getValue() == 0) {
            return 0;
        }
        return n + Math.max(1, mutableInt.getValue());
    }

    @Override
    protected void checkNeighborsAfterUpdate(long l, int n, boolean bl) {
        long l2;
        long l3;
        int n2;
        long l4 = SectionPos.blockToSection(l);
        int n3 = BlockPos.getY(l);
        int n4 = SectionPos.sectionRelative(n3);
        int n5 = SectionPos.blockToSectionCoord(n3);
        if (n4 != 0) {
            n2 = 0;
        } else {
            int n6 = 0;
            while (!((SkyLightSectionStorage)this.storage).storingLightForSection(SectionPos.offset(l4, 0, -n6 - 1, 0)) && ((SkyLightSectionStorage)this.storage).hasSectionsBelow(n5 - n6 - 1)) {
                ++n6;
            }
            n2 = n6;
        }
        long l5 = BlockPos.offset(l, 0, -1 - n2 * 16, 0);
        long l6 = SectionPos.blockToSection(l5);
        if (l4 == l6 || ((SkyLightSectionStorage)this.storage).storingLightForSection(l6)) {
            this.checkNeighbor(l, l5, n, bl);
        }
        if (l4 == (l3 = SectionPos.blockToSection(l2 = BlockPos.offset(l, Direction.UP))) || ((SkyLightSectionStorage)this.storage).storingLightForSection(l3)) {
            this.checkNeighbor(l, l2, n, bl);
        }
        block1: for (Direction direction : HORIZONTALS) {
            int n7 = 0;
            do {
                long l7;
                long l8;
                if (l4 == (l8 = SectionPos.blockToSection(l7 = BlockPos.offset(l, direction.getStepX(), -n7, direction.getStepZ())))) {
                    this.checkNeighbor(l, l7, n, bl);
                    continue block1;
                }
                if (!((SkyLightSectionStorage)this.storage).storingLightForSection(l8)) continue;
                long l9 = BlockPos.offset(l, 0, -n7, 0);
                this.checkNeighbor(l9, l7, n, bl);
            } while (++n7 <= n2 * 16);
        }
    }

    @Override
    protected int getComputedLevel(long l, long l2, int n) {
        int n2 = n;
        long l3 = SectionPos.blockToSection(l);
        DataLayer dataLayer = ((SkyLightSectionStorage)this.storage).getDataLayer(l3, true);
        for (Direction direction : DIRECTIONS) {
            int n3;
            long l4 = BlockPos.offset(l, direction);
            if (l4 == l2) continue;
            long l5 = SectionPos.blockToSection(l4);
            DataLayer dataLayer2 = l3 == l5 ? dataLayer : ((SkyLightSectionStorage)this.storage).getDataLayer(l5, true);
            if (dataLayer2 != null) {
                n3 = this.getLevel(dataLayer2, l4);
            } else {
                if (direction == Direction.DOWN) continue;
                n3 = 15 - ((SkyLightSectionStorage)this.storage).getLightValue(l4, true);
            }
            int n4 = this.computeLevelFromNeighbor(l4, l, n3);
            if (n2 > n4) {
                n2 = n4;
            }
            if (n2 != 0) continue;
            return n2;
        }
        return n2;
    }

    @Override
    protected void checkNode(long l) {
        ((SkyLightSectionStorage)this.storage).runAllUpdates();
        long l2 = SectionPos.blockToSection(l);
        if (((SkyLightSectionStorage)this.storage).storingLightForSection(l2)) {
            super.checkNode(l);
        } else {
            l = BlockPos.getFlatIndex(l);
            while (!((SkyLightSectionStorage)this.storage).storingLightForSection(l2) && !((SkyLightSectionStorage)this.storage).isAboveData(l2)) {
                l2 = SectionPos.offset(l2, Direction.UP);
                l = BlockPos.offset(l, 0, 16, 0);
            }
            if (((SkyLightSectionStorage)this.storage).storingLightForSection(l2)) {
                super.checkNode(l);
            }
        }
    }

    @Override
    public String getDebugData(long l) {
        return super.getDebugData(l) + (((SkyLightSectionStorage)this.storage).isAboveData(l) ? "*" : "");
    }
}

