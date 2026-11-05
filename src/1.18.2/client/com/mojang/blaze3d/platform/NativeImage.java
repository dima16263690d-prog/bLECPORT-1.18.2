/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Charsets
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.apache.commons.io.IOUtils
 *  org.lwjgl.stb.STBIWriteCallback
 *  org.lwjgl.stb.STBImage
 *  org.lwjgl.stb.STBImageResize
 *  org.lwjgl.stb.STBImageWrite
 *  org.lwjgl.stb.STBTTFontinfo
 *  org.lwjgl.stb.STBTruetype
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.google.common.base.Charsets;
import com.mojang.blaze3d.platform.DebugMemoryUntracker;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public final class NativeImage
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int OFFSET_A = 24;
    private static final int OFFSET_B = 16;
    private static final int OFFSET_G = 8;
    private static final int OFFSET_R = 0;
    private static final Set<StandardOpenOption> OPEN_OPTIONS = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    private final Format format;
    private final int width;
    private final int height;
    private final boolean useStbFree;
    private long pixels;
    private final long size;

    public NativeImage(int n, int n2, boolean bl) {
        this(Format.RGBA, n, n2, bl);
    }

    public NativeImage(Format format, int n, int n2, boolean bl) {
        if (n <= 0 || n2 <= 0) {
            throw new IllegalArgumentException("Invalid texture size: " + n + "x" + n2);
        }
        this.format = format;
        this.width = n;
        this.height = n2;
        this.size = (long)n * (long)n2 * (long)format.components();
        this.useStbFree = false;
        this.pixels = bl ? MemoryUtil.nmemCalloc((long)1L, (long)this.size) : MemoryUtil.nmemAlloc((long)this.size);
    }

    private NativeImage(Format format, int n, int n2, boolean bl, long l) {
        if (n <= 0 || n2 <= 0) {
            throw new IllegalArgumentException("Invalid texture size: " + n + "x" + n2);
        }
        this.format = format;
        this.width = n;
        this.height = n2;
        this.useStbFree = bl;
        this.pixels = l;
        this.size = (long)n * (long)n2 * (long)format.components();
    }

    public String toString() {
        return "NativeImage[" + this.format + " " + this.width + "x" + this.height + "@" + this.pixels + (this.useStbFree ? "S" : "N") + "]";
    }

    private boolean isOutsideBounds(int n, int n2) {
        return n < 0 || n >= this.width || n2 < 0 || n2 >= this.height;
    }

    public static NativeImage read(InputStream inputStream) throws IOException {
        return NativeImage.read(Format.RGBA, inputStream);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeImage read(@Nullable Format format, InputStream inputStream) throws IOException {
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = TextureUtil.readResource(inputStream);
            byteBuffer.rewind();
            NativeImage nativeImage = NativeImage.read(format, byteBuffer);
            return nativeImage;
        }
        finally {
            MemoryUtil.memFree((Buffer)byteBuffer);
            IOUtils.closeQuietly((InputStream)inputStream);
        }
    }

    public static NativeImage read(ByteBuffer byteBuffer) throws IOException {
        return NativeImage.read(Format.RGBA, byteBuffer);
    }

    public static NativeImage read(@Nullable Format format, ByteBuffer byteBuffer) throws IOException {
        if (format != null && !format.supportedByStb()) {
            throw new UnsupportedOperationException("Don't know how to read format " + format);
        }
        if (MemoryUtil.memAddress((ByteBuffer)byteBuffer) == 0L) {
            throw new IllegalArgumentException("Invalid buffer");
        }
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            IntBuffer intBuffer2 = memoryStack.mallocInt(1);
            IntBuffer intBuffer3 = memoryStack.mallocInt(1);
            ByteBuffer byteBuffer2 = STBImage.stbi_load_from_memory((ByteBuffer)byteBuffer, (IntBuffer)intBuffer, (IntBuffer)intBuffer2, (IntBuffer)intBuffer3, (int)(format == null ? 0 : format.components));
            if (byteBuffer2 == null) {
                throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
            }
            NativeImage nativeImage = new NativeImage(format == null ? Format.getStbFormat(intBuffer3.get(0)) : format, intBuffer.get(0), intBuffer2.get(0), true, MemoryUtil.memAddress((ByteBuffer)byteBuffer2));
            return nativeImage;
        }
    }

    private static void setFilter(boolean bl, boolean bl2) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (bl) {
            GlStateManager._texParameter(3553, 10241, bl2 ? 9987 : 9729);
            GlStateManager._texParameter(3553, 10240, 9729);
        } else {
            GlStateManager._texParameter(3553, 10241, bl2 ? 9986 : 9728);
            GlStateManager._texParameter(3553, 10240, 9728);
        }
    }

    private void checkAllocated() {
        if (this.pixels == 0L) {
            throw new IllegalStateException("Image is not allocated.");
        }
    }

    @Override
    public void close() {
        if (this.pixels != 0L) {
            if (this.useStbFree) {
                STBImage.nstbi_image_free((long)this.pixels);
            } else {
                MemoryUtil.nmemFree((long)this.pixels);
            }
        }
        this.pixels = 0L;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Format format() {
        return this.format;
    }

    public int getPixelRGBA(int n, int n2) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format("getPixelRGBA only works on RGBA images; have %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(n, n2)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", n, n2, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)n + (long)n2 * (long)this.width) * 4L;
        return MemoryUtil.memGetInt((long)(this.pixels + l));
    }

    public void setPixelRGBA(int n, int n2, int n3) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format("getPixelRGBA only works on RGBA images; have %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(n, n2)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", n, n2, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)n + (long)n2 * (long)this.width) * 4L;
        MemoryUtil.memPutInt((long)(this.pixels + l), (int)n3);
    }

    public void setPixelLuminance(int n, int n2, byte by) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasLuminance()) {
            throw new IllegalArgumentException(String.format("setPixelLuminance only works on image with luminance; have %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(n, n2)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", n, n2, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)n + (long)n2 * (long)this.width) * (long)this.format.components() + (long)(this.format.luminanceOffset() / 8);
        MemoryUtil.memPutByte((long)(this.pixels + l), (byte)by);
    }

    public byte getRedOrLuminance(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasLuminanceOrRed()) {
            throw new IllegalArgumentException(String.format("no red or luminance in %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(n, n2)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", n, n2, this.width, this.height));
        }
        int n3 = (n + n2 * this.width) * this.format.components() + this.format.luminanceOrRedOffset() / 8;
        return MemoryUtil.memGetByte((long)(this.pixels + (long)n3));
    }

    public byte getGreenOrLuminance(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasLuminanceOrGreen()) {
            throw new IllegalArgumentException(String.format("no green or luminance in %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(n, n2)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", n, n2, this.width, this.height));
        }
        int n3 = (n + n2 * this.width) * this.format.components() + this.format.luminanceOrGreenOffset() / 8;
        return MemoryUtil.memGetByte((long)(this.pixels + (long)n3));
    }

    public byte getBlueOrLuminance(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasLuminanceOrBlue()) {
            throw new IllegalArgumentException(String.format("no blue or luminance in %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(n, n2)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", n, n2, this.width, this.height));
        }
        int n3 = (n + n2 * this.width) * this.format.components() + this.format.luminanceOrBlueOffset() / 8;
        return MemoryUtil.memGetByte((long)(this.pixels + (long)n3));
    }

    public byte getLuminanceOrAlpha(int n, int n2) {
        if (!this.format.hasLuminanceOrAlpha()) {
            throw new IllegalArgumentException(String.format("no luminance or alpha in %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(n, n2)) {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", n, n2, this.width, this.height));
        }
        int n3 = (n + n2 * this.width) * this.format.components() + this.format.luminanceOrAlphaOffset() / 8;
        return MemoryUtil.memGetByte((long)(this.pixels + (long)n3));
    }

    public void blendPixel(int n, int n2, int n3) {
        if (this.format != Format.RGBA) {
            throw new UnsupportedOperationException("Can only call blendPixel with RGBA format");
        }
        int n4 = this.getPixelRGBA(n, n2);
        float f = (float)NativeImage.getA(n3) / 255.0f;
        float f2 = (float)NativeImage.getB(n3) / 255.0f;
        float f3 = (float)NativeImage.getG(n3) / 255.0f;
        float f4 = (float)NativeImage.getR(n3) / 255.0f;
        float f5 = (float)NativeImage.getA(n4) / 255.0f;
        float f6 = (float)NativeImage.getB(n4) / 255.0f;
        float f7 = (float)NativeImage.getG(n4) / 255.0f;
        float f8 = (float)NativeImage.getR(n4) / 255.0f;
        float f9 = f;
        float f10 = 1.0f - f;
        float f11 = f * f9 + f5 * f10;
        float f12 = f2 * f9 + f6 * f10;
        float f13 = f3 * f9 + f7 * f10;
        float f14 = f4 * f9 + f8 * f10;
        if (f11 > 1.0f) {
            f11 = 1.0f;
        }
        if (f12 > 1.0f) {
            f12 = 1.0f;
        }
        if (f13 > 1.0f) {
            f13 = 1.0f;
        }
        if (f14 > 1.0f) {
            f14 = 1.0f;
        }
        int n5 = (int)(f11 * 255.0f);
        int n6 = (int)(f12 * 255.0f);
        int n7 = (int)(f13 * 255.0f);
        int n8 = (int)(f14 * 255.0f);
        this.setPixelRGBA(n, n2, NativeImage.combine(n5, n6, n7, n8));
    }

    @Deprecated
    public int[] makePixelArray() {
        if (this.format != Format.RGBA) {
            throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
        }
        this.checkAllocated();
        int[] nArray = new int[this.getWidth() * this.getHeight()];
        for (int i = 0; i < this.getHeight(); ++i) {
            for (int j = 0; j < this.getWidth(); ++j) {
                int n;
                int n2 = this.getPixelRGBA(j, i);
                int n3 = NativeImage.getA(n2);
                int n4 = NativeImage.getB(n2);
                int n5 = NativeImage.getG(n2);
                int n6 = NativeImage.getR(n2);
                nArray[j + i * this.getWidth()] = n = n3 << 24 | n6 << 16 | n5 << 8 | n4;
            }
        }
        return nArray;
    }

    public void upload(int n, int n2, int n3, boolean bl) {
        this.upload(n, n2, n3, 0, 0, this.width, this.height, false, bl);
    }

    public void upload(int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, boolean bl2) {
        this.upload(n, n2, n3, n4, n5, n6, n7, false, false, bl, bl2);
    }

    public void upload(int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> this._upload(n, n2, n3, n4, n5, n6, n7, bl, bl2, bl3, bl4));
        } else {
            this._upload(n, n2, n3, n4, n5, n6, n7, bl, bl2, bl3, bl4);
        }
    }

    private void _upload(int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.checkAllocated();
        NativeImage.setFilter(bl, bl3);
        if (n6 == this.getWidth()) {
            GlStateManager._pixelStore(3314, 0);
        } else {
            GlStateManager._pixelStore(3314, this.getWidth());
        }
        GlStateManager._pixelStore(3316, n4);
        GlStateManager._pixelStore(3315, n5);
        this.format.setUnpackPixelStoreState();
        GlStateManager._texSubImage2D(3553, n, n2, n3, n6, n7, this.format.glFormat(), 5121, this.pixels);
        if (bl2) {
            GlStateManager._texParameter(3553, 10242, 33071);
            GlStateManager._texParameter(3553, 10243, 33071);
        }
        if (bl4) {
            this.close();
        }
    }

    public void downloadTexture(int n, boolean bl) {
        RenderSystem.assertOnRenderThread();
        this.checkAllocated();
        this.format.setPackPixelStoreState();
        GlStateManager._getTexImage(3553, n, this.format.glFormat(), 5121, this.pixels);
        if (bl && this.format.hasAlpha()) {
            for (int i = 0; i < this.getHeight(); ++i) {
                for (int j = 0; j < this.getWidth(); ++j) {
                    this.setPixelRGBA(j, i, this.getPixelRGBA(j, i) | 255 << this.format.alphaOffset());
                }
            }
        }
    }

    public void downloadDepthBuffer(float f) {
        RenderSystem.assertOnRenderThread();
        if (this.format.components() != 1) {
            throw new IllegalStateException("Depth buffer must be stored in NativeImage with 1 component.");
        }
        this.checkAllocated();
        this.format.setPackPixelStoreState();
        GlStateManager._readPixels(0, 0, this.width, this.height, 6402, 5121, this.pixels);
    }

    public void drawPixels() {
        RenderSystem.assertOnRenderThread();
        this.format.setUnpackPixelStoreState();
        GlStateManager._glDrawPixels(this.width, this.height, this.format.glFormat(), 5121, this.pixels);
    }

    public void writeToFile(String string) throws IOException {
        this.writeToFile(FileSystems.getDefault().getPath(string, new String[0]));
    }

    public void writeToFile(File file) throws IOException {
        this.writeToFile(file.toPath());
    }

    public void copyFromFont(STBTTFontinfo sTBTTFontinfo, int n, int n2, int n3, float f, float f2, float f3, float f4, int n4, int n5) {
        if (n4 < 0 || n4 + n2 > this.getWidth() || n5 < 0 || n5 + n3 > this.getHeight()) {
            throw new IllegalArgumentException(String.format("Out of bounds: start: (%s, %s) (size: %sx%s); size: %sx%s", n4, n5, n2, n3, this.getWidth(), this.getHeight()));
        }
        if (this.format.components() != 1) {
            throw new IllegalArgumentException("Can only write fonts into 1-component images.");
        }
        STBTruetype.nstbtt_MakeGlyphBitmapSubpixel((long)sTBTTFontinfo.address(), (long)(this.pixels + (long)n4 + (long)(n5 * this.getWidth())), (int)n2, (int)n3, (int)this.getWidth(), (float)f, (float)f2, (float)f3, (float)f4, (int)n);
    }

    public void writeToFile(Path path) throws IOException {
        if (!this.format.supportedByStb()) {
            throw new UnsupportedOperationException("Don't know how to write format " + this.format);
        }
        this.checkAllocated();
        try (SeekableByteChannel seekableByteChannel = Files.newByteChannel(path, OPEN_OPTIONS, new FileAttribute[0]);){
            if (!this.writeToChannel(seekableByteChannel)) {
                throw new IOException("Could not write image to the PNG file \"" + path.toAbsolutePath() + "\": " + STBImage.stbi_failure_reason());
            }
        }
    }

    public byte[] asByteArray() throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
            byte[] byArray;
            block12: {
                WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
                try {
                    if (!this.writeToChannel(writableByteChannel)) {
                        throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
                    }
                    byArray = byteArrayOutputStream.toByteArray();
                    if (writableByteChannel == null) break block12;
                }
                catch (Throwable throwable) {
                    if (writableByteChannel != null) {
                        try {
                            writableByteChannel.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                writableByteChannel.close();
            }
            return byArray;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean writeToChannel(WritableByteChannel writableByteChannel) throws IOException {
        WriteCallback writeCallback = new WriteCallback(writableByteChannel);
        try {
            int n = Math.min(this.getHeight(), Integer.MAX_VALUE / this.getWidth() / this.format.components());
            if (n < this.getHeight()) {
                LOGGER.warn("Dropping image height from {} to {} to fit the size into 32-bit signed int", (Object)this.getHeight(), (Object)n);
            }
            if (STBImageWrite.nstbi_write_png_to_func((long)writeCallback.address(), (long)0L, (int)this.getWidth(), (int)n, (int)this.format.components(), (long)this.pixels, (int)0) == 0) {
                boolean bl = false;
                return bl;
            }
            writeCallback.throwIfException();
            boolean bl = true;
            return bl;
        }
        finally {
            writeCallback.free();
        }
    }

    public void copyFrom(NativeImage nativeImage) {
        if (nativeImage.format() != this.format) {
            throw new UnsupportedOperationException("Image formats don't match.");
        }
        int n = this.format.components();
        this.checkAllocated();
        nativeImage.checkAllocated();
        if (this.width == nativeImage.width) {
            MemoryUtil.memCopy((long)nativeImage.pixels, (long)this.pixels, (long)Math.min(this.size, nativeImage.size));
        } else {
            int n2 = Math.min(this.getWidth(), nativeImage.getWidth());
            int n3 = Math.min(this.getHeight(), nativeImage.getHeight());
            for (int i = 0; i < n3; ++i) {
                int n4 = i * nativeImage.getWidth() * n;
                int n5 = i * this.getWidth() * n;
                MemoryUtil.memCopy((long)(nativeImage.pixels + (long)n4), (long)(this.pixels + (long)n5), (long)n2);
            }
        }
    }

    public void fillRect(int n, int n2, int n3, int n4, int n5) {
        for (int i = n2; i < n2 + n4; ++i) {
            for (int j = n; j < n + n3; ++j) {
                this.setPixelRGBA(j, i, n5);
            }
        }
    }

    public void copyRect(int n, int n2, int n3, int n4, int n5, int n6, boolean bl, boolean bl2) {
        for (int i = 0; i < n6; ++i) {
            for (int j = 0; j < n5; ++j) {
                int n7 = bl ? n5 - 1 - j : j;
                int n8 = bl2 ? n6 - 1 - i : i;
                int n9 = this.getPixelRGBA(n + j, n2 + i);
                this.setPixelRGBA(n + n3 + n7, n2 + n4 + n8, n9);
            }
        }
    }

    public void flipY() {
        this.checkAllocated();
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            int n = this.format.components();
            int n2 = this.getWidth() * n;
            long l = memoryStack.nmalloc(n2);
            for (int i = 0; i < this.getHeight() / 2; ++i) {
                int n3 = i * this.getWidth() * n;
                int n4 = (this.getHeight() - 1 - i) * this.getWidth() * n;
                MemoryUtil.memCopy((long)(this.pixels + (long)n3), (long)l, (long)n2);
                MemoryUtil.memCopy((long)(this.pixels + (long)n4), (long)(this.pixels + (long)n3), (long)n2);
                MemoryUtil.memCopy((long)l, (long)(this.pixels + (long)n4), (long)n2);
            }
        }
    }

    public void resizeSubRectTo(int n, int n2, int n3, int n4, NativeImage nativeImage) {
        this.checkAllocated();
        if (nativeImage.format() != this.format) {
            throw new UnsupportedOperationException("resizeSubRectTo only works for images of the same format.");
        }
        int n5 = this.format.components();
        STBImageResize.nstbir_resize_uint8((long)(this.pixels + (long)((n + n2 * this.getWidth()) * n5)), (int)n3, (int)n4, (int)(this.getWidth() * n5), (long)nativeImage.pixels, (int)nativeImage.getWidth(), (int)nativeImage.getHeight(), (int)0, (int)n5);
    }

    public void untrack() {
        DebugMemoryUntracker.untrack(this.pixels);
    }

    public static NativeImage fromBase64(String string) throws IOException {
        byte[] byArray = Base64.getDecoder().decode(string.replaceAll("\n", "").getBytes(Charsets.UTF_8));
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(byArray.length);
            byteBuffer.put(byArray);
            byteBuffer.rewind();
            NativeImage nativeImage = NativeImage.read(byteBuffer);
            return nativeImage;
        }
    }

    public static int getA(int n) {
        return n >> 24 & 0xFF;
    }

    public static int getR(int n) {
        return n >> 0 & 0xFF;
    }

    public static int getG(int n) {
        return n >> 8 & 0xFF;
    }

    public static int getB(int n) {
        return n >> 16 & 0xFF;
    }

    public static int combine(int n, int n2, int n3, int n4) {
        return (n & 0xFF) << 24 | (n2 & 0xFF) << 16 | (n3 & 0xFF) << 8 | (n4 & 0xFF) << 0;
    }

    public static enum Format {
        RGBA(4, 6408, true, true, true, false, true, 0, 8, 16, 255, 24, true),
        RGB(3, 6407, true, true, true, false, false, 0, 8, 16, 255, 255, true),
        LUMINANCE_ALPHA(2, 33319, false, false, false, true, true, 255, 255, 255, 0, 8, true),
        LUMINANCE(1, 6403, false, false, false, true, false, 0, 0, 0, 0, 255, true);

        final int components;
        private final int glFormat;
        private final boolean hasRed;
        private final boolean hasGreen;
        private final boolean hasBlue;
        private final boolean hasLuminance;
        private final boolean hasAlpha;
        private final int redOffset;
        private final int greenOffset;
        private final int blueOffset;
        private final int luminanceOffset;
        private final int alphaOffset;
        private final boolean supportedByStb;

        private Format(int n2, int n3, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, int n4, int n5, int n6, int n7, int n8, boolean bl6) {
            this.components = n2;
            this.glFormat = n3;
            this.hasRed = bl;
            this.hasGreen = bl2;
            this.hasBlue = bl3;
            this.hasLuminance = bl4;
            this.hasAlpha = bl5;
            this.redOffset = n4;
            this.greenOffset = n5;
            this.blueOffset = n6;
            this.luminanceOffset = n7;
            this.alphaOffset = n8;
            this.supportedByStb = bl6;
        }

        public int components() {
            return this.components;
        }

        public void setPackPixelStoreState() {
            RenderSystem.assertOnRenderThread();
            GlStateManager._pixelStore(3333, this.components());
        }

        public void setUnpackPixelStoreState() {
            RenderSystem.assertOnRenderThreadOrInit();
            GlStateManager._pixelStore(3317, this.components());
        }

        public int glFormat() {
            return this.glFormat;
        }

        public boolean hasRed() {
            return this.hasRed;
        }

        public boolean hasGreen() {
            return this.hasGreen;
        }

        public boolean hasBlue() {
            return this.hasBlue;
        }

        public boolean hasLuminance() {
            return this.hasLuminance;
        }

        public boolean hasAlpha() {
            return this.hasAlpha;
        }

        public int redOffset() {
            return this.redOffset;
        }

        public int greenOffset() {
            return this.greenOffset;
        }

        public int blueOffset() {
            return this.blueOffset;
        }

        public int luminanceOffset() {
            return this.luminanceOffset;
        }

        public int alphaOffset() {
            return this.alphaOffset;
        }

        public boolean hasLuminanceOrRed() {
            return this.hasLuminance || this.hasRed;
        }

        public boolean hasLuminanceOrGreen() {
            return this.hasLuminance || this.hasGreen;
        }

        public boolean hasLuminanceOrBlue() {
            return this.hasLuminance || this.hasBlue;
        }

        public boolean hasLuminanceOrAlpha() {
            return this.hasLuminance || this.hasAlpha;
        }

        public int luminanceOrRedOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.redOffset;
        }

        public int luminanceOrGreenOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.greenOffset;
        }

        public int luminanceOrBlueOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.blueOffset;
        }

        public int luminanceOrAlphaOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.alphaOffset;
        }

        public boolean supportedByStb() {
            return this.supportedByStb;
        }

        static Format getStbFormat(int n) {
            switch (n) {
                case 1: {
                    return LUMINANCE;
                }
                case 2: {
                    return LUMINANCE_ALPHA;
                }
                case 3: {
                    return RGB;
                }
            }
            return RGBA;
        }
    }

    static class WriteCallback
    extends STBIWriteCallback {
        private final WritableByteChannel output;
        @Nullable
        private IOException exception;

        WriteCallback(WritableByteChannel writableByteChannel) {
            this.output = writableByteChannel;
        }

        public void invoke(long l, long l2, int n) {
            ByteBuffer byteBuffer = WriteCallback.getData((long)l2, (int)n);
            try {
                this.output.write(byteBuffer);
            }
            catch (IOException iOException) {
                this.exception = iOException;
            }
        }

        public void throwIfException() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
        }
    }

    public static enum InternalGlFormat {
        RGBA(6408),
        RGB(6407),
        RG(33319),
        RED(6403);

        private final int glFormat;

        private InternalGlFormat(int n2) {
            this.glFormat = n2;
        }

        public int glFormat() {
            return this.glFormat;
        }
    }
}

