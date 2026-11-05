/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.longs.LongSets
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddVibrationSignalPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListenerRegistrar;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.LevelTicks;
import org.slf4j.Logger;

public class ServerLevel
extends Level
implements WorldGenLevel {
    public static final BlockPos END_SPAWN_POINT = new BlockPos(100, 50, 0);
    private static final int MIN_RAIN_DELAY_TIME = 12000;
    private static final int MAX_RAIN_DELAY_TIME = 180000;
    private static final int MIN_RAIN_TIME = 12000;
    private static final int MAX_RAIN_TIME = 24000;
    private static final int MIN_THUNDER_DELAY_TIME = 12000;
    private static final int MAX_THUNDER_DELAY_TIME = 180000;
    private static final int MIN_THUNDER_TIME = 3600;
    private static final int MAX_THUNDER_TIME = 15600;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int EMPTY_TIME_NO_TICK = 300;
    private static final int MAX_SCHEDULED_TICKS_PER_TICK = 65536;
    final List<ServerPlayer> players = Lists.newArrayList();
    private final ServerChunkCache chunkSource;
    private final MinecraftServer server;
    private final ServerLevelData serverLevelData;
    final EntityTickList entityTickList = new EntityTickList();
    private final PersistentEntitySectionManager<Entity> entityManager;
    public boolean noSave;
    private final SleepStatus sleepStatus;
    private int emptyTime;
    private final PortalForcer portalForcer;
    private final LevelTicks<Block> blockTicks = new LevelTicks(this::isPositionTickingWithEntitiesLoaded, this.getProfilerSupplier());
    private final LevelTicks<Fluid> fluidTicks = new LevelTicks(this::isPositionTickingWithEntitiesLoaded, this.getProfilerSupplier());
    final Set<Mob> navigatingMobs = new ObjectOpenHashSet();
    volatile boolean isUpdatingNavigations;
    protected final Raids raids;
    private final ObjectLinkedOpenHashSet<BlockEventData> blockEvents = new ObjectLinkedOpenHashSet();
    private final List<BlockEventData> blockEventsToReschedule = new ArrayList<BlockEventData>(64);
    private boolean handlingTick;
    private final List<CustomSpawner> customSpawners;
    @Nullable
    private final EndDragonFight dragonFight;
    final Int2ObjectMap<EnderDragonPart> dragonParts = new Int2ObjectOpenHashMap();
    private final StructureFeatureManager structureFeatureManager;
    private final StructureCheck structureCheck;
    private final boolean tickTime;

    public ServerLevel(MinecraftServer minecraftServer, Executor executor, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, ChunkProgressListener chunkProgressListener, ChunkGenerator chunkGenerator, boolean bl, long l, List<CustomSpawner> list, boolean bl2) {
        super(serverLevelData, resourceKey, holder, minecraftServer::getProfiler, false, bl, l);
        this.tickTime = bl2;
        this.server = minecraftServer;
        this.customSpawners = list;
        this.serverLevelData = serverLevelData;
        chunkGenerator.ensureStructuresGenerated();
        boolean bl3 = minecraftServer.forceSynchronousWrites();
        DataFixer dataFixer = minecraftServer.getFixerUpper();
        EntityStorage entityStorage = new EntityStorage(this, levelStorageAccess.getDimensionPath(resourceKey).resolve("entities"), dataFixer, bl3, minecraftServer);
        this.entityManager = new PersistentEntitySectionManager<Entity>(Entity.class, new EntityCallbacks(), entityStorage);
        this.chunkSource = new ServerChunkCache(this, levelStorageAccess, dataFixer, minecraftServer.getStructureManager(), executor, chunkGenerator, minecraftServer.getPlayerList().getViewDistance(), minecraftServer.getPlayerList().getSimulationDistance(), bl3, chunkProgressListener, this.entityManager::updateChunkStatus, () -> minecraftServer.overworld().getDataStorage());
        this.portalForcer = new PortalForcer(this);
        this.updateSkyBrightness();
        this.prepareWeather();
        this.getWorldBorder().setAbsoluteMaxSize(minecraftServer.getAbsoluteMaxWorldSize());
        this.raids = this.getDataStorage().computeIfAbsent(compoundTag -> Raids.load(this, compoundTag), () -> new Raids(this), Raids.getFileId(this.dimensionTypeRegistration()));
        if (!minecraftServer.isSingleplayer()) {
            serverLevelData.setGameType(minecraftServer.getDefaultGameType());
        }
        long l2 = minecraftServer.getWorldData().worldGenSettings().seed();
        this.structureCheck = new StructureCheck(this.chunkSource.chunkScanner(), this.registryAccess(), minecraftServer.getStructureManager(), resourceKey, chunkGenerator, this, chunkGenerator.getBiomeSource(), l2, dataFixer);
        this.structureFeatureManager = new StructureFeatureManager(this, minecraftServer.getWorldData().worldGenSettings(), this.structureCheck);
        this.dragonFight = this.dimensionType().createDragonFight() ? new EndDragonFight(this, l2, minecraftServer.getWorldData().endDragonFightData()) : null;
        this.sleepStatus = new SleepStatus();
    }

    public void setWeatherParameters(int n, int n2, boolean bl, boolean bl2) {
        this.serverLevelData.setClearWeatherTime(n);
        this.serverLevelData.setRainTime(n2);
        this.serverLevelData.setThunderTime(n2);
        this.serverLevelData.setRaining(bl);
        this.serverLevelData.setThundering(bl2);
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int n, int n2, int n3) {
        return this.getChunkSource().getGenerator().getNoiseBiome(n, n2, n3);
    }

    public StructureFeatureManager structureFeatureManager() {
        return this.structureFeatureManager;
    }

    public void tick(BooleanSupplier booleanSupplier) {
        boolean bl;
        long l;
        ProfilerFiller profilerFiller = this.getProfiler();
        this.handlingTick = true;
        profilerFiller.push("world border");
        this.getWorldBorder().tick();
        profilerFiller.popPush("weather");
        this.advanceWeatherCycle();
        int n = this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
        if (this.sleepStatus.areEnoughSleeping(n) && this.sleepStatus.areEnoughDeepSleeping(n, this.players)) {
            if (this.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                l = this.levelData.getDayTime() + 24000L;
                this.setDayTime(l - l % 24000L);
            }
            this.wakeUpAllPlayers();
            if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && this.isRaining()) {
                this.resetWeatherCycle();
            }
        }
        this.updateSkyBrightness();
        this.tickTime();
        profilerFiller.popPush("tickPending");
        if (!this.isDebug()) {
            l = this.getGameTime();
            profilerFiller.push("blockTicks");
            this.blockTicks.tick(l, 65536, this::tickBlock);
            profilerFiller.popPush("fluidTicks");
            this.fluidTicks.tick(l, 65536, this::tickFluid);
            profilerFiller.pop();
        }
        profilerFiller.popPush("raid");
        this.raids.tick();
        profilerFiller.popPush("chunkSource");
        this.getChunkSource().tick(booleanSupplier, true);
        profilerFiller.popPush("blockEvents");
        this.runBlockEvents();
        this.handlingTick = false;
        profilerFiller.pop();
        boolean bl2 = bl = !this.players.isEmpty() || !this.getForcedChunks().isEmpty();
        if (bl) {
            this.resetEmptyTime();
        }
        if (bl || this.emptyTime++ < 300) {
            profilerFiller.push("entities");
            if (this.dragonFight != null) {
                profilerFiller.push("dragonFight");
                this.dragonFight.tick();
                profilerFiller.pop();
            }
            this.entityTickList.forEach(entity -> {
                if (entity.isRemoved()) {
                    return;
                }
                if (this.shouldDiscardEntity((Entity)entity)) {
                    entity.discard();
                    return;
                }
                profilerFiller.push("checkDespawn");
                entity.checkDespawn();
                profilerFiller.pop();
                if (!this.chunkSource.chunkMap.getDistanceManager().inEntityTickingRange(entity.chunkPosition().toLong())) {
                    return;
                }
                Entity entity2 = entity.getVehicle();
                if (entity2 != null) {
                    if (entity2.isRemoved() || !entity2.hasPassenger((Entity)entity)) {
                        entity.stopRiding();
                    } else {
                        return;
                    }
                }
                profilerFiller.push("tick");
                this.guardEntityTick(this::tickNonPassenger, entity);
                profilerFiller.pop();
            });
            profilerFiller.pop();
            this.tickBlockEntities();
        }
        profilerFiller.push("entityManagement");
        this.entityManager.tick();
        profilerFiller.pop();
    }

    @Override
    public boolean shouldTickBlocksAt(long l) {
        return this.chunkSource.chunkMap.getDistanceManager().inBlockTickingRange(l);
    }

    protected void tickTime() {
        if (!this.tickTime) {
            return;
        }
        long l = this.levelData.getGameTime() + 1L;
        this.serverLevelData.setGameTime(l);
        this.serverLevelData.getScheduledEvents().tick(this.server, l);
        if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            this.setDayTime(this.levelData.getDayTime() + 1L);
        }
    }

    public void setDayTime(long l) {
        this.serverLevelData.setDayTime(l);
    }

    public void tickCustomSpawners(boolean bl, boolean bl2) {
        for (CustomSpawner customSpawner : this.customSpawners) {
            customSpawner.tick(this, bl, bl2);
        }
    }

    private boolean shouldDiscardEntity(Entity entity) {
        if (!this.server.isSpawningAnimals() && (entity instanceof Animal || entity instanceof WaterAnimal)) {
            return true;
        }
        return !this.server.areNpcsEnabled() && entity instanceof Npc;
    }

    private void wakeUpAllPlayers() {
        this.sleepStatus.removeAllSleepers();
        this.players.stream().filter(LivingEntity::isSleeping).collect(Collectors.toList()).forEach(serverPlayer -> serverPlayer.stopSleepInBed(false, false));
    }

    public void tickChunk(LevelChunk levelChunk, int n) {
        Object object2;
        Object object3;
        ChunkPos chunkPos = levelChunk.getPos();
        boolean bl = this.isRaining();
        int n2 = chunkPos.getMinBlockX();
        int n3 = chunkPos.getMinBlockZ();
        ProfilerFiller profilerFiller = this.getProfiler();
        profilerFiller.push("thunder");
        if (bl && this.isThundering() && this.random.nextInt(100000) == 0 && this.isRainingAt((BlockPos)(object3 = this.findLightningTargetAround(this.getBlockRandomPos(n2, 0, n3, 15))))) {
            int n4;
            object2 = this.getCurrentDifficultyAt((BlockPos)object3);
            int n5 = n4 = this.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && this.random.nextDouble() < (double)((DifficultyInstance)object2).getEffectiveDifficulty() * 0.01 && !this.getBlockState(((BlockPos)object3).below()).is(Blocks.LIGHTNING_ROD) ? 1 : 0;
            if (n4) {
                SkeletonHorse object4 = EntityType.SKELETON_HORSE.create(this);
                object4.setTrap(true);
                object4.setAge(0);
                object4.setPos(((Vec3i)object3).getX(), ((Vec3i)object3).getY(), ((Vec3i)object3).getZ());
                this.addFreshEntity(object4);
            }
            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(this);
            lightningBolt.moveTo(Vec3.atBottomCenterOf((Vec3i)object3));
            lightningBolt.setVisualOnly(n4 != 0);
            this.addFreshEntity(lightningBolt);
        }
        profilerFiller.popPush("iceandsnow");
        if (this.random.nextInt(16) == 0) {
            object3 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.getBlockRandomPos(n2, 0, n3, 15));
            object2 = ((BlockPos)object3).below();
            Biome biome = this.getBiome((BlockPos)object3).value();
            if (biome.shouldFreeze(this, (BlockPos)object2)) {
                this.setBlockAndUpdate((BlockPos)object2, Blocks.ICE.defaultBlockState());
            }
            if (bl) {
                if (biome.shouldSnow(this, (BlockPos)object3)) {
                    this.setBlockAndUpdate((BlockPos)object3, Blocks.SNOW.defaultBlockState());
                }
                BlockState blockState = this.getBlockState((BlockPos)object2);
                Biome.Precipitation precipitation = biome.getPrecipitation();
                if (precipitation == Biome.Precipitation.RAIN && biome.coldEnoughToSnow((BlockPos)object2)) {
                    precipitation = Biome.Precipitation.SNOW;
                }
                blockState.getBlock().handlePrecipitation(blockState, this, (BlockPos)object2, precipitation);
            }
        }
        profilerFiller.popPush("tickBlocks");
        if (n > 0) {
            for (LevelChunkSection levelChunkSection : levelChunk.getSections()) {
                if (!levelChunkSection.isRandomlyTicking()) continue;
                int n6 = levelChunkSection.bottomBlockY();
                for (int i = 0; i < n; ++i) {
                    FluidState fluidState;
                    BlockPos blockPos = this.getBlockRandomPos(n2, n6, n3, 15);
                    profilerFiller.push("randomTick");
                    BlockState blockState = levelChunkSection.getBlockState(blockPos.getX() - n2, blockPos.getY() - n6, blockPos.getZ() - n3);
                    if (blockState.isRandomlyTicking()) {
                        blockState.randomTick(this, blockPos, this.random);
                    }
                    if ((fluidState = blockState.getFluidState()).isRandomlyTicking()) {
                        fluidState.randomTick(this, blockPos, this.random);
                    }
                    profilerFiller.pop();
                }
            }
        }
        profilerFiller.pop();
    }

    private Optional<BlockPos> findLightningRod(BlockPos blockPos2) {
        Optional<BlockPos> optional = this.getPoiManager().findClosest(poiType -> poiType == PoiType.LIGHTNING_ROD, blockPos -> blockPos.getY() == this.getLevel().getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.getX(), blockPos.getZ()) - 1, blockPos2, 128, PoiManager.Occupancy.ANY);
        return optional.map(blockPos -> blockPos.above(1));
    }

    protected BlockPos findLightningTargetAround(BlockPos blockPos) {
        BlockPos blockPos2 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos);
        Optional<BlockPos> optional = this.findLightningRod(blockPos2);
        if (optional.isPresent()) {
            return optional.get();
        }
        AABB aABB = new AABB(blockPos2, new BlockPos(blockPos2.getX(), this.getMaxBuildHeight(), blockPos2.getZ())).inflate(3.0);
        List<LivingEntity> list = this.getEntitiesOfClass(LivingEntity.class, aABB, livingEntity -> livingEntity != null && livingEntity.isAlive() && this.canSeeSky(livingEntity.blockPosition()));
        if (!list.isEmpty()) {
            return list.get(this.random.nextInt(list.size())).blockPosition();
        }
        if (blockPos2.getY() == this.getMinBuildHeight() - 1) {
            blockPos2 = blockPos2.above(2);
        }
        return blockPos2;
    }

    public boolean isHandlingTick() {
        return this.handlingTick;
    }

    public boolean canSleepThroughNights() {
        return this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE) <= 100;
    }

    private void announceSleepStatus() {
        if (!this.canSleepThroughNights()) {
            return;
        }
        if (this.getServer().isSingleplayer() && !this.getServer().isPublished()) {
            return;
        }
        int n = this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
        TranslatableComponent translatableComponent = this.sleepStatus.areEnoughSleeping(n) ? new TranslatableComponent("sleep.skipping_night") : new TranslatableComponent("sleep.players_sleeping", this.sleepStatus.amountSleeping(), this.sleepStatus.sleepersNeeded(n));
        for (ServerPlayer serverPlayer : this.players) {
            serverPlayer.displayClientMessage(translatableComponent, true);
        }
    }

    public void updateSleepingPlayerList() {
        if (!this.players.isEmpty() && this.sleepStatus.update(this.players)) {
            this.announceSleepStatus();
        }
    }

    @Override
    public ServerScoreboard getScoreboard() {
        return this.server.getScoreboard();
    }

    private void advanceWeatherCycle() {
        boolean bl = this.isRaining();
        if (this.dimensionType().hasSkyLight()) {
            if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
                int n = this.serverLevelData.getClearWeatherTime();
                int n2 = this.serverLevelData.getThunderTime();
                int n3 = this.serverLevelData.getRainTime();
                boolean bl2 = this.levelData.isThundering();
                boolean bl3 = this.levelData.isRaining();
                if (n > 0) {
                    --n;
                    n2 = bl2 ? 0 : 1;
                    n3 = bl3 ? 0 : 1;
                    bl2 = false;
                    bl3 = false;
                } else {
                    if (n2 > 0) {
                        if (--n2 == 0) {
                            bl2 = !bl2;
                        }
                    } else {
                        n2 = bl2 ? Mth.randomBetweenInclusive(this.random, 3600, 15600) : Mth.randomBetweenInclusive(this.random, 12000, 180000);
                    }
                    if (n3 > 0) {
                        if (--n3 == 0) {
                            bl3 = !bl3;
                        }
                    } else {
                        n3 = bl3 ? Mth.randomBetweenInclusive(this.random, 12000, 24000) : Mth.randomBetweenInclusive(this.random, 12000, 180000);
                    }
                }
                this.serverLevelData.setThunderTime(n2);
                this.serverLevelData.setRainTime(n3);
                this.serverLevelData.setClearWeatherTime(n);
                this.serverLevelData.setThundering(bl2);
                this.serverLevelData.setRaining(bl3);
            }
            this.oThunderLevel = this.thunderLevel;
            this.thunderLevel = this.levelData.isThundering() ? (this.thunderLevel += 0.01f) : (this.thunderLevel -= 0.01f);
            this.thunderLevel = Mth.clamp(this.thunderLevel, 0.0f, 1.0f);
            this.oRainLevel = this.rainLevel;
            this.rainLevel = this.levelData.isRaining() ? (this.rainLevel += 0.01f) : (this.rainLevel -= 0.01f);
            this.rainLevel = Mth.clamp(this.rainLevel, 0.0f, 1.0f);
        }
        if (this.oRainLevel != this.rainLevel) {
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel), this.dimension());
        }
        if (this.oThunderLevel != this.thunderLevel) {
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel), this.dimension());
        }
        if (bl != this.isRaining()) {
            if (bl) {
                this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0f));
            } else {
                this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0f));
            }
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel));
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel));
        }
    }

    private void resetWeatherCycle() {
        this.serverLevelData.setRainTime(0);
        this.serverLevelData.setRaining(false);
        this.serverLevelData.setThunderTime(0);
        this.serverLevelData.setThundering(false);
    }

    public void resetEmptyTime() {
        this.emptyTime = 0;
    }

    private void tickFluid(BlockPos blockPos, Fluid fluid) {
        FluidState fluidState = this.getFluidState(blockPos);
        if (fluidState.is(fluid)) {
            fluidState.tick(this, blockPos);
        }
    }

    private void tickBlock(BlockPos blockPos, Block block) {
        BlockState blockState = this.getBlockState(blockPos);
        if (blockState.is(block)) {
            blockState.tick(this, blockPos, this.random);
        }
    }

    public void tickNonPassenger(Entity entity) {
        entity.setOldPosAndRot();
        ProfilerFiller profilerFiller = this.getProfiler();
        ++entity.tickCount;
        this.getProfiler().push(() -> Registry.ENTITY_TYPE.getKey(entity.getType()).toString());
        profilerFiller.incrementCounter("tickNonPassenger");
        entity.tick();
        this.getProfiler().pop();
        for (Entity entity2 : entity.getPassengers()) {
            this.tickPassenger(entity, entity2);
        }
    }

    private void tickPassenger(Entity entity, Entity entity2) {
        if (entity2.isRemoved() || entity2.getVehicle() != entity) {
            entity2.stopRiding();
            return;
        }
        if (!(entity2 instanceof Player) && !this.entityTickList.contains(entity2)) {
            return;
        }
        entity2.setOldPosAndRot();
        ++entity2.tickCount;
        ProfilerFiller profilerFiller = this.getProfiler();
        profilerFiller.push(() -> Registry.ENTITY_TYPE.getKey(entity2.getType()).toString());
        profilerFiller.incrementCounter("tickPassenger");
        entity2.rideTick();
        profilerFiller.pop();
        for (Entity entity3 : entity2.getPassengers()) {
            this.tickPassenger(entity2, entity3);
        }
    }

    @Override
    public boolean mayInteract(Player player, BlockPos blockPos) {
        return !this.server.isUnderSpawnProtection(this, blockPos, player) && this.getWorldBorder().isWithinBounds(blockPos);
    }

    public void save(@Nullable ProgressListener progressListener, boolean bl, boolean bl2) {
        ServerChunkCache serverChunkCache = this.getChunkSource();
        if (bl2) {
            return;
        }
        if (progressListener != null) {
            progressListener.progressStartNoAbort(new TranslatableComponent("menu.savingLevel"));
        }
        this.saveLevelData();
        if (progressListener != null) {
            progressListener.progressStage(new TranslatableComponent("menu.savingChunks"));
        }
        serverChunkCache.save(bl);
        if (bl) {
            this.entityManager.saveAll();
        } else {
            this.entityManager.autoSave();
        }
    }

    private void saveLevelData() {
        if (this.dragonFight != null) {
            this.server.getWorldData().setEndDragonFightData(this.dragonFight.saveData());
        }
        this.getChunkSource().getDataStorage().save();
    }

    public <T extends Entity> List<? extends T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, Predicate<? super T> predicate) {
        ArrayList arrayList = Lists.newArrayList();
        this.getEntities().get(entityTypeTest, entity -> {
            if (predicate.test(entity)) {
                arrayList.add(entity);
            }
        });
        return arrayList;
    }

    public List<? extends EnderDragon> getDragons() {
        return this.getEntities(EntityType.ENDER_DRAGON, LivingEntity::isAlive);
    }

    public List<ServerPlayer> getPlayers(Predicate<? super ServerPlayer> predicate) {
        ArrayList arrayList = Lists.newArrayList();
        for (ServerPlayer serverPlayer : this.players) {
            if (!predicate.test(serverPlayer)) continue;
            arrayList.add(serverPlayer);
        }
        return arrayList;
    }

    @Nullable
    public ServerPlayer getRandomPlayer() {
        List<ServerPlayer> list = this.getPlayers(LivingEntity::isAlive);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(this.random.nextInt(list.size()));
    }

    @Override
    public boolean addFreshEntity(Entity entity) {
        return this.addEntity(entity);
    }

    public boolean addWithUUID(Entity entity) {
        return this.addEntity(entity);
    }

    public void addDuringTeleport(Entity entity) {
        this.addEntity(entity);
    }

    public void addDuringCommandTeleport(ServerPlayer serverPlayer) {
        this.addPlayer(serverPlayer);
    }

    public void addDuringPortalTeleport(ServerPlayer serverPlayer) {
        this.addPlayer(serverPlayer);
    }

    public void addNewPlayer(ServerPlayer serverPlayer) {
        this.addPlayer(serverPlayer);
    }

    public void addRespawnedPlayer(ServerPlayer serverPlayer) {
        this.addPlayer(serverPlayer);
    }

    private void addPlayer(ServerPlayer serverPlayer) {
        Entity entity = this.getEntities().get(serverPlayer.getUUID());
        if (entity != null) {
            LOGGER.warn("Force-added player with duplicate UUID {}", (Object)serverPlayer.getUUID().toString());
            entity.unRide();
            this.removePlayerImmediately((ServerPlayer)entity, Entity.RemovalReason.DISCARDED);
        }
        this.entityManager.addNewEntity(serverPlayer);
    }

    private boolean addEntity(Entity entity) {
        if (entity.isRemoved()) {
            LOGGER.warn("Tried to add entity {} but it was marked as removed already", (Object)EntityType.getKey(entity.getType()));
            return false;
        }
        return this.entityManager.addNewEntity(entity);
    }

    public boolean tryAddFreshEntityWithPassengers(Entity entity) {
        if (entity.getSelfAndPassengers().map(Entity::getUUID).anyMatch(this.entityManager::isLoaded)) {
            return false;
        }
        this.addFreshEntityWithPassengers(entity);
        return true;
    }

    public void unload(LevelChunk levelChunk) {
        levelChunk.clearAllBlockEntities();
        levelChunk.unregisterTickContainerFromLevel(this);
    }

    public void removePlayerImmediately(ServerPlayer serverPlayer, Entity.RemovalReason removalReason) {
        serverPlayer.remove(removalReason);
    }

    @Override
    public void destroyBlockProgress(int n, BlockPos blockPos, int n2) {
        for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
            double d;
            double d2;
            double d3;
            if (serverPlayer == null || serverPlayer.level != this || serverPlayer.getId() == n || !((d3 = (double)blockPos.getX() - serverPlayer.getX()) * d3 + (d2 = (double)blockPos.getY() - serverPlayer.getY()) * d2 + (d = (double)blockPos.getZ() - serverPlayer.getZ()) * d < 1024.0)) continue;
            serverPlayer.connection.send(new ClientboundBlockDestructionPacket(n, blockPos, n2));
        }
    }

    @Override
    public void playSound(@Nullable Player player, double d, double d2, double d3, SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
        this.server.getPlayerList().broadcast(player, d, d2, d3, f > 1.0f ? (double)(16.0f * f) : 16.0, this.dimension(), new ClientboundSoundPacket(soundEvent, soundSource, d, d2, d3, f, f2));
    }

    @Override
    public void playSound(@Nullable Player player, Entity entity, SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
        this.server.getPlayerList().broadcast(player, entity.getX(), entity.getY(), entity.getZ(), f > 1.0f ? (double)(16.0f * f) : 16.0, this.dimension(), new ClientboundSoundEntityPacket(soundEvent, soundSource, entity, f, f2));
    }

    @Override
    public void globalLevelEvent(int n, BlockPos blockPos, int n2) {
        this.server.getPlayerList().broadcastAll(new ClientboundLevelEventPacket(n, blockPos, n2, true));
    }

    @Override
    public void levelEvent(@Nullable Player player, int n, BlockPos blockPos, int n2) {
        this.server.getPlayerList().broadcast(player, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 64.0, this.dimension(), new ClientboundLevelEventPacket(n, blockPos, n2, false));
    }

    public int getLogicalHeight() {
        return this.dimensionType().logicalHeight();
    }

    @Override
    public void gameEvent(@Nullable Entity entity, GameEvent gameEvent, BlockPos blockPos) {
        this.postGameEventInRadius(entity, gameEvent, blockPos, gameEvent.getNotificationRadius());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void sendBlockUpdated(BlockPos blockPos, BlockState blockState, BlockState blockState2, int n) {
        Object object;
        if (this.isUpdatingNavigations) {
            object = "recursive call to sendBlockUpdated";
            Util.logAndPauseIfInIde("recursive call to sendBlockUpdated", new IllegalStateException("recursive call to sendBlockUpdated"));
        }
        this.getChunkSource().blockChanged(blockPos);
        object = blockState.getCollisionShape(this, blockPos);
        VoxelShape voxelShape = blockState2.getCollisionShape(this, blockPos);
        if (!Shapes.joinIsNotEmpty((VoxelShape)object, voxelShape, BooleanOp.NOT_SAME)) {
            return;
        }
        ObjectArrayList objectArrayList = new ObjectArrayList();
        for (Mob object2 : this.navigatingMobs) {
            PathNavigation pathNavigation = object2.getNavigation();
            if (!pathNavigation.shouldRecomputePath(blockPos)) continue;
            objectArrayList.add(pathNavigation);
        }
        try {
            this.isUpdatingNavigations = true;
            for (PathNavigation pathNavigation : objectArrayList) {
                pathNavigation.recomputePath();
            }
        }
        finally {
            this.isUpdatingNavigations = false;
        }
    }

    @Override
    public void broadcastEntityEvent(Entity entity, byte by) {
        this.getChunkSource().broadcastAndSend(entity, new ClientboundEntityEventPacket(entity, by));
    }

    @Override
    public ServerChunkCache getChunkSource() {
        return this.chunkSource;
    }

    @Override
    public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, double d, double d2, double d3, float f, boolean bl, Explosion.BlockInteraction blockInteraction) {
        Explosion explosion = new Explosion(this, entity, damageSource, explosionDamageCalculator, d, d2, d3, f, bl, blockInteraction);
        explosion.explode();
        explosion.finalizeExplosion(false);
        if (blockInteraction == Explosion.BlockInteraction.NONE) {
            explosion.clearToBlow();
        }
        for (ServerPlayer serverPlayer : this.players) {
            if (!(serverPlayer.distanceToSqr(d, d2, d3) < 4096.0)) continue;
            serverPlayer.connection.send(new ClientboundExplodePacket(d, d2, d3, f, explosion.getToBlow(), explosion.getHitPlayers().get(serverPlayer)));
        }
        return explosion;
    }

    @Override
    public void blockEvent(BlockPos blockPos, Block block, int n, int n2) {
        this.blockEvents.add((Object)new BlockEventData(blockPos, block, n, n2));
    }

    private void runBlockEvents() {
        this.blockEventsToReschedule.clear();
        while (!this.blockEvents.isEmpty()) {
            BlockEventData blockEventData = (BlockEventData)((Object)this.blockEvents.removeFirst());
            if (this.shouldTickBlocksAt(ChunkPos.asLong(blockEventData.pos()))) {
                if (!this.doBlockEvent(blockEventData)) continue;
                this.server.getPlayerList().broadcast(null, blockEventData.pos().getX(), blockEventData.pos().getY(), blockEventData.pos().getZ(), 64.0, this.dimension(), new ClientboundBlockEventPacket(blockEventData.pos(), blockEventData.block(), blockEventData.paramA(), blockEventData.paramB()));
                continue;
            }
            this.blockEventsToReschedule.add(blockEventData);
        }
        this.blockEvents.addAll(this.blockEventsToReschedule);
    }

    private boolean doBlockEvent(BlockEventData blockEventData) {
        BlockState blockState = this.getBlockState(blockEventData.pos());
        if (blockState.is(blockEventData.block())) {
            return blockState.triggerEvent(this, blockEventData.pos(), blockEventData.paramA(), blockEventData.paramB());
        }
        return false;
    }

    public LevelTicks<Block> getBlockTicks() {
        return this.blockTicks;
    }

    public LevelTicks<Fluid> getFluidTicks() {
        return this.fluidTicks;
    }

    @Override
    @Nonnull
    public MinecraftServer getServer() {
        return this.server;
    }

    public PortalForcer getPortalForcer() {
        return this.portalForcer;
    }

    public StructureManager getStructureManager() {
        return this.server.getStructureManager();
    }

    public void sendVibrationParticle(VibrationPath vibrationPath) {
        BlockPos blockPos = vibrationPath.getOrigin();
        ClientboundAddVibrationSignalPacket clientboundAddVibrationSignalPacket = new ClientboundAddVibrationSignalPacket(vibrationPath);
        this.players.forEach(serverPlayer -> this.sendParticles((ServerPlayer)serverPlayer, false, blockPos.getX(), blockPos.getY(), blockPos.getZ(), clientboundAddVibrationSignalPacket));
    }

    public <T extends ParticleOptions> int sendParticles(T t, double d, double d2, double d3, int n, double d4, double d5, double d6, double d7) {
        ClientboundLevelParticlesPacket clientboundLevelParticlesPacket = new ClientboundLevelParticlesPacket(t, false, d, d2, d3, (float)d4, (float)d5, (float)d6, (float)d7, n);
        int n2 = 0;
        for (int i = 0; i < this.players.size(); ++i) {
            ServerPlayer serverPlayer = this.players.get(i);
            if (!this.sendParticles(serverPlayer, false, d, d2, d3, clientboundLevelParticlesPacket)) continue;
            ++n2;
        }
        return n2;
    }

    public <T extends ParticleOptions> boolean sendParticles(ServerPlayer serverPlayer, T t, boolean bl, double d, double d2, double d3, int n, double d4, double d5, double d6, double d7) {
        ClientboundLevelParticlesPacket clientboundLevelParticlesPacket = new ClientboundLevelParticlesPacket(t, bl, d, d2, d3, (float)d4, (float)d5, (float)d6, (float)d7, n);
        return this.sendParticles(serverPlayer, bl, d, d2, d3, clientboundLevelParticlesPacket);
    }

    private boolean sendParticles(ServerPlayer serverPlayer, boolean bl, double d, double d2, double d3, Packet<?> packet) {
        if (serverPlayer.getLevel() != this) {
            return false;
        }
        BlockPos blockPos = serverPlayer.blockPosition();
        if (blockPos.closerToCenterThan(new Vec3(d, d2, d3), bl ? 512.0 : 32.0)) {
            serverPlayer.connection.send(packet);
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public Entity getEntity(int n) {
        return this.getEntities().get(n);
    }

    @Deprecated
    @Nullable
    public Entity getEntityOrPart(int n) {
        Entity entity = this.getEntities().get(n);
        if (entity != null) {
            return entity;
        }
        return (Entity)this.dragonParts.get(n);
    }

    @Nullable
    public Entity getEntity(UUID uUID) {
        return this.getEntities().get(uUID);
    }

    @Nullable
    public BlockPos findNearestMapFeature(TagKey<ConfiguredStructureFeature<?, ?>> tagKey, BlockPos blockPos, int n, boolean bl) {
        if (!this.server.getWorldData().worldGenSettings().generateFeatures()) {
            return null;
        }
        Optional<HolderSet.Named<ConfiguredStructureFeature<?, ?>>> optional = this.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY).getTag(tagKey);
        if (optional.isEmpty()) {
            return null;
        }
        Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> pair = this.getChunkSource().getGenerator().findNearestMapFeature(this, (HolderSet)optional.get(), blockPos, n, bl);
        return pair != null ? (BlockPos)pair.getFirst() : null;
    }

    @Nullable
    public Pair<BlockPos, Holder<Biome>> findNearestBiome(Predicate<Holder<Biome>> predicate, BlockPos blockPos, int n, int n2) {
        return this.getChunkSource().getGenerator().getBiomeSource().findBiomeHorizontal(blockPos.getX(), blockPos.getY(), blockPos.getZ(), n, n2, predicate, this.random, true, this.getChunkSource().getGenerator().climateSampler());
    }

    @Override
    public RecipeManager getRecipeManager() {
        return this.server.getRecipeManager();
    }

    @Override
    public boolean noSave() {
        return this.noSave;
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.server.registryAccess();
    }

    public DimensionDataStorage getDataStorage() {
        return this.getChunkSource().getDataStorage();
    }

    @Override
    @Nullable
    public MapItemSavedData getMapData(String string) {
        return this.getServer().overworld().getDataStorage().get(MapItemSavedData::load, string);
    }

    @Override
    public void setMapData(String string, MapItemSavedData mapItemSavedData) {
        this.getServer().overworld().getDataStorage().set(string, mapItemSavedData);
    }

    @Override
    public int getFreeMapId() {
        return this.getServer().overworld().getDataStorage().computeIfAbsent(MapIndex::load, MapIndex::new, "idcounts").getFreeAuxValueForMap();
    }

    public void setDefaultSpawnPos(BlockPos blockPos, float f) {
        ChunkPos chunkPos = new ChunkPos(new BlockPos(this.levelData.getXSpawn(), 0, this.levelData.getZSpawn()));
        this.levelData.setSpawn(blockPos, f);
        this.getChunkSource().removeRegionTicket(TicketType.START, chunkPos, 11, Unit.INSTANCE);
        this.getChunkSource().addRegionTicket(TicketType.START, new ChunkPos(blockPos), 11, Unit.INSTANCE);
        this.getServer().getPlayerList().broadcastAll(new ClientboundSetDefaultSpawnPositionPacket(blockPos, f));
    }

    public BlockPos getSharedSpawnPos() {
        BlockPos blockPos = new BlockPos(this.levelData.getXSpawn(), this.levelData.getYSpawn(), this.levelData.getZSpawn());
        if (!this.getWorldBorder().isWithinBounds(blockPos)) {
            blockPos = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ()));
        }
        return blockPos;
    }

    public float getSharedSpawnAngle() {
        return this.levelData.getSpawnAngle();
    }

    public LongSet getForcedChunks() {
        ForcedChunksSavedData forcedChunksSavedData = this.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
        return forcedChunksSavedData != null ? LongSets.unmodifiable((LongSet)forcedChunksSavedData.getChunks()) : LongSets.EMPTY_SET;
    }

    public boolean setChunkForced(int n, int n2, boolean bl) {
        boolean bl2;
        ForcedChunksSavedData forcedChunksSavedData = this.getDataStorage().computeIfAbsent(ForcedChunksSavedData::load, ForcedChunksSavedData::new, "chunks");
        ChunkPos chunkPos = new ChunkPos(n, n2);
        long l = chunkPos.toLong();
        if (bl) {
            bl2 = forcedChunksSavedData.getChunks().add(l);
            if (bl2) {
                this.getChunk(n, n2);
            }
        } else {
            bl2 = forcedChunksSavedData.getChunks().remove(l);
        }
        forcedChunksSavedData.setDirty(bl2);
        if (bl2) {
            this.getChunkSource().updateChunkForced(chunkPos, bl);
        }
        return bl2;
    }

    public List<ServerPlayer> players() {
        return this.players;
    }

    @Override
    public void onBlockStateChange(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
        Optional<PoiType> optional;
        Optional<PoiType> optional2 = PoiType.forState(blockState);
        if (Objects.equals(optional2, optional = PoiType.forState(blockState2))) {
            return;
        }
        BlockPos blockPos2 = blockPos.immutable();
        optional2.ifPresent(poiType -> this.getServer().execute(() -> {
            this.getPoiManager().remove(blockPos2);
            DebugPackets.sendPoiRemovedPacket(this, blockPos2);
        }));
        optional.ifPresent(poiType -> this.getServer().execute(() -> {
            this.getPoiManager().add(blockPos2, (PoiType)poiType);
            DebugPackets.sendPoiAddedPacket(this, blockPos2);
        }));
    }

    public PoiManager getPoiManager() {
        return this.getChunkSource().getPoiManager();
    }

    public boolean isVillage(BlockPos blockPos) {
        return this.isCloseToVillage(blockPos, 1);
    }

    public boolean isVillage(SectionPos sectionPos) {
        return this.isVillage(sectionPos.center());
    }

    public boolean isCloseToVillage(BlockPos blockPos, int n) {
        if (n > 6) {
            return false;
        }
        return this.sectionsToVillage(SectionPos.of(blockPos)) <= n;
    }

    public int sectionsToVillage(SectionPos sectionPos) {
        return this.getPoiManager().sectionsToVillage(sectionPos);
    }

    public Raids getRaids() {
        return this.raids;
    }

    @Nullable
    public Raid getRaidAt(BlockPos blockPos) {
        return this.raids.getNearbyRaid(blockPos, 9216);
    }

    public boolean isRaided(BlockPos blockPos) {
        return this.getRaidAt(blockPos) != null;
    }

    public void onReputationEvent(ReputationEventType reputationEventType, Entity entity, ReputationEventHandler reputationEventHandler) {
        reputationEventHandler.onReputationEventFrom(reputationEventType, entity);
    }

    public void saveDebugReport(Path path) throws IOException {
        Object object4;
        Object object2;
        ChunkMap chunkMap = this.getChunkSource().chunkMap;
        try (Object object3 = Files.newBufferedWriter(path.resolve("stats.txt"), new OpenOption[0]);){
            ((Writer)object3).write(String.format("spawning_chunks: %d\n", chunkMap.getDistanceManager().getNaturalSpawnChunkCount()));
            object2 = this.getChunkSource().getLastSpawnState();
            if (object2 != null) {
                for (Object object4 : ((NaturalSpawner.SpawnState)object2).getMobCategoryCounts().object2IntEntrySet()) {
                    ((Writer)object3).write(String.format("spawn_count.%s: %d\n", ((MobCategory)object4.getKey()).getName(), object4.getIntValue()));
                }
            }
            ((Writer)object3).write(String.format("entities: %s\n", this.entityManager.gatherStats()));
            ((Writer)object3).write(String.format("block_entity_tickers: %d\n", this.blockEntityTickers.size()));
            ((Writer)object3).write(String.format("block_ticks: %d\n", ((LevelTicks)this.getBlockTicks()).count()));
            ((Writer)object3).write(String.format("fluid_ticks: %d\n", ((LevelTicks)this.getFluidTicks()).count()));
            ((Writer)object3).write("distance_manager: " + chunkMap.getDistanceManager().getDebugStatus() + "\n");
            ((Writer)object3).write(String.format("pending_tasks: %d\n", this.getChunkSource().getPendingTasksCount()));
        }
        object3 = new CrashReport("Level dump", new Exception("dummy"));
        this.fillReportDetails((CrashReport)object3);
        object2 = Files.newBufferedWriter(path.resolve("example_crash.txt"), new OpenOption[0]);
        try {
            ((Writer)object2).write(((CrashReport)object3).getFriendlyReport());
        }
        finally {
            if (object2 != null) {
                ((Writer)object2).close();
            }
        }
        object2 = path.resolve("chunks.csv");
        try (Object object5 = Files.newBufferedWriter((Path)object2, new OpenOption[0]);){
            chunkMap.dumpChunks((Writer)object5);
        }
        object5 = path.resolve("entity_chunks.csv");
        object4 = Files.newBufferedWriter((Path)object5, new OpenOption[0]);
        try {
            this.entityManager.dumpSections((Writer)object4);
        }
        finally {
            if (object4 != null) {
                ((Writer)object4).close();
            }
        }
        object4 = path.resolve("entities.csv");
        try (Object object6 = Files.newBufferedWriter((Path)object4, new OpenOption[0]);){
            ServerLevel.dumpEntities((Writer)object6, this.getEntities().getAll());
        }
        object6 = path.resolve("block_entities.csv");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter((Path)object6, new OpenOption[0]);){
            this.dumpBlockEntityTickers(bufferedWriter);
        }
    }

    private static void dumpEntities(Writer writer, Iterable<Entity> iterable) throws IOException {
        CsvOutput csvOutput = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("uuid").addColumn("type").addColumn("alive").addColumn("display_name").addColumn("custom_name").build(writer);
        for (Entity entity : iterable) {
            Component component = entity.getCustomName();
            Component component2 = entity.getDisplayName();
            csvOutput.writeRow(entity.getX(), entity.getY(), entity.getZ(), entity.getUUID(), Registry.ENTITY_TYPE.getKey(entity.getType()), entity.isAlive(), component2.getString(), component != null ? component.getString() : null);
        }
    }

    private void dumpBlockEntityTickers(Writer writer) throws IOException {
        CsvOutput csvOutput = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("type").build(writer);
        for (TickingBlockEntity tickingBlockEntity : this.blockEntityTickers) {
            BlockPos blockPos = tickingBlockEntity.getPos();
            csvOutput.writeRow(blockPos.getX(), blockPos.getY(), blockPos.getZ(), tickingBlockEntity.getType());
        }
    }

    @VisibleForTesting
    public void clearBlockEvents(BoundingBox boundingBox) {
        this.blockEvents.removeIf(blockEventData -> boundingBox.isInside(blockEventData.pos()));
    }

    @Override
    public void blockUpdated(BlockPos blockPos, Block block) {
        if (!this.isDebug()) {
            this.updateNeighborsAt(blockPos, block);
        }
    }

    @Override
    public float getShade(Direction direction, boolean bl) {
        return 1.0f;
    }

    public Iterable<Entity> getAllEntities() {
        return this.getEntities().getAll();
    }

    public String toString() {
        return "ServerLevel[" + this.serverLevelData.getLevelName() + "]";
    }

    public boolean isFlat() {
        return this.server.getWorldData().worldGenSettings().isFlatWorld();
    }

    @Override
    public long getSeed() {
        return this.server.getWorldData().worldGenSettings().seed();
    }

    @Nullable
    public EndDragonFight dragonFight() {
        return this.dragonFight;
    }

    @Override
    public ServerLevel getLevel() {
        return this;
    }

    @VisibleForTesting
    public String getWatchdogStats() {
        return String.format("players: %s, entities: %s [%s], block_entities: %d [%s], block_ticks: %d, fluid_ticks: %d, chunk_source: %s", this.players.size(), this.entityManager.gatherStats(), ServerLevel.getTypeCount(this.entityManager.getEntityGetter().getAll(), entity -> Registry.ENTITY_TYPE.getKey(entity.getType()).toString()), this.blockEntityTickers.size(), ServerLevel.getTypeCount(this.blockEntityTickers, TickingBlockEntity::getType), ((LevelTicks)this.getBlockTicks()).count(), ((LevelTicks)this.getFluidTicks()).count(), this.gatherChunkSourceStats());
    }

    private static <T> String getTypeCount(Iterable<T> iterable, Function<T, String> function) {
        try {
            Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
            for (T t : iterable) {
                String string = function.apply(t);
                object2IntOpenHashMap.addTo((Object)string, 1);
            }
            return object2IntOpenHashMap.object2IntEntrySet().stream().sorted(Comparator.comparing(Object2IntMap.Entry::getIntValue).reversed()).limit(5L).map(entry -> (String)entry.getKey() + ":" + entry.getIntValue()).collect(Collectors.joining(","));
        }
        catch (Exception exception) {
            return "";
        }
    }

    public static void makeObsidianPlatform(ServerLevel serverLevel) {
        BlockPos blockPos2 = END_SPAWN_POINT;
        int n = blockPos2.getX();
        int n2 = blockPos2.getY() - 2;
        int n3 = blockPos2.getZ();
        BlockPos.betweenClosed(n - 2, n2 + 1, n3 - 2, n + 2, n2 + 3, n3 + 2).forEach(blockPos -> serverLevel.setBlockAndUpdate((BlockPos)blockPos, Blocks.AIR.defaultBlockState()));
        BlockPos.betweenClosed(n - 2, n2, n3 - 2, n + 2, n2, n3 + 2).forEach(blockPos -> serverLevel.setBlockAndUpdate((BlockPos)blockPos, Blocks.OBSIDIAN.defaultBlockState()));
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return this.entityManager.getEntityGetter();
    }

    public void addLegacyChunkEntities(Stream<Entity> stream) {
        this.entityManager.addLegacyChunkEntities(stream);
    }

    public void addWorldGenChunkEntities(Stream<Entity> stream) {
        this.entityManager.addWorldGenChunkEntities(stream);
    }

    public void startTickingChunk(LevelChunk levelChunk) {
        levelChunk.unpackTicks(this.getLevelData().getGameTime());
    }

    public void onStructureStartsAvailable(ChunkAccess chunkAccess) {
        this.server.execute(() -> this.structureCheck.onStructureLoad(chunkAccess.getPos(), chunkAccess.getAllStarts()));
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.entityManager.close();
    }

    @Override
    public String gatherChunkSourceStats() {
        return "Chunks[S] W: " + this.chunkSource.gatherStats() + " E: " + this.entityManager.gatherStats();
    }

    public boolean areEntitiesLoaded(long l) {
        return this.entityManager.areEntitiesLoaded(l);
    }

    private boolean isPositionTickingWithEntitiesLoaded(long l) {
        return this.areEntitiesLoaded(l) && this.chunkSource.isPositionTicking(l);
    }

    public boolean isPositionEntityTicking(BlockPos blockPos) {
        return this.entityManager.canPositionTick(blockPos) && this.chunkSource.chunkMap.getDistanceManager().inEntityTickingRange(ChunkPos.asLong(blockPos));
    }

    public boolean isNaturalSpawningAllowed(BlockPos blockPos) {
        return this.entityManager.canPositionTick(blockPos);
    }

    public boolean isNaturalSpawningAllowed(ChunkPos chunkPos) {
        return this.entityManager.canPositionTick(chunkPos);
    }

    @Override
    public /* synthetic */ Scoreboard getScoreboard() {
        return this.getScoreboard();
    }

    @Override
    public /* synthetic */ ChunkSource getChunkSource() {
        return this.getChunkSource();
    }

    public /* synthetic */ LevelTickAccess getFluidTicks() {
        return this.getFluidTicks();
    }

    public /* synthetic */ LevelTickAccess getBlockTicks() {
        return this.getBlockTicks();
    }

    final class EntityCallbacks
    implements LevelCallback<Entity> {
        EntityCallbacks() {
        }

        @Override
        public void onCreated(Entity entity) {
        }

        @Override
        public void onDestroyed(Entity entity) {
            ServerLevel.this.getScoreboard().entityRemoved(entity);
        }

        @Override
        public void onTickingStart(Entity entity) {
            ServerLevel.this.entityTickList.add(entity);
        }

        @Override
        public void onTickingEnd(Entity entity) {
            ServerLevel.this.entityTickList.remove(entity);
        }

        @Override
        public void onTrackingStart(Entity entity) {
            LivingEntity livingEntity;
            ServerLevel.this.getChunkSource().addEntity(entity);
            if (entity instanceof ServerPlayer) {
                livingEntity = (ServerPlayer)entity;
                ServerLevel.this.players.add((ServerPlayer)livingEntity);
                ServerLevel.this.updateSleepingPlayerList();
            }
            if (entity instanceof Mob) {
                livingEntity = (Mob)entity;
                if (ServerLevel.this.isUpdatingNavigations) {
                    EnderDragonPart[] enderDragonPartArray = "onTrackingStart called during navigation iteration";
                    Util.logAndPauseIfInIde("onTrackingStart called during navigation iteration", new IllegalStateException("onTrackingStart called during navigation iteration"));
                }
                ServerLevel.this.navigatingMobs.add((Mob)livingEntity);
            }
            if (entity instanceof EnderDragon) {
                livingEntity = (EnderDragon)entity;
                for (EnderDragonPart enderDragonPart : ((EnderDragon)livingEntity).getSubEntities()) {
                    ServerLevel.this.dragonParts.put(enderDragonPart.getId(), (Object)enderDragonPart);
                }
            }
        }

        @Override
        public void onTrackingEnd(Entity entity) {
            Object object;
            ServerLevel.this.getChunkSource().removeEntity(entity);
            if (entity instanceof ServerPlayer) {
                object = (ServerPlayer)entity;
                ServerLevel.this.players.remove(object);
                ServerLevel.this.updateSleepingPlayerList();
            }
            if (entity instanceof Mob) {
                object = (Mob)entity;
                if (ServerLevel.this.isUpdatingNavigations) {
                    EnderDragonPart[] enderDragonPartArray = "onTrackingStart called during navigation iteration";
                    Util.logAndPauseIfInIde("onTrackingStart called during navigation iteration", new IllegalStateException("onTrackingStart called during navigation iteration"));
                }
                ServerLevel.this.navigatingMobs.remove(object);
            }
            if (entity instanceof EnderDragon) {
                object = (EnderDragon)entity;
                for (EnderDragonPart enderDragonPart : ((EnderDragon)object).getSubEntities()) {
                    ServerLevel.this.dragonParts.remove(enderDragonPart.getId());
                }
            }
            if ((object = entity.getGameEventListenerRegistrar()) != null) {
                ((GameEventListenerRegistrar)object).onListenerRemoved(entity.level);
            }
        }

        @Override
        public /* synthetic */ void onTrackingEnd(Object object) {
            this.onTrackingEnd((Entity)object);
        }

        @Override
        public /* synthetic */ void onTrackingStart(Object object) {
            this.onTrackingStart((Entity)object);
        }

        @Override
        public /* synthetic */ void onTickingEnd(Object object) {
            this.onTickingEnd((Entity)object);
        }

        @Override
        public /* synthetic */ void onTickingStart(Object object) {
            this.onTickingStart((Entity)object);
        }

        @Override
        public /* synthetic */ void onDestroyed(Object object) {
            this.onDestroyed((Entity)object);
        }

        @Override
        public /* synthetic */ void onCreated(Object object) {
            this.onCreated((Entity)object);
        }
    }
}

