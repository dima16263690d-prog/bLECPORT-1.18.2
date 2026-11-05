/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfileRepository
 *  com.mojang.authlib.minecraft.MinecraftSessionService
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Lifecycle
 *  joptsimple.AbstractOptionSpec
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.NonOptionArgumentSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  joptsimple.OptionSpecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.OutputStream;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.Eula;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class Main {
    private static final Logger LOGGER = LogUtils.getLogger();

    @DontObfuscate
    public static void main(String[] stringArray) {
        SharedConstants.tryDetectVersion();
        OptionParser optionParser = new OptionParser();
        OptionSpecBuilder optionSpecBuilder = optionParser.accepts("nogui");
        OptionSpecBuilder optionSpecBuilder2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpecBuilder optionSpecBuilder3 = optionParser.accepts("demo");
        OptionSpecBuilder optionSpecBuilder4 = optionParser.accepts("bonusChest");
        OptionSpecBuilder optionSpecBuilder5 = optionParser.accepts("forceUpgrade");
        OptionSpecBuilder optionSpecBuilder6 = optionParser.accepts("eraseCache");
        OptionSpecBuilder optionSpecBuilder7 = optionParser.accepts("safeMode", "Loads level with vanilla datapack only");
        AbstractOptionSpec abstractOptionSpec = optionParser.accepts("help").forHelp();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec = optionParser.accepts("singleplayer").withRequiredArg();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec2 = optionParser.accepts("universe").withRequiredArg().defaultsTo((Object)".", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec3 = optionParser.accepts("world").withRequiredArg();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec4 = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo((Object)-1, (Object[])new Integer[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec5 = optionParser.accepts("serverId").withRequiredArg();
        OptionSpecBuilder optionSpecBuilder8 = optionParser.accepts("jfrProfile");
        NonOptionArgumentSpec nonOptionArgumentSpec = optionParser.nonOptions();
        try {
            WorldStem worldStem;
            Object object;
            boolean bl;
            OptionSet optionSet = optionParser.parse(stringArray);
            if (optionSet.has((OptionSpec)abstractOptionSpec)) {
                optionParser.printHelpOn((OutputStream)System.err);
                return;
            }
            CrashReport.preload();
            if (optionSet.has((OptionSpec)optionSpecBuilder8)) {
                JvmProfiler.INSTANCE.start(Environment.SERVER);
            }
            Bootstrap.bootStrap();
            Bootstrap.validate();
            Util.startTimerHackThread();
            Path path = Paths.get("server.properties", new String[0]);
            DedicatedServerSettings dedicatedServerSettings = new DedicatedServerSettings(path);
            dedicatedServerSettings.forceSave();
            Path path2 = Paths.get("eula.txt", new String[0]);
            Eula eula = new Eula(path2);
            if (optionSet.has((OptionSpec)optionSpecBuilder2)) {
                LOGGER.info("Initialized '{}' and '{}'", (Object)path.toAbsolutePath(), (Object)path2.toAbsolutePath());
                return;
            }
            if (!eula.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }
            File file = new File((String)optionSet.valueOf((OptionSpec)argumentAcceptingOptionSpec2));
            YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
            MinecraftSessionService minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
            GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
            GameProfileCache gameProfileCache = new GameProfileCache(gameProfileRepository, new File(file, MinecraftServer.USERID_CACHE_FILE.getName()));
            String string = Optional.ofNullable((String)optionSet.valueOf((OptionSpec)argumentAcceptingOptionSpec3)).orElse(dedicatedServerSettings.getProperties().levelName);
            LevelStorageSource levelStorageSource = LevelStorageSource.createDefault(file.toPath());
            LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(string);
            LevelSummary levelSummary = levelStorageAccess.getSummary();
            if (levelSummary != null) {
                if (levelSummary.requiresManualConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }
                if (!levelSummary.isCompatible()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            }
            if (bl = optionSet.has((OptionSpec)optionSpecBuilder7)) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }
            PackRepository packRepository = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD));
            try {
                object = new WorldStem.InitConfig(packRepository, Commands.CommandSelection.DEDICATED, dedicatedServerSettings.getProperties().functionPermissionLevel, bl);
                worldStem = WorldStem.load(object, () -> {
                    DataPackConfig dataPackConfig = levelStorageAccess.getDataPacks();
                    return dataPackConfig == null ? DataPackConfig.DEFAULT : dataPackConfig;
                }, (arg_0, arg_1) -> Main.lambda$main$1(levelStorageAccess, optionSet, (OptionSpec)optionSpecBuilder3, dedicatedServerSettings, (OptionSpec)optionSpecBuilder4, arg_0, arg_1), Util.backgroundExecutor(), Runnable::run).get();
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", (Throwable)exception);
                packRepository.close();
                return;
            }
            worldStem.updateGlobals();
            object = worldStem.registryAccess();
            dedicatedServerSettings.getProperties().getWorldGenSettings((RegistryAccess)object);
            WorldData worldData = worldStem.worldData();
            if (optionSet.has((OptionSpec)optionSpecBuilder5)) {
                Main.forceUpgrade(levelStorageAccess, DataFixers.getDataFixer(), optionSet.has((OptionSpec)optionSpecBuilder6), () -> true, worldData.worldGenSettings());
            }
            levelStorageAccess.saveDataTag((RegistryAccess)object, worldData);
            final DedicatedServer dedicatedServer = MinecraftServer.spin(arg_0 -> Main.lambda$main$3(levelStorageAccess, packRepository, worldStem, dedicatedServerSettings, minecraftSessionService, gameProfileRepository, gameProfileCache, optionSet, (OptionSpec)argumentAcceptingOptionSpec, (OptionSpec)argumentAcceptingOptionSpec4, (OptionSpec)optionSpecBuilder3, (OptionSpec)argumentAcceptingOptionSpec5, (OptionSpec)optionSpecBuilder, (OptionSpec)nonOptionArgumentSpec, arg_0));
            Thread thread = new Thread("Server Shutdown Thread"){

                @Override
                public void run() {
                    dedicatedServer.halt(true);
                }
            };
            thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(thread);
        }
        catch (Exception exception) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", (Throwable)exception);
        }
    }

    private static void forceUpgrade(LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, boolean bl, BooleanSupplier booleanSupplier, WorldGenSettings worldGenSettings) {
        LOGGER.info("Forcing world upgrade!");
        WorldUpgrader worldUpgrader = new WorldUpgrader(levelStorageAccess, dataFixer, worldGenSettings, bl);
        Component component = null;
        while (!worldUpgrader.isFinished()) {
            int n;
            Component component2 = worldUpgrader.getStatus();
            if (component != component2) {
                component = component2;
                LOGGER.info(worldUpgrader.getStatus().getString());
            }
            if ((n = worldUpgrader.getTotalChunks()) > 0) {
                int n2 = worldUpgrader.getConverted() + worldUpgrader.getSkipped();
                LOGGER.info("{}% completed ({} / {} chunks)...", new Object[]{Mth.floor((float)n2 / (float)n * 100.0f), n2, n});
            }
            if (!booleanSupplier.getAsBoolean()) {
                worldUpgrader.cancel();
                continue;
            }
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException interruptedException) {}
        }
    }

    private static /* synthetic */ DedicatedServer lambda$main$3(LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, DedicatedServerSettings dedicatedServerSettings, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, GameProfileCache gameProfileCache, OptionSet optionSet, OptionSpec optionSpec, OptionSpec optionSpec2, OptionSpec optionSpec3, OptionSpec optionSpec4, OptionSpec optionSpec5, OptionSpec optionSpec6, Thread thread) {
        boolean bl;
        DedicatedServer dedicatedServer = new DedicatedServer(thread, levelStorageAccess, packRepository, worldStem, dedicatedServerSettings, DataFixers.getDataFixer(), minecraftSessionService, gameProfileRepository, gameProfileCache, LoggerChunkProgressListener::new);
        dedicatedServer.setSingleplayerName((String)optionSet.valueOf(optionSpec));
        dedicatedServer.setPort((Integer)optionSet.valueOf(optionSpec2));
        dedicatedServer.setDemo(optionSet.has(optionSpec3));
        dedicatedServer.setId((String)optionSet.valueOf(optionSpec4));
        boolean bl2 = bl = !optionSet.has(optionSpec5) && !optionSet.valuesOf(optionSpec6).contains("nogui");
        if (bl && !GraphicsEnvironment.isHeadless()) {
            dedicatedServer.showGui();
        }
        return dedicatedServer;
    }

    private static /* synthetic */ Pair lambda$main$1(LevelStorageSource.LevelStorageAccess levelStorageAccess, OptionSet optionSet, OptionSpec optionSpec, DedicatedServerSettings dedicatedServerSettings, OptionSpec optionSpec2, ResourceManager resourceManager, DataPackConfig dataPackConfig) {
        Object object;
        WorldGenSettings worldGenSettings;
        LevelSettings levelSettings;
        RegistryAccess.Writable writable = RegistryAccess.builtinCopy();
        RegistryOps<Tag> registryOps = RegistryOps.createAndLoad(NbtOps.INSTANCE, writable, resourceManager);
        WorldData worldData = levelStorageAccess.getDataTag(registryOps, dataPackConfig, writable.allElementsLifecycle());
        if (worldData != null) {
            return Pair.of((Object)worldData, (Object)writable.freeze());
        }
        if (optionSet.has(optionSpec)) {
            levelSettings = MinecraftServer.DEMO_SETTINGS;
            worldGenSettings = WorldGenSettings.demoSettings(writable);
        } else {
            object = dedicatedServerSettings.getProperties();
            levelSettings = new LevelSettings(((DedicatedServerProperties)object).levelName, ((DedicatedServerProperties)object).gamemode, ((DedicatedServerProperties)object).hardcore, ((DedicatedServerProperties)object).difficulty, false, new GameRules(), dataPackConfig);
            worldGenSettings = optionSet.has(optionSpec2) ? ((DedicatedServerProperties)object).getWorldGenSettings(writable).withBonusChest() : ((DedicatedServerProperties)object).getWorldGenSettings(writable);
        }
        object = new PrimaryLevelData(levelSettings, worldGenSettings, Lifecycle.stable());
        return Pair.of((Object)object, (Object)writable.freeze());
    }
}

