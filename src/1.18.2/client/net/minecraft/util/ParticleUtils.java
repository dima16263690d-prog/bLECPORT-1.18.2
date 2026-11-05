/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ParticleUtils {
    public static void spawnParticlesOnBlockFaces(Level level, BlockPos blockPos, ParticleOptions particleOptions, UniformInt uniformInt) {
        for (Direction direction : Direction.values()) {
            int n = uniformInt.sample(level.random);
            for (int i = 0; i < n; ++i) {
                ParticleUtils.spawnParticleOnFace(level, blockPos, direction, particleOptions);
            }
        }
    }

    public static void spawnParticlesAlongAxis(Direction.Axis axis, Level level, BlockPos blockPos, double d, ParticleOptions particleOptions, UniformInt uniformInt) {
        Vec3 vec3 = Vec3.atCenterOf(blockPos);
        boolean bl = axis == Direction.Axis.X;
        boolean bl2 = axis == Direction.Axis.Y;
        boolean bl3 = axis == Direction.Axis.Z;
        int n = uniformInt.sample(level.random);
        for (int i = 0; i < n; ++i) {
            double d2 = vec3.x + Mth.nextDouble(level.random, -1.0, 1.0) * (bl ? 0.5 : d);
            double d3 = vec3.y + Mth.nextDouble(level.random, -1.0, 1.0) * (bl2 ? 0.5 : d);
            double d4 = vec3.z + Mth.nextDouble(level.random, -1.0, 1.0) * (bl3 ? 0.5 : d);
            double d5 = bl ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
            double d6 = bl2 ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
            double d7 = bl3 ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
            level.addParticle(particleOptions, d2, d3, d4, d5, d6, d7);
        }
    }

    public static void spawnParticleOnFace(Level level, BlockPos blockPos, Direction direction, ParticleOptions particleOptions) {
        Vec3 vec3 = Vec3.atCenterOf(blockPos);
        int n = direction.getStepX();
        int n2 = direction.getStepY();
        int n3 = direction.getStepZ();
        double d = vec3.x + (n == 0 ? Mth.nextDouble(level.random, -0.5, 0.5) : (double)n * 0.55);
        double d2 = vec3.y + (n2 == 0 ? Mth.nextDouble(level.random, -0.5, 0.5) : (double)n2 * 0.55);
        double d3 = vec3.z + (n3 == 0 ? Mth.nextDouble(level.random, -0.5, 0.5) : (double)n3 * 0.55);
        double d4 = n == 0 ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
        double d5 = n2 == 0 ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
        double d6 = n3 == 0 ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
        level.addParticle(particleOptions, d, d2, d3, d4, d5, d6);
    }
}

