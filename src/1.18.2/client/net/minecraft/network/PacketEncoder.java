/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.MessageToByteEncoder
 *  org.slf4j.Logger
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.SkipPacketException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketEncoder
extends MessageToByteEncoder<Packet<?>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PacketFlow flow;

    public PacketEncoder(PacketFlow packetFlow) {
        this.flow = packetFlow;
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, ByteBuf byteBuf) throws Exception {
        ConnectionProtocol connectionProtocol = (ConnectionProtocol)((Object)channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get());
        if (connectionProtocol == null) {
            throw new RuntimeException("ConnectionProtocol unknown: " + packet);
        }
        Integer n = connectionProtocol.getPacketId(this.flow, packet);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Connection.PACKET_SENT_MARKER, "OUT: [{}:{}] {}", new Object[]{channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(), n, packet.getClass().getName()});
        }
        if (n == null) {
            throw new IOException("Can't serialize unregistered packet");
        }
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
        friendlyByteBuf.writeVarInt(n);
        try {
            int n2 = friendlyByteBuf.writerIndex();
            packet.write(friendlyByteBuf);
            int n3 = friendlyByteBuf.writerIndex() - n2;
            if (n3 > 0x800000) {
                throw new IllegalArgumentException("Packet too big (is " + n3 + ", should be less than 8388608): " + packet);
            }
            int n4 = ((ConnectionProtocol)((Object)channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get())).getId();
            JvmProfiler.INSTANCE.onPacketSent(n4, n, channelHandlerContext.channel().remoteAddress(), n3);
        }
        catch (Throwable throwable) {
            LOGGER.error("Error receiving packet {}", (Object)n, (Object)throwable);
            if (packet.isSkippable()) {
                throw new SkipPacketException(throwable);
            }
            throw throwable;
        }
    }

    protected /* synthetic */ void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {
        this.encode(channelHandlerContext, (Packet)object, byteBuf);
    }
}

