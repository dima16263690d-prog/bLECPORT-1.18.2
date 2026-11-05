/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item.alchemy;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

public class Potion {
    @Nullable
    private final String name;
    private final ImmutableList<MobEffectInstance> effects;

    public static Potion byName(String string) {
        return Registry.POTION.get(ResourceLocation.tryParse(string));
    }

    public Potion(MobEffectInstance ... mobEffectInstanceArray) {
        this((String)null, mobEffectInstanceArray);
    }

    public Potion(@Nullable String string, MobEffectInstance ... mobEffectInstanceArray) {
        this.name = string;
        this.effects = ImmutableList.copyOf((Object[])mobEffectInstanceArray);
    }

    public String getName(String string) {
        return string + (this.name == null ? Registry.POTION.getKey(this).getPath() : this.name);
    }

    public List<MobEffectInstance> getEffects() {
        return this.effects;
    }

    public boolean hasInstantEffects() {
        if (!this.effects.isEmpty()) {
            for (MobEffectInstance mobEffectInstance : this.effects) {
                if (!mobEffectInstance.getEffect().isInstantenous()) continue;
                return true;
            }
        }
        return false;
    }
}

