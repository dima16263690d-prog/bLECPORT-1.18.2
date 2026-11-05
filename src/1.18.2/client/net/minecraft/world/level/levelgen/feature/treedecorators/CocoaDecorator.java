/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class CocoaDecorator
extends TreeDecorator {
    public static final Codec<CocoaDecorator> CODEC = Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").xmap(CocoaDecorator::new, cocoaDecorator -> Float.valueOf(cocoaDecorator.probability)).codec();
    private final float probability;

    public CocoaDecorator(float f) {
        this.probability = f;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.COCOA;
    }

    @Override
    public void place(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, List<BlockPos> list, List<BlockPos> list2) {
        if (random.nextFloat() >= this.probability) {
            return;
        }
        int n = list.get(0).getY();
        list.stream().filter(blockPos -> blockPos.getY() - n <= 2).forEach(blockPos -> {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                Direction direction2;
                BlockPos blockPos2;
                if (!(random.nextFloat() <= 0.25f) || !Feature.isAir(levelSimulatedReader, blockPos2 = blockPos.offset((direction2 = direction.getOpposite()).getStepX(), 0, direction2.getStepZ()))) continue;
                biConsumer.accept(blockPos2, (BlockState)((BlockState)Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.AGE, random.nextInt(3))).setValue(CocoaBlock.FACING, direction));
            }
        });
    }
}

