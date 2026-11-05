/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.stream.Collectors;

public class VertexFormat {
    private final ImmutableList<VertexFormatElement> elements;
    private final ImmutableMap<String, VertexFormatElement> elementMapping;
    private final IntList offsets = new IntArrayList();
    private final int vertexSize;
    private int vertexArrayObject;
    private int vertexBufferObject;
    private int indexBufferObject;

    public VertexFormat(ImmutableMap<String, VertexFormatElement> immutableMap) {
        this.elementMapping = immutableMap;
        this.elements = immutableMap.values().asList();
        int n = 0;
        for (VertexFormatElement vertexFormatElement : immutableMap.values()) {
            this.offsets.add(n);
            n += vertexFormatElement.getByteSize();
        }
        this.vertexSize = n;
    }

    public String toString() {
        return "format: " + this.elementMapping.size() + " elements: " + this.elementMapping.entrySet().stream().map(Object::toString).collect(Collectors.joining(" "));
    }

    public int getIntegerSize() {
        return this.getVertexSize() / 4;
    }

    public int getVertexSize() {
        return this.vertexSize;
    }

    public ImmutableList<VertexFormatElement> getElements() {
        return this.elements;
    }

    public ImmutableList<String> getElementAttributeNames() {
        return this.elementMapping.keySet().asList();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        VertexFormat vertexFormat = (VertexFormat)object;
        if (this.vertexSize != vertexFormat.vertexSize) {
            return false;
        }
        return this.elementMapping.equals(vertexFormat.elementMapping);
    }

    public int hashCode() {
        return this.elementMapping.hashCode();
    }

    public void setupBufferState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::_setupBufferState);
            return;
        }
        this._setupBufferState();
    }

    private void _setupBufferState() {
        int n = this.getVertexSize();
        ImmutableList<VertexFormatElement> immutableList = this.getElements();
        for (int i = 0; i < immutableList.size(); ++i) {
            ((VertexFormatElement)immutableList.get(i)).setupBufferState(i, this.offsets.getInt(i), n);
        }
    }

    public void clearBufferState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::_clearBufferState);
            return;
        }
        this._clearBufferState();
    }

    private void _clearBufferState() {
        ImmutableList<VertexFormatElement> immutableList = this.getElements();
        for (int i = 0; i < immutableList.size(); ++i) {
            VertexFormatElement vertexFormatElement = (VertexFormatElement)immutableList.get(i);
            vertexFormatElement.clearBufferState(i);
        }
    }

    public int getOrCreateVertexArrayObject() {
        if (this.vertexArrayObject == 0) {
            this.vertexArrayObject = GlStateManager._glGenVertexArrays();
        }
        return this.vertexArrayObject;
    }

    public int getOrCreateVertexBufferObject() {
        if (this.vertexBufferObject == 0) {
            this.vertexBufferObject = GlStateManager._glGenBuffers();
        }
        return this.vertexBufferObject;
    }

    public int getOrCreateIndexBufferObject() {
        if (this.indexBufferObject == 0) {
            this.indexBufferObject = GlStateManager._glGenBuffers();
        }
        return this.indexBufferObject;
    }

    public static enum Mode {
        LINES(4, 2, 2),
        LINE_STRIP(5, 2, 1),
        DEBUG_LINES(1, 2, 2),
        DEBUG_LINE_STRIP(3, 2, 1),
        TRIANGLES(4, 3, 3),
        TRIANGLE_STRIP(5, 3, 1),
        TRIANGLE_FAN(6, 3, 1),
        QUADS(4, 4, 4);

        public final int asGLMode;
        public final int primitiveLength;
        public final int primitiveStride;

        private Mode(int n2, int n3, int n4) {
            this.asGLMode = n2;
            this.primitiveLength = n3;
            this.primitiveStride = n4;
        }

        public int indexCount(int n) {
            return switch (this) {
                case LINE_STRIP, DEBUG_LINES, DEBUG_LINE_STRIP, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN -> n;
                case LINES, QUADS -> n / 4 * 6;
                default -> 0;
            };
        }
    }

    public static enum IndexType {
        BYTE(5121, 1),
        SHORT(5123, 2),
        INT(5125, 4);

        public final int asGLType;
        public final int bytes;

        private IndexType(int n2, int n3) {
            this.asGLType = n2;
            this.bytes = n3;
        }

        public static IndexType least(int n) {
            if ((n & 0xFFFF0000) != 0) {
                return INT;
            }
            if ((n & 0xFF00) != 0) {
                return SHORT;
            }
            return BYTE;
        }
    }
}

