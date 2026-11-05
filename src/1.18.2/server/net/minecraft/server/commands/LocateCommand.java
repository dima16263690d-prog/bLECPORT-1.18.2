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
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

public class LocateCommand {
    private static final DynamicCommandExceptionType ERROR_FAILED = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.locate.failed", object));
    private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.locate.invalid", object));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("locate").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("structure", ResourceOrTagLocationArgument.resourceOrTag(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY)).executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), ResourceOrTagLocationArgument.getStructureFeature((CommandContext<CommandSourceStack>)commandContext, "structure")))));
    }

    private static int locate(CommandSourceStack commandSourceStack, ResourceOrTagLocationArgument.Result<ConfiguredStructureFeature<?, ?>> result) throws CommandSyntaxException {
        Registry<ConfiguredStructureFeature<?, ?>> registry = commandSourceStack.getLevel().registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        HolderSet holderSet = (HolderSet)((Optional)result.unwrap().map(resourceKey -> registry.getHolder((ResourceKey<ConfiguredStructureFeature<?, ?>>)resourceKey).map(holder -> HolderSet.direct(holder)), registry::getTag)).orElseThrow(() -> ERROR_INVALID.create((Object)result.asPrintable()));
        BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
        ServerLevel serverLevel = commandSourceStack.getLevel();
        Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> pair = serverLevel.getChunkSource().getGenerator().findNearestMapFeature(serverLevel, holderSet, blockPos, 100, false);
        if (pair == null) {
            throw ERROR_FAILED.create((Object)result.asPrintable());
        }
        return LocateCommand.showLocateResult(commandSourceStack, result, blockPos, pair, "commands.locate.success");
    }

    public static int showLocateResult(CommandSourceStack commandSourceStack, ResourceOrTagLocationArgument.Result<?> result, BlockPos blockPos, Pair<BlockPos, ? extends Holder<?>> pair, String string) {
        BlockPos blockPos2 = (BlockPos)pair.getFirst();
        String string2 = (String)result.unwrap().map(resourceKey -> resourceKey.location().toString(), tagKey -> "#" + tagKey.location() + " (" + ((Holder)pair.getSecond()).unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("[unregistered]") + ")");
        int n = Mth.floor(LocateCommand.dist(blockPos.getX(), blockPos.getZ(), blockPos2.getX(), blockPos2.getZ()));
        MutableComponent mutableComponent = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", blockPos2.getX(), "~", blockPos2.getZ())).withStyle(style -> style.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockPos2.getX() + " ~ " + blockPos2.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.coordinates.tooltip"))));
        commandSourceStack.sendSuccess(new TranslatableComponent(string, string2, mutableComponent, n), false);
        return n;
    }

    private static float dist(int n, int n2, int n3, int n4) {
        int n5 = n3 - n;
        int n6 = n4 - n2;
        return Mth.sqrt(n5 * n5 + n6 * n6);
    }
}

