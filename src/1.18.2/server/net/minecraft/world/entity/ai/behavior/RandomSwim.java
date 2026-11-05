/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.phys.Vec3;

public class RandomSwim
extends RandomStroll {
    public static final int[][] XY_DISTANCE_TIERS = new int[][]{{1, 1}, {3, 3}, {5, 5}, {6, 5}, {7, 7}, {10, 7}};

    public RandomSwim(float f) {
        super(f);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        return pathfinderMob.isInWaterOrBubble();
    }

    @Override
    @Nullable
    protected Vec3 getTargetPos(PathfinderMob pathfinderMob) {
        Vec3 vec3 = null;
        Vec3 vec32 = null;
        for (int[] nArray : XY_DISTANCE_TIERS) {
            vec32 = vec3 == null ? BehaviorUtils.getRandomSwimmablePos(pathfinderMob, nArray[0], nArray[1]) : pathfinderMob.position().add(pathfinderMob.position().vectorTo(vec3).normalize().multiply(nArray[0], nArray[1], nArray[0]));
            if (vec32 == null || pathfinderMob.level.getFluidState(new BlockPos(vec32)).isEmpty()) {
                return vec3;
            }
            vec3 = vec32;
        }
        return vec32;
    }
}

