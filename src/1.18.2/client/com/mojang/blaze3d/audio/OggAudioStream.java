/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.stb.STBVorbis
 *  org.lwjgl.stb.STBVorbisInfo
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.audio;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.util.Mth;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class OggAudioStream
implements AudioStream {
    private static final int EXPECTED_MAX_FRAME_SIZE = 8192;
    private long handle;
    private final AudioFormat audioFormat;
    private final InputStream input;
    private ByteBuffer buffer = MemoryUtil.memAlloc((int)8192);

    public OggAudioStream(InputStream inputStream) throws IOException {
        this.input = inputStream;
        this.buffer.limit(0);
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            IntBuffer intBuffer2 = memoryStack.mallocInt(1);
            while (this.handle == 0L) {
                if (!this.refillFromStream()) {
                    throw new IOException("Failed to find Ogg header");
                }
                int n = this.buffer.position();
                this.buffer.position(0);
                this.handle = STBVorbis.stb_vorbis_open_pushdata((ByteBuffer)this.buffer, (IntBuffer)intBuffer, (IntBuffer)intBuffer2, null);
                this.buffer.position(n);
                int n2 = intBuffer2.get(0);
                if (n2 == 1) {
                    this.forwardBuffer();
                    continue;
                }
                if (n2 == 0) continue;
                throw new IOException("Failed to read Ogg file " + n2);
            }
            this.buffer.position(this.buffer.position() + intBuffer.get(0));
            STBVorbisInfo sTBVorbisInfo = STBVorbisInfo.mallocStack((MemoryStack)memoryStack);
            STBVorbis.stb_vorbis_get_info((long)this.handle, (STBVorbisInfo)sTBVorbisInfo);
            this.audioFormat = new AudioFormat(sTBVorbisInfo.sample_rate(), 16, sTBVorbisInfo.channels(), true, false);
        }
    }

    private boolean refillFromStream() throws IOException {
        int n = this.buffer.limit();
        int n2 = this.buffer.capacity() - n;
        if (n2 == 0) {
            return true;
        }
        byte[] byArray = new byte[n2];
        int n3 = this.input.read(byArray);
        if (n3 == -1) {
            return false;
        }
        int n4 = this.buffer.position();
        this.buffer.limit(n + n3);
        this.buffer.position(n);
        this.buffer.put(byArray, 0, n3);
        this.buffer.position(n4);
        return true;
    }

    private void forwardBuffer() {
        boolean bl;
        boolean bl2 = this.buffer.position() == 0;
        boolean bl3 = bl = this.buffer.position() == this.buffer.limit();
        if (bl && !bl2) {
            this.buffer.position(0);
            this.buffer.limit(0);
        } else {
            ByteBuffer byteBuffer = MemoryUtil.memAlloc((int)(bl2 ? 2 * this.buffer.capacity() : this.buffer.capacity()));
            byteBuffer.put(this.buffer);
            MemoryUtil.memFree((Buffer)this.buffer);
            byteBuffer.flip();
            this.buffer = byteBuffer;
        }
    }

    private boolean readFrame(OutputConcat outputConcat) throws IOException {
        if (this.handle == 0L) {
            return false;
        }
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            int n;
            block14: {
                int n2;
                PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
                IntBuffer intBuffer = memoryStack.mallocInt(1);
                IntBuffer intBuffer2 = memoryStack.mallocInt(1);
                while (true) {
                    n = STBVorbis.stb_vorbis_decode_frame_pushdata((long)this.handle, (ByteBuffer)this.buffer, (IntBuffer)intBuffer, (PointerBuffer)pointerBuffer, (IntBuffer)intBuffer2);
                    this.buffer.position(this.buffer.position() + n);
                    int n3 = STBVorbis.stb_vorbis_get_error((long)this.handle);
                    if (n3 == 1) {
                        this.forwardBuffer();
                        if (this.refillFromStream()) continue;
                        break block14;
                    }
                    if (n3 != 0) {
                        throw new IOException("Failed to read Ogg file " + n3);
                    }
                    n2 = intBuffer2.get(0);
                    if (n2 != 0) break;
                }
                int n4 = intBuffer.get(0);
                PointerBuffer pointerBuffer2 = pointerBuffer.getPointerBuffer(n4);
                if (n4 == 1) {
                    this.convertMono(pointerBuffer2.getFloatBuffer(0, n2), outputConcat);
                    boolean bl = true;
                    return bl;
                }
                if (n4 == 2) {
                    this.convertStereo(pointerBuffer2.getFloatBuffer(0, n2), pointerBuffer2.getFloatBuffer(1, n2), outputConcat);
                    boolean bl = true;
                    return bl;
                }
                throw new IllegalStateException("Invalid number of channels: " + n4);
            }
            n = 0;
            return n != 0;
        }
    }

    private void convertMono(FloatBuffer floatBuffer, OutputConcat outputConcat) {
        while (floatBuffer.hasRemaining()) {
            outputConcat.put(floatBuffer.get());
        }
    }

    private void convertStereo(FloatBuffer floatBuffer, FloatBuffer floatBuffer2, OutputConcat outputConcat) {
        while (floatBuffer.hasRemaining() && floatBuffer2.hasRemaining()) {
            outputConcat.put(floatBuffer.get());
            outputConcat.put(floatBuffer2.get());
        }
    }

    @Override
    public void close() throws IOException {
        if (this.handle != 0L) {
            STBVorbis.stb_vorbis_close((long)this.handle);
            this.handle = 0L;
        }
        MemoryUtil.memFree((Buffer)this.buffer);
        this.input.close();
    }

    @Override
    public AudioFormat getFormat() {
        return this.audioFormat;
    }

    @Override
    public ByteBuffer read(int n) throws IOException {
        OutputConcat outputConcat = new OutputConcat(n + 8192);
        while (this.readFrame(outputConcat) && outputConcat.byteCount < n) {
        }
        return outputConcat.get();
    }

    public ByteBuffer readAll() throws IOException {
        OutputConcat outputConcat = new OutputConcat(16384);
        while (this.readFrame(outputConcat)) {
        }
        return outputConcat.get();
    }

    static class OutputConcat {
        private final List<ByteBuffer> buffers = Lists.newArrayList();
        private final int bufferSize;
        int byteCount;
        private ByteBuffer currentBuffer;

        public OutputConcat(int n) {
            this.bufferSize = n + 1 & 0xFFFFFFFE;
            this.createNewBuffer();
        }

        private void createNewBuffer() {
            this.currentBuffer = BufferUtils.createByteBuffer((int)this.bufferSize);
        }

        public void put(float f) {
            if (this.currentBuffer.remaining() == 0) {
                this.currentBuffer.flip();
                this.buffers.add(this.currentBuffer);
                this.createNewBuffer();
            }
            int n = Mth.clamp((int)(f * 32767.5f - 0.5f), Short.MIN_VALUE, Short.MAX_VALUE);
            this.currentBuffer.putShort((short)n);
            this.byteCount += 2;
        }

        public ByteBuffer get() {
            this.currentBuffer.flip();
            if (this.buffers.isEmpty()) {
                return this.currentBuffer;
            }
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer((int)this.byteCount);
            this.buffers.forEach(byteBuffer::put);
            byteBuffer.put(this.currentBuffer);
            byteBuffer.flip();
            return byteBuffer;
        }
    }
}

