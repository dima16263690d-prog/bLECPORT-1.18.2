/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;

public class VertexFormatElement {
    private final Type type;
    private final Usage usage;
    private final int index;
    private final int count;
    private final int byteSize;

    public VertexFormatElement(int n, Type type, Usage usage, int n2) {
        if (!this.supportsUsage(n, usage)) {
            throw new IllegalStateException("Multiple vertex elements of the same type other than UVs are not supported");
        }
        this.usage = usage;
        this.type = type;
        this.index = n;
        this.count = n2;
        this.byteSize = type.getSize() * this.count;
    }

    private boolean supportsUsage(int n, Usage usage) {
        return n == 0 || usage == Usage.UV;
    }

    public final Type getType() {
        return this.type;
    }

    public final Usage getUsage() {
        return this.usage;
    }

    public final int getCount() {
        return this.count;
    }

    public final int getIndex() {
        return this.index;
    }

    public String toString() {
        return this.count + "," + this.usage.getName() + "," + this.type.getName();
    }

    public final int getByteSize() {
        return this.byteSize;
    }

    public final boolean isPosition() {
        return this.usage == Usage.POSITION;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        VertexFormatElement vertexFormatElement = (VertexFormatElement)object;
        if (this.count != vertexFormatElement.count) {
            return false;
        }
        if (this.index != vertexFormatElement.index) {
            return false;
        }
        if (this.type != vertexFormatElement.type) {
            return false;
        }
        return this.usage == vertexFormatElement.usage;
    }

    public int hashCode() {
        int n = this.type.hashCode();
        n = 31 * n + this.usage.hashCode();
        n = 31 * n + this.index;
        n = 31 * n + this.count;
        return n;
    }

    public void setupBufferState(int n, long l, int n2) {
        this.usage.setupBufferState(this.count, this.type.getGlType(), n2, l, this.index, n);
    }

    public void clearBufferState(int n) {
        this.usage.clearBufferState(this.index, n);
    }

    public static enum Usage {
        POSITION("Position", (n, n2, n3, l, n4, n5) -> {
            GlStateManager._enableVertexAttribArray(n5);
            GlStateManager._vertexAttribPointer(n5, n, n2, false, n3, l);
        }, (n, n2) -> GlStateManager._disableVertexAttribArray(n2)),
        NORMAL("Normal", (n, n2, n3, l, n4, n5) -> {
            GlStateManager._enableVertexAttribArray(n5);
            GlStateManager._vertexAttribPointer(n5, n, n2, true, n3, l);
        }, (n, n2) -> GlStateManager._disableVertexAttribArray(n2)),
        COLOR("Vertex Color", (n, n2, n3, l, n4, n5) -> {
            GlStateManager._enableVertexAttribArray(n5);
            GlStateManager._vertexAttribPointer(n5, n, n2, true, n3, l);
        }, (n, n2) -> GlStateManager._disableVertexAttribArray(n2)),
        UV("UV", (n, n2, n3, l, n4, n5) -> {
            GlStateManager._enableVertexAttribArray(n5);
            if (n2 == 5126) {
                GlStateManager._vertexAttribPointer(n5, n, n2, false, n3, l);
            } else {
                GlStateManager._vertexAttribIPointer(n5, n, n2, n3, l);
            }
        }, (n, n2) -> GlStateManager._disableVertexAttribArray(n2)),
        PADDING("Padding", (n, n2, n3, l, n4, n5) -> {}, (n, n2) -> {}),
        GENERIC("Generic", (n, n2, n3, l, n4, n5) -> {
            GlStateManager._enableVertexAttribArray(n5);
            GlStateManager._vertexAttribPointer(n5, n, n2, false, n3, l);
        }, (n, n2) -> GlStateManager._disableVertexAttribArray(n2));

        private final String name;
        private final SetupState setupState;
        private final ClearState clearState;

        private Usage(String string2, SetupState setupState, ClearState clearState) {
            this.name = string2;
            this.setupState = setupState;
            this.clearState = clearState;
        }

        void setupBufferState(int n, int n2, int n3, long l, int n4, int n5) {
            this.setupState.setupBufferState(n, n2, n3, l, n4, n5);
        }

        public void clearBufferState(int n, int n2) {
            this.clearState.clearBufferState(n, n2);
        }

        public String getName() {
            return this.name;
        }

        @FunctionalInterface
        static interface SetupState {
            public void setupBufferState(int var1, int var2, int var3, long var4, int var6, int var7);
        }

        @FunctionalInterface
        static interface ClearState {
            public void clearBufferState(int var1, int var2);
        }
    }

    public static enum Type {
        FLOAT(4, "Float", 5126),
        UBYTE(1, "Unsigned Byte", 5121),
        BYTE(1, "Byte", 5120),
        USHORT(2, "Unsigned Short", 5123),
        SHORT(2, "Short", 5122),
        UINT(4, "Unsigned Int", 5125),
        INT(4, "Int", 5124);

        private final int size;
        private final String name;
        private final int glType;

        private Type(int n2, String string2, int n3) {
            this.size = n2;
            this.name = string2;
            this.glType = n3;
        }

        public int getSize() {
            return this.size;
        }

        public String getName() {
            return this.name;
        }

        public int getGlType() {
            return this.glType;
        }
    }
}

