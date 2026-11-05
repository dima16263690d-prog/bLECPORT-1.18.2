/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlockRenderer {
    private static final float MAX_FLUID_HEIGHT = 0.8888889f;
    private final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
    private final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
    private TextureAtlasSprite waterOverlay;

    protected void setupSprites() {
        this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
        this.lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
        this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
        this.waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
        this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
    }

    private static boolean isNeighborSameFluid(FluidState fluidState, FluidState fluidState2) {
        return fluidState2.getType().isSame(fluidState.getType());
    }

    private static boolean isFaceOccludedByState(BlockGetter blockGetter, Direction direction, float f, BlockPos blockPos, BlockState blockState) {
        if (blockState.canOcclude()) {
            VoxelShape voxelShape = Shapes.box(0.0, 0.0, 0.0, 1.0, f, 1.0);
            VoxelShape voxelShape2 = blockState.getOcclusionShape(blockGetter, blockPos);
            return Shapes.blockOccudes(voxelShape, voxelShape2, direction);
        }
        return false;
    }

    private static boolean isFaceOccludedByNeighbor(BlockGetter blockGetter, BlockPos blockPos, Direction direction, float f, BlockState blockState) {
        return LiquidBlockRenderer.isFaceOccludedByState(blockGetter, direction, f, blockPos.relative(direction), blockState);
    }

    private static boolean isFaceOccludedBySelf(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction) {
        return LiquidBlockRenderer.isFaceOccludedByState(blockGetter, direction.getOpposite(), 1.0f, blockPos, blockState);
    }

    public static boolean shouldRenderFace(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, FluidState fluidState, BlockState blockState, Direction direction, FluidState fluidState2) {
        return !LiquidBlockRenderer.isFaceOccludedBySelf(blockAndTintGetter, blockPos, blockState, direction) && !LiquidBlockRenderer.isNeighborSameFluid(fluidState, fluidState2);
    }

    public boolean tesselate(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        float f;
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        float f7;
        float f8;
        float f9;
        float f10;
        float f11;
        float f12;
        float f13;
        float f14;
        boolean bl = fluidState.is(FluidTags.LAVA);
        TextureAtlasSprite[] textureAtlasSpriteArray = bl ? this.lavaIcons : this.waterIcons;
        int n = bl ? 0xFFFFFF : BiomeColors.getAverageWaterColor(blockAndTintGetter, blockPos);
        float f15 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f16 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f17 = (float)(n & 0xFF) / 255.0f;
        BlockState blockState2 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.DOWN));
        FluidState fluidState2 = blockState2.getFluidState();
        BlockState blockState3 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.UP));
        FluidState fluidState3 = blockState3.getFluidState();
        BlockState blockState4 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.NORTH));
        FluidState fluidState4 = blockState4.getFluidState();
        BlockState blockState5 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.SOUTH));
        FluidState fluidState5 = blockState5.getFluidState();
        BlockState blockState6 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.WEST));
        FluidState fluidState6 = blockState6.getFluidState();
        BlockState blockState7 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.EAST));
        FluidState fluidState7 = blockState7.getFluidState();
        boolean bl2 = !LiquidBlockRenderer.isNeighborSameFluid(fluidState, fluidState3);
        boolean bl3 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.DOWN, fluidState2) && !LiquidBlockRenderer.isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, Direction.DOWN, 0.8888889f, blockState2);
        boolean bl4 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.NORTH, fluidState4);
        boolean bl5 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.SOUTH, fluidState5);
        boolean bl6 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.WEST, fluidState6);
        boolean bl7 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.EAST, fluidState7);
        if (!(bl2 || bl3 || bl7 || bl6 || bl4 || bl5)) {
            return false;
        }
        boolean bl8 = false;
        float f18 = blockAndTintGetter.getShade(Direction.DOWN, true);
        float f19 = blockAndTintGetter.getShade(Direction.UP, true);
        float f20 = blockAndTintGetter.getShade(Direction.NORTH, true);
        float f21 = blockAndTintGetter.getShade(Direction.WEST, true);
        Fluid fluid = fluidState.getType();
        float f22 = this.getHeight(blockAndTintGetter, fluid, blockPos, blockState, fluidState);
        if (f22 >= 1.0f) {
            f14 = 1.0f;
            f13 = 1.0f;
            f12 = 1.0f;
            f11 = 1.0f;
        } else {
            float f23 = this.getHeight(blockAndTintGetter, fluid, blockPos.north(), blockState4, fluidState4);
            float f24 = this.getHeight(blockAndTintGetter, fluid, blockPos.south(), blockState5, fluidState5);
            float f25 = this.getHeight(blockAndTintGetter, fluid, blockPos.east(), blockState7, fluidState7);
            float f26 = this.getHeight(blockAndTintGetter, fluid, blockPos.west(), blockState6, fluidState6);
            f14 = this.calculateAverageHeight(blockAndTintGetter, fluid, f22, f23, f25, blockPos.relative(Direction.NORTH).relative(Direction.EAST));
            f13 = this.calculateAverageHeight(blockAndTintGetter, fluid, f22, f23, f26, blockPos.relative(Direction.NORTH).relative(Direction.WEST));
            f12 = this.calculateAverageHeight(blockAndTintGetter, fluid, f22, f24, f25, blockPos.relative(Direction.SOUTH).relative(Direction.EAST));
            f11 = this.calculateAverageHeight(blockAndTintGetter, fluid, f22, f24, f26, blockPos.relative(Direction.SOUTH).relative(Direction.WEST));
        }
        double d = blockPos.getX() & 0xF;
        double d2 = blockPos.getY() & 0xF;
        double d3 = blockPos.getZ() & 0xF;
        float f27 = 0.001f;
        float f28 = f10 = bl3 ? 0.001f : 0.0f;
        if (bl2 && !LiquidBlockRenderer.isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, Direction.UP, Math.min(Math.min(f13, f11), Math.min(f12, f14)), blockState3)) {
            float f29;
            float f30;
            float f31;
            float f32;
            float f33;
            bl8 = true;
            f13 -= 0.001f;
            f11 -= 0.001f;
            f12 -= 0.001f;
            f14 -= 0.001f;
            Vec3 vec3 = fluidState.getFlow(blockAndTintGetter, blockPos);
            if (vec3.x == 0.0 && vec3.z == 0.0) {
                var58_51 = textureAtlasSpriteArray[0];
                f9 = var58_51.getU(0.0);
                f33 = var58_51.getV(0.0);
                f8 = f9;
                f7 = var58_51.getV(16.0);
                f6 = var58_51.getU(16.0);
                f5 = f7;
                f4 = f6;
                f3 = f33;
            } else {
                var58_51 = textureAtlasSpriteArray[1];
                f32 = (float)Mth.atan2(vec3.z, vec3.x) - 1.5707964f;
                f31 = Mth.sin(f32) * 0.25f;
                f30 = Mth.cos(f32) * 0.25f;
                f29 = 8.0f;
                f9 = var58_51.getU(8.0f + (-f30 - f31) * 16.0f);
                f33 = var58_51.getV(8.0f + (-f30 + f31) * 16.0f);
                f8 = var58_51.getU(8.0f + (-f30 + f31) * 16.0f);
                f7 = var58_51.getV(8.0f + (f30 + f31) * 16.0f);
                f6 = var58_51.getU(8.0f + (f30 + f31) * 16.0f);
                f5 = var58_51.getV(8.0f + (f30 - f31) * 16.0f);
                f4 = var58_51.getU(8.0f + (f30 - f31) * 16.0f);
                f3 = var58_51.getV(8.0f + (-f30 - f31) * 16.0f);
            }
            float f34 = (f9 + f8 + f6 + f4) / 4.0f;
            f32 = (f33 + f7 + f5 + f3) / 4.0f;
            f31 = (float)textureAtlasSpriteArray[0].getWidth() / (textureAtlasSpriteArray[0].getU1() - textureAtlasSpriteArray[0].getU0());
            f30 = (float)textureAtlasSpriteArray[0].getHeight() / (textureAtlasSpriteArray[0].getV1() - textureAtlasSpriteArray[0].getV0());
            f29 = 4.0f / Math.max(f30, f31);
            f9 = Mth.lerp(f29, f9, f34);
            f8 = Mth.lerp(f29, f8, f34);
            f6 = Mth.lerp(f29, f6, f34);
            f4 = Mth.lerp(f29, f4, f34);
            f33 = Mth.lerp(f29, f33, f32);
            f7 = Mth.lerp(f29, f7, f32);
            f5 = Mth.lerp(f29, f5, f32);
            f3 = Mth.lerp(f29, f3, f32);
            int n2 = this.getLightColor(blockAndTintGetter, blockPos);
            float f35 = f19 * f15;
            f2 = f19 * f16;
            f = f19 * f17;
            this.vertex(vertexConsumer, d + 0.0, d2 + (double)f13, d3 + 0.0, f35, f2, f, f9, f33, n2);
            this.vertex(vertexConsumer, d + 0.0, d2 + (double)f11, d3 + 1.0, f35, f2, f, f8, f7, n2);
            this.vertex(vertexConsumer, d + 1.0, d2 + (double)f12, d3 + 1.0, f35, f2, f, f6, f5, n2);
            this.vertex(vertexConsumer, d + 1.0, d2 + (double)f14, d3 + 0.0, f35, f2, f, f4, f3, n2);
            if (fluidState.shouldRenderBackwardUpFace(blockAndTintGetter, blockPos.above())) {
                this.vertex(vertexConsumer, d + 0.0, d2 + (double)f13, d3 + 0.0, f35, f2, f, f9, f33, n2);
                this.vertex(vertexConsumer, d + 1.0, d2 + (double)f14, d3 + 0.0, f35, f2, f, f4, f3, n2);
                this.vertex(vertexConsumer, d + 1.0, d2 + (double)f12, d3 + 1.0, f35, f2, f, f6, f5, n2);
                this.vertex(vertexConsumer, d + 0.0, d2 + (double)f11, d3 + 1.0, f35, f2, f, f8, f7, n2);
            }
        }
        if (bl3) {
            f9 = textureAtlasSpriteArray[0].getU0();
            f8 = textureAtlasSpriteArray[0].getU1();
            f6 = textureAtlasSpriteArray[0].getV0();
            f4 = textureAtlasSpriteArray[0].getV1();
            int n3 = this.getLightColor(blockAndTintGetter, blockPos.below());
            f7 = f18 * f15;
            f5 = f18 * f16;
            f3 = f18 * f17;
            this.vertex(vertexConsumer, d, d2 + (double)f10, d3 + 1.0, f7, f5, f3, f9, f4, n3);
            this.vertex(vertexConsumer, d, d2 + (double)f10, d3, f7, f5, f3, f9, f6, n3);
            this.vertex(vertexConsumer, d + 1.0, d2 + (double)f10, d3, f7, f5, f3, f8, f6, n3);
            this.vertex(vertexConsumer, d + 1.0, d2 + (double)f10, d3 + 1.0, f7, f5, f3, f8, f4, n3);
            bl8 = true;
        }
        int n4 = this.getLightColor(blockAndTintGetter, blockPos);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Block block;
            double d4;
            double d5;
            double d6;
            double d7;
            float f36;
            if (!(switch (direction) {
                case Direction.NORTH -> {
                    f4 = f13;
                    f36 = f14;
                    d7 = d;
                    d6 = d + 1.0;
                    d5 = d3 + (double)0.001f;
                    d4 = d3 + (double)0.001f;
                    yield bl4;
                }
                case Direction.SOUTH -> {
                    f4 = f12;
                    f36 = f11;
                    d7 = d + 1.0;
                    d6 = d;
                    d5 = d3 + 1.0 - (double)0.001f;
                    d4 = d3 + 1.0 - (double)0.001f;
                    yield bl5;
                }
                case Direction.WEST -> {
                    f4 = f11;
                    f36 = f13;
                    d7 = d + (double)0.001f;
                    d6 = d + (double)0.001f;
                    d5 = d3 + 1.0;
                    d4 = d3;
                    yield bl6;
                }
                default -> {
                    f4 = f14;
                    f36 = f12;
                    d7 = d + 1.0 - (double)0.001f;
                    d6 = d + 1.0 - (double)0.001f;
                    d5 = d3;
                    d4 = d3 + 1.0;
                    yield bl7;
                }
            }) || LiquidBlockRenderer.isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, direction, Math.max(f4, f36), blockAndTintGetter.getBlockState(blockPos.relative(direction)))) continue;
            bl8 = true;
            BlockPos blockPos2 = blockPos.relative(direction);
            TextureAtlasSprite textureAtlasSprite = textureAtlasSpriteArray[1];
            if (!bl && ((block = blockAndTintGetter.getBlockState(blockPos2).getBlock()) instanceof HalfTransparentBlock || block instanceof LeavesBlock)) {
                textureAtlasSprite = this.waterOverlay;
            }
            f2 = textureAtlasSprite.getU(0.0);
            f = textureAtlasSprite.getU(8.0);
            float f37 = textureAtlasSprite.getV((1.0f - f4) * 16.0f * 0.5f);
            float f38 = textureAtlasSprite.getV((1.0f - f36) * 16.0f * 0.5f);
            float f39 = textureAtlasSprite.getV(8.0);
            float f40 = direction.getAxis() == Direction.Axis.Z ? f20 : f21;
            float f41 = f19 * f40 * f15;
            float f42 = f19 * f40 * f16;
            float f43 = f19 * f40 * f17;
            this.vertex(vertexConsumer, d7, d2 + (double)f4, d5, f41, f42, f43, f2, f37, n4);
            this.vertex(vertexConsumer, d6, d2 + (double)f36, d4, f41, f42, f43, f, f38, n4);
            this.vertex(vertexConsumer, d6, d2 + (double)f10, d4, f41, f42, f43, f, f39, n4);
            this.vertex(vertexConsumer, d7, d2 + (double)f10, d5, f41, f42, f43, f2, f39, n4);
            if (textureAtlasSprite == this.waterOverlay) continue;
            this.vertex(vertexConsumer, d7, d2 + (double)f10, d5, f41, f42, f43, f2, f39, n4);
            this.vertex(vertexConsumer, d6, d2 + (double)f10, d4, f41, f42, f43, f, f39, n4);
            this.vertex(vertexConsumer, d6, d2 + (double)f36, d4, f41, f42, f43, f, f38, n4);
            this.vertex(vertexConsumer, d7, d2 + (double)f4, d5, f41, f42, f43, f2, f37, n4);
        }
        return bl8;
    }

    private float calculateAverageHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, float f, float f2, float f3, BlockPos blockPos) {
        if (f3 >= 1.0f || f2 >= 1.0f) {
            return 1.0f;
        }
        float[] fArray = new float[2];
        if (f3 > 0.0f || f2 > 0.0f) {
            float f4 = this.getHeight(blockAndTintGetter, fluid, blockPos);
            if (f4 >= 1.0f) {
                return 1.0f;
            }
            this.addWeightedHeight(fArray, f4);
        }
        this.addWeightedHeight(fArray, f);
        this.addWeightedHeight(fArray, f3);
        this.addWeightedHeight(fArray, f2);
        return fArray[0] / fArray[1];
    }

    private void addWeightedHeight(float[] fArray, float f) {
        if (f >= 0.8f) {
            fArray[0] = fArray[0] + f * 10.0f;
            fArray[1] = fArray[1] + 10.0f;
        } else if (f >= 0.0f) {
            fArray[0] = fArray[0] + f;
            fArray[1] = fArray[1] + 1.0f;
        }
    }

    private float getHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, BlockPos blockPos) {
        BlockState blockState = blockAndTintGetter.getBlockState(blockPos);
        return this.getHeight(blockAndTintGetter, fluid, blockPos, blockState, blockState.getFluidState());
    }

    private float getHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (fluid.isSame(fluidState.getType())) {
            BlockState blockState2 = blockAndTintGetter.getBlockState(blockPos.above());
            if (fluid.isSame(blockState2.getFluidState().getType())) {
                return 1.0f;
            }
            return fluidState.getOwnHeight();
        }
        if (!blockState.getMaterial().isSolid()) {
            return 0.0f;
        }
        return -1.0f;
    }

    private void vertex(VertexConsumer vertexConsumer, double d, double d2, double d3, float f, float f2, float f3, float f4, float f5, int n) {
        vertexConsumer.vertex(d, d2, d3).color(f, f2, f3, 1.0f).uv(f4, f5).uv2(n).normal(0.0f, 1.0f, 0.0f).endVertex();
    }

    private int getLightColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
        int n = LevelRenderer.getLightColor(blockAndTintGetter, blockPos);
        int n2 = LevelRenderer.getLightColor(blockAndTintGetter, blockPos.above());
        int n3 = n & 0xFF;
        int n4 = n2 & 0xFF;
        int n5 = n >> 16 & 0xFF;
        int n6 = n2 >> 16 & 0xFF;
        return (n3 > n4 ? n3 : n4) | (n5 > n6 ? n5 : n6) << 16;
    }
}

