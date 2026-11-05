/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.system.MemoryUtil$MemoryAllocator
 */
package com.mojang.blaze3d.platform;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

public class MemoryTracker {
    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator((boolean)false);

    public static ByteBuffer create(int n) {
        long l = ALLOCATOR.malloc((long)n);
        if (l == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + n + " bytes");
        }
        return MemoryUtil.memByteBuffer((long)l, (int)n);
    }

    public static ByteBuffer resize(ByteBuffer byteBuffer, int n) {
        long l = ALLOCATOR.realloc(MemoryUtil.memAddress0((Buffer)byteBuffer), (long)n);
        if (l == 0L) {
            throw new OutOfMemoryError("Failed to resize buffer from " + byteBuffer.capacity() + " bytes to " + n + " bytes");
        }
        return MemoryUtil.memByteBuffer((long)l, (int)n);
    }
}

