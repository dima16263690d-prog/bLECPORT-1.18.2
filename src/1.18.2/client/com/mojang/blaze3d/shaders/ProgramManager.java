/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import org.slf4j.Logger;

public class ProgramManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void glUseProgram(int n) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUseProgram(n);
    }

    public static void releaseProgram(Shader shader) {
        RenderSystem.assertOnRenderThread();
        shader.getFragmentProgram().close();
        shader.getVertexProgram().close();
        GlStateManager.glDeleteProgram(shader.getId());
    }

    public static int createProgram() throws IOException {
        RenderSystem.assertOnRenderThread();
        int n = GlStateManager.glCreateProgram();
        if (n <= 0) {
            throw new IOException("Could not create shader program (returned program ID " + n + ")");
        }
        return n;
    }

    public static void linkShader(Shader shader) {
        RenderSystem.assertOnRenderThread();
        shader.attachToProgram();
        GlStateManager.glLinkProgram(shader.getId());
        int n = GlStateManager.glGetProgrami(shader.getId(), 35714);
        if (n == 0) {
            LOGGER.warn("Error encountered when linking program containing VS {} and FS {}. Log output:", (Object)shader.getVertexProgram().getName(), (Object)shader.getFragmentProgram().getName());
            LOGGER.warn(GlStateManager.glGetProgramInfoLog(shader.getId(), 32768));
        }
    }
}

