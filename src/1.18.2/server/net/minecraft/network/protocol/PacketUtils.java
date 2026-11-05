/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.network.protocol;

import com.mojang.logging.LogUtils;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import org.slf4j.Logger;

public class PacketUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T t, ServerLevel serverLevel) throws RunningOnDifferentThreadException {
        PacketUtils.ensureRunningOnSameThread(packet, t, serverLevel.getServer());
    }

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T t, BlockableEventLoop<?> blockableEventLoop) throws RunningOnDifferentThreadException {
        if (!blockableEventLoop.isSameThread()) {
            blockableEventLoop.executeIfPossible(() -> {
                if (t.getConnection().isConnected()) {
                    try {
                        packet.handle(t);
                    }
                    catch (Exception exception) {
                        if (t.shouldPropagateHandlingExceptions()) {
                            throw exception;
                        }
                        LOGGER.error("Failed to handle packet {}, suppressing error", (Object)packet, (Object)exception);
                    }
                } else {
                    LOGGER.debug("Ignoring packet due to disconnection: {}", (Object)packet);
                }
            });
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }
}

