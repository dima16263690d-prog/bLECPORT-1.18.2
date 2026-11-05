/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class WaterWalkerEnchantment
extends Enchantment {
    public WaterWalkerEnchantment(Enchantment.Rarity rarity, EquipmentSlot ... equipmentSlotArray) {
        super(rarity, EnchantmentCategory.ARMOR_FEET, equipmentSlotArray);
    }

    @Override
    public int getMinCost(int n) {
        return n * 10;
    }

    @Override
    public int getMaxCost(int n) {
        return this.getMinCost(n) + 15;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.FROST_WALKER;
    }
}

