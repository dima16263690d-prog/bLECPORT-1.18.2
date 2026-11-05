/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public abstract class ContainerOpenersCounter {
    private static final int CHECK_TICK_DELAY = 5;
    private int openCount;

    protected abstract void onOpen(Level var1, BlockPos var2, BlockState var3);

    protected abstract void onClose(Level var1, BlockPos var2, BlockState var3);

    protected abstract void openerCountChanged(Level var1, BlockPos var2, BlockState var3, int var4, int var5);

    protected abstract boolean isOwnContainer(Player var1);

    public void incrementOpeners(Player player, Level level, BlockPos blockPos, BlockState blockState) {
        int n;
        if ((n = this.openCount++) == 0) {
            this.onOpen(level, blockPos, blockState);
            level.gameEvent((Entity)player, GameEvent.CONTAINER_OPEN, blockPos);
            ContainerOpenersCounter.scheduleRecheck(level, blockPos, blockState);
        }
        this.openerCountChanged(level, blockPos, blockState, n, this.openCount);
    }

    public void decrementOpeners(Player player, Level level, BlockPos blockPos, BlockState blockState) {
        int n = this.openCount--;
        if (this.openCount == 0) {
            this.onClose(level, blockPos, blockState);
            level.gameEvent((Entity)player, GameEvent.CONTAINER_CLOSE, blockPos);
        }
        this.openerCountChanged(level, blockPos, blockState, n, this.openCount);
    }

    private int getOpenCount(Level level, BlockPos blockPos) {
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        float f = 5.0f;
        AABB aABB = new AABB((float)n - 5.0f, (float)n2 - 5.0f, (float)n3 - 5.0f, (float)(n + 1) + 5.0f, (float)(n2 + 1) + 5.0f, (float)(n3 + 1) + 5.0f);
        return level.getEntities(EntityTypeTest.forClass(Player.class), aABB, this::isOwnContainer).size();
    }

    public void recheckOpeners(Level level, BlockPos blockPos, BlockState blockState) {
        int n = this.openCount;
        int n2 = this.getOpenCount(level, blockPos);
        if (n != n2) {
            boolean bl;
            boolean bl2 = n2 != 0;
            boolean bl3 = bl = n != 0;
            if (bl2 && !bl) {
                this.onOpen(level, blockPos, blockState);
                level.gameEvent(null, GameEvent.CONTAINER_OPEN, blockPos);
            } else if (!bl2) {
                this.onClose(level, blockPos, blockState);
                level.gameEvent(null, GameEvent.CONTAINER_CLOSE, blockPos);
            }
            this.openCount = n2;
        }
        this.openerCountChanged(level, blockPos, blockState, n, n2);
        if (n2 > 0) {
            ContainerOpenersCounter.scheduleRecheck(level, blockPos, blockState);
        }
    }

    public int getOpenerCount() {
        return this.openCount;
    }

    private static void scheduleRecheck(Level level, BlockPos blockPos, BlockState blockState) {
        level.scheduleTick(blockPos, blockState.getBlock(), 5);
    }
}

