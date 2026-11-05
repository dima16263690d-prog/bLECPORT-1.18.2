/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class TreeFeature
extends Feature<TreeConfiguration> {
    private static final int BLOCK_UPDATE_FLAGS = 19;

    public TreeFeature(Codec<TreeConfiguration> codec) {
        super(codec);
    }

    public static boolean isFree(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return TreeFeature.validTreePos(levelSimulatedReader, blockPos) || levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(BlockTags.LOGS));
    }

    private static boolean isVine(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(Blocks.VINE));
    }

    private static boolean isBlockWater(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(Blocks.WATER));
    }

    public static boolean isAirOrLeaves(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.isAir() || blockState.is(BlockTags.LEAVES));
    }

    private static boolean isReplaceablePlant(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
            Material material = blockState.getMaterial();
            return material == Material.REPLACEABLE_PLANT;
        });
    }

    private static void setBlockKnownShape(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        levelWriter.setBlock(blockPos, blockState, 19);
    }

    public static boolean validTreePos(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return TreeFeature.isAirOrLeaves(levelSimulatedReader, blockPos) || TreeFeature.isReplaceablePlant(levelSimulatedReader, blockPos) || TreeFeature.isBlockWater(levelSimulatedReader, blockPos);
    }

    private boolean doPlace(WorldGenLevel worldGenLevel, Random random, BlockPos blockPos, BiConsumer<BlockPos, BlockState> biConsumer, BiConsumer<BlockPos, BlockState> biConsumer2, TreeConfiguration treeConfiguration) {
        int n = treeConfiguration.trunkPlacer.getTreeHeight(random);
        int n2 = treeConfiguration.foliagePlacer.foliageHeight(random, n, treeConfiguration);
        int n3 = n - n2;
        int n4 = treeConfiguration.foliagePlacer.foliageRadius(random, n3);
        if (blockPos.getY() < worldGenLevel.getMinBuildHeight() + 1 || blockPos.getY() + n + 1 > worldGenLevel.getMaxBuildHeight()) {
            return false;
        }
        OptionalInt optionalInt = treeConfiguration.minimumSize.minClippedHeight();
        int n5 = this.getMaxFreeTreeHeight(worldGenLevel, n, blockPos, treeConfiguration);
        if (!(n5 >= n || optionalInt.isPresent() && n5 >= optionalInt.getAsInt())) {
            return false;
        }
        List<FoliagePlacer.FoliageAttachment> list = treeConfiguration.trunkPlacer.placeTrunk(worldGenLevel, biConsumer, random, n5, blockPos, treeConfiguration);
        list.forEach(foliageAttachment -> treeConfiguration.foliagePlacer.createFoliage(worldGenLevel, biConsumer2, random, treeConfiguration, n5, (FoliagePlacer.FoliageAttachment)foliageAttachment, n2, n4));
        return true;
    }

    private int getMaxFreeTreeHeight(LevelSimulatedReader levelSimulatedReader, int n, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= n + 1; ++i) {
            int n2 = treeConfiguration.minimumSize.getSizeAtHeight(n, i);
            for (int j = -n2; j <= n2; ++j) {
                for (int k = -n2; k <= n2; ++k) {
                    mutableBlockPos.setWithOffset(blockPos, j, i, k);
                    if (TreeFeature.isFree(levelSimulatedReader, mutableBlockPos) && (treeConfiguration.ignoreVines || !TreeFeature.isVine(levelSimulatedReader, mutableBlockPos))) continue;
                    return i - 2;
                }
            }
        }
        return n;
    }

    @Override
    protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        TreeFeature.setBlockKnownShape(levelWriter, blockPos, blockState);
    }

    @Override
    public final boolean place(FeaturePlaceContext<TreeConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        Random random = featurePlaceContext.random();
        BlockPos blockPos2 = featurePlaceContext.origin();
        TreeConfiguration treeConfiguration = featurePlaceContext.config();
        HashSet hashSet = Sets.newHashSet();
        HashSet hashSet2 = Sets.newHashSet();
        HashSet hashSet3 = Sets.newHashSet();
        BiConsumer<BlockPos, BlockState> biConsumer = (blockPos, blockState) -> {
            hashSet.add(blockPos.immutable());
            worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
        };
        BiConsumer<BlockPos, BlockState> biConsumer2 = (blockPos, blockState) -> {
            hashSet2.add(blockPos.immutable());
            worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
        };
        BiConsumer<BlockPos, BlockState> biConsumer3 = (blockPos, blockState) -> {
            hashSet3.add(blockPos.immutable());
            worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
        };
        boolean bl = this.doPlace(worldGenLevel, random, blockPos2, biConsumer, biConsumer2, treeConfiguration);
        if (!bl || hashSet.isEmpty() && hashSet2.isEmpty()) {
            return false;
        }
        if (!treeConfiguration.decorators.isEmpty()) {
            ArrayList arrayList = Lists.newArrayList((Iterable)hashSet);
            ArrayList arrayList2 = Lists.newArrayList((Iterable)hashSet2);
            arrayList.sort(Comparator.comparingInt(Vec3i::getY));
            arrayList2.sort(Comparator.comparingInt(Vec3i::getY));
            treeConfiguration.decorators.forEach(treeDecorator -> treeDecorator.place(worldGenLevel, biConsumer3, random, arrayList, arrayList2));
        }
        return BoundingBox.encapsulatingPositions(Iterables.concat((Iterable)hashSet, (Iterable)hashSet2, (Iterable)hashSet3)).map(boundingBox -> {
            DiscreteVoxelShape discreteVoxelShape = TreeFeature.updateLeaves(worldGenLevel, boundingBox, hashSet, hashSet3);
            StructureTemplate.updateShapeAtEdge(worldGenLevel, 3, discreteVoxelShape, boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
            return true;
        }).orElse(false);
    }

    private static DiscreteVoxelShape updateLeaves(LevelAccessor levelAccessor, BoundingBox boundingBox, Set<BlockPos> set, Set<BlockPos> set2) {
        ArrayList arrayList = Lists.newArrayList();
        BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = new BitSetDiscreteVoxelShape(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());
        int n = 6;
        for (int i = 0; i < 6; ++i) {
            arrayList.add(Sets.newHashSet());
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Object object : Lists.newArrayList(set2)) {
            if (!boundingBox.isInside((Vec3i)object)) continue;
            ((DiscreteVoxelShape)bitSetDiscreteVoxelShape).fill(((Vec3i)object).getX() - boundingBox.minX(), ((Vec3i)object).getY() - boundingBox.minY(), ((Vec3i)object).getZ() - boundingBox.minZ());
        }
        for (Object object : Lists.newArrayList(set)) {
            if (boundingBox.isInside((Vec3i)object)) {
                ((DiscreteVoxelShape)bitSetDiscreteVoxelShape).fill(((Vec3i)object).getX() - boundingBox.minX(), ((Vec3i)object).getY() - boundingBox.minY(), ((Vec3i)object).getZ() - boundingBox.minZ());
            }
            for (Direction directionArray : Direction.values()) {
                BlockState blockState;
                mutableBlockPos.setWithOffset((Vec3i)object, directionArray);
                if (set.contains(mutableBlockPos) || !(blockState = levelAccessor.getBlockState(mutableBlockPos)).hasProperty(BlockStateProperties.DISTANCE)) continue;
                ((Set)arrayList.get(0)).add(mutableBlockPos.immutable());
                TreeFeature.setBlockKnownShape(levelAccessor, mutableBlockPos, (BlockState)blockState.setValue(BlockStateProperties.DISTANCE, 1));
                if (!boundingBox.isInside(mutableBlockPos)) continue;
                ((DiscreteVoxelShape)bitSetDiscreteVoxelShape).fill(mutableBlockPos.getX() - boundingBox.minX(), mutableBlockPos.getY() - boundingBox.minY(), mutableBlockPos.getZ() - boundingBox.minZ());
            }
        }
        for (int i = 1; i < 6; ++i) {
            Object object;
            object = (Set)arrayList.get(i - 1);
            Set set3 = (Set)arrayList.get(i);
            Iterator iterator = object.iterator();
            while (iterator.hasNext()) {
                BlockPos blockPos = (BlockPos)iterator.next();
                if (boundingBox.isInside(blockPos)) {
                    ((DiscreteVoxelShape)bitSetDiscreteVoxelShape).fill(blockPos.getX() - boundingBox.minX(), blockPos.getY() - boundingBox.minY(), blockPos.getZ() - boundingBox.minZ());
                }
                for (Direction direction : Direction.values()) {
                    int n2;
                    BlockState blockState;
                    mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
                    if (object.contains(mutableBlockPos) || set3.contains(mutableBlockPos) || !(blockState = levelAccessor.getBlockState(mutableBlockPos)).hasProperty(BlockStateProperties.DISTANCE) || (n2 = blockState.getValue(BlockStateProperties.DISTANCE).intValue()) <= i + 1) continue;
                    BlockState blockState2 = (BlockState)blockState.setValue(BlockStateProperties.DISTANCE, i + 1);
                    TreeFeature.setBlockKnownShape(levelAccessor, mutableBlockPos, blockState2);
                    if (boundingBox.isInside(mutableBlockPos)) {
                        ((DiscreteVoxelShape)bitSetDiscreteVoxelShape).fill(mutableBlockPos.getX() - boundingBox.minX(), mutableBlockPos.getY() - boundingBox.minY(), mutableBlockPos.getZ() - boundingBox.minZ());
                    }
                    set3.add(mutableBlockPos.immutable());
                }
            }
        }
        return bitSetDiscreteVoxelShape;
    }
}

