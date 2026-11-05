/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap$Builder
 */
package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public class AxeItem
extends DiggerItem {
    protected static final Map<Block, Block> STRIPPABLES = new ImmutableMap.Builder().put((Object)Blocks.OAK_WOOD, (Object)Blocks.STRIPPED_OAK_WOOD).put((Object)Blocks.OAK_LOG, (Object)Blocks.STRIPPED_OAK_LOG).put((Object)Blocks.DARK_OAK_WOOD, (Object)Blocks.STRIPPED_DARK_OAK_WOOD).put((Object)Blocks.DARK_OAK_LOG, (Object)Blocks.STRIPPED_DARK_OAK_LOG).put((Object)Blocks.ACACIA_WOOD, (Object)Blocks.STRIPPED_ACACIA_WOOD).put((Object)Blocks.ACACIA_LOG, (Object)Blocks.STRIPPED_ACACIA_LOG).put((Object)Blocks.BIRCH_WOOD, (Object)Blocks.STRIPPED_BIRCH_WOOD).put((Object)Blocks.BIRCH_LOG, (Object)Blocks.STRIPPED_BIRCH_LOG).put((Object)Blocks.JUNGLE_WOOD, (Object)Blocks.STRIPPED_JUNGLE_WOOD).put((Object)Blocks.JUNGLE_LOG, (Object)Blocks.STRIPPED_JUNGLE_LOG).put((Object)Blocks.SPRUCE_WOOD, (Object)Blocks.STRIPPED_SPRUCE_WOOD).put((Object)Blocks.SPRUCE_LOG, (Object)Blocks.STRIPPED_SPRUCE_LOG).put((Object)Blocks.WARPED_STEM, (Object)Blocks.STRIPPED_WARPED_STEM).put((Object)Blocks.WARPED_HYPHAE, (Object)Blocks.STRIPPED_WARPED_HYPHAE).put((Object)Blocks.CRIMSON_STEM, (Object)Blocks.STRIPPED_CRIMSON_STEM).put((Object)Blocks.CRIMSON_HYPHAE, (Object)Blocks.STRIPPED_CRIMSON_HYPHAE).build();

    protected AxeItem(Tier tier, float f, float f2, Item.Properties properties) {
        super(f, f2, tier, BlockTags.MINEABLE_WITH_AXE, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        Player player2 = useOnContext.getPlayer();
        BlockState blockState = level.getBlockState(blockPos);
        Optional<BlockState> optional = this.getStripped(blockState);
        Optional<BlockState> optional2 = WeatheringCopper.getPrevious(blockState);
        Optional<BlockState> optional3 = Optional.ofNullable((Block)HoneycombItem.WAX_OFF_BY_BLOCK.get().get((Object)blockState.getBlock())).map(block -> block.withPropertiesOf(blockState));
        ItemStack itemStack = useOnContext.getItemInHand();
        Optional<Object> optional4 = Optional.empty();
        if (optional.isPresent()) {
            level.playSound(player2, blockPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);
            optional4 = optional;
        } else if (optional2.isPresent()) {
            level.playSound(player2, blockPos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.levelEvent(player2, 3005, blockPos, 0);
            optional4 = optional2;
        } else if (optional3.isPresent()) {
            level.playSound(player2, blockPos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.levelEvent(player2, 3004, blockPos, 0);
            optional4 = optional3;
        }
        if (optional4.isPresent()) {
            if (player2 instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player2, blockPos, itemStack);
            }
            level.setBlock(blockPos, (BlockState)optional4.get(), 11);
            if (player2 != null) {
                itemStack.hurtAndBreak(1, player2, player -> player.broadcastBreakEvent(useOnContext.getHand()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private Optional<BlockState> getStripped(BlockState blockState) {
        return Optional.ofNullable(STRIPPABLES.get(blockState.getBlock())).map(block -> (BlockState)block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, blockState.getValue(RotatedPillarBlock.AXIS)));
    }
}

