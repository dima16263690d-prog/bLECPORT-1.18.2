/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.concurrent.Executor;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EmoteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("me").then(Commands.argument("action", StringArgumentType.greedyString()).executes(commandContext -> {
            String string = StringArgumentType.getString((CommandContext)commandContext, (String)"action");
            Entity entity = ((CommandSourceStack)commandContext.getSource()).getEntity();
            MinecraftServer minecraftServer = ((CommandSourceStack)commandContext.getSource()).getServer();
            if (entity != null) {
                if (entity instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer)entity;
                    serverPlayer.getTextFilter().processStreamMessage(string).thenAcceptAsync(filteredText -> {
                        String string = filteredText.getFiltered();
                        Component component = string.isEmpty() ? null : EmoteCommands.createMessage((CommandContext<CommandSourceStack>)commandContext, string);
                        Component component2 = EmoteCommands.createMessage((CommandContext<CommandSourceStack>)commandContext, filteredText.getRaw());
                        minecraftServer.getPlayerList().broadcastMessage(component2, serverPlayer2 -> serverPlayer.shouldFilterMessageTo((ServerPlayer)serverPlayer2) ? component : component2, ChatType.CHAT, entity.getUUID());
                    }, (Executor)minecraftServer);
                    return 1;
                }
                minecraftServer.getPlayerList().broadcastMessage(EmoteCommands.createMessage((CommandContext<CommandSourceStack>)commandContext, string), ChatType.CHAT, entity.getUUID());
            } else {
                minecraftServer.getPlayerList().broadcastMessage(EmoteCommands.createMessage((CommandContext<CommandSourceStack>)commandContext, string), ChatType.SYSTEM, Util.NIL_UUID);
            }
            return 1;
        })));
    }

    private static Component createMessage(CommandContext<CommandSourceStack> commandContext, String string) {
        return new TranslatableComponent("chat.type.emote", ((CommandSourceStack)commandContext.getSource()).getDisplayName(), string);
    }
}

