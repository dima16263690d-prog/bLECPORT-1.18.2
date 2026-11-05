/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntConsumer
 *  javax.annotation.Nullable
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.glfw.GLFWErrorCallbackI
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.systems;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.slf4j.Logger;

@DontObfuscate
public class RenderSystem {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final ConcurrentLinkedQueue<RenderCall> recordingQueue = Queues.newConcurrentLinkedQueue();
    private static final Tesselator RENDER_THREAD_TESSELATOR = new Tesselator();
    private static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
    private static boolean isReplayingQueue;
    @Nullable
    private static Thread gameThread;
    @Nullable
    private static Thread renderThread;
    private static int MAX_SUPPORTED_TEXTURE_SIZE;
    private static boolean isInInit;
    private static double lastDrawTime;
    private static final AutoStorageIndexBuffer sharedSequential;
    private static final AutoStorageIndexBuffer sharedSequentialQuad;
    private static final AutoStorageIndexBuffer sharedSequentialLines;
    private static Matrix3f inverseViewRotationMatrix;
    private static Matrix4f projectionMatrix;
    private static Matrix4f savedProjectionMatrix;
    private static PoseStack modelViewStack;
    private static Matrix4f modelViewMatrix;
    private static Matrix4f textureMatrix;
    private static final int[] shaderTextures;
    private static final float[] shaderColor;
    private static float shaderFogStart;
    private static float shaderFogEnd;
    private static final float[] shaderFogColor;
    private static FogShape shaderFogShape;
    private static final Vector3f[] shaderLightDirections;
    private static float shaderGameTime;
    private static float shaderLineWidth;
    private static String apiDescription;
    @Nullable
    private static ShaderInstance shader;

    public static void initRenderThread() {
        if (renderThread != null || gameThread == Thread.currentThread()) {
            throw new IllegalStateException("Could not initialize render thread");
        }
        renderThread = Thread.currentThread();
    }

    public static boolean isOnRenderThread() {
        return Thread.currentThread() == renderThread;
    }

    public static boolean isOnRenderThreadOrInit() {
        return isInInit || RenderSystem.isOnRenderThread();
    }

    public static void initGameThread(boolean bl) {
        boolean bl2;
        boolean bl3 = bl2 = renderThread == Thread.currentThread();
        if (gameThread != null || renderThread == null || bl2 == bl) {
            throw new IllegalStateException("Could not initialize tick thread");
        }
        gameThread = Thread.currentThread();
    }

    public static boolean isOnGameThread() {
        return true;
    }

    public static void assertInInitPhase() {
        if (!RenderSystem.isInInitPhase()) {
            throw RenderSystem.constructThreadException();
        }
    }

    public static void assertOnGameThreadOrInit() {
        if (isInInit || RenderSystem.isOnGameThread()) {
            return;
        }
        throw RenderSystem.constructThreadException();
    }

    public static void assertOnRenderThreadOrInit() {
        if (isInInit || RenderSystem.isOnRenderThread()) {
            return;
        }
        throw RenderSystem.constructThreadException();
    }

    public static void assertOnRenderThread() {
        if (!RenderSystem.isOnRenderThread()) {
            throw RenderSystem.constructThreadException();
        }
    }

    public static void assertOnGameThread() {
        if (!RenderSystem.isOnGameThread()) {
            throw RenderSystem.constructThreadException();
        }
    }

    private static IllegalStateException constructThreadException() {
        return new IllegalStateException("Rendersystem called from wrong thread");
    }

    public static boolean isInInitPhase() {
        return true;
    }

    public static void recordRenderCall(RenderCall renderCall) {
        recordingQueue.add(renderCall);
    }

    public static void flipFrame(long l) {
        GLFW.glfwPollEvents();
        RenderSystem.replayQueue();
        Tesselator.getInstance().getBuilder().clear();
        GLFW.glfwSwapBuffers((long)l);
        GLFW.glfwPollEvents();
    }

    public static void replayQueue() {
        isReplayingQueue = true;
        while (!recordingQueue.isEmpty()) {
            RenderCall renderCall = recordingQueue.poll();
            renderCall.execute();
        }
        isReplayingQueue = false;
    }

    public static void limitDisplayFPS(int n) {
        double d = lastDrawTime + 1.0 / (double)n;
        double d2 = GLFW.glfwGetTime();
        while (d2 < d) {
            GLFW.glfwWaitEventsTimeout((double)(d - d2));
            d2 = GLFW.glfwGetTime();
        }
        lastDrawTime = d2;
    }

    public static void disableDepthTest() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._disableDepthTest();
    }

    public static void enableDepthTest() {
        RenderSystem.assertOnGameThreadOrInit();
        GlStateManager._enableDepthTest();
    }

    public static void enableScissor(int n, int n2, int n3, int n4) {
        RenderSystem.assertOnGameThreadOrInit();
        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox(n, n2, n3, n4);
    }

    public static void disableScissor() {
        RenderSystem.assertOnGameThreadOrInit();
        GlStateManager._disableScissorTest();
    }

    public static void depthFunc(int n) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._depthFunc(n);
    }

    public static void depthMask(boolean bl) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._depthMask(bl);
    }

    public static void enableBlend() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._enableBlend();
    }

    public static void disableBlend() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._disableBlend();
    }

    public static void blendFunc(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._blendFunc(sourceFactor.value, destFactor.value);
    }

    public static void blendFunc(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._blendFunc(n, n2);
    }

    public static void blendFuncSeparate(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor, GlStateManager.SourceFactor sourceFactor2, GlStateManager.DestFactor destFactor2) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._blendFuncSeparate(sourceFactor.value, destFactor.value, sourceFactor2.value, destFactor2.value);
    }

    public static void blendFuncSeparate(int n, int n2, int n3, int n4) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._blendFuncSeparate(n, n2, n3, n4);
    }

    public static void blendEquation(int n) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._blendEquation(n);
    }

    public static void enableCull() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._enableCull();
    }

    public static void disableCull() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._disableCull();
    }

    public static void polygonMode(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._polygonMode(n, n2);
    }

    public static void enablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._enablePolygonOffset();
    }

    public static void disablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._disablePolygonOffset();
    }

    public static void polygonOffset(float f, float f2) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._polygonOffset(f, f2);
    }

    public static void enableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._enableColorLogicOp();
    }

    public static void disableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._disableColorLogicOp();
    }

    public static void logicOp(GlStateManager.LogicOp logicOp) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._logicOp(logicOp.value);
    }

    public static void activeTexture(int n) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._activeTexture(n);
    }

    public static void enableTexture() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._enableTexture();
    }

    public static void disableTexture() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._disableTexture();
    }

    public static void texParameter(int n, int n2, int n3) {
        GlStateManager._texParameter(n, n2, n3);
    }

    public static void deleteTexture(int n) {
        RenderSystem.assertOnGameThreadOrInit();
        GlStateManager._deleteTexture(n);
    }

    public static void bindTextureForSetup(int n) {
        RenderSystem.bindTexture(n);
    }

    public static void bindTexture(int n) {
        GlStateManager._bindTexture(n);
    }

    public static void viewport(int n, int n2, int n3, int n4) {
        RenderSystem.assertOnGameThreadOrInit();
        GlStateManager._viewport(n, n2, n3, n4);
    }

    public static void colorMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._colorMask(bl, bl2, bl3, bl4);
    }

    public static void stencilFunc(int n, int n2, int n3) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._stencilFunc(n, n2, n3);
    }

    public static void stencilMask(int n) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._stencilMask(n);
    }

    public static void stencilOp(int n, int n2, int n3) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._stencilOp(n, n2, n3);
    }

    public static void clearDepth(double d) {
        RenderSystem.assertOnGameThreadOrInit();
        GlStateManager._clearDepth(d);
    }

    public static void clearColor(float f, float f2, float f3, float f4) {
        RenderSystem.assertOnGameThreadOrInit();
        GlStateManager._clearColor(f, f2, f3, f4);
    }

    public static void clearStencil(int n) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._clearStencil(n);
    }

    public static void clear(int n, boolean bl) {
        RenderSystem.assertOnGameThreadOrInit();
        GlStateManager._clear(n, bl);
    }

    public static void setShaderFogStart(float f) {
        RenderSystem.assertOnRenderThread();
        RenderSystem._setShaderFogStart(f);
    }

    private static void _setShaderFogStart(float f) {
        shaderFogStart = f;
    }

    public static float getShaderFogStart() {
        RenderSystem.assertOnRenderThread();
        return shaderFogStart;
    }

    public static void setShaderFogEnd(float f) {
        RenderSystem.assertOnRenderThread();
        RenderSystem._setShaderFogEnd(f);
    }

    private static void _setShaderFogEnd(float f) {
        shaderFogEnd = f;
    }

    public static float getShaderFogEnd() {
        RenderSystem.assertOnRenderThread();
        return shaderFogEnd;
    }

    public static void setShaderFogColor(float f, float f2, float f3, float f4) {
        RenderSystem.assertOnRenderThread();
        RenderSystem._setShaderFogColor(f, f2, f3, f4);
    }

    public static void setShaderFogColor(float f, float f2, float f3) {
        RenderSystem.setShaderFogColor(f, f2, f3, 1.0f);
    }

    private static void _setShaderFogColor(float f, float f2, float f3, float f4) {
        RenderSystem.shaderFogColor[0] = f;
        RenderSystem.shaderFogColor[1] = f2;
        RenderSystem.shaderFogColor[2] = f3;
        RenderSystem.shaderFogColor[3] = f4;
    }

    public static float[] getShaderFogColor() {
        RenderSystem.assertOnRenderThread();
        return shaderFogColor;
    }

    public static void setShaderFogShape(FogShape fogShape) {
        RenderSystem.assertOnRenderThread();
        RenderSystem._setShaderFogShape(fogShape);
    }

    private static void _setShaderFogShape(FogShape fogShape) {
        shaderFogShape = fogShape;
    }

    public static FogShape getShaderFogShape() {
        RenderSystem.assertOnRenderThread();
        return shaderFogShape;
    }

    public static void setShaderLights(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertOnRenderThread();
        RenderSystem._setShaderLights(vector3f, vector3f2);
    }

    public static void _setShaderLights(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.shaderLightDirections[0] = vector3f;
        RenderSystem.shaderLightDirections[1] = vector3f2;
    }

    public static void setupShaderLights(ShaderInstance shaderInstance) {
        RenderSystem.assertOnRenderThread();
        if (shaderInstance.LIGHT0_DIRECTION != null) {
            shaderInstance.LIGHT0_DIRECTION.set(shaderLightDirections[0]);
        }
        if (shaderInstance.LIGHT1_DIRECTION != null) {
            shaderInstance.LIGHT1_DIRECTION.set(shaderLightDirections[1]);
        }
    }

    public static void setShaderColor(float f, float f2, float f3, float f4) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> RenderSystem._setShaderColor(f, f2, f3, f4));
        } else {
            RenderSystem._setShaderColor(f, f2, f3, f4);
        }
    }

    private static void _setShaderColor(float f, float f2, float f3, float f4) {
        RenderSystem.shaderColor[0] = f;
        RenderSystem.shaderColor[1] = f2;
        RenderSystem.shaderColor[2] = f3;
        RenderSystem.shaderColor[3] = f4;
    }

    public static float[] getShaderColor() {
        RenderSystem.assertOnRenderThread();
        return shaderColor;
    }

    public static void drawElements(int n, int n2, int n3) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._drawElements(n, n2, n3, 0L);
    }

    public static void lineWidth(float f) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                shaderLineWidth = f;
            });
        } else {
            shaderLineWidth = f;
        }
    }

    public static float getShaderLineWidth() {
        RenderSystem.assertOnRenderThread();
        return shaderLineWidth;
    }

    public static void pixelStore(int n, int n2) {
        RenderSystem.assertOnGameThreadOrInit();
        GlStateManager._pixelStore(n, n2);
    }

    public static void readPixels(int n, int n2, int n3, int n4, int n5, int n6, ByteBuffer byteBuffer) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._readPixels(n, n2, n3, n4, n5, n6, byteBuffer);
    }

    public static void getString(int n, Consumer<String> consumer) {
        RenderSystem.assertOnRenderThread();
        consumer.accept(GlStateManager._getString(n));
    }

    public static String getBackendDescription() {
        RenderSystem.assertInInitPhase();
        return String.format("LWJGL version %s", GLX._getLWJGLVersion());
    }

    public static String getApiDescription() {
        return apiDescription;
    }

    public static LongSupplier initBackendSystem() {
        RenderSystem.assertInInitPhase();
        return GLX._initGlfw();
    }

    public static void initRenderer(int n, boolean bl) {
        RenderSystem.assertInInitPhase();
        GLX._init(n, bl);
        apiDescription = GLX.getOpenGLVersionString();
    }

    public static void setErrorCallback(GLFWErrorCallbackI gLFWErrorCallbackI) {
        RenderSystem.assertInInitPhase();
        GLX._setGlfwErrorCallback(gLFWErrorCallbackI);
    }

    public static void renderCrosshair(int n) {
        RenderSystem.assertOnRenderThread();
        GLX._renderCrosshair(n, true, true, true);
    }

    public static String getCapsString() {
        RenderSystem.assertOnRenderThread();
        return "Using framebuffer using OpenGL 3.2";
    }

    public static void setupDefaultState(int n, int n2, int n3, int n4) {
        RenderSystem.assertInInitPhase();
        GlStateManager._enableTexture();
        GlStateManager._clearDepth(1.0);
        GlStateManager._enableDepthTest();
        GlStateManager._depthFunc(515);
        projectionMatrix.setIdentity();
        savedProjectionMatrix.setIdentity();
        modelViewMatrix.setIdentity();
        textureMatrix.setIdentity();
        GlStateManager._viewport(n, n2, n3, n4);
    }

    public static int maxSupportedTextureSize() {
        if (MAX_SUPPORTED_TEXTURE_SIZE == -1) {
            RenderSystem.assertOnRenderThreadOrInit();
            int n = GlStateManager._getInteger(3379);
            for (int i = Math.max(32768, n); i >= 1024; i >>= 1) {
                GlStateManager._texImage2D(32868, 0, 6408, i, i, 0, 6408, 5121, null);
                int n2 = GlStateManager._getTexLevelParameter(32868, 0, 4096);
                if (n2 == 0) continue;
                MAX_SUPPORTED_TEXTURE_SIZE = i;
                return i;
            }
            MAX_SUPPORTED_TEXTURE_SIZE = Math.max(n, 1024);
            LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", (Object)MAX_SUPPORTED_TEXTURE_SIZE);
        }
        return MAX_SUPPORTED_TEXTURE_SIZE;
    }

    public static void glBindBuffer(int n, IntSupplier intSupplier) {
        GlStateManager._glBindBuffer(n, intSupplier.getAsInt());
    }

    public static void glBindVertexArray(Supplier<Integer> supplier) {
        GlStateManager._glBindVertexArray(supplier.get());
    }

    public static void glBufferData(int n, ByteBuffer byteBuffer, int n2) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._glBufferData(n, byteBuffer, n2);
    }

    public static void glDeleteBuffers(int n) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glDeleteBuffers(n);
    }

    public static void glDeleteVertexArrays(int n) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glDeleteVertexArrays(n);
    }

    public static void glUniform1i(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUniform1i(n, n2);
    }

    public static void glUniform1(int n, IntBuffer intBuffer) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUniform1(n, intBuffer);
    }

    public static void glUniform2(int n, IntBuffer intBuffer) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUniform2(n, intBuffer);
    }

    public static void glUniform3(int n, IntBuffer intBuffer) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUniform3(n, intBuffer);
    }

    public static void glUniform4(int n, IntBuffer intBuffer) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUniform4(n, intBuffer);
    }

    public static void glUniform1(int n, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUniform1(n, floatBuffer);
    }

    public static void glUniform2(int n, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUniform2(n, floatBuffer);
    }

    public static void glUniform3(int n, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUniform3(n, floatBuffer);
    }

    public static void glUniform4(int n, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUniform4(n, floatBuffer);
    }

    public static void glUniformMatrix2(int n, boolean bl, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUniformMatrix2(n, bl, floatBuffer);
    }

    public static void glUniformMatrix3(int n, boolean bl, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUniformMatrix3(n, bl, floatBuffer);
    }

    public static void glUniformMatrix4(int n, boolean bl, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUniformMatrix4(n, bl, floatBuffer);
    }

    public static void setupOverlayColor(IntSupplier intSupplier, int n) {
        RenderSystem.assertOnRenderThread();
        int n2 = intSupplier.getAsInt();
        RenderSystem.setShaderTexture(1, n2);
    }

    public static void teardownOverlayColor() {
        RenderSystem.assertOnRenderThread();
        RenderSystem.setShaderTexture(1, 0);
    }

    public static void setupLevelDiffuseLighting(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f) {
        RenderSystem.assertOnRenderThread();
        GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
    }

    public static void setupGuiFlatDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertOnRenderThread();
        GlStateManager.setupGuiFlatDiffuseLighting(vector3f, vector3f2);
    }

    public static void setupGui3DDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertOnRenderThread();
        GlStateManager.setupGui3DDiffuseLighting(vector3f, vector3f2);
    }

    public static void beginInitialization() {
        isInInit = true;
    }

    public static void finishInitialization() {
        isInInit = false;
        if (!recordingQueue.isEmpty()) {
            RenderSystem.replayQueue();
        }
        if (!recordingQueue.isEmpty()) {
            throw new IllegalStateException("Recorded to render queue during initialization");
        }
    }

    public static void glGenBuffers(Consumer<Integer> consumer) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> consumer.accept(GlStateManager._glGenBuffers()));
        } else {
            consumer.accept(GlStateManager._glGenBuffers());
        }
    }

    public static void glGenVertexArrays(Consumer<Integer> consumer) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> consumer.accept(GlStateManager._glGenVertexArrays()));
        } else {
            consumer.accept(GlStateManager._glGenVertexArrays());
        }
    }

    public static Tesselator renderThreadTesselator() {
        RenderSystem.assertOnRenderThread();
        return RENDER_THREAD_TESSELATOR;
    }

    public static void defaultBlendFunc() {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    @Deprecated
    public static void runAsFancy(Runnable runnable) {
        boolean bl = Minecraft.useShaderTransparency();
        if (!bl) {
            runnable.run();
            return;
        }
        Options options = Minecraft.getInstance().options;
        GraphicsStatus graphicsStatus = options.graphicsMode;
        options.graphicsMode = GraphicsStatus.FANCY;
        runnable.run();
        options.graphicsMode = graphicsStatus;
    }

    public static void setShader(Supplier<ShaderInstance> supplier) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                shader = (ShaderInstance)supplier.get();
            });
        } else {
            shader = supplier.get();
        }
    }

    @Nullable
    public static ShaderInstance getShader() {
        RenderSystem.assertOnRenderThread();
        return shader;
    }

    public static int getTextureId(int n) {
        return GlStateManager._getTextureId(n);
    }

    public static void setShaderTexture(int n, ResourceLocation resourceLocation) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> RenderSystem._setShaderTexture(n, resourceLocation));
        } else {
            RenderSystem._setShaderTexture(n, resourceLocation);
        }
    }

    public static void _setShaderTexture(int n, ResourceLocation resourceLocation) {
        if (n >= 0 && n < shaderTextures.length) {
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            AbstractTexture abstractTexture = textureManager.getTexture(resourceLocation);
            RenderSystem.shaderTextures[n] = abstractTexture.getId();
        }
    }

    public static void setShaderTexture(int n, int n2) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> RenderSystem._setShaderTexture(n, n2));
        } else {
            RenderSystem._setShaderTexture(n, n2);
        }
    }

    public static void _setShaderTexture(int n, int n2) {
        if (n >= 0 && n < shaderTextures.length) {
            RenderSystem.shaderTextures[n] = n2;
        }
    }

    public static int getShaderTexture(int n) {
        RenderSystem.assertOnRenderThread();
        if (n >= 0 && n < shaderTextures.length) {
            return shaderTextures[n];
        }
        return 0;
    }

    public static void setProjectionMatrix(Matrix4f matrix4f) {
        Matrix4f matrix4f2 = matrix4f.copy();
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                projectionMatrix = matrix4f2;
            });
        } else {
            projectionMatrix = matrix4f2;
        }
    }

    public static void setInverseViewRotationMatrix(Matrix3f matrix3f) {
        Matrix3f matrix3f2 = matrix3f.copy();
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                inverseViewRotationMatrix = matrix3f2;
            });
        } else {
            inverseViewRotationMatrix = matrix3f2;
        }
    }

    public static void setTextureMatrix(Matrix4f matrix4f) {
        Matrix4f matrix4f2 = matrix4f.copy();
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                textureMatrix = matrix4f2;
            });
        } else {
            textureMatrix = matrix4f2;
        }
    }

    public static void resetTextureMatrix() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> textureMatrix.setIdentity());
        } else {
            textureMatrix.setIdentity();
        }
    }

    public static void applyModelViewMatrix() {
        Matrix4f matrix4f = modelViewStack.last().pose().copy();
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                modelViewMatrix = matrix4f;
            });
        } else {
            modelViewMatrix = matrix4f;
        }
    }

    public static void backupProjectionMatrix() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> RenderSystem._backupProjectionMatrix());
        } else {
            RenderSystem._backupProjectionMatrix();
        }
    }

    private static void _backupProjectionMatrix() {
        savedProjectionMatrix = projectionMatrix;
    }

    public static void restoreProjectionMatrix() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> RenderSystem._restoreProjectionMatrix());
        } else {
            RenderSystem._restoreProjectionMatrix();
        }
    }

    private static void _restoreProjectionMatrix() {
        projectionMatrix = savedProjectionMatrix;
    }

    public static Matrix4f getProjectionMatrix() {
        RenderSystem.assertOnRenderThread();
        return projectionMatrix;
    }

    public static Matrix3f getInverseViewRotationMatrix() {
        RenderSystem.assertOnRenderThread();
        return inverseViewRotationMatrix;
    }

    public static Matrix4f getModelViewMatrix() {
        RenderSystem.assertOnRenderThread();
        return modelViewMatrix;
    }

    public static PoseStack getModelViewStack() {
        return modelViewStack;
    }

    public static Matrix4f getTextureMatrix() {
        RenderSystem.assertOnRenderThread();
        return textureMatrix;
    }

    public static AutoStorageIndexBuffer getSequentialBuffer(VertexFormat.Mode mode, int n) {
        RenderSystem.assertOnRenderThread();
        AutoStorageIndexBuffer autoStorageIndexBuffer = mode == VertexFormat.Mode.QUADS ? sharedSequentialQuad : (mode == VertexFormat.Mode.LINES ? sharedSequentialLines : sharedSequential);
        autoStorageIndexBuffer.ensureStorage(n);
        return autoStorageIndexBuffer;
    }

    public static void setShaderGameTime(long l, float f) {
        float f2 = ((float)(l % 24000L) + f) / 24000.0f;
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                shaderGameTime = f2;
            });
        } else {
            shaderGameTime = f2;
        }
    }

    public static float getShaderGameTime() {
        RenderSystem.assertOnRenderThread();
        return shaderGameTime;
    }

    private static /* synthetic */ void lambda$setupGui3DDiffuseLighting$58(Vector3f vector3f, Vector3f vector3f2) {
        GlStateManager.setupGui3DDiffuseLighting(vector3f, vector3f2);
    }

    private static /* synthetic */ void lambda$setupGuiFlatDiffuseLighting$57(Vector3f vector3f, Vector3f vector3f2) {
        GlStateManager.setupGuiFlatDiffuseLighting(vector3f, vector3f2);
    }

    private static /* synthetic */ void lambda$setupLevelDiffuseLighting$56(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f) {
        GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
    }

    private static /* synthetic */ void lambda$teardownOverlayColor$55() {
        RenderSystem.setShaderTexture(1, 0);
    }

    private static /* synthetic */ void lambda$setupOverlayColor$54(IntSupplier intSupplier) {
        int n = intSupplier.getAsInt();
        RenderSystem.setShaderTexture(1, n);
    }

    private static /* synthetic */ void lambda$glUniformMatrix4$53(int n, boolean bl, FloatBuffer floatBuffer) {
        GlStateManager._glUniformMatrix4(n, bl, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniformMatrix3$52(int n, boolean bl, FloatBuffer floatBuffer) {
        GlStateManager._glUniformMatrix3(n, bl, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniformMatrix2$51(int n, boolean bl, FloatBuffer floatBuffer) {
        GlStateManager._glUniformMatrix2(n, bl, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform4$50(int n, FloatBuffer floatBuffer) {
        GlStateManager._glUniform4(n, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform3$49(int n, FloatBuffer floatBuffer) {
        GlStateManager._glUniform3(n, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform2$48(int n, FloatBuffer floatBuffer) {
        GlStateManager._glUniform2(n, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform1$47(int n, FloatBuffer floatBuffer) {
        GlStateManager._glUniform1(n, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform4$46(int n, IntBuffer intBuffer) {
        GlStateManager._glUniform4(n, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform3$45(int n, IntBuffer intBuffer) {
        GlStateManager._glUniform3(n, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform2$44(int n, IntBuffer intBuffer) {
        GlStateManager._glUniform2(n, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform1$43(int n, IntBuffer intBuffer) {
        GlStateManager._glUniform1(n, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform1i$42(int n, int n2) {
        GlStateManager._glUniform1i(n, n2);
    }

    private static /* synthetic */ void lambda$glDeleteVertexArrays$41(int n) {
        GlStateManager._glDeleteVertexArrays(n);
    }

    private static /* synthetic */ void lambda$glDeleteBuffers$40(int n) {
        GlStateManager._glDeleteBuffers(n);
    }

    private static /* synthetic */ void lambda$glBindVertexArray$39(Supplier supplier) {
        GlStateManager._glBindVertexArray((Integer)supplier.get());
    }

    private static /* synthetic */ void lambda$glBindBuffer$38(int n, IntSupplier intSupplier) {
        GlStateManager._glBindBuffer(n, intSupplier.getAsInt());
    }

    private static /* synthetic */ void lambda$renderCrosshair$37(int n) {
        GLX._renderCrosshair(n, true, true, true);
    }

    private static /* synthetic */ void lambda$getString$36(int n, Consumer consumer) {
        String string = GlStateManager._getString(n);
        consumer.accept(string);
    }

    private static /* synthetic */ void lambda$readPixels$35(int n, int n2, int n3, int n4, int n5, int n6, ByteBuffer byteBuffer) {
        GlStateManager._readPixels(n, n2, n3, n4, n5, n6, byteBuffer);
    }

    private static /* synthetic */ void lambda$pixelStore$34(int n, int n2) {
        GlStateManager._pixelStore(n, n2);
    }

    private static /* synthetic */ void lambda$drawElements$32(int n, int n2, int n3) {
        GlStateManager._drawElements(n, n2, n3, 0L);
    }

    private static /* synthetic */ void lambda$setShaderLights$30(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem._setShaderLights(vector3f, vector3f2);
    }

    private static /* synthetic */ void lambda$setShaderFogShape$29(FogShape fogShape) {
        RenderSystem._setShaderFogShape(fogShape);
    }

    private static /* synthetic */ void lambda$setShaderFogColor$28(float f, float f2, float f3, float f4) {
        RenderSystem._setShaderFogColor(f, f2, f3, f4);
    }

    private static /* synthetic */ void lambda$setShaderFogEnd$27(float f) {
        RenderSystem._setShaderFogEnd(f);
    }

    private static /* synthetic */ void lambda$setShaderFogStart$26(float f) {
        RenderSystem._setShaderFogStart(f);
    }

    private static /* synthetic */ void lambda$clear$25(int n, boolean bl) {
        GlStateManager._clear(n, bl);
    }

    private static /* synthetic */ void lambda$clearStencil$24(int n) {
        GlStateManager._clearStencil(n);
    }

    private static /* synthetic */ void lambda$clearColor$23(float f, float f2, float f3, float f4) {
        GlStateManager._clearColor(f, f2, f3, f4);
    }

    private static /* synthetic */ void lambda$clearDepth$22(double d) {
        GlStateManager._clearDepth(d);
    }

    private static /* synthetic */ void lambda$stencilOp$21(int n, int n2, int n3) {
        GlStateManager._stencilOp(n, n2, n3);
    }

    private static /* synthetic */ void lambda$stencilMask$20(int n) {
        GlStateManager._stencilMask(n);
    }

    private static /* synthetic */ void lambda$stencilFunc$19(int n, int n2, int n3) {
        GlStateManager._stencilFunc(n, n2, n3);
    }

    private static /* synthetic */ void lambda$colorMask$18(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        GlStateManager._colorMask(bl, bl2, bl3, bl4);
    }

    private static /* synthetic */ void lambda$viewport$17(int n, int n2, int n3, int n4) {
        GlStateManager._viewport(n, n2, n3, n4);
    }

    private static /* synthetic */ void lambda$bindTexture$16(int n) {
        GlStateManager._bindTexture(n);
    }

    private static /* synthetic */ void lambda$deleteTexture$15(int n) {
        GlStateManager._deleteTexture(n);
    }

    private static /* synthetic */ void lambda$texParameter$14(int n, int n2, int n3) {
        GlStateManager._texParameter(n, n2, n3);
    }

    private static /* synthetic */ void lambda$activeTexture$13(int n) {
        GlStateManager._activeTexture(n);
    }

    private static /* synthetic */ void lambda$logicOp$12(GlStateManager.LogicOp logicOp) {
        GlStateManager._logicOp(logicOp.value);
    }

    private static /* synthetic */ void lambda$polygonOffset$11(float f, float f2) {
        GlStateManager._polygonOffset(f, f2);
    }

    private static /* synthetic */ void lambda$polygonMode$10(int n, int n2) {
        GlStateManager._polygonMode(n, n2);
    }

    private static /* synthetic */ void lambda$blendEquation$9(int n) {
        GlStateManager._blendEquation(n);
    }

    private static /* synthetic */ void lambda$blendFuncSeparate$8(int n, int n2, int n3, int n4) {
        GlStateManager._blendFuncSeparate(n, n2, n3, n4);
    }

    private static /* synthetic */ void lambda$blendFuncSeparate$7(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor, GlStateManager.SourceFactor sourceFactor2, GlStateManager.DestFactor destFactor2) {
        GlStateManager._blendFuncSeparate(sourceFactor.value, destFactor.value, sourceFactor2.value, destFactor2.value);
    }

    private static /* synthetic */ void lambda$blendFunc$6(int n, int n2) {
        GlStateManager._blendFunc(n, n2);
    }

    private static /* synthetic */ void lambda$blendFunc$5(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor) {
        GlStateManager._blendFunc(sourceFactor.value, destFactor.value);
    }

    private static /* synthetic */ void lambda$depthMask$4(boolean bl) {
        GlStateManager._depthMask(bl);
    }

    private static /* synthetic */ void lambda$depthFunc$3(int n) {
        GlStateManager._depthFunc(n);
    }

    private static /* synthetic */ void lambda$enableScissor$2(int n, int n2, int n3, int n4) {
        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox(n, n2, n3, n4);
    }

    static {
        MAX_SUPPORTED_TEXTURE_SIZE = -1;
        lastDrawTime = Double.MIN_VALUE;
        sharedSequential = new AutoStorageIndexBuffer(1, 1, java.util.function.IntConsumer::accept);
        sharedSequentialQuad = new AutoStorageIndexBuffer(4, 6, (intConsumer, n) -> {
            intConsumer.accept(n + 0);
            intConsumer.accept(n + 1);
            intConsumer.accept(n + 2);
            intConsumer.accept(n + 2);
            intConsumer.accept(n + 3);
            intConsumer.accept(n + 0);
        });
        sharedSequentialLines = new AutoStorageIndexBuffer(4, 6, (intConsumer, n) -> {
            intConsumer.accept(n + 0);
            intConsumer.accept(n + 1);
            intConsumer.accept(n + 2);
            intConsumer.accept(n + 3);
            intConsumer.accept(n + 2);
            intConsumer.accept(n + 1);
        });
        inverseViewRotationMatrix = new Matrix3f();
        projectionMatrix = new Matrix4f();
        savedProjectionMatrix = new Matrix4f();
        modelViewStack = new PoseStack();
        modelViewMatrix = new Matrix4f();
        textureMatrix = new Matrix4f();
        shaderTextures = new int[12];
        shaderColor = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        shaderFogEnd = 1.0f;
        shaderFogColor = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        shaderFogShape = FogShape.SPHERE;
        shaderLightDirections = new Vector3f[2];
        shaderLineWidth = 1.0f;
        apiDescription = "Unknown";
        projectionMatrix.setIdentity();
        savedProjectionMatrix.setIdentity();
        modelViewMatrix.setIdentity();
        textureMatrix.setIdentity();
    }

    public static final class AutoStorageIndexBuffer {
        private final int vertexStride;
        private final int indexStride;
        private final IndexGenerator generator;
        private int name;
        private VertexFormat.IndexType type = VertexFormat.IndexType.BYTE;
        private int indexCount;

        AutoStorageIndexBuffer(int n, int n2, IndexGenerator indexGenerator) {
            this.vertexStride = n;
            this.indexStride = n2;
            this.generator = indexGenerator;
        }

        void ensureStorage(int n) {
            if (n <= this.indexCount) {
                return;
            }
            n = Mth.roundToward(n * 2, this.indexStride);
            LOGGER.debug("Growing IndexBuffer: Old limit {}, new limit {}.", (Object)this.indexCount, (Object)n);
            if (this.name == 0) {
                this.name = GlStateManager._glGenBuffers();
            }
            VertexFormat.IndexType indexType = VertexFormat.IndexType.least(n);
            int n2 = Mth.roundToward(n * indexType.bytes, 4);
            GlStateManager._glBindBuffer(34963, this.name);
            GlStateManager._glBufferData(34963, n2, 35048);
            ByteBuffer byteBuffer = GlStateManager._glMapBuffer(34963, 35001);
            if (byteBuffer == null) {
                throw new RuntimeException("Failed to map GL buffer");
            }
            this.type = indexType;
            IntConsumer intConsumer = this.intConsumer(byteBuffer);
            for (int i = 0; i < n; i += this.indexStride) {
                this.generator.accept(intConsumer, i * this.vertexStride / this.indexStride);
            }
            GlStateManager._glUnmapBuffer(34963);
            GlStateManager._glBindBuffer(34963, 0);
            this.indexCount = n;
            BufferUploader.invalidateElementArrayBufferBinding();
        }

        private IntConsumer intConsumer(ByteBuffer byteBuffer) {
            switch (this.type) {
                case BYTE: {
                    return n -> byteBuffer.put((byte)n);
                }
                case SHORT: {
                    return n -> byteBuffer.putShort((short)n);
                }
            }
            return byteBuffer::putInt;
        }

        public int name() {
            return this.name;
        }

        public VertexFormat.IndexType type() {
            return this.type;
        }

        static interface IndexGenerator {
            public void accept(IntConsumer var1, int var2);
        }
    }
}

