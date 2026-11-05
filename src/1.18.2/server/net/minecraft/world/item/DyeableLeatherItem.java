/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface DyeableLeatherItem {
    public static final String TAG_COLOR = "color";
    public static final String TAG_DISPLAY = "display";
    public static final int DEFAULT_LEATHER_COLOR = 10511680;

    default public boolean hasCustomColor(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTagElement(TAG_DISPLAY);
        return compoundTag != null && compoundTag.contains(TAG_COLOR, 99);
    }

    default public int getColor(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTagElement(TAG_DISPLAY);
        if (compoundTag != null && compoundTag.contains(TAG_COLOR, 99)) {
            return compoundTag.getInt(TAG_COLOR);
        }
        return 10511680;
    }

    default public void clearColor(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTagElement(TAG_DISPLAY);
        if (compoundTag != null && compoundTag.contains(TAG_COLOR)) {
            compoundTag.remove(TAG_COLOR);
        }
    }

    default public void setColor(ItemStack itemStack, int n) {
        itemStack.getOrCreateTagElement(TAG_DISPLAY).putInt(TAG_COLOR, n);
    }

    public static ItemStack dyeArmor(ItemStack itemStack, List<DyeItem> list) {
        int n;
        float f;
        ItemStack itemStack2 = ItemStack.EMPTY;
        int[] nArray = new int[3];
        int n2 = 0;
        int n3 = 0;
        DyeableLeatherItem dyeableLeatherItem = null;
        Item item = itemStack.getItem();
        if (item instanceof DyeableLeatherItem) {
            dyeableLeatherItem = (DyeableLeatherItem)((Object)item);
            itemStack2 = itemStack.copy();
            itemStack2.setCount(1);
            if (dyeableLeatherItem.hasCustomColor(itemStack)) {
                int n4 = dyeableLeatherItem.getColor(itemStack2);
                float f2 = (float)(n4 >> 16 & 0xFF) / 255.0f;
                float f3 = (float)(n4 >> 8 & 0xFF) / 255.0f;
                f = (float)(n4 & 0xFF) / 255.0f;
                n2 += (int)(Math.max(f2, Math.max(f3, f)) * 255.0f);
                nArray[0] = nArray[0] + (int)(f2 * 255.0f);
                nArray[1] = nArray[1] + (int)(f3 * 255.0f);
                nArray[2] = nArray[2] + (int)(f * 255.0f);
                ++n3;
            }
            for (DyeItem dyeItem : list) {
                float[] fArray = dyeItem.getDyeColor().getTextureDiffuseColors();
                int n5 = (int)(fArray[0] * 255.0f);
                int n6 = (int)(fArray[1] * 255.0f);
                n = (int)(fArray[2] * 255.0f);
                n2 += Math.max(n5, Math.max(n6, n));
                nArray[0] = nArray[0] + n5;
                nArray[1] = nArray[1] + n6;
                nArray[2] = nArray[2] + n;
                ++n3;
            }
        }
        if (dyeableLeatherItem == null) {
            return ItemStack.EMPTY;
        }
        int n7 = nArray[0] / n3;
        int n8 = nArray[1] / n3;
        int n9 = nArray[2] / n3;
        f = (float)n2 / (float)n3;
        float f4 = Math.max(n7, Math.max(n8, n9));
        n7 = (int)((float)n7 * f / f4);
        n8 = (int)((float)n8 * f / f4);
        n9 = (int)((float)n9 * f / f4);
        n = n7;
        n = (n << 8) + n8;
        n = (n << 8) + n9;
        dyeableLeatherItem.setColor(itemStack2, n);
        return itemStack2;
    }
}

