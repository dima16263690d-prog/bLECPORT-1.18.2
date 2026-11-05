/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantmentTableBlockEntity
extends BlockEntity
implements Nameable {
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final Random RANDOM = new Random();
    private Component name;

    public EnchantmentTableBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.ENCHANTING_TABLE, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        if (this.hasCustomName()) {
            compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        if (compoundTag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
        }
    }

    public static void bookAnimationTick(Level level, BlockPos blockPos, BlockState blockState, EnchantmentTableBlockEntity enchantmentTableBlockEntity) {
        float f;
        enchantmentTableBlockEntity.oOpen = enchantmentTableBlockEntity.open;
        enchantmentTableBlockEntity.oRot = enchantmentTableBlockEntity.rot;
        Player player = level.getNearestPlayer((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 3.0, false);
        if (player != null) {
            double d = player.getX() - ((double)blockPos.getX() + 0.5);
            double d2 = player.getZ() - ((double)blockPos.getZ() + 0.5);
            enchantmentTableBlockEntity.tRot = (float)Mth.atan2(d2, d);
            enchantmentTableBlockEntity.open += 0.1f;
            if (enchantmentTableBlockEntity.open < 0.5f || RANDOM.nextInt(40) == 0) {
                float f2 = enchantmentTableBlockEntity.flipT;
                do {
                    enchantmentTableBlockEntity.flipT += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while (f2 == enchantmentTableBlockEntity.flipT);
            }
        } else {
            enchantmentTableBlockEntity.tRot += 0.02f;
            enchantmentTableBlockEntity.open -= 0.1f;
        }
        while (enchantmentTableBlockEntity.rot >= (float)Math.PI) {
            enchantmentTableBlockEntity.rot -= (float)Math.PI * 2;
        }
        while (enchantmentTableBlockEntity.rot < (float)(-Math.PI)) {
            enchantmentTableBlockEntity.rot += (float)Math.PI * 2;
        }
        while (enchantmentTableBlockEntity.tRot >= (float)Math.PI) {
            enchantmentTableBlockEntity.tRot -= (float)Math.PI * 2;
        }
        while (enchantmentTableBlockEntity.tRot < (float)(-Math.PI)) {
            enchantmentTableBlockEntity.tRot += (float)Math.PI * 2;
        }
        for (f = enchantmentTableBlockEntity.tRot - enchantmentTableBlockEntity.rot; f >= (float)Math.PI; f -= (float)Math.PI * 2) {
        }
        while (f < (float)(-Math.PI)) {
            f += (float)Math.PI * 2;
        }
        enchantmentTableBlockEntity.rot += f * 0.4f;
        enchantmentTableBlockEntity.open = Mth.clamp(enchantmentTableBlockEntity.open, 0.0f, 1.0f);
        ++enchantmentTableBlockEntity.time;
        enchantmentTableBlockEntity.oFlip = enchantmentTableBlockEntity.flip;
        float f3 = (enchantmentTableBlockEntity.flipT - enchantmentTableBlockEntity.flip) * 0.4f;
        float f4 = 0.2f;
        f3 = Mth.clamp(f3, -0.2f, 0.2f);
        enchantmentTableBlockEntity.flipA += (f3 - enchantmentTableBlockEntity.flipA) * 0.9f;
        enchantmentTableBlockEntity.flip += enchantmentTableBlockEntity.flipA;
    }

    @Override
    public Component getName() {
        if (this.name != null) {
            return this.name;
        }
        return new TranslatableComponent("container.enchant");
    }

    public void setCustomName(@Nullable Component component) {
        this.name = component;
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return this.name;
    }
}

