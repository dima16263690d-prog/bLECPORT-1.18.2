/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.primitives.Floats
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntArrays
 *  it.unimi.dsi.fastutil.ints.IntConsumer
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferVertexConsumer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

public class BufferBuilder
extends DefaultedVertexConsumer
implements BufferVertexConsumer {
    private static final int GROWTH_SIZE = 0x200000;
    private static final Logger LOGGER = LogUtils.getLogger();
    private ByteBuffer buffer;
    private final List<DrawState> drawStates = Lists.newArrayList();
    private int lastPoppedStateIndex;
    private int totalRenderedBytes;
    private int nextElementByte;
    private int totalUploadedBytes;
    private int vertices;
    @Nullable
    private VertexFormatElement currentElement;
    private int elementIndex;
    private VertexFormat format;
    private VertexFormat.Mode mode;
    private boolean fastFormat;
    private boolean fullFormat;
    private boolean building;
    @Nullable
    private Vector3f[] sortingPoints;
    private float sortX = Float.NaN;
    private float sortY = Float.NaN;
    private float sortZ = Float.NaN;
    private boolean indexOnly;

    public BufferBuilder(int n) {
        this.buffer = MemoryTracker.create(n * 6);
    }

    private void ensureVertexCapacity() {
        this.ensureCapacity(this.format.getVertexSize());
    }

    private void ensureCapacity(int n) {
        if (this.nextElementByte + n <= this.buffer.capacity()) {
            return;
        }
        int n2 = this.buffer.capacity();
        int n3 = n2 + BufferBuilder.roundUp(n);
        LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", (Object)n2, (Object)n3);
        ByteBuffer byteBuffer = MemoryTracker.resize(this.buffer, n3);
        byteBuffer.rewind();
        this.buffer = byteBuffer;
    }

    private static int roundUp(int n) {
        int n2;
        int n3 = 0x200000;
        if (n == 0) {
            return n3;
        }
        if (n < 0) {
            n3 *= -1;
        }
        if ((n2 = n % n3) == 0) {
            return n;
        }
        return n + n3 - n2;
    }

    public void setQuadSortOrigin(float f, float f2, float f3) {
        if (this.mode != VertexFormat.Mode.QUADS) {
            return;
        }
        if (this.sortX != f || this.sortY != f2 || this.sortZ != f3) {
            this.sortX = f;
            this.sortY = f2;
            this.sortZ = f3;
            if (this.sortingPoints == null) {
                this.sortingPoints = this.makeQuadSortingPoints();
            }
        }
    }

    public SortState getSortState() {
        return new SortState(this.mode, this.vertices, this.sortingPoints, this.sortX, this.sortY, this.sortZ);
    }

    public void restoreSortState(SortState sortState) {
        this.buffer.clear();
        this.mode = sortState.mode;
        this.vertices = sortState.vertices;
        this.nextElementByte = this.totalRenderedBytes;
        this.sortingPoints = sortState.sortingPoints;
        this.sortX = sortState.sortX;
        this.sortY = sortState.sortY;
        this.sortZ = sortState.sortZ;
        this.indexOnly = true;
    }

    public void begin(VertexFormat.Mode mode, VertexFormat vertexFormat) {
        if (this.building) {
            throw new IllegalStateException("Already building!");
        }
        this.building = true;
        this.mode = mode;
        this.switchFormat(vertexFormat);
        this.currentElement = (VertexFormatElement)vertexFormat.getElements().get(0);
        this.elementIndex = 0;
        this.buffer.clear();
    }

    private void switchFormat(VertexFormat vertexFormat) {
        if (this.format == vertexFormat) {
            return;
        }
        this.format = vertexFormat;
        boolean bl = vertexFormat == DefaultVertexFormat.NEW_ENTITY;
        boolean bl2 = vertexFormat == DefaultVertexFormat.BLOCK;
        this.fastFormat = bl || bl2;
        this.fullFormat = bl;
    }

    private IntConsumer intConsumer(VertexFormat.IndexType indexType) {
        switch (indexType) {
            case BYTE: {
                return n -> this.buffer.put((byte)n);
            }
            case SHORT: {
                return n -> this.buffer.putShort((short)n);
            }
        }
        return n -> this.buffer.putInt(n);
    }

    private Vector3f[] makeQuadSortingPoints() {
        FloatBuffer floatBuffer = this.buffer.asFloatBuffer();
        int n = this.totalRenderedBytes / 4;
        int n2 = this.format.getIntegerSize();
        int n3 = n2 * this.mode.primitiveStride;
        int n4 = this.vertices / this.mode.primitiveStride;
        Vector3f[] vector3fArray = new Vector3f[n4];
        for (int i = 0; i < n4; ++i) {
            float f = floatBuffer.get(n + i * n3 + 0);
            float f2 = floatBuffer.get(n + i * n3 + 1);
            float f3 = floatBuffer.get(n + i * n3 + 2);
            float f4 = floatBuffer.get(n + i * n3 + n2 * 2 + 0);
            float f5 = floatBuffer.get(n + i * n3 + n2 * 2 + 1);
            float f6 = floatBuffer.get(n + i * n3 + n2 * 2 + 2);
            float f7 = (f + f4) / 2.0f;
            float f8 = (f2 + f5) / 2.0f;
            float f9 = (f3 + f6) / 2.0f;
            vector3fArray[i] = new Vector3f(f7, f8, f9);
        }
        return vector3fArray;
    }

    private void putSortedQuadIndices(VertexFormat.IndexType indexType) {
        float[] fArray = new float[this.sortingPoints.length];
        int[] nArray = new int[this.sortingPoints.length];
        for (int i = 0; i < this.sortingPoints.length; ++i) {
            float f = this.sortingPoints[i].x() - this.sortX;
            float f2 = this.sortingPoints[i].y() - this.sortY;
            float f3 = this.sortingPoints[i].z() - this.sortZ;
            fArray[i] = f * f + f2 * f2 + f3 * f3;
            nArray[i] = i;
        }
        IntArrays.mergeSort((int[])nArray, (n, n2) -> Floats.compare((float)fArray[n2], (float)fArray[n]));
        IntConsumer intConsumer = this.intConsumer(indexType);
        this.buffer.position(this.nextElementByte);
        for (int n3 : nArray) {
            intConsumer.accept(n3 * this.mode.primitiveStride + 0);
            intConsumer.accept(n3 * this.mode.primitiveStride + 1);
            intConsumer.accept(n3 * this.mode.primitiveStride + 2);
            intConsumer.accept(n3 * this.mode.primitiveStride + 2);
            intConsumer.accept(n3 * this.mode.primitiveStride + 3);
            intConsumer.accept(n3 * this.mode.primitiveStride + 0);
        }
    }

    public void end() {
        boolean bl;
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        }
        int n = this.mode.indexCount(this.vertices);
        VertexFormat.IndexType indexType = VertexFormat.IndexType.least(n);
        if (this.sortingPoints != null) {
            int n2 = Mth.roundToward(n * indexType.bytes, 4);
            this.ensureCapacity(n2);
            this.putSortedQuadIndices(indexType);
            bl = false;
            this.nextElementByte += n2;
            this.totalRenderedBytes += this.vertices * this.format.getVertexSize() + n2;
        } else {
            bl = true;
            this.totalRenderedBytes += this.vertices * this.format.getVertexSize();
        }
        this.building = false;
        this.drawStates.add(new DrawState(this.format, this.vertices, n, this.mode, indexType, this.indexOnly, bl));
        this.vertices = 0;
        this.currentElement = null;
        this.elementIndex = 0;
        this.sortingPoints = null;
        this.sortX = Float.NaN;
        this.sortY = Float.NaN;
        this.sortZ = Float.NaN;
        this.indexOnly = false;
    }

    @Override
    public void putByte(int n, byte by) {
        this.buffer.put(this.nextElementByte + n, by);
    }

    @Override
    public void putShort(int n, short s) {
        this.buffer.putShort(this.nextElementByte + n, s);
    }

    @Override
    public void putFloat(int n, float f) {
        this.buffer.putFloat(this.nextElementByte + n, f);
    }

    @Override
    public void endVertex() {
        if (this.elementIndex != 0) {
            throw new IllegalStateException("Not filled all elements of the vertex");
        }
        ++this.vertices;
        this.ensureVertexCapacity();
        if (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP) {
            int n = this.format.getVertexSize();
            this.buffer.position(this.nextElementByte);
            ByteBuffer byteBuffer = this.buffer.duplicate();
            byteBuffer.position(this.nextElementByte - n).limit(this.nextElementByte);
            this.buffer.put(byteBuffer);
            this.nextElementByte += n;
            ++this.vertices;
            this.ensureVertexCapacity();
        }
    }

    @Override
    public void nextElement() {
        VertexFormatElement vertexFormatElement;
        ImmutableList<VertexFormatElement> immutableList = this.format.getElements();
        this.elementIndex = (this.elementIndex + 1) % immutableList.size();
        this.nextElementByte += this.currentElement.getByteSize();
        this.currentElement = vertexFormatElement = (VertexFormatElement)immutableList.get(this.elementIndex);
        if (vertexFormatElement.getUsage() == VertexFormatElement.Usage.PADDING) {
            this.nextElement();
        }
        if (this.defaultColorSet && this.currentElement.getUsage() == VertexFormatElement.Usage.COLOR) {
            BufferVertexConsumer.super.color(this.defaultR, this.defaultG, this.defaultB, this.defaultA);
        }
    }

    @Override
    public VertexConsumer color(int n, int n2, int n3, int n4) {
        if (this.defaultColorSet) {
            throw new IllegalStateException();
        }
        return BufferVertexConsumer.super.color(n, n2, n3, n4);
    }

    @Override
    public void vertex(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, int n, int n2, float f10, float f11, float f12) {
        if (this.defaultColorSet) {
            throw new IllegalStateException();
        }
        if (this.fastFormat) {
            int n3;
            this.putFloat(0, f);
            this.putFloat(4, f2);
            this.putFloat(8, f3);
            this.putByte(12, (byte)(f4 * 255.0f));
            this.putByte(13, (byte)(f5 * 255.0f));
            this.putByte(14, (byte)(f6 * 255.0f));
            this.putByte(15, (byte)(f7 * 255.0f));
            this.putFloat(16, f8);
            this.putFloat(20, f9);
            if (this.fullFormat) {
                this.putShort(24, (short)(n & 0xFFFF));
                this.putShort(26, (short)(n >> 16 & 0xFFFF));
                n3 = 28;
            } else {
                n3 = 24;
            }
            this.putShort(n3 + 0, (short)(n2 & 0xFFFF));
            this.putShort(n3 + 2, (short)(n2 >> 16 & 0xFFFF));
            this.putByte(n3 + 4, BufferVertexConsumer.normalIntValue(f10));
            this.putByte(n3 + 5, BufferVertexConsumer.normalIntValue(f11));
            this.putByte(n3 + 6, BufferVertexConsumer.normalIntValue(f12));
            this.nextElementByte += n3 + 8;
            this.endVertex();
            return;
        }
        super.vertex(f, f2, f3, f4, f5, f6, f7, f8, f9, n, n2, f10, f11, f12);
    }

    public Pair<DrawState, ByteBuffer> popNextBuffer() {
        DrawState drawState = this.drawStates.get(this.lastPoppedStateIndex++);
        this.buffer.position(this.totalUploadedBytes);
        this.totalUploadedBytes += Mth.roundToward(drawState.bufferSize(), 4);
        this.buffer.limit(this.totalUploadedBytes);
        if (this.lastPoppedStateIndex == this.drawStates.size() && this.vertices == 0) {
            this.clear();
        }
        ByteBuffer byteBuffer = this.buffer.slice();
        this.buffer.clear();
        return Pair.of((Object)drawState, (Object)byteBuffer);
    }

    public void clear() {
        if (this.totalRenderedBytes != this.totalUploadedBytes) {
            LOGGER.warn("Bytes mismatch {} {}", (Object)this.totalRenderedBytes, (Object)this.totalUploadedBytes);
        }
        this.discard();
    }

    public void discard() {
        this.totalRenderedBytes = 0;
        this.totalUploadedBytes = 0;
        this.nextElementByte = 0;
        this.drawStates.clear();
        this.lastPoppedStateIndex = 0;
    }

    @Override
    public VertexFormatElement currentElement() {
        if (this.currentElement == null) {
            throw new IllegalStateException("BufferBuilder not started");
        }
        return this.currentElement;
    }

    public boolean building() {
        return this.building;
    }

    public static class SortState {
        final VertexFormat.Mode mode;
        final int vertices;
        @Nullable
        final Vector3f[] sortingPoints;
        final float sortX;
        final float sortY;
        final float sortZ;

        SortState(VertexFormat.Mode mode, int n, @Nullable Vector3f[] vector3fArray, float f, float f2, float f3) {
            this.mode = mode;
            this.vertices = n;
            this.sortingPoints = vector3fArray;
            this.sortX = f;
            this.sortY = f2;
            this.sortZ = f3;
        }
    }

    public static final class DrawState {
        private final VertexFormat format;
        private final int vertexCount;
        private final int indexCount;
        private final VertexFormat.Mode mode;
        private final VertexFormat.IndexType indexType;
        private final boolean indexOnly;
        private final boolean sequentialIndex;

        DrawState(VertexFormat vertexFormat, int n, int n2, VertexFormat.Mode mode, VertexFormat.IndexType indexType, boolean bl, boolean bl2) {
            this.format = vertexFormat;
            this.vertexCount = n;
            this.indexCount = n2;
            this.mode = mode;
            this.indexType = indexType;
            this.indexOnly = bl;
            this.sequentialIndex = bl2;
        }

        public VertexFormat format() {
            return this.format;
        }

        public int vertexCount() {
            return this.vertexCount;
        }

        public int indexCount() {
            return this.indexCount;
        }

        public VertexFormat.Mode mode() {
            return this.mode;
        }

        public VertexFormat.IndexType indexType() {
            return this.indexType;
        }

        public int vertexBufferSize() {
            return this.vertexCount * this.format.getVertexSize();
        }

        private int indexBufferSize() {
            return this.sequentialIndex ? 0 : this.indexCount * this.indexType.bytes;
        }

        public int bufferSize() {
            return this.vertexBufferSize() + this.indexBufferSize();
        }

        public boolean indexOnly() {
            return this.indexOnly;
        }

        public boolean sequentialIndex() {
            return this.sequentialIndex;
        }
    }
}

