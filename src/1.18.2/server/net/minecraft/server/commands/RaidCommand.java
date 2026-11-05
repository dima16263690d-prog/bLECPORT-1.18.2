/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  javax.annotation.Nullable
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.raid.Raids;

public class RaidCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("raid").requires(commandSourceStack -> commandSourceStack.hasPermission(3))).then(Commands.literal("start").then(Commands.argument("omenlvl", IntegerArgumentType.integer((int)0)).executes(commandContext -> RaidCommand.start((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"omenlvl")))))).then(Commands.literal("stop").executes(commandContext -> RaidCommand.stop((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("check").executes(commandContext -> RaidCommand.check((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("sound").then(Commands.argument("type", ComponentArgument.textComponent()).executes(commandContext -> RaidCommand.playSound((CommandSourceStack)commandContext.getSource(), ComponentArgument.getComponent((CommandContext<CommandSourceStack>)commandContext, "type")))))).then(Commands.literal("spawnleader").executes(commandContext -> RaidCommand.spawnLeader((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("setomen").then(Commands.argument("level", IntegerArgumentType.integer((int)0)).executes(commandContext -> RaidCommand.setBadOmenLevel((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"level")))))).then(Commands.literal("glow").executes(commandContext -> RaidCommand.glow((CommandSourceStack)commandContext.getSource()))));
    }

    private static int glow(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        Raid raid = RaidCommand.getRaid(commandSourceStack.getPlayerOrException());
        if (raid != null) {
            Set<Raider> set = raid.getAllRaiders();
            for (Raider raider : set) {
                raider.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1000, 1));
            }
        }
        return 1;
    }

    private static int setBadOmenLevel(CommandSourceStack commandSourceStack, int n) throws CommandSyntaxException {
        Raid raid = RaidCommand.getRaid(commandSourceStack.getPlayerOrException());
        if (raid != null) {
            int n2 = raid.getMaxBadOmenLevel();
            if (n > n2) {
                commandSourceStack.sendFailure(new TextComponent("Sorry, the max bad omen level you can set is " + n2));
            } else {
                int n3 = raid.getBadOmenLevel();
                raid.setBadOmenLevel(n);
                commandSourceStack.sendSuccess(new TextComponent("Changed village's bad omen level from " + n3 + " to " + n), false);
            }
        } else {
            commandSourceStack.sendFailure(new TextComponent("No raid found here"));
        }
        return 1;
    }

    private static int spawnLeader(CommandSourceStack commandSourceStack) {
        commandSourceStack.sendSuccess(new TextComponent("Spawned a raid captain"), false);
        Raider raider = EntityType.PILLAGER.create(commandSourceStack.getLevel());
        raider.setPatrolLeader(true);
        raider.setItemSlot(EquipmentSlot.HEAD, Raid.getLeaderBannerInstance());
        raider.setPos(commandSourceStack.getPosition().x, commandSourceStack.getPosition().y, commandSourceStack.getPosition().z);
        raider.finalizeSpawn(commandSourceStack.getLevel(), commandSourceStack.getLevel().getCurrentDifficultyAt(new BlockPos(commandSourceStack.getPosition())), MobSpawnType.COMMAND, null, null);
        commandSourceStack.getLevel().addFreshEntityWithPassengers(raider);
        return 1;
    }

    private static int playSound(CommandSourceStack commandSourceStack, Component component) {
        if (component != null && component.getString().equals("local")) {
            commandSourceStack.getLevel().playSound(null, new BlockPos(commandSourceStack.getPosition().add(5.0, 0.0, 0.0)), SoundEvents.RAID_HORN, SoundSource.NEUTRAL, 2.0f, 1.0f);
        }
        return 1;
    }

    private static int start(CommandSourceStack commandSourceStack, int n) throws CommandSyntaxException {
        ServerPlayer serverPlayer = commandSourceStack.getPlayerOrException();
        BlockPos blockPos = serverPlayer.blockPosition();
        if (serverPlayer.getLevel().isRaided(blockPos)) {
            commandSourceStack.sendFailure(new TextComponent("Raid already started close by"));
            return -1;
        }
        Raids raids = serverPlayer.getLevel().getRaids();
        Raid raid = raids.createOrExtendRaid(serverPlayer);
        if (raid != null) {
            raid.setBadOmenLevel(n);
            raids.setDirty();
            commandSourceStack.sendSuccess(new TextComponent("Created a raid in your local village"), false);
        } else {
            commandSourceStack.sendFailure(new TextComponent("Failed to create a raid in your local village"));
        }
        return 1;
    }

    private static int stop(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        ServerPlayer serverPlayer = commandSourceStack.getPlayerOrException();
        BlockPos blockPos = serverPlayer.blockPosition();
        Raid raid = serverPlayer.getLevel().getRaidAt(blockPos);
        if (raid != null) {
            raid.stop();
            commandSourceStack.sendSuccess(new TextComponent("Stopped raid"), false);
            return 1;
        }
        commandSourceStack.sendFailure(new TextComponent("No raid here"));
        return -1;
    }

    private static int check(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        Raid raid = RaidCommand.getRaid(commandSourceStack.getPlayerOrException());
        if (raid != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Found a started raid! ");
            commandSourceStack.sendSuccess(new TextComponent(stringBuilder.toString()), false);
            stringBuilder = new StringBuilder();
            stringBuilder.append("Num groups spawned: ");
            stringBuilder.append(raid.getGroupsSpawned());
            stringBuilder.append(" Bad omen level: ");
            stringBuilder.append(raid.getBadOmenLevel());
            stringBuilder.append(" Num mobs: ");
            stringBuilder.append(raid.getTotalRaidersAlive());
            stringBuilder.append(" Raid health: ");
            stringBuilder.append(raid.getHealthOfLivingRaiders());
            stringBuilder.append(" / ");
            stringBuilder.append(raid.getTotalHealth());
            commandSourceStack.sendSuccess(new TextComponent(stringBuilder.toString()), false);
            return 1;
        }
        commandSourceStack.sendFailure(new TextComponent("Found no started raids"));
        return 0;
    }

    @Nullable
    private static Raid getRaid(ServerPlayer serverPlayer) {
        return serverPlayer.getLevel().getRaidAt(serverPlayer.blockPosition());
    }
}

