/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BoneMealItem
extends Item {
    public static final int GRASS_SPREAD_WIDTH = 3;
    public static final int GRASS_SPREAD_HEIGHT = 1;
    public static final int GRASS_COUNT_MULTIPLIER = 3;

    public BoneMealItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockPos blockPos2 = blockPos.relative(useOnContext.getClickedFace());
        if (BoneMealItem.growCrop(useOnContext.getItemInHand(), level, blockPos)) {
            if (!level.isClientSide) {
                level.levelEvent(1505, blockPos, 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        BlockState blockState = level.getBlockState(blockPos);
        boolean bl = blockState.isFaceSturdy(level, blockPos, useOnContext.getClickedFace());
        if (bl && BoneMealItem.growWaterPlant(useOnContext.getItemInHand(), level, blockPos2, useOnContext.getClickedFace())) {
            if (!level.isClientSide) {
                level.levelEvent(1505, blockPos2, 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static boolean growCrop(ItemStack itemStack, Level level, BlockPos blockPos) {
        BonemealableBlock bonemealableBlock;
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.getBlock() instanceof BonemealableBlock && (bonemealableBlock = (BonemealableBlock)((Object)blockState.getBlock())).isValidBonemealTarget(level, blockPos, blockState, level.isClientSide)) {
            if (level instanceof ServerLevel) {
                if (bonemealableBlock.isBonemealSuccess(level, level.random, blockPos, blockState)) {
                    bonemealableBlock.performBonemeal((ServerLevel)level, level.random, blockPos, blockState);
                }
                itemStack.shrink(1);
            }
            return true;
        }
        return false;
    }

    public static boolean growWaterPlant(ItemStack itemStack, Level level, BlockPos blockPos, @Nullable Direction direction) {
        if (!level.getBlockState(blockPos).is(Blocks.WATER) || level.getFluidState(blockPos).getAmount() != 8) {
            return false;
        }
        if (!(level instanceof ServerLevel)) {
            return true;
        }
        Random random = level.getRandom();
        block0: for (int i = 0; i < 128; ++i) {
            BlockPos blockPos2 = blockPos;
            BlockState blockState = Blocks.SEAGRASS.defaultBlockState();
            for (int j = 0; j < i / 16; ++j) {
                if (level.getBlockState(blockPos2 = blockPos2.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1)).isCollisionShapeFullBlock(level, blockPos2)) continue block0;
            }
            Holder<Biome> holder2 = level.getBiome(blockPos2);
            if (holder2.is(Biomes.WARM_OCEAN)) {
                if (i == 0 && direction != null && direction.getAxis().isHorizontal()) {
                    blockState = Registry.BLOCK.getTag(BlockTags.WALL_CORALS).flatMap(named -> named.getRandomElement(level.random)).map(holder -> ((Block)holder.value()).defaultBlockState()).orElse(blockState);
                    if (blockState.hasProperty(BaseCoralWallFanBlock.FACING)) {
                        blockState = (BlockState)blockState.setValue(BaseCoralWallFanBlock.FACING, direction);
                    }
                } else if (random.nextInt(4) == 0) {
                    blockState = Registry.BLOCK.getTag(BlockTags.UNDERWATER_BONEMEALS).flatMap(named -> named.getRandomElement(level.random)).map(holder -> ((Block)holder.value()).defaultBlockState()).orElse(blockState);
                }
            }
            if (blockState.is(BlockTags.WALL_CORALS, blockStateBase -> blockStateBase.hasProperty(BaseCoralWallFanBlock.FACING))) {
                for (int j = 0; !blockState.canSurvive(level, blockPos2) && j < 4; ++j) {
                    blockState = (BlockState)blockState.setValue(BaseCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
                }
            }
            if (!blockState.canSurvive(level, blockPos2)) continue;
            BlockState blockState2 = level.getBlockState(blockPos2);
            if (blockState2.is(Blocks.WATER) && level.getFluidState(blockPos2).getAmount() == 8) {
                level.setBlock(blockPos2, blockState, 3);
                continue;
            }
            if (!blockState2.is(Blocks.SEAGRASS) || random.nextInt(10) != 0) continue;
            ((BonemealableBlock)((Object)Blocks.SEAGRASS)).performBonemeal((ServerLevel)level, random, blockPos2, blockState2);
        }
        itemStack.shrink(1);
        return true;
    }

    public static void addGrowthParticles(LevelAccessor levelAccessor, BlockPos blockPos, int n) {
        double d;
        BlockState blockState;
        if (n == 0) {
            n = 15;
        }
        if ((blockState = levelAccessor.getBlockState(blockPos)).isAir()) {
            return;
        }
        double d2 = 0.5;
        if (blockState.is(Blocks.WATER)) {
            n *= 3;
            d = 1.0;
            d2 = 3.0;
        } else if (blockState.isSolidRender(levelAccessor, blockPos)) {
            blockPos = blockPos.above();
            n *= 3;
            d2 = 3.0;
            d = 1.0;
        } else {
            d = blockState.getShape(levelAccessor, blockPos).max(Direction.Axis.Y);
        }
        levelAccessor.addParticle(ParticleTypes.HAPPY_VILLAGER, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
        Random random = levelAccessor.getRandom();
        for (int i = 0; i < n; ++i) {
            double d3;
            double d4;
            double d5 = random.nextGaussian() * 0.02;
            double d6 = random.nextGaussian() * 0.02;
            double d7 = random.nextGaussian() * 0.02;
            double d8 = 0.5 - d2;
            double d9 = (double)blockPos.getX() + d8 + random.nextDouble() * d2 * 2.0;
            if (levelAccessor.getBlockState(new BlockPos(d9, d4 = (double)blockPos.getY() + random.nextDouble() * d, d3 = (double)blockPos.getZ() + d8 + random.nextDouble() * d2 * 2.0).below()).isAir()) continue;
            levelAccessor.addParticle(ParticleTypes.HAPPY_VILLAGER, d9, d4, d3, d5, d6, d7);
        }
    }
}

