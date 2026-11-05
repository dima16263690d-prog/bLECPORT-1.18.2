/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.OptionalDynamic
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.slf4j.Logger;

public class SectionStorage<R>
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SECTIONS_TAG = "Sections";
    private final IOWorker worker;
    private final Long2ObjectMap<Optional<R>> storage = new Long2ObjectOpenHashMap();
    private final LongLinkedOpenHashSet dirty = new LongLinkedOpenHashSet();
    private final Function<Runnable, Codec<R>> codec;
    private final Function<Runnable, R> factory;
    private final DataFixer fixerUpper;
    private final DataFixTypes type;
    protected final LevelHeightAccessor levelHeightAccessor;

    public SectionStorage(Path path, Function<Runnable, Codec<R>> function, Function<Runnable, R> function2, DataFixer dataFixer, DataFixTypes dataFixTypes, boolean bl, LevelHeightAccessor levelHeightAccessor) {
        this.codec = function;
        this.factory = function2;
        this.fixerUpper = dataFixer;
        this.type = dataFixTypes;
        this.levelHeightAccessor = levelHeightAccessor;
        this.worker = new IOWorker(path, bl, path.getFileName().toString());
    }

    protected void tick(BooleanSupplier booleanSupplier) {
        while (this.hasWork() && booleanSupplier.getAsBoolean()) {
            ChunkPos chunkPos = SectionPos.of(this.dirty.firstLong()).chunk();
            this.writeColumn(chunkPos);
        }
    }

    public boolean hasWork() {
        return !this.dirty.isEmpty();
    }

    @Nullable
    protected Optional<R> get(long l) {
        return (Optional)this.storage.get(l);
    }

    protected Optional<R> getOrLoad(long l) {
        if (this.outsideStoredRange(l)) {
            return Optional.empty();
        }
        Optional<R> optional = this.get(l);
        if (optional != null) {
            return optional;
        }
        this.readColumn(SectionPos.of(l).chunk());
        optional = this.get(l);
        if (optional == null) {
            throw Util.pauseInIde(new IllegalStateException());
        }
        return optional;
    }

    protected boolean outsideStoredRange(long l) {
        int n = SectionPos.sectionToBlockCoord(SectionPos.y(l));
        return this.levelHeightAccessor.isOutsideBuildHeight(n);
    }

    protected R getOrCreate(long l) {
        if (this.outsideStoredRange(l)) {
            throw Util.pauseInIde(new IllegalArgumentException("sectionPos out of bounds"));
        }
        Optional<R> optional = this.getOrLoad(l);
        if (optional.isPresent()) {
            return optional.get();
        }
        R r = this.factory.apply(() -> this.setDirty(l));
        this.storage.put(l, Optional.of(r));
        return r;
    }

    private void readColumn(ChunkPos chunkPos) {
        this.readColumn(chunkPos, NbtOps.INSTANCE, this.tryRead(chunkPos));
    }

    @Nullable
    private CompoundTag tryRead(ChunkPos chunkPos) {
        try {
            return this.worker.load(chunkPos);
        }
        catch (IOException iOException) {
            LOGGER.error("Error reading chunk {} data from disk", (Object)chunkPos, (Object)iOException);
            return null;
        }
    }

    private <T> void readColumn(ChunkPos chunkPos, DynamicOps<T> dynamicOps, @Nullable T t) {
        if (t == null) {
            for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
                this.storage.put(SectionStorage.getKey(chunkPos, i), Optional.empty());
            }
        } else {
            int n;
            Dynamic dynamic2 = new Dynamic(dynamicOps, t);
            int n2 = SectionStorage.getVersion(dynamic2);
            boolean bl = n2 != (n = SharedConstants.getCurrentVersion().getWorldVersion());
            Dynamic dynamic3 = this.fixerUpper.update(this.type.getType(), dynamic2, n2, n);
            OptionalDynamic optionalDynamic = dynamic3.get(SECTIONS_TAG);
            for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
                long l = SectionStorage.getKey(chunkPos, i);
                Optional optional = optionalDynamic.get(Integer.toString(i)).result().flatMap(dynamic -> this.codec.apply(() -> this.setDirty(l)).parse(dynamic).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)));
                this.storage.put(l, optional);
                optional.ifPresent(object -> {
                    this.onSectionLoad(l);
                    if (bl) {
                        this.setDirty(l);
                    }
                });
            }
        }
    }

    private void writeColumn(ChunkPos chunkPos) {
        Dynamic<Tag> dynamic = this.writeColumn(chunkPos, NbtOps.INSTANCE);
        Tag tag = (Tag)dynamic.getValue();
        if (tag instanceof CompoundTag) {
            this.worker.store(chunkPos, (CompoundTag)tag);
        } else {
            LOGGER.error("Expected compound tag, got {}", (Object)tag);
        }
    }

    private <T> Dynamic<T> writeColumn(ChunkPos chunkPos, DynamicOps<T> dynamicOps) {
        HashMap hashMap = Maps.newHashMap();
        for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
            long l = SectionStorage.getKey(chunkPos, i);
            this.dirty.remove(l);
            Optional optional = (Optional)this.storage.get(l);
            if (optional == null || !optional.isPresent()) continue;
            DataResult dataResult = this.codec.apply(() -> this.setDirty(l)).encodeStart(dynamicOps, optional.get());
            String string = Integer.toString(i);
            dataResult.resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(object -> hashMap.put(dynamicOps.createString(string), object));
        }
        return new Dynamic(dynamicOps, dynamicOps.createMap((Map)ImmutableMap.of((Object)dynamicOps.createString(SECTIONS_TAG), (Object)dynamicOps.createMap((Map)hashMap), (Object)dynamicOps.createString("DataVersion"), (Object)dynamicOps.createInt(SharedConstants.getCurrentVersion().getWorldVersion()))));
    }

    private static long getKey(ChunkPos chunkPos, int n) {
        return SectionPos.asLong(chunkPos.x, n, chunkPos.z);
    }

    protected void onSectionLoad(long l) {
    }

    protected void setDirty(long l) {
        Optional optional = (Optional)this.storage.get(l);
        if (optional == null || !optional.isPresent()) {
            LOGGER.warn("No data for position: {}", (Object)SectionPos.of(l));
            return;
        }
        this.dirty.add(l);
    }

    private static int getVersion(Dynamic<?> dynamic) {
        return dynamic.get("DataVersion").asInt(1945);
    }

    public void flush(ChunkPos chunkPos) {
        if (this.hasWork()) {
            for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
                long l = SectionStorage.getKey(chunkPos, i);
                if (!this.dirty.contains(l)) continue;
                this.writeColumn(chunkPos);
                return;
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }
}

