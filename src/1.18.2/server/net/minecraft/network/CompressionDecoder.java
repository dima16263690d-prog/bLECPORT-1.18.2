/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.Unpooled
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.ByteToMessageDecoder
 *  io.netty.handler.codec.DecoderException
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.zip.Inflater;
import net.minecraft.network.FriendlyByteBuf;

public class CompressionDecoder
extends ByteToMessageDecoder {
    public static final int MAXIMUM_COMPRESSED_LENGTH = 0x200000;
    public static final int MAXIMUM_UNCOMPRESSED_LENGTH = 0x800000;
    private final Inflater inflater;
    private int threshold;
    private boolean validateDecompressed;

    public CompressionDecoder(int n, boolean bl) {
        this.threshold = n;
        this.validateDecompressed = bl;
        this.inflater = new Inflater();
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() == 0) {
            return;
        }
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
        int n = friendlyByteBuf.readVarInt();
        if (n == 0) {
            list.add(friendlyByteBuf.readBytes(friendlyByteBuf.readableBytes()));
            return;
        }
        if (this.validateDecompressed) {
            if (n < this.threshold) {
                throw new DecoderException("Badly compressed packet - size of " + n + " is below server threshold of " + this.threshold);
            }
            if (n > 0x800000) {
                throw new DecoderException("Badly compressed packet - size of " + n + " is larger than protocol maximum of 8388608");
            }
        }
        byte[] byArray = new byte[friendlyByteBuf.readableBytes()];
        friendlyByteBuf.readBytes(byArray);
        this.inflater.setInput(byArray);
        byte[] byArray2 = new byte[n];
        this.inflater.inflate(byArray2);
        list.add(Unpooled.wrappedBuffer((byte[])byArray2));
        this.inflater.reset();
    }

    public void setThreshold(int n, boolean bl) {
        this.threshold = n;
        this.validateDecompressed = bl;
    }
}

