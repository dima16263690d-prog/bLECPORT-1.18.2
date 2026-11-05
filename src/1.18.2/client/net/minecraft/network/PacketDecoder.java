/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.ByteToMessageDecoder
 *  org.slf4j.Logger
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketDecoder
extends ByteToMessageDecoder {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PacketFlow flow;

    public PacketDecoder(PacketFlow packetFlow) {
        this.flow = packetFlow;
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int n = byteBuf.readableBytes();
        if (n == 0) {
            return;
        }
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
        int n2 = friendlyByteBuf.readVarInt();
        Packet<?> packet = ((ConnectionProtocol)((Object)channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get())).createPacket(this.flow, n2, friendlyByteBuf);
        if (packet == null) {
            throw new IOException("Bad packet id " + n2);
        }
        int n3 = ((ConnectionProtocol)((Object)channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get())).getId();
        JvmProfiler.INSTANCE.onPacketReceived(n3, n2, channelHandlerContext.channel().remoteAddress(), n);
        if (friendlyByteBuf.readableBytes() > 0) {
            throw new IOException("Packet " + ((ConnectionProtocol)((Object)channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get())).getId() + "/" + n2 + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + friendlyByteBuf.readableBytes() + " bytes extra whilst reading packet " + n2);
        }
        list.add(packet);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Connection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {}", new Object[]{channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(), n2, packet.getClass().getName()});
        }
    }
}

