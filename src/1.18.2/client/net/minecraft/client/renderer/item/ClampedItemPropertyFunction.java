/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface ClampedItemPropertyFunction
extends ItemPropertyFunction {
    @Override
    @Deprecated
    default public float call(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        return Mth.clamp(this.unclampedCall(itemStack, clientLevel, livingEntity, n), 0.0f, 1.0f);
    }

    public float unclampedCall(ItemStack var1, @Nullable ClientLevel var2, @Nullable LivingEntity var3, int var4);
}

