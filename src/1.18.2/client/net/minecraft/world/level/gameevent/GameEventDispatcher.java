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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;

public interface GameEventDispatcher {
    public static final GameEventDispatcher NOOP = new GameEventDispatcher(){

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void register(GameEventListener gameEventListener) {
        }

        @Override
        public void unregister(GameEventListener gameEventListener) {
        }

        @Override
        public void post(GameEvent gameEvent, @Nullable Entity entity, BlockPos blockPos) {
        }
    };

    public boolean isEmpty();

    public void register(GameEventListener var1);

    public void unregister(GameEventListener var1);

    public void post(GameEvent var1, @Nullable Entity var2, BlockPos var3);
}

