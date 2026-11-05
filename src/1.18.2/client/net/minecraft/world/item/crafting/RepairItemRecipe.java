/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 */
package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class RepairItemRecipe
extends CustomRecipe {
    public RepairItemRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        ArrayList arrayList = Lists.newArrayList();
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack;
            ItemStack itemStack2 = craftingContainer.getItem(i);
            if (itemStack2.isEmpty()) continue;
            arrayList.add(itemStack2);
            if (arrayList.size() <= 1 || itemStack2.is((itemStack = (ItemStack)arrayList.get(0)).getItem()) && itemStack.getCount() == 1 && itemStack2.getCount() == 1 && itemStack.getItem().canBeDepleted()) continue;
            return false;
        }
        return arrayList.size() == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack itemStack;
        Object object;
        ItemStack itemStack2;
        ArrayList arrayList = Lists.newArrayList();
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            itemStack2 = craftingContainer.getItem(i);
            if (itemStack2.isEmpty()) continue;
            arrayList.add(itemStack2);
            if (arrayList.size() <= 1 || itemStack2.is(((ItemStack)(object = (ItemStack)arrayList.get(0))).getItem()) && ((ItemStack)object).getCount() == 1 && itemStack2.getCount() == 1 && ((ItemStack)object).getItem().canBeDepleted()) continue;
            return ItemStack.EMPTY;
        }
        if (arrayList.size() == 2 && (itemStack = (ItemStack)arrayList.get(0)).is((itemStack2 = (ItemStack)arrayList.get(1)).getItem()) && itemStack.getCount() == 1 && itemStack2.getCount() == 1 && itemStack.getItem().canBeDepleted()) {
            object = itemStack.getItem();
            int n = ((Item)object).getMaxDamage() - itemStack.getDamageValue();
            int n2 = ((Item)object).getMaxDamage() - itemStack2.getDamageValue();
            int n3 = n + n2 + ((Item)object).getMaxDamage() * 5 / 100;
            int n4 = ((Item)object).getMaxDamage() - n3;
            if (n4 < 0) {
                n4 = 0;
            }
            ItemStack itemStack3 = new ItemStack(itemStack.getItem());
            itemStack3.setDamageValue(n4);
            HashMap hashMap = Maps.newHashMap();
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemStack);
            Map<Enchantment, Integer> map2 = EnchantmentHelper.getEnchantments(itemStack2);
            Registry.ENCHANTMENT.stream().filter(Enchantment::isCurse).forEach(enchantment -> {
                int n = Math.max(map.getOrDefault(enchantment, 0), map2.getOrDefault(enchantment, 0));
                if (n > 0) {
                    hashMap.put(enchantment, n);
                }
            });
            if (!hashMap.isEmpty()) {
                EnchantmentHelper.setEnchantments(hashMap, itemStack3);
            }
            return itemStack3;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int n, int n2) {
        return n * n2 >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.REPAIR_ITEM;
    }
}

