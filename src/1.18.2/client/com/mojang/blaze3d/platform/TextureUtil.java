/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.system.MemoryUtil
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@DontObfuscate
public class TextureUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MIN_MIPMAP_LEVEL = 0;
    private static final int DEFAULT_IMAGE_BUFFER_SIZE = 8192;

    public static int generateTextureId() {
        RenderSystem.assertOnRenderThreadOrInit();
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            int[] nArray = new int[ThreadLocalRandom.current().nextInt(15) + 1];
            GlStateManager._genTextures(nArray);
            int n = GlStateManager._genTexture();
            GlStateManager._deleteTextures(nArray);
            return n;
        }
        return GlStateManager._genTexture();
    }

    public static void releaseTextureId(int n) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._deleteTexture(n);
    }

    public static void prepareImage(int n, int n2, int n3) {
        TextureUtil.prepareImage(NativeImage.InternalGlFormat.RGBA, n, 0, n2, n3);
    }

    public static void prepareImage(NativeImage.InternalGlFormat internalGlFormat, int n, int n2, int n3) {
        TextureUtil.prepareImage(internalGlFormat, n, 0, n2, n3);
    }

    public static void prepareImage(int n, int n2, int n3, int n4) {
        TextureUtil.prepareImage(NativeImage.InternalGlFormat.RGBA, n, n2, n3, n4);
    }

    public static void prepareImage(NativeImage.InternalGlFormat internalGlFormat, int n, int n2, int n3, int n4) {
        RenderSystem.assertOnRenderThreadOrInit();
        TextureUtil.bind(n);
        if (n2 >= 0) {
            GlStateManager._texParameter(3553, 33085, n2);
            GlStateManager._texParameter(3553, 33082, 0);
            GlStateManager._texParameter(3553, 33083, n2);
            GlStateManager._texParameter(3553, 34049, 0.0f);
        }
        for (int i = 0; i <= n2; ++i) {
            GlStateManager._texImage2D(3553, i, internalGlFormat.glFormat(), n3 >> i, n4 >> i, 0, 6408, 5121, null);
        }
    }

    private static void bind(int n) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._bindTexture(n);
    }

    public static ByteBuffer readResource(InputStream inputStream) throws IOException {
        ByteBuffer byteBuffer;
        if (inputStream instanceof FileInputStream) {
            FileInputStream fileInputStream = (FileInputStream)inputStream;
            FileChannel fileChannel = fileInputStream.getChannel();
            byteBuffer = MemoryUtil.memAlloc((int)((int)fileChannel.size() + 1));
            while (fileChannel.read(byteBuffer) != -1) {
            }
        } else {
            byteBuffer = MemoryUtil.memAlloc((int)8192);
            ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
            while (readableByteChannel.read(byteBuffer) != -1) {
                if (byteBuffer.remaining() != 0) continue;
                byteBuffer = MemoryUtil.memRealloc((ByteBuffer)byteBuffer, (int)(byteBuffer.capacity() * 2));
            }
        }
        return byteBuffer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Nullable
    public static String readResourceAsString(InputStream inputStream) {
        RenderSystem.assertOnRenderThread();
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = TextureUtil.readResource(inputStream);
            int n = byteBuffer.position();
            byteBuffer.rewind();
            String string = MemoryUtil.memASCII((ByteBuffer)byteBuffer, (int)n);
            return string;
        }
        catch (IOException iOException) {
        }
        finally {
            if (byteBuffer != null) {
                MemoryUtil.memFree((Buffer)byteBuffer);
            }
        }
        return null;
    }

    public static void writeAsPNG(String string, int n, int n2, int n3, int n4) {
        RenderSystem.assertOnRenderThread();
        TextureUtil.bind(n);
        for (int i = 0; i <= n2; ++i) {
            String string2 = string + "_" + i + ".png";
            int n5 = n3 >> i;
            int n6 = n4 >> i;
            try (NativeImage nativeImage = new NativeImage(n5, n6, false);){
                nativeImage.downloadTexture(i, false);
                nativeImage.writeToFile(string2);
                LOGGER.debug("Exported png to: {}", (Object)new File(string2).getAbsolutePath());
                continue;
            }
            catch (IOException iOException) {
                LOGGER.debug("Unable to write: ", (Throwable)iOException);
            }
        }
    }

    public static void initTexture(IntBuffer intBuffer, int n, int n2) {
        RenderSystem.assertOnRenderThread();
        GL11.glPixelStorei((int)3312, (int)0);
        GL11.glPixelStorei((int)3313, (int)0);
        GL11.glPixelStorei((int)3314, (int)0);
        GL11.glPixelStorei((int)3315, (int)0);
        GL11.glPixelStorei((int)3316, (int)0);
        GL11.glPixelStorei((int)3317, (int)4);
        GL11.glTexImage2D((int)3553, (int)0, (int)6408, (int)n, (int)n2, (int)0, (int)32993, (int)33639, (IntBuffer)intBuffer);
        GL11.glTexParameteri((int)3553, (int)10240, (int)9728);
        GL11.glTexParameteri((int)3553, (int)10241, (int)9729);
    }
}

