/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.lang3.StringUtils
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.shaders;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class Program {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_LOG_LENGTH = 32768;
    private final Type type;
    private final String name;
    private int id;

    protected Program(Type type, int n, String string) {
        this.type = type;
        this.id = n;
        this.name = string;
    }

    public void attachToShader(Shader shader) {
        RenderSystem.assertOnRenderThread();
        GlStateManager.glAttachShader(shader.getId(), this.getId());
    }

    public void close() {
        if (this.id == -1) {
            return;
        }
        RenderSystem.assertOnRenderThread();
        GlStateManager.glDeleteShader(this.id);
        this.id = -1;
        this.type.getPrograms().remove(this.name);
    }

    public String getName() {
        return this.name;
    }

    public static Program compileShader(Type type, String string, InputStream inputStream, String string2, GlslPreprocessor glslPreprocessor) throws IOException {
        RenderSystem.assertOnRenderThread();
        int n = Program.compileShaderInternal(type, string, inputStream, string2, glslPreprocessor);
        Program program = new Program(type, n, string);
        type.getPrograms().put(string, program);
        return program;
    }

    protected static int compileShaderInternal(Type type, String string, InputStream inputStream, String string2, GlslPreprocessor glslPreprocessor) throws IOException {
        String string3 = TextureUtil.readResourceAsString(inputStream);
        if (string3 == null) {
            throw new IOException("Could not load program " + type.getName());
        }
        int n = GlStateManager.glCreateShader(type.getGlType());
        GlStateManager.glShaderSource(n, glslPreprocessor.process(string3));
        GlStateManager.glCompileShader(n);
        if (GlStateManager.glGetShaderi(n, 35713) == 0) {
            String string4 = StringUtils.trim((String)GlStateManager.glGetShaderInfoLog(n, 32768));
            throw new IOException("Couldn't compile " + type.getName() + " program (" + string2 + ", " + string + ") : " + string4);
        }
        return n;
    }

    private static Program createProgram(Type type, String string, int n) {
        return new Program(type, n, string);
    }

    protected int getId() {
        return this.id;
    }

    public static enum Type {
        VERTEX("vertex", ".vsh", 35633),
        FRAGMENT("fragment", ".fsh", 35632);

        private final String name;
        private final String extension;
        private final int glType;
        private final Map<String, Program> programs = Maps.newHashMap();

        private Type(String string2, String string3, int n2) {
            this.name = string2;
            this.extension = string3;
            this.glType = n2;
        }

        public String getName() {
            return this.name;
        }

        public String getExtension() {
            return this.extension;
        }

        int getGlType() {
            return this.glType;
        }

        public Map<String, Program> getPrograms() {
            return this.programs;
        }
    }
}

