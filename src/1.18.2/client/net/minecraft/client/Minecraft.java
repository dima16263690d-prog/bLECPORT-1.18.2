/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.Queues
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.GameProfileRepository
 *  com.mojang.authlib.exceptions.AuthenticationException
 *  com.mojang.authlib.minecraft.MinecraftSessionService
 *  com.mojang.authlib.minecraft.UserApiService
 *  com.mojang.authlib.minecraft.UserApiService$UserFlag
 *  com.mojang.authlib.properties.PropertyMap
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  it.unimi.dsi.fastutil.objects.Object2BooleanFunction
 *  javax.annotation.Nullable
 *  org.apache.commons.io.FileUtils
 *  org.lwjgl.util.tinyfd.TinyFileDialogs
 *  org.slf4j.Logger
 */
package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.GlDebug;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Matrix4f;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.FileUtil;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.client.AmbientOcclusionStatus;
import net.minecraft.client.CameraType;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientTelemetryManager;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Game;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.PeriodicNotificationManager;
import net.minecraft.client.ResourceLoadStateTracker;
import net.minecraft.client.Screenshot;
import net.minecraft.client.Timer;
import net.minecraft.client.User;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.OutOfMemoryScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.profiling.ClientMetricsSamplersProvider;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.VirtualScreen;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.LegacyPackResourcesAdapter;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.PackResourcesAdapterV4;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.client.searchtree.ReloadableIdSearchTree;
import net.minecraft.client.searchtree.ReloadableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FileZipper;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.ModCheck;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.io.FileUtils;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

public class Minecraft
extends ReentrantBlockableEventLoop<Runnable>
implements WindowEventHandler {
    private static Minecraft instance;
    private static final Logger LOGGER;
    public static final boolean ON_OSX;
    private static final int MAX_TICKS_PER_UPDATE = 10;
    public static final ResourceLocation DEFAULT_FONT;
    public static final ResourceLocation UNIFORM_FONT;
    public static final ResourceLocation ALT_FONT;
    private static final ResourceLocation REGIONAL_COMPLIANCIES;
    private static final CompletableFuture<Unit> RESOURCE_RELOAD_INITIAL_TASK;
    private static final Component SOCIAL_INTERACTIONS_NOT_AVAILABLE;
    public static final String UPDATE_DRIVERS_ADVICE = "Please make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).";
    private final File resourcePackDirectory;
    private final PropertyMap profileProperties;
    private final TextureManager textureManager;
    private final DataFixer fixerUpper;
    private final VirtualScreen virtualScreen;
    private final Window window;
    private final Timer timer = new Timer(20.0f, 0L);
    private final RenderBuffers renderBuffers;
    public final LevelRenderer levelRenderer;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemRenderer itemRenderer;
    private final ItemInHandRenderer itemInHandRenderer;
    public final ParticleEngine particleEngine;
    private final SearchRegistry searchRegistry = new SearchRegistry();
    private final User user;
    public final Font font;
    public final GameRenderer gameRenderer;
    public final DebugRenderer debugRenderer;
    private final AtomicReference<StoringChunkProgressListener> progressListener = new AtomicReference();
    public final Gui gui;
    public final Options options;
    private final HotbarManager hotbarManager;
    public final MouseHandler mouseHandler;
    public final KeyboardHandler keyboardHandler;
    public final File gameDirectory;
    private final String launchedVersion;
    private final String versionType;
    private final Proxy proxy;
    private final LevelStorageSource levelSource;
    public final FrameTimer frameTimer = new FrameTimer();
    private final boolean is64bit;
    private final boolean demo;
    private final boolean allowsMultiplayer;
    private final boolean allowsChat;
    private final ReloadableResourceManager resourceManager;
    private final ClientPackSource clientPackSource;
    private final PackRepository resourcePackRepository;
    private final LanguageManager languageManager;
    private final BlockColors blockColors;
    private final ItemColors itemColors;
    private final RenderTarget mainRenderTarget;
    private final SoundManager soundManager;
    private final MusicManager musicManager;
    private final FontManager fontManager;
    private final SplashManager splashManager;
    private final GpuWarnlistManager gpuWarnlistManager;
    private final PeriodicNotificationManager regionalCompliancies = new PeriodicNotificationManager(REGIONAL_COMPLIANCIES, (Object2BooleanFunction<String>)((Object2BooleanFunction)Minecraft::countryEqualsISO3));
    private final MinecraftSessionService minecraftSessionService;
    private final UserApiService userApiService;
    private final SkinManager skinManager;
    private final ModelManager modelManager;
    private final BlockRenderDispatcher blockRenderer;
    private final PaintingTextureManager paintingTextures;
    private final MobEffectTextureManager mobEffectTextures;
    private final ToastComponent toast;
    private final Game game = new Game(this);
    private final Tutorial tutorial;
    private final PlayerSocialManager playerSocialManager;
    private final EntityModelSet entityModels;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final UUID deviceSessionId = UUID.randomUUID();
    @Nullable
    public MultiPlayerGameMode gameMode;
    @Nullable
    public ClientLevel level;
    @Nullable
    public LocalPlayer player;
    @Nullable
    private IntegratedServer singleplayerServer;
    @Nullable
    private ServerData currentServer;
    @Nullable
    private Connection pendingConnection;
    private boolean isLocalServer;
    @Nullable
    public Entity cameraEntity;
    @Nullable
    public Entity crosshairPickEntity;
    @Nullable
    public HitResult hitResult;
    private int rightClickDelay;
    protected int missTime;
    private volatile boolean pause;
    private float pausePartialTick;
    private long lastNanoTime = Util.getNanos();
    private long lastTime;
    private int frames;
    public boolean noRender;
    @Nullable
    public Screen screen;
    @Nullable
    private Overlay overlay;
    private boolean connectedToRealms;
    private Thread gameThread;
    private volatile boolean running = true;
    @Nullable
    private Supplier<CrashReport> delayedCrash;
    private static int fps;
    public String fpsString = "";
    public boolean wireframe;
    public boolean chunkPath;
    public boolean chunkVisibility;
    public boolean smartCull = true;
    private boolean windowActive;
    private final Queue<Runnable> progressTasks = Queues.newConcurrentLinkedQueue();
    @Nullable
    private CompletableFuture<Void> pendingReload;
    @Nullable
    private TutorialToast socialInteractionsToast;
    private ProfilerFiller profiler = InactiveProfiler.INSTANCE;
    private int fpsPieRenderTicks;
    private final ContinuousProfiler fpsPieProfiler = new ContinuousProfiler(Util.timeSource, () -> this.fpsPieRenderTicks);
    @Nullable
    private ProfileResults fpsPieResults;
    private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    private final ResourceLoadStateTracker reloadStateTracker = new ResourceLoadStateTracker();
    private String debugPath = "root";

    public Minecraft(GameConfig gameConfig) {
        super("Client");
        List<PackResources> list;
        Object object;
        int n;
        String string;
        instance = this;
        this.gameDirectory = gameConfig.location.gameDirectory;
        File file = gameConfig.location.assetDirectory;
        this.resourcePackDirectory = gameConfig.location.resourcePackDirectory;
        this.launchedVersion = gameConfig.game.launchVersion;
        this.versionType = gameConfig.game.versionType;
        this.profileProperties = gameConfig.user.profileProperties;
        this.clientPackSource = new ClientPackSource(new File(this.gameDirectory, "server-resource-packs"), gameConfig.location.getAssetIndex());
        this.resourcePackRepository = new PackRepository(Minecraft::createClientPackAdapter, this.clientPackSource, new FolderRepositorySource(this.resourcePackDirectory, PackSource.DEFAULT));
        this.proxy = gameConfig.user.proxy;
        YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(this.proxy);
        this.minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
        this.userApiService = this.createUserApiService(yggdrasilAuthenticationService, gameConfig);
        this.user = gameConfig.user.user;
        LOGGER.info("Setting user: {}", (Object)this.user.getName());
        LOGGER.debug("(Session ID is {})", (Object)this.user.getSessionId());
        this.demo = gameConfig.game.demo;
        this.allowsMultiplayer = !gameConfig.game.disableMultiplayer;
        this.allowsChat = !gameConfig.game.disableChat;
        this.is64bit = Minecraft.checkIs64Bit();
        this.singleplayerServer = null;
        if (this.allowsMultiplayer() && gameConfig.server.hostname != null) {
            string = gameConfig.server.hostname;
            n = gameConfig.server.port;
        } else {
            string = null;
            n = 0;
        }
        KeybindComponent.setKeyResolver(KeyMapping::createNameSupplier);
        this.fixerUpper = DataFixers.getDataFixer();
        this.toast = new ToastComponent(this);
        this.gameThread = Thread.currentThread();
        this.options = new Options(this, this.gameDirectory);
        this.tutorial = new Tutorial(this, this.options);
        this.hotbarManager = new HotbarManager(this.gameDirectory, this.fixerUpper);
        LOGGER.info("Backend library: {}", (Object)RenderSystem.getBackendDescription());
        DisplayData displayData = this.options.overrideHeight > 0 && this.options.overrideWidth > 0 ? new DisplayData(this.options.overrideWidth, this.options.overrideHeight, gameConfig.display.fullscreenWidth, gameConfig.display.fullscreenHeight, gameConfig.display.isFullscreen) : gameConfig.display;
        Util.timeSource = RenderSystem.initBackendSystem();
        this.virtualScreen = new VirtualScreen(this);
        this.window = this.virtualScreen.newWindow(displayData, this.options.fullscreenVideoModeString, this.createTitle());
        this.setWindowActive(true);
        if (!ON_OSX) {
            try {
                object = this.getClientPackSource().getVanillaPack().getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_16x16.png"));
                list = this.getClientPackSource().getVanillaPack().getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_32x32.png"));
                this.window.setIcon((InputStream)object, (InputStream)((Object)list));
            }
            catch (IOException iOException) {
                LOGGER.error("Couldn't set icon", (Throwable)iOException);
            }
        }
        this.window.setFramerateLimit(this.options.framerateLimit);
        this.mouseHandler = new MouseHandler(this);
        this.mouseHandler.setup(this.window.getWindow());
        this.keyboardHandler = new KeyboardHandler(this);
        this.keyboardHandler.setup(this.window.getWindow());
        RenderSystem.initRenderer(this.options.glDebugVerbosity, false);
        this.mainRenderTarget = new MainTarget(this.window.getWidth(), this.window.getHeight());
        this.mainRenderTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        this.mainRenderTarget.clear(ON_OSX);
        this.resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);
        this.resourcePackRepository.reload();
        this.options.loadSelectedResourcePacks(this.resourcePackRepository);
        this.languageManager = new LanguageManager(this.options.languageCode);
        this.resourceManager.registerReloadListener(this.languageManager);
        this.textureManager = new TextureManager(this.resourceManager);
        this.resourceManager.registerReloadListener(this.textureManager);
        this.skinManager = new SkinManager(this.textureManager, new File(file, "skins"), this.minecraftSessionService);
        this.levelSource = new LevelStorageSource(this.gameDirectory.toPath().resolve("saves"), this.gameDirectory.toPath().resolve("backups"), this.fixerUpper);
        this.soundManager = new SoundManager(this.resourceManager, this.options);
        this.resourceManager.registerReloadListener(this.soundManager);
        this.splashManager = new SplashManager(this.user);
        this.resourceManager.registerReloadListener(this.splashManager);
        this.musicManager = new MusicManager(this);
        this.fontManager = new FontManager(this.textureManager);
        this.font = this.fontManager.createFont();
        this.resourceManager.registerReloadListener(this.fontManager.getReloadListener());
        this.selectMainFont(this.isEnforceUnicode());
        this.resourceManager.registerReloadListener(new GrassColorReloadListener());
        this.resourceManager.registerReloadListener(new FoliageColorReloadListener());
        this.window.setErrorSection("Startup");
        RenderSystem.setupDefaultState(0, 0, this.window.getWidth(), this.window.getHeight());
        this.window.setErrorSection("Post startup");
        this.blockColors = BlockColors.createDefault();
        this.itemColors = ItemColors.createDefault(this.blockColors);
        this.modelManager = new ModelManager(this.textureManager, this.blockColors, this.options.mipmapLevels);
        this.resourceManager.registerReloadListener(this.modelManager);
        this.entityModels = new EntityModelSet();
        this.resourceManager.registerReloadListener(this.entityModels);
        this.blockEntityRenderDispatcher = new BlockEntityRenderDispatcher(this.font, this.entityModels, this::getBlockRenderer);
        this.resourceManager.registerReloadListener(this.blockEntityRenderDispatcher);
        object = new BlockEntityWithoutLevelRenderer(this.blockEntityRenderDispatcher, this.entityModels);
        this.resourceManager.registerReloadListener((PreparableReloadListener)object);
        this.itemRenderer = new ItemRenderer(this.textureManager, this.modelManager, this.itemColors, (BlockEntityWithoutLevelRenderer)object);
        this.entityRenderDispatcher = new EntityRenderDispatcher(this.textureManager, this.itemRenderer, this.font, this.options, this.entityModels);
        this.resourceManager.registerReloadListener(this.entityRenderDispatcher);
        this.itemInHandRenderer = new ItemInHandRenderer(this);
        this.resourceManager.registerReloadListener(this.itemRenderer);
        this.renderBuffers = new RenderBuffers();
        this.gameRenderer = new GameRenderer(this, this.resourceManager, this.renderBuffers);
        this.resourceManager.registerReloadListener(this.gameRenderer);
        this.playerSocialManager = new PlayerSocialManager(this, this.userApiService);
        this.blockRenderer = new BlockRenderDispatcher(this.modelManager.getBlockModelShaper(), (BlockEntityWithoutLevelRenderer)object, this.blockColors);
        this.resourceManager.registerReloadListener(this.blockRenderer);
        this.levelRenderer = new LevelRenderer(this, this.renderBuffers);
        this.resourceManager.registerReloadListener(this.levelRenderer);
        this.createSearchTrees();
        this.resourceManager.registerReloadListener(this.searchRegistry);
        this.particleEngine = new ParticleEngine(this.level, this.textureManager);
        this.resourceManager.registerReloadListener(this.particleEngine);
        this.paintingTextures = new PaintingTextureManager(this.textureManager);
        this.resourceManager.registerReloadListener(this.paintingTextures);
        this.mobEffectTextures = new MobEffectTextureManager(this.textureManager);
        this.resourceManager.registerReloadListener(this.mobEffectTextures);
        this.gpuWarnlistManager = new GpuWarnlistManager();
        this.resourceManager.registerReloadListener(this.gpuWarnlistManager);
        this.resourceManager.registerReloadListener(this.regionalCompliancies);
        this.gui = new Gui(this);
        this.debugRenderer = new DebugRenderer(this);
        RenderSystem.setErrorCallback(this::onFullscreenError);
        if (this.mainRenderTarget.width != this.window.getWidth() || this.mainRenderTarget.height != this.window.getHeight()) {
            list = new StringBuilder("Recovering from unsupported resolution (" + this.window.getWidth() + "x" + this.window.getHeight() + ").\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).");
            if (GlDebug.isDebugEnabled()) {
                ((StringBuilder)((Object)list)).append("\n\nReported GL debug messages:\n").append(String.join((CharSequence)"\n", GlDebug.getLastOpenGlDebugMessages()));
            }
            this.window.setWindowed(this.mainRenderTarget.width, this.mainRenderTarget.height);
            TinyFileDialogs.tinyfd_messageBox((CharSequence)"Minecraft", (CharSequence)((StringBuilder)((Object)list)).toString(), (CharSequence)"ok", (CharSequence)"error", (boolean)false);
        } else if (this.options.fullscreen && !this.window.isFullscreen()) {
            this.window.toggleFullScreen();
            this.options.fullscreen = this.window.isFullscreen();
        }
        this.window.updateVsync(this.options.enableVsync);
        this.window.updateRawMouseInput(this.options.rawMouseInput);
        this.window.setDefaultErrorCallback();
        this.resizeDisplay();
        this.gameRenderer.preloadUiShader(this.getClientPackSource().getVanillaPack());
        LoadingOverlay.registerTextures(this);
        list = this.resourcePackRepository.openAllSelected();
        this.reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.INITIAL, list);
        this.setOverlay(new LoadingOverlay(this, this.resourceManager.createReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, list), optional -> Util.ifElse(optional, this::rollbackResourcePacks, () -> {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                this.selfTest();
            }
            this.reloadStateTracker.finishReload();
        }), false));
        if (string != null) {
            ConnectScreen.startConnecting(new TitleScreen(), this, new ServerAddress(string, n), null);
        } else {
            this.setScreen(new TitleScreen(true));
        }
    }

    private static boolean countryEqualsISO3(Object object) {
        try {
            return Locale.getDefault().getISO3Country().equals(object);
        }
        catch (MissingResourceException missingResourceException) {
            return false;
        }
    }

    public void updateTitle() {
        this.window.setTitle(this.createTitle());
    }

    private String createTitle() {
        StringBuilder stringBuilder = new StringBuilder("Minecraft");
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            stringBuilder.append("*");
        }
        stringBuilder.append(" ");
        stringBuilder.append(SharedConstants.getCurrentVersion().getName());
        ClientPacketListener clientPacketListener = this.getConnection();
        if (clientPacketListener != null && clientPacketListener.getConnection().isConnected()) {
            stringBuilder.append(" - ");
            if (this.singleplayerServer != null && !this.singleplayerServer.isPublished()) {
                stringBuilder.append(I18n.get("title.singleplayer", new Object[0]));
            } else if (this.isConnectedToRealms()) {
                stringBuilder.append(I18n.get("title.multiplayer.realms", new Object[0]));
            } else if (this.singleplayerServer != null || this.currentServer != null && this.currentServer.isLan()) {
                stringBuilder.append(I18n.get("title.multiplayer.lan", new Object[0]));
            } else {
                stringBuilder.append(I18n.get("title.multiplayer.other", new Object[0]));
            }
        }
        return stringBuilder.toString();
    }

    private UserApiService createUserApiService(YggdrasilAuthenticationService yggdrasilAuthenticationService, GameConfig gameConfig) {
        try {
            return yggdrasilAuthenticationService.createUserApiService(gameConfig.user.user.getAccessToken());
        }
        catch (AuthenticationException authenticationException) {
            LOGGER.error("Failed to verify authentication", (Throwable)authenticationException);
            return UserApiService.OFFLINE;
        }
    }

    public static ModCheck checkModStatus() {
        return ModCheck.identify("vanilla", ClientBrandRetriever::getClientModName, "Client", Minecraft.class);
    }

    private void rollbackResourcePacks(Throwable throwable) {
        if (this.resourcePackRepository.getSelectedIds().size() > 1) {
            this.clearResourcePacksOnError(throwable, null);
        } else {
            Util.throwAsRuntime(throwable);
        }
    }

    public void clearResourcePacksOnError(Throwable throwable, @Nullable Component component) {
        LOGGER.info("Caught error loading resourcepacks, removing all selected resourcepacks", throwable);
        this.reloadStateTracker.startRecovery(throwable);
        this.resourcePackRepository.setSelected(Collections.emptyList());
        this.options.resourcePacks.clear();
        this.options.incompatibleResourcePacks.clear();
        this.options.save();
        this.reloadResourcePacks(true).thenRun(() -> {
            ToastComponent toastComponent = this.getToasts();
            SystemToast.addOrUpdate(toastComponent, SystemToast.SystemToastIds.PACK_LOAD_FAILURE, new TranslatableComponent("resourcePack.load_fail"), component);
        });
    }

    public void run() {
        this.gameThread = Thread.currentThread();
        if (Runtime.getRuntime().availableProcessors() > 4) {
            this.gameThread.setPriority(10);
        }
        try {
            boolean bl = false;
            while (this.running) {
                if (this.delayedCrash != null) {
                    Minecraft.crash(this.delayedCrash.get());
                    return;
                }
                try {
                    SingleTickProfiler singleTickProfiler = SingleTickProfiler.createTickProfiler("Renderer");
                    boolean bl2 = this.shouldRenderFpsPie();
                    this.profiler = this.constructProfiler(bl2, singleTickProfiler);
                    this.profiler.startTick();
                    this.metricsRecorder.startTick();
                    this.runTick(!bl);
                    this.metricsRecorder.endTick();
                    this.profiler.endTick();
                    this.finishProfilers(bl2, singleTickProfiler);
                }
                catch (OutOfMemoryError outOfMemoryError) {
                    if (bl) {
                        throw outOfMemoryError;
                    }
                    this.emergencySave();
                    this.setScreen(new OutOfMemoryScreen());
                    System.gc();
                    LOGGER.error(LogUtils.FATAL_MARKER, "Out of memory", (Throwable)outOfMemoryError);
                    bl = true;
                }
            }
        }
        catch (ReportedException reportedException) {
            this.fillReport(reportedException.getReport());
            this.emergencySave();
            LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", (Throwable)reportedException);
            Minecraft.crash(reportedException.getReport());
        }
        catch (Throwable throwable) {
            CrashReport crashReport = this.fillReport(new CrashReport("Unexpected error", throwable));
            LOGGER.error(LogUtils.FATAL_MARKER, "Unreported exception thrown!", throwable);
            this.emergencySave();
            Minecraft.crash(crashReport);
        }
    }

    void selectMainFont(boolean bl) {
        this.fontManager.setRenames((Map<ResourceLocation, ResourceLocation>)(bl ? ImmutableMap.of((Object)DEFAULT_FONT, (Object)UNIFORM_FONT) : ImmutableMap.of()));
    }

    private void createSearchTrees() {
        ReloadableSearchTree<ItemStack> reloadableSearchTree = new ReloadableSearchTree<ItemStack>(itemStack -> itemStack.getTooltipLines(null, TooltipFlag.Default.NORMAL).stream().map(component -> ChatFormatting.stripFormatting(component.getString()).trim()).filter(string -> !string.isEmpty()), itemStack -> Stream.of(Registry.ITEM.getKey(itemStack.getItem())));
        ReloadableIdSearchTree<ItemStack> reloadableIdSearchTree = new ReloadableIdSearchTree<ItemStack>(itemStack -> itemStack.getTags().map(TagKey::location));
        NonNullList<ItemStack> nonNullList = NonNullList.create();
        for (Item item : Registry.ITEM) {
            item.fillItemCategory(CreativeModeTab.TAB_SEARCH, nonNullList);
        }
        nonNullList.forEach(itemStack -> {
            reloadableSearchTree.add((ItemStack)itemStack);
            reloadableIdSearchTree.add((ItemStack)itemStack);
        });
        ReloadableSearchTree<RecipeCollection> reloadableSearchTree2 = new ReloadableSearchTree<RecipeCollection>(recipeCollection -> recipeCollection.getRecipes().stream().flatMap(recipe -> recipe.getResultItem().getTooltipLines(null, TooltipFlag.Default.NORMAL).stream()).map(component -> ChatFormatting.stripFormatting(component.getString()).trim()).filter(string -> !string.isEmpty()), recipeCollection -> recipeCollection.getRecipes().stream().map(recipe -> Registry.ITEM.getKey(recipe.getResultItem().getItem())));
        this.searchRegistry.register(SearchRegistry.CREATIVE_NAMES, reloadableSearchTree);
        this.searchRegistry.register(SearchRegistry.CREATIVE_TAGS, reloadableIdSearchTree);
        this.searchRegistry.register(SearchRegistry.RECIPE_COLLECTIONS, reloadableSearchTree2);
    }

    private void onFullscreenError(int n, long l) {
        this.options.enableVsync = false;
        this.options.save();
    }

    private static boolean checkIs64Bit() {
        String[] stringArray;
        for (String string : stringArray = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"}) {
            String string2 = System.getProperty(string);
            if (string2 == null || !string2.contains("64")) continue;
            return true;
        }
        return false;
    }

    public RenderTarget getMainRenderTarget() {
        return this.mainRenderTarget;
    }

    public String getLaunchedVersion() {
        return this.launchedVersion;
    }

    public String getVersionType() {
        return this.versionType;
    }

    public void delayCrash(Supplier<CrashReport> supplier) {
        this.delayedCrash = supplier;
    }

    public static void crash(CrashReport crashReport) {
        File file = new File(Minecraft.getInstance().gameDirectory, "crash-reports");
        File file2 = new File(file, "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-client.txt");
        Bootstrap.realStdoutPrintln(crashReport.getFriendlyReport());
        if (crashReport.getSaveFile() != null) {
            Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReport.getSaveFile());
            System.exit(-1);
        } else if (crashReport.saveToFile(file2)) {
            Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
            System.exit(-1);
        } else {
            Bootstrap.realStdoutPrintln("#@?@# Game crashed! Crash report could not be saved. #@?@#");
            System.exit(-2);
        }
    }

    public boolean isEnforceUnicode() {
        return this.options.forceUnicodeFont;
    }

    public CompletableFuture<Void> reloadResourcePacks() {
        return this.reloadResourcePacks(false);
    }

    private CompletableFuture<Void> reloadResourcePacks(boolean bl) {
        if (this.pendingReload != null) {
            return this.pendingReload;
        }
        CompletableFuture<Void> completableFuture = new CompletableFuture<Void>();
        if (!bl && this.overlay instanceof LoadingOverlay) {
            this.pendingReload = completableFuture;
            return completableFuture;
        }
        this.resourcePackRepository.reload();
        List<PackResources> list = this.resourcePackRepository.openAllSelected();
        if (!bl) {
            this.reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.MANUAL, list);
        }
        this.setOverlay(new LoadingOverlay(this, this.resourceManager.createReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, list), optional -> Util.ifElse(optional, this::rollbackResourcePacks, () -> {
            this.levelRenderer.allChanged();
            this.reloadStateTracker.finishReload();
            completableFuture.complete(null);
        }), true));
        return completableFuture;
    }

    private void selfTest() {
        boolean bl = false;
        BlockModelShaper blockModelShaper = this.getBlockRenderer().getBlockModelShaper();
        BakedModel bakedModel = blockModelShaper.getModelManager().getMissingModel();
        for (Block block : Registry.BLOCK) {
            for (Object object : block.getStateDefinition().getPossibleStates()) {
                Iterator iterator;
                if (((BlockBehaviour.BlockStateBase)object).getRenderShape() != RenderShape.MODEL || (iterator = blockModelShaper.getBlockModel((BlockState)object)) != bakedModel) continue;
                LOGGER.debug("Missing model for: {}", object);
                bl = true;
            }
        }
        TextureAtlasSprite textureAtlasSprite = bakedModel.getParticleIcon();
        for (Object object : Registry.BLOCK) {
            for (Iterator iterator : ((Block)object).getStateDefinition().getPossibleStates()) {
                Object object2 = blockModelShaper.getParticleIcon((BlockState)((Object)iterator));
                if (((BlockBehaviour.BlockStateBase)((Object)iterator)).isAir() || object2 != textureAtlasSprite) continue;
                LOGGER.debug("Missing particle icon for: {}", iterator);
                bl = true;
            }
        }
        NonNullList<ItemStack> nonNullList = NonNullList.create();
        for (Object object : Registry.ITEM) {
            nonNullList.clear();
            ((Item)object).fillItemCategory(CreativeModeTab.TAB_SEARCH, nonNullList);
            for (Object object2 : nonNullList) {
                String string = ((ItemStack)object2).getDescriptionId();
                String string2 = new TranslatableComponent(string).getString();
                if (!string2.toLowerCase(Locale.ROOT).equals(((Item)object).getDescriptionId())) continue;
                LOGGER.debug("Missing translation for: {} {} {}", new Object[]{object2, string, ((ItemStack)object2).getItem()});
            }
        }
        bl |= MenuScreens.selfTest();
        if (bl |= EntityRenderers.validateRegistrations()) {
            throw new IllegalStateException("Your game data is foobar, fix the errors above!");
        }
    }

    public LevelStorageSource getLevelSource() {
        return this.levelSource;
    }

    private void openChatScreen(String string) {
        ChatStatus chatStatus = this.getChatStatus();
        if (!chatStatus.isChatAllowed(this.isLocalServer())) {
            this.gui.setOverlayMessage(chatStatus.getMessage(), false);
        } else {
            this.setScreen(new ChatScreen(string));
        }
    }

    public void setScreen(@Nullable Screen screen) {
        if (SharedConstants.IS_RUNNING_IN_IDE && Thread.currentThread() != this.gameThread) {
            LOGGER.error("setScreen called from non-game thread");
        }
        if (this.screen != null) {
            this.screen.removed();
        }
        if (screen == null && this.level == null) {
            screen = new TitleScreen();
        } else if (screen == null && this.player.isDeadOrDying()) {
            if (this.player.shouldShowDeathScreen()) {
                screen = new DeathScreen(null, this.level.getLevelData().isHardcore());
            } else {
                this.player.respawn();
            }
        }
        this.screen = screen;
        BufferUploader.reset();
        if (screen != null) {
            this.mouseHandler.releaseMouse();
            KeyMapping.releaseAll();
            screen.init(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
            this.noRender = false;
        } else {
            this.soundManager.resume();
            this.mouseHandler.grabMouse();
        }
        this.updateTitle();
    }

    public void setOverlay(@Nullable Overlay overlay) {
        this.overlay = overlay;
    }

    public void destroy() {
        try {
            LOGGER.info("Stopping!");
            try {
                NarratorChatListener.INSTANCE.destroy();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            try {
                if (this.level != null) {
                    this.level.disconnect();
                }
                this.clearLevel();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (this.screen != null) {
                this.screen.removed();
            }
            this.close();
        }
        finally {
            Util.timeSource = System::nanoTime;
            if (this.delayedCrash == null) {
                System.exit(0);
            }
        }
    }

    @Override
    public void close() {
        try {
            this.regionalCompliancies.close();
            this.modelManager.close();
            this.fontManager.close();
            this.gameRenderer.close();
            this.levelRenderer.close();
            this.soundManager.destroy();
            this.resourcePackRepository.close();
            this.particleEngine.close();
            this.mobEffectTextures.close();
            this.paintingTextures.close();
            this.textureManager.close();
            this.resourceManager.close();
            Util.shutdownExecutors();
        }
        catch (Throwable throwable) {
            LOGGER.error("Shutdown failure!", throwable);
            throw throwable;
        }
        finally {
            this.virtualScreen.close();
            this.window.close();
        }
    }

    private void runTick(boolean bl) {
        boolean bl2;
        int n;
        Object object;
        this.window.setErrorSection("Pre render");
        long l = Util.getNanos();
        if (this.window.shouldClose()) {
            this.stop();
        }
        if (this.pendingReload != null && !(this.overlay instanceof LoadingOverlay)) {
            object = this.pendingReload;
            this.pendingReload = null;
            this.reloadResourcePacks().thenRun(() -> Minecraft.lambda$runTick$18((CompletableFuture)object));
        }
        while ((object = this.progressTasks.poll()) != null) {
            object.run();
        }
        if (bl) {
            int n2 = this.timer.advanceTime(Util.getMillis());
            this.profiler.push("scheduledExecutables");
            this.runAllTasks();
            this.profiler.pop();
            this.profiler.push("tick");
            for (n = 0; n < Math.min(10, n2); ++n) {
                this.profiler.incrementCounter("clientTick");
                this.tick();
            }
            this.profiler.pop();
        }
        this.mouseHandler.turnPlayer();
        this.window.setErrorSection("Render");
        this.profiler.push("sound");
        this.soundManager.updateSource(this.gameRenderer.getMainCamera());
        this.profiler.pop();
        this.profiler.push("render");
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.clear(16640, ON_OSX);
        this.mainRenderTarget.bindWrite(true);
        FogRenderer.setupNoFog();
        this.profiler.push("display");
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        this.profiler.pop();
        if (!this.noRender) {
            this.profiler.popPush("gameRenderer");
            this.gameRenderer.render(this.pause ? this.pausePartialTick : this.timer.partialTick, l, bl);
            this.profiler.popPush("toasts");
            this.toast.render(new PoseStack());
            this.profiler.pop();
        }
        if (this.fpsPieResults != null) {
            this.profiler.push("fpsPie");
            this.renderFpsMeter(new PoseStack(), this.fpsPieResults);
            this.profiler.pop();
        }
        this.profiler.push("blit");
        this.mainRenderTarget.unbindWrite();
        poseStack.popPose();
        poseStack.pushPose();
        RenderSystem.applyModelViewMatrix();
        this.mainRenderTarget.blitToScreen(this.window.getWidth(), this.window.getHeight());
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
        this.profiler.popPush("updateDisplay");
        this.window.updateDisplay();
        n = this.getFramerateLimit();
        if ((double)n < Option.FRAMERATE_LIMIT.getMaxValue()) {
            RenderSystem.limitDisplayFPS(n);
        }
        this.profiler.popPush("yield");
        Thread.yield();
        this.profiler.pop();
        this.window.setErrorSection("Post render");
        ++this.frames;
        boolean bl3 = bl2 = this.hasSingleplayerServer() && (this.screen != null && this.screen.isPauseScreen() || this.overlay != null && this.overlay.isPauseScreen()) && !this.singleplayerServer.isPublished();
        if (this.pause != bl2) {
            if (this.pause) {
                this.pausePartialTick = this.timer.partialTick;
            } else {
                this.timer.partialTick = this.pausePartialTick;
            }
            this.pause = bl2;
        }
        long l2 = Util.getNanos();
        this.frameTimer.logFrameDuration(l2 - this.lastNanoTime);
        this.lastNanoTime = l2;
        this.profiler.push("fpsUpdate");
        while (Util.getMillis() >= this.lastTime + 1000L) {
            fps = this.frames;
            this.fpsString = String.format("%d fps T: %s%s%s%s B: %d", new Object[]{fps, (double)this.options.framerateLimit == Option.FRAMERATE_LIMIT.getMaxValue() ? "inf" : Integer.valueOf(this.options.framerateLimit), this.options.enableVsync ? " vsync" : "", this.options.graphicsMode, this.options.renderClouds == CloudStatus.OFF ? "" : (this.options.renderClouds == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds"), this.options.biomeBlendRadius});
            this.lastTime += 1000L;
            this.frames = 0;
        }
        this.profiler.pop();
    }

    private boolean shouldRenderFpsPie() {
        return this.options.renderDebug && this.options.renderDebugCharts && !this.options.hideGui;
    }

    private ProfilerFiller constructProfiler(boolean bl, @Nullable SingleTickProfiler singleTickProfiler) {
        ProfilerFiller profilerFiller;
        if (!bl) {
            this.fpsPieProfiler.disable();
            if (!this.metricsRecorder.isRecording() && singleTickProfiler == null) {
                return InactiveProfiler.INSTANCE;
            }
        }
        if (bl) {
            if (!this.fpsPieProfiler.isEnabled()) {
                this.fpsPieRenderTicks = 0;
                this.fpsPieProfiler.enable();
            }
            ++this.fpsPieRenderTicks;
            profilerFiller = this.fpsPieProfiler.getFiller();
        } else {
            profilerFiller = InactiveProfiler.INSTANCE;
        }
        if (this.metricsRecorder.isRecording()) {
            profilerFiller = ProfilerFiller.tee(profilerFiller, this.metricsRecorder.getProfiler());
        }
        return SingleTickProfiler.decorateFiller(profilerFiller, singleTickProfiler);
    }

    private void finishProfilers(boolean bl, @Nullable SingleTickProfiler singleTickProfiler) {
        if (singleTickProfiler != null) {
            singleTickProfiler.endTick();
        }
        this.fpsPieResults = bl ? this.fpsPieProfiler.getResults() : null;
        this.profiler = this.fpsPieProfiler.getFiller();
    }

    @Override
    public void resizeDisplay() {
        int n = this.window.calculateScale(this.options.guiScale, this.isEnforceUnicode());
        this.window.setGuiScale(n);
        if (this.screen != null) {
            this.screen.resize(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
        }
        RenderTarget renderTarget = this.getMainRenderTarget();
        renderTarget.resize(this.window.getWidth(), this.window.getHeight(), ON_OSX);
        this.gameRenderer.resize(this.window.getWidth(), this.window.getHeight());
        this.mouseHandler.setIgnoreFirstMove();
    }

    @Override
    public void cursorEntered() {
        this.mouseHandler.cursorEntered();
    }

    private int getFramerateLimit() {
        if (this.level == null && (this.screen != null || this.overlay != null)) {
            return 60;
        }
        return this.window.getFramerateLimit();
    }

    public void emergencySave() {
        try {
            MemoryReserve.release();
            this.levelRenderer.clear();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            System.gc();
            if (this.isLocalServer && this.singleplayerServer != null) {
                this.singleplayerServer.halt(true);
            }
            this.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        System.gc();
    }

    public boolean debugClientMetricsStart(Consumer<TranslatableComponent> consumer) {
        Consumer<Path> consumer2;
        if (this.metricsRecorder.isRecording()) {
            this.debugClientMetricsStop();
            return false;
        }
        Consumer<ProfileResults> consumer3 = profileResults -> {
            int n = profileResults.getTickDuration();
            double d = (double)profileResults.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
            this.execute(() -> consumer.accept(new TranslatableComponent("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d), n, String.format(Locale.ROOT, "%.2f", (double)n / d))));
        };
        Consumer<Path> consumer4 = path -> {
            MutableComponent mutableComponent = new TextComponent(path.toString()).withStyle(ChatFormatting.UNDERLINE).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.toFile().getParent())));
            this.execute(() -> consumer.accept(new TranslatableComponent("debug.profiling.stop", mutableComponent)));
        };
        SystemReport systemReport = Minecraft.fillSystemReport(new SystemReport(), this, this.languageManager, this.launchedVersion, this.options);
        Consumer<List> consumer5 = list -> {
            Path path = this.archiveProfilingReport(systemReport, (List<Path>)list);
            consumer4.accept(path);
        };
        if (this.singleplayerServer == null) {
            consumer2 = path -> consumer5.accept((List)ImmutableList.of((Object)path));
        } else {
            this.singleplayerServer.fillSystemReport(systemReport);
            CompletableFuture completableFuture = new CompletableFuture();
            CompletableFuture completableFuture2 = new CompletableFuture();
            CompletableFuture.allOf(completableFuture, completableFuture2).thenRunAsync(() -> consumer5.accept((List)ImmutableList.of((Object)((Path)completableFuture.join()), (Object)((Path)completableFuture2.join()))), Util.ioPool());
            this.singleplayerServer.startRecordingMetrics(profileResults -> {}, completableFuture2::complete);
            consumer2 = completableFuture::complete;
        }
        this.metricsRecorder = ActiveMetricsRecorder.createStarted(new ClientMetricsSamplersProvider(Util.timeSource, this.levelRenderer), Util.timeSource, Util.ioPool(), new MetricsPersister("client"), profileResults -> {
            this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
            consumer3.accept((ProfileResults)profileResults);
        }, consumer2);
        return true;
    }

    private void debugClientMetricsStop() {
        this.metricsRecorder.end();
        if (this.singleplayerServer != null) {
            this.singleplayerServer.finishRecordingMetrics();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Path archiveProfilingReport(SystemReport systemReport, List<Path> list) {
        Path path;
        Iterator<Path> iterator;
        String string = this.isLocalServer() ? this.getSingleplayerServer().getWorldData().getLevelName() : this.getCurrentServer().name;
        try {
            iterator = String.format("%s-%s-%s", new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()), string, SharedConstants.getCurrentVersion().getId());
            String object = FileUtil.findAvailableName(MetricsPersister.PROFILING_RESULTS_DIR, (String)((Object)iterator), ".zip");
            path = MetricsPersister.PROFILING_RESULTS_DIR.resolve(object);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
        try {
            iterator = new FileZipper(path);
            try {
                ((FileZipper)((Object)iterator)).add(Paths.get("system.txt", new String[0]), systemReport.toLineSeparatedString());
                ((FileZipper)((Object)iterator)).add(Paths.get("client", new String[0]).resolve(this.options.getFile().getName()), this.options.dumpOptionsForReport());
                list.forEach(((FileZipper)((Object)iterator))::add);
            }
            finally {
                ((FileZipper)((Object)iterator)).close();
            }
        }
        finally {
            for (Path path2 : list) {
                try {
                    FileUtils.forceDelete((File)path2.toFile());
                }
                catch (IOException iOException) {
                    LOGGER.warn("Failed to delete temporary profiling result {}", (Object)path2, (Object)iOException);
                }
            }
        }
        return path;
    }

    public void debugFpsMeterKeyPress(int n) {
        if (this.fpsPieResults == null) {
            return;
        }
        List<ResultField> list = this.fpsPieResults.getTimes(this.debugPath);
        if (list.isEmpty()) {
            return;
        }
        ResultField resultField = list.remove(0);
        if (n == 0) {
            int n2;
            if (!resultField.name.isEmpty() && (n2 = this.debugPath.lastIndexOf(30)) >= 0) {
                this.debugPath = this.debugPath.substring(0, n2);
            }
        } else if (--n < list.size() && !"unspecified".equals(list.get((int)n).name)) {
            if (!this.debugPath.isEmpty()) {
                this.debugPath = this.debugPath + "\u001e";
            }
            this.debugPath = this.debugPath + list.get((int)n).name;
        }
    }

    /*
     * WARNING - void declaration
     */
    private void renderFpsMeter(PoseStack poseStack, ProfileResults profileResults) {
        void var15_17;
        int n;
        List<ResultField> list = profileResults.getTimes(this.debugPath);
        ResultField resultField = list.remove(0);
        RenderSystem.clear(256, ON_OSX);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f matrix4f = Matrix4f.orthographic(0.0f, this.window.getWidth(), 0.0f, this.window.getHeight(), 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(matrix4f);
        PoseStack poseStack2 = RenderSystem.getModelViewStack();
        poseStack2.setIdentity();
        poseStack2.translate(0.0, 0.0, -2000.0);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableTexture();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        int n2 = 160;
        int n3 = this.window.getWidth() - 160 - 10;
        int n4 = this.window.getHeight() - 320;
        RenderSystem.enableBlend();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex((float)n3 - 176.0f, (float)n4 - 96.0f - 16.0f, 0.0).color(200, 0, 0, 0).endVertex();
        bufferBuilder.vertex((float)n3 - 176.0f, n4 + 320, 0.0).color(200, 0, 0, 0).endVertex();
        bufferBuilder.vertex((float)n3 + 176.0f, n4 + 320, 0.0).color(200, 0, 0, 0).endVertex();
        bufferBuilder.vertex((float)n3 + 176.0f, (float)n4 - 96.0f - 16.0f, 0.0).color(200, 0, 0, 0).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
        double d = 0.0;
        for (ResultField object2 : list) {
            float f;
            float f2;
            float f3;
            int n5;
            int n6 = Mth.floor(object2.percentage / 4.0) + 1;
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            n = object2.getColor();
            int n7 = n >> 16 & 0xFF;
            int n8 = n >> 8 & 0xFF;
            int n9 = n & 0xFF;
            bufferBuilder.vertex(n3, n4, 0.0).color(n7, n8, n9, 255).endVertex();
            for (n5 = n6; n5 >= 0; --n5) {
                f3 = (float)((d + object2.percentage * (double)n5 / (double)n6) * 6.2831854820251465 / 100.0);
                f2 = Mth.sin(f3) * 160.0f;
                f = Mth.cos(f3) * 160.0f * 0.5f;
                bufferBuilder.vertex((float)n3 + f2, (float)n4 - f, 0.0).color(n7, n8, n9, 255).endVertex();
            }
            tesselator.end();
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (n5 = n6; n5 >= 0; --n5) {
                f3 = (float)((d + object2.percentage * (double)n5 / (double)n6) * 6.2831854820251465 / 100.0);
                f2 = Mth.sin(f3) * 160.0f;
                f = Mth.cos(f3) * 160.0f * 0.5f;
                if (f > 0.0f) continue;
                bufferBuilder.vertex((float)n3 + f2, (float)n4 - f, 0.0).color(n7 >> 1, n8 >> 1, n9 >> 1, 255).endVertex();
                bufferBuilder.vertex((float)n3 + f2, (float)n4 - f + 10.0f, 0.0).color(n7 >> 1, n8 >> 1, n9 >> 1, 255).endVertex();
            }
            tesselator.end();
            d += object2.percentage;
        }
        DecimalFormat decimalFormat = new DecimalFormat("##0.00");
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        RenderSystem.enableTexture();
        String i = ProfileResults.demanglePath(resultField.name);
        Object object = "";
        if (!"unspecified".equals(i)) {
            object = (String)object + "[0] ";
        }
        object = i.isEmpty() ? (String)object + "ROOT " : (String)object + i + " ";
        n = 0xFFFFFF;
        this.font.drawShadow(poseStack, (String)object, (float)(n3 - 160), (float)(n4 - 80 - 16), 0xFFFFFF);
        object = decimalFormat.format(resultField.globalPercentage) + "%";
        this.font.drawShadow(poseStack, (String)object, (float)(n3 + 160 - this.font.width((String)object)), (float)(n4 - 80 - 16), 0xFFFFFF);
        boolean bl = false;
        while (var15_17 < list.size()) {
            object = list.get((int)var15_17);
            StringBuilder stringBuilder = new StringBuilder();
            if ("unspecified".equals(((ResultField)object).name)) {
                stringBuilder.append("[?] ");
            } else {
                stringBuilder.append("[").append((int)(var15_17 + true)).append("] ");
            }
            Object object2 = stringBuilder.append(((ResultField)object).name).toString();
            this.font.drawShadow(poseStack, (String)object2, (float)(n3 - 160), (float)(n4 + 80 + var15_17 * 8 + 20), ((ResultField)object).getColor());
            object2 = decimalFormat.format(((ResultField)object).percentage) + "%";
            this.font.drawShadow(poseStack, (String)object2, (float)(n3 + 160 - 50 - this.font.width((String)object2)), (float)(n4 + 80 + var15_17 * 8 + 20), ((ResultField)object).getColor());
            object2 = decimalFormat.format(((ResultField)object).globalPercentage) + "%";
            this.font.drawShadow(poseStack, (String)object2, (float)(n3 + 160 - this.font.width((String)object2)), (float)(n4 + 80 + var15_17 * 8 + 20), ((ResultField)object).getColor());
            ++var15_17;
        }
    }

    public void stop() {
        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void pauseGame(boolean bl) {
        boolean bl2;
        if (this.screen != null) {
            return;
        }
        boolean bl3 = bl2 = this.hasSingleplayerServer() && !this.singleplayerServer.isPublished();
        if (bl2) {
            this.setScreen(new PauseScreen(!bl));
            this.soundManager.pause();
        } else {
            this.setScreen(new PauseScreen(true));
        }
    }

    private void continueAttack(boolean bl) {
        if (!bl) {
            this.missTime = 0;
        }
        if (this.missTime > 0 || this.player.isUsingItem()) {
            return;
        }
        if (bl && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
            Direction direction;
            BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            if (!this.level.getBlockState(blockPos).isAir() && this.gameMode.continueDestroyBlock(blockPos, direction = blockHitResult.getDirection())) {
                this.particleEngine.crack(blockPos, direction);
                this.player.swing(InteractionHand.MAIN_HAND);
            }
            return;
        }
        this.gameMode.stopDestroyBlock();
    }

    private boolean startAttack() {
        if (this.missTime > 0) {
            return false;
        }
        if (this.hitResult == null) {
            LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
            if (this.gameMode.hasMissTime()) {
                this.missTime = 10;
            }
            return false;
        }
        if (this.player.isHandsBusy()) {
            return false;
        }
        boolean bl = false;
        switch (this.hitResult.getType()) {
            case ENTITY: {
                this.gameMode.attack(this.player, ((EntityHitResult)this.hitResult).getEntity());
                break;
            }
            case BLOCK: {
                BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
                BlockPos blockPos = blockHitResult.getBlockPos();
                if (!this.level.getBlockState(blockPos).isAir()) {
                    this.gameMode.startDestroyBlock(blockPos, blockHitResult.getDirection());
                    if (!this.level.getBlockState(blockPos).isAir()) break;
                    bl = true;
                    break;
                }
            }
            case MISS: {
                if (this.gameMode.hasMissTime()) {
                    this.missTime = 10;
                }
                this.player.resetAttackStrengthTicker();
            }
        }
        this.player.swing(InteractionHand.MAIN_HAND);
        return bl;
    }

    private void startUseItem() {
        if (this.gameMode.isDestroying()) {
            return;
        }
        this.rightClickDelay = 4;
        if (this.player.isHandsBusy()) {
            return;
        }
        if (this.hitResult == null) {
            LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
        }
        for (InteractionHand interactionHand : InteractionHand.values()) {
            Object object;
            ItemStack itemStack = this.player.getItemInHand(interactionHand);
            if (this.hitResult != null) {
                switch (this.hitResult.getType()) {
                    case ENTITY: {
                        object = (EntityHitResult)this.hitResult;
                        Entity entity = ((EntityHitResult)object).getEntity();
                        if (!this.level.getWorldBorder().isWithinBounds(entity.blockPosition())) {
                            return;
                        }
                        InteractionResult interactionResult = this.gameMode.interactAt(this.player, entity, (EntityHitResult)object, interactionHand);
                        if (!interactionResult.consumesAction()) {
                            interactionResult = this.gameMode.interact(this.player, entity, interactionHand);
                        }
                        if (!interactionResult.consumesAction()) break;
                        if (interactionResult.shouldSwing()) {
                            this.player.swing(interactionHand);
                        }
                        return;
                    }
                    case BLOCK: {
                        BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
                        int n = itemStack.getCount();
                        InteractionResult interactionResult = this.gameMode.useItemOn(this.player, this.level, interactionHand, blockHitResult);
                        if (interactionResult.consumesAction()) {
                            if (interactionResult.shouldSwing()) {
                                this.player.swing(interactionHand);
                                if (!itemStack.isEmpty() && (itemStack.getCount() != n || this.gameMode.hasInfiniteItems())) {
                                    this.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
                                }
                            }
                            return;
                        }
                        if (interactionResult != InteractionResult.FAIL) break;
                        return;
                    }
                }
            }
            if (itemStack.isEmpty() || !((InteractionResult)((Object)(object = this.gameMode.useItem(this.player, this.level, interactionHand)))).consumesAction()) continue;
            if (((InteractionResult)((Object)object)).shouldSwing()) {
                this.player.swing(interactionHand);
            }
            this.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
            return;
        }
    }

    public MusicManager getMusicManager() {
        return this.musicManager;
    }

    public void tick() {
        Object object;
        Object object2;
        if (this.rightClickDelay > 0) {
            --this.rightClickDelay;
        }
        this.profiler.push("gui");
        this.gui.tick(this.pause);
        this.profiler.pop();
        this.gameRenderer.pick(1.0f);
        this.tutorial.onLookAt(this.level, this.hitResult);
        this.profiler.push("gameMode");
        if (!this.pause && this.level != null) {
            this.gameMode.tick();
        }
        this.profiler.popPush("textures");
        if (this.level != null) {
            this.textureManager.tick();
        }
        if (this.screen == null && this.player != null) {
            if (this.player.isDeadOrDying() && !(this.screen instanceof DeathScreen)) {
                this.setScreen(null);
            } else if (this.player.isSleeping() && this.level != null) {
                this.setScreen(new InBedChatScreen());
            }
        } else {
            object2 = this.screen;
            if (object2 instanceof InBedChatScreen) {
                object = (InBedChatScreen)object2;
                if (!this.player.isSleeping()) {
                    ((InBedChatScreen)object).onPlayerWokeUp();
                }
            }
        }
        if (this.screen != null) {
            this.missTime = 10000;
        }
        if (this.screen != null) {
            Screen.wrapScreenError(() -> this.screen.tick(), "Ticking screen", this.screen.getClass().getCanonicalName());
        }
        if (!this.options.renderDebug) {
            this.gui.clearCache();
        }
        if (this.overlay == null && (this.screen == null || this.screen.passEvents)) {
            this.profiler.popPush("Keybindings");
            this.handleKeybinds();
            if (this.missTime > 0) {
                --this.missTime;
            }
        }
        if (this.level != null) {
            this.profiler.popPush("gameRenderer");
            if (!this.pause) {
                this.gameRenderer.tick();
            }
            this.profiler.popPush("levelRenderer");
            if (!this.pause) {
                this.levelRenderer.tick();
            }
            this.profiler.popPush("level");
            if (!this.pause) {
                if (this.level.getSkyFlashTime() > 0) {
                    this.level.setSkyFlashTime(this.level.getSkyFlashTime() - 1);
                }
                this.level.tickEntities();
            }
        } else if (this.gameRenderer.currentEffect() != null) {
            this.gameRenderer.shutdownEffect();
        }
        if (!this.pause) {
            this.musicManager.tick();
        }
        this.soundManager.tick(this.pause);
        if (this.level != null) {
            if (!this.pause) {
                if (!this.options.joinedFirstServer && this.isMultiplayerServer()) {
                    object = new TranslatableComponent("tutorial.socialInteractions.title");
                    object2 = new TranslatableComponent("tutorial.socialInteractions.description", Tutorial.key("socialInteractions"));
                    this.socialInteractionsToast = new TutorialToast(TutorialToast.Icons.SOCIAL_INTERACTIONS, (Component)object, (Component)object2, true);
                    this.tutorial.addTimedToast(this.socialInteractionsToast, 160);
                    this.options.joinedFirstServer = true;
                    this.options.save();
                }
                this.tutorial.tick();
                try {
                    this.level.tick(() -> true);
                }
                catch (Throwable throwable) {
                    object2 = CrashReport.forThrowable(throwable, "Exception in world tick");
                    if (this.level == null) {
                        CrashReportCategory crashReportCategory = ((CrashReport)object2).addCategory("Affected level");
                        crashReportCategory.setDetail("Problem", "Level is null!");
                    } else {
                        this.level.fillReportDetails((CrashReport)object2);
                    }
                    throw new ReportedException((CrashReport)object2);
                }
            }
            this.profiler.popPush("animateTick");
            if (!this.pause && this.level != null) {
                this.level.animateTick(this.player.getBlockX(), this.player.getBlockY(), this.player.getBlockZ());
            }
            this.profiler.popPush("particles");
            if (!this.pause) {
                this.particleEngine.tick();
            }
        } else if (this.pendingConnection != null) {
            this.profiler.popPush("pendingConnection");
            this.pendingConnection.tick();
        }
        this.profiler.popPush("keyboard");
        this.keyboardHandler.tick();
        this.profiler.pop();
    }

    private boolean isMultiplayerServer() {
        return !this.isLocalServer || this.singleplayerServer != null && this.singleplayerServer.isPublished();
    }

    private void handleKeybinds() {
        int n;
        while (this.options.keyTogglePerspective.consumeClick()) {
            CameraType cameraType = this.options.getCameraType();
            this.options.setCameraType(this.options.getCameraType().cycle());
            if (cameraType.isFirstPerson() != this.options.getCameraType().isFirstPerson()) {
                this.gameRenderer.checkEntityPostEffect(this.options.getCameraType().isFirstPerson() ? this.getCameraEntity() : null);
            }
            this.levelRenderer.needsUpdate();
        }
        while (this.options.keySmoothCamera.consumeClick()) {
            this.options.smoothCamera = !this.options.smoothCamera;
        }
        for (n = 0; n < 9; ++n) {
            boolean bl = this.options.keySaveHotbarActivator.isDown();
            boolean bl2 = this.options.keyLoadHotbarActivator.isDown();
            if (!this.options.keyHotbarSlots[n].consumeClick()) continue;
            if (this.player.isSpectator()) {
                this.gui.getSpectatorGui().onHotbarSelected(n);
                continue;
            }
            if (this.player.isCreative() && this.screen == null && (bl2 || bl)) {
                CreativeModeInventoryScreen.handleHotbarLoadOrSave(this, n, bl2, bl);
                continue;
            }
            this.player.getInventory().selected = n;
        }
        while (this.options.keySocialInteractions.consumeClick()) {
            if (!this.isMultiplayerServer()) {
                this.player.displayClientMessage(SOCIAL_INTERACTIONS_NOT_AVAILABLE, true);
                NarratorChatListener.INSTANCE.sayNow(SOCIAL_INTERACTIONS_NOT_AVAILABLE);
                continue;
            }
            if (this.socialInteractionsToast != null) {
                this.tutorial.removeTimedToast(this.socialInteractionsToast);
                this.socialInteractionsToast = null;
            }
            this.setScreen(new SocialInteractionsScreen());
        }
        while (this.options.keyInventory.consumeClick()) {
            if (this.gameMode.isServerControlledInventory()) {
                this.player.sendOpenInventory();
                continue;
            }
            this.tutorial.onOpenInventory();
            this.setScreen(new InventoryScreen(this.player));
        }
        while (this.options.keyAdvancements.consumeClick()) {
            this.setScreen(new AdvancementsScreen(this.player.connection.getAdvancements()));
        }
        while (this.options.keySwapOffhand.consumeClick()) {
            if (this.player.isSpectator()) continue;
            this.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
        }
        while (this.options.keyDrop.consumeClick()) {
            if (this.player.isSpectator() || !this.player.drop(Screen.hasControlDown())) continue;
            this.player.swing(InteractionHand.MAIN_HAND);
        }
        while (this.options.keyChat.consumeClick()) {
            this.openChatScreen("");
        }
        if (this.screen == null && this.overlay == null && this.options.keyCommand.consumeClick()) {
            this.openChatScreen("/");
        }
        n = 0;
        if (this.player.isUsingItem()) {
            if (!this.options.keyUse.isDown()) {
                this.gameMode.releaseUsingItem(this.player);
            }
            while (this.options.keyAttack.consumeClick()) {
            }
            while (this.options.keyUse.consumeClick()) {
            }
            while (this.options.keyPickItem.consumeClick()) {
            }
        } else {
            while (this.options.keyAttack.consumeClick()) {
                n |= this.startAttack();
            }
            while (this.options.keyUse.consumeClick()) {
                this.startUseItem();
            }
            while (this.options.keyPickItem.consumeClick()) {
                this.pickBlock();
            }
        }
        if (this.options.keyUse.isDown() && this.rightClickDelay == 0 && !this.player.isUsingItem()) {
            this.startUseItem();
        }
        this.continueAttack(this.screen == null && n == 0 && this.options.keyAttack.isDown() && this.mouseHandler.isMouseGrabbed());
    }

    public ClientTelemetryManager createTelemetryManager() {
        return new ClientTelemetryManager(this, this.userApiService, this.user.getXuid(), this.user.getClientId(), this.deviceSessionId);
    }

    public void loadLevel(String string) {
        this.doLoadLevel(string, WorldStem.DataPackConfigSupplier::loadFromWorld, WorldStem.WorldDataSupplier::loadFromWorld, false, ExperimentalDialogType.BACKUP);
    }

    public void createLevel(String string, LevelSettings levelSettings, RegistryAccess registryAccess, WorldGenSettings worldGenSettings) {
        this.doLoadLevel(string, levelStorageAccess -> levelSettings::getDataPackConfig, levelStorageAccess -> (resourceManager, dataPackConfig) -> {
            RegistryAccess.Writable writable = RegistryAccess.builtinCopy();
            RegistryOps registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
            RegistryOps registryOps2 = RegistryOps.createAndLoad(JsonOps.INSTANCE, writable, resourceManager);
            DataResult dataResult = WorldGenSettings.CODEC.encodeStart(registryOps, (Object)worldGenSettings).setLifecycle(Lifecycle.stable()).flatMap(jsonElement -> WorldGenSettings.CODEC.parse(registryOps2, jsonElement));
            WorldGenSettings worldGenSettings2 = (WorldGenSettings)dataResult.getOrThrow(false, Util.prefix("Error reading worldgen settings after loading data packs: ", arg_0 -> ((Logger)LOGGER).error(arg_0)));
            return Pair.of((Object)new PrimaryLevelData(levelSettings, worldGenSettings2, dataResult.lifecycle()), (Object)writable.freeze());
        }, false, ExperimentalDialogType.CREATE);
    }

    private void doLoadLevel(String string, Function<LevelStorageSource.LevelStorageAccess, WorldStem.DataPackConfigSupplier> function, Function<LevelStorageSource.LevelStorageAccess, WorldStem.WorldDataSupplier> function2, boolean bl, ExperimentalDialogType experimentalDialogType) {
        Object object;
        Object object2;
        Object object3;
        boolean bl2;
        WorldStem worldStem;
        LevelStorageSource.LevelStorageAccess levelStorageAccess;
        try {
            levelStorageAccess = this.levelSource.createAccess(string);
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to read level {} data", (Object)string, (Object)iOException);
            SystemToast.onWorldAccessFailure(this, string);
            this.setScreen(null);
            return;
        }
        PackRepository packRepository = Minecraft.createPackRepository(levelStorageAccess);
        try {
            worldStem = this.makeWorldStem(packRepository, bl, function.apply(levelStorageAccess), function2.apply(levelStorageAccess));
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", (Throwable)exception);
            this.setScreen(new DatapackLoadFailureScreen(() -> this.doLoadLevel(string, function, function2, true, experimentalDialogType)));
            try {
                packRepository.close();
                levelStorageAccess.close();
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to unlock access to level {}", (Object)string, (Object)iOException);
            }
            return;
        }
        WorldData worldData = worldStem.worldData();
        boolean bl3 = worldData.worldGenSettings().isOldCustomizedWorld();
        boolean bl4 = bl2 = worldData.worldGenSettingsLifecycle() != Lifecycle.stable();
        if (experimentalDialogType != ExperimentalDialogType.NONE && (bl3 || bl2)) {
            this.displayExperimentalConfirmationDialog(experimentalDialogType, string, bl3, () -> this.doLoadLevel(string, function, function2, bl, ExperimentalDialogType.NONE));
            worldStem.close();
            try {
                packRepository.close();
                levelStorageAccess.close();
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to unlock access to level {}", (Object)string, (Object)iOException);
            }
            return;
        }
        this.clearLevel();
        this.progressListener.set(null);
        try {
            object3 = worldStem.registryAccess();
            levelStorageAccess.saveDataTag((RegistryAccess)object3, worldData);
            worldStem.updateGlobals();
            object2 = new YggdrasilAuthenticationService(this.proxy);
            object = object2.createMinecraftSessionService();
            GameProfileRepository gameProfileRepository = object2.createProfileRepository();
            GameProfileCache gameProfileCache = new GameProfileCache(gameProfileRepository, new File(this.gameDirectory, MinecraftServer.USERID_CACHE_FILE.getName()));
            gameProfileCache.setExecutor(this);
            SkullBlockEntity.setup(gameProfileCache, (MinecraftSessionService)object, this);
            GameProfileCache.setUsesAuthentication(false);
            this.singleplayerServer = MinecraftServer.spin(arg_0 -> this.lambda$doLoadLevel$38(levelStorageAccess, packRepository, worldStem, (MinecraftSessionService)object, gameProfileRepository, gameProfileCache, arg_0));
            this.isLocalServer = true;
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Starting integrated server");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Starting integrated server");
            crashReportCategory.setDetail("Level ID", string);
            crashReportCategory.setDetail("Level Name", worldData.getLevelName());
            throw new ReportedException(crashReport);
        }
        while (this.progressListener.get() == null) {
            Thread.yield();
        }
        object3 = new LevelLoadingScreen(this.progressListener.get());
        this.setScreen((Screen)object3);
        this.profiler.push("waitForServer");
        while (!this.singleplayerServer.isReady()) {
            ((Screen)object3).tick();
            this.runTick(false);
            try {
                Thread.sleep(16L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
            if (this.delayedCrash == null) continue;
            Minecraft.crash(this.delayedCrash.get());
            return;
        }
        this.profiler.pop();
        object2 = this.singleplayerServer.getConnection().startMemoryChannel();
        object = Connection.connectToLocalServer((SocketAddress)object2);
        ((Connection)((Object)object)).setListener(new ClientHandshakePacketListenerImpl((Connection)((Object)object), this, null, component -> {}));
        ((Connection)((Object)object)).send(new ClientIntentionPacket(object2.toString(), 0, ConnectionProtocol.LOGIN));
        ((Connection)((Object)object)).send(new ServerboundHelloPacket(this.getUser().getGameProfile()));
        this.pendingConnection = object;
    }

    private void displayExperimentalConfirmationDialog(ExperimentalDialogType experimentalDialogType, String string, boolean bl3, Runnable runnable) {
        if (experimentalDialogType == ExperimentalDialogType.BACKUP) {
            TranslatableComponent translatableComponent;
            TranslatableComponent translatableComponent2;
            if (bl3) {
                translatableComponent2 = new TranslatableComponent("selectWorld.backupQuestion.customized");
                translatableComponent = new TranslatableComponent("selectWorld.backupWarning.customized");
            } else {
                translatableComponent2 = new TranslatableComponent("selectWorld.backupQuestion.experimental");
                translatableComponent = new TranslatableComponent("selectWorld.backupWarning.experimental");
            }
            this.setScreen(new BackupConfirmScreen(null, (bl, bl2) -> {
                if (bl) {
                    EditWorldScreen.makeBackupAndShowToast(this.levelSource, string);
                }
                runnable.run();
            }, translatableComponent2, translatableComponent, false));
        } else {
            this.setScreen(new ConfirmScreen(bl -> {
                if (bl) {
                    runnable.run();
                } else {
                    this.setScreen(null);
                    try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.levelSource.createAccess(string);){
                        levelStorageAccess.deleteLevel();
                    }
                    catch (IOException iOException) {
                        SystemToast.onWorldDeleteFailure(this, string);
                        LOGGER.error("Failed to delete world {}", (Object)string, (Object)iOException);
                    }
                }
            }, new TranslatableComponent("selectWorld.backupQuestion.experimental"), new TranslatableComponent("selectWorld.backupWarning.experimental"), CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
        }
    }

    public WorldStem makeWorldStem(LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl) throws ExecutionException, InterruptedException {
        PackRepository packRepository = Minecraft.createPackRepository(levelStorageAccess);
        return this.makeWorldStem(packRepository, bl, WorldStem.DataPackConfigSupplier.loadFromWorld(levelStorageAccess), WorldStem.WorldDataSupplier.loadFromWorld(levelStorageAccess));
    }

    public WorldStem makeWorldStem(PackRepository packRepository, boolean bl, WorldStem.DataPackConfigSupplier dataPackConfigSupplier, WorldStem.WorldDataSupplier worldDataSupplier) throws InterruptedException, ExecutionException {
        try {
            WorldStem.InitConfig initConfig = new WorldStem.InitConfig(packRepository, Commands.CommandSelection.INTEGRATED, 2, bl);
            CompletableFuture<WorldStem> completableFuture = WorldStem.load(initConfig, dataPackConfigSupplier, worldDataSupplier, Util.backgroundExecutor(), this);
            this.managedBlock(completableFuture::isDone);
            return completableFuture.get();
        }
        catch (InterruptedException | ExecutionException exception) {
            packRepository.close();
            throw exception;
        }
    }

    private static PackRepository createPackRepository(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        return new PackRepository(PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD));
    }

    public void setLevel(ClientLevel clientLevel) {
        ProgressScreen progressScreen = new ProgressScreen(true);
        progressScreen.progressStartNoAbort(new TranslatableComponent("connect.joining"));
        this.updateScreenAndTick(progressScreen);
        this.level = clientLevel;
        this.updateLevelInEngines(clientLevel);
        if (!this.isLocalServer) {
            YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(this.proxy);
            MinecraftSessionService minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
            GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
            GameProfileCache gameProfileCache = new GameProfileCache(gameProfileRepository, new File(this.gameDirectory, MinecraftServer.USERID_CACHE_FILE.getName()));
            gameProfileCache.setExecutor(this);
            SkullBlockEntity.setup(gameProfileCache, minecraftSessionService, this);
            GameProfileCache.setUsesAuthentication(false);
        }
    }

    public void clearLevel() {
        this.clearLevel(new ProgressScreen(true));
    }

    public void clearLevel(Screen screen) {
        ClientPacketListener clientPacketListener = this.getConnection();
        if (clientPacketListener != null) {
            this.dropAllTasks();
            clientPacketListener.cleanup();
        }
        this.playerSocialManager.stopOnlineMode();
        IntegratedServer integratedServer = this.singleplayerServer;
        this.singleplayerServer = null;
        this.gameRenderer.resetData();
        this.gameMode = null;
        NarratorChatListener.INSTANCE.clear();
        this.updateScreenAndTick(screen);
        if (this.level != null) {
            if (integratedServer != null) {
                this.profiler.push("waitForServer");
                while (!integratedServer.isShutdown()) {
                    this.runTick(false);
                }
                this.profiler.pop();
            }
            this.clientPackSource.clearServerPack();
            this.gui.onDisconnected();
            this.currentServer = null;
            this.isLocalServer = false;
            this.game.onLeaveGameSession();
        }
        this.level = null;
        this.updateLevelInEngines(null);
        this.player = null;
        SkullBlockEntity.clear();
    }

    private void updateScreenAndTick(Screen screen) {
        this.profiler.push("forcedTick");
        this.soundManager.stop();
        this.cameraEntity = null;
        this.pendingConnection = null;
        this.setScreen(screen);
        this.runTick(false);
        this.profiler.pop();
    }

    public void forceSetScreen(Screen screen) {
        this.profiler.push("forcedTick");
        this.setScreen(screen);
        this.runTick(false);
        this.profiler.pop();
    }

    private void updateLevelInEngines(@Nullable ClientLevel clientLevel) {
        this.levelRenderer.setLevel(clientLevel);
        this.particleEngine.setLevel(clientLevel);
        this.blockEntityRenderDispatcher.setLevel(clientLevel);
        this.updateTitle();
    }

    public boolean allowsMultiplayer() {
        return this.allowsMultiplayer && this.userApiService.properties().flag(UserApiService.UserFlag.SERVERS_ALLOWED);
    }

    public boolean allowsRealms() {
        return this.userApiService.properties().flag(UserApiService.UserFlag.REALMS_ALLOWED);
    }

    public boolean isBlocked(UUID uUID) {
        if (!this.getChatStatus().isChatAllowed(false)) {
            return (this.player == null || !uUID.equals(this.player.getUUID())) && !uUID.equals(Util.NIL_UUID);
        }
        return this.playerSocialManager.shouldHideMessageFrom(uUID);
    }

    public ChatStatus getChatStatus() {
        if (this.options.chatVisibility == ChatVisiblity.HIDDEN) {
            return ChatStatus.DISABLED_BY_OPTIONS;
        }
        if (!this.allowsChat) {
            return ChatStatus.DISABLED_BY_LAUNCHER;
        }
        if (!this.userApiService.properties().flag(UserApiService.UserFlag.CHAT_ALLOWED)) {
            return ChatStatus.DISABLED_BY_PROFILE;
        }
        return ChatStatus.ENABLED;
    }

    public final boolean isDemo() {
        return this.demo;
    }

    @Nullable
    public ClientPacketListener getConnection() {
        return this.player == null ? null : this.player.connection;
    }

    public static boolean renderNames() {
        return !Minecraft.instance.options.hideGui;
    }

    public static boolean useFancyGraphics() {
        return Minecraft.instance.options.graphicsMode.getId() >= GraphicsStatus.FANCY.getId();
    }

    public static boolean useShaderTransparency() {
        return !Minecraft.instance.gameRenderer.isPanoramicMode() && Minecraft.instance.options.graphicsMode.getId() >= GraphicsStatus.FABULOUS.getId();
    }

    public static boolean useAmbientOcclusion() {
        return Minecraft.instance.options.ambientOcclusion != AmbientOcclusionStatus.OFF;
    }

    private void pickBlock() {
        ItemStack itemStack;
        Object object;
        if (this.hitResult == null || this.hitResult.getType() == HitResult.Type.MISS) {
            return;
        }
        boolean bl = this.player.getAbilities().instabuild;
        BlockEntity blockEntity = null;
        HitResult.Type type = this.hitResult.getType();
        if (type == HitResult.Type.BLOCK) {
            object = ((BlockHitResult)this.hitResult).getBlockPos();
            BlockState blockState = this.level.getBlockState((BlockPos)object);
            if (blockState.isAir()) {
                return;
            }
            Block block = blockState.getBlock();
            itemStack = block.getCloneItemStack(this.level, (BlockPos)object, blockState);
            if (itemStack.isEmpty()) {
                return;
            }
            if (bl && Screen.hasControlDown() && blockState.hasBlockEntity()) {
                blockEntity = this.level.getBlockEntity((BlockPos)object);
            }
        } else if (type == HitResult.Type.ENTITY && bl) {
            object = ((EntityHitResult)this.hitResult).getEntity();
            itemStack = ((Entity)object).getPickResult();
            if (itemStack == null) {
                return;
            }
        } else {
            return;
        }
        if (itemStack.isEmpty()) {
            object = "";
            if (type == HitResult.Type.BLOCK) {
                object = Registry.BLOCK.getKey(this.level.getBlockState(((BlockHitResult)this.hitResult).getBlockPos()).getBlock()).toString();
            } else if (type == HitResult.Type.ENTITY) {
                object = Registry.ENTITY_TYPE.getKey(((EntityHitResult)this.hitResult).getEntity().getType()).toString();
            }
            LOGGER.warn("Picking on: [{}] {} gave null item", (Object)type, object);
            return;
        }
        object = this.player.getInventory();
        if (blockEntity != null) {
            this.addCustomNbtData(itemStack, blockEntity);
        }
        int n = ((Inventory)object).findSlotMatchingItem(itemStack);
        if (bl) {
            ((Inventory)object).setPickedItem(itemStack);
            this.gameMode.handleCreativeModeItemAdd(this.player.getItemInHand(InteractionHand.MAIN_HAND), 36 + ((Inventory)object).selected);
        } else if (n != -1) {
            if (Inventory.isHotbarSlot(n)) {
                ((Inventory)object).selected = n;
            } else {
                this.gameMode.handlePickItem(n);
            }
        }
    }

    private ItemStack addCustomNbtData(ItemStack itemStack, BlockEntity blockEntity) {
        CompoundTag compoundTag = blockEntity.saveWithFullMetadata();
        if (itemStack.getItem() instanceof PlayerHeadItem && compoundTag.contains("SkullOwner")) {
            CompoundTag compoundTag2 = compoundTag.getCompound("SkullOwner");
            itemStack.getOrCreateTag().put("SkullOwner", compoundTag2);
            return itemStack;
        }
        BlockItem.setBlockEntityData(itemStack, blockEntity.getType(), compoundTag);
        CompoundTag compoundTag3 = new CompoundTag();
        ListTag listTag = new ListTag();
        listTag.add(StringTag.valueOf("\"(+NBT)\""));
        compoundTag3.put("Lore", listTag);
        itemStack.addTagElement("display", compoundTag3);
        return itemStack;
    }

    public CrashReport fillReport(CrashReport crashReport) {
        SystemReport systemReport = crashReport.getSystemReport();
        Minecraft.fillSystemReport(systemReport, this, this.languageManager, this.launchedVersion, this.options);
        if (this.level != null) {
            this.level.fillReportDetails(crashReport);
        }
        if (this.singleplayerServer != null) {
            this.singleplayerServer.fillSystemReport(systemReport);
        }
        this.reloadStateTracker.fillCrashReport(crashReport);
        return crashReport;
    }

    public static void fillReport(@Nullable Minecraft minecraft, @Nullable LanguageManager languageManager, String string, @Nullable Options options, CrashReport crashReport) {
        SystemReport systemReport = crashReport.getSystemReport();
        Minecraft.fillSystemReport(systemReport, minecraft, languageManager, string, options);
    }

    private static SystemReport fillSystemReport(SystemReport systemReport, @Nullable Minecraft minecraft, @Nullable LanguageManager languageManager, String string, Options options) {
        systemReport.setDetail("Launched Version", () -> string);
        systemReport.setDetail("Backend library", RenderSystem::getBackendDescription);
        systemReport.setDetail("Backend API", RenderSystem::getApiDescription);
        systemReport.setDetail("Window size", () -> minecraft != null ? minecraft.window.getWidth() + "x" + minecraft.window.getHeight() : "<not initialized>");
        systemReport.setDetail("GL Caps", RenderSystem::getCapsString);
        systemReport.setDetail("GL debug messages", () -> GlDebug.isDebugEnabled() ? String.join((CharSequence)"\n", GlDebug.getLastOpenGlDebugMessages()) : "<disabled>");
        systemReport.setDetail("Using VBOs", () -> "Yes");
        systemReport.setDetail("Is Modded", () -> Minecraft.checkModStatus().fullDescription());
        systemReport.setDetail("Type", "Client (map_client.txt)");
        if (options != null) {
            String string2;
            if (instance != null && (string2 = instance.getGpuWarnlistManager().getAllWarnings()) != null) {
                systemReport.setDetail("GPU Warnings", string2);
            }
            systemReport.setDetail("Graphics mode", options.graphicsMode.toString());
            systemReport.setDetail("Resource Packs", () -> {
                StringBuilder stringBuilder = new StringBuilder();
                for (String string : options.resourcePacks) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append(string);
                    if (!options.incompatibleResourcePacks.contains(string)) continue;
                    stringBuilder.append(" (incompatible)");
                }
                return stringBuilder.toString();
            });
        }
        if (languageManager != null) {
            systemReport.setDetail("Current Language", () -> languageManager.getSelected().toString());
        }
        systemReport.setDetail("CPU", GlUtil::getCpuInfo);
        return systemReport;
    }

    public static Minecraft getInstance() {
        return instance;
    }

    public CompletableFuture<Void> delayTextureReload() {
        return this.submit(this::reloadResourcePacks).thenCompose(completableFuture -> completableFuture);
    }

    public void setCurrentServer(@Nullable ServerData serverData) {
        this.currentServer = serverData;
    }

    @Nullable
    public ServerData getCurrentServer() {
        return this.currentServer;
    }

    public boolean isLocalServer() {
        return this.isLocalServer;
    }

    public boolean hasSingleplayerServer() {
        return this.isLocalServer && this.singleplayerServer != null;
    }

    @Nullable
    public IntegratedServer getSingleplayerServer() {
        return this.singleplayerServer;
    }

    public User getUser() {
        return this.user;
    }

    public PropertyMap getProfileProperties() {
        if (this.profileProperties.isEmpty()) {
            GameProfile gameProfile = this.getMinecraftSessionService().fillProfileProperties(this.user.getGameProfile(), false);
            this.profileProperties.putAll((Multimap)gameProfile.getProperties());
        }
        return this.profileProperties;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public TextureManager getTextureManager() {
        return this.textureManager;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public PackRepository getResourcePackRepository() {
        return this.resourcePackRepository;
    }

    public ClientPackSource getClientPackSource() {
        return this.clientPackSource;
    }

    public File getResourcePackDirectory() {
        return this.resourcePackDirectory;
    }

    public LanguageManager getLanguageManager() {
        return this.languageManager;
    }

    public Function<ResourceLocation, TextureAtlasSprite> getTextureAtlas(ResourceLocation resourceLocation) {
        return this.modelManager.getAtlas(resourceLocation)::getSprite;
    }

    public boolean is64Bit() {
        return this.is64bit;
    }

    public boolean isPaused() {
        return this.pause;
    }

    public GpuWarnlistManager getGpuWarnlistManager() {
        return this.gpuWarnlistManager;
    }

    public SoundManager getSoundManager() {
        return this.soundManager;
    }

    public Music getSituationalMusic() {
        if (this.screen instanceof WinScreen) {
            return Musics.CREDITS;
        }
        if (this.player != null) {
            if (this.player.level.dimension() == Level.END) {
                if (this.gui.getBossOverlay().shouldPlayMusic()) {
                    return Musics.END_BOSS;
                }
                return Musics.END;
            }
            Holder<Biome> holder = this.player.level.getBiome(this.player.blockPosition());
            Biome.BiomeCategory biomeCategory = Biome.getBiomeCategory(holder);
            if (this.musicManager.isPlayingMusic(Musics.UNDER_WATER) || this.player.isUnderWater() && (biomeCategory == Biome.BiomeCategory.OCEAN || biomeCategory == Biome.BiomeCategory.RIVER)) {
                return Musics.UNDER_WATER;
            }
            if (this.player.level.dimension() != Level.NETHER && this.player.getAbilities().instabuild && this.player.getAbilities().mayfly) {
                return Musics.CREATIVE;
            }
            return holder.value().getBackgroundMusic().orElse(Musics.GAME);
        }
        return Musics.MENU;
    }

    public MinecraftSessionService getMinecraftSessionService() {
        return this.minecraftSessionService;
    }

    public SkinManager getSkinManager() {
        return this.skinManager;
    }

    @Nullable
    public Entity getCameraEntity() {
        return this.cameraEntity;
    }

    public void setCameraEntity(Entity entity) {
        this.cameraEntity = entity;
        this.gameRenderer.checkEntityPostEffect(entity);
    }

    public boolean shouldEntityAppearGlowing(Entity entity) {
        return entity.isCurrentlyGlowing() || this.player != null && this.player.isSpectator() && this.options.keySpectatorOutlines.isDown() && entity.getType() == EntityType.PLAYER;
    }

    @Override
    protected Thread getRunningThread() {
        return this.gameThread;
    }

    @Override
    protected Runnable wrapRunnable(Runnable runnable) {
        return runnable;
    }

    @Override
    protected boolean shouldRun(Runnable runnable) {
        return true;
    }

    public BlockRenderDispatcher getBlockRenderer() {
        return this.blockRenderer;
    }

    public EntityRenderDispatcher getEntityRenderDispatcher() {
        return this.entityRenderDispatcher;
    }

    public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
        return this.blockEntityRenderDispatcher;
    }

    public ItemRenderer getItemRenderer() {
        return this.itemRenderer;
    }

    public ItemInHandRenderer getItemInHandRenderer() {
        return this.itemInHandRenderer;
    }

    public <T> MutableSearchTree<T> getSearchTree(SearchRegistry.Key<T> key) {
        return this.searchRegistry.getTree(key);
    }

    public FrameTimer getFrameTimer() {
        return this.frameTimer;
    }

    public boolean isConnectedToRealms() {
        return this.connectedToRealms;
    }

    public void setConnectedToRealms(boolean bl) {
        this.connectedToRealms = bl;
    }

    public DataFixer getFixerUpper() {
        return this.fixerUpper;
    }

    public float getFrameTime() {
        return this.timer.partialTick;
    }

    public float getDeltaFrameTime() {
        return this.timer.tickDelta;
    }

    public BlockColors getBlockColors() {
        return this.blockColors;
    }

    public boolean showOnlyReducedInfo() {
        return this.player != null && this.player.isReducedDebugInfo() || this.options.reducedDebugInfo;
    }

    public ToastComponent getToasts() {
        return this.toast;
    }

    public Tutorial getTutorial() {
        return this.tutorial;
    }

    public boolean isWindowActive() {
        return this.windowActive;
    }

    public HotbarManager getHotbarManager() {
        return this.hotbarManager;
    }

    public ModelManager getModelManager() {
        return this.modelManager;
    }

    public PaintingTextureManager getPaintingTextures() {
        return this.paintingTextures;
    }

    public MobEffectTextureManager getMobEffectTextures() {
        return this.mobEffectTextures;
    }

    @Override
    public void setWindowActive(boolean bl) {
        this.windowActive = bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Component grabPanoramixScreenshot(File file, int n, int n2) {
        int n3 = this.window.getWidth();
        int n4 = this.window.getHeight();
        TextureTarget textureTarget = new TextureTarget(n, n2, true, ON_OSX);
        float f = this.player.getXRot();
        float f2 = this.player.getYRot();
        float f3 = this.player.xRotO;
        float f4 = this.player.yRotO;
        this.gameRenderer.setRenderBlockOutline(false);
        try {
            this.gameRenderer.setPanoramicMode(true);
            this.levelRenderer.graphicsChanged();
            this.window.setWidth(n);
            this.window.setHeight(n2);
            for (int i = 0; i < 6; ++i) {
                switch (i) {
                    case 0: {
                        this.player.setYRot(f2);
                        this.player.setXRot(0.0f);
                        break;
                    }
                    case 1: {
                        this.player.setYRot((f2 + 90.0f) % 360.0f);
                        this.player.setXRot(0.0f);
                        break;
                    }
                    case 2: {
                        this.player.setYRot((f2 + 180.0f) % 360.0f);
                        this.player.setXRot(0.0f);
                        break;
                    }
                    case 3: {
                        this.player.setYRot((f2 - 90.0f) % 360.0f);
                        this.player.setXRot(0.0f);
                        break;
                    }
                    case 4: {
                        this.player.setYRot(f2);
                        this.player.setXRot(-90.0f);
                        break;
                    }
                    default: {
                        this.player.setYRot(f2);
                        this.player.setXRot(90.0f);
                    }
                }
                this.player.yRotO = this.player.getYRot();
                this.player.xRotO = this.player.getXRot();
                textureTarget.bindWrite(true);
                this.gameRenderer.renderLevel(1.0f, 0L, new PoseStack());
                try {
                    Thread.sleep(10L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                Screenshot.grab(file, "panorama_" + i + ".png", textureTarget, component -> {});
            }
            MutableComponent mutableComponent = new TextComponent(file.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
            TranslatableComponent translatableComponent = new TranslatableComponent("screenshot.success", mutableComponent);
            return translatableComponent;
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't save image", (Throwable)exception);
            TranslatableComponent translatableComponent = new TranslatableComponent("screenshot.failure", exception.getMessage());
            return translatableComponent;
        }
        finally {
            this.player.setXRot(f);
            this.player.setYRot(f2);
            this.player.xRotO = f3;
            this.player.yRotO = f4;
            this.gameRenderer.setRenderBlockOutline(true);
            this.window.setWidth(n3);
            this.window.setHeight(n4);
            textureTarget.destroyBuffers();
            this.gameRenderer.setPanoramicMode(false);
            this.levelRenderer.graphicsChanged();
            this.getMainRenderTarget().bindWrite(true);
        }
    }

    private Component grabHugeScreenshot(File file, int n, int n2, int n3, int n4) {
        try {
            ByteBuffer byteBuffer = GlUtil.allocateMemory(n * n2 * 3);
            Screenshot screenshot = new Screenshot(file, n3, n4, n2);
            float f = (float)n3 / (float)n;
            float f2 = (float)n4 / (float)n2;
            float f3 = f > f2 ? f : f2;
            for (int i = (n4 - 1) / n2 * n2; i >= 0; i -= n2) {
                for (int j = 0; j < n3; j += n) {
                    RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
                    float f4 = (float)(n3 - n) / 2.0f * 2.0f - (float)(j * 2);
                    float f5 = (float)(n4 - n2) / 2.0f * 2.0f - (float)(i * 2);
                    this.gameRenderer.renderZoomed(f3, f4 /= (float)n, f5 /= (float)n2);
                    byteBuffer.clear();
                    RenderSystem.pixelStore(3333, 1);
                    RenderSystem.pixelStore(3317, 1);
                    RenderSystem.readPixels(0, 0, n, n2, 32992, 5121, byteBuffer);
                    screenshot.addRegion(byteBuffer, j, i, n, n2);
                }
                screenshot.saveRow();
            }
            File file2 = screenshot.close();
            GlUtil.freeMemory(byteBuffer);
            MutableComponent mutableComponent = new TextComponent(file2.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file2.getAbsolutePath())));
            return new TranslatableComponent("screenshot.success", mutableComponent);
        }
        catch (Exception exception) {
            LOGGER.warn("Couldn't save screenshot", (Throwable)exception);
            return new TranslatableComponent("screenshot.failure", exception.getMessage());
        }
    }

    public ProfilerFiller getProfiler() {
        return this.profiler;
    }

    public Game getGame() {
        return this.game;
    }

    @Nullable
    public StoringChunkProgressListener getProgressListener() {
        return this.progressListener.get();
    }

    public SplashManager getSplashManager() {
        return this.splashManager;
    }

    @Nullable
    public Overlay getOverlay() {
        return this.overlay;
    }

    public PlayerSocialManager getPlayerSocialManager() {
        return this.playerSocialManager;
    }

    public boolean renderOnThread() {
        return false;
    }

    public Window getWindow() {
        return this.window;
    }

    public RenderBuffers renderBuffers() {
        return this.renderBuffers;
    }

    private static Pack createClientPackAdapter(String string, Component component, boolean bl, Supplier<PackResources> supplier, PackMetadataSection packMetadataSection, Pack.Position position, PackSource packSource) {
        int n = packMetadataSection.getPackFormat();
        Supplier<PackResources> supplier2 = supplier;
        if (n <= 3) {
            supplier2 = Minecraft.adaptV3(supplier2);
        }
        if (n <= 4) {
            supplier2 = Minecraft.adaptV4(supplier2);
        }
        return new Pack(string, component, bl, supplier2, packMetadataSection, PackType.CLIENT_RESOURCES, position, packSource);
    }

    private static Supplier<PackResources> adaptV3(Supplier<PackResources> supplier) {
        return () -> new LegacyPackResourcesAdapter((PackResources)supplier.get(), LegacyPackResourcesAdapter.V3);
    }

    private static Supplier<PackResources> adaptV4(Supplier<PackResources> supplier) {
        return () -> new PackResourcesAdapterV4((PackResources)supplier.get());
    }

    public void updateMaxMipLevel(int n) {
        this.modelManager.updateMaxMipLevel(n);
    }

    public EntityModelSet getEntityModels() {
        return this.entityModels;
    }

    public boolean isTextFilteringEnabled() {
        return this.userApiService.properties().flag(UserApiService.UserFlag.PROFANITY_FILTER_ENABLED);
    }

    public void prepareForMultiplayer() {
        this.playerSocialManager.startOnlineMode();
    }

    private /* synthetic */ IntegratedServer lambda$doLoadLevel$38(LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, GameProfileCache gameProfileCache, Thread thread) {
        return new IntegratedServer(thread, this, levelStorageAccess, packRepository, worldStem, minecraftSessionService, gameProfileRepository, gameProfileCache, n -> {
            StoringChunkProgressListener storingChunkProgressListener = new StoringChunkProgressListener(n + 0);
            this.progressListener.set(storingChunkProgressListener);
            return ProcessorChunkProgressListener.createStarted(storingChunkProgressListener, this.progressTasks::add);
        });
    }

    private static /* synthetic */ void lambda$runTick$18(CompletableFuture completableFuture) {
        completableFuture.complete(null);
    }

    static {
        LOGGER = LogUtils.getLogger();
        ON_OSX = Util.getPlatform() == Util.OS.OSX;
        DEFAULT_FONT = new ResourceLocation("default");
        UNIFORM_FONT = new ResourceLocation("uniform");
        ALT_FONT = new ResourceLocation("alt");
        REGIONAL_COMPLIANCIES = new ResourceLocation("regional_compliancies.json");
        RESOURCE_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
        SOCIAL_INTERACTIONS_NOT_AVAILABLE = new TranslatableComponent("multiplayer.socialInteractions.not_available");
    }

    /*
     * Uses 'sealed' constructs - enablewith --sealed true
     */
    public static enum ChatStatus {
        ENABLED(TextComponent.EMPTY){

            @Override
            public boolean isChatAllowed(boolean bl) {
                return true;
            }
        }
        ,
        DISABLED_BY_OPTIONS(new TranslatableComponent("chat.disabled.options").withStyle(ChatFormatting.RED)){

            @Override
            public boolean isChatAllowed(boolean bl) {
                return false;
            }
        }
        ,
        DISABLED_BY_LAUNCHER(new TranslatableComponent("chat.disabled.launcher").withStyle(ChatFormatting.RED)){

            @Override
            public boolean isChatAllowed(boolean bl) {
                return bl;
            }
        }
        ,
        DISABLED_BY_PROFILE(new TranslatableComponent("chat.disabled.profile").withStyle(ChatFormatting.RED)){

            @Override
            public boolean isChatAllowed(boolean bl) {
                return bl;
            }
        };

        private final Component message;

        ChatStatus(Component component) {
            this.message = component;
        }

        public Component getMessage() {
            return this.message;
        }

        public abstract boolean isChatAllowed(boolean var1);
    }

    static enum ExperimentalDialogType {
        NONE,
        CREATE,
        BACKUP;

    }
}

