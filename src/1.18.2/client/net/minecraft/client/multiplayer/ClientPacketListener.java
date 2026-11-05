/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.Unpooled
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.ClientTelemetryManager;
import net.minecraft.client.DebugQueryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DemoIntroScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.client.resources.sounds.BeeAggressiveSoundInstance;
import net.minecraft.client.resources.sounds.BeeFlyingSoundInstance;
import net.minecraft.client.resources.sounds.BeeSoundInstance;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundAddPaintingPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAddVibrationSignalPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

public class ClientPacketListener
implements ClientGamePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component GENERIC_DISCONNECT_MESSAGE = new TranslatableComponent("disconnect.lost");
    private final Connection connection;
    private final GameProfile localGameProfile;
    private final Screen callbackScreen;
    private final Minecraft minecraft;
    private ClientLevel level;
    private ClientLevel.ClientLevelData levelData;
    private final Map<UUID, PlayerInfo> playerInfoMap = Maps.newHashMap();
    private final ClientAdvancements advancements;
    private final ClientSuggestionProvider suggestionsProvider;
    private final DebugQueryHandler debugQueryHandler = new DebugQueryHandler(this);
    private int serverChunkRadius = 3;
    private int serverSimulationDistance = 3;
    private final Random random = new Random();
    private CommandDispatcher<SharedSuggestionProvider> commands = new CommandDispatcher();
    private final RecipeManager recipeManager = new RecipeManager();
    private final UUID id = UUID.randomUUID();
    private Set<ResourceKey<Level>> levels;
    private RegistryAccess.Frozen registryAccess = RegistryAccess.BUILTIN.get();
    private final ClientTelemetryManager telemetryManager;

    public ClientPacketListener(Minecraft minecraft, Screen screen, Connection connection, GameProfile gameProfile, ClientTelemetryManager clientTelemetryManager) {
        this.minecraft = minecraft;
        this.callbackScreen = screen;
        this.connection = connection;
        this.localGameProfile = gameProfile;
        this.advancements = new ClientAdvancements(minecraft);
        this.suggestionsProvider = new ClientSuggestionProvider(this, minecraft);
        this.telemetryManager = clientTelemetryManager;
    }

    public ClientSuggestionProvider getSuggestionsProvider() {
        return this.suggestionsProvider;
    }

    public void cleanup() {
        this.level = null;
    }

    public RecipeManager getRecipeManager() {
        return this.recipeManager;
    }

    @Override
    public void handleLogin(ClientboundLoginPacket clientboundLoginPacket) {
        ClientLevel.ClientLevelData clientLevelData;
        PacketUtils.ensureRunningOnSameThread(clientboundLoginPacket, this, this.minecraft);
        this.minecraft.gameMode = new MultiPlayerGameMode(this.minecraft, this);
        this.registryAccess = clientboundLoginPacket.registryHolder();
        if (!this.connection.isMemoryConnection()) {
            this.registryAccess.registries().forEach(registryEntry -> registryEntry.value().resetTags());
        }
        ArrayList arrayList = Lists.newArrayList(clientboundLoginPacket.levels());
        Collections.shuffle(arrayList);
        this.levels = Sets.newLinkedHashSet((Iterable)arrayList);
        ResourceKey<Level> resourceKey = clientboundLoginPacket.dimension();
        Holder<DimensionType> holder = clientboundLoginPacket.dimensionType();
        this.serverChunkRadius = clientboundLoginPacket.chunkRadius();
        this.serverSimulationDistance = clientboundLoginPacket.simulationDistance();
        boolean bl = clientboundLoginPacket.isDebug();
        boolean bl2 = clientboundLoginPacket.isFlat();
        this.levelData = clientLevelData = new ClientLevel.ClientLevelData(Difficulty.NORMAL, clientboundLoginPacket.hardcore(), bl2);
        this.level = new ClientLevel(this, clientLevelData, resourceKey, holder, this.serverChunkRadius, this.serverSimulationDistance, this.minecraft::getProfiler, this.minecraft.levelRenderer, bl, clientboundLoginPacket.seed());
        this.minecraft.setLevel(this.level);
        if (this.minecraft.player == null) {
            this.minecraft.player = this.minecraft.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook());
            this.minecraft.player.setYRot(-180.0f);
            if (this.minecraft.getSingleplayerServer() != null) {
                this.minecraft.getSingleplayerServer().setUUID(this.minecraft.player.getUUID());
            }
        }
        this.minecraft.debugRenderer.clear();
        this.minecraft.player.resetPos();
        int n = clientboundLoginPacket.playerId();
        this.minecraft.player.setId(n);
        this.level.addPlayer(n, this.minecraft.player);
        this.minecraft.player.input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(this.minecraft.player);
        this.minecraft.cameraEntity = this.minecraft.player;
        this.minecraft.setScreen(new ReceivingLevelScreen());
        this.minecraft.player.setReducedDebugInfo(clientboundLoginPacket.reducedDebugInfo());
        this.minecraft.player.setShowDeathScreen(clientboundLoginPacket.showDeathScreen());
        this.minecraft.gameMode.setLocalMode(clientboundLoginPacket.gameType(), clientboundLoginPacket.previousGameType());
        this.minecraft.options.setServerRenderDistance(clientboundLoginPacket.chunkRadius());
        this.minecraft.options.broadcastOptions();
        this.connection.send(new ServerboundCustomPayloadPacket(ServerboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(Unpooled.buffer()).writeUtf(ClientBrandRetriever.getClientModName())));
        this.minecraft.getGame().onStartGameSession();
        this.telemetryManager.onPlayerInfoReceived(clientboundLoginPacket.gameType(), clientboundLoginPacket.hardcore());
    }

    @Override
    public void handleAddEntity(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundAddEntityPacket, this, this.minecraft);
        EntityType<?> entityType = clientboundAddEntityPacket.getType();
        Object obj = entityType.create(this.level);
        if (obj != null) {
            ((Entity)obj).recreateFromPacket(clientboundAddEntityPacket);
            int n = clientboundAddEntityPacket.getId();
            this.level.putNonPlayerEntity(n, (Entity)obj);
            if (obj instanceof AbstractMinecart) {
                this.minecraft.getSoundManager().play(new MinecartSoundInstance((AbstractMinecart)obj));
            }
        }
    }

    @Override
    public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket clientboundAddExperienceOrbPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundAddExperienceOrbPacket, this, this.minecraft);
        double d = clientboundAddExperienceOrbPacket.getX();
        double d2 = clientboundAddExperienceOrbPacket.getY();
        double d3 = clientboundAddExperienceOrbPacket.getZ();
        ExperienceOrb experienceOrb = new ExperienceOrb(this.level, d, d2, d3, clientboundAddExperienceOrbPacket.getValue());
        experienceOrb.setPacketCoordinates(d, d2, d3);
        experienceOrb.setYRot(0.0f);
        experienceOrb.setXRot(0.0f);
        experienceOrb.setId(clientboundAddExperienceOrbPacket.getId());
        this.level.putNonPlayerEntity(clientboundAddExperienceOrbPacket.getId(), experienceOrb);
    }

    @Override
    public void handleAddVibrationSignal(ClientboundAddVibrationSignalPacket clientboundAddVibrationSignalPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundAddVibrationSignalPacket, this, this.minecraft);
        VibrationPath vibrationPath = clientboundAddVibrationSignalPacket.getVibrationPath();
        BlockPos blockPos = vibrationPath.getOrigin();
        this.level.addAlwaysVisibleParticle(new VibrationParticleOption(vibrationPath), true, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
    }

    @Override
    public void handleAddPainting(ClientboundAddPaintingPacket clientboundAddPaintingPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundAddPaintingPacket, this, this.minecraft);
        Painting painting = new Painting(this.level, clientboundAddPaintingPacket.getPos(), clientboundAddPaintingPacket.getDirection(), clientboundAddPaintingPacket.getMotive());
        painting.setId(clientboundAddPaintingPacket.getId());
        painting.setUUID(clientboundAddPaintingPacket.getUUID());
        this.level.putNonPlayerEntity(clientboundAddPaintingPacket.getId(), painting);
    }

    @Override
    public void handleSetEntityMotion(ClientboundSetEntityMotionPacket clientboundSetEntityMotionPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetEntityMotionPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundSetEntityMotionPacket.getId());
        if (entity == null) {
            return;
        }
        entity.lerpMotion((double)clientboundSetEntityMotionPacket.getXa() / 8000.0, (double)clientboundSetEntityMotionPacket.getYa() / 8000.0, (double)clientboundSetEntityMotionPacket.getZa() / 8000.0);
    }

    @Override
    public void handleSetEntityData(ClientboundSetEntityDataPacket clientboundSetEntityDataPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetEntityDataPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundSetEntityDataPacket.getId());
        if (entity != null && clientboundSetEntityDataPacket.getUnpackedData() != null) {
            entity.getEntityData().assignValues(clientboundSetEntityDataPacket.getUnpackedData());
        }
    }

    @Override
    public void handleAddPlayer(ClientboundAddPlayerPacket clientboundAddPlayerPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundAddPlayerPacket, this, this.minecraft);
        double d = clientboundAddPlayerPacket.getX();
        double d2 = clientboundAddPlayerPacket.getY();
        double d3 = clientboundAddPlayerPacket.getZ();
        float f = (float)(clientboundAddPlayerPacket.getyRot() * 360) / 256.0f;
        float f2 = (float)(clientboundAddPlayerPacket.getxRot() * 360) / 256.0f;
        int n = clientboundAddPlayerPacket.getEntityId();
        RemotePlayer remotePlayer = new RemotePlayer(this.minecraft.level, this.getPlayerInfo(clientboundAddPlayerPacket.getPlayerId()).getProfile());
        remotePlayer.setId(n);
        remotePlayer.setPacketCoordinates(d, d2, d3);
        remotePlayer.absMoveTo(d, d2, d3, f, f2);
        remotePlayer.setOldPosAndRot();
        this.level.addPlayer(n, remotePlayer);
    }

    @Override
    public void handleTeleportEntity(ClientboundTeleportEntityPacket clientboundTeleportEntityPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundTeleportEntityPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundTeleportEntityPacket.getId());
        if (entity == null) {
            return;
        }
        double d = clientboundTeleportEntityPacket.getX();
        double d2 = clientboundTeleportEntityPacket.getY();
        double d3 = clientboundTeleportEntityPacket.getZ();
        entity.setPacketCoordinates(d, d2, d3);
        if (!entity.isControlledByLocalInstance()) {
            float f = (float)(clientboundTeleportEntityPacket.getyRot() * 360) / 256.0f;
            float f2 = (float)(clientboundTeleportEntityPacket.getxRot() * 360) / 256.0f;
            entity.lerpTo(d, d2, d3, f, f2, 3, true);
            entity.setOnGround(clientboundTeleportEntityPacket.isOnGround());
        }
    }

    @Override
    public void handleSetCarriedItem(ClientboundSetCarriedItemPacket clientboundSetCarriedItemPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetCarriedItemPacket, this, this.minecraft);
        if (Inventory.isHotbarSlot(clientboundSetCarriedItemPacket.getSlot())) {
            this.minecraft.player.getInventory().selected = clientboundSetCarriedItemPacket.getSlot();
        }
    }

    @Override
    public void handleMoveEntity(ClientboundMoveEntityPacket clientboundMoveEntityPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundMoveEntityPacket, this, this.minecraft);
        Entity entity = clientboundMoveEntityPacket.getEntity(this.level);
        if (entity == null) {
            return;
        }
        if (!entity.isControlledByLocalInstance()) {
            if (clientboundMoveEntityPacket.hasPosition()) {
                Vec3 vec3 = clientboundMoveEntityPacket.updateEntityPosition(entity.getPacketCoordinates());
                entity.setPacketCoordinates(vec3);
                float f = clientboundMoveEntityPacket.hasRotation() ? (float)(clientboundMoveEntityPacket.getyRot() * 360) / 256.0f : entity.getYRot();
                float f2 = clientboundMoveEntityPacket.hasRotation() ? (float)(clientboundMoveEntityPacket.getxRot() * 360) / 256.0f : entity.getXRot();
                entity.lerpTo(vec3.x(), vec3.y(), vec3.z(), f, f2, 3, false);
            } else if (clientboundMoveEntityPacket.hasRotation()) {
                float f = (float)(clientboundMoveEntityPacket.getyRot() * 360) / 256.0f;
                float f3 = (float)(clientboundMoveEntityPacket.getxRot() * 360) / 256.0f;
                entity.lerpTo(entity.getX(), entity.getY(), entity.getZ(), f, f3, 3, false);
            }
            entity.setOnGround(clientboundMoveEntityPacket.isOnGround());
        }
    }

    @Override
    public void handleRotateMob(ClientboundRotateHeadPacket clientboundRotateHeadPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundRotateHeadPacket, this, this.minecraft);
        Entity entity = clientboundRotateHeadPacket.getEntity(this.level);
        if (entity == null) {
            return;
        }
        float f = (float)(clientboundRotateHeadPacket.getYHeadRot() * 360) / 256.0f;
        entity.lerpHeadTo(f, 3);
    }

    @Override
    public void handleRemoveEntities(ClientboundRemoveEntitiesPacket clientboundRemoveEntitiesPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundRemoveEntitiesPacket, this, this.minecraft);
        clientboundRemoveEntitiesPacket.getEntityIds().forEach(n -> this.level.removeEntity(n, Entity.RemovalReason.DISCARDED));
    }

    @Override
    public void handleMovePlayer(ClientboundPlayerPositionPacket clientboundPlayerPositionPacket) {
        double d;
        double d2;
        double d3;
        double d4;
        double d5;
        double d6;
        PacketUtils.ensureRunningOnSameThread(clientboundPlayerPositionPacket, this, this.minecraft);
        LocalPlayer localPlayer = this.minecraft.player;
        if (clientboundPlayerPositionPacket.requestDismountVehicle()) {
            ((Player)localPlayer).removeVehicle();
        }
        Vec3 vec3 = localPlayer.getDeltaMovement();
        boolean bl = clientboundPlayerPositionPacket.getRelativeArguments().contains((Object)ClientboundPlayerPositionPacket.RelativeArgument.X);
        boolean bl2 = clientboundPlayerPositionPacket.getRelativeArguments().contains((Object)ClientboundPlayerPositionPacket.RelativeArgument.Y);
        boolean bl3 = clientboundPlayerPositionPacket.getRelativeArguments().contains((Object)ClientboundPlayerPositionPacket.RelativeArgument.Z);
        if (bl) {
            d6 = vec3.x();
            d5 = localPlayer.getX() + clientboundPlayerPositionPacket.getX();
            localPlayer.xOld += clientboundPlayerPositionPacket.getX();
        } else {
            d6 = 0.0;
            localPlayer.xOld = d5 = clientboundPlayerPositionPacket.getX();
        }
        if (bl2) {
            d4 = vec3.y();
            d3 = localPlayer.getY() + clientboundPlayerPositionPacket.getY();
            localPlayer.yOld += clientboundPlayerPositionPacket.getY();
        } else {
            d4 = 0.0;
            localPlayer.yOld = d3 = clientboundPlayerPositionPacket.getY();
        }
        if (bl3) {
            d2 = vec3.z();
            d = localPlayer.getZ() + clientboundPlayerPositionPacket.getZ();
            localPlayer.zOld += clientboundPlayerPositionPacket.getZ();
        } else {
            d2 = 0.0;
            localPlayer.zOld = d = clientboundPlayerPositionPacket.getZ();
        }
        localPlayer.setPosRaw(d5, d3, d);
        localPlayer.xo = d5;
        localPlayer.yo = d3;
        localPlayer.zo = d;
        localPlayer.setDeltaMovement(d6, d4, d2);
        float f = clientboundPlayerPositionPacket.getYRot();
        float f2 = clientboundPlayerPositionPacket.getXRot();
        if (clientboundPlayerPositionPacket.getRelativeArguments().contains((Object)ClientboundPlayerPositionPacket.RelativeArgument.X_ROT)) {
            f2 += localPlayer.getXRot();
        }
        if (clientboundPlayerPositionPacket.getRelativeArguments().contains((Object)ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT)) {
            f += localPlayer.getYRot();
        }
        localPlayer.absMoveTo(d5, d3, d, f, f2);
        this.connection.send(new ServerboundAcceptTeleportationPacket(clientboundPlayerPositionPacket.getId()));
        this.connection.send(new ServerboundMovePlayerPacket.PosRot(localPlayer.getX(), localPlayer.getY(), localPlayer.getZ(), localPlayer.getYRot(), localPlayer.getXRot(), false));
    }

    @Override
    public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket clientboundSectionBlocksUpdatePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSectionBlocksUpdatePacket, this, this.minecraft);
        int n = 0x13 | (clientboundSectionBlocksUpdatePacket.shouldSuppressLightUpdates() ? 128 : 0);
        clientboundSectionBlocksUpdatePacket.runUpdates((blockPos, blockState) -> this.level.setBlock((BlockPos)blockPos, (BlockState)blockState, n));
    }

    @Override
    public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket clientboundLevelChunkWithLightPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundLevelChunkWithLightPacket, this, this.minecraft);
        this.updateLevelChunk(clientboundLevelChunkWithLightPacket.getX(), clientboundLevelChunkWithLightPacket.getZ(), clientboundLevelChunkWithLightPacket.getChunkData());
        this.queueLightUpdate(clientboundLevelChunkWithLightPacket.getX(), clientboundLevelChunkWithLightPacket.getZ(), clientboundLevelChunkWithLightPacket.getLightData());
    }

    private void updateLevelChunk(int n, int n2, ClientboundLevelChunkPacketData clientboundLevelChunkPacketData) {
        this.level.getChunkSource().replaceWithPacketData(n, n2, clientboundLevelChunkPacketData.getReadBuffer(), clientboundLevelChunkPacketData.getHeightmaps(), clientboundLevelChunkPacketData.getBlockEntitiesTagsConsumer(n, n2));
    }

    private void queueLightUpdate(int n, int n2, ClientboundLightUpdatePacketData clientboundLightUpdatePacketData) {
        this.level.queueLightUpdate(() -> {
            this.applyLightData(n, n2, clientboundLightUpdatePacketData);
            LevelChunk levelChunk = this.level.getChunkSource().getChunk(n, n2, false);
            if (levelChunk != null) {
                this.enableChunkLight(levelChunk, n, n2);
            }
        });
    }

    private void enableChunkLight(LevelChunk levelChunk, int n, int n2) {
        LevelLightEngine levelLightEngine = this.level.getChunkSource().getLightEngine();
        LevelChunkSection[] levelChunkSectionArray = levelChunk.getSections();
        ChunkPos chunkPos = levelChunk.getPos();
        levelLightEngine.enableLightSources(chunkPos, true);
        for (int i = 0; i < levelChunkSectionArray.length; ++i) {
            LevelChunkSection levelChunkSection = levelChunkSectionArray[i];
            int n3 = this.level.getSectionYFromSectionIndex(i);
            levelLightEngine.updateSectionStatus(SectionPos.of(chunkPos, n3), levelChunkSection.hasOnlyAir());
            this.level.setSectionDirtyWithNeighbors(n, n3, n2);
        }
        this.level.setLightReady(n, n2);
    }

    @Override
    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket clientboundForgetLevelChunkPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundForgetLevelChunkPacket, this, this.minecraft);
        int n = clientboundForgetLevelChunkPacket.getX();
        int n2 = clientboundForgetLevelChunkPacket.getZ();
        ClientChunkCache clientChunkCache = this.level.getChunkSource();
        clientChunkCache.drop(n, n2);
        this.queueLightUpdate(clientboundForgetLevelChunkPacket);
    }

    private void queueLightUpdate(ClientboundForgetLevelChunkPacket clientboundForgetLevelChunkPacket) {
        this.level.queueLightUpdate(() -> {
            LevelLightEngine levelLightEngine = this.level.getLightEngine();
            for (int i = this.level.getMinSection(); i < this.level.getMaxSection(); ++i) {
                levelLightEngine.updateSectionStatus(SectionPos.of(clientboundForgetLevelChunkPacket.getX(), i, clientboundForgetLevelChunkPacket.getZ()), true);
            }
            levelLightEngine.enableLightSources(new ChunkPos(clientboundForgetLevelChunkPacket.getX(), clientboundForgetLevelChunkPacket.getZ()), false);
            this.level.setLightReady(clientboundForgetLevelChunkPacket.getX(), clientboundForgetLevelChunkPacket.getZ());
        });
    }

    @Override
    public void handleBlockUpdate(ClientboundBlockUpdatePacket clientboundBlockUpdatePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundBlockUpdatePacket, this, this.minecraft);
        this.level.setKnownState(clientboundBlockUpdatePacket.getPos(), clientboundBlockUpdatePacket.getBlockState());
    }

    @Override
    public void handleDisconnect(ClientboundDisconnectPacket clientboundDisconnectPacket) {
        this.connection.disconnect(clientboundDisconnectPacket.getReason());
    }

    @Override
    public void onDisconnect(Component component) {
        this.minecraft.clearLevel();
        this.telemetryManager.onDisconnect();
        if (this.callbackScreen != null) {
            if (this.callbackScreen instanceof RealmsScreen) {
                this.minecraft.setScreen(new DisconnectedRealmsScreen(this.callbackScreen, GENERIC_DISCONNECT_MESSAGE, component));
            } else {
                this.minecraft.setScreen(new DisconnectedScreen(this.callbackScreen, GENERIC_DISCONNECT_MESSAGE, component));
            }
        } else {
            this.minecraft.setScreen(new DisconnectedScreen(new JoinMultiplayerScreen(new TitleScreen()), GENERIC_DISCONNECT_MESSAGE, component));
        }
    }

    public void send(Packet<?> packet) {
        this.connection.send(packet);
    }

    @Override
    public void handleTakeItemEntity(ClientboundTakeItemEntityPacket clientboundTakeItemEntityPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundTakeItemEntityPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundTakeItemEntityPacket.getItemId());
        LivingEntity livingEntity = (LivingEntity)this.level.getEntity(clientboundTakeItemEntityPacket.getPlayerId());
        if (livingEntity == null) {
            livingEntity = this.minecraft.player;
        }
        if (entity != null) {
            if (entity instanceof ExperienceOrb) {
                this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1f, (this.random.nextFloat() - this.random.nextFloat()) * 0.35f + 0.9f, false);
            } else {
                this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, (this.random.nextFloat() - this.random.nextFloat()) * 1.4f + 2.0f, false);
            }
            this.minecraft.particleEngine.add(new ItemPickupParticle(this.minecraft.getEntityRenderDispatcher(), this.minecraft.renderBuffers(), this.level, entity, livingEntity));
            if (entity instanceof ItemEntity) {
                ItemEntity itemEntity = (ItemEntity)entity;
                ItemStack itemStack = itemEntity.getItem();
                itemStack.shrink(clientboundTakeItemEntityPacket.getAmount());
                if (itemStack.isEmpty()) {
                    this.level.removeEntity(clientboundTakeItemEntityPacket.getItemId(), Entity.RemovalReason.DISCARDED);
                }
            } else if (!(entity instanceof ExperienceOrb)) {
                this.level.removeEntity(clientboundTakeItemEntityPacket.getItemId(), Entity.RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    public void handleChat(ClientboundChatPacket clientboundChatPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundChatPacket, this, this.minecraft);
        this.minecraft.gui.handleChat(clientboundChatPacket.getType(), clientboundChatPacket.getMessage(), clientboundChatPacket.getSender());
    }

    @Override
    public void handleAnimate(ClientboundAnimatePacket clientboundAnimatePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundAnimatePacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundAnimatePacket.getId());
        if (entity == null) {
            return;
        }
        if (clientboundAnimatePacket.getAction() == 0) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.swing(InteractionHand.MAIN_HAND);
        } else if (clientboundAnimatePacket.getAction() == 3) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.swing(InteractionHand.OFF_HAND);
        } else if (clientboundAnimatePacket.getAction() == 1) {
            entity.animateHurt();
        } else if (clientboundAnimatePacket.getAction() == 2) {
            Player player = (Player)entity;
            player.stopSleepInBed(false, false);
        } else if (clientboundAnimatePacket.getAction() == 4) {
            this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.CRIT);
        } else if (clientboundAnimatePacket.getAction() == 5) {
            this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.ENCHANTED_HIT);
        }
    }

    @Override
    public void handleAddMob(ClientboundAddMobPacket clientboundAddMobPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundAddMobPacket, this, this.minecraft);
        LivingEntity livingEntity = (LivingEntity)EntityType.create(clientboundAddMobPacket.getType(), (Level)this.level);
        if (livingEntity != null) {
            livingEntity.recreateFromPacket(clientboundAddMobPacket);
            this.level.putNonPlayerEntity(clientboundAddMobPacket.getId(), livingEntity);
            if (livingEntity instanceof Bee) {
                boolean bl = ((Bee)livingEntity).isAngry();
                BeeSoundInstance beeSoundInstance = bl ? new BeeAggressiveSoundInstance((Bee)livingEntity) : new BeeFlyingSoundInstance((Bee)livingEntity);
                this.minecraft.getSoundManager().queueTickingSound(beeSoundInstance);
            }
        } else {
            LOGGER.warn("Skipping Entity with id {}", (Object)clientboundAddMobPacket.getType());
        }
    }

    @Override
    public void handleSetTime(ClientboundSetTimePacket clientboundSetTimePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetTimePacket, this, this.minecraft);
        this.minecraft.level.setGameTime(clientboundSetTimePacket.getGameTime());
        this.minecraft.level.setDayTime(clientboundSetTimePacket.getDayTime());
    }

    @Override
    public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket clientboundSetDefaultSpawnPositionPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetDefaultSpawnPositionPacket, this, this.minecraft);
        this.minecraft.level.setDefaultSpawnPos(clientboundSetDefaultSpawnPositionPacket.getPos(), clientboundSetDefaultSpawnPositionPacket.getAngle());
        Screen screen = this.minecraft.screen;
        if (screen instanceof ReceivingLevelScreen) {
            ReceivingLevelScreen receivingLevelScreen = (ReceivingLevelScreen)screen;
            receivingLevelScreen.loadingPacketsReceived();
        }
    }

    @Override
    public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket clientboundSetPassengersPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetPassengersPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundSetPassengersPacket.getVehicle());
        if (entity == null) {
            LOGGER.warn("Received passengers for unknown entity");
            return;
        }
        boolean bl = entity.hasIndirectPassenger(this.minecraft.player);
        entity.ejectPassengers();
        for (int n : clientboundSetPassengersPacket.getPassengers()) {
            Entity entity2 = this.level.getEntity(n);
            if (entity2 == null) continue;
            entity2.startRiding(entity, true);
            if (entity2 != this.minecraft.player || bl) continue;
            if (entity instanceof Boat) {
                this.minecraft.player.yRotO = entity.getYRot();
                this.minecraft.player.setYRot(entity.getYRot());
                this.minecraft.player.setYHeadRot(entity.getYRot());
            }
            this.minecraft.gui.setOverlayMessage(new TranslatableComponent("mount.onboard", this.minecraft.options.keyShift.getTranslatedKeyMessage()), false);
        }
    }

    @Override
    public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket clientboundSetEntityLinkPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetEntityLinkPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundSetEntityLinkPacket.getSourceId());
        if (entity instanceof Mob) {
            ((Mob)entity).setDelayedLeashHolderId(clientboundSetEntityLinkPacket.getDestId());
        }
    }

    private static ItemStack findTotem(Player player) {
        for (InteractionHand interactionHand : InteractionHand.values()) {
            ItemStack itemStack = player.getItemInHand(interactionHand);
            if (!itemStack.is(Items.TOTEM_OF_UNDYING)) continue;
            return itemStack;
        }
        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }

    @Override
    public void handleEntityEvent(ClientboundEntityEventPacket clientboundEntityEventPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundEntityEventPacket, this, this.minecraft);
        Entity entity = clientboundEntityEventPacket.getEntity(this.level);
        if (entity != null) {
            if (clientboundEntityEventPacket.getEventId() == 21) {
                this.minecraft.getSoundManager().play(new GuardianAttackSoundInstance((Guardian)entity));
            } else if (clientboundEntityEventPacket.getEventId() == 35) {
                int n = 40;
                this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0f, 1.0f, false);
                if (entity == this.minecraft.player) {
                    this.minecraft.gameRenderer.displayItemActivation(ClientPacketListener.findTotem(this.minecraft.player));
                }
            } else {
                entity.handleEntityEvent(clientboundEntityEventPacket.getEventId());
            }
        }
    }

    @Override
    public void handleSetHealth(ClientboundSetHealthPacket clientboundSetHealthPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetHealthPacket, this, this.minecraft);
        this.minecraft.player.hurtTo(clientboundSetHealthPacket.getHealth());
        this.minecraft.player.getFoodData().setFoodLevel(clientboundSetHealthPacket.getFood());
        this.minecraft.player.getFoodData().setSaturation(clientboundSetHealthPacket.getSaturation());
    }

    @Override
    public void handleSetExperience(ClientboundSetExperiencePacket clientboundSetExperiencePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetExperiencePacket, this, this.minecraft);
        this.minecraft.player.setExperienceValues(clientboundSetExperiencePacket.getExperienceProgress(), clientboundSetExperiencePacket.getTotalExperience(), clientboundSetExperiencePacket.getExperienceLevel());
    }

    @Override
    public void handleRespawn(ClientboundRespawnPacket clientboundRespawnPacket) {
        Object object;
        Object object2;
        PacketUtils.ensureRunningOnSameThread(clientboundRespawnPacket, this, this.minecraft);
        ResourceKey<Level> resourceKey = clientboundRespawnPacket.getDimension();
        Holder<DimensionType> holder = clientboundRespawnPacket.getDimensionType();
        LocalPlayer localPlayer = this.minecraft.player;
        int n = localPlayer.getId();
        if (resourceKey != localPlayer.level.dimension()) {
            ClientLevel.ClientLevelData clientLevelData;
            object2 = this.level.getScoreboard();
            object = this.level.getAllMapData();
            boolean bl = clientboundRespawnPacket.isDebug();
            boolean bl2 = clientboundRespawnPacket.isFlat();
            this.levelData = clientLevelData = new ClientLevel.ClientLevelData(this.levelData.getDifficulty(), this.levelData.isHardcore(), bl2);
            this.level = new ClientLevel(this, clientLevelData, resourceKey, holder, this.serverChunkRadius, this.serverSimulationDistance, this.minecraft::getProfiler, this.minecraft.levelRenderer, bl, clientboundRespawnPacket.getSeed());
            this.level.setScoreboard((Scoreboard)object2);
            this.level.addMapData((Map<String, MapItemSavedData>)object);
            this.minecraft.setLevel(this.level);
            this.minecraft.setScreen(new ReceivingLevelScreen());
        }
        object2 = localPlayer.getServerBrand();
        this.minecraft.cameraEntity = null;
        object = this.minecraft.gameMode.createPlayer(this.level, localPlayer.getStats(), localPlayer.getRecipeBook(), localPlayer.isShiftKeyDown(), localPlayer.isSprinting());
        ((Entity)object).setId(n);
        this.minecraft.player = object;
        if (resourceKey != localPlayer.level.dimension()) {
            this.minecraft.getMusicManager().stopPlaying();
        }
        this.minecraft.cameraEntity = object;
        ((Entity)object).getEntityData().assignValues(localPlayer.getEntityData().getAll());
        if (clientboundRespawnPacket.shouldKeepAllPlayerData()) {
            ((LivingEntity)object).getAttributes().assignValues(localPlayer.getAttributes());
        }
        ((LocalPlayer)object).resetPos();
        ((LocalPlayer)object).setServerBrand((String)object2);
        this.level.addPlayer(n, (AbstractClientPlayer)object);
        ((Entity)object).setYRot(-180.0f);
        ((LocalPlayer)object).input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer((Player)object);
        ((Player)object).setReducedDebugInfo(localPlayer.isReducedDebugInfo());
        ((LocalPlayer)object).setShowDeathScreen(localPlayer.shouldShowDeathScreen());
        if (this.minecraft.screen instanceof DeathScreen) {
            this.minecraft.setScreen(null);
        }
        this.minecraft.gameMode.setLocalMode(clientboundRespawnPacket.getPlayerGameType(), clientboundRespawnPacket.getPreviousPlayerGameType());
    }

    @Override
    public void handleExplosion(ClientboundExplodePacket clientboundExplodePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundExplodePacket, this, this.minecraft);
        Explosion explosion = new Explosion(this.minecraft.level, null, clientboundExplodePacket.getX(), clientboundExplodePacket.getY(), clientboundExplodePacket.getZ(), clientboundExplodePacket.getPower(), clientboundExplodePacket.getToBlow());
        explosion.finalizeExplosion(true);
        this.minecraft.player.setDeltaMovement(this.minecraft.player.getDeltaMovement().add(clientboundExplodePacket.getKnockbackX(), clientboundExplodePacket.getKnockbackY(), clientboundExplodePacket.getKnockbackZ()));
    }

    @Override
    public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket clientboundHorseScreenOpenPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundHorseScreenOpenPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundHorseScreenOpenPacket.getEntityId());
        if (entity instanceof AbstractHorse) {
            LocalPlayer localPlayer = this.minecraft.player;
            AbstractHorse abstractHorse = (AbstractHorse)entity;
            SimpleContainer simpleContainer = new SimpleContainer(clientboundHorseScreenOpenPacket.getSize());
            HorseInventoryMenu horseInventoryMenu = new HorseInventoryMenu(clientboundHorseScreenOpenPacket.getContainerId(), localPlayer.getInventory(), simpleContainer, abstractHorse);
            localPlayer.containerMenu = horseInventoryMenu;
            this.minecraft.setScreen(new HorseInventoryScreen(horseInventoryMenu, localPlayer.getInventory(), abstractHorse));
        }
    }

    @Override
    public void handleOpenScreen(ClientboundOpenScreenPacket clientboundOpenScreenPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundOpenScreenPacket, this, this.minecraft);
        MenuScreens.create(clientboundOpenScreenPacket.getType(), this.minecraft, clientboundOpenScreenPacket.getContainerId(), clientboundOpenScreenPacket.getTitle());
    }

    @Override
    public void handleContainerSetSlot(ClientboundContainerSetSlotPacket clientboundContainerSetSlotPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundContainerSetSlotPacket, this, this.minecraft);
        LocalPlayer localPlayer = this.minecraft.player;
        ItemStack itemStack = clientboundContainerSetSlotPacket.getItem();
        int n = clientboundContainerSetSlotPacket.getSlot();
        this.minecraft.getTutorial().onGetItem(itemStack);
        if (clientboundContainerSetSlotPacket.getContainerId() == -1) {
            if (!(this.minecraft.screen instanceof CreativeModeInventoryScreen)) {
                localPlayer.containerMenu.setCarried(itemStack);
            }
        } else if (clientboundContainerSetSlotPacket.getContainerId() == -2) {
            localPlayer.getInventory().setItem(n, itemStack);
        } else {
            Object object;
            boolean bl = false;
            if (this.minecraft.screen instanceof CreativeModeInventoryScreen) {
                object = (CreativeModeInventoryScreen)this.minecraft.screen;
                boolean bl2 = bl = ((CreativeModeInventoryScreen)object).getSelectedTab() != CreativeModeTab.TAB_INVENTORY.getId();
            }
            if (clientboundContainerSetSlotPacket.getContainerId() == 0 && InventoryMenu.isHotbarSlot(n)) {
                if (!itemStack.isEmpty() && (((ItemStack)(object = localPlayer.inventoryMenu.getSlot(n).getItem())).isEmpty() || ((ItemStack)object).getCount() < itemStack.getCount())) {
                    itemStack.setPopTime(5);
                }
                localPlayer.inventoryMenu.setItem(n, clientboundContainerSetSlotPacket.getStateId(), itemStack);
            } else if (!(clientboundContainerSetSlotPacket.getContainerId() != localPlayer.containerMenu.containerId || clientboundContainerSetSlotPacket.getContainerId() == 0 && bl)) {
                localPlayer.containerMenu.setItem(n, clientboundContainerSetSlotPacket.getStateId(), itemStack);
            }
        }
    }

    @Override
    public void handleContainerContent(ClientboundContainerSetContentPacket clientboundContainerSetContentPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundContainerSetContentPacket, this, this.minecraft);
        LocalPlayer localPlayer = this.minecraft.player;
        if (clientboundContainerSetContentPacket.getContainerId() == 0) {
            localPlayer.inventoryMenu.initializeContents(clientboundContainerSetContentPacket.getStateId(), clientboundContainerSetContentPacket.getItems(), clientboundContainerSetContentPacket.getCarriedItem());
        } else if (clientboundContainerSetContentPacket.getContainerId() == localPlayer.containerMenu.containerId) {
            localPlayer.containerMenu.initializeContents(clientboundContainerSetContentPacket.getStateId(), clientboundContainerSetContentPacket.getItems(), clientboundContainerSetContentPacket.getCarriedItem());
        }
    }

    @Override
    public void handleOpenSignEditor(ClientboundOpenSignEditorPacket clientboundOpenSignEditorPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundOpenSignEditorPacket, this, this.minecraft);
        BlockPos blockPos = clientboundOpenSignEditorPacket.getPos();
        BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof SignBlockEntity)) {
            BlockState blockState = this.level.getBlockState(blockPos);
            blockEntity = new SignBlockEntity(blockPos, blockState);
            blockEntity.setLevel(this.level);
        }
        this.minecraft.player.openTextEdit((SignBlockEntity)blockEntity);
    }

    @Override
    public void handleBlockEntityData(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundBlockEntityDataPacket, this, this.minecraft);
        BlockPos blockPos = clientboundBlockEntityDataPacket.getPos();
        this.minecraft.level.getBlockEntity(blockPos, clientboundBlockEntityDataPacket.getType()).ifPresent(blockEntity -> {
            CompoundTag compoundTag = clientboundBlockEntityDataPacket.getTag();
            if (compoundTag != null) {
                blockEntity.load(compoundTag);
            }
            if (blockEntity instanceof CommandBlockEntity && this.minecraft.screen instanceof CommandBlockEditScreen) {
                ((CommandBlockEditScreen)this.minecraft.screen).updateGui();
            }
        });
    }

    @Override
    public void handleContainerSetData(ClientboundContainerSetDataPacket clientboundContainerSetDataPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundContainerSetDataPacket, this, this.minecraft);
        LocalPlayer localPlayer = this.minecraft.player;
        if (localPlayer.containerMenu != null && localPlayer.containerMenu.containerId == clientboundContainerSetDataPacket.getContainerId()) {
            localPlayer.containerMenu.setData(clientboundContainerSetDataPacket.getId(), clientboundContainerSetDataPacket.getValue());
        }
    }

    @Override
    public void handleSetEquipment(ClientboundSetEquipmentPacket clientboundSetEquipmentPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetEquipmentPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundSetEquipmentPacket.getEntity());
        if (entity != null) {
            clientboundSetEquipmentPacket.getSlots().forEach(pair -> entity.setItemSlot((EquipmentSlot)((Object)((Object)pair.getFirst())), (ItemStack)pair.getSecond()));
        }
    }

    @Override
    public void handleContainerClose(ClientboundContainerClosePacket clientboundContainerClosePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundContainerClosePacket, this, this.minecraft);
        this.minecraft.player.clientSideCloseContainer();
    }

    @Override
    public void handleBlockEvent(ClientboundBlockEventPacket clientboundBlockEventPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundBlockEventPacket, this, this.minecraft);
        this.minecraft.level.blockEvent(clientboundBlockEventPacket.getPos(), clientboundBlockEventPacket.getBlock(), clientboundBlockEventPacket.getB0(), clientboundBlockEventPacket.getB1());
    }

    @Override
    public void handleBlockDestruction(ClientboundBlockDestructionPacket clientboundBlockDestructionPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundBlockDestructionPacket, this, this.minecraft);
        this.minecraft.level.destroyBlockProgress(clientboundBlockDestructionPacket.getId(), clientboundBlockDestructionPacket.getPos(), clientboundBlockDestructionPacket.getProgress());
    }

    @Override
    public void handleGameEvent(ClientboundGameEventPacket clientboundGameEventPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundGameEventPacket, this, this.minecraft);
        LocalPlayer localPlayer = this.minecraft.player;
        ClientboundGameEventPacket.Type type = clientboundGameEventPacket.getEvent();
        float f = clientboundGameEventPacket.getParam();
        int n = Mth.floor(f + 0.5f);
        if (type == ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE) {
            ((Player)localPlayer).displayClientMessage(new TranslatableComponent("block.minecraft.spawn.not_valid"), false);
        } else if (type == ClientboundGameEventPacket.START_RAINING) {
            this.level.getLevelData().setRaining(true);
            this.level.setRainLevel(0.0f);
        } else if (type == ClientboundGameEventPacket.STOP_RAINING) {
            this.level.getLevelData().setRaining(false);
            this.level.setRainLevel(1.0f);
        } else if (type == ClientboundGameEventPacket.CHANGE_GAME_MODE) {
            this.minecraft.gameMode.setLocalMode(GameType.byId(n));
        } else if (type == ClientboundGameEventPacket.WIN_GAME) {
            if (n == 0) {
                this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
                this.minecraft.setScreen(new ReceivingLevelScreen());
            } else if (n == 1) {
                this.minecraft.setScreen(new WinScreen(true, () -> this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN))));
            }
        } else if (type == ClientboundGameEventPacket.DEMO_EVENT) {
            Options options = this.minecraft.options;
            if (f == 0.0f) {
                this.minecraft.setScreen(new DemoIntroScreen());
            } else if (f == 101.0f) {
                this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.help.movement", options.keyUp.getTranslatedKeyMessage(), options.keyLeft.getTranslatedKeyMessage(), options.keyDown.getTranslatedKeyMessage(), options.keyRight.getTranslatedKeyMessage()));
            } else if (f == 102.0f) {
                this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.help.jump", options.keyJump.getTranslatedKeyMessage()));
            } else if (f == 103.0f) {
                this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage()));
            } else if (f == 104.0f) {
                this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.day.6", options.keyScreenshot.getTranslatedKeyMessage()));
            }
        } else if (type == ClientboundGameEventPacket.ARROW_HIT_PLAYER) {
            this.level.playSound(localPlayer, localPlayer.getX(), localPlayer.getEyeY(), localPlayer.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.18f, 0.45f);
        } else if (type == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE) {
            this.level.setRainLevel(f);
        } else if (type == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE) {
            this.level.setThunderLevel(f);
        } else if (type == ClientboundGameEventPacket.PUFFER_FISH_STING) {
            this.level.playSound(localPlayer, localPlayer.getX(), localPlayer.getY(), localPlayer.getZ(), SoundEvents.PUFFER_FISH_STING, SoundSource.NEUTRAL, 1.0f, 1.0f);
        } else if (type == ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT) {
            this.level.addParticle(ParticleTypes.ELDER_GUARDIAN, localPlayer.getX(), localPlayer.getY(), localPlayer.getZ(), 0.0, 0.0, 0.0);
            if (n == 1) {
                this.level.playSound(localPlayer, localPlayer.getX(), localPlayer.getY(), localPlayer.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0f, 1.0f);
            }
        } else if (type == ClientboundGameEventPacket.IMMEDIATE_RESPAWN) {
            this.minecraft.player.setShowDeathScreen(f == 0.0f);
        }
    }

    @Override
    public void handleMapItemData(ClientboundMapItemDataPacket clientboundMapItemDataPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundMapItemDataPacket, this, this.minecraft);
        MapRenderer mapRenderer = this.minecraft.gameRenderer.getMapRenderer();
        int n = clientboundMapItemDataPacket.getMapId();
        String string = MapItem.makeKey(n);
        MapItemSavedData mapItemSavedData = this.minecraft.level.getMapData(string);
        if (mapItemSavedData == null) {
            mapItemSavedData = MapItemSavedData.createForClient(clientboundMapItemDataPacket.getScale(), clientboundMapItemDataPacket.isLocked(), this.minecraft.level.dimension());
            this.minecraft.level.setMapData(string, mapItemSavedData);
        }
        clientboundMapItemDataPacket.applyToMap(mapItemSavedData);
        mapRenderer.update(n, mapItemSavedData);
    }

    @Override
    public void handleLevelEvent(ClientboundLevelEventPacket clientboundLevelEventPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundLevelEventPacket, this, this.minecraft);
        if (clientboundLevelEventPacket.isGlobalEvent()) {
            this.minecraft.level.globalLevelEvent(clientboundLevelEventPacket.getType(), clientboundLevelEventPacket.getPos(), clientboundLevelEventPacket.getData());
        } else {
            this.minecraft.level.levelEvent(clientboundLevelEventPacket.getType(), clientboundLevelEventPacket.getPos(), clientboundLevelEventPacket.getData());
        }
    }

    @Override
    public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket clientboundUpdateAdvancementsPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundUpdateAdvancementsPacket, this, this.minecraft);
        this.advancements.update(clientboundUpdateAdvancementsPacket);
    }

    @Override
    public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket clientboundSelectAdvancementsTabPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSelectAdvancementsTabPacket, this, this.minecraft);
        ResourceLocation resourceLocation = clientboundSelectAdvancementsTabPacket.getTab();
        if (resourceLocation == null) {
            this.advancements.setSelectedTab(null, false);
        } else {
            Advancement advancement = this.advancements.getAdvancements().get(resourceLocation);
            this.advancements.setSelectedTab(advancement, false);
        }
    }

    @Override
    public void handleCommands(ClientboundCommandsPacket clientboundCommandsPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundCommandsPacket, this, this.minecraft);
        this.commands = new CommandDispatcher(clientboundCommandsPacket.getRoot());
    }

    @Override
    public void handleStopSoundEvent(ClientboundStopSoundPacket clientboundStopSoundPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundStopSoundPacket, this, this.minecraft);
        this.minecraft.getSoundManager().stop(clientboundStopSoundPacket.getName(), clientboundStopSoundPacket.getSource());
    }

    @Override
    public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket clientboundCommandSuggestionsPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundCommandSuggestionsPacket, this, this.minecraft);
        this.suggestionsProvider.completeCustomSuggestions(clientboundCommandSuggestionsPacket.getId(), clientboundCommandSuggestionsPacket.getSuggestions());
    }

    @Override
    public void handleUpdateRecipes(ClientboundUpdateRecipesPacket clientboundUpdateRecipesPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundUpdateRecipesPacket, this, this.minecraft);
        this.recipeManager.replaceRecipes(clientboundUpdateRecipesPacket.getRecipes());
        MutableSearchTree<RecipeCollection> mutableSearchTree = this.minecraft.getSearchTree(SearchRegistry.RECIPE_COLLECTIONS);
        mutableSearchTree.clear();
        ClientRecipeBook clientRecipeBook = this.minecraft.player.getRecipeBook();
        clientRecipeBook.setupCollections(this.recipeManager.getRecipes());
        clientRecipeBook.getCollections().forEach(mutableSearchTree::add);
        mutableSearchTree.refresh();
    }

    @Override
    public void handleLookAt(ClientboundPlayerLookAtPacket clientboundPlayerLookAtPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPlayerLookAtPacket, this, this.minecraft);
        Vec3 vec3 = clientboundPlayerLookAtPacket.getPosition(this.level);
        if (vec3 != null) {
            this.minecraft.player.lookAt(clientboundPlayerLookAtPacket.getFromAnchor(), vec3);
        }
    }

    @Override
    public void handleTagQueryPacket(ClientboundTagQueryPacket clientboundTagQueryPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundTagQueryPacket, this, this.minecraft);
        if (!this.debugQueryHandler.handleResponse(clientboundTagQueryPacket.getTransactionId(), clientboundTagQueryPacket.getTag())) {
            LOGGER.debug("Got unhandled response to tag query {}", (Object)clientboundTagQueryPacket.getTransactionId());
        }
    }

    @Override
    public void handleAwardStats(ClientboundAwardStatsPacket clientboundAwardStatsPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundAwardStatsPacket, this, this.minecraft);
        for (Map.Entry<Stat<?>, Integer> entry : clientboundAwardStatsPacket.getStats().entrySet()) {
            Stat<?> stat = entry.getKey();
            int n = entry.getValue();
            this.minecraft.player.getStats().setValue(this.minecraft.player, stat, n);
        }
        if (this.minecraft.screen instanceof StatsUpdateListener) {
            ((StatsUpdateListener)((Object)this.minecraft.screen)).onStatsUpdated();
        }
    }

    @Override
    public void handleAddOrRemoveRecipes(ClientboundRecipePacket clientboundRecipePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundRecipePacket, this, this.minecraft);
        ClientRecipeBook clientRecipeBook = this.minecraft.player.getRecipeBook();
        clientRecipeBook.setBookSettings(clientboundRecipePacket.getBookSettings());
        ClientboundRecipePacket.State state = clientboundRecipePacket.getState();
        switch (state) {
            case REMOVE: {
                for (ResourceLocation resourceLocation : clientboundRecipePacket.getRecipes()) {
                    this.recipeManager.byKey(resourceLocation).ifPresent(clientRecipeBook::remove);
                }
                break;
            }
            case INIT: {
                for (ResourceLocation resourceLocation : clientboundRecipePacket.getRecipes()) {
                    this.recipeManager.byKey(resourceLocation).ifPresent(clientRecipeBook::add);
                }
                for (ResourceLocation resourceLocation : clientboundRecipePacket.getHighlights()) {
                    this.recipeManager.byKey(resourceLocation).ifPresent(clientRecipeBook::addHighlight);
                }
                break;
            }
            case ADD: {
                for (ResourceLocation resourceLocation : clientboundRecipePacket.getRecipes()) {
                    this.recipeManager.byKey(resourceLocation).ifPresent(recipe -> {
                        clientRecipeBook.add((Recipe<?>)recipe);
                        clientRecipeBook.addHighlight((Recipe<?>)recipe);
                        RecipeToast.addOrUpdate(this.minecraft.getToasts(), recipe);
                    });
                }
                break;
            }
        }
        clientRecipeBook.getCollections().forEach(recipeCollection -> recipeCollection.updateKnownRecipes(clientRecipeBook));
        if (this.minecraft.screen instanceof RecipeUpdateListener) {
            ((RecipeUpdateListener)((Object)this.minecraft.screen)).recipesUpdated();
        }
    }

    @Override
    public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket clientboundUpdateMobEffectPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundUpdateMobEffectPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundUpdateMobEffectPacket.getEntityId());
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        MobEffect mobEffect = MobEffect.byId(clientboundUpdateMobEffectPacket.getEffectId());
        if (mobEffect == null) {
            return;
        }
        MobEffectInstance mobEffectInstance = new MobEffectInstance(mobEffect, clientboundUpdateMobEffectPacket.getEffectDurationTicks(), clientboundUpdateMobEffectPacket.getEffectAmplifier(), clientboundUpdateMobEffectPacket.isEffectAmbient(), clientboundUpdateMobEffectPacket.isEffectVisible(), clientboundUpdateMobEffectPacket.effectShowsIcon());
        mobEffectInstance.setNoCounter(clientboundUpdateMobEffectPacket.isSuperLongDuration());
        ((LivingEntity)entity).forceAddEffect(mobEffectInstance, null);
    }

    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket clientboundUpdateTagsPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundUpdateTagsPacket, this, this.minecraft);
        clientboundUpdateTagsPacket.getTags().forEach(this::updateTagsForRegistry);
        if (!this.connection.isMemoryConnection()) {
            Blocks.rebuildCache();
        }
        this.minecraft.getSearchTree(SearchRegistry.CREATIVE_TAGS).refresh();
    }

    private <T> void updateTagsForRegistry(ResourceKey<? extends Registry<? extends T>> resourceKey, TagNetworkSerialization.NetworkPayload networkPayload) {
        if (networkPayload.isEmpty()) {
            return;
        }
        Registry registry = this.registryAccess.registry(resourceKey).orElseThrow(() -> new IllegalStateException("Unknown registry " + resourceKey));
        ResourceKey resourceKey2 = resourceKey;
        HashMap hashMap = new HashMap();
        TagNetworkSerialization.deserializeTagsFromNetwork(resourceKey2, registry, networkPayload, hashMap::put);
        registry.bindTags(hashMap);
    }

    @Override
    public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket clientboundPlayerCombatEndPacket) {
    }

    @Override
    public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket clientboundPlayerCombatEnterPacket) {
    }

    @Override
    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket clientboundPlayerCombatKillPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPlayerCombatKillPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundPlayerCombatKillPacket.getPlayerId());
        if (entity == this.minecraft.player) {
            if (this.minecraft.player.shouldShowDeathScreen()) {
                this.minecraft.setScreen(new DeathScreen(clientboundPlayerCombatKillPacket.getMessage(), this.level.getLevelData().isHardcore()));
            } else {
                this.minecraft.player.respawn();
            }
        }
    }

    @Override
    public void handleChangeDifficulty(ClientboundChangeDifficultyPacket clientboundChangeDifficultyPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundChangeDifficultyPacket, this, this.minecraft);
        this.levelData.setDifficulty(clientboundChangeDifficultyPacket.getDifficulty());
        this.levelData.setDifficultyLocked(clientboundChangeDifficultyPacket.isLocked());
    }

    @Override
    public void handleSetCamera(ClientboundSetCameraPacket clientboundSetCameraPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetCameraPacket, this, this.minecraft);
        Entity entity = clientboundSetCameraPacket.getEntity(this.level);
        if (entity != null) {
            this.minecraft.setCameraEntity(entity);
        }
    }

    @Override
    public void handleInitializeBorder(ClientboundInitializeBorderPacket clientboundInitializeBorderPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundInitializeBorderPacket, this, this.minecraft);
        WorldBorder worldBorder = this.level.getWorldBorder();
        worldBorder.setCenter(clientboundInitializeBorderPacket.getNewCenterX(), clientboundInitializeBorderPacket.getNewCenterZ());
        long l = clientboundInitializeBorderPacket.getLerpTime();
        if (l > 0L) {
            worldBorder.lerpSizeBetween(clientboundInitializeBorderPacket.getOldSize(), clientboundInitializeBorderPacket.getNewSize(), l);
        } else {
            worldBorder.setSize(clientboundInitializeBorderPacket.getNewSize());
        }
        worldBorder.setAbsoluteMaxSize(clientboundInitializeBorderPacket.getNewAbsoluteMaxSize());
        worldBorder.setWarningBlocks(clientboundInitializeBorderPacket.getWarningBlocks());
        worldBorder.setWarningTime(clientboundInitializeBorderPacket.getWarningTime());
    }

    @Override
    public void handleSetBorderCenter(ClientboundSetBorderCenterPacket clientboundSetBorderCenterPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetBorderCenterPacket, this, this.minecraft);
        this.level.getWorldBorder().setCenter(clientboundSetBorderCenterPacket.getNewCenterX(), clientboundSetBorderCenterPacket.getNewCenterZ());
    }

    @Override
    public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket clientboundSetBorderLerpSizePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetBorderLerpSizePacket, this, this.minecraft);
        this.level.getWorldBorder().lerpSizeBetween(clientboundSetBorderLerpSizePacket.getOldSize(), clientboundSetBorderLerpSizePacket.getNewSize(), clientboundSetBorderLerpSizePacket.getLerpTime());
    }

    @Override
    public void handleSetBorderSize(ClientboundSetBorderSizePacket clientboundSetBorderSizePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetBorderSizePacket, this, this.minecraft);
        this.level.getWorldBorder().setSize(clientboundSetBorderSizePacket.getSize());
    }

    @Override
    public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket clientboundSetBorderWarningDistancePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetBorderWarningDistancePacket, this, this.minecraft);
        this.level.getWorldBorder().setWarningBlocks(clientboundSetBorderWarningDistancePacket.getWarningBlocks());
    }

    @Override
    public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket clientboundSetBorderWarningDelayPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetBorderWarningDelayPacket, this, this.minecraft);
        this.level.getWorldBorder().setWarningTime(clientboundSetBorderWarningDelayPacket.getWarningDelay());
    }

    @Override
    public void handleTitlesClear(ClientboundClearTitlesPacket clientboundClearTitlesPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundClearTitlesPacket, this, this.minecraft);
        this.minecraft.gui.clear();
        if (clientboundClearTitlesPacket.shouldResetTimes()) {
            this.minecraft.gui.resetTitleTimes();
        }
    }

    @Override
    public void setActionBarText(ClientboundSetActionBarTextPacket clientboundSetActionBarTextPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetActionBarTextPacket, this, this.minecraft);
        this.minecraft.gui.setOverlayMessage(clientboundSetActionBarTextPacket.getText(), false);
    }

    @Override
    public void setTitleText(ClientboundSetTitleTextPacket clientboundSetTitleTextPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetTitleTextPacket, this, this.minecraft);
        this.minecraft.gui.setTitle(clientboundSetTitleTextPacket.getText());
    }

    @Override
    public void setSubtitleText(ClientboundSetSubtitleTextPacket clientboundSetSubtitleTextPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetSubtitleTextPacket, this, this.minecraft);
        this.minecraft.gui.setSubtitle(clientboundSetSubtitleTextPacket.getText());
    }

    @Override
    public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket clientboundSetTitlesAnimationPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetTitlesAnimationPacket, this, this.minecraft);
        this.minecraft.gui.setTimes(clientboundSetTitlesAnimationPacket.getFadeIn(), clientboundSetTitlesAnimationPacket.getStay(), clientboundSetTitlesAnimationPacket.getFadeOut());
    }

    @Override
    public void handleTabListCustomisation(ClientboundTabListPacket clientboundTabListPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundTabListPacket, this, this.minecraft);
        this.minecraft.gui.getTabList().setHeader(clientboundTabListPacket.getHeader().getString().isEmpty() ? null : clientboundTabListPacket.getHeader());
        this.minecraft.gui.getTabList().setFooter(clientboundTabListPacket.getFooter().getString().isEmpty() ? null : clientboundTabListPacket.getFooter());
    }

    @Override
    public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket clientboundRemoveMobEffectPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundRemoveMobEffectPacket, this, this.minecraft);
        Entity entity = clientboundRemoveMobEffectPacket.getEntity(this.level);
        if (entity instanceof LivingEntity) {
            ((LivingEntity)entity).removeEffectNoUpdate(clientboundRemoveMobEffectPacket.getEffect());
        }
    }

    @Override
    public void handlePlayerInfo(ClientboundPlayerInfoPacket clientboundPlayerInfoPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPlayerInfoPacket, this, this.minecraft);
        for (ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate : clientboundPlayerInfoPacket.getEntries()) {
            if (clientboundPlayerInfoPacket.getAction() == ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER) {
                this.minecraft.getPlayerSocialManager().removePlayer(playerUpdate.getProfile().getId());
                this.playerInfoMap.remove(playerUpdate.getProfile().getId());
                continue;
            }
            PlayerInfo playerInfo = this.playerInfoMap.get(playerUpdate.getProfile().getId());
            if (clientboundPlayerInfoPacket.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
                playerInfo = new PlayerInfo(playerUpdate);
                this.playerInfoMap.put(playerInfo.getProfile().getId(), playerInfo);
                this.minecraft.getPlayerSocialManager().addPlayer(playerInfo);
            }
            if (playerInfo == null) continue;
            switch (clientboundPlayerInfoPacket.getAction()) {
                case ADD_PLAYER: {
                    playerInfo.setGameMode(playerUpdate.getGameMode());
                    playerInfo.setLatency(playerUpdate.getLatency());
                    playerInfo.setTabListDisplayName(playerUpdate.getDisplayName());
                    break;
                }
                case UPDATE_GAME_MODE: {
                    playerInfo.setGameMode(playerUpdate.getGameMode());
                    break;
                }
                case UPDATE_LATENCY: {
                    playerInfo.setLatency(playerUpdate.getLatency());
                    break;
                }
                case UPDATE_DISPLAY_NAME: {
                    playerInfo.setTabListDisplayName(playerUpdate.getDisplayName());
                }
            }
        }
    }

    @Override
    public void handleKeepAlive(ClientboundKeepAlivePacket clientboundKeepAlivePacket) {
        this.send(new ServerboundKeepAlivePacket(clientboundKeepAlivePacket.getId()));
    }

    @Override
    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket clientboundPlayerAbilitiesPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPlayerAbilitiesPacket, this, this.minecraft);
        LocalPlayer localPlayer = this.minecraft.player;
        localPlayer.getAbilities().flying = clientboundPlayerAbilitiesPacket.isFlying();
        localPlayer.getAbilities().instabuild = clientboundPlayerAbilitiesPacket.canInstabuild();
        localPlayer.getAbilities().invulnerable = clientboundPlayerAbilitiesPacket.isInvulnerable();
        localPlayer.getAbilities().mayfly = clientboundPlayerAbilitiesPacket.canFly();
        localPlayer.getAbilities().setFlyingSpeed(clientboundPlayerAbilitiesPacket.getFlyingSpeed());
        localPlayer.getAbilities().setWalkingSpeed(clientboundPlayerAbilitiesPacket.getWalkingSpeed());
    }

    @Override
    public void handleSoundEvent(ClientboundSoundPacket clientboundSoundPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSoundPacket, this, this.minecraft);
        this.minecraft.level.playSound(this.minecraft.player, clientboundSoundPacket.getX(), clientboundSoundPacket.getY(), clientboundSoundPacket.getZ(), clientboundSoundPacket.getSound(), clientboundSoundPacket.getSource(), clientboundSoundPacket.getVolume(), clientboundSoundPacket.getPitch());
    }

    @Override
    public void handleSoundEntityEvent(ClientboundSoundEntityPacket clientboundSoundEntityPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSoundEntityPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundSoundEntityPacket.getId());
        if (entity == null) {
            return;
        }
        this.minecraft.level.playSound((Player)this.minecraft.player, entity, clientboundSoundEntityPacket.getSound(), clientboundSoundEntityPacket.getSource(), clientboundSoundEntityPacket.getVolume(), clientboundSoundEntityPacket.getPitch());
    }

    @Override
    public void handleCustomSoundEvent(ClientboundCustomSoundPacket clientboundCustomSoundPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundCustomSoundPacket, this, this.minecraft);
        this.minecraft.getSoundManager().play(new SimpleSoundInstance(clientboundCustomSoundPacket.getName(), clientboundCustomSoundPacket.getSource(), clientboundCustomSoundPacket.getVolume(), clientboundCustomSoundPacket.getPitch(), false, 0, SoundInstance.Attenuation.LINEAR, clientboundCustomSoundPacket.getX(), clientboundCustomSoundPacket.getY(), clientboundCustomSoundPacket.getZ(), false));
    }

    @Override
    public void handleResourcePack(ClientboundResourcePackPacket clientboundResourcePackPacket) {
        String string = clientboundResourcePackPacket.getUrl();
        String string2 = clientboundResourcePackPacket.getHash();
        boolean bl = clientboundResourcePackPacket.isRequired();
        if (!this.validateResourcePackUrl(string)) {
            return;
        }
        if (string.startsWith("level://")) {
            try {
                String string3 = URLDecoder.decode(string.substring("level://".length()), StandardCharsets.UTF_8.toString());
                File file = new File(this.minecraft.gameDirectory, "saves");
                File file2 = new File(file, string3);
                if (file2.isFile()) {
                    this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                    CompletableFuture<Void> completableFuture = this.minecraft.getClientPackSource().setServerPack(file2, PackSource.WORLD);
                    this.downloadCallback(completableFuture);
                    return;
                }
            }
            catch (UnsupportedEncodingException unsupportedEncodingException) {
                // empty catch block
            }
            this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
            return;
        }
        ServerData serverData = this.minecraft.getCurrentServer();
        if (serverData != null && serverData.getResourcePackStatus() == ServerData.ServerPackStatus.ENABLED) {
            this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
            this.downloadCallback(this.minecraft.getClientPackSource().downloadAndSelectResourcePack(string, string2, true));
        } else if (serverData == null || serverData.getResourcePackStatus() == ServerData.ServerPackStatus.PROMPT || bl && serverData.getResourcePackStatus() == ServerData.ServerPackStatus.DISABLED) {
            this.minecraft.execute(() -> this.minecraft.setScreen(new ConfirmScreen(bl2 -> {
                this.minecraft.setScreen(null);
                ServerData serverData = this.minecraft.getCurrentServer();
                if (bl2) {
                    if (serverData != null) {
                        serverData.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
                    }
                    this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                    this.downloadCallback(this.minecraft.getClientPackSource().downloadAndSelectResourcePack(string, string2, true));
                } else {
                    this.send(ServerboundResourcePackPacket.Action.DECLINED);
                    if (bl) {
                        this.connection.disconnect(new TranslatableComponent("multiplayer.requiredTexturePrompt.disconnect"));
                    } else if (serverData != null) {
                        serverData.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
                    }
                }
                if (serverData != null) {
                    ServerList.saveSingleServer(serverData);
                }
            }, bl ? new TranslatableComponent("multiplayer.requiredTexturePrompt.line1") : new TranslatableComponent("multiplayer.texturePrompt.line1"), ClientPacketListener.preparePackPrompt(bl ? new TranslatableComponent("multiplayer.requiredTexturePrompt.line2").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD) : new TranslatableComponent("multiplayer.texturePrompt.line2"), clientboundResourcePackPacket.getPrompt()), bl ? CommonComponents.GUI_PROCEED : CommonComponents.GUI_YES, bl ? new TranslatableComponent("menu.disconnect") : CommonComponents.GUI_NO)));
        } else {
            this.send(ServerboundResourcePackPacket.Action.DECLINED);
            if (bl) {
                this.connection.disconnect(new TranslatableComponent("multiplayer.requiredTexturePrompt.disconnect"));
            }
        }
    }

    private static Component preparePackPrompt(Component component, @Nullable Component component2) {
        if (component2 == null) {
            return component;
        }
        return new TranslatableComponent("multiplayer.texturePrompt.serverPrompt", component, component2);
    }

    private boolean validateResourcePackUrl(String string) {
        try {
            URI uRI = new URI(string);
            String string2 = uRI.getScheme();
            boolean bl = "level".equals(string2);
            if (!("http".equals(string2) || "https".equals(string2) || bl)) {
                throw new URISyntaxException(string, "Wrong protocol");
            }
            if (bl && (string.contains("..") || !string.endsWith("/resources.zip"))) {
                throw new URISyntaxException(string, "Invalid levelstorage resourcepack path");
            }
        }
        catch (URISyntaxException uRISyntaxException) {
            this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
            return false;
        }
        return true;
    }

    private void downloadCallback(CompletableFuture<?> completableFuture) {
        ((CompletableFuture)completableFuture.thenRun(() -> this.send(ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED))).exceptionally(throwable -> {
            this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
            return null;
        });
    }

    private void send(ServerboundResourcePackPacket.Action action) {
        this.connection.send(new ServerboundResourcePackPacket(action));
    }

    @Override
    public void handleBossUpdate(ClientboundBossEventPacket clientboundBossEventPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundBossEventPacket, this, this.minecraft);
        this.minecraft.gui.getBossOverlay().update(clientboundBossEventPacket);
    }

    @Override
    public void handleItemCooldown(ClientboundCooldownPacket clientboundCooldownPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundCooldownPacket, this, this.minecraft);
        if (clientboundCooldownPacket.getDuration() == 0) {
            this.minecraft.player.getCooldowns().removeCooldown(clientboundCooldownPacket.getItem());
        } else {
            this.minecraft.player.getCooldowns().addCooldown(clientboundCooldownPacket.getItem(), clientboundCooldownPacket.getDuration());
        }
    }

    @Override
    public void handleMoveVehicle(ClientboundMoveVehiclePacket clientboundMoveVehiclePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundMoveVehiclePacket, this, this.minecraft);
        Entity entity = this.minecraft.player.getRootVehicle();
        if (entity != this.minecraft.player && entity.isControlledByLocalInstance()) {
            entity.absMoveTo(clientboundMoveVehiclePacket.getX(), clientboundMoveVehiclePacket.getY(), clientboundMoveVehiclePacket.getZ(), clientboundMoveVehiclePacket.getYRot(), clientboundMoveVehiclePacket.getXRot());
            this.connection.send(new ServerboundMoveVehiclePacket(entity));
        }
    }

    @Override
    public void handleOpenBook(ClientboundOpenBookPacket clientboundOpenBookPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundOpenBookPacket, this, this.minecraft);
        ItemStack itemStack = this.minecraft.player.getItemInHand(clientboundOpenBookPacket.getHand());
        if (itemStack.is(Items.WRITTEN_BOOK)) {
            this.minecraft.setScreen(new BookViewScreen(new BookViewScreen.WrittenBookAccess(itemStack)));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void handleCustomPayload(ClientboundCustomPayloadPacket clientboundCustomPayloadPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundCustomPayloadPacket, this, this.minecraft);
        ResourceLocation resourceLocation = clientboundCustomPayloadPacket.getIdentifier();
        FriendlyByteBuf friendlyByteBuf = null;
        try {
            friendlyByteBuf = clientboundCustomPayloadPacket.getData();
            if (ClientboundCustomPayloadPacket.BRAND.equals(resourceLocation)) {
                String string = friendlyByteBuf.readUtf();
                this.minecraft.player.setServerBrand(string);
                this.telemetryManager.onServerBrandReceived(string);
            } else if (ClientboundCustomPayloadPacket.DEBUG_PATHFINDING_PACKET.equals(resourceLocation)) {
                int n = friendlyByteBuf.readInt();
                float f = friendlyByteBuf.readFloat();
                Path path = Path.createFromStream(friendlyByteBuf);
                this.minecraft.debugRenderer.pathfindingRenderer.addPath(n, path, f);
            } else if (ClientboundCustomPayloadPacket.DEBUG_NEIGHBORSUPDATE_PACKET.equals(resourceLocation)) {
                long l = friendlyByteBuf.readVarLong();
                BlockPos blockPos = friendlyByteBuf.readBlockPos();
                ((NeighborsUpdateRenderer)this.minecraft.debugRenderer.neighborsUpdateRenderer).addUpdate(l, blockPos);
            } else if (ClientboundCustomPayloadPacket.DEBUG_STRUCTURES_PACKET.equals(resourceLocation)) {
                DimensionType dimensionType = this.registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).get(friendlyByteBuf.readResourceLocation());
                BoundingBox boundingBox = new BoundingBox(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
                int n = friendlyByteBuf.readInt();
                ArrayList arrayList = Lists.newArrayList();
                ArrayList arrayList2 = Lists.newArrayList();
                for (int i = 0; i < n; ++i) {
                    arrayList.add(new BoundingBox(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt()));
                    arrayList2.add(friendlyByteBuf.readBoolean());
                }
                this.minecraft.debugRenderer.structureRenderer.addBoundingBox(boundingBox, arrayList, arrayList2, dimensionType);
            } else if (ClientboundCustomPayloadPacket.DEBUG_WORLDGENATTEMPT_PACKET.equals(resourceLocation)) {
                ((WorldGenAttemptRenderer)this.minecraft.debugRenderer.worldGenAttemptRenderer).addPos(friendlyByteBuf.readBlockPos(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
            } else if (ClientboundCustomPayloadPacket.DEBUG_VILLAGE_SECTIONS.equals(resourceLocation)) {
                int n;
                int n2 = friendlyByteBuf.readInt();
                for (n = 0; n < n2; ++n) {
                    this.minecraft.debugRenderer.villageSectionsDebugRenderer.setVillageSection(friendlyByteBuf.readSectionPos());
                }
                n = friendlyByteBuf.readInt();
                for (int i = 0; i < n; ++i) {
                    this.minecraft.debugRenderer.villageSectionsDebugRenderer.setNotVillageSection(friendlyByteBuf.readSectionPos());
                }
            } else if (ClientboundCustomPayloadPacket.DEBUG_POI_ADDED_PACKET.equals(resourceLocation)) {
                BlockPos blockPos = friendlyByteBuf.readBlockPos();
                String string = friendlyByteBuf.readUtf();
                int n = friendlyByteBuf.readInt();
                BrainDebugRenderer.PoiInfo poiInfo = new BrainDebugRenderer.PoiInfo(blockPos, string, n);
                this.minecraft.debugRenderer.brainDebugRenderer.addPoi(poiInfo);
            } else if (ClientboundCustomPayloadPacket.DEBUG_POI_REMOVED_PACKET.equals(resourceLocation)) {
                BlockPos blockPos = friendlyByteBuf.readBlockPos();
                this.minecraft.debugRenderer.brainDebugRenderer.removePoi(blockPos);
            } else if (ClientboundCustomPayloadPacket.DEBUG_POI_TICKET_COUNT_PACKET.equals(resourceLocation)) {
                BlockPos blockPos = friendlyByteBuf.readBlockPos();
                int n = friendlyByteBuf.readInt();
                this.minecraft.debugRenderer.brainDebugRenderer.setFreeTicketCount(blockPos, n);
            } else if (ClientboundCustomPayloadPacket.DEBUG_GOAL_SELECTOR.equals(resourceLocation)) {
                BlockPos blockPos = friendlyByteBuf.readBlockPos();
                int n = friendlyByteBuf.readInt();
                int n3 = friendlyByteBuf.readInt();
                ArrayList arrayList = Lists.newArrayList();
                for (int i = 0; i < n3; ++i) {
                    int n4 = friendlyByteBuf.readInt();
                    boolean bl = friendlyByteBuf.readBoolean();
                    String string = friendlyByteBuf.readUtf(255);
                    arrayList.add(new GoalSelectorDebugRenderer.DebugGoal(blockPos, n4, string, bl));
                }
                this.minecraft.debugRenderer.goalSelectorRenderer.addGoalSelector(n, arrayList);
            } else if (ClientboundCustomPayloadPacket.DEBUG_RAIDS.equals(resourceLocation)) {
                int n = friendlyByteBuf.readInt();
                ArrayList arrayList = Lists.newArrayList();
                for (int i = 0; i < n; ++i) {
                    arrayList.add(friendlyByteBuf.readBlockPos());
                }
                this.minecraft.debugRenderer.raidDebugRenderer.setRaidCenters(arrayList);
            } else if (ClientboundCustomPayloadPacket.DEBUG_BRAIN.equals(resourceLocation)) {
                int n;
                int n5;
                int n6;
                int n7;
                int n8;
                double d = friendlyByteBuf.readDouble();
                double d2 = friendlyByteBuf.readDouble();
                double d3 = friendlyByteBuf.readDouble();
                PositionImpl positionImpl = new PositionImpl(d, d2, d3);
                UUID uUID = friendlyByteBuf.readUUID();
                int n9 = friendlyByteBuf.readInt();
                String string = friendlyByteBuf.readUtf();
                String string2 = friendlyByteBuf.readUtf();
                int n10 = friendlyByteBuf.readInt();
                float f = friendlyByteBuf.readFloat();
                float f2 = friendlyByteBuf.readFloat();
                String string3 = friendlyByteBuf.readUtf();
                boolean bl = friendlyByteBuf.readBoolean();
                Path path = bl ? Path.createFromStream(friendlyByteBuf) : null;
                boolean bl2 = friendlyByteBuf.readBoolean();
                BrainDebugRenderer.BrainDump brainDump = new BrainDebugRenderer.BrainDump(uUID, n9, string, string2, n10, f, f2, positionImpl, string3, path, bl2);
                int n11 = friendlyByteBuf.readVarInt();
                for (n8 = 0; n8 < n11; ++n8) {
                    String string4 = friendlyByteBuf.readUtf();
                    brainDump.activities.add(string4);
                }
                n8 = friendlyByteBuf.readVarInt();
                for (n7 = 0; n7 < n8; ++n7) {
                    String string5 = friendlyByteBuf.readUtf();
                    brainDump.behaviors.add(string5);
                }
                n7 = friendlyByteBuf.readVarInt();
                for (n6 = 0; n6 < n7; ++n6) {
                    String string6 = friendlyByteBuf.readUtf();
                    brainDump.memories.add(string6);
                }
                n6 = friendlyByteBuf.readVarInt();
                for (n5 = 0; n5 < n6; ++n5) {
                    BlockPos blockPos = friendlyByteBuf.readBlockPos();
                    brainDump.pois.add(blockPos);
                }
                n5 = friendlyByteBuf.readVarInt();
                for (n = 0; n < n5; ++n) {
                    BlockPos blockPos = friendlyByteBuf.readBlockPos();
                    brainDump.potentialPois.add(blockPos);
                }
                n = friendlyByteBuf.readVarInt();
                for (int i = 0; i < n; ++i) {
                    String string7 = friendlyByteBuf.readUtf();
                    brainDump.gossips.add(string7);
                }
                this.minecraft.debugRenderer.brainDebugRenderer.addOrUpdateBrainDump(brainDump);
            } else if (ClientboundCustomPayloadPacket.DEBUG_BEE.equals(resourceLocation)) {
                int n;
                double d = friendlyByteBuf.readDouble();
                double d4 = friendlyByteBuf.readDouble();
                double d5 = friendlyByteBuf.readDouble();
                PositionImpl positionImpl = new PositionImpl(d, d4, d5);
                UUID uUID = friendlyByteBuf.readUUID();
                int n12 = friendlyByteBuf.readInt();
                boolean bl = friendlyByteBuf.readBoolean();
                BlockPos blockPos = null;
                if (bl) {
                    blockPos = friendlyByteBuf.readBlockPos();
                }
                boolean bl3 = friendlyByteBuf.readBoolean();
                BlockPos blockPos2 = null;
                if (bl3) {
                    blockPos2 = friendlyByteBuf.readBlockPos();
                }
                int n13 = friendlyByteBuf.readInt();
                boolean bl4 = friendlyByteBuf.readBoolean();
                Path path = null;
                if (bl4) {
                    path = Path.createFromStream(friendlyByteBuf);
                }
                BeeDebugRenderer.BeeInfo beeInfo = new BeeDebugRenderer.BeeInfo(uUID, n12, positionImpl, path, blockPos, blockPos2, n13);
                int n14 = friendlyByteBuf.readVarInt();
                for (n = 0; n < n14; ++n) {
                    String string = friendlyByteBuf.readUtf();
                    beeInfo.goals.add(string);
                }
                n = friendlyByteBuf.readVarInt();
                for (int i = 0; i < n; ++i) {
                    BlockPos blockPos3 = friendlyByteBuf.readBlockPos();
                    beeInfo.blacklistedHives.add(blockPos3);
                }
                this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateBeeInfo(beeInfo);
            } else if (ClientboundCustomPayloadPacket.DEBUG_HIVE.equals(resourceLocation)) {
                BlockPos blockPos = friendlyByteBuf.readBlockPos();
                String string = friendlyByteBuf.readUtf();
                int n = friendlyByteBuf.readInt();
                int n15 = friendlyByteBuf.readInt();
                boolean bl = friendlyByteBuf.readBoolean();
                BeeDebugRenderer.HiveInfo hiveInfo = new BeeDebugRenderer.HiveInfo(blockPos, string, n, n15, bl, this.level.getGameTime());
                this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateHiveInfo(hiveInfo);
            } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_CLEAR.equals(resourceLocation)) {
                this.minecraft.debugRenderer.gameTestDebugRenderer.clear();
            } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER.equals(resourceLocation)) {
                BlockPos blockPos = friendlyByteBuf.readBlockPos();
                int n = friendlyByteBuf.readInt();
                String string = friendlyByteBuf.readUtf();
                int n16 = friendlyByteBuf.readInt();
                this.minecraft.debugRenderer.gameTestDebugRenderer.addMarker(blockPos, n, string, n16);
            } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT.equals(resourceLocation)) {
                GameEvent gameEvent = Registry.GAME_EVENT.get(new ResourceLocation(friendlyByteBuf.readUtf()));
                BlockPos blockPos = friendlyByteBuf.readBlockPos();
                this.minecraft.debugRenderer.gameEventListenerRenderer.trackGameEvent(gameEvent, blockPos);
            } else if (ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT_LISTENER.equals(resourceLocation)) {
                ResourceLocation resourceLocation2 = friendlyByteBuf.readResourceLocation();
                Object obj = Registry.POSITION_SOURCE_TYPE.getOptional(resourceLocation2).orElseThrow(() -> new IllegalArgumentException("Unknown position source type " + resourceLocation2)).read(friendlyByteBuf);
                int n = friendlyByteBuf.readVarInt();
                this.minecraft.debugRenderer.gameEventListenerRenderer.trackListener((PositionSource)obj, n);
            } else {
                LOGGER.warn("Unknown custom packed identifier: {}", (Object)resourceLocation);
            }
        }
        finally {
            if (friendlyByteBuf != null) {
                friendlyByteBuf.release();
            }
        }
    }

    @Override
    public void handleAddObjective(ClientboundSetObjectivePacket clientboundSetObjectivePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetObjectivePacket, this, this.minecraft);
        Scoreboard scoreboard = this.level.getScoreboard();
        String string = clientboundSetObjectivePacket.getObjectiveName();
        if (clientboundSetObjectivePacket.getMethod() == 0) {
            scoreboard.addObjective(string, ObjectiveCriteria.DUMMY, clientboundSetObjectivePacket.getDisplayName(), clientboundSetObjectivePacket.getRenderType());
        } else if (scoreboard.hasObjective(string)) {
            Objective objective = scoreboard.getObjective(string);
            if (clientboundSetObjectivePacket.getMethod() == 1) {
                scoreboard.removeObjective(objective);
            } else if (clientboundSetObjectivePacket.getMethod() == 2) {
                objective.setRenderType(clientboundSetObjectivePacket.getRenderType());
                objective.setDisplayName(clientboundSetObjectivePacket.getDisplayName());
            }
        }
    }

    @Override
    public void handleSetScore(ClientboundSetScorePacket clientboundSetScorePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetScorePacket, this, this.minecraft);
        Scoreboard scoreboard = this.level.getScoreboard();
        String string = clientboundSetScorePacket.getObjectiveName();
        switch (clientboundSetScorePacket.getMethod()) {
            case CHANGE: {
                Objective objective = scoreboard.getOrCreateObjective(string);
                Score score = scoreboard.getOrCreatePlayerScore(clientboundSetScorePacket.getOwner(), objective);
                score.setScore(clientboundSetScorePacket.getScore());
                break;
            }
            case REMOVE: {
                scoreboard.resetPlayerScore(clientboundSetScorePacket.getOwner(), scoreboard.getObjective(string));
            }
        }
    }

    @Override
    public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket clientboundSetDisplayObjectivePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetDisplayObjectivePacket, this, this.minecraft);
        Scoreboard scoreboard = this.level.getScoreboard();
        String string = clientboundSetDisplayObjectivePacket.getObjectiveName();
        Objective objective = string == null ? null : scoreboard.getOrCreateObjective(string);
        scoreboard.setDisplayObjective(clientboundSetDisplayObjectivePacket.getSlot(), objective);
    }

    @Override
    public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket clientboundSetPlayerTeamPacket) {
        PlayerTeam playerTeam;
        PacketUtils.ensureRunningOnSameThread(clientboundSetPlayerTeamPacket, this, this.minecraft);
        Scoreboard scoreboard = this.level.getScoreboard();
        ClientboundSetPlayerTeamPacket.Action action = clientboundSetPlayerTeamPacket.getTeamAction();
        if (action == ClientboundSetPlayerTeamPacket.Action.ADD) {
            playerTeam = scoreboard.addPlayerTeam(clientboundSetPlayerTeamPacket.getName());
        } else {
            playerTeam = scoreboard.getPlayerTeam(clientboundSetPlayerTeamPacket.getName());
            if (playerTeam == null) {
                LOGGER.warn("Received packet for unknown team {}: team action: {}, player action: {}", new Object[]{clientboundSetPlayerTeamPacket.getName(), clientboundSetPlayerTeamPacket.getTeamAction(), clientboundSetPlayerTeamPacket.getPlayerAction()});
                return;
            }
        }
        Optional<ClientboundSetPlayerTeamPacket.Parameters> optional = clientboundSetPlayerTeamPacket.getParameters();
        optional.ifPresent(parameters -> {
            Team.CollisionRule collisionRule;
            playerTeam.setDisplayName(parameters.getDisplayName());
            playerTeam.setColor(parameters.getColor());
            playerTeam.unpackOptions(parameters.getOptions());
            Team.Visibility visibility = Team.Visibility.byName(parameters.getNametagVisibility());
            if (visibility != null) {
                playerTeam.setNameTagVisibility(visibility);
            }
            if ((collisionRule = Team.CollisionRule.byName(parameters.getCollisionRule())) != null) {
                playerTeam.setCollisionRule(collisionRule);
            }
            playerTeam.setPlayerPrefix(parameters.getPlayerPrefix());
            playerTeam.setPlayerSuffix(parameters.getPlayerSuffix());
        });
        ClientboundSetPlayerTeamPacket.Action action2 = clientboundSetPlayerTeamPacket.getPlayerAction();
        if (action2 == ClientboundSetPlayerTeamPacket.Action.ADD) {
            for (String string : clientboundSetPlayerTeamPacket.getPlayers()) {
                scoreboard.addPlayerToTeam(string, playerTeam);
            }
        } else if (action2 == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
            for (String string : clientboundSetPlayerTeamPacket.getPlayers()) {
                scoreboard.removePlayerFromTeam(string, playerTeam);
            }
        }
        if (action == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
            scoreboard.removePlayerTeam(playerTeam);
        }
    }

    @Override
    public void handleParticleEvent(ClientboundLevelParticlesPacket clientboundLevelParticlesPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundLevelParticlesPacket, this, this.minecraft);
        if (clientboundLevelParticlesPacket.getCount() == 0) {
            double d = clientboundLevelParticlesPacket.getMaxSpeed() * clientboundLevelParticlesPacket.getXDist();
            double d2 = clientboundLevelParticlesPacket.getMaxSpeed() * clientboundLevelParticlesPacket.getYDist();
            double d3 = clientboundLevelParticlesPacket.getMaxSpeed() * clientboundLevelParticlesPacket.getZDist();
            try {
                this.level.addParticle(clientboundLevelParticlesPacket.getParticle(), clientboundLevelParticlesPacket.isOverrideLimiter(), clientboundLevelParticlesPacket.getX(), clientboundLevelParticlesPacket.getY(), clientboundLevelParticlesPacket.getZ(), d, d2, d3);
            }
            catch (Throwable throwable) {
                LOGGER.warn("Could not spawn particle effect {}", (Object)clientboundLevelParticlesPacket.getParticle());
            }
        } else {
            for (int i = 0; i < clientboundLevelParticlesPacket.getCount(); ++i) {
                double d = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getXDist();
                double d4 = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getYDist();
                double d5 = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getZDist();
                double d6 = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getMaxSpeed();
                double d7 = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getMaxSpeed();
                double d8 = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getMaxSpeed();
                try {
                    this.level.addParticle(clientboundLevelParticlesPacket.getParticle(), clientboundLevelParticlesPacket.isOverrideLimiter(), clientboundLevelParticlesPacket.getX() + d, clientboundLevelParticlesPacket.getY() + d4, clientboundLevelParticlesPacket.getZ() + d5, d6, d7, d8);
                    continue;
                }
                catch (Throwable throwable) {
                    LOGGER.warn("Could not spawn particle effect {}", (Object)clientboundLevelParticlesPacket.getParticle());
                    return;
                }
            }
        }
    }

    @Override
    public void handlePing(ClientboundPingPacket clientboundPingPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPingPacket, this, this.minecraft);
        this.send(new ServerboundPongPacket(clientboundPingPacket.getId()));
    }

    @Override
    public void handleUpdateAttributes(ClientboundUpdateAttributesPacket clientboundUpdateAttributesPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundUpdateAttributesPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundUpdateAttributesPacket.getEntityId());
        if (entity == null) {
            return;
        }
        if (!(entity instanceof LivingEntity)) {
            throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
        }
        AttributeMap attributeMap = ((LivingEntity)entity).getAttributes();
        for (ClientboundUpdateAttributesPacket.AttributeSnapshot attributeSnapshot : clientboundUpdateAttributesPacket.getValues()) {
            AttributeInstance attributeInstance = attributeMap.getInstance(attributeSnapshot.getAttribute());
            if (attributeInstance == null) {
                LOGGER.warn("Entity {} does not have attribute {}", (Object)entity, (Object)Registry.ATTRIBUTE.getKey(attributeSnapshot.getAttribute()));
                continue;
            }
            attributeInstance.setBaseValue(attributeSnapshot.getBase());
            attributeInstance.removeModifiers();
            for (AttributeModifier attributeModifier : attributeSnapshot.getModifiers()) {
                attributeInstance.addTransientModifier(attributeModifier);
            }
        }
    }

    @Override
    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket clientboundPlaceGhostRecipePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPlaceGhostRecipePacket, this, this.minecraft);
        AbstractContainerMenu abstractContainerMenu = this.minecraft.player.containerMenu;
        if (abstractContainerMenu.containerId != clientboundPlaceGhostRecipePacket.getContainerId()) {
            return;
        }
        this.recipeManager.byKey(clientboundPlaceGhostRecipePacket.getRecipe()).ifPresent(recipe -> {
            if (this.minecraft.screen instanceof RecipeUpdateListener) {
                RecipeBookComponent recipeBookComponent = ((RecipeUpdateListener)((Object)this.minecraft.screen)).getRecipeBookComponent();
                recipeBookComponent.setupGhostRecipe((Recipe<?>)recipe, (List<Slot>)abstractContainerMenu.slots);
            }
        });
    }

    @Override
    public void handleLightUpdatePacket(ClientboundLightUpdatePacket clientboundLightUpdatePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundLightUpdatePacket, this, this.minecraft);
        int n = clientboundLightUpdatePacket.getX();
        int n2 = clientboundLightUpdatePacket.getZ();
        ClientboundLightUpdatePacketData clientboundLightUpdatePacketData = clientboundLightUpdatePacket.getLightData();
        this.level.queueLightUpdate(() -> this.applyLightData(n, n2, clientboundLightUpdatePacketData));
    }

    private void applyLightData(int n, int n2, ClientboundLightUpdatePacketData clientboundLightUpdatePacketData) {
        LevelLightEngine levelLightEngine = this.level.getChunkSource().getLightEngine();
        BitSet bitSet = clientboundLightUpdatePacketData.getSkyYMask();
        BitSet bitSet2 = clientboundLightUpdatePacketData.getEmptySkyYMask();
        Iterator<byte[]> iterator = clientboundLightUpdatePacketData.getSkyUpdates().iterator();
        this.readSectionList(n, n2, levelLightEngine, LightLayer.SKY, bitSet, bitSet2, iterator, clientboundLightUpdatePacketData.getTrustEdges());
        BitSet bitSet3 = clientboundLightUpdatePacketData.getBlockYMask();
        BitSet bitSet4 = clientboundLightUpdatePacketData.getEmptyBlockYMask();
        Iterator<byte[]> iterator2 = clientboundLightUpdatePacketData.getBlockUpdates().iterator();
        this.readSectionList(n, n2, levelLightEngine, LightLayer.BLOCK, bitSet3, bitSet4, iterator2, clientboundLightUpdatePacketData.getTrustEdges());
        this.level.setLightReady(n, n2);
    }

    @Override
    public void handleMerchantOffers(ClientboundMerchantOffersPacket clientboundMerchantOffersPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundMerchantOffersPacket, this, this.minecraft);
        AbstractContainerMenu abstractContainerMenu = this.minecraft.player.containerMenu;
        if (clientboundMerchantOffersPacket.getContainerId() == abstractContainerMenu.containerId && abstractContainerMenu instanceof MerchantMenu) {
            MerchantMenu merchantMenu = (MerchantMenu)abstractContainerMenu;
            merchantMenu.setOffers(new MerchantOffers(clientboundMerchantOffersPacket.getOffers().createTag()));
            merchantMenu.setXp(clientboundMerchantOffersPacket.getVillagerXp());
            merchantMenu.setMerchantLevel(clientboundMerchantOffersPacket.getVillagerLevel());
            merchantMenu.setShowProgressBar(clientboundMerchantOffersPacket.showProgress());
            merchantMenu.setCanRestock(clientboundMerchantOffersPacket.canRestock());
        }
    }

    @Override
    public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket clientboundSetChunkCacheRadiusPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetChunkCacheRadiusPacket, this, this.minecraft);
        this.serverChunkRadius = clientboundSetChunkCacheRadiusPacket.getRadius();
        this.minecraft.options.setServerRenderDistance(this.serverChunkRadius);
        this.level.getChunkSource().updateViewRadius(clientboundSetChunkCacheRadiusPacket.getRadius());
    }

    @Override
    public void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket clientboundSetSimulationDistancePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetSimulationDistancePacket, this, this.minecraft);
        this.serverSimulationDistance = clientboundSetSimulationDistancePacket.simulationDistance();
        this.level.setServerSimulationDistance(this.serverSimulationDistance);
    }

    @Override
    public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket clientboundSetChunkCacheCenterPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetChunkCacheCenterPacket, this, this.minecraft);
        this.level.getChunkSource().updateViewCenter(clientboundSetChunkCacheCenterPacket.getX(), clientboundSetChunkCacheCenterPacket.getZ());
    }

    @Override
    public void handleBlockBreakAck(ClientboundBlockBreakAckPacket clientboundBlockBreakAckPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundBlockBreakAckPacket, this, this.minecraft);
        this.minecraft.gameMode.handleBlockBreakAck(this.level, clientboundBlockBreakAckPacket.pos(), clientboundBlockBreakAckPacket.state(), clientboundBlockBreakAckPacket.action(), clientboundBlockBreakAckPacket.allGood());
    }

    private void readSectionList(int n, int n2, LevelLightEngine levelLightEngine, LightLayer lightLayer, BitSet bitSet, BitSet bitSet2, Iterator<byte[]> iterator, boolean bl) {
        for (int i = 0; i < levelLightEngine.getLightSectionCount(); ++i) {
            int n3 = levelLightEngine.getMinLightSection() + i;
            boolean bl2 = bitSet.get(i);
            boolean bl3 = bitSet2.get(i);
            if (!bl2 && !bl3) continue;
            levelLightEngine.queueSectionData(lightLayer, SectionPos.of(n, n3, n2), bl2 ? new DataLayer((byte[])iterator.next().clone()) : new DataLayer(), bl);
            this.level.setSectionDirtyWithNeighbors(n, n3, n2);
        }
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    public Collection<PlayerInfo> getOnlinePlayers() {
        return this.playerInfoMap.values();
    }

    public Collection<UUID> getOnlinePlayerIds() {
        return this.playerInfoMap.keySet();
    }

    @Nullable
    public PlayerInfo getPlayerInfo(UUID uUID) {
        return this.playerInfoMap.get(uUID);
    }

    @Nullable
    public PlayerInfo getPlayerInfo(String string) {
        for (PlayerInfo playerInfo : this.playerInfoMap.values()) {
            if (!playerInfo.getProfile().getName().equals(string)) continue;
            return playerInfo;
        }
        return null;
    }

    public GameProfile getLocalGameProfile() {
        return this.localGameProfile;
    }

    public ClientAdvancements getAdvancements() {
        return this.advancements;
    }

    public CommandDispatcher<SharedSuggestionProvider> getCommands() {
        return this.commands;
    }

    public ClientLevel getLevel() {
        return this.level;
    }

    public DebugQueryHandler getDebugQueryHandler() {
        return this.debugQueryHandler;
    }

    public UUID getId() {
        return this.id;
    }

    public Set<ResourceKey<Level>> levels() {
        return this.levels;
    }

    public RegistryAccess registryAccess() {
        return this.registryAccess;
    }
}

