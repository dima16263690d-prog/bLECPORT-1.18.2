/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.MessageToByteEncoder
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;
import net.minecraft.network.FriendlyByteBuf;

public class CompressionEncoder
extends MessageToByteEncoder<ByteBuf> {
    private final byte[] encodeBuf = new byte[8192];
    private final Deflater deflater;
    private int threshold;

    public CompressionEncoder(int n) {
        this.threshold = n;
        this.deflater = new Deflater();
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) {
        int n = byteBuf.readableBytes();
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf2);
        if (n < this.threshold) {
            friendlyByteBuf.writeVarInt(0);
            friendlyByteBuf.writeBytes(byteBuf);
        } else {
            byte[] byArray = new byte[n];
            byteBuf.readBytes(byArray);
            friendlyByteBuf.writeVarInt(byArray.length);
            this.deflater.setInput(byArray, 0, n);
            this.deflater.finish();
            while (!this.deflater.finished()) {
                int n2 = this.deflater.deflate(this.encodeBuf);
                friendlyByteBuf.writeBytes(this.encodeBuf, 0, n2);
            }
            this.deflater.reset();
        }
    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int n) {
        this.threshold = n;
    }

    protected /* synthetic */ void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {
        this.encode(channelHandlerContext, (ByteBuf)object, byteBuf);
    }
}

