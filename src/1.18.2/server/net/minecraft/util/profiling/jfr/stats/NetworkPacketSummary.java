/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.util.profiling.jfr.stats;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public final class NetworkPacketSummary {
    private final PacketCountAndSize totalPacketCountAndSize;
    private final List<Pair<PacketIdentification, PacketCountAndSize>> largestSizeContributors;
    private final Duration recordingDuration;

    public NetworkPacketSummary(Duration duration, List<Pair<PacketIdentification, PacketCountAndSize>> list) {
        this.recordingDuration = duration;
        this.totalPacketCountAndSize = list.stream().map(Pair::getSecond).reduce(PacketCountAndSize::add).orElseGet(() -> new PacketCountAndSize(0L, 0L));
        this.largestSizeContributors = list.stream().sorted(Comparator.comparing(Pair::getSecond, PacketCountAndSize.SIZE_THEN_COUNT)).limit(10L).toList();
    }

    public double getCountsPerSecond() {
        return (double)this.totalPacketCountAndSize.totalCount / (double)this.recordingDuration.getSeconds();
    }

    public double getSizePerSecond() {
        return (double)this.totalPacketCountAndSize.totalSize / (double)this.recordingDuration.getSeconds();
    }

    public long getTotalCount() {
        return this.totalPacketCountAndSize.totalCount;
    }

    public long getTotalSize() {
        return this.totalPacketCountAndSize.totalSize;
    }

    public List<Pair<PacketIdentification, PacketCountAndSize>> largestSizeContributors() {
        return this.largestSizeContributors;
    }

    public static final class PacketCountAndSize
    extends Record {
        final long totalCount;
        final long totalSize;
        static final Comparator<PacketCountAndSize> SIZE_THEN_COUNT = Comparator.comparing(PacketCountAndSize::totalSize).thenComparing(PacketCountAndSize::totalCount).reversed();

        public PacketCountAndSize(long l, long l2) {
            this.totalCount = l;
            this.totalSize = l2;
        }

        PacketCountAndSize add(PacketCountAndSize packetCountAndSize) {
            return new PacketCountAndSize(this.totalCount + packetCountAndSize.totalCount, this.totalSize + packetCountAndSize.totalSize);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PacketCountAndSize.class, "totalCount;totalSize", "totalCount", "totalSize"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PacketCountAndSize.class, "totalCount;totalSize", "totalCount", "totalSize"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PacketCountAndSize.class, "totalCount;totalSize", "totalCount", "totalSize"}, this, object);
        }

        public long totalCount() {
            return this.totalCount;
        }

        public long totalSize() {
            return this.totalSize;
        }
    }

    public record PacketIdentification(PacketFlow direction, int protocolId, int packetId) {
        private static final Map<PacketIdentification, String> PACKET_NAME_BY_ID;

        public String packetName() {
            return PACKET_NAME_BY_ID.getOrDefault((Object)this, "unknown");
        }

        public static PacketIdentification from(RecordedEvent recordedEvent) {
            return new PacketIdentification(recordedEvent.getEventType().getName().equals("minecraft.PacketSent") ? PacketFlow.CLIENTBOUND : PacketFlow.SERVERBOUND, recordedEvent.getInt("protocolId"), recordedEvent.getInt("packetId"));
        }

        static {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            for (ConnectionProtocol connectionProtocol : ConnectionProtocol.values()) {
                for (PacketFlow packetFlow : PacketFlow.values()) {
                    Int2ObjectMap<Class<? extends Packet<?>>> int2ObjectMap = connectionProtocol.getPacketsByIds(packetFlow);
                    int2ObjectMap.forEach((n, clazz) -> builder.put((Object)new PacketIdentification(packetFlow, connectionProtocol.getId(), (int)n), (Object)clazz.getSimpleName()));
                }
            }
            PACKET_NAME_BY_ID = builder.build();
        }
    }
}

