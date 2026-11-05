/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.world.level.biome.Biome;

public class LocateBiomeCommand {
    private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.locatebiome.notFound", object));
    private static final int MAX_SEARCH_RADIUS = 6400;
    private static final int SEARCH_STEP = 8;

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("locatebiome").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("biome", ResourceOrTagLocationArgument.resourceOrTag(Registry.BIOME_REGISTRY)).executes(commandContext -> LocateBiomeCommand.locateBiome((CommandSourceStack)commandContext.getSource(), ResourceOrTagLocationArgument.getBiome((CommandContext<CommandSourceStack>)commandContext, "biome")))));
    }

    private static int locateBiome(CommandSourceStack commandSourceStack, ResourceOrTagLocationArgument.Result<Biome> result) throws CommandSyntaxException {
        BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
        Pair<BlockPos, Holder<Biome>> pair = commandSourceStack.getLevel().findNearestBiome(result, blockPos, 6400, 8);
        if (pair == null) {
            throw ERROR_BIOME_NOT_FOUND.create((Object)result.asPrintable());
        }
        return LocateCommand.showLocateResult(commandSourceStack, result, blockPos, pair, "commands.locatebiome.success");
    }
}

