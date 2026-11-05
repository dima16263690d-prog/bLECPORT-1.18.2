/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.datafixers.util.Unit
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ResetChunksCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("resetchunks").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).executes(commandContext -> ResetChunksCommand.resetChunks((CommandSourceStack)commandContext.getSource(), 0, true))).then(((RequiredArgumentBuilder)Commands.argument("range", IntegerArgumentType.integer((int)0, (int)5)).executes(commandContext -> ResetChunksCommand.resetChunks((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"range"), true))).then(Commands.argument("skipOldChunks", BoolArgumentType.bool()).executes(commandContext -> ResetChunksCommand.resetChunks((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"range"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"skipOldChunks"))))));
    }

    private static int resetChunks(CommandSourceStack commandSourceStack, int n, boolean bl) {
        Object object;
        ServerLevel serverLevel = commandSourceStack.getLevel();
        ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        serverChunkCache.chunkMap.debugReloadGenerator();
        Vec3 vec3 = commandSourceStack.getPosition();
        ChunkPos chunkPos = new ChunkPos(new BlockPos(vec3));
        int n2 = chunkPos.z - n;
        int n3 = chunkPos.z + n;
        int n4 = chunkPos.x - n;
        int n5 = chunkPos.x + n;
        for (int i = n2; i <= n3; ++i) {
            for (int j = n4; j <= n5; ++j) {
                ChunkPos chunkPos2 = new ChunkPos(j, i);
                LevelChunk levelChunk = serverChunkCache.getChunk(j, i, false);
                if (levelChunk == null || bl && levelChunk.isOldNoiseGeneration()) continue;
                for (BlockPos object2 : BlockPos.betweenClosed(chunkPos2.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos2.getMinBlockZ(), chunkPos2.getMaxBlockX(), serverLevel.getMaxBuildHeight() - 1, chunkPos2.getMaxBlockZ())) {
                    serverLevel.setBlock(object2, Blocks.AIR.defaultBlockState(), 16);
                }
            }
        }
        ProcessorMailbox<Runnable> processorMailbox = ProcessorMailbox.create(Util.backgroundExecutor(), "worldgen-resetchunks");
        long l = System.currentTimeMillis();
        int n6 = (n * 2 + 1) * (n * 2 + 1);
        for (ChunkStatus chunkStatus : ImmutableList.of((Object)ChunkStatus.BIOMES, (Object)ChunkStatus.NOISE, (Object)ChunkStatus.SURFACE, (Object)ChunkStatus.CARVERS, (Object)ChunkStatus.LIQUID_CARVERS, (Object)ChunkStatus.FEATURES)) {
            long l4 = System.currentTimeMillis();
            object = CompletableFuture.supplyAsync(() -> Unit.INSTANCE, processorMailbox::tell);
            for (int levelChunk = chunkPos.z - n; levelChunk <= chunkPos.z + n; ++levelChunk) {
                for (int i = chunkPos.x - n; i <= chunkPos.x + n; ++i) {
                    ChunkPos chunkPos2 = new ChunkPos(i, levelChunk);
                    LevelChunk levelChunk2 = serverChunkCache.getChunk(i, levelChunk, false);
                    if (levelChunk2 == null || bl && levelChunk2.isOldNoiseGeneration()) continue;
                    ArrayList arrayList = Lists.newArrayList();
                    int n7 = Math.max(1, chunkStatus.getRange());
                    for (int j = chunkPos2.z - n7; j <= chunkPos2.z + n7; ++j) {
                        for (int k = chunkPos2.x - n7; k <= chunkPos2.x + n7; ++k) {
                            ChunkAccess chunkAccess = serverChunkCache.getChunk(k, j, chunkStatus.getParent(), true);
                            ChunkAccess chunkAccess2 = chunkAccess instanceof ImposterProtoChunk ? new ImposterProtoChunk(((ImposterProtoChunk)chunkAccess).getWrapped(), true) : (chunkAccess instanceof LevelChunk ? new ImposterProtoChunk((LevelChunk)chunkAccess, true) : chunkAccess);
                            arrayList.add(chunkAccess2);
                        }
                    }
                    object = ((CompletableFuture)object).thenComposeAsync(unit -> chunkStatus.generate(processorMailbox::tell, serverLevel, serverChunkCache.getGenerator(), serverLevel.getStructureManager(), serverChunkCache.getLightEngine(), chunkAccess -> {
                        throw new UnsupportedOperationException("Not creating full chunks here");
                    }, arrayList, true).thenApply(either -> {
                        if (chunkStatus == ChunkStatus.NOISE) {
                            either.left().ifPresent(chunkAccess -> Heightmap.primeHeightmaps(chunkAccess, ChunkStatus.POST_FEATURES));
                        }
                        return Unit.INSTANCE;
                    }), processorMailbox::tell);
                }
            }
            commandSourceStack.getServer().managedBlock(() -> object.isDone());
            LOGGER.debug(chunkStatus.getName() + " took " + (System.currentTimeMillis() - l4) + " ms");
        }
        long l3 = System.currentTimeMillis();
        for (int i = chunkPos.z - n; i <= chunkPos.z + n; ++i) {
            for (int j = chunkPos.x - n; j <= chunkPos.x + n; ++j) {
                object = new ChunkPos(j, i);
                LevelChunk levelChunk = serverChunkCache.getChunk(j, i, false);
                if (levelChunk == null || bl && levelChunk.isOldNoiseGeneration()) continue;
                for (BlockPos blockPos : BlockPos.betweenClosed(((ChunkPos)object).getMinBlockX(), serverLevel.getMinBuildHeight(), ((ChunkPos)object).getMinBlockZ(), ((ChunkPos)object).getMaxBlockX(), serverLevel.getMaxBuildHeight() - 1, ((ChunkPos)object).getMaxBlockZ())) {
                    serverChunkCache.blockChanged(blockPos);
                }
            }
        }
        LOGGER.debug("blockChanged took " + (System.currentTimeMillis() - l3) + " ms");
        long l2 = System.currentTimeMillis() - l;
        commandSourceStack.sendSuccess(new TextComponent(String.format("%d chunks have been reset. This took %d ms for %d chunks, or %02f ms per chunk", n6, l2, n6, Float.valueOf((float)l2 / (float)n6))), true);
        return 1;
    }
}

