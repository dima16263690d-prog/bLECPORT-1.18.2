/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ItemCommands {
    static final Dynamic3CommandExceptionType ERROR_TARGET_NOT_A_CONTAINER = new Dynamic3CommandExceptionType((object, object2, object3) -> new TranslatableComponent("commands.item.target.not_a_container", object, object2, object3));
    private static final Dynamic3CommandExceptionType ERROR_SOURCE_NOT_A_CONTAINER = new Dynamic3CommandExceptionType((object, object2, object3) -> new TranslatableComponent("commands.item.source.not_a_container", object, object2, object3));
    static final DynamicCommandExceptionType ERROR_TARGET_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.item.target.no_such_slot", object));
    private static final DynamicCommandExceptionType ERROR_SOURCE_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.item.source.no_such_slot", object));
    private static final DynamicCommandExceptionType ERROR_TARGET_NO_CHANGES = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.item.target.no_changes", object));
    private static final Dynamic2CommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM = new Dynamic2CommandExceptionType((object, object2) -> new TranslatableComponent("commands.item.target.no_changed.known_item", object, object2));
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MODIFIER = (commandContext, suggestionsBuilder) -> {
        ItemModifierManager itemModifierManager = ((CommandSourceStack)commandContext.getSource()).getServer().getItemModifierManager();
        return SharedSuggestionProvider.suggestResource(itemModifierManager.getKeys(), suggestionsBuilder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("item").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((LiteralArgumentBuilder)Commands.literal("replace").then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("slot", SlotArgument.slot()).then(Commands.literal("with").then(((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item()).executes(commandContext -> ItemCommands.setBlockItem((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot"), ItemArgument.getItem(commandContext, "item").createItemStack(1, false)))).then(Commands.argument("count", IntegerArgumentType.integer((int)1, (int)64)).executes(commandContext -> ItemCommands.setBlockItem((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot"), ItemArgument.getItem(commandContext, "item").createItemStack(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"count"), true))))))).then(((LiteralArgumentBuilder)Commands.literal("from").then(Commands.literal("block").then(Commands.argument("source", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot()).executes(commandContext -> ItemCommands.blockToBlock((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "sourceSlot"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot")))).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes(commandContext -> ItemCommands.blockToBlock((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "sourceSlot"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot"), ResourceLocationArgument.getItemModifier((CommandContext<CommandSourceStack>)commandContext, "modifier")))))))).then(Commands.literal("entity").then(Commands.argument("source", EntityArgument.entity()).then(((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot()).executes(commandContext -> ItemCommands.entityToBlock((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "sourceSlot"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot")))).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes(commandContext -> ItemCommands.entityToBlock((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "sourceSlot"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot"), ResourceLocationArgument.getItemModifier((CommandContext<CommandSourceStack>)commandContext, "modifier")))))))))))).then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(((RequiredArgumentBuilder)Commands.argument("slot", SlotArgument.slot()).then(Commands.literal("with").then(((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item()).executes(commandContext -> ItemCommands.setEntityItem((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot"), ItemArgument.getItem(commandContext, "item").createItemStack(1, false)))).then(Commands.argument("count", IntegerArgumentType.integer((int)1, (int)64)).executes(commandContext -> ItemCommands.setEntityItem((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot"), ItemArgument.getItem(commandContext, "item").createItemStack(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"count"), true))))))).then(((LiteralArgumentBuilder)Commands.literal("from").then(Commands.literal("block").then(Commands.argument("source", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot()).executes(commandContext -> ItemCommands.blockToEntities((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "sourceSlot"), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot")))).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes(commandContext -> ItemCommands.blockToEntities((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "sourceSlot"), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot"), ResourceLocationArgument.getItemModifier((CommandContext<CommandSourceStack>)commandContext, "modifier")))))))).then(Commands.literal("entity").then(Commands.argument("source", EntityArgument.entity()).then(((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot()).executes(commandContext -> ItemCommands.entityToEntities((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "sourceSlot"), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot")))).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes(commandContext -> ItemCommands.entityToEntities((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "sourceSlot"), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot"), ResourceLocationArgument.getItemModifier((CommandContext<CommandSourceStack>)commandContext, "modifier"))))))))))))).then(((LiteralArgumentBuilder)Commands.literal("modify").then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes(commandContext -> ItemCommands.modifyBlockItem((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot"), ResourceLocationArgument.getItemModifier((CommandContext<CommandSourceStack>)commandContext, "modifier")))))))).then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes(commandContext -> ItemCommands.modifyEntityItem((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)commandContext, "slot"), ResourceLocationArgument.getItemModifier((CommandContext<CommandSourceStack>)commandContext, "modifier")))))))));
    }

    private static int modifyBlockItem(CommandSourceStack commandSourceStack, BlockPos blockPos, int n, LootItemFunction lootItemFunction) throws CommandSyntaxException {
        Container container = ItemCommands.getContainer(commandSourceStack, blockPos, ERROR_TARGET_NOT_A_CONTAINER);
        if (n < 0 || n >= container.getContainerSize()) {
            throw ERROR_TARGET_INAPPLICABLE_SLOT.create((Object)n);
        }
        ItemStack itemStack = ItemCommands.applyModifier(commandSourceStack, lootItemFunction, container.getItem(n));
        container.setItem(n, itemStack);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.block.set.success", blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack.getDisplayName()), true);
        return 1;
    }

    private static int modifyEntityItem(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, int n, LootItemFunction lootItemFunction) throws CommandSyntaxException {
        HashMap hashMap = Maps.newHashMapWithExpectedSize((int)collection.size());
        for (Entity entity : collection) {
            ItemStack itemStack;
            SlotAccess slotAccess = entity.getSlot(n);
            if (slotAccess == SlotAccess.NULL || !slotAccess.set(itemStack = ItemCommands.applyModifier(commandSourceStack, lootItemFunction, slotAccess.get().copy()))) continue;
            hashMap.put(entity, itemStack);
            if (!(entity instanceof ServerPlayer)) continue;
            ((ServerPlayer)entity).containerMenu.broadcastChanges();
        }
        if (hashMap.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES.create((Object)n);
        }
        if (hashMap.size() == 1) {
            Map.Entry entry = hashMap.entrySet().iterator().next();
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.entity.set.success.single", ((Entity)entry.getKey()).getDisplayName(), ((ItemStack)entry.getValue()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.entity.set.success.multiple", hashMap.size()), true);
        }
        return hashMap.size();
    }

    private static int setBlockItem(CommandSourceStack commandSourceStack, BlockPos blockPos, int n, ItemStack itemStack) throws CommandSyntaxException {
        Container container = ItemCommands.getContainer(commandSourceStack, blockPos, ERROR_TARGET_NOT_A_CONTAINER);
        if (n < 0 || n >= container.getContainerSize()) {
            throw ERROR_TARGET_INAPPLICABLE_SLOT.create((Object)n);
        }
        container.setItem(n, itemStack);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.block.set.success", blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack.getDisplayName()), true);
        return 1;
    }

    private static Container getContainer(CommandSourceStack commandSourceStack, BlockPos blockPos, Dynamic3CommandExceptionType dynamic3CommandExceptionType) throws CommandSyntaxException {
        BlockEntity blockEntity = commandSourceStack.getLevel().getBlockEntity(blockPos);
        if (!(blockEntity instanceof Container)) {
            throw dynamic3CommandExceptionType.create((Object)blockPos.getX(), (Object)blockPos.getY(), (Object)blockPos.getZ());
        }
        return (Container)((Object)blockEntity);
    }

    private static int setEntityItem(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, int n, ItemStack itemStack) throws CommandSyntaxException {
        ArrayList arrayList = Lists.newArrayListWithCapacity((int)collection.size());
        for (Entity entity : collection) {
            SlotAccess slotAccess = entity.getSlot(n);
            if (slotAccess == SlotAccess.NULL || !slotAccess.set(itemStack.copy())) continue;
            arrayList.add(entity);
            if (!(entity instanceof ServerPlayer)) continue;
            ((ServerPlayer)entity).containerMenu.broadcastChanges();
        }
        if (arrayList.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create((Object)itemStack.getDisplayName(), (Object)n);
        }
        if (arrayList.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.entity.set.success.single", ((Entity)arrayList.iterator().next()).getDisplayName(), itemStack.getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.entity.set.success.multiple", arrayList.size(), itemStack.getDisplayName()), true);
        }
        return arrayList.size();
    }

    private static int blockToEntities(CommandSourceStack commandSourceStack, BlockPos blockPos, int n, Collection<? extends Entity> collection, int n2) throws CommandSyntaxException {
        return ItemCommands.setEntityItem(commandSourceStack, collection, n2, ItemCommands.getBlockItem(commandSourceStack, blockPos, n));
    }

    private static int blockToEntities(CommandSourceStack commandSourceStack, BlockPos blockPos, int n, Collection<? extends Entity> collection, int n2, LootItemFunction lootItemFunction) throws CommandSyntaxException {
        return ItemCommands.setEntityItem(commandSourceStack, collection, n2, ItemCommands.applyModifier(commandSourceStack, lootItemFunction, ItemCommands.getBlockItem(commandSourceStack, blockPos, n)));
    }

    private static int blockToBlock(CommandSourceStack commandSourceStack, BlockPos blockPos, int n, BlockPos blockPos2, int n2) throws CommandSyntaxException {
        return ItemCommands.setBlockItem(commandSourceStack, blockPos2, n2, ItemCommands.getBlockItem(commandSourceStack, blockPos, n));
    }

    private static int blockToBlock(CommandSourceStack commandSourceStack, BlockPos blockPos, int n, BlockPos blockPos2, int n2, LootItemFunction lootItemFunction) throws CommandSyntaxException {
        return ItemCommands.setBlockItem(commandSourceStack, blockPos2, n2, ItemCommands.applyModifier(commandSourceStack, lootItemFunction, ItemCommands.getBlockItem(commandSourceStack, blockPos, n)));
    }

    private static int entityToBlock(CommandSourceStack commandSourceStack, Entity entity, int n, BlockPos blockPos, int n2) throws CommandSyntaxException {
        return ItemCommands.setBlockItem(commandSourceStack, blockPos, n2, ItemCommands.getEntityItem(entity, n));
    }

    private static int entityToBlock(CommandSourceStack commandSourceStack, Entity entity, int n, BlockPos blockPos, int n2, LootItemFunction lootItemFunction) throws CommandSyntaxException {
        return ItemCommands.setBlockItem(commandSourceStack, blockPos, n2, ItemCommands.applyModifier(commandSourceStack, lootItemFunction, ItemCommands.getEntityItem(entity, n)));
    }

    private static int entityToEntities(CommandSourceStack commandSourceStack, Entity entity, int n, Collection<? extends Entity> collection, int n2) throws CommandSyntaxException {
        return ItemCommands.setEntityItem(commandSourceStack, collection, n2, ItemCommands.getEntityItem(entity, n));
    }

    private static int entityToEntities(CommandSourceStack commandSourceStack, Entity entity, int n, Collection<? extends Entity> collection, int n2, LootItemFunction lootItemFunction) throws CommandSyntaxException {
        return ItemCommands.setEntityItem(commandSourceStack, collection, n2, ItemCommands.applyModifier(commandSourceStack, lootItemFunction, ItemCommands.getEntityItem(entity, n)));
    }

    private static ItemStack applyModifier(CommandSourceStack commandSourceStack, LootItemFunction lootItemFunction, ItemStack itemStack) {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        LootContext.Builder builder = new LootContext.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, commandSourceStack.getPosition()).withOptionalParameter(LootContextParams.THIS_ENTITY, commandSourceStack.getEntity());
        return (ItemStack)lootItemFunction.apply(itemStack, builder.create(LootContextParamSets.COMMAND));
    }

    private static ItemStack getEntityItem(Entity entity, int n) throws CommandSyntaxException {
        SlotAccess slotAccess = entity.getSlot(n);
        if (slotAccess == SlotAccess.NULL) {
            throw ERROR_SOURCE_INAPPLICABLE_SLOT.create((Object)n);
        }
        return slotAccess.get().copy();
    }

    private static ItemStack getBlockItem(CommandSourceStack commandSourceStack, BlockPos blockPos, int n) throws CommandSyntaxException {
        Container container = ItemCommands.getContainer(commandSourceStack, blockPos, ERROR_SOURCE_NOT_A_CONTAINER);
        if (n < 0 || n >= container.getContainerSize()) {
            throw ERROR_SOURCE_INAPPLICABLE_SLOT.create((Object)n);
        }
        return container.getItem(n).copy();
    }
}

