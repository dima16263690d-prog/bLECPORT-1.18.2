/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface ChangeOverTimeBlock<T extends Enum<T>> {
    public static final int SCAN_DISTANCE = 4;

    public Optional<BlockState> getNext(BlockState var1);

    public float getChanceModifier();

    default public void onRandomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        float f = 0.05688889f;
        if (random.nextFloat() < 0.05688889f) {
            this.applyChangeOverTime(blockState, serverLevel, blockPos, random);
        }
    }

    public T getAge();

    default public void applyChangeOverTime(BlockState blockState2, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        BlockPos blockPos2;
        int n;
        int n2 = ((Enum)this.getAge()).ordinal();
        int n3 = 0;
        int n4 = 0;
        Iterator<BlockPos> iterator = BlockPos.withinManhattan(blockPos, 4, 4, 4).iterator();
        while (iterator.hasNext() && (n = (blockPos2 = iterator.next()).distManhattan(blockPos)) <= 4) {
            BlockState blockState3;
            Block block;
            if (blockPos2.equals(blockPos) || !((block = (blockState3 = serverLevel.getBlockState(blockPos2)).getBlock()) instanceof ChangeOverTimeBlock)) continue;
            T t = ((ChangeOverTimeBlock)((Object)block)).getAge();
            if (this.getAge().getClass() != t.getClass()) continue;
            int n5 = ((Enum)t).ordinal();
            if (n5 < n2) {
                return;
            }
            if (n5 > n2) {
                ++n4;
                continue;
            }
            ++n3;
        }
        float f = (float)(n4 + 1) / (float)(n4 + n3 + 1);
        float f2 = f * f * this.getChanceModifier();
        if (random.nextFloat() < f2) {
            this.getNext(blockState2).ifPresent(blockState -> serverLevel.setBlockAndUpdate(blockPos, (BlockState)blockState));
        }
    }
}

