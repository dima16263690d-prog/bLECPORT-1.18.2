/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.Unpooled
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

public class ClientboundLevelChunkPacketData {
    private static final int TWO_MEGABYTES = 0x200000;
    private final CompoundTag heightmaps;
    private final byte[] buffer;
    private final List<BlockEntityInfo> blockEntitiesData;

    public ClientboundLevelChunkPacketData(LevelChunk levelChunk) {
        this.heightmaps = new CompoundTag();
        for (Map.Entry<Heightmap.Types, Heightmap> entry : levelChunk.getHeightmaps()) {
            if (!entry.getKey().sendToClient()) continue;
            this.heightmaps.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
        }
        this.buffer = new byte[ClientboundLevelChunkPacketData.calculateChunkSize(levelChunk)];
        ClientboundLevelChunkPacketData.extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), levelChunk);
        this.blockEntitiesData = Lists.newArrayList();
        for (Map.Entry<Object, Object> entry : levelChunk.getBlockEntities().entrySet()) {
            this.blockEntitiesData.add(BlockEntityInfo.create((BlockEntity)entry.getValue()));
        }
    }

    public ClientboundLevelChunkPacketData(FriendlyByteBuf friendlyByteBuf, int n, int n2) {
        this.heightmaps = friendlyByteBuf.readNbt();
        if (this.heightmaps == null) {
            throw new RuntimeException("Can't read heightmap in packet for [" + n + ", " + n2 + "]");
        }
        int n3 = friendlyByteBuf.readVarInt();
        if (n3 > 0x200000) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
        }
        this.buffer = new byte[n3];
        friendlyByteBuf.readBytes(this.buffer);
        this.blockEntitiesData = friendlyByteBuf.readList(BlockEntityInfo::new);
    }

    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeNbt(this.heightmaps);
        friendlyByteBuf2.writeVarInt(this.buffer.length);
        friendlyByteBuf2.writeBytes(this.buffer);
        friendlyByteBuf2.writeCollection(this.blockEntitiesData, (friendlyByteBuf, blockEntityInfo) -> blockEntityInfo.write((FriendlyByteBuf)((Object)friendlyByteBuf)));
    }

    private static int calculateChunkSize(LevelChunk levelChunk) {
        int n = 0;
        for (LevelChunkSection levelChunkSection : levelChunk.getSections()) {
            n += levelChunkSection.getSerializedSize();
        }
        return n;
    }

    private ByteBuf getWriteBuffer() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer((byte[])this.buffer);
        byteBuf.writerIndex(0);
        return byteBuf;
    }

    public static void extractChunkData(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk) {
        for (LevelChunkSection levelChunkSection : levelChunk.getSections()) {
            levelChunkSection.write(friendlyByteBuf);
        }
    }

    public Consumer<BlockEntityTagOutput> getBlockEntitiesTagsConsumer(int n, int n2) {
        return blockEntityTagOutput -> this.getBlockEntitiesTags((BlockEntityTagOutput)blockEntityTagOutput, n, n2);
    }

    private void getBlockEntitiesTags(BlockEntityTagOutput blockEntityTagOutput, int n, int n2) {
        int n3 = 16 * n;
        int n4 = 16 * n2;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (BlockEntityInfo blockEntityInfo : this.blockEntitiesData) {
            int n5 = n3 + SectionPos.sectionRelative(blockEntityInfo.packedXZ >> 4);
            int n6 = n4 + SectionPos.sectionRelative(blockEntityInfo.packedXZ);
            mutableBlockPos.set(n5, blockEntityInfo.y, n6);
            blockEntityTagOutput.accept(mutableBlockPos, blockEntityInfo.type, blockEntityInfo.tag);
        }
    }

    public FriendlyByteBuf getReadBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer((byte[])this.buffer));
    }

    public CompoundTag getHeightmaps() {
        return this.heightmaps;
    }

    static class BlockEntityInfo {
        final int packedXZ;
        final int y;
        final BlockEntityType<?> type;
        @Nullable
        final CompoundTag tag;

        private BlockEntityInfo(int n, int n2, BlockEntityType<?> blockEntityType, @Nullable CompoundTag compoundTag) {
            this.packedXZ = n;
            this.y = n2;
            this.type = blockEntityType;
            this.tag = compoundTag;
        }

        private BlockEntityInfo(FriendlyByteBuf friendlyByteBuf) {
            this.packedXZ = friendlyByteBuf.readByte();
            this.y = friendlyByteBuf.readShort();
            int n = friendlyByteBuf.readVarInt();
            this.type = (BlockEntityType)Registry.BLOCK_ENTITY_TYPE.byId(n);
            this.tag = friendlyByteBuf.readNbt();
        }

        void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeByte(this.packedXZ);
            friendlyByteBuf.writeShort(this.y);
            friendlyByteBuf.writeVarInt(Registry.BLOCK_ENTITY_TYPE.getId(this.type));
            friendlyByteBuf.writeNbt(this.tag);
        }

        static BlockEntityInfo create(BlockEntity blockEntity) {
            CompoundTag compoundTag = blockEntity.getUpdateTag();
            BlockPos blockPos = blockEntity.getBlockPos();
            int n = SectionPos.sectionRelative(blockPos.getX()) << 4 | SectionPos.sectionRelative(blockPos.getZ());
            return new BlockEntityInfo(n, blockPos.getY(), blockEntity.getType(), compoundTag.isEmpty() ? null : compoundTag);
        }
    }

    @FunctionalInterface
    public static interface BlockEntityTagOutput {
        public void accept(BlockPos var1, BlockEntityType<?> var2, @Nullable CompoundTag var3);
    }
}

