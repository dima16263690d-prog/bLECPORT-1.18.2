/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr.event;

import java.net.SocketAddress;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.util.profiling.jfr.event.PacketEvent;

@Name(value="minecraft.PacketReceived")
@Label(value="Network Packet Received")
@DontObfuscate
public class PacketReceivedEvent
extends PacketEvent {
    public static final String NAME = "minecraft.PacketReceived";
    public static final EventType TYPE = EventType.getEventType(PacketReceivedEvent.class);

    public PacketReceivedEvent(int n, int n2, SocketAddress socketAddress, int n3) {
        super(n, n2, socketAddress, n3);
    }
}

