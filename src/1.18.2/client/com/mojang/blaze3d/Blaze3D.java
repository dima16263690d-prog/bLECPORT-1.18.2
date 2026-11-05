/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

public class Blaze3D {
    public static void process(RenderPipeline renderPipeline, float f) {
        ConcurrentLinkedQueue<RenderCall> concurrentLinkedQueue = renderPipeline.getRecordingQueue();
    }

    public static void render(RenderPipeline renderPipeline, float f) {
        ConcurrentLinkedQueue<RenderCall> concurrentLinkedQueue = renderPipeline.getProcessedQueue();
    }

    public static void youJustLostTheGame() {
        MemoryUtil.memSet((long)0L, (int)0, (long)1L);
    }

    public static double getTime() {
        return GLFW.glfwGetTime();
    }
}

