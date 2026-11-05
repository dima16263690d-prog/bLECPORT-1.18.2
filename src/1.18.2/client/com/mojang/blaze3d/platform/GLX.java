/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  org.lwjgl.Version
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.glfw.GLFWErrorCallback
 *  org.lwjgl.glfw.GLFWErrorCallbackI
 *  org.lwjgl.glfw.GLFWVidMode
 *  org.slf4j.Logger
 *  oshi.SystemInfo
 *  oshi.hardware.CentralProcessor
 */
package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.platform.GlDebug;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

@DontObfuscate
public class GLX {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static String cpuInfo;

    public static String getOpenGLVersionString() {
        RenderSystem.assertOnRenderThread();
        if (GLFW.glfwGetCurrentContext() == 0L) {
            return "NO CONTEXT";
        }
        return GlStateManager._getString(7937) + " GL version " + GlStateManager._getString(7938) + ", " + GlStateManager._getString(7936);
    }

    public static int _getRefreshRate(Window window) {
        RenderSystem.assertOnRenderThread();
        long l = GLFW.glfwGetWindowMonitor((long)window.getWindow());
        if (l == 0L) {
            l = GLFW.glfwGetPrimaryMonitor();
        }
        GLFWVidMode gLFWVidMode = l == 0L ? null : GLFW.glfwGetVideoMode((long)l);
        return gLFWVidMode == null ? 0 : gLFWVidMode.refreshRate();
    }

    public static String _getLWJGLVersion() {
        RenderSystem.assertInInitPhase();
        return Version.getVersion();
    }

    public static LongSupplier _initGlfw() {
        LongSupplier longSupplier;
        RenderSystem.assertInInitPhase();
        Window.checkGlfwError((n, string) -> {
            throw new IllegalStateException(String.format("GLFW error before init: [0x%X]%s", n, string));
        });
        ArrayList arrayList = Lists.newArrayList();
        GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback((n, l) -> arrayList.add(String.format("GLFW error during init: [0x%X]%s", n, l)));
        if (GLFW.glfwInit()) {
            longSupplier = () -> (long)(GLFW.glfwGetTime() * 1.0E9);
            for (String string2 : arrayList) {
                LOGGER.error("GLFW error collected during initialization: {}", (Object)string2);
            }
        } else {
            throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on((String)",").join((Iterable)arrayList));
        }
        RenderSystem.setErrorCallback((GLFWErrorCallbackI)gLFWErrorCallback);
        return longSupplier;
    }

    public static void _setGlfwErrorCallback(GLFWErrorCallbackI gLFWErrorCallbackI) {
        RenderSystem.assertInInitPhase();
        GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback((GLFWErrorCallbackI)gLFWErrorCallbackI);
        if (gLFWErrorCallback != null) {
            gLFWErrorCallback.free();
        }
    }

    public static boolean _shouldClose(Window window) {
        return GLFW.glfwWindowShouldClose((long)window.getWindow());
    }

    public static void _init(int n, boolean bl) {
        RenderSystem.assertInInitPhase();
        try {
            CentralProcessor centralProcessor = new SystemInfo().getHardware().getProcessor();
            cpuInfo = String.format("%dx %s", centralProcessor.getLogicalProcessorCount(), centralProcessor.getProcessorIdentifier().getName()).replaceAll("\\s+", " ");
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        GlDebug.enableDebugCallback(n, bl);
    }

    public static String _getCpuInfo() {
        return cpuInfo == null ? "<unknown>" : cpuInfo;
    }

    public static void _renderCrosshair(int n, boolean bl, boolean bl2, boolean bl3) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._disableTexture();
        GlStateManager._depthMask(false);
        GlStateManager._disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.lineWidth(4.0f);
        bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        if (bl) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).normal(1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(n, 0.0, 0.0).color(0, 0, 0, 255).normal(1.0f, 0.0f, 0.0f).endVertex();
        }
        if (bl2) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).normal(0.0f, 1.0f, 0.0f).endVertex();
            bufferBuilder.vertex(0.0, n, 0.0).color(0, 0, 0, 255).normal(0.0f, 1.0f, 0.0f).endVertex();
        }
        if (bl3) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).normal(0.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(0.0, 0.0, n).color(0, 0, 0, 255).normal(0.0f, 0.0f, 1.0f).endVertex();
        }
        tesselator.end();
        RenderSystem.lineWidth(2.0f);
        bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        if (bl) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(255, 0, 0, 255).normal(1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(n, 0.0, 0.0).color(255, 0, 0, 255).normal(1.0f, 0.0f, 0.0f).endVertex();
        }
        if (bl2) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 255, 0, 255).normal(0.0f, 1.0f, 0.0f).endVertex();
            bufferBuilder.vertex(0.0, n, 0.0).color(0, 255, 0, 255).normal(0.0f, 1.0f, 0.0f).endVertex();
        }
        if (bl3) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(127, 127, 255, 255).normal(0.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(0.0, 0.0, n).color(127, 127, 255, 255).normal(0.0f, 0.0f, 1.0f).endVertex();
        }
        tesselator.end();
        RenderSystem.lineWidth(1.0f);
        GlStateManager._enableCull();
        GlStateManager._depthMask(true);
        GlStateManager._enableTexture();
    }

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T t, Consumer<T> consumer) {
        consumer.accept(t);
        return t;
    }
}

