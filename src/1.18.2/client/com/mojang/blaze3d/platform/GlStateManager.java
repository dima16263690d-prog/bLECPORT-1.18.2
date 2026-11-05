/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Charsets
 *  javax.annotation.Nullable
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL13
 *  org.lwjgl.opengl.GL14
 *  org.lwjgl.opengl.GL15
 *  org.lwjgl.opengl.GL20
 *  org.lwjgl.opengl.GL20C
 *  org.lwjgl.opengl.GL30
 *  org.lwjgl.opengl.GL32C
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.platform;

import com.google.common.base.Charsets;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@DontObfuscate
public class GlStateManager {
    private static final boolean ON_LINUX = Util.getPlatform() == Util.OS.LINUX;
    public static final int TEXTURE_COUNT = 12;
    private static final BlendState BLEND = new BlendState();
    private static final DepthState DEPTH = new DepthState();
    private static final CullState CULL = new CullState();
    private static final PolygonOffsetState POLY_OFFSET = new PolygonOffsetState();
    private static final ColorLogicState COLOR_LOGIC = new ColorLogicState();
    private static final StencilState STENCIL = new StencilState();
    private static final ScissorState SCISSOR = new ScissorState();
    private static int activeTexture;
    private static final TextureState[] TEXTURES;
    private static final ColorMask COLOR_MASK;

    public static void _disableScissorTest() {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager.SCISSOR.mode.disable();
    }

    public static void _enableScissorTest() {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager.SCISSOR.mode.enable();
    }

    public static void _scissorBox(int n, int n2, int n3, int n4) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL20.glScissor((int)n, (int)n2, (int)n3, (int)n4);
    }

    public static void _disableDepthTest() {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager.DEPTH.mode.disable();
    }

    public static void _enableDepthTest() {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager.DEPTH.mode.enable();
    }

    public static void _depthFunc(int n) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (n != GlStateManager.DEPTH.func) {
            GlStateManager.DEPTH.func = n;
            GL11.glDepthFunc((int)n);
        }
    }

    public static void _depthMask(boolean bl) {
        RenderSystem.assertOnRenderThread();
        if (bl != GlStateManager.DEPTH.mask) {
            GlStateManager.DEPTH.mask = bl;
            GL11.glDepthMask((boolean)bl);
        }
    }

    public static void _disableBlend() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.BLEND.mode.disable();
    }

    public static void _enableBlend() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.BLEND.mode.enable();
    }

    public static void _blendFunc(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        if (n != GlStateManager.BLEND.srcRgb || n2 != GlStateManager.BLEND.dstRgb) {
            GlStateManager.BLEND.srcRgb = n;
            GlStateManager.BLEND.dstRgb = n2;
            GL11.glBlendFunc((int)n, (int)n2);
        }
    }

    public static void _blendFuncSeparate(int n, int n2, int n3, int n4) {
        RenderSystem.assertOnRenderThread();
        if (n != GlStateManager.BLEND.srcRgb || n2 != GlStateManager.BLEND.dstRgb || n3 != GlStateManager.BLEND.srcAlpha || n4 != GlStateManager.BLEND.dstAlpha) {
            GlStateManager.BLEND.srcRgb = n;
            GlStateManager.BLEND.dstRgb = n2;
            GlStateManager.BLEND.srcAlpha = n3;
            GlStateManager.BLEND.dstAlpha = n4;
            GlStateManager.glBlendFuncSeparate(n, n2, n3, n4);
        }
    }

    public static void _blendEquation(int n) {
        RenderSystem.assertOnRenderThread();
        GL14.glBlendEquation((int)n);
    }

    public static int glGetProgrami(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetProgrami((int)n, (int)n2);
    }

    public static void glAttachShader(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        GL20.glAttachShader((int)n, (int)n2);
    }

    public static void glDeleteShader(int n) {
        RenderSystem.assertOnRenderThread();
        GL20.glDeleteShader((int)n);
    }

    public static int glCreateShader(int n) {
        RenderSystem.assertOnRenderThread();
        return GL20.glCreateShader((int)n);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void glShaderSource(int n, List<String> list) {
        RenderSystem.assertOnRenderThread();
        StringBuilder stringBuilder = new StringBuilder();
        for (String object2 : list) {
            stringBuilder.append(object2);
        }
        Object object3 = stringBuilder.toString().getBytes(Charsets.UTF_8);
        ByteBuffer byteBuffer = MemoryUtil.memAlloc((int)(((Object)object3).length + 1));
        byteBuffer.put((byte[])object3);
        byteBuffer.put((byte)0);
        byteBuffer.flip();
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
            pointerBuffer.put(byteBuffer);
            GL20C.nglShaderSource((int)n, (int)1, (long)pointerBuffer.address0(), (long)0L);
        }
        finally {
            MemoryUtil.memFree((Buffer)byteBuffer);
        }
    }

    public static void glCompileShader(int n) {
        RenderSystem.assertOnRenderThread();
        GL20.glCompileShader((int)n);
    }

    public static int glGetShaderi(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetShaderi((int)n, (int)n2);
    }

    public static void _glUseProgram(int n) {
        RenderSystem.assertOnRenderThread();
        GL20.glUseProgram((int)n);
    }

    public static int glCreateProgram() {
        RenderSystem.assertOnRenderThread();
        return GL20.glCreateProgram();
    }

    public static void glDeleteProgram(int n) {
        RenderSystem.assertOnRenderThread();
        GL20.glDeleteProgram((int)n);
    }

    public static void glLinkProgram(int n) {
        RenderSystem.assertOnRenderThread();
        GL20.glLinkProgram((int)n);
    }

    public static int _glGetUniformLocation(int n, CharSequence charSequence) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetUniformLocation((int)n, (CharSequence)charSequence);
    }

    public static void _glUniform1(int n, IntBuffer intBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform1iv((int)n, (IntBuffer)intBuffer);
    }

    public static void _glUniform1i(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform1i((int)n, (int)n2);
    }

    public static void _glUniform1(int n, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform1fv((int)n, (FloatBuffer)floatBuffer);
    }

    public static void _glUniform2(int n, IntBuffer intBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform2iv((int)n, (IntBuffer)intBuffer);
    }

    public static void _glUniform2(int n, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform2fv((int)n, (FloatBuffer)floatBuffer);
    }

    public static void _glUniform3(int n, IntBuffer intBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform3iv((int)n, (IntBuffer)intBuffer);
    }

    public static void _glUniform3(int n, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform3fv((int)n, (FloatBuffer)floatBuffer);
    }

    public static void _glUniform4(int n, IntBuffer intBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform4iv((int)n, (IntBuffer)intBuffer);
    }

    public static void _glUniform4(int n, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform4fv((int)n, (FloatBuffer)floatBuffer);
    }

    public static void _glUniformMatrix2(int n, boolean bl, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniformMatrix2fv((int)n, (boolean)bl, (FloatBuffer)floatBuffer);
    }

    public static void _glUniformMatrix3(int n, boolean bl, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniformMatrix3fv((int)n, (boolean)bl, (FloatBuffer)floatBuffer);
    }

    public static void _glUniformMatrix4(int n, boolean bl, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniformMatrix4fv((int)n, (boolean)bl, (FloatBuffer)floatBuffer);
    }

    public static int _glGetAttribLocation(int n, CharSequence charSequence) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetAttribLocation((int)n, (CharSequence)charSequence);
    }

    public static void _glBindAttribLocation(int n, int n2, CharSequence charSequence) {
        RenderSystem.assertOnRenderThread();
        GL20.glBindAttribLocation((int)n, (int)n2, (CharSequence)charSequence);
    }

    public static int _glGenBuffers() {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL15.glGenBuffers();
    }

    public static int _glGenVertexArrays() {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL30.glGenVertexArrays();
    }

    public static void _glBindBuffer(int n, int n2) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL15.glBindBuffer((int)n, (int)n2);
    }

    public static void _glBindVertexArray(int n) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glBindVertexArray((int)n);
    }

    public static void _glBufferData(int n, ByteBuffer byteBuffer, int n2) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL15.glBufferData((int)n, (ByteBuffer)byteBuffer, (int)n2);
    }

    public static void _glBufferData(int n, long l, int n2) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL15.glBufferData((int)n, (long)l, (int)n2);
    }

    @Nullable
    public static ByteBuffer _glMapBuffer(int n, int n2) {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL15.glMapBuffer((int)n, (int)n2);
    }

    public static void _glUnmapBuffer(int n) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL15.glUnmapBuffer((int)n);
    }

    public static void _glDeleteBuffers(int n) {
        RenderSystem.assertOnRenderThread();
        if (ON_LINUX) {
            GL32C.glBindBuffer((int)34962, (int)n);
            GL32C.glBufferData((int)34962, (long)0L, (int)35048);
            GL32C.glBindBuffer((int)34962, (int)0);
        }
        GL15.glDeleteBuffers((int)n);
    }

    public static void _glCopyTexSubImage2D(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL20.glCopyTexSubImage2D((int)n, (int)n2, (int)n3, (int)n4, (int)n5, (int)n6, (int)n7, (int)n8);
    }

    public static void _glDeleteVertexArrays(int n) {
        RenderSystem.assertOnRenderThread();
        GL30.glDeleteVertexArrays((int)n);
    }

    public static void _glBindFramebuffer(int n, int n2) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glBindFramebuffer((int)n, (int)n2);
    }

    public static void _glBlitFrameBuffer(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9, int n10) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glBlitFramebuffer((int)n, (int)n2, (int)n3, (int)n4, (int)n5, (int)n6, (int)n7, (int)n8, (int)n9, (int)n10);
    }

    public static void _glBindRenderbuffer(int n, int n2) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glBindRenderbuffer((int)n, (int)n2);
    }

    public static void _glDeleteRenderbuffers(int n) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glDeleteRenderbuffers((int)n);
    }

    public static void _glDeleteFramebuffers(int n) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glDeleteFramebuffers((int)n);
    }

    public static int glGenFramebuffers() {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL30.glGenFramebuffers();
    }

    public static int glGenRenderbuffers() {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL30.glGenRenderbuffers();
    }

    public static void _glRenderbufferStorage(int n, int n2, int n3, int n4) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glRenderbufferStorage((int)n, (int)n2, (int)n3, (int)n4);
    }

    public static void _glFramebufferRenderbuffer(int n, int n2, int n3, int n4) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glFramebufferRenderbuffer((int)n, (int)n2, (int)n3, (int)n4);
    }

    public static int glCheckFramebufferStatus(int n) {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL30.glCheckFramebufferStatus((int)n);
    }

    public static void _glFramebufferTexture2D(int n, int n2, int n3, int n4, int n5) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL30.glFramebufferTexture2D((int)n, (int)n2, (int)n3, (int)n4, (int)n5);
    }

    public static int getBoundFramebuffer() {
        RenderSystem.assertOnRenderThread();
        return GlStateManager._getInteger(36006);
    }

    public static void glActiveTexture(int n) {
        RenderSystem.assertOnRenderThread();
        GL13.glActiveTexture((int)n);
    }

    public static void glBlendFuncSeparate(int n, int n2, int n3, int n4) {
        RenderSystem.assertOnRenderThread();
        GL14.glBlendFuncSeparate((int)n, (int)n2, (int)n3, (int)n4);
    }

    public static String glGetShaderInfoLog(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetShaderInfoLog((int)n, (int)n2);
    }

    public static String glGetProgramInfoLog(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetProgramInfoLog((int)n, (int)n2);
    }

    public static void setupLevelDiffuseLighting(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f) {
        RenderSystem.assertOnRenderThread();
        Vector4f vector4f = new Vector4f(vector3f);
        vector4f.transform(matrix4f);
        Vector4f vector4f2 = new Vector4f(vector3f2);
        vector4f2.transform(matrix4f);
        RenderSystem.setShaderLights(new Vector3f(vector4f), new Vector3f(vector4f2));
    }

    public static void setupGuiFlatDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertOnRenderThread();
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        matrix4f.multiply(Matrix4f.createScaleMatrix(1.0f, -1.0f, 1.0f));
        matrix4f.multiply(Vector3f.YP.rotationDegrees(-22.5f));
        matrix4f.multiply(Vector3f.XP.rotationDegrees(135.0f));
        GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
    }

    public static void setupGui3DDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertOnRenderThread();
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        matrix4f.multiply(Vector3f.YP.rotationDegrees(62.0f));
        matrix4f.multiply(Vector3f.XP.rotationDegrees(185.5f));
        matrix4f.multiply(Vector3f.YP.rotationDegrees(-22.5f));
        matrix4f.multiply(Vector3f.XP.rotationDegrees(135.0f));
        GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
    }

    public static void _enableCull() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.CULL.enable.enable();
    }

    public static void _disableCull() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.CULL.enable.disable();
    }

    public static void _polygonMode(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        GL11.glPolygonMode((int)n, (int)n2);
    }

    public static void _enablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.POLY_OFFSET.fill.enable();
    }

    public static void _disablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.POLY_OFFSET.fill.disable();
    }

    public static void _polygonOffset(float f, float f2) {
        RenderSystem.assertOnRenderThread();
        if (f != GlStateManager.POLY_OFFSET.factor || f2 != GlStateManager.POLY_OFFSET.units) {
            GlStateManager.POLY_OFFSET.factor = f;
            GlStateManager.POLY_OFFSET.units = f2;
            GL11.glPolygonOffset((float)f, (float)f2);
        }
    }

    public static void _enableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.COLOR_LOGIC.enable.enable();
    }

    public static void _disableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.COLOR_LOGIC.enable.disable();
    }

    public static void _logicOp(int n) {
        RenderSystem.assertOnRenderThread();
        if (n != GlStateManager.COLOR_LOGIC.op) {
            GlStateManager.COLOR_LOGIC.op = n;
            GL11.glLogicOp((int)n);
        }
    }

    public static void _activeTexture(int n) {
        RenderSystem.assertOnRenderThread();
        if (activeTexture != n - 33984) {
            activeTexture = n - 33984;
            GlStateManager.glActiveTexture(n);
        }
    }

    public static void _enableTexture() {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager.TEXTURES[GlStateManager.activeTexture].enable = true;
    }

    public static void _disableTexture() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.TEXTURES[GlStateManager.activeTexture].enable = false;
    }

    public static void _texParameter(int n, int n2, float f) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glTexParameterf((int)n, (int)n2, (float)f);
    }

    public static void _texParameter(int n, int n2, int n3) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glTexParameteri((int)n, (int)n2, (int)n3);
    }

    public static int _getTexLevelParameter(int n, int n2, int n3) {
        RenderSystem.assertInInitPhase();
        return GL11.glGetTexLevelParameteri((int)n, (int)n2, (int)n3);
    }

    public static int _genTexture() {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL11.glGenTextures();
    }

    public static void _genTextures(int[] nArray) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glGenTextures((int[])nArray);
    }

    public static void _deleteTexture(int n) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glDeleteTextures((int)n);
        for (TextureState textureState : TEXTURES) {
            if (textureState.binding != n) continue;
            textureState.binding = -1;
        }
    }

    public static void _deleteTextures(int[] nArray) {
        RenderSystem.assertOnRenderThreadOrInit();
        for (TextureState textureState : TEXTURES) {
            for (int n : nArray) {
                if (textureState.binding != n) continue;
                textureState.binding = -1;
            }
        }
        GL11.glDeleteTextures((int[])nArray);
    }

    public static void _bindTexture(int n) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (n != GlStateManager.TEXTURES[GlStateManager.activeTexture].binding) {
            GlStateManager.TEXTURES[GlStateManager.activeTexture].binding = n;
            GL11.glBindTexture((int)3553, (int)n);
        }
    }

    public static int _getTextureId(int n) {
        if (n >= 0 && n < 12 && GlStateManager.TEXTURES[n].enable) {
            return GlStateManager.TEXTURES[n].binding;
        }
        return 0;
    }

    public static int _getActiveTexture() {
        return activeTexture + 33984;
    }

    public static void _texImage2D(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, @Nullable IntBuffer intBuffer) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glTexImage2D((int)n, (int)n2, (int)n3, (int)n4, (int)n5, (int)n6, (int)n7, (int)n8, (IntBuffer)intBuffer);
    }

    public static void _texSubImage2D(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, long l) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glTexSubImage2D((int)n, (int)n2, (int)n3, (int)n4, (int)n5, (int)n6, (int)n7, (int)n8, (long)l);
    }

    public static void _getTexImage(int n, int n2, int n3, int n4, long l) {
        RenderSystem.assertOnRenderThread();
        GL11.glGetTexImage((int)n, (int)n2, (int)n3, (int)n4, (long)l);
    }

    public static void _viewport(int n, int n2, int n3, int n4) {
        RenderSystem.assertOnRenderThreadOrInit();
        Viewport.INSTANCE.x = n;
        Viewport.INSTANCE.y = n2;
        Viewport.INSTANCE.width = n3;
        Viewport.INSTANCE.height = n4;
        GL11.glViewport((int)n, (int)n2, (int)n3, (int)n4);
    }

    public static void _colorMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        RenderSystem.assertOnRenderThread();
        if (bl != GlStateManager.COLOR_MASK.red || bl2 != GlStateManager.COLOR_MASK.green || bl3 != GlStateManager.COLOR_MASK.blue || bl4 != GlStateManager.COLOR_MASK.alpha) {
            GlStateManager.COLOR_MASK.red = bl;
            GlStateManager.COLOR_MASK.green = bl2;
            GlStateManager.COLOR_MASK.blue = bl3;
            GlStateManager.COLOR_MASK.alpha = bl4;
            GL11.glColorMask((boolean)bl, (boolean)bl2, (boolean)bl3, (boolean)bl4);
        }
    }

    public static void _stencilFunc(int n, int n2, int n3) {
        RenderSystem.assertOnRenderThread();
        if (n != GlStateManager.STENCIL.func.func || n != GlStateManager.STENCIL.func.ref || n != GlStateManager.STENCIL.func.mask) {
            GlStateManager.STENCIL.func.func = n;
            GlStateManager.STENCIL.func.ref = n2;
            GlStateManager.STENCIL.func.mask = n3;
            GL11.glStencilFunc((int)n, (int)n2, (int)n3);
        }
    }

    public static void _stencilMask(int n) {
        RenderSystem.assertOnRenderThread();
        if (n != GlStateManager.STENCIL.mask) {
            GlStateManager.STENCIL.mask = n;
            GL11.glStencilMask((int)n);
        }
    }

    public static void _stencilOp(int n, int n2, int n3) {
        RenderSystem.assertOnRenderThread();
        if (n != GlStateManager.STENCIL.fail || n2 != GlStateManager.STENCIL.zfail || n3 != GlStateManager.STENCIL.zpass) {
            GlStateManager.STENCIL.fail = n;
            GlStateManager.STENCIL.zfail = n2;
            GlStateManager.STENCIL.zpass = n3;
            GL11.glStencilOp((int)n, (int)n2, (int)n3);
        }
    }

    public static void _clearDepth(double d) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glClearDepth((double)d);
    }

    public static void _clearColor(float f, float f2, float f3, float f4) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glClearColor((float)f, (float)f2, (float)f3, (float)f4);
    }

    public static void _clearStencil(int n) {
        RenderSystem.assertOnRenderThread();
        GL11.glClearStencil((int)n);
    }

    public static void _clear(int n, boolean bl) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glClear((int)n);
        if (bl) {
            GlStateManager._getError();
        }
    }

    public static void _glDrawPixels(int n, int n2, int n3, int n4, long l) {
        RenderSystem.assertOnRenderThread();
        GL11.glDrawPixels((int)n, (int)n2, (int)n3, (int)n4, (long)l);
    }

    public static void _vertexAttribPointer(int n, int n2, int n3, boolean bl, int n4, long l) {
        RenderSystem.assertOnRenderThread();
        GL20.glVertexAttribPointer((int)n, (int)n2, (int)n3, (boolean)bl, (int)n4, (long)l);
    }

    public static void _vertexAttribIPointer(int n, int n2, int n3, int n4, long l) {
        RenderSystem.assertOnRenderThread();
        GL30.glVertexAttribIPointer((int)n, (int)n2, (int)n3, (int)n4, (long)l);
    }

    public static void _enableVertexAttribArray(int n) {
        RenderSystem.assertOnRenderThread();
        GL20.glEnableVertexAttribArray((int)n);
    }

    public static void _disableVertexAttribArray(int n) {
        RenderSystem.assertOnRenderThread();
        GL20.glDisableVertexAttribArray((int)n);
    }

    public static void _drawElements(int n, int n2, int n3, long l) {
        RenderSystem.assertOnRenderThread();
        GL11.glDrawElements((int)n, (int)n2, (int)n3, (long)l);
    }

    public static void _pixelStore(int n, int n2) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL11.glPixelStorei((int)n, (int)n2);
    }

    public static void _readPixels(int n, int n2, int n3, int n4, int n5, int n6, ByteBuffer byteBuffer) {
        RenderSystem.assertOnRenderThread();
        GL11.glReadPixels((int)n, (int)n2, (int)n3, (int)n4, (int)n5, (int)n6, (ByteBuffer)byteBuffer);
    }

    public static void _readPixels(int n, int n2, int n3, int n4, int n5, int n6, long l) {
        RenderSystem.assertOnRenderThread();
        GL11.glReadPixels((int)n, (int)n2, (int)n3, (int)n4, (int)n5, (int)n6, (long)l);
    }

    public static int _getError() {
        RenderSystem.assertOnRenderThread();
        return GL11.glGetError();
    }

    public static String _getString(int n) {
        RenderSystem.assertOnRenderThread();
        return GL11.glGetString((int)n);
    }

    public static int _getInteger(int n) {
        RenderSystem.assertOnRenderThreadOrInit();
        return GL11.glGetInteger((int)n);
    }

    static {
        TEXTURES = (TextureState[])IntStream.range(0, 12).mapToObj(n -> new TextureState()).toArray(TextureState[]::new);
        COLOR_MASK = new ColorMask();
    }

    static class ScissorState {
        public final BooleanState mode = new BooleanState(3089);

        ScissorState() {
        }
    }

    static class BooleanState {
        private final int state;
        private boolean enabled;

        public BooleanState(int n) {
            this.state = n;
        }

        public void disable() {
            this.setEnabled(false);
        }

        public void enable() {
            this.setEnabled(true);
        }

        public void setEnabled(boolean bl) {
            RenderSystem.assertOnRenderThreadOrInit();
            if (bl != this.enabled) {
                this.enabled = bl;
                if (bl) {
                    GL11.glEnable((int)this.state);
                } else {
                    GL11.glDisable((int)this.state);
                }
            }
        }
    }

    static class DepthState {
        public final BooleanState mode = new BooleanState(2929);
        public boolean mask = true;
        public int func = 513;

        DepthState() {
        }
    }

    static class BlendState {
        public final BooleanState mode = new BooleanState(3042);
        public int srcRgb = 1;
        public int dstRgb = 0;
        public int srcAlpha = 1;
        public int dstAlpha = 0;

        BlendState() {
        }
    }

    static class CullState {
        public final BooleanState enable = new BooleanState(2884);
        public int mode = 1029;

        CullState() {
        }
    }

    static class PolygonOffsetState {
        public final BooleanState fill = new BooleanState(32823);
        public final BooleanState line = new BooleanState(10754);
        public float factor;
        public float units;

        PolygonOffsetState() {
        }
    }

    static class ColorLogicState {
        public final BooleanState enable = new BooleanState(3058);
        public int op = 5379;

        ColorLogicState() {
        }
    }

    static class TextureState {
        public boolean enable;
        public int binding;

        TextureState() {
        }
    }

    public static enum Viewport {
        INSTANCE;

        protected int x;
        protected int y;
        protected int width;
        protected int height;

        public static int x() {
            return Viewport.INSTANCE.x;
        }

        public static int y() {
            return Viewport.INSTANCE.y;
        }

        public static int width() {
            return Viewport.INSTANCE.width;
        }

        public static int height() {
            return Viewport.INSTANCE.height;
        }
    }

    static class ColorMask {
        public boolean red = true;
        public boolean green = true;
        public boolean blue = true;
        public boolean alpha = true;

        ColorMask() {
        }
    }

    static class StencilState {
        public final StencilFunc func = new StencilFunc();
        public int mask = -1;
        public int fail = 7680;
        public int zfail = 7680;
        public int zpass = 7680;

        StencilState() {
        }
    }

    static class StencilFunc {
        public int func = 519;
        public int ref;
        public int mask = -1;

        StencilFunc() {
        }
    }

    @DontObfuscate
    public static enum DestFactor {
        CONSTANT_ALPHA(32771),
        CONSTANT_COLOR(32769),
        DST_ALPHA(772),
        DST_COLOR(774),
        ONE(1),
        ONE_MINUS_CONSTANT_ALPHA(32772),
        ONE_MINUS_CONSTANT_COLOR(32770),
        ONE_MINUS_DST_ALPHA(773),
        ONE_MINUS_DST_COLOR(775),
        ONE_MINUS_SRC_ALPHA(771),
        ONE_MINUS_SRC_COLOR(769),
        SRC_ALPHA(770),
        SRC_COLOR(768),
        ZERO(0);

        public final int value;

        private DestFactor(int n2) {
            this.value = n2;
        }
    }

    @DontObfuscate
    public static enum SourceFactor {
        CONSTANT_ALPHA(32771),
        CONSTANT_COLOR(32769),
        DST_ALPHA(772),
        DST_COLOR(774),
        ONE(1),
        ONE_MINUS_CONSTANT_ALPHA(32772),
        ONE_MINUS_CONSTANT_COLOR(32770),
        ONE_MINUS_DST_ALPHA(773),
        ONE_MINUS_DST_COLOR(775),
        ONE_MINUS_SRC_ALPHA(771),
        ONE_MINUS_SRC_COLOR(769),
        SRC_ALPHA(770),
        SRC_ALPHA_SATURATE(776),
        SRC_COLOR(768),
        ZERO(0);

        public final int value;

        private SourceFactor(int n2) {
            this.value = n2;
        }
    }

    public static enum LogicOp {
        AND(5377),
        AND_INVERTED(5380),
        AND_REVERSE(5378),
        CLEAR(5376),
        COPY(5379),
        COPY_INVERTED(5388),
        EQUIV(5385),
        INVERT(5386),
        NAND(5390),
        NOOP(5381),
        NOR(5384),
        OR(5383),
        OR_INVERTED(5389),
        OR_REVERSE(5387),
        SET(5391),
        XOR(5382);

        public final int value;

        private LogicOp(int n2) {
            this.value = n2;
        }
    }
}

