/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  org.apache.commons.lang3.StringUtils
 */
package net.minecraft.world.item;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.StringUtils;

public class PlayerHeadItem
extends StandingAndWallBlockItem {
    public static final String TAG_SKULL_OWNER = "SkullOwner";

    public PlayerHeadItem(Block block, Block block2, Item.Properties properties) {
        super(block, block2, properties);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        if (itemStack.is(Items.PLAYER_HEAD) && itemStack.hasTag()) {
            CompoundTag compoundTag;
            String string = null;
            CompoundTag compoundTag2 = itemStack.getTag();
            if (compoundTag2.contains(TAG_SKULL_OWNER, 8)) {
                string = compoundTag2.getString(TAG_SKULL_OWNER);
            } else if (compoundTag2.contains(TAG_SKULL_OWNER, 10) && (compoundTag = compoundTag2.getCompound(TAG_SKULL_OWNER)).contains("Name", 8)) {
                string = compoundTag.getString("Name");
            }
            if (string != null) {
                return new TranslatableComponent(this.getDescriptionId() + ".named", string);
            }
        }
        return super.getName(itemStack);
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag compoundTag) {
        super.verifyTagAfterLoad(compoundTag);
        if (compoundTag.contains(TAG_SKULL_OWNER, 8) && !StringUtils.isBlank((CharSequence)compoundTag.getString(TAG_SKULL_OWNER))) {
            GameProfile gameProfile2 = new GameProfile(null, compoundTag.getString(TAG_SKULL_OWNER));
            SkullBlockEntity.updateGameprofile(gameProfile2, gameProfile -> compoundTag.put(TAG_SKULL_OWNER, NbtUtils.writeGameProfile(new CompoundTag(), gameProfile)));
        }
    }
}

