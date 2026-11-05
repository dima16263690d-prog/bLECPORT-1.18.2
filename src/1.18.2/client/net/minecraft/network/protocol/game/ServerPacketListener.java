/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketListener;

public interface ServerPacketListener
extends PacketListener {
    @Override
    default public boolean shouldPropagateHandlingExceptions() {
        return false;
    }
}

