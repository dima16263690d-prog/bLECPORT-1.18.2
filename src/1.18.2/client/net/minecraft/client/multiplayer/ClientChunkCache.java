/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.slf4j.Logger;

public class ClientChunkCache
extends ChunkSource {
    static final Logger LOGGER = LogUtils.getLogger();
    private final LevelChunk emptyChunk;
    private final LevelLightEngine lightEngine;
    volatile Storage storage;
    final ClientLevel level;

    public ClientChunkCache(ClientLevel clientLevel, int n) {
        this.level = clientLevel;
        this.emptyChunk = new EmptyLevelChunk(clientLevel, new ChunkPos(0, 0), clientLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getHolderOrThrow(Biomes.PLAINS));
        this.lightEngine = new LevelLightEngine(this, true, clientLevel.dimensionType().hasSkyLight());
        this.storage = new Storage(ClientChunkCache.calculateStorageRange(n));
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    private static boolean isValidChunk(@Nullable LevelChunk levelChunk, int n, int n2) {
        if (levelChunk == null) {
            return false;
        }
        ChunkPos chunkPos = levelChunk.getPos();
        return chunkPos.x == n && chunkPos.z == n2;
    }

    public void drop(int n, int n2) {
        if (!this.storage.inRange(n, n2)) {
            return;
        }
        int n3 = this.storage.getIndex(n, n2);
        LevelChunk levelChunk = this.storage.getChunk(n3);
        if (ClientChunkCache.isValidChunk(levelChunk, n, n2)) {
            this.storage.replace(n3, levelChunk, null);
        }
    }

    @Override
    @Nullable
    public LevelChunk getChunk(int n, int n2, ChunkStatus chunkStatus, boolean bl) {
        LevelChunk levelChunk;
        if (this.storage.inRange(n, n2) && ClientChunkCache.isValidChunk(levelChunk = this.storage.getChunk(this.storage.getIndex(n, n2)), n, n2)) {
            return levelChunk;
        }
        if (bl) {
            return this.emptyChunk;
        }
        return null;
    }

    @Override
    public BlockGetter getLevel() {
        return this.level;
    }

    @Nullable
    public LevelChunk replaceWithPacketData(int n, int n2, FriendlyByteBuf friendlyByteBuf, CompoundTag compoundTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer) {
        if (!this.storage.inRange(n, n2)) {
            LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", (Object)n, (Object)n2);
            return null;
        }
        int n3 = this.storage.getIndex(n, n2);
        LevelChunk levelChunk = this.storage.chunks.get(n3);
        ChunkPos chunkPos = new ChunkPos(n, n2);
        if (!ClientChunkCache.isValidChunk(levelChunk, n, n2)) {
            levelChunk = new LevelChunk(this.level, chunkPos);
            levelChunk.replaceWithPacketData(friendlyByteBuf, compoundTag, consumer);
            this.storage.replace(n3, levelChunk);
        } else {
            levelChunk.replaceWithPacketData(friendlyByteBuf, compoundTag, consumer);
        }
        this.level.onChunkLoaded(chunkPos);
        return levelChunk;
    }

    @Override
    public void tick(BooleanSupplier booleanSupplier, boolean bl) {
    }

    public void updateViewCenter(int n, int n2) {
        this.storage.viewCenterX = n;
        this.storage.viewCenterZ = n2;
    }

    public void updateViewRadius(int n) {
        int n2 = this.storage.chunkRadius;
        int n3 = ClientChunkCache.calculateStorageRange(n);
        if (n2 != n3) {
            Storage storage = new Storage(n3);
            storage.viewCenterX = this.storage.viewCenterX;
            storage.viewCenterZ = this.storage.viewCenterZ;
            for (int i = 0; i < this.storage.chunks.length(); ++i) {
                LevelChunk levelChunk = this.storage.chunks.get(i);
                if (levelChunk == null) continue;
                ChunkPos chunkPos = levelChunk.getPos();
                if (!storage.inRange(chunkPos.x, chunkPos.z)) continue;
                storage.replace(storage.getIndex(chunkPos.x, chunkPos.z), levelChunk);
            }
            this.storage = storage;
        }
    }

    private static int calculateStorageRange(int n) {
        return Math.max(2, n) + 3;
    }

    @Override
    public String gatherStats() {
        return this.storage.chunks.length() + ", " + this.getLoadedChunksCount();
    }

    @Override
    public int getLoadedChunksCount() {
        return this.storage.chunkCount;
    }

    @Override
    public void onLightUpdate(LightLayer lightLayer, SectionPos sectionPos) {
        Minecraft.getInstance().levelRenderer.setSectionDirty(sectionPos.x(), sectionPos.y(), sectionPos.z());
    }

    @Override
    @Nullable
    public /* synthetic */ ChunkAccess getChunk(int n, int n2, ChunkStatus chunkStatus, boolean bl) {
        return this.getChunk(n, n2, chunkStatus, bl);
    }

    final class Storage {
        final AtomicReferenceArray<LevelChunk> chunks;
        final int chunkRadius;
        private final int viewRange;
        volatile int viewCenterX;
        volatile int viewCenterZ;
        int chunkCount;

        Storage(int n) {
            this.chunkRadius = n;
            this.viewRange = n * 2 + 1;
            this.chunks = new AtomicReferenceArray(this.viewRange * this.viewRange);
        }

        int getIndex(int n, int n2) {
            return Math.floorMod(n2, this.viewRange) * this.viewRange + Math.floorMod(n, this.viewRange);
        }

        protected void replace(int n, @Nullable LevelChunk levelChunk) {
            LevelChunk levelChunk2 = this.chunks.getAndSet(n, levelChunk);
            if (levelChunk2 != null) {
                --this.chunkCount;
                ClientChunkCache.this.level.unload(levelChunk2);
            }
            if (levelChunk != null) {
                ++this.chunkCount;
            }
        }

        protected LevelChunk replace(int n, LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
            if (this.chunks.compareAndSet(n, levelChunk, levelChunk2) && levelChunk2 == null) {
                --this.chunkCount;
            }
            ClientChunkCache.this.level.unload(levelChunk);
            return levelChunk;
        }

        boolean inRange(int n, int n2) {
            return Math.abs(n - this.viewCenterX) <= this.chunkRadius && Math.abs(n2 - this.viewCenterZ) <= this.chunkRadius;
        }

        @Nullable
        protected LevelChunk getChunk(int n) {
            return this.chunks.get(n);
        }

        private void dumpChunks(String string) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(string);){
                int n = ClientChunkCache.this.storage.chunkRadius;
                for (int i = this.viewCenterZ - n; i <= this.viewCenterZ + n; ++i) {
                    for (int j = this.viewCenterX - n; j <= this.viewCenterX + n; ++j) {
                        LevelChunk levelChunk = ClientChunkCache.this.storage.chunks.get(ClientChunkCache.this.storage.getIndex(j, i));
                        if (levelChunk == null) continue;
                        ChunkPos chunkPos = levelChunk.getPos();
                        fileOutputStream.write((chunkPos.x + "\t" + chunkPos.z + "\t" + levelChunk.isEmpty() + "\n").getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
            catch (IOException iOException) {
                LOGGER.error("Failed to dump chunks to file {}", (Object)string, (Object)iOException);
            }
        }
    }
}

