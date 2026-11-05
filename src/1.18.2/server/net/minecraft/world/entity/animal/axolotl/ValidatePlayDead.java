/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

public class ValidatePlayDead
extends Behavior<Axolotl> {
    public ValidatePlayDead() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.PLAY_DEAD_TICKS, (Object)((Object)MemoryStatus.VALUE_PRESENT)));
    }

    @Override
    protected void start(ServerLevel serverLevel, Axolotl axolotl, long l) {
        Brain<Axolotl> brain = axolotl.getBrain();
        int n = brain.getMemory(MemoryModuleType.PLAY_DEAD_TICKS).get();
        if (n <= 0) {
            brain.eraseMemory(MemoryModuleType.PLAY_DEAD_TICKS);
            brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
            brain.useDefaultActivity();
        } else {
            brain.setMemory(MemoryModuleType.PLAY_DEAD_TICKS, n - 1);
        }
    }
}

