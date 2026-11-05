/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

public class GlUtil {
    public static ByteBuffer allocateMemory(int n) {
        return MemoryUtil.memAlloc((int)n);
    }

    public static void freeMemory(Buffer buffer) {
        MemoryUtil.memFree((Buffer)buffer);
    }

    public static String getVendor() {
        return GlStateManager._getString(7936);
    }

    public static String getCpuInfo() {
        return GLX._getCpuInfo();
    }

    public static String getRenderer() {
        return GlStateManager._getString(7937);
    }

    public static String getOpenGLVersion() {
        return GlStateManager._getString(7938);
    }
}

