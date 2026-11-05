/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Keyable
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.BastionFeature;
import net.minecraft.world.level.levelgen.feature.BuriedTreasureFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.DesertPyramidFeature;
import net.minecraft.world.level.levelgen.feature.EndCityFeature;
import net.minecraft.world.level.levelgen.feature.IglooFeature;
import net.minecraft.world.level.levelgen.feature.JunglePyramidFeature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.NetherFortressFeature;
import net.minecraft.world.level.levelgen.feature.OceanMonumentFeature;
import net.minecraft.world.level.levelgen.feature.PillagerOutpostFeature;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;
import net.minecraft.world.level.levelgen.feature.ShipwreckFeature;
import net.minecraft.world.level.levelgen.feature.StrongholdFeature;
import net.minecraft.world.level.levelgen.feature.SwamplandHutFeature;
import net.minecraft.world.level.levelgen.feature.VillageFeature;
import net.minecraft.world.level.levelgen.feature.WoodlandMansionFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.slf4j.Logger;

public class StructureFeature<C extends FeatureConfiguration> {
    private static final Map<StructureFeature<?>, GenerationStep.Decoration> STEP = Maps.newHashMap();
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final StructureFeature<JigsawConfiguration> PILLAGER_OUTPOST = StructureFeature.register("pillager_outpost", new PillagerOutpostFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<MineshaftConfiguration> MINESHAFT = StructureFeature.register("mineshaft", new MineshaftFeature(MineshaftConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = StructureFeature.register("mansion", new WoodlandMansionFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = StructureFeature.register("jungle_pyramid", new JunglePyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = StructureFeature.register("desert_pyramid", new DesertPyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> IGLOO = StructureFeature.register("igloo", new IglooFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<RuinedPortalConfiguration> RUINED_PORTAL = StructureFeature.register("ruined_portal", new RuinedPortalFeature(RuinedPortalConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = StructureFeature.register("shipwreck", new ShipwreckFeature(ShipwreckConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> SWAMP_HUT = StructureFeature.register("swamp_hut", new SwamplandHutFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = StructureFeature.register("stronghold", new StrongholdFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.STRONGHOLDS);
    public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = StructureFeature.register("monument", new OceanMonumentFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = StructureFeature.register("ocean_ruin", new OceanRuinFeature(OceanRuinConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> FORTRESS = StructureFeature.register("fortress", new NetherFortressFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION);
    public static final StructureFeature<NoneFeatureConfiguration> END_CITY = StructureFeature.register("endcity", new EndCityFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<ProbabilityFeatureConfiguration> BURIED_TREASURE = StructureFeature.register("buried_treasure", new BuriedTreasureFeature(ProbabilityFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
    public static final StructureFeature<JigsawConfiguration> VILLAGE = StructureFeature.register("village", new VillageFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<RangeConfiguration> NETHER_FOSSIL = StructureFeature.register("nether_fossil", new NetherFossilFeature(RangeConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION);
    public static final StructureFeature<JigsawConfiguration> BASTION_REMNANT = StructureFeature.register("bastion_remnant", new BastionFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final int MAX_STRUCTURE_RANGE = 8;
    private final Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec = RecordCodecBuilder.create(instance -> instance.group((App)codec.fieldOf("config").forGetter(configuredStructureFeature -> configuredStructureFeature.config), (App)RegistryCodecs.homogeneousList(Registry.BIOME_REGISTRY).fieldOf("biomes").forGetter(ConfiguredStructureFeature::biomes), (App)Codec.BOOL.optionalFieldOf("adapt_noise", (Object)false).forGetter(configuredStructureFeature -> configuredStructureFeature.adaptNoise), (App)Codec.simpleMap(MobCategory.CODEC, StructureSpawnOverride.CODEC, (Keyable)StringRepresentable.keys(MobCategory.values())).fieldOf("spawn_overrides").forGetter(configuredStructureFeature -> configuredStructureFeature.spawnOverrides)).apply((Applicative)instance, (featureConfiguration, holderSet, bl, map) -> new ConfiguredStructureFeature<FeatureConfiguration, StructureFeature>(this, (FeatureConfiguration)featureConfiguration, (HolderSet<Biome>)holderSet, (boolean)bl, (Map<MobCategory, StructureSpawnOverride>)map)));
    private final PieceGeneratorSupplier<C> pieceGenerator;
    private final PostPlacementProcessor postPlacementProcessor;

    private static <F extends StructureFeature<?>> F register(String string, F f, GenerationStep.Decoration decoration) {
        STEP.put(f, decoration);
        return (F)Registry.register(Registry.STRUCTURE_FEATURE, string, f);
    }

    public StructureFeature(Codec<C> codec, PieceGeneratorSupplier<C> pieceGeneratorSupplier) {
        this(codec, pieceGeneratorSupplier, PostPlacementProcessor.NONE);
    }

    public StructureFeature(Codec<C> codec, PieceGeneratorSupplier<C> pieceGeneratorSupplier, PostPlacementProcessor postPlacementProcessor) {
        this.pieceGenerator = pieceGeneratorSupplier;
        this.postPlacementProcessor = postPlacementProcessor;
    }

    public GenerationStep.Decoration step() {
        return STEP.get(this);
    }

    public static void bootstrap() {
    }

    @Nullable
    public static StructureStart loadStaticStart(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l) {
        String string = compoundTag.getString("id");
        if ("INVALID".equals(string)) {
            return StructureStart.INVALID_START;
        }
        Registry<ConfiguredStructureFeature<?, ?>> registry = structurePieceSerializationContext.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        ConfiguredStructureFeature<?, ?> configuredStructureFeature = registry.get(new ResourceLocation(string));
        if (configuredStructureFeature == null) {
            LOGGER.error("Unknown feature id: {}", (Object)string);
            return null;
        }
        ChunkPos chunkPos = new ChunkPos(compoundTag.getInt("ChunkX"), compoundTag.getInt("ChunkZ"));
        int n = compoundTag.getInt("references");
        ListTag listTag = compoundTag.getList("Children", 10);
        try {
            PiecesContainer piecesContainer = PiecesContainer.load(listTag, structurePieceSerializationContext);
            if (configuredStructureFeature.feature == OCEAN_MONUMENT) {
                piecesContainer = OceanMonumentFeature.regeneratePiecesAfterLoad(chunkPos, l, piecesContainer);
            }
            return new StructureStart(configuredStructureFeature, chunkPos, n, piecesContainer);
        }
        catch (Exception exception) {
            LOGGER.error("Failed Start with id {}", (Object)string, (Object)exception);
            return null;
        }
    }

    public Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec() {
        return this.configuredStructureCodec;
    }

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(C c, TagKey<Biome> tagKey) {
        return this.configured(c, tagKey, false);
    }

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(C c, TagKey<Biome> tagKey, boolean bl) {
        return new ConfiguredStructureFeature<C, StructureFeature>(this, c, BuiltinRegistries.BIOME.getOrCreateTag(tagKey), bl, Map.of());
    }

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(C c, TagKey<Biome> tagKey, Map<MobCategory, StructureSpawnOverride> map) {
        return new ConfiguredStructureFeature<C, StructureFeature>(this, c, BuiltinRegistries.BIOME.getOrCreateTag(tagKey), false, map);
    }

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(C c, TagKey<Biome> tagKey, boolean bl, Map<MobCategory, StructureSpawnOverride> map) {
        return new ConfiguredStructureFeature<C, StructureFeature>(this, c, BuiltinRegistries.BIOME.getOrCreateTag(tagKey), bl, map);
    }

    public static BlockPos getLocatePos(RandomSpreadStructurePlacement randomSpreadStructurePlacement, ChunkPos chunkPos) {
        return new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ()).offset(randomSpreadStructurePlacement.locateOffset());
    }

    public boolean canGenerate(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, StructureManager structureManager, long l, ChunkPos chunkPos, C c, LevelHeightAccessor levelHeightAccessor, Predicate<Holder<Biome>> predicate) {
        return this.pieceGenerator.createGenerator(new PieceGeneratorSupplier.Context<C>(chunkGenerator, biomeSource, l, chunkPos, c, levelHeightAccessor, predicate, structureManager, registryAccess)).isPresent();
    }

    public PieceGeneratorSupplier<C> pieceGeneratorSupplier() {
        return this.pieceGenerator;
    }

    public PostPlacementProcessor getPostPlacementProcessor() {
        return this.postPlacementProcessor;
    }
}

