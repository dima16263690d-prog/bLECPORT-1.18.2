/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  com.google.gson.LongSerializationPolicy
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.profiling.jfr.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;
import net.minecraft.Util;
import net.minecraft.util.profiling.jfr.Percentiles;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.NetworkPacketSummary;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;
import net.minecraft.util.profiling.jfr.stats.TimedStatSummary;
import net.minecraft.world.level.chunk.ChunkStatus;

public class JfrResultJsonSerializer {
    private static final String BYTES_PER_SECOND = "bytesPerSecond";
    private static final String COUNT = "count";
    private static final String DURATION_NANOS_TOTAL = "durationNanosTotal";
    private static final String TOTAL_BYTES = "totalBytes";
    private static final String COUNT_PER_SECOND = "countPerSecond";
    final Gson gson = new GsonBuilder().setPrettyPrinting().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT).create();

    public String format(JfrStatsResult jfrStatsResult) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("startedEpoch", (Number)jfrStatsResult.recordingStarted().toEpochMilli());
        jsonObject.addProperty("endedEpoch", (Number)jfrStatsResult.recordingEnded().toEpochMilli());
        jsonObject.addProperty("durationMs", (Number)jfrStatsResult.recordingDuration().toMillis());
        Duration duration = jfrStatsResult.worldCreationDuration();
        if (duration != null) {
            jsonObject.addProperty("worldGenDurationMs", (Number)duration.toMillis());
        }
        jsonObject.add("heap", this.heap(jfrStatsResult.heapSummary()));
        jsonObject.add("cpuPercent", this.cpu(jfrStatsResult.cpuLoadStats()));
        jsonObject.add("network", this.network(jfrStatsResult));
        jsonObject.add("fileIO", this.fileIO(jfrStatsResult));
        jsonObject.add("serverTick", this.serverTicks(jfrStatsResult.tickTimes()));
        jsonObject.add("threadAllocation", this.threadAllocations(jfrStatsResult.threadAllocationSummary()));
        jsonObject.add("chunkGen", this.chunkGen(jfrStatsResult.chunkGenSummary()));
        return this.gson.toJson((JsonElement)jsonObject);
    }

    private JsonElement heap(GcHeapStat.Summary summary) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("allocationRateBytesPerSecond", (Number)summary.allocationRateBytesPerSecond());
        jsonObject.addProperty("gcCount", (Number)summary.totalGCs());
        jsonObject.addProperty("gcOverHeadPercent", (Number)Float.valueOf(summary.gcOverHead()));
        jsonObject.addProperty("gcTotalDurationMs", (Number)summary.gcTotalDuration().toMillis());
        return jsonObject;
    }

    private JsonElement chunkGen(List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> list) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(DURATION_NANOS_TOTAL, (Number)list.stream().mapToDouble(pair -> ((TimedStatSummary)((Object)((Object)pair.getSecond()))).totalDuration().toNanos()).sum());
        JsonArray jsonArray2 = Util.make(new JsonArray(), jsonArray -> jsonObject.add("status", (JsonElement)jsonArray));
        for (Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>> pair2 : list) {
            TimedStatSummary timedStatSummary = (TimedStatSummary)((Object)pair2.getSecond());
            JsonObject jsonObject3 = Util.make(new JsonObject(), arg_0 -> ((JsonArray)jsonArray2).add(arg_0));
            jsonObject3.addProperty("state", ((ChunkStatus)pair2.getFirst()).getName());
            jsonObject3.addProperty(COUNT, (Number)timedStatSummary.count());
            jsonObject3.addProperty(DURATION_NANOS_TOTAL, (Number)timedStatSummary.totalDuration().toNanos());
            jsonObject3.addProperty("durationNanosAvg", (Number)(timedStatSummary.totalDuration().toNanos() / (long)timedStatSummary.count()));
            JsonObject jsonObject4 = Util.make(new JsonObject(), jsonObject2 -> jsonObject3.add("durationNanosPercentiles", (JsonElement)jsonObject2));
            timedStatSummary.percentilesNanos().forEach((n, d) -> jsonObject4.addProperty("p" + n, (Number)d));
            Function<ChunkGenStat, JsonElement> function = chunkGenStat -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("durationNanos", (Number)chunkGenStat.duration().toNanos());
                jsonObject.addProperty("level", chunkGenStat.level());
                jsonObject.addProperty("chunkPosX", (Number)chunkGenStat.chunkPos().x);
                jsonObject.addProperty("chunkPosZ", (Number)chunkGenStat.chunkPos().z);
                jsonObject.addProperty("worldPosX", (Number)chunkGenStat.worldPos().x);
                jsonObject.addProperty("worldPosZ", (Number)chunkGenStat.worldPos().z);
                return jsonObject;
            };
            jsonObject3.add("fastest", function.apply((ChunkGenStat)timedStatSummary.fastest()));
            jsonObject3.add("slowest", function.apply((ChunkGenStat)timedStatSummary.slowest()));
            jsonObject3.add("secondSlowest", (JsonElement)(timedStatSummary.secondSlowest() != null ? function.apply((ChunkGenStat)timedStatSummary.secondSlowest()) : JsonNull.INSTANCE));
        }
        return jsonObject;
    }

    private JsonElement threadAllocations(ThreadAllocationStat.Summary summary) {
        JsonArray jsonArray = new JsonArray();
        summary.allocationsPerSecondByThread().forEach((string, d) -> jsonArray.add((JsonElement)Util.make(new JsonObject(), jsonObject -> {
            jsonObject.addProperty("thread", string);
            jsonObject.addProperty(BYTES_PER_SECOND, (Number)d);
        })));
        return jsonArray;
    }

    private JsonElement serverTicks(List<TickTimeStat> list) {
        if (list.isEmpty()) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        double[] dArray = list.stream().mapToDouble(tickTimeStat -> (double)tickTimeStat.currentAverage().toNanos() / 1000000.0).toArray();
        DoubleSummaryStatistics doubleSummaryStatistics = DoubleStream.of(dArray).summaryStatistics();
        jsonObject.addProperty("minMs", (Number)doubleSummaryStatistics.getMin());
        jsonObject.addProperty("averageMs", (Number)doubleSummaryStatistics.getAverage());
        jsonObject.addProperty("maxMs", (Number)doubleSummaryStatistics.getMax());
        Map<Integer, Double> map = Percentiles.evaluate(dArray);
        map.forEach((n, d) -> jsonObject.addProperty("p" + n, (Number)d));
        return jsonObject;
    }

    private JsonElement fileIO(JfrStatsResult jfrStatsResult) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("write", this.fileIoSummary(jfrStatsResult.fileWrites()));
        jsonObject.add("read", this.fileIoSummary(jfrStatsResult.fileReads()));
        return jsonObject;
    }

    private JsonElement fileIoSummary(FileIOStat.Summary summary) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TOTAL_BYTES, (Number)summary.totalBytes());
        jsonObject.addProperty(COUNT, (Number)summary.counts());
        jsonObject.addProperty(BYTES_PER_SECOND, (Number)summary.bytesPerSecond());
        jsonObject.addProperty(COUNT_PER_SECOND, (Number)summary.countsPerSecond());
        JsonArray jsonArray = new JsonArray();
        jsonObject.add("topContributors", (JsonElement)jsonArray);
        summary.topTenContributorsByTotalBytes().forEach(pair -> {
            JsonObject jsonObject = new JsonObject();
            jsonArray.add((JsonElement)jsonObject);
            jsonObject.addProperty("path", (String)pair.getFirst());
            jsonObject.addProperty(TOTAL_BYTES, (Number)pair.getSecond());
        });
        return jsonObject;
    }

    private JsonElement network(JfrStatsResult jfrStatsResult) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("sent", this.packets(jfrStatsResult.sentPacketsSummary()));
        jsonObject.add("received", this.packets(jfrStatsResult.receivedPacketsSummary()));
        return jsonObject;
    }

    private JsonElement packets(NetworkPacketSummary networkPacketSummary) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TOTAL_BYTES, (Number)networkPacketSummary.getTotalSize());
        jsonObject.addProperty(COUNT, (Number)networkPacketSummary.getTotalCount());
        jsonObject.addProperty(BYTES_PER_SECOND, (Number)networkPacketSummary.getSizePerSecond());
        jsonObject.addProperty(COUNT_PER_SECOND, (Number)networkPacketSummary.getCountsPerSecond());
        JsonArray jsonArray = new JsonArray();
        jsonObject.add("topContributors", (JsonElement)jsonArray);
        networkPacketSummary.largestSizeContributors().forEach(pair -> {
            JsonObject jsonObject = new JsonObject();
            jsonArray.add((JsonElement)jsonObject);
            NetworkPacketSummary.PacketIdentification packetIdentification = (NetworkPacketSummary.PacketIdentification)((Object)((Object)pair.getFirst()));
            NetworkPacketSummary.PacketCountAndSize packetCountAndSize = (NetworkPacketSummary.PacketCountAndSize)((Object)((Object)pair.getSecond()));
            jsonObject.addProperty("protocolId", (Number)packetIdentification.protocolId());
            jsonObject.addProperty("packetId", (Number)packetIdentification.packetId());
            jsonObject.addProperty("packetName", packetIdentification.packetName());
            jsonObject.addProperty(TOTAL_BYTES, (Number)packetCountAndSize.totalSize());
            jsonObject.addProperty(COUNT, (Number)packetCountAndSize.totalCount());
        });
        return jsonObject;
    }

    private JsonElement cpu(List<CpuLoadStat> list2) {
        JsonObject jsonObject = new JsonObject();
        BiFunction<List, ToDoubleFunction, JsonObject> biFunction = (list, toDoubleFunction) -> {
            JsonObject jsonObject = new JsonObject();
            DoubleSummaryStatistics doubleSummaryStatistics = list.stream().mapToDouble(toDoubleFunction).summaryStatistics();
            jsonObject.addProperty("min", (Number)doubleSummaryStatistics.getMin());
            jsonObject.addProperty("average", (Number)doubleSummaryStatistics.getAverage());
            jsonObject.addProperty("max", (Number)doubleSummaryStatistics.getMax());
            return jsonObject;
        };
        jsonObject.add("jvm", (JsonElement)biFunction.apply(list2, CpuLoadStat::jvm));
        jsonObject.add("userJvm", (JsonElement)biFunction.apply(list2, CpuLoadStat::userJvm));
        jsonObject.add("system", (JsonElement)biFunction.apply(list2, CpuLoadStat::system));
        return jsonObject;
    }
}

