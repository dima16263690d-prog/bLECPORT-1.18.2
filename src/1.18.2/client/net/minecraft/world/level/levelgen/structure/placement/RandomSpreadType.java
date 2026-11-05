/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.RandomSource;

public enum RandomSpreadType implements StringRepresentable
{
    LINEAR("linear"),
    TRIANGULAR("triangular");

    private static final RandomSpreadType[] VALUES;
    public static final Codec<RandomSpreadType> CODEC;
    private final String id;

    private RandomSpreadType(String string2) {
        this.id = string2;
    }

    public static RandomSpreadType byName(String string) {
        for (RandomSpreadType randomSpreadType : VALUES) {
            if (!randomSpreadType.getSerializedName().equals(string)) continue;
            return randomSpreadType;
        }
        throw new IllegalArgumentException("Unknown Random Spread type: " + string);
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    public int evaluate(RandomSource randomSource, int n) {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case LINEAR -> randomSource.nextInt(n);
            case TRIANGULAR -> (randomSource.nextInt(n) + randomSource.nextInt(n)) / 2;
        };
    }

    static {
        VALUES = RandomSpreadType.values();
        CODEC = StringRepresentable.fromEnum(() -> VALUES, RandomSpreadType::byName);
    }
}

