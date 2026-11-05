/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ShieldDecorationRecipe
extends CustomRecipe {
    public ShieldDecorationRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack itemStack2 = ItemStack.EMPTY;
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack3 = craftingContainer.getItem(i);
            if (itemStack3.isEmpty()) continue;
            if (itemStack3.getItem() instanceof BannerItem) {
                if (!itemStack2.isEmpty()) {
                    return false;
                }
                itemStack2 = itemStack3;
                continue;
            }
            if (itemStack3.is(Items.SHIELD)) {
                if (!itemStack.isEmpty()) {
                    return false;
                }
                if (BlockItem.getBlockEntityData(itemStack3) != null) {
                    return false;
                }
                itemStack = itemStack3;
                continue;
            }
            return false;
        }
        return !itemStack.isEmpty() && !itemStack2.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        Object object;
        Object object2 = ItemStack.EMPTY;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            object = craftingContainer.getItem(i);
            if (((ItemStack)object).isEmpty()) continue;
            if (((ItemStack)object).getItem() instanceof BannerItem) {
                object2 = object;
                continue;
            }
            if (!((ItemStack)object).is(Items.SHIELD)) continue;
            itemStack = ((ItemStack)object).copy();
        }
        if (itemStack.isEmpty()) {
            return itemStack;
        }
        CompoundTag compoundTag = BlockItem.getBlockEntityData((ItemStack)object2);
        object = compoundTag == null ? new CompoundTag() : compoundTag.copy();
        ((CompoundTag)object).putInt("Base", ((BannerItem)((ItemStack)object2).getItem()).getColor().getId());
        BlockItem.setBlockEntityData(itemStack, BlockEntityType.BANNER, (CompoundTag)object);
        return itemStack;
    }

    @Override
    public boolean canCraftInDimensions(int n, int n2) {
        return n * n2 >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHIELD_DECORATION;
    }
}

