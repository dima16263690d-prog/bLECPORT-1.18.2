/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LightEventListener;

public interface LayerLightEventListener
extends LightEventListener {
    @Nullable
    public DataLayer getDataLayerData(SectionPos var1);

    public int getLightValue(BlockPos var1);

    public static enum DummyLightLayerEventListener implements LayerLightEventListener
    {
        INSTANCE;


        @Override
        @Nullable
        public DataLayer getDataLayerData(SectionPos sectionPos) {
            return null;
        }

        @Override
        public int getLightValue(BlockPos blockPos) {
            return 0;
        }

        @Override
        public void checkBlock(BlockPos blockPos) {
        }

        @Override
        public void onBlockEmissionIncrease(BlockPos blockPos, int n) {
        }

        @Override
        public boolean hasLightWork() {
            return false;
        }

        @Override
        public int runUpdates(int n, boolean bl, boolean bl2) {
            return n;
        }

        @Override
        public void updateSectionStatus(SectionPos sectionPos, boolean bl) {
        }

        @Override
        public void enableLightSources(ChunkPos chunkPos, boolean bl) {
        }
    }
}

