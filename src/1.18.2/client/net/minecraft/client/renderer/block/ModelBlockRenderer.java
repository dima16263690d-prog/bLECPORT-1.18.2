/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ModelBlockRenderer {
    private static final int FACE_CUBIC = 0;
    private static final int FACE_PARTIAL = 1;
    static final Direction[] DIRECTIONS = Direction.values();
    private final BlockColors blockColors;
    private static final int CACHE_SIZE = 100;
    static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);

    public ModelBlockRenderer(BlockColors blockColors) {
        this.blockColors = blockColors;
    }

    public boolean tesselateBlock(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, Random random, long l, int n) {
        boolean bl2 = Minecraft.useAmbientOcclusion() && blockState.getLightEmission() == 0 && bakedModel.useAmbientOcclusion();
        Vec3 vec3 = blockState.getOffset(blockAndTintGetter, blockPos);
        poseStack.translate(vec3.x, vec3.y, vec3.z);
        try {
            if (bl2) {
                return this.tesselateWithAO(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, random, l, n);
            }
            return this.tesselateWithoutAO(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, random, l, n);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Tesselating block model");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block model being tesselated");
            CrashReportCategory.populateBlockDetails(crashReportCategory, blockAndTintGetter, blockPos, blockState);
            crashReportCategory.setDetail("Using AO", bl2);
            throw new ReportedException(crashReport);
        }
    }

    public boolean tesselateWithAO(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, Random random, long l, int n) {
        boolean bl2 = false;
        float[] fArray = new float[DIRECTIONS.length * 2];
        BitSet bitSet = new BitSet(3);
        AmbientOcclusionFace ambientOcclusionFace = new AmbientOcclusionFace();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (Direction direction : DIRECTIONS) {
            random.setSeed(l);
            List<BakedQuad> list = bakedModel.getQuads(blockState, direction, random);
            if (list.isEmpty()) continue;
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            if (bl && !Block.shouldRenderFace(blockState, blockAndTintGetter, blockPos, direction, mutableBlockPos)) continue;
            this.renderModelFaceAO(blockAndTintGetter, blockState, blockPos, poseStack, vertexConsumer, list, fArray, bitSet, ambientOcclusionFace, n);
            bl2 = true;
        }
        random.setSeed(l);
        List<BakedQuad> list = bakedModel.getQuads(blockState, null, random);
        if (!list.isEmpty()) {
            this.renderModelFaceAO(blockAndTintGetter, blockState, blockPos, poseStack, vertexConsumer, list, fArray, bitSet, ambientOcclusionFace, n);
            bl2 = true;
        }
        return bl2;
    }

    public boolean tesselateWithoutAO(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, Random random, long l, int n) {
        boolean bl2 = false;
        BitSet bitSet = new BitSet(3);
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (Direction direction : DIRECTIONS) {
            random.setSeed(l);
            List<BakedQuad> list = bakedModel.getQuads(blockState, direction, random);
            if (list.isEmpty()) continue;
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            if (bl && !Block.shouldRenderFace(blockState, blockAndTintGetter, blockPos, direction, mutableBlockPos)) continue;
            int n2 = LevelRenderer.getLightColor(blockAndTintGetter, blockState, mutableBlockPos);
            this.renderModelFaceFlat(blockAndTintGetter, blockState, blockPos, n2, n, false, poseStack, vertexConsumer, list, bitSet);
            bl2 = true;
        }
        random.setSeed(l);
        List<BakedQuad> list = bakedModel.getQuads(blockState, null, random);
        if (!list.isEmpty()) {
            this.renderModelFaceFlat(blockAndTintGetter, blockState, blockPos, -1, n, true, poseStack, vertexConsumer, list, bitSet);
            bl2 = true;
        }
        return bl2;
    }

    private void renderModelFaceAO(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, float[] fArray, BitSet bitSet, AmbientOcclusionFace ambientOcclusionFace, int n) {
        for (BakedQuad bakedQuad : list) {
            this.calculateShape(blockAndTintGetter, blockState, blockPos, bakedQuad.getVertices(), bakedQuad.getDirection(), fArray, bitSet);
            ambientOcclusionFace.calculate(blockAndTintGetter, blockState, blockPos, bakedQuad.getDirection(), fArray, bitSet, bakedQuad.isShade());
            this.putQuadData(blockAndTintGetter, blockState, blockPos, vertexConsumer, poseStack.last(), bakedQuad, ambientOcclusionFace.brightness[0], ambientOcclusionFace.brightness[1], ambientOcclusionFace.brightness[2], ambientOcclusionFace.brightness[3], ambientOcclusionFace.lightmap[0], ambientOcclusionFace.lightmap[1], ambientOcclusionFace.lightmap[2], ambientOcclusionFace.lightmap[3], n);
        }
    }

    private void putQuadData(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, VertexConsumer vertexConsumer, PoseStack.Pose pose, BakedQuad bakedQuad, float f, float f2, float f3, float f4, int n, int n2, int n3, int n4, int n5) {
        float f5;
        float f6;
        float f7;
        if (bakedQuad.isTinted()) {
            int n6 = this.blockColors.getColor(blockState, blockAndTintGetter, blockPos, bakedQuad.getTintIndex());
            f7 = (float)(n6 >> 16 & 0xFF) / 255.0f;
            f6 = (float)(n6 >> 8 & 0xFF) / 255.0f;
            f5 = (float)(n6 & 0xFF) / 255.0f;
        } else {
            f7 = 1.0f;
            f6 = 1.0f;
            f5 = 1.0f;
        }
        vertexConsumer.putBulkData(pose, bakedQuad, new float[]{f, f2, f3, f4}, f7, f6, f5, new int[]{n, n2, n3, n4}, n5, true);
    }

    private void calculateShape(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, int[] nArray, Direction direction, @Nullable float[] fArray, BitSet bitSet) {
        float f;
        int n;
        float f2 = 32.0f;
        float f3 = 32.0f;
        float f4 = 32.0f;
        float f5 = -32.0f;
        float f6 = -32.0f;
        float f7 = -32.0f;
        for (n = 0; n < 4; ++n) {
            f = Float.intBitsToFloat(nArray[n * 8]);
            float f8 = Float.intBitsToFloat(nArray[n * 8 + 1]);
            float f9 = Float.intBitsToFloat(nArray[n * 8 + 2]);
            f2 = Math.min(f2, f);
            f3 = Math.min(f3, f8);
            f4 = Math.min(f4, f9);
            f5 = Math.max(f5, f);
            f6 = Math.max(f6, f8);
            f7 = Math.max(f7, f9);
        }
        if (fArray != null) {
            fArray[Direction.WEST.get3DDataValue()] = f2;
            fArray[Direction.EAST.get3DDataValue()] = f5;
            fArray[Direction.DOWN.get3DDataValue()] = f3;
            fArray[Direction.UP.get3DDataValue()] = f6;
            fArray[Direction.NORTH.get3DDataValue()] = f4;
            fArray[Direction.SOUTH.get3DDataValue()] = f7;
            n = DIRECTIONS.length;
            fArray[Direction.WEST.get3DDataValue() + n] = 1.0f - f2;
            fArray[Direction.EAST.get3DDataValue() + n] = 1.0f - f5;
            fArray[Direction.DOWN.get3DDataValue() + n] = 1.0f - f3;
            fArray[Direction.UP.get3DDataValue() + n] = 1.0f - f6;
            fArray[Direction.NORTH.get3DDataValue() + n] = 1.0f - f4;
            fArray[Direction.SOUTH.get3DDataValue() + n] = 1.0f - f7;
        }
        float f10 = 1.0E-4f;
        f = 0.9999f;
        switch (direction) {
            case DOWN: {
                bitSet.set(1, f2 >= 1.0E-4f || f4 >= 1.0E-4f || f5 <= 0.9999f || f7 <= 0.9999f);
                bitSet.set(0, f3 == f6 && (f3 < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
                break;
            }
            case UP: {
                bitSet.set(1, f2 >= 1.0E-4f || f4 >= 1.0E-4f || f5 <= 0.9999f || f7 <= 0.9999f);
                bitSet.set(0, f3 == f6 && (f6 > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
                break;
            }
            case NORTH: {
                bitSet.set(1, f2 >= 1.0E-4f || f3 >= 1.0E-4f || f5 <= 0.9999f || f6 <= 0.9999f);
                bitSet.set(0, f4 == f7 && (f4 < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
                break;
            }
            case SOUTH: {
                bitSet.set(1, f2 >= 1.0E-4f || f3 >= 1.0E-4f || f5 <= 0.9999f || f6 <= 0.9999f);
                bitSet.set(0, f4 == f7 && (f7 > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
                break;
            }
            case WEST: {
                bitSet.set(1, f3 >= 1.0E-4f || f4 >= 1.0E-4f || f6 <= 0.9999f || f7 <= 0.9999f);
                bitSet.set(0, f2 == f5 && (f2 < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
                break;
            }
            case EAST: {
                bitSet.set(1, f3 >= 1.0E-4f || f4 >= 1.0E-4f || f6 <= 0.9999f || f7 <= 0.9999f);
                bitSet.set(0, f2 == f5 && (f5 > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
            }
        }
    }

    private void renderModelFaceFlat(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, int n, int n2, boolean bl, PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, BitSet bitSet) {
        for (BakedQuad bakedQuad : list) {
            if (bl) {
                this.calculateShape(blockAndTintGetter, blockState, blockPos, bakedQuad.getVertices(), bakedQuad.getDirection(), null, bitSet);
                BlockPos blockPos2 = bitSet.get(0) ? blockPos.relative(bakedQuad.getDirection()) : blockPos;
                n = LevelRenderer.getLightColor(blockAndTintGetter, blockState, blockPos2);
            }
            float f = blockAndTintGetter.getShade(bakedQuad.getDirection(), bakedQuad.isShade());
            this.putQuadData(blockAndTintGetter, blockState, blockPos, vertexConsumer, poseStack.last(), bakedQuad, f, f, f, f, n, n, n, n, n2);
        }
    }

    public void renderModel(PoseStack.Pose pose, VertexConsumer vertexConsumer, @Nullable BlockState blockState, BakedModel bakedModel, float f, float f2, float f3, int n, int n2) {
        Random random = new Random();
        long l = 42L;
        for (Direction direction : DIRECTIONS) {
            random.setSeed(42L);
            ModelBlockRenderer.renderQuadList(pose, vertexConsumer, f, f2, f3, bakedModel.getQuads(blockState, direction, random), n, n2);
        }
        random.setSeed(42L);
        ModelBlockRenderer.renderQuadList(pose, vertexConsumer, f, f2, f3, bakedModel.getQuads(blockState, null, random), n, n2);
    }

    private static void renderQuadList(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float f2, float f3, List<BakedQuad> list, int n, int n2) {
        for (BakedQuad bakedQuad : list) {
            float f4;
            float f5;
            float f6;
            if (bakedQuad.isTinted()) {
                f6 = Mth.clamp(f, 0.0f, 1.0f);
                f5 = Mth.clamp(f2, 0.0f, 1.0f);
                f4 = Mth.clamp(f3, 0.0f, 1.0f);
            } else {
                f6 = 1.0f;
                f5 = 1.0f;
                f4 = 1.0f;
            }
            vertexConsumer.putBulkData(pose, bakedQuad, f6, f5, f4, n, n2);
        }
    }

    public static void enableCaching() {
        CACHE.get().enable();
    }

    public static void clearCache() {
        CACHE.get().disable();
    }

    class AmbientOcclusionFace {
        final float[] brightness = new float[4];
        final int[] lightmap = new int[4];

        public void calculate(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, Direction direction, float[] fArray, BitSet bitSet, boolean bl) {
            float f;
            int n;
            float f2;
            int n2;
            float f3;
            int n3;
            float f4;
            int n4;
            float f5;
            BlockState blockState2;
            boolean bl2;
            BlockPos blockPos2 = bitSet.get(0) ? blockPos.relative(direction) : blockPos;
            AdjacencyInfo adjacencyInfo = AdjacencyInfo.fromFacing(direction);
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            Cache cache = CACHE.get();
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]);
            BlockState blockState3 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int n5 = cache.getLightColor(blockState3, blockAndTintGetter, mutableBlockPos);
            float f6 = cache.getShadeBrightness(blockState3, blockAndTintGetter, mutableBlockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]);
            BlockState blockState4 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int n6 = cache.getLightColor(blockState4, blockAndTintGetter, mutableBlockPos);
            float f7 = cache.getShadeBrightness(blockState4, blockAndTintGetter, mutableBlockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[2]);
            BlockState blockState5 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int n7 = cache.getLightColor(blockState5, blockAndTintGetter, mutableBlockPos);
            float f8 = cache.getShadeBrightness(blockState5, blockAndTintGetter, mutableBlockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[3]);
            BlockState blockState6 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int n8 = cache.getLightColor(blockState6, blockAndTintGetter, mutableBlockPos);
            float f9 = cache.getShadeBrightness(blockState6, blockAndTintGetter, mutableBlockPos);
            BlockState blockState7 = blockAndTintGetter.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]).move(direction));
            boolean bl3 = !blockState7.isViewBlocking(blockAndTintGetter, mutableBlockPos) || blockState7.getLightBlock(blockAndTintGetter, mutableBlockPos) == 0;
            BlockState blockState8 = blockAndTintGetter.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]).move(direction));
            boolean bl4 = !blockState8.isViewBlocking(blockAndTintGetter, mutableBlockPos) || blockState8.getLightBlock(blockAndTintGetter, mutableBlockPos) == 0;
            BlockState blockState9 = blockAndTintGetter.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[2]).move(direction));
            boolean bl5 = !blockState9.isViewBlocking(blockAndTintGetter, mutableBlockPos) || blockState9.getLightBlock(blockAndTintGetter, mutableBlockPos) == 0;
            BlockState blockState10 = blockAndTintGetter.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[3]).move(direction));
            boolean bl6 = bl2 = !blockState10.isViewBlocking(blockAndTintGetter, mutableBlockPos) || blockState10.getLightBlock(blockAndTintGetter, mutableBlockPos) == 0;
            if (bl5 || bl3) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[2]);
                blockState2 = blockAndTintGetter.getBlockState(mutableBlockPos);
                f5 = cache.getShadeBrightness(blockState2, blockAndTintGetter, mutableBlockPos);
                n4 = cache.getLightColor(blockState2, blockAndTintGetter, mutableBlockPos);
            } else {
                f5 = f6;
                n4 = n5;
            }
            if (bl2 || bl3) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[3]);
                blockState2 = blockAndTintGetter.getBlockState(mutableBlockPos);
                f4 = cache.getShadeBrightness(blockState2, blockAndTintGetter, mutableBlockPos);
                n3 = cache.getLightColor(blockState2, blockAndTintGetter, mutableBlockPos);
            } else {
                f4 = f6;
                n3 = n5;
            }
            if (bl5 || bl4) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[2]);
                blockState2 = blockAndTintGetter.getBlockState(mutableBlockPos);
                f3 = cache.getShadeBrightness(blockState2, blockAndTintGetter, mutableBlockPos);
                n2 = cache.getLightColor(blockState2, blockAndTintGetter, mutableBlockPos);
            } else {
                f3 = f6;
                n2 = n5;
            }
            if (bl2 || bl4) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[3]);
                blockState2 = blockAndTintGetter.getBlockState(mutableBlockPos);
                f2 = cache.getShadeBrightness(blockState2, blockAndTintGetter, mutableBlockPos);
                n = cache.getLightColor(blockState2, blockAndTintGetter, mutableBlockPos);
            } else {
                f2 = f6;
                n = n5;
            }
            int n9 = cache.getLightColor(blockState, blockAndTintGetter, blockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            BlockState blockState11 = blockAndTintGetter.getBlockState(mutableBlockPos);
            if (bitSet.get(0) || !blockState11.isSolidRender(blockAndTintGetter, mutableBlockPos)) {
                n9 = cache.getLightColor(blockState11, blockAndTintGetter, mutableBlockPos);
            }
            float f10 = bitSet.get(0) ? cache.getShadeBrightness(blockAndTintGetter.getBlockState(blockPos2), blockAndTintGetter, blockPos2) : cache.getShadeBrightness(blockAndTintGetter.getBlockState(blockPos), blockAndTintGetter, blockPos);
            AmbientVertexRemap ambientVertexRemap = AmbientVertexRemap.fromFacing(direction);
            if (!bitSet.get(1) || !adjacencyInfo.doNonCubicWeight) {
                f = (f9 + f6 + f4 + f10) * 0.25f;
                var45_46 = (f8 + f6 + f5 + f10) * 0.25f;
                var46_48 = (f8 + f7 + f3 + f10) * 0.25f;
                var47_49 = (f9 + f7 + f2 + f10) * 0.25f;
                this.lightmap[ambientVertexRemap.vert0] = this.blend(n8, n5, n3, n9);
                this.lightmap[ambientVertexRemap.vert1] = this.blend(n7, n5, n4, n9);
                this.lightmap[ambientVertexRemap.vert2] = this.blend(n7, n6, n2, n9);
                this.lightmap[ambientVertexRemap.vert3] = this.blend(n8, n6, n, n9);
                this.brightness[ambientVertexRemap.vert0] = f;
                this.brightness[ambientVertexRemap.vert1] = var45_46;
                this.brightness[ambientVertexRemap.vert2] = var46_48;
                this.brightness[ambientVertexRemap.vert3] = var47_49;
            } else {
                f = (f9 + f6 + f4 + f10) * 0.25f;
                var45_46 = (f8 + f6 + f5 + f10) * 0.25f;
                var46_48 = (f8 + f7 + f3 + f10) * 0.25f;
                var47_49 = (f9 + f7 + f2 + f10) * 0.25f;
                float f11 = fArray[adjacencyInfo.vert0Weights[0].shape] * fArray[adjacencyInfo.vert0Weights[1].shape];
                float f12 = fArray[adjacencyInfo.vert0Weights[2].shape] * fArray[adjacencyInfo.vert0Weights[3].shape];
                float f13 = fArray[adjacencyInfo.vert0Weights[4].shape] * fArray[adjacencyInfo.vert0Weights[5].shape];
                float f14 = fArray[adjacencyInfo.vert0Weights[6].shape] * fArray[adjacencyInfo.vert0Weights[7].shape];
                float f15 = fArray[adjacencyInfo.vert1Weights[0].shape] * fArray[adjacencyInfo.vert1Weights[1].shape];
                float f16 = fArray[adjacencyInfo.vert1Weights[2].shape] * fArray[adjacencyInfo.vert1Weights[3].shape];
                float f17 = fArray[adjacencyInfo.vert1Weights[4].shape] * fArray[adjacencyInfo.vert1Weights[5].shape];
                float f18 = fArray[adjacencyInfo.vert1Weights[6].shape] * fArray[adjacencyInfo.vert1Weights[7].shape];
                float f19 = fArray[adjacencyInfo.vert2Weights[0].shape] * fArray[adjacencyInfo.vert2Weights[1].shape];
                float f20 = fArray[adjacencyInfo.vert2Weights[2].shape] * fArray[adjacencyInfo.vert2Weights[3].shape];
                float f21 = fArray[adjacencyInfo.vert2Weights[4].shape] * fArray[adjacencyInfo.vert2Weights[5].shape];
                float f22 = fArray[adjacencyInfo.vert2Weights[6].shape] * fArray[adjacencyInfo.vert2Weights[7].shape];
                float f23 = fArray[adjacencyInfo.vert3Weights[0].shape] * fArray[adjacencyInfo.vert3Weights[1].shape];
                float f24 = fArray[adjacencyInfo.vert3Weights[2].shape] * fArray[adjacencyInfo.vert3Weights[3].shape];
                float f25 = fArray[adjacencyInfo.vert3Weights[4].shape] * fArray[adjacencyInfo.vert3Weights[5].shape];
                float f26 = fArray[adjacencyInfo.vert3Weights[6].shape] * fArray[adjacencyInfo.vert3Weights[7].shape];
                this.brightness[ambientVertexRemap.vert0] = f * f11 + var45_46 * f12 + var46_48 * f13 + var47_49 * f14;
                this.brightness[ambientVertexRemap.vert1] = f * f15 + var45_46 * f16 + var46_48 * f17 + var47_49 * f18;
                this.brightness[ambientVertexRemap.vert2] = f * f19 + var45_46 * f20 + var46_48 * f21 + var47_49 * f22;
                this.brightness[ambientVertexRemap.vert3] = f * f23 + var45_46 * f24 + var46_48 * f25 + var47_49 * f26;
                int n10 = this.blend(n8, n5, n3, n9);
                int n11 = this.blend(n7, n5, n4, n9);
                int n12 = this.blend(n7, n6, n2, n9);
                int n13 = this.blend(n8, n6, n, n9);
                this.lightmap[ambientVertexRemap.vert0] = this.blend(n10, n11, n12, n13, f11, f12, f13, f14);
                this.lightmap[ambientVertexRemap.vert1] = this.blend(n10, n11, n12, n13, f15, f16, f17, f18);
                this.lightmap[ambientVertexRemap.vert2] = this.blend(n10, n11, n12, n13, f19, f20, f21, f22);
                this.lightmap[ambientVertexRemap.vert3] = this.blend(n10, n11, n12, n13, f23, f24, f25, f26);
            }
            f = blockAndTintGetter.getShade(direction, bl);
            int n14 = 0;
            while (n14 < this.brightness.length) {
                int n15 = n14++;
                this.brightness[n15] = this.brightness[n15] * f;
            }
        }

        private int blend(int n, int n2, int n3, int n4) {
            if (n == 0) {
                n = n4;
            }
            if (n2 == 0) {
                n2 = n4;
            }
            if (n3 == 0) {
                n3 = n4;
            }
            return n + n2 + n3 + n4 >> 2 & 0xFF00FF;
        }

        private int blend(int n, int n2, int n3, int n4, float f, float f2, float f3, float f4) {
            int n5 = (int)((float)(n >> 16 & 0xFF) * f + (float)(n2 >> 16 & 0xFF) * f2 + (float)(n3 >> 16 & 0xFF) * f3 + (float)(n4 >> 16 & 0xFF) * f4) & 0xFF;
            int n6 = (int)((float)(n & 0xFF) * f + (float)(n2 & 0xFF) * f2 + (float)(n3 & 0xFF) * f3 + (float)(n4 & 0xFF) * f4) & 0xFF;
            return n5 << 16 | n6;
        }
    }

    static class Cache {
        private boolean enabled;
        private final Long2IntLinkedOpenHashMap colorCache = Util.make(() -> {
            Long2IntLinkedOpenHashMap long2IntLinkedOpenHashMap = new Long2IntLinkedOpenHashMap(100, 0.25f){

                protected void rehash(int n) {
                }
            };
            long2IntLinkedOpenHashMap.defaultReturnValue(Integer.MAX_VALUE);
            return long2IntLinkedOpenHashMap;
        });
        private final Long2FloatLinkedOpenHashMap brightnessCache = Util.make(() -> {
            Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(100, 0.25f){

                protected void rehash(int n) {
                }
            };
            long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
            return long2FloatLinkedOpenHashMap;
        });

        private Cache() {
        }

        public void enable() {
            this.enabled = true;
        }

        public void disable() {
            this.enabled = false;
            this.colorCache.clear();
            this.brightnessCache.clear();
        }

        public int getLightColor(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
            int n;
            long l = blockPos.asLong();
            if (this.enabled && (n = this.colorCache.get(l)) != Integer.MAX_VALUE) {
                return n;
            }
            n = LevelRenderer.getLightColor(blockAndTintGetter, blockState, blockPos);
            if (this.enabled) {
                if (this.colorCache.size() == 100) {
                    this.colorCache.removeFirstInt();
                }
                this.colorCache.put(l, n);
            }
            return n;
        }

        public float getShadeBrightness(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
            float f;
            long l = blockPos.asLong();
            if (this.enabled && !Float.isNaN(f = this.brightnessCache.get(l))) {
                return f;
            }
            f = blockState.getShadeBrightness(blockAndTintGetter, blockPos);
            if (this.enabled) {
                if (this.brightnessCache.size() == 100) {
                    this.brightnessCache.removeFirstFloat();
                }
                this.brightnessCache.put(l, f);
            }
            return f;
        }
    }

    protected static enum AdjacencyInfo {
        DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5f, true, new SizeInfo[]{SizeInfo.FLIP_WEST, SizeInfo.SOUTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_SOUTH, SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.WEST, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.FLIP_WEST, SizeInfo.NORTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_NORTH, SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.WEST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_EAST, SizeInfo.NORTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_NORTH, SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.EAST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_EAST, SizeInfo.SOUTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_SOUTH, SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.EAST, SizeInfo.SOUTH}),
        UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0f, true, new SizeInfo[]{SizeInfo.EAST, SizeInfo.SOUTH, SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.EAST, SizeInfo.NORTH, SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.WEST, SizeInfo.NORTH, SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.WEST, SizeInfo.SOUTH, SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST, SizeInfo.SOUTH}),
        NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.UP, SizeInfo.WEST, SizeInfo.FLIP_UP, SizeInfo.WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST}, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.UP, SizeInfo.EAST, SizeInfo.FLIP_UP, SizeInfo.EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.DOWN, SizeInfo.EAST, SizeInfo.FLIP_DOWN, SizeInfo.EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.DOWN, SizeInfo.WEST, SizeInfo.FLIP_DOWN, SizeInfo.WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST}),
        SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST, SizeInfo.FLIP_UP, SizeInfo.WEST, SizeInfo.UP, SizeInfo.WEST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST, SizeInfo.FLIP_DOWN, SizeInfo.WEST, SizeInfo.DOWN, SizeInfo.WEST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST, SizeInfo.FLIP_DOWN, SizeInfo.EAST, SizeInfo.DOWN, SizeInfo.EAST}, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST, SizeInfo.FLIP_UP, SizeInfo.EAST, SizeInfo.UP, SizeInfo.EAST}),
        WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.SOUTH, SizeInfo.UP, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.UP, SizeInfo.NORTH, SizeInfo.UP, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.NORTH, SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.SOUTH, SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.SOUTH}),
        EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new SizeInfo[]{SizeInfo.FLIP_DOWN, SizeInfo.SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.DOWN, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.FLIP_DOWN, SizeInfo.NORTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_NORTH, SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.DOWN, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_UP, SizeInfo.NORTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_NORTH, SizeInfo.UP, SizeInfo.FLIP_NORTH, SizeInfo.UP, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_UP, SizeInfo.SOUTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_SOUTH, SizeInfo.UP, SizeInfo.FLIP_SOUTH, SizeInfo.UP, SizeInfo.SOUTH});

        final Direction[] corners;
        final boolean doNonCubicWeight;
        final SizeInfo[] vert0Weights;
        final SizeInfo[] vert1Weights;
        final SizeInfo[] vert2Weights;
        final SizeInfo[] vert3Weights;
        private static final AdjacencyInfo[] BY_FACING;

        private AdjacencyInfo(Direction[] directionArray, float f, boolean bl, SizeInfo[] sizeInfoArray, SizeInfo[] sizeInfoArray2, SizeInfo[] sizeInfoArray3, SizeInfo[] sizeInfoArray4) {
            this.corners = directionArray;
            this.doNonCubicWeight = bl;
            this.vert0Weights = sizeInfoArray;
            this.vert1Weights = sizeInfoArray2;
            this.vert2Weights = sizeInfoArray3;
            this.vert3Weights = sizeInfoArray4;
        }

        public static AdjacencyInfo fromFacing(Direction direction) {
            return BY_FACING[direction.get3DDataValue()];
        }

        static {
            BY_FACING = Util.make(new AdjacencyInfo[6], adjacencyInfoArray -> {
                adjacencyInfoArray[Direction.DOWN.get3DDataValue()] = DOWN;
                adjacencyInfoArray[Direction.UP.get3DDataValue()] = UP;
                adjacencyInfoArray[Direction.NORTH.get3DDataValue()] = NORTH;
                adjacencyInfoArray[Direction.SOUTH.get3DDataValue()] = SOUTH;
                adjacencyInfoArray[Direction.WEST.get3DDataValue()] = WEST;
                adjacencyInfoArray[Direction.EAST.get3DDataValue()] = EAST;
            });
        }
    }

    protected static enum SizeInfo {
        DOWN(Direction.DOWN, false),
        UP(Direction.UP, false),
        NORTH(Direction.NORTH, false),
        SOUTH(Direction.SOUTH, false),
        WEST(Direction.WEST, false),
        EAST(Direction.EAST, false),
        FLIP_DOWN(Direction.DOWN, true),
        FLIP_UP(Direction.UP, true),
        FLIP_NORTH(Direction.NORTH, true),
        FLIP_SOUTH(Direction.SOUTH, true),
        FLIP_WEST(Direction.WEST, true),
        FLIP_EAST(Direction.EAST, true);

        final int shape;

        private SizeInfo(Direction direction, boolean bl) {
            this.shape = direction.get3DDataValue() + (bl ? DIRECTIONS.length : 0);
        }
    }

    static enum AmbientVertexRemap {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        final int vert0;
        final int vert1;
        final int vert2;
        final int vert3;
        private static final AmbientVertexRemap[] BY_FACING;

        private AmbientVertexRemap(int n2, int n3, int n4, int n5) {
            this.vert0 = n2;
            this.vert1 = n3;
            this.vert2 = n4;
            this.vert3 = n5;
        }

        public static AmbientVertexRemap fromFacing(Direction direction) {
            return BY_FACING[direction.get3DDataValue()];
        }

        static {
            BY_FACING = Util.make(new AmbientVertexRemap[6], ambientVertexRemapArray -> {
                ambientVertexRemapArray[Direction.DOWN.get3DDataValue()] = DOWN;
                ambientVertexRemapArray[Direction.UP.get3DDataValue()] = UP;
                ambientVertexRemapArray[Direction.NORTH.get3DDataValue()] = NORTH;
                ambientVertexRemapArray[Direction.SOUTH.get3DDataValue()] = SOUTH;
                ambientVertexRemapArray[Direction.WEST.get3DDataValue()] = WEST;
                ambientVertexRemapArray[Direction.EAST.get3DDataValue()] = EAST;
            });
        }
    }
}

