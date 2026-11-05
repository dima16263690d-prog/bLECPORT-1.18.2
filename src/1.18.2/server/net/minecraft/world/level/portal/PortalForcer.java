/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.portal;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

public class PortalForcer {
    private static final int TICKET_RADIUS = 3;
    private static final int SEARCH_RADIUS = 128;
    private static final int CREATE_RADIUS = 16;
    private static final int FRAME_HEIGHT = 5;
    private static final int FRAME_WIDTH = 4;
    private static final int FRAME_BOX = 3;
    private static final int FRAME_HEIGHT_START = -1;
    private static final int FRAME_HEIGHT_END = 4;
    private static final int FRAME_WIDTH_START = -1;
    private static final int FRAME_WIDTH_END = 3;
    private static final int FRAME_BOX_START = -1;
    private static final int FRAME_BOX_END = 2;
    private static final int NOTHING_FOUND = -1;
    private final ServerLevel level;

    public PortalForcer(ServerLevel serverLevel) {
        this.level = serverLevel;
    }

    public Optional<BlockUtil.FoundRectangle> findPortalAround(BlockPos blockPos, boolean bl, WorldBorder worldBorder) {
        PoiManager poiManager = this.level.getPoiManager();
        int n = bl ? 16 : 128;
        poiManager.ensureLoadedAndValid(this.level, blockPos, n);
        Optional<PoiRecord> optional = poiManager.getInSquare(poiType -> poiType == PoiType.NETHER_PORTAL, blockPos, n, PoiManager.Occupancy.ANY).filter(poiRecord -> worldBorder.isWithinBounds(poiRecord.getPos())).sorted(Comparator.comparingDouble(poiRecord -> poiRecord.getPos().distSqr(blockPos)).thenComparingInt(poiRecord -> poiRecord.getPos().getY())).filter(poiRecord -> this.level.getBlockState(poiRecord.getPos()).hasProperty(BlockStateProperties.HORIZONTAL_AXIS)).findFirst();
        return optional.map(poiRecord -> {
            BlockPos blockPos2 = poiRecord.getPos();
            this.level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockPos2), 3, blockPos2);
            BlockState blockState = this.level.getBlockState(blockPos2);
            return BlockUtil.getLargestRectangleAround(blockPos2, blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, blockPos -> this.level.getBlockState((BlockPos)blockPos) == blockState);
        });
    }

    public Optional<BlockUtil.FoundRectangle> createPortal(BlockPos blockPos, Direction.Axis axis) {
        int n;
        int n2;
        int n3;
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        double d = -1.0;
        BlockPos blockPos2 = null;
        double d2 = -1.0;
        BlockPos blockPos3 = null;
        WorldBorder worldBorder = this.level.getWorldBorder();
        int n4 = Math.min(this.level.getMaxBuildHeight(), this.level.getMinBuildHeight() + this.level.getLogicalHeight()) - 1;
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (BlockPos.MutableBlockPos mutableBlockPos2 : BlockPos.spiralAround(blockPos, 16, Direction.EAST, Direction.SOUTH)) {
            int n5 = Math.min(n4, this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, mutableBlockPos2.getX(), mutableBlockPos2.getZ()));
            n3 = 1;
            if (!worldBorder.isWithinBounds(mutableBlockPos2) || !worldBorder.isWithinBounds(mutableBlockPos2.move(direction, 1))) continue;
            mutableBlockPos2.move(direction.getOpposite(), 1);
            for (n2 = n5; n2 >= this.level.getMinBuildHeight(); --n2) {
                int n6;
                mutableBlockPos2.setY(n2);
                if (!this.level.isEmptyBlock(mutableBlockPos2)) continue;
                n = n2;
                while (n2 > this.level.getMinBuildHeight() && this.level.isEmptyBlock(mutableBlockPos2.move(Direction.DOWN))) {
                    --n2;
                }
                if (n2 + 4 > n4 || (n6 = n - n2) > 0 && n6 < 3) continue;
                mutableBlockPos2.setY(n2);
                if (!this.canHostFrame(mutableBlockPos2, mutableBlockPos, direction, 0)) continue;
                double d3 = blockPos.distSqr(mutableBlockPos2);
                if (this.canHostFrame(mutableBlockPos2, mutableBlockPos, direction, -1) && this.canHostFrame(mutableBlockPos2, mutableBlockPos, direction, 1) && (d == -1.0 || d > d3)) {
                    d = d3;
                    blockPos2 = mutableBlockPos2.immutable();
                }
                if (d != -1.0 || d2 != -1.0 && !(d2 > d3)) continue;
                d2 = d3;
                blockPos3 = mutableBlockPos2.immutable();
            }
        }
        if (d == -1.0 && d2 != -1.0) {
            blockPos2 = blockPos3;
            d = d2;
        }
        if (d == -1.0) {
            int n7 = n4 - 9;
            int n8 = Math.max(this.level.getMinBuildHeight() - -1, 70);
            if (n7 < n8) {
                return Optional.empty();
            }
            blockPos2 = new BlockPos(blockPos.getX(), Mth.clamp(blockPos.getY(), n8, n7), blockPos.getZ()).immutable();
            Direction direction2 = direction.getClockWise();
            if (!worldBorder.isWithinBounds(blockPos2)) {
                return Optional.empty();
            }
            for (n3 = -1; n3 < 2; ++n3) {
                for (n2 = 0; n2 < 2; ++n2) {
                    for (n = -1; n < 3; ++n) {
                        BlockState blockState = n < 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
                        mutableBlockPos.setWithOffset(blockPos2, n2 * direction.getStepX() + n3 * direction2.getStepX(), n, n2 * direction.getStepZ() + n3 * direction2.getStepZ());
                        this.level.setBlockAndUpdate(mutableBlockPos, blockState);
                    }
                }
            }
        }
        for (int i = -1; i < 3; ++i) {
            for (int j = -1; j < 4; ++j) {
                if (i != -1 && i != 2 && j != -1 && j != 3) continue;
                mutableBlockPos.setWithOffset(blockPos2, i * direction.getStepX(), j, i * direction.getStepZ());
                this.level.setBlock(mutableBlockPos, Blocks.OBSIDIAN.defaultBlockState(), 3);
            }
        }
        BlockState blockState = (BlockState)Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 3; ++j) {
                mutableBlockPos.setWithOffset(blockPos2, i * direction.getStepX(), j, i * direction.getStepZ());
                this.level.setBlock(mutableBlockPos, blockState, 18);
            }
        }
        return Optional.of(new BlockUtil.FoundRectangle(blockPos2.immutable(), 2, 3));
    }

    private boolean canHostFrame(BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos, Direction direction, int n) {
        Direction direction2 = direction.getClockWise();
        for (int i = -1; i < 3; ++i) {
            for (int j = -1; j < 4; ++j) {
                mutableBlockPos.setWithOffset(blockPos, direction.getStepX() * i + direction2.getStepX() * n, j, direction.getStepZ() * i + direction2.getStepZ() * n);
                if (j < 0 && !this.level.getBlockState(mutableBlockPos).getMaterial().isSolid()) {
                    return false;
                }
                if (j < 0 || this.level.isEmptyBlock(mutableBlockPos)) continue;
                return false;
            }
        }
        return true;
    }
}

