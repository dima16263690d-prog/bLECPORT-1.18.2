/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class AdventureModeCheck {
    public static final BlockPredicateArgument PREDICATE_PARSER = BlockPredicateArgument.blockPredicate();
    private final String tagName;
    @Nullable
    private BlockInWorld lastCheckedBlock;
    private boolean lastResult;
    private boolean checksBlockEntity;

    public AdventureModeCheck(String string) {
        this.tagName = string;
    }

    private static boolean areSameBlocks(BlockInWorld blockInWorld, @Nullable BlockInWorld blockInWorld2, boolean bl) {
        if (blockInWorld2 == null || blockInWorld.getState() != blockInWorld2.getState()) {
            return false;
        }
        if (!bl) {
            return true;
        }
        if (blockInWorld.getEntity() == null && blockInWorld2.getEntity() == null) {
            return true;
        }
        if (blockInWorld.getEntity() == null || blockInWorld2.getEntity() == null) {
            return false;
        }
        return Objects.equals(blockInWorld.getEntity().saveWithId(), blockInWorld2.getEntity().saveWithId());
    }

    public boolean test(ItemStack itemStack, Registry<Block> registry, BlockInWorld blockInWorld) {
        if (AdventureModeCheck.areSameBlocks(blockInWorld, this.lastCheckedBlock, this.checksBlockEntity)) {
            return this.lastResult;
        }
        this.lastCheckedBlock = blockInWorld;
        this.checksBlockEntity = false;
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null && compoundTag.contains(this.tagName, 9)) {
            ListTag listTag = compoundTag.getList(this.tagName, 8);
            for (int i = 0; i < listTag.size(); ++i) {
                String string = listTag.getString(i);
                try {
                    BlockPredicateArgument.Result result = PREDICATE_PARSER.parse(new StringReader(string));
                    this.checksBlockEntity |= result.requiresNbt();
                    Predicate<BlockInWorld> predicate = result.create(registry);
                    if (predicate.test(blockInWorld)) {
                        this.lastResult = true;
                        return true;
                    }
                    continue;
                }
                catch (CommandSyntaxException commandSyntaxException) {
                    // empty catch block
                }
            }
        }
        this.lastResult = false;
        return false;
    }
}

