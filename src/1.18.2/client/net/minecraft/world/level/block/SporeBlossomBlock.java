/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SporeBlossomBlock
extends Block {
    private static final VoxelShape SHAPE = Block.box(2.0, 13.0, 2.0, 14.0, 16.0, 14.0);
    private static final int ADD_PARTICLE_ATTEMPTS = 14;
    private static final int PARTICLE_XZ_RADIUS = 10;
    private static final int PARTICLE_Y_MAX = 10;

    public SporeBlossomBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return Block.canSupportCenter(levelReader, blockPos.above(), Direction.DOWN) && !levelReader.isWaterAt(blockPos);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.UP && !this.canSurvive(blockState, levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        double d = (double)n + random.nextDouble();
        double d2 = (double)n2 + 0.7;
        double d3 = (double)n3 + random.nextDouble();
        level.addParticle(ParticleTypes.FALLING_SPORE_BLOSSOM, d, d2, d3, 0.0, 0.0, 0.0);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 14; ++i) {
            mutableBlockPos.set(n + Mth.nextInt(random, -10, 10), n2 - random.nextInt(10), n3 + Mth.nextInt(random, -10, 10));
            BlockState blockState2 = level.getBlockState(mutableBlockPos);
            if (blockState2.isCollisionShapeFullBlock(level, mutableBlockPos)) continue;
            level.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, (double)mutableBlockPos.getX() + random.nextDouble(), (double)mutableBlockPos.getY() + random.nextDouble(), (double)mutableBlockPos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }
}

