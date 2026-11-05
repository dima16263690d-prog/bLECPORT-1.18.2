/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory.tooltip;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class BundleTooltip
implements TooltipComponent {
    private final NonNullList<ItemStack> items;
    private final int weight;

    public BundleTooltip(NonNullList<ItemStack> nonNullList, int n) {
        this.items = nonNullList;
        this.weight = n;
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public int getWeight() {
        return this.weight;
    }
}

