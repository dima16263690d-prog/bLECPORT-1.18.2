/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.entity;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CampfireBlockEntity
extends BlockEntity
implements Clearable {
    private static final int BURN_COOL_SPEED = 2;
    private static final int NUM_SLOTS = 4;
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final int[] cookingProgress = new int[4];
    private final int[] cookingTime = new int[4];

    public CampfireBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.CAMPFIRE, blockPos, blockState);
    }

    public static void cookTick(Level level, BlockPos blockPos, BlockState blockState, CampfireBlockEntity campfireBlockEntity) {
        boolean bl = false;
        for (int i = 0; i < campfireBlockEntity.items.size(); ++i) {
            ItemStack itemStack = campfireBlockEntity.items.get(i);
            if (itemStack.isEmpty()) continue;
            bl = true;
            int n = i;
            campfireBlockEntity.cookingProgress[n] = campfireBlockEntity.cookingProgress[n] + 1;
            if (campfireBlockEntity.cookingProgress[i] < campfireBlockEntity.cookingTime[i]) continue;
            SimpleContainer simpleContainer = new SimpleContainer(itemStack);
            ItemStack itemStack2 = level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, simpleContainer, level).map(campfireCookingRecipe -> campfireCookingRecipe.assemble(simpleContainer)).orElse(itemStack);
            Containers.dropItemStack(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack2);
            campfireBlockEntity.items.set(i, ItemStack.EMPTY);
            level.sendBlockUpdated(blockPos, blockState, blockState, 3);
        }
        if (bl) {
            CampfireBlockEntity.setChanged(level, blockPos, blockState);
        }
    }

    public static void cooldownTick(Level level, BlockPos blockPos, BlockState blockState, CampfireBlockEntity campfireBlockEntity) {
        boolean bl = false;
        for (int i = 0; i < campfireBlockEntity.items.size(); ++i) {
            if (campfireBlockEntity.cookingProgress[i] <= 0) continue;
            bl = true;
            campfireBlockEntity.cookingProgress[i] = Mth.clamp(campfireBlockEntity.cookingProgress[i] - 2, 0, campfireBlockEntity.cookingTime[i]);
        }
        if (bl) {
            CampfireBlockEntity.setChanged(level, blockPos, blockState);
        }
    }

    public static void particleTick(Level level, BlockPos blockPos, BlockState blockState, CampfireBlockEntity campfireBlockEntity) {
        int n;
        Random random = level.random;
        if (random.nextFloat() < 0.11f) {
            for (n = 0; n < random.nextInt(2) + 2; ++n) {
                CampfireBlock.makeParticles(level, blockPos, blockState.getValue(CampfireBlock.SIGNAL_FIRE), false);
            }
        }
        n = blockState.getValue(CampfireBlock.FACING).get2DDataValue();
        for (int i = 0; i < campfireBlockEntity.items.size(); ++i) {
            if (campfireBlockEntity.items.get(i).isEmpty() || !(random.nextFloat() < 0.2f)) continue;
            Direction direction = Direction.from2DDataValue(Math.floorMod(i + n, 4));
            float f = 0.3125f;
            double d = (double)blockPos.getX() + 0.5 - (double)((float)direction.getStepX() * 0.3125f) + (double)((float)direction.getClockWise().getStepX() * 0.3125f);
            double d2 = (double)blockPos.getY() + 0.5;
            double d3 = (double)blockPos.getZ() + 0.5 - (double)((float)direction.getStepZ() * 0.3125f) + (double)((float)direction.getClockWise().getStepZ() * 0.3125f);
            for (int j = 0; j < 4; ++j) {
                level.addParticle(ParticleTypes.SMOKE, d, d2, d3, 0.0, 5.0E-4, 0.0);
            }
        }
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        int[] nArray;
        super.load(compoundTag);
        this.items.clear();
        ContainerHelper.loadAllItems(compoundTag, this.items);
        if (compoundTag.contains("CookingTimes", 11)) {
            nArray = compoundTag.getIntArray("CookingTimes");
            System.arraycopy(nArray, 0, this.cookingProgress, 0, Math.min(this.cookingTime.length, nArray.length));
        }
        if (compoundTag.contains("CookingTotalTimes", 11)) {
            nArray = compoundTag.getIntArray("CookingTotalTimes");
            System.arraycopy(nArray, 0, this.cookingTime, 0, Math.min(this.cookingTime.length, nArray.length));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        ContainerHelper.saveAllItems(compoundTag, this.items, true);
        compoundTag.putIntArray("CookingTimes", this.cookingProgress);
        compoundTag.putIntArray("CookingTotalTimes", this.cookingTime);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compoundTag = new CompoundTag();
        ContainerHelper.saveAllItems(compoundTag, this.items, true);
        return compoundTag;
    }

    public Optional<CampfireCookingRecipe> getCookableRecipe(ItemStack itemStack) {
        if (this.items.stream().noneMatch(ItemStack::isEmpty)) {
            return Optional.empty();
        }
        return this.level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SimpleContainer(itemStack), this.level);
    }

    public boolean placeFood(ItemStack itemStack, int n) {
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack2 = this.items.get(i);
            if (!itemStack2.isEmpty()) continue;
            this.cookingTime[i] = n;
            this.cookingProgress[i] = 0;
            this.items.set(i, itemStack.split(1));
            this.markUpdated();
            return true;
        }
        return false;
    }

    private void markUpdated() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    public void dowse() {
        if (this.level != null) {
            this.markUpdated();
        }
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }
}

