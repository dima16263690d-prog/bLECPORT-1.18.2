/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class DefaultGameModeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)Commands.literal("defaultgamemode").requires(commandSourceStack -> commandSourceStack.hasPermission(2));
        for (GameType gameType : GameType.values()) {
            literalArgumentBuilder.then(Commands.literal(gameType.getName()).executes(commandContext -> DefaultGameModeCommands.setMode((CommandSourceStack)commandContext.getSource(), gameType)));
        }
        commandDispatcher.register(literalArgumentBuilder);
    }

    private static int setMode(CommandSourceStack commandSourceStack, GameType gameType) {
        int n = 0;
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        minecraftServer.setDefaultGameType(gameType);
        GameType gameType2 = minecraftServer.getForcedGameType();
        if (gameType2 != null) {
            for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
                if (!serverPlayer.setGameMode(gameType2)) continue;
                ++n;
            }
        }
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.defaultgamemode.success", gameType.getLongDisplayName()), true);
        return n;
    }
}

