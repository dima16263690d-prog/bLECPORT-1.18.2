/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LeadItem
extends Item {
    public LeadItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Level level = useOnContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos = useOnContext.getClickedPos());
        if (blockState.is(BlockTags.FENCES)) {
            Player player = useOnContext.getPlayer();
            if (!level.isClientSide && player != null) {
                LeadItem.bindPlayerMobs(player, level, blockPos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult bindPlayerMobs(Player player, Level level, BlockPos blockPos) {
        LeashFenceKnotEntity leashFenceKnotEntity = null;
        boolean bl = false;
        double d = 7.0;
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        List<Mob> list = level.getEntitiesOfClass(Mob.class, new AABB((double)n - 7.0, (double)n2 - 7.0, (double)n3 - 7.0, (double)n + 7.0, (double)n2 + 7.0, (double)n3 + 7.0));
        for (Mob mob : list) {
            if (mob.getLeashHolder() != player) continue;
            if (leashFenceKnotEntity == null) {
                leashFenceKnotEntity = LeashFenceKnotEntity.getOrCreateKnot(level, blockPos);
                leashFenceKnotEntity.playPlacementSound();
            }
            mob.setLeashedTo(leashFenceKnotEntity, true);
            bl = true;
        }
        return bl ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}

