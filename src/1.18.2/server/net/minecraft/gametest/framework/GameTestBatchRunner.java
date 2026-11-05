/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class GameTestBatchRunner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BlockPos firstTestNorthWestCorner;
    final ServerLevel level;
    private final GameTestTicker testTicker;
    private final int testsPerRow;
    private final List<GameTestInfo> allTestInfos;
    private final List<Pair<GameTestBatch, Collection<GameTestInfo>>> batches;
    private final BlockPos.MutableBlockPos nextTestNorthWestCorner;

    public GameTestBatchRunner(Collection<GameTestBatch> collection, BlockPos blockPos, Rotation rotation, ServerLevel serverLevel, GameTestTicker gameTestTicker, int n) {
        this.nextTestNorthWestCorner = blockPos.mutable();
        this.firstTestNorthWestCorner = blockPos;
        this.level = serverLevel;
        this.testTicker = gameTestTicker;
        this.testsPerRow = n;
        this.batches = (List)collection.stream().map(gameTestBatch -> {
            Collection collection = (Collection)gameTestBatch.getTestFunctions().stream().map(testFunction -> new GameTestInfo((TestFunction)testFunction, rotation, serverLevel)).collect(ImmutableList.toImmutableList());
            return Pair.of((Object)gameTestBatch, (Object)collection);
        }).collect(ImmutableList.toImmutableList());
        this.allTestInfos = (List)this.batches.stream().flatMap(pair -> ((Collection)pair.getSecond()).stream()).collect(ImmutableList.toImmutableList());
    }

    public List<GameTestInfo> getTestInfos() {
        return this.allTestInfos;
    }

    public void start() {
        this.runBatch(0);
    }

    void runBatch(final int n) {
        if (n >= this.batches.size()) {
            return;
        }
        Pair<GameTestBatch, Collection<GameTestInfo>> pair = this.batches.get(n);
        final GameTestBatch gameTestBatch = (GameTestBatch)pair.getFirst();
        Collection collection = (Collection)pair.getSecond();
        Map<GameTestInfo, BlockPos> map = this.createStructuresForBatch(collection);
        String string = gameTestBatch.getName();
        LOGGER.info("Running test batch '{}' ({} tests)...", (Object)string, (Object)collection.size());
        gameTestBatch.runBeforeBatchFunction(this.level);
        final MultipleTestTracker multipleTestTracker = new MultipleTestTracker();
        collection.forEach(multipleTestTracker::addTestToTrack);
        multipleTestTracker.addListener(new GameTestListener(){

            private void testCompleted() {
                if (multipleTestTracker.isDone()) {
                    gameTestBatch.runAfterBatchFunction(GameTestBatchRunner.this.level);
                    GameTestBatchRunner.this.runBatch(n + 1);
                }
            }

            @Override
            public void testStructureLoaded(GameTestInfo gameTestInfo) {
            }

            @Override
            public void testPassed(GameTestInfo gameTestInfo) {
                this.testCompleted();
            }

            @Override
            public void testFailed(GameTestInfo gameTestInfo) {
                this.testCompleted();
            }
        });
        collection.forEach(gameTestInfo -> {
            BlockPos blockPos = (BlockPos)map.get(gameTestInfo);
            GameTestRunner.runTest(gameTestInfo, blockPos, this.testTicker);
        });
    }

    private Map<GameTestInfo, BlockPos> createStructuresForBatch(Collection<GameTestInfo> collection) {
        HashMap hashMap = Maps.newHashMap();
        int n = 0;
        AABB aABB = new AABB(this.nextTestNorthWestCorner);
        for (GameTestInfo gameTestInfo : collection) {
            BlockPos blockPos = new BlockPos(this.nextTestNorthWestCorner);
            StructureBlockEntity structureBlockEntity = StructureUtils.spawnStructure(gameTestInfo.getStructureName(), blockPos, gameTestInfo.getRotation(), 2, this.level, true);
            AABB aABB2 = StructureUtils.getStructureBounds(structureBlockEntity);
            gameTestInfo.setStructureBlockPos(structureBlockEntity.getBlockPos());
            hashMap.put(gameTestInfo, new BlockPos(this.nextTestNorthWestCorner));
            aABB = aABB.minmax(aABB2);
            this.nextTestNorthWestCorner.move((int)aABB2.getXsize() + 5, 0, 0);
            if (n++ % this.testsPerRow != this.testsPerRow - 1) continue;
            this.nextTestNorthWestCorner.move(0, 0, (int)aABB.getZsize() + 6);
            this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
            aABB = new AABB(this.nextTestNorthWestCorner);
        }
        return hashMap;
    }
}

