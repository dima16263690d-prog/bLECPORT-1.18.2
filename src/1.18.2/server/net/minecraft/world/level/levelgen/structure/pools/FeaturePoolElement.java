/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class FeaturePoolElement
extends StructurePoolElement {
    public static final Codec<FeaturePoolElement> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)PlacedFeature.CODEC.fieldOf("feature").forGetter(featurePoolElement -> featurePoolElement.feature), FeaturePoolElement.projectionCodec()).apply((Applicative)instance, FeaturePoolElement::new));
    private final Holder<PlacedFeature> feature;
    private final CompoundTag defaultJigsawNBT;

    protected FeaturePoolElement(Holder<PlacedFeature> holder, StructureTemplatePool.Projection projection) {
        super(projection);
        this.feature = holder;
        this.defaultJigsawNBT = this.fillDefaultJigsawNBT();
    }

    private CompoundTag fillDefaultJigsawNBT() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("name", "minecraft:bottom");
        compoundTag.putString("final_state", "minecraft:air");
        compoundTag.putString("pool", "minecraft:empty");
        compoundTag.putString("target", "minecraft:empty");
        compoundTag.putString("joint", JigsawBlockEntity.JointType.ROLLABLE.getSerializedName());
        return compoundTag;
    }

    @Override
    public Vec3i getSize(StructureManager structureManager, Rotation rotation) {
        return Vec3i.ZERO;
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager structureManager, BlockPos blockPos, Rotation rotation, Random random) {
        ArrayList arrayList = Lists.newArrayList();
        arrayList.add(new StructureTemplate.StructureBlockInfo(blockPos, (BlockState)Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, FrontAndTop.fromFrontAndTop(Direction.DOWN, Direction.SOUTH)), this.defaultJigsawNBT));
        return arrayList;
    }

    @Override
    public BoundingBox getBoundingBox(StructureManager structureManager, BlockPos blockPos, Rotation rotation) {
        Vec3i vec3i = this.getSize(structureManager, rotation);
        return new BoundingBox(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + vec3i.getX(), blockPos.getY() + vec3i.getY(), blockPos.getZ() + vec3i.getZ());
    }

    @Override
    public boolean place(StructureManager structureManager, WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos2, Rotation rotation, BoundingBox boundingBox, Random random, boolean bl) {
        return this.feature.value().place(worldGenLevel, chunkGenerator, random, blockPos);
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.FEATURE;
    }

    public String toString() {
        return "Feature[" + this.feature + "]";
    }
}

