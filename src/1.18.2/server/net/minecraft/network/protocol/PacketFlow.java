/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol;

public enum PacketFlow {
    SERVERBOUND,
    CLIENTBOUND;


    public PacketFlow getOpposite() {
        return this == CLIENTBOUND ? SERVERBOUND : CLIENTBOUND;
    }
}

