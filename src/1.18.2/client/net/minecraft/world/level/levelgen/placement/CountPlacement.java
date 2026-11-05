/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

public class CountPlacement
extends RepeatingPlacement {
    public static final Codec<CountPlacement> CODEC = IntProvider.codec(0, 256).fieldOf("count").xmap(CountPlacement::new, countPlacement -> countPlacement.count).codec();
    private final IntProvider count;

    private CountPlacement(IntProvider intProvider) {
        this.count = intProvider;
    }

    public static CountPlacement of(IntProvider intProvider) {
        return new CountPlacement(intProvider);
    }

    public static CountPlacement of(int n) {
        return CountPlacement.of(ConstantInt.of(n));
    }

    @Override
    protected int count(Random random, BlockPos blockPos) {
        return this.count.sample(random);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.COUNT;
    }
}

