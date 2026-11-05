/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.dimension;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class DimensionType {
    public static final int BITS_FOR_Y = BlockPos.PACKED_Y_LENGTH;
    public static final int MIN_HEIGHT = 16;
    public static final int Y_SIZE = (1 << BITS_FOR_Y) - 32;
    public static final int MAX_Y = (Y_SIZE >> 1) - 1;
    public static final int MIN_Y = MAX_Y - Y_SIZE + 1;
    public static final int WAY_ABOVE_MAX_Y = MAX_Y << 4;
    public static final int WAY_BELOW_MIN_Y = MIN_Y << 4;
    public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("overworld");
    public static final ResourceLocation NETHER_EFFECTS = new ResourceLocation("the_nether");
    public static final ResourceLocation END_EFFECTS = new ResourceLocation("the_end");
    public static final Codec<DimensionType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.LONG.optionalFieldOf("fixed_time").xmap(optional -> optional.map(OptionalLong::of).orElseGet(OptionalLong::empty), optionalLong -> optionalLong.isPresent() ? Optional.of(optionalLong.getAsLong()) : Optional.empty()).forGetter(dimensionType -> dimensionType.fixedTime), (App)Codec.BOOL.fieldOf("has_skylight").forGetter(DimensionType::hasSkyLight), (App)Codec.BOOL.fieldOf("has_ceiling").forGetter(DimensionType::hasCeiling), (App)Codec.BOOL.fieldOf("ultrawarm").forGetter(DimensionType::ultraWarm), (App)Codec.BOOL.fieldOf("natural").forGetter(DimensionType::natural), (App)Codec.doubleRange((double)1.0E-5f, (double)3.0E7).fieldOf("coordinate_scale").forGetter(DimensionType::coordinateScale), (App)Codec.BOOL.fieldOf("piglin_safe").forGetter(DimensionType::piglinSafe), (App)Codec.BOOL.fieldOf("bed_works").forGetter(DimensionType::bedWorks), (App)Codec.BOOL.fieldOf("respawn_anchor_works").forGetter(DimensionType::respawnAnchorWorks), (App)Codec.BOOL.fieldOf("has_raids").forGetter(DimensionType::hasRaids), (App)Codec.intRange((int)MIN_Y, (int)MAX_Y).fieldOf("min_y").forGetter(DimensionType::minY), (App)Codec.intRange((int)16, (int)Y_SIZE).fieldOf("height").forGetter(DimensionType::height), (App)Codec.intRange((int)0, (int)Y_SIZE).fieldOf("logical_height").forGetter(DimensionType::logicalHeight), (App)TagKey.hashedCodec(Registry.BLOCK_REGISTRY).fieldOf("infiniburn").forGetter(dimensionType -> dimensionType.infiniburn), (App)ResourceLocation.CODEC.fieldOf("effects").orElse((Object)OVERWORLD_EFFECTS).forGetter(dimensionType -> dimensionType.effectsLocation), (App)Codec.FLOAT.fieldOf("ambient_light").forGetter(dimensionType -> Float.valueOf(dimensionType.ambientLight))).apply((Applicative)instance, DimensionType::new)).comapFlatMap(DimensionType::guardY, Function.identity());
    private static final int MOON_PHASES = 8;
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0f, 0.75f, 0.5f, 0.25f, 0.0f, 0.25f, 0.5f, 0.75f};
    public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_end"));
    protected static final DimensionType DEFAULT_OVERWORLD = DimensionType.create(OptionalLong.empty(), true, false, false, true, 1.0, false, false, true, false, true, -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD, OVERWORLD_EFFECTS, 0.0f);
    protected static final DimensionType DEFAULT_NETHER = DimensionType.create(OptionalLong.of(18000L), false, true, true, false, 8.0, false, true, false, true, false, 0, 256, 128, BlockTags.INFINIBURN_NETHER, NETHER_EFFECTS, 0.1f);
    protected static final DimensionType DEFAULT_END = DimensionType.create(OptionalLong.of(6000L), false, false, false, false, 1.0, true, false, false, false, true, 0, 256, 256, BlockTags.INFINIBURN_END, END_EFFECTS, 0.0f);
    public static final ResourceKey<DimensionType> OVERWORLD_CAVES_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld_caves"));
    protected static final DimensionType DEFAULT_OVERWORLD_CAVES = DimensionType.create(OptionalLong.empty(), true, true, false, true, 1.0, false, false, true, false, true, -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD, OVERWORLD_EFFECTS, 0.0f);
    public static final Codec<Holder<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, DIRECT_CODEC);
    private final OptionalLong fixedTime;
    private final boolean hasSkylight;
    private final boolean hasCeiling;
    private final boolean ultraWarm;
    private final boolean natural;
    private final double coordinateScale;
    private final boolean createDragonFight;
    private final boolean piglinSafe;
    private final boolean bedWorks;
    private final boolean respawnAnchorWorks;
    private final boolean hasRaids;
    private final int minY;
    private final int height;
    private final int logicalHeight;
    private final TagKey<Block> infiniburn;
    private final ResourceLocation effectsLocation;
    private final float ambientLight;
    private final transient float[] brightnessRamp;

    private static DataResult<DimensionType> guardY(DimensionType dimensionType) {
        if (dimensionType.height() < 16) {
            return DataResult.error((String)"height has to be at least 16");
        }
        if (dimensionType.minY() + dimensionType.height() > MAX_Y + 1) {
            return DataResult.error((String)("min_y + height cannot be higher than: " + (MAX_Y + 1)));
        }
        if (dimensionType.logicalHeight() > dimensionType.height()) {
            return DataResult.error((String)"logical_height cannot be higher than height");
        }
        if (dimensionType.height() % 16 != 0) {
            return DataResult.error((String)"height has to be multiple of 16");
        }
        if (dimensionType.minY() % 16 != 0) {
            return DataResult.error((String)"min_y has to be a multiple of 16");
        }
        return DataResult.success((Object)dimensionType);
    }

    private DimensionType(OptionalLong optionalLong, boolean bl, boolean bl2, boolean bl3, boolean bl4, double d, boolean bl5, boolean bl6, boolean bl7, boolean bl8, int n, int n2, int n3, TagKey<Block> tagKey, ResourceLocation resourceLocation, float f) {
        this(optionalLong, bl, bl2, bl3, bl4, d, false, bl5, bl6, bl7, bl8, n, n2, n3, tagKey, resourceLocation, f);
    }

    public static DimensionType create(OptionalLong optionalLong, boolean bl, boolean bl2, boolean bl3, boolean bl4, double d, boolean bl5, boolean bl6, boolean bl7, boolean bl8, boolean bl9, int n, int n2, int n3, TagKey<Block> tagKey, ResourceLocation resourceLocation, float f) {
        DimensionType dimensionType = new DimensionType(optionalLong, bl, bl2, bl3, bl4, d, bl5, bl6, bl7, bl8, bl9, n, n2, n3, tagKey, resourceLocation, f);
        DimensionType.guardY(dimensionType).error().ifPresent(partialResult -> {
            throw new IllegalStateException(partialResult.message());
        });
        return dimensionType;
    }

    @Deprecated
    private DimensionType(OptionalLong optionalLong, boolean bl, boolean bl2, boolean bl3, boolean bl4, double d, boolean bl5, boolean bl6, boolean bl7, boolean bl8, boolean bl9, int n, int n2, int n3, TagKey<Block> tagKey, ResourceLocation resourceLocation, float f) {
        this.fixedTime = optionalLong;
        this.hasSkylight = bl;
        this.hasCeiling = bl2;
        this.ultraWarm = bl3;
        this.natural = bl4;
        this.coordinateScale = d;
        this.createDragonFight = bl5;
        this.piglinSafe = bl6;
        this.bedWorks = bl7;
        this.respawnAnchorWorks = bl8;
        this.hasRaids = bl9;
        this.minY = n;
        this.height = n2;
        this.logicalHeight = n3;
        this.infiniburn = tagKey;
        this.effectsLocation = resourceLocation;
        this.ambientLight = f;
        this.brightnessRamp = DimensionType.fillBrightnessRamp(f);
    }

    private static float[] fillBrightnessRamp(float f) {
        float[] fArray = new float[16];
        for (int i = 0; i <= 15; ++i) {
            float f2 = (float)i / 15.0f;
            float f3 = f2 / (4.0f - 3.0f * f2);
            fArray[i] = Mth.lerp(f, f3, 1.0f);
        }
        return fArray;
    }

    @Deprecated
    public static DataResult<ResourceKey<Level>> parseLegacy(Dynamic<?> dynamic) {
        Optional optional = dynamic.asNumber().result();
        if (optional.isPresent()) {
            int n = ((Number)optional.get()).intValue();
            if (n == -1) {
                return DataResult.success(Level.NETHER);
            }
            if (n == 0) {
                return DataResult.success(Level.OVERWORLD);
            }
            if (n == 1) {
                return DataResult.success(Level.END);
            }
        }
        return Level.RESOURCE_KEY_CODEC.parse(dynamic);
    }

    public static RegistryAccess.Writable registerBuiltin(RegistryAccess.Writable writable) {
        WritableRegistry<DimensionType> writableRegistry = writable.ownedWritableRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        writableRegistry.register(OVERWORLD_LOCATION, DEFAULT_OVERWORLD, Lifecycle.stable());
        writableRegistry.register(OVERWORLD_CAVES_LOCATION, DEFAULT_OVERWORLD_CAVES, Lifecycle.stable());
        writableRegistry.register(NETHER_LOCATION, DEFAULT_NETHER, Lifecycle.stable());
        writableRegistry.register(END_LOCATION, DEFAULT_END, Lifecycle.stable());
        return writable;
    }

    public static Registry<LevelStem> defaultDimensions(RegistryAccess registryAccess, long l) {
        return DimensionType.defaultDimensions(registryAccess, l, true);
    }

    public static Registry<LevelStem> defaultDimensions(RegistryAccess registryAccess, long l, boolean bl) {
        MappedRegistry<LevelStem> mappedRegistry = new MappedRegistry<LevelStem>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
        Registry<DimensionType> registry = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> registry2 = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<StructureSet> registry3 = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NoiseGeneratorSettings> registry4 = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<NormalNoise.NoiseParameters> registry5 = registryAccess.registryOrThrow(Registry.NOISE_REGISTRY);
        ((WritableRegistry)mappedRegistry).register(LevelStem.NETHER, new LevelStem(registry.getOrCreateHolder(NETHER_LOCATION), new NoiseBasedChunkGenerator(registry3, registry5, (BiomeSource)MultiNoiseBiomeSource.Preset.NETHER.biomeSource(registry2, bl), l, registry4.getOrCreateHolder(NoiseGeneratorSettings.NETHER))), Lifecycle.stable());
        ((WritableRegistry)mappedRegistry).register(LevelStem.END, new LevelStem(registry.getOrCreateHolder(END_LOCATION), new NoiseBasedChunkGenerator(registry3, registry5, (BiomeSource)new TheEndBiomeSource(registry2, l), l, registry4.getOrCreateHolder(NoiseGeneratorSettings.END))), Lifecycle.stable());
        return mappedRegistry;
    }

    public static double getTeleportationScale(DimensionType dimensionType, DimensionType dimensionType2) {
        double d = dimensionType.coordinateScale();
        double d2 = dimensionType2.coordinateScale();
        return d / d2;
    }

    public static Path getStorageFolder(ResourceKey<Level> resourceKey, Path path) {
        if (resourceKey == Level.OVERWORLD) {
            return path;
        }
        if (resourceKey == Level.END) {
            return path.resolve("DIM1");
        }
        if (resourceKey == Level.NETHER) {
            return path.resolve("DIM-1");
        }
        return path.resolve("dimensions").resolve(resourceKey.location().getNamespace()).resolve(resourceKey.location().getPath());
    }

    public boolean hasSkyLight() {
        return this.hasSkylight;
    }

    public boolean hasCeiling() {
        return this.hasCeiling;
    }

    public boolean ultraWarm() {
        return this.ultraWarm;
    }

    public boolean natural() {
        return this.natural;
    }

    public double coordinateScale() {
        return this.coordinateScale;
    }

    public boolean piglinSafe() {
        return this.piglinSafe;
    }

    public boolean bedWorks() {
        return this.bedWorks;
    }

    public boolean respawnAnchorWorks() {
        return this.respawnAnchorWorks;
    }

    public boolean hasRaids() {
        return this.hasRaids;
    }

    public int minY() {
        return this.minY;
    }

    public int height() {
        return this.height;
    }

    public int logicalHeight() {
        return this.logicalHeight;
    }

    public boolean createDragonFight() {
        return this.createDragonFight;
    }

    public boolean hasFixedTime() {
        return this.fixedTime.isPresent();
    }

    public float timeOfDay(long l) {
        double d = Mth.frac((double)this.fixedTime.orElse(l) / 24000.0 - 0.25);
        double d2 = 0.5 - Math.cos(d * Math.PI) / 2.0;
        return (float)(d * 2.0 + d2) / 3.0f;
    }

    public int moonPhase(long l) {
        return (int)(l / 24000L % 8L + 8L) % 8;
    }

    public float brightness(int n) {
        return this.brightnessRamp[n];
    }

    public TagKey<Block> infiniburn() {
        return this.infiniburn;
    }

    public ResourceLocation effectsLocation() {
        return this.effectsLocation;
    }
}

