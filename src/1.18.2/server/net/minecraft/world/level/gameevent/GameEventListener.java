/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.gameevent;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;

public interface GameEventListener {
    public PositionSource getListenerSource();

    public int getListenerRadius();

    public boolean handleGameEvent(Level var1, GameEvent var2, @Nullable Entity var3, BlockPos var4);
}

