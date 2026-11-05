/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.slf4j.Logger;

public class PoolElementStructurePiece
extends StructurePiece {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final StructurePoolElement element;
    protected BlockPos position;
    private final int groundLevelDelta;
    protected final Rotation rotation;
    private final List<JigsawJunction> junctions = Lists.newArrayList();
    private final StructureManager structureManager;

    public PoolElementStructurePiece(StructureManager structureManager, StructurePoolElement structurePoolElement, BlockPos blockPos, int n, Rotation rotation, BoundingBox boundingBox) {
        super(StructurePieceType.JIGSAW, 0, boundingBox);
        this.structureManager = structureManager;
        this.element = structurePoolElement;
        this.position = blockPos;
        this.groundLevelDelta = n;
        this.rotation = rotation;
    }

    public PoolElementStructurePiece(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
        super(StructurePieceType.JIGSAW, compoundTag);
        this.structureManager = structurePieceSerializationContext.structureManager();
        this.position = new BlockPos(compoundTag.getInt("PosX"), compoundTag.getInt("PosY"), compoundTag.getInt("PosZ"));
        this.groundLevelDelta = compoundTag.getInt("ground_level_delta");
        RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, structurePieceSerializationContext.registryAccess());
        this.element = (StructurePoolElement)StructurePoolElement.CODEC.parse(registryOps, (Object)compoundTag.getCompound("pool_element")).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).orElseThrow(() -> new IllegalStateException("Invalid pool element found"));
        this.rotation = Rotation.valueOf(compoundTag.getString("rotation"));
        this.boundingBox = this.element.getBoundingBox(this.structureManager, this.position, this.rotation);
        ListTag listTag = compoundTag.getList("junctions", 10);
        this.junctions.clear();
        listTag.forEach(tag -> this.junctions.add(JigsawJunction.deserialize(new Dynamic(registryOps, tag))));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
        compoundTag.putInt("PosX", this.position.getX());
        compoundTag.putInt("PosY", this.position.getY());
        compoundTag.putInt("PosZ", this.position.getZ());
        compoundTag.putInt("ground_level_delta", this.groundLevelDelta);
        RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, structurePieceSerializationContext.registryAccess());
        StructurePoolElement.CODEC.encodeStart(registryOps, (Object)this.element).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag.put("pool_element", (Tag)tag));
        compoundTag.putString("rotation", this.rotation.name());
        ListTag listTag = new ListTag();
        for (JigsawJunction jigsawJunction : this.junctions) {
            listTag.add((Tag)jigsawJunction.serialize(registryOps).getValue());
        }
        compoundTag.put("junctions", listTag);
    }

    @Override
    public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        this.place(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, blockPos, false);
    }

    public void place(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, BlockPos blockPos, boolean bl) {
        this.element.place(this.structureManager, worldGenLevel, structureFeatureManager, chunkGenerator, this.position, blockPos, this.rotation, boundingBox, random, bl);
    }

    @Override
    public void move(int n, int n2, int n3) {
        super.move(n, n2, n3);
        this.position = this.position.offset(n, n2, n3);
    }

    @Override
    public Rotation getRotation() {
        return this.rotation;
    }

    public String toString() {
        return String.format("<%s | %s | %s | %s>", new Object[]{this.getClass().getSimpleName(), this.position, this.rotation, this.element});
    }

    public StructurePoolElement getElement() {
        return this.element;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public int getGroundLevelDelta() {
        return this.groundLevelDelta;
    }

    public void addJunction(JigsawJunction jigsawJunction) {
        this.junctions.add(jigsawJunction);
    }

    public List<JigsawJunction> getJunctions() {
        return this.junctions;
    }
}

