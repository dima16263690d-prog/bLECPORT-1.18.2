/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.EnumMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.Target;

public class SwimNodeEvaluator
extends NodeEvaluator {
    private final boolean allowBreaching;
    private final Long2ObjectMap<BlockPathTypes> pathTypesByPosCache = new Long2ObjectOpenHashMap();

    public SwimNodeEvaluator(boolean bl) {
        this.allowBreaching = bl;
    }

    @Override
    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        super.prepare(pathNavigationRegion, mob);
        this.pathTypesByPosCache.clear();
    }

    @Override
    public void done() {
        super.done();
        this.pathTypesByPosCache.clear();
    }

    @Override
    public Node getStart() {
        return super.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ));
    }

    @Override
    public Target getGoal(double d, double d2, double d3) {
        return new Target(super.getNode(Mth.floor(d), Mth.floor(d2), Mth.floor(d3)));
    }

    @Override
    public int getNeighbors(Node[] nodeArray, Node node) {
        int n = 0;
        EnumMap enumMap = Maps.newEnumMap(Direction.class);
        for (Direction object : Direction.values()) {
            Node node2 = this.getNode(node.x + object.getStepX(), node.y + object.getStepY(), node.z + object.getStepZ());
            enumMap.put(object, node2);
            if (!this.isNodeValid(node2)) continue;
            nodeArray[n++] = node2;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Direction direction2 = direction.getClockWise();
            Node node3 = this.getNode(node.x + direction.getStepX() + direction2.getStepX(), node.y, node.z + direction.getStepZ() + direction2.getStepZ());
            if (!this.isDiagonalNodeValid(node3, (Node)enumMap.get(direction), (Node)enumMap.get(direction2))) continue;
            nodeArray[n++] = node3;
        }
        return n;
    }

    protected boolean isNodeValid(@Nullable Node node) {
        return node != null && !node.closed;
    }

    protected boolean isDiagonalNodeValid(@Nullable Node node, @Nullable Node node2, @Nullable Node node3) {
        return this.isNodeValid(node) && node2 != null && node2.costMalus >= 0.0f && node3 != null && node3.costMalus >= 0.0f;
    }

    @Override
    @Nullable
    protected Node getNode(int n, int n2, int n3) {
        float f;
        Node node = null;
        BlockPathTypes blockPathTypes = this.getCachedBlockType(n, n2, n3);
        if ((this.allowBreaching && blockPathTypes == BlockPathTypes.BREACH || blockPathTypes == BlockPathTypes.WATER) && (f = this.mob.getPathfindingMalus(blockPathTypes)) >= 0.0f) {
            node = super.getNode(n, n2, n3);
            node.type = blockPathTypes;
            node.costMalus = Math.max(node.costMalus, f);
            if (this.level.getFluidState(new BlockPos(n, n2, n3)).isEmpty()) {
                node.costMalus += 8.0f;
            }
        }
        return node;
    }

    protected BlockPathTypes getCachedBlockType(int n, int n2, int n3) {
        return (BlockPathTypes)((Object)this.pathTypesByPosCache.computeIfAbsent(BlockPos.asLong(n, n2, n3), l -> this.getBlockPathType(this.level, n, n2, n3)));
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int n, int n2, int n3) {
        return this.getBlockPathType(blockGetter, n, n2, n3, this.mob, this.entityWidth, this.entityHeight, this.entityDepth, this.canOpenDoors(), this.canPassDoors());
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int n, int n2, int n3, Mob mob, int n4, int n5, int n6, boolean bl, boolean bl2) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = n; i < n + n4; ++i) {
            for (int j = n2; j < n2 + n5; ++j) {
                for (int k = n3; k < n3 + n6; ++k) {
                    FluidState fluidState = blockGetter.getFluidState(mutableBlockPos.set(i, j, k));
                    BlockState blockState = blockGetter.getBlockState(mutableBlockPos.set(i, j, k));
                    if (fluidState.isEmpty() && blockState.isPathfindable(blockGetter, (BlockPos)mutableBlockPos.below(), PathComputationType.WATER) && blockState.isAir()) {
                        return BlockPathTypes.BREACH;
                    }
                    if (fluidState.is(FluidTags.WATER)) continue;
                    return BlockPathTypes.BLOCKED;
                }
            }
        }
        BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
        if (blockState.isPathfindable(blockGetter, mutableBlockPos, PathComputationType.WATER)) {
            return BlockPathTypes.WATER;
        }
        return BlockPathTypes.BLOCKED;
    }
}

