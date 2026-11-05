/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.damagesource;

import net.minecraft.util.Mth;

public class CombatRules {
    public static final float MAX_ARMOR = 20.0f;
    public static final float ARMOR_PROTECTION_DIVIDER = 25.0f;
    public static final float BASE_ARMOR_TOUGHNESS = 2.0f;
    public static final float MIN_ARMOR_RATIO = 0.2f;
    private static final int NUM_ARMOR_ITEMS = 4;

    public static float getDamageAfterAbsorb(float f, float f2, float f3) {
        float f4 = 2.0f + f3 / 4.0f;
        float f5 = Mth.clamp(f2 - f / f4, f2 * 0.2f, 20.0f);
        return f * (1.0f - f5 / 25.0f);
    }

    public static float getDamageAfterMagicAbsorb(float f, float f2) {
        float f3 = Mth.clamp(f2, 0.0f, 20.0f);
        return f * (1.0f - f3 / 25.0f);
    }
}

