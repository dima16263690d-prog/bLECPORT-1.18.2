/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.ImmutableBiMap
 */
package net.minecraft.world.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class HoneycombItem
extends Item {
    public static final Supplier<BiMap<Block, Block>> WAXABLES = Suppliers.memoize(() -> ImmutableBiMap.builder().put((Object)Blocks.COPPER_BLOCK, (Object)Blocks.WAXED_COPPER_BLOCK).put((Object)Blocks.EXPOSED_COPPER, (Object)Blocks.WAXED_EXPOSED_COPPER).put((Object)Blocks.WEATHERED_COPPER, (Object)Blocks.WAXED_WEATHERED_COPPER).put((Object)Blocks.OXIDIZED_COPPER, (Object)Blocks.WAXED_OXIDIZED_COPPER).put((Object)Blocks.CUT_COPPER, (Object)Blocks.WAXED_CUT_COPPER).put((Object)Blocks.EXPOSED_CUT_COPPER, (Object)Blocks.WAXED_EXPOSED_CUT_COPPER).put((Object)Blocks.WEATHERED_CUT_COPPER, (Object)Blocks.WAXED_WEATHERED_CUT_COPPER).put((Object)Blocks.OXIDIZED_CUT_COPPER, (Object)Blocks.WAXED_OXIDIZED_CUT_COPPER).put((Object)Blocks.CUT_COPPER_SLAB, (Object)Blocks.WAXED_CUT_COPPER_SLAB).put((Object)Blocks.EXPOSED_CUT_COPPER_SLAB, (Object)Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB).put((Object)Blocks.WEATHERED_CUT_COPPER_SLAB, (Object)Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB).put((Object)Blocks.OXIDIZED_CUT_COPPER_SLAB, (Object)Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB).put((Object)Blocks.CUT_COPPER_STAIRS, (Object)Blocks.WAXED_CUT_COPPER_STAIRS).put((Object)Blocks.EXPOSED_CUT_COPPER_STAIRS, (Object)Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS).put((Object)Blocks.WEATHERED_CUT_COPPER_STAIRS, (Object)Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS).put((Object)Blocks.OXIDIZED_CUT_COPPER_STAIRS, (Object)Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS).build());
    public static final Supplier<BiMap<Block, Block>> WAX_OFF_BY_BLOCK = Suppliers.memoize(() -> WAXABLES.get().inverse());

    public HoneycombItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockState blockState2 = level.getBlockState(blockPos);
        return HoneycombItem.getWaxed(blockState2).map(blockState -> {
            Player player = useOnContext.getPlayer();
            ItemStack itemStack = useOnContext.getItemInHand();
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
            }
            itemStack.shrink(1);
            level.setBlock(blockPos, (BlockState)blockState, 11);
            level.levelEvent(player, 3003, blockPos, 0);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }).orElse(InteractionResult.PASS);
    }

    public static Optional<BlockState> getWaxed(BlockState blockState) {
        return Optional.ofNullable((Block)WAXABLES.get().get((Object)blockState.getBlock())).map(block -> block.withPropertiesOf(blockState));
    }
}

