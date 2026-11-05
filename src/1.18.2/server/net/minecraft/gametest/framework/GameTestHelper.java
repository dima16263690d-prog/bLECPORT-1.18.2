/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  javax.annotation.Nullable
 */
package net.minecraft.gametest.framework;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class GameTestHelper {
    private final GameTestInfo testInfo;
    private boolean finalCheckAdded;

    public GameTestHelper(GameTestInfo gameTestInfo) {
        this.testInfo = gameTestInfo;
    }

    public ServerLevel getLevel() {
        return this.testInfo.getLevel();
    }

    public BlockState getBlockState(BlockPos blockPos) {
        return this.getLevel().getBlockState(this.absolutePos(blockPos));
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return this.getLevel().getBlockEntity(this.absolutePos(blockPos));
    }

    public void killAllEntities() {
        AABB aABB = this.getBounds();
        List<Entity> list = this.getLevel().getEntitiesOfClass(Entity.class, aABB.inflate(1.0), entity -> !(entity instanceof Player));
        list.forEach(Entity::kill);
    }

    public ItemEntity spawnItem(Item item, float f, float f2, float f3) {
        ServerLevel serverLevel = this.getLevel();
        Vec3 vec3 = this.absoluteVec(new Vec3(f, f2, f3));
        ItemEntity itemEntity = new ItemEntity(serverLevel, vec3.x, vec3.y, vec3.z, new ItemStack(item, 1));
        itemEntity.setDeltaMovement(0.0, 0.0, 0.0);
        serverLevel.addFreshEntity(itemEntity);
        return itemEntity;
    }

    public <E extends Entity> E spawn(EntityType<E> entityType, BlockPos blockPos) {
        return this.spawn(entityType, Vec3.atBottomCenterOf(blockPos));
    }

    public <E extends Entity> E spawn(EntityType<E> entityType, Vec3 vec3) {
        ServerLevel serverLevel = this.getLevel();
        E e = entityType.create(serverLevel);
        if (e instanceof Mob) {
            ((Mob)e).setPersistenceRequired();
        }
        Vec3 vec32 = this.absoluteVec(vec3);
        ((Entity)e).moveTo(vec32.x, vec32.y, vec32.z, ((Entity)e).getYRot(), ((Entity)e).getXRot());
        serverLevel.addFreshEntity((Entity)e);
        return e;
    }

    public <E extends Entity> E spawn(EntityType<E> entityType, int n, int n2, int n3) {
        return this.spawn(entityType, new BlockPos(n, n2, n3));
    }

    public <E extends Entity> E spawn(EntityType<E> entityType, float f, float f2, float f3) {
        return this.spawn(entityType, new Vec3(f, f2, f3));
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, BlockPos blockPos) {
        Mob mob = (Mob)this.spawn(entityType, blockPos);
        mob.removeFreeWill();
        return (E)mob;
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, int n, int n2, int n3) {
        return this.spawnWithNoFreeWill(entityType, new BlockPos(n, n2, n3));
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, Vec3 vec3) {
        Mob mob = (Mob)this.spawn(entityType, vec3);
        mob.removeFreeWill();
        return (E)mob;
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, float f, float f2, float f3) {
        return this.spawnWithNoFreeWill(entityType, new Vec3(f, f2, f3));
    }

    public GameTestSequence walkTo(Mob mob, BlockPos blockPos, float f) {
        return this.startSequence().thenExecuteAfter(2, () -> {
            Path path = mob.getNavigation().createPath(this.absolutePos(blockPos), 0);
            mob.getNavigation().moveTo(path, (double)f);
        });
    }

    public void pressButton(int n, int n2, int n3) {
        this.pressButton(new BlockPos(n, n2, n3));
    }

    public void pressButton(BlockPos blockPos) {
        this.assertBlockState(blockPos, blockState -> blockState.is(BlockTags.BUTTONS), () -> "Expected button");
        BlockPos blockPos2 = this.absolutePos(blockPos);
        BlockState blockState2 = this.getLevel().getBlockState(blockPos2);
        ButtonBlock buttonBlock = (ButtonBlock)blockState2.getBlock();
        buttonBlock.press(blockState2, this.getLevel(), blockPos2);
    }

    public void useBlock(BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        BlockState blockState = this.getLevel().getBlockState(blockPos2);
        blockState.use(this.getLevel(), this.makeMockPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(blockPos2), Direction.NORTH, blockPos2, true));
    }

    public LivingEntity makeAboutToDrown(LivingEntity livingEntity) {
        livingEntity.setAirSupply(0);
        livingEntity.setHealth(0.25f);
        return livingEntity;
    }

    public Player makeMockPlayer() {
        return new Player(this.getLevel(), BlockPos.ZERO, 0.0f, new GameProfile(UUID.randomUUID(), "test-mock-player")){

            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }
        };
    }

    public void pullLever(int n, int n2, int n3) {
        this.pullLever(new BlockPos(n, n2, n3));
    }

    public void pullLever(BlockPos blockPos) {
        this.assertBlockPresent(Blocks.LEVER, blockPos);
        BlockPos blockPos2 = this.absolutePos(blockPos);
        BlockState blockState = this.getLevel().getBlockState(blockPos2);
        LeverBlock leverBlock = (LeverBlock)blockState.getBlock();
        leverBlock.pull(blockState, this.getLevel(), blockPos2);
    }

    public void pulseRedstone(BlockPos blockPos, long l) {
        this.setBlock(blockPos, Blocks.REDSTONE_BLOCK);
        this.runAfterDelay(l, () -> this.setBlock(blockPos, Blocks.AIR));
    }

    public void destroyBlock(BlockPos blockPos) {
        this.getLevel().destroyBlock(this.absolutePos(blockPos), false, null);
    }

    public void setBlock(int n, int n2, int n3, Block block) {
        this.setBlock(new BlockPos(n, n2, n3), block);
    }

    public void setBlock(int n, int n2, int n3, BlockState blockState) {
        this.setBlock(new BlockPos(n, n2, n3), blockState);
    }

    public void setBlock(BlockPos blockPos, Block block) {
        this.setBlock(blockPos, block.defaultBlockState());
    }

    public void setBlock(BlockPos blockPos, BlockState blockState) {
        this.getLevel().setBlock(this.absolutePos(blockPos), blockState, 3);
    }

    public void setNight() {
        this.setDayTime(13000);
    }

    public void setDayTime(int n) {
        this.getLevel().setDayTime(n);
    }

    public void assertBlockPresent(Block block, int n, int n2, int n3) {
        this.assertBlockPresent(block, new BlockPos(n, n2, n3));
    }

    public void assertBlockPresent(Block block, BlockPos blockPos) {
        BlockState blockState = this.getBlockState(blockPos);
        this.assertBlock(blockPos, (Block block2) -> blockState.is(block), "Expected " + block.getName().getString() + ", got " + blockState.getBlock().getName().getString());
    }

    public void assertBlockNotPresent(Block block, int n, int n2, int n3) {
        this.assertBlockNotPresent(block, new BlockPos(n, n2, n3));
    }

    public void assertBlockNotPresent(Block block, BlockPos blockPos) {
        this.assertBlock(blockPos, (Block block2) -> !this.getBlockState(blockPos).is(block), "Did not expect " + block.getName().getString());
    }

    public void succeedWhenBlockPresent(Block block, int n, int n2, int n3) {
        this.succeedWhenBlockPresent(block, new BlockPos(n, n2, n3));
    }

    public void succeedWhenBlockPresent(Block block, BlockPos blockPos) {
        this.succeedWhen(() -> this.assertBlockPresent(block, blockPos));
    }

    public void assertBlock(BlockPos blockPos, Predicate<Block> predicate, String string) {
        this.assertBlock(blockPos, predicate, () -> string);
    }

    public void assertBlock(BlockPos blockPos, Predicate<Block> predicate, Supplier<String> supplier) {
        this.assertBlockState(blockPos, blockState -> predicate.test(blockState.getBlock()), supplier);
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPos blockPos, Property<T> property, T t) {
        this.assertBlockState(blockPos, blockState -> blockState.hasProperty(property) && blockState.getValue(property).equals(t), () -> "Expected property " + property.getName() + " to be " + t);
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPos blockPos, Property<T> property, Predicate<T> predicate, String string) {
        this.assertBlockState(blockPos, blockState -> predicate.test(blockState.getValue(property)), () -> string);
    }

    public void assertBlockState(BlockPos blockPos, Predicate<BlockState> predicate, Supplier<String> supplier) {
        BlockState blockState = this.getBlockState(blockPos);
        if (!predicate.test(blockState)) {
            throw new GameTestAssertPosException(supplier.get(), this.absolutePos(blockPos), blockPos, this.testInfo.getTick());
        }
    }

    public void assertEntityPresent(EntityType<?> entityType) {
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), Entity::isAlive);
        if (list.isEmpty()) {
            throw new GameTestAssertException("Expected " + entityType.toShortString() + " to exist");
        }
    }

    public void assertEntityPresent(EntityType<?> entityType, int n, int n2, int n3) {
        this.assertEntityPresent(entityType, new BlockPos(n, n2, n3));
    }

    public void assertEntityPresent(EntityType<?> entityType, BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
        if (list.isEmpty()) {
            throw new GameTestAssertPosException("Expected " + entityType.toShortString(), blockPos2, blockPos, this.testInfo.getTick());
        }
    }

    public void assertEntityPresent(EntityType<?> entityType, BlockPos blockPos, double d) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2).inflate(d), Entity::isAlive);
        if (list.isEmpty()) {
            throw new GameTestAssertPosException("Expected " + entityType.toShortString(), blockPos2, blockPos, this.testInfo.getTick());
        }
    }

    public void assertEntityInstancePresent(Entity entity, int n, int n2, int n3) {
        this.assertEntityInstancePresent(entity, new BlockPos(n, n2, n3));
    }

    public void assertEntityInstancePresent(Entity entity, BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entity.getType(), new AABB(blockPos2), Entity::isAlive);
        list.stream().filter(entity2 -> entity2 == entity).findFirst().orElseThrow(() -> new GameTestAssertPosException("Expected " + entity.getType().toShortString(), blockPos2, blockPos, this.testInfo.getTick()));
    }

    public void assertItemEntityCountIs(Item item, BlockPos blockPos, double d, int n) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, new AABB(blockPos2).inflate(d), Entity::isAlive);
        int n2 = 0;
        for (Entity entity : list) {
            ItemEntity itemEntity = (ItemEntity)entity;
            if (!itemEntity.getItem().getItem().equals(item)) continue;
            n2 += itemEntity.getItem().getCount();
        }
        if (n2 != n) {
            throw new GameTestAssertPosException("Expected " + n + " " + item.getDescription().getString() + " items to exist (found " + n2 + ")", blockPos2, blockPos, this.testInfo.getTick());
        }
    }

    public void assertItemEntityPresent(Item item, BlockPos blockPos, double d) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, new AABB(blockPos2).inflate(d), Entity::isAlive);
        for (Entity entity : list) {
            ItemEntity itemEntity = (ItemEntity)entity;
            if (!itemEntity.getItem().getItem().equals(item)) continue;
            return;
        }
        throw new GameTestAssertPosException("Expected " + item.getDescription().getString() + " item", blockPos2, blockPos, this.testInfo.getTick());
    }

    public void assertEntityNotPresent(EntityType<?> entityType) {
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), Entity::isAlive);
        if (!list.isEmpty()) {
            throw new GameTestAssertException("Did not expect " + entityType.toShortString() + " to exist");
        }
    }

    public void assertEntityNotPresent(EntityType<?> entityType, int n, int n2, int n3) {
        this.assertEntityNotPresent(entityType, new BlockPos(n, n2, n3));
    }

    public void assertEntityNotPresent(EntityType<?> entityType, BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
        if (!list.isEmpty()) {
            throw new GameTestAssertPosException("Did not expect " + entityType.toShortString(), blockPos2, blockPos, this.testInfo.getTick());
        }
    }

    public void assertEntityTouching(EntityType<?> entityType, double d, double d2, double d3) {
        Vec3 vec3 = new Vec3(d, d2, d3);
        Vec3 vec32 = this.absoluteVec(vec3);
        Predicate<Entity> predicate = entity -> entity.getBoundingBox().intersects(vec32, vec32);
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), predicate);
        if (list.isEmpty()) {
            throw new GameTestAssertException("Expected " + entityType.toShortString() + " to touch " + vec32 + " (relative " + vec3 + ")");
        }
    }

    public void assertEntityNotTouching(EntityType<?> entityType, double d, double d2, double d3) {
        Vec3 vec3 = new Vec3(d, d2, d3);
        Vec3 vec32 = this.absoluteVec(vec3);
        Predicate<Entity> predicate = entity -> !entity.getBoundingBox().intersects(vec32, vec32);
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), predicate);
        if (list.isEmpty()) {
            throw new GameTestAssertException("Did not expect " + entityType.toShortString() + " to touch " + vec32 + " (relative " + vec3 + ")");
        }
    }

    public <E extends Entity, T> void assertEntityData(BlockPos blockPos, EntityType<E> entityType, Function<? super E, T> function, @Nullable T t) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
        if (list.isEmpty()) {
            throw new GameTestAssertPosException("Expected " + entityType.toShortString(), blockPos2, blockPos, this.testInfo.getTick());
        }
        for (Entity entity : list) {
            T t2 = function.apply(entity);
            if (!(t2 == null ? t != null : !t2.equals(t))) continue;
            throw new GameTestAssertException("Expected entity data to be: " + t + ", but was: " + t2);
        }
    }

    public void assertContainerEmpty(BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        BlockEntity blockEntity = this.getLevel().getBlockEntity(blockPos2);
        if (blockEntity instanceof BaseContainerBlockEntity && !((BaseContainerBlockEntity)blockEntity).isEmpty()) {
            throw new GameTestAssertException("Container should be empty");
        }
    }

    public void assertContainerContains(BlockPos blockPos, Item item) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        BlockEntity blockEntity = this.getLevel().getBlockEntity(blockPos2);
        if (blockEntity instanceof BaseContainerBlockEntity && ((BaseContainerBlockEntity)blockEntity).countItem(item) != 1) {
            throw new GameTestAssertException("Container should contain: " + item);
        }
    }

    public void assertSameBlockStates(BoundingBox boundingBox, BlockPos blockPos) {
        BlockPos.betweenClosedStream(boundingBox).forEach(blockPos2 -> {
            BlockPos blockPos3 = blockPos.offset(blockPos2.getX() - boundingBox.minX(), blockPos2.getY() - boundingBox.minY(), blockPos2.getZ() - boundingBox.minZ());
            this.assertSameBlockState((BlockPos)blockPos2, blockPos3);
        });
    }

    public void assertSameBlockState(BlockPos blockPos, BlockPos blockPos2) {
        BlockState blockState;
        BlockState blockState2 = this.getBlockState(blockPos);
        if (blockState2 != (blockState = this.getBlockState(blockPos2))) {
            this.fail("Incorrect state. Expected " + blockState + ", got " + blockState2, blockPos);
        }
    }

    public void assertAtTickTimeContainerContains(long l, BlockPos blockPos, Item item) {
        this.runAtTickTime(l, () -> this.assertContainerContains(blockPos, item));
    }

    public void assertAtTickTimeContainerEmpty(long l, BlockPos blockPos) {
        this.runAtTickTime(l, () -> this.assertContainerEmpty(blockPos));
    }

    public <E extends Entity, T> void succeedWhenEntityData(BlockPos blockPos, EntityType<E> entityType, Function<E, T> function, T t) {
        this.succeedWhen(() -> this.assertEntityData(blockPos, entityType, function, t));
    }

    public <E extends Entity> void assertEntityProperty(E e, Predicate<E> predicate, String string) {
        if (!predicate.test(e)) {
            throw new GameTestAssertException("Entity " + e + " failed " + string + " test");
        }
    }

    public <E extends Entity, T> void assertEntityProperty(E e, Function<E, T> function, String string, T t) {
        T t2 = function.apply(e);
        if (!t2.equals(t)) {
            throw new GameTestAssertException("Entity " + e + " value " + string + "=" + t2 + " is not equal to expected " + t);
        }
    }

    public void succeedWhenEntityPresent(EntityType<?> entityType, int n, int n2, int n3) {
        this.succeedWhenEntityPresent(entityType, new BlockPos(n, n2, n3));
    }

    public void succeedWhenEntityPresent(EntityType<?> entityType, BlockPos blockPos) {
        this.succeedWhen(() -> this.assertEntityPresent(entityType, blockPos));
    }

    public void succeedWhenEntityNotPresent(EntityType<?> entityType, int n, int n2, int n3) {
        this.succeedWhenEntityNotPresent(entityType, new BlockPos(n, n2, n3));
    }

    public void succeedWhenEntityNotPresent(EntityType<?> entityType, BlockPos blockPos) {
        this.succeedWhen(() -> this.assertEntityNotPresent(entityType, blockPos));
    }

    public void succeed() {
        this.testInfo.succeed();
    }

    private void ensureSingleFinalCheck() {
        if (this.finalCheckAdded) {
            throw new IllegalStateException("This test already has final clause");
        }
        this.finalCheckAdded = true;
    }

    public void succeedIf(Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(0L, runnable).thenSucceed();
    }

    public void succeedWhen(Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(runnable).thenSucceed();
    }

    public void succeedOnTickWhen(int n, Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(n, runnable).thenSucceed();
    }

    public void runAtTickTime(long l, Runnable runnable) {
        this.testInfo.setRunAtTickTime(l, runnable);
    }

    public void runAfterDelay(long l, Runnable runnable) {
        this.runAtTickTime(this.testInfo.getTick() + l, runnable);
    }

    public void randomTick(BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        ServerLevel serverLevel = this.getLevel();
        serverLevel.getBlockState(blockPos2).randomTick(serverLevel, blockPos2, serverLevel.random);
    }

    public void fail(String string, BlockPos blockPos) {
        throw new GameTestAssertPosException(string, this.absolutePos(blockPos), blockPos, this.getTick());
    }

    public void fail(String string, Entity entity) {
        throw new GameTestAssertPosException(string, entity.blockPosition(), this.relativePos(entity.blockPosition()), this.getTick());
    }

    public void fail(String string) {
        throw new GameTestAssertException(string);
    }

    public void failIf(Runnable runnable) {
        this.testInfo.createSequence().thenWaitUntil(runnable).thenFail(() -> new GameTestAssertException("Fail conditions met"));
    }

    public void failIfEver(Runnable runnable) {
        LongStream.range(this.testInfo.getTick(), this.testInfo.getTimeoutTicks()).forEach(l -> this.testInfo.setRunAtTickTime(l, runnable::run));
    }

    public GameTestSequence startSequence() {
        return this.testInfo.createSequence();
    }

    public BlockPos absolutePos(BlockPos blockPos) {
        BlockPos blockPos2 = this.testInfo.getStructureBlockPos();
        BlockPos blockPos3 = blockPos2.offset(blockPos);
        return StructureTemplate.transform(blockPos3, Mirror.NONE, this.testInfo.getRotation(), blockPos2);
    }

    public BlockPos relativePos(BlockPos blockPos) {
        BlockPos blockPos2 = this.testInfo.getStructureBlockPos();
        Rotation rotation = this.testInfo.getRotation().getRotated(Rotation.CLOCKWISE_180);
        BlockPos blockPos3 = StructureTemplate.transform(blockPos, Mirror.NONE, rotation, blockPos2);
        return blockPos3.subtract(blockPos2);
    }

    public Vec3 absoluteVec(Vec3 vec3) {
        Vec3 vec32 = Vec3.atLowerCornerOf(this.testInfo.getStructureBlockPos());
        return StructureTemplate.transform(vec32.add(vec3), Mirror.NONE, this.testInfo.getRotation(), this.testInfo.getStructureBlockPos());
    }

    public long getTick() {
        return this.testInfo.getTick();
    }

    private AABB getBounds() {
        return this.testInfo.getStructureBounds();
    }

    private AABB getRelativeBounds() {
        AABB aABB = this.testInfo.getStructureBounds();
        return aABB.move(BlockPos.ZERO.subtract(this.absolutePos(BlockPos.ZERO)));
    }

    public void forEveryBlockInStructure(Consumer<BlockPos> consumer) {
        AABB aABB = this.getRelativeBounds();
        BlockPos.MutableBlockPos.betweenClosedStream(aABB.move(0.0, 1.0, 0.0)).forEach(consumer);
    }

    public void onEachTick(Runnable runnable) {
        LongStream.range(this.testInfo.getTick(), this.testInfo.getTimeoutTicks()).forEach(l -> this.testInfo.setRunAtTickTime(l, runnable::run));
    }
}

