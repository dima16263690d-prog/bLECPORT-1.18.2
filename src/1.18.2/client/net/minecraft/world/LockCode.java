/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.concurrent.Immutable
 */
package net.minecraft.world;

import javax.annotation.concurrent.Immutable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@Immutable
public class LockCode {
    public static final LockCode NO_LOCK = new LockCode("");
    public static final String TAG_LOCK = "Lock";
    private final String key;

    public LockCode(String string) {
        this.key = string;
    }

    public boolean unlocksWith(ItemStack itemStack) {
        return this.key.isEmpty() || !itemStack.isEmpty() && itemStack.hasCustomHoverName() && this.key.equals(itemStack.getHoverName().getString());
    }

    public void addToTag(CompoundTag compoundTag) {
        if (!this.key.isEmpty()) {
            compoundTag.putString(TAG_LOCK, this.key);
        }
    }

    public static LockCode fromTag(CompoundTag compoundTag) {
        if (compoundTag.contains(TAG_LOCK, 8)) {
            return new LockCode(compoundTag.getString(TAG_LOCK));
        }
        return NO_LOCK;
    }
}

