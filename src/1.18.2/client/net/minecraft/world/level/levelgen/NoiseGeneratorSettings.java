/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseRouterWithOnlyNoises;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public record NoiseGeneratorSettings(NoiseSettings noiseSettings, BlockState defaultBlock, BlockState defaultFluid, NoiseRouterWithOnlyNoises noiseRouter, SurfaceRules.RuleSource surfaceRule, int seaLevel, boolean disableMobGeneration, boolean aquifersEnabled, boolean oreVeinsEnabled, boolean useLegacyRandomSource) {
    private final boolean oreVeinsEnabled;
    public static final Codec<NoiseGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)NoiseSettings.CODEC.fieldOf("noise").forGetter(NoiseGeneratorSettings::noiseSettings), (App)BlockState.CODEC.fieldOf("default_block").forGetter(NoiseGeneratorSettings::defaultBlock), (App)BlockState.CODEC.fieldOf("default_fluid").forGetter(NoiseGeneratorSettings::defaultFluid), (App)NoiseRouterWithOnlyNoises.CODEC.fieldOf("noise_router").forGetter(NoiseGeneratorSettings::noiseRouter), (App)SurfaceRules.RuleSource.CODEC.fieldOf("surface_rule").forGetter(NoiseGeneratorSettings::surfaceRule), (App)Codec.INT.fieldOf("sea_level").forGetter(NoiseGeneratorSettings::seaLevel), (App)Codec.BOOL.fieldOf("disable_mob_generation").forGetter(NoiseGeneratorSettings::disableMobGeneration), (App)Codec.BOOL.fieldOf("aquifers_enabled").forGetter(NoiseGeneratorSettings::isAquifersEnabled), (App)Codec.BOOL.fieldOf("ore_veins_enabled").forGetter(NoiseGeneratorSettings::oreVeinsEnabled), (App)Codec.BOOL.fieldOf("legacy_random_source").forGetter(NoiseGeneratorSettings::useLegacyRandomSource)).apply((Applicative)instance, NoiseGeneratorSettings::new));
    public static final Codec<Holder<NoiseGeneratorSettings>> CODEC = RegistryFileCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DIRECT_CODEC);
    public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<NoiseGeneratorSettings> LARGE_BIOMES = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("large_biomes"));
    public static final ResourceKey<NoiseGeneratorSettings> AMPLIFIED = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("amplified"));
    public static final ResourceKey<NoiseGeneratorSettings> NETHER = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("nether"));
    public static final ResourceKey<NoiseGeneratorSettings> END = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("end"));
    public static final ResourceKey<NoiseGeneratorSettings> CAVES = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("caves"));
    public static final ResourceKey<NoiseGeneratorSettings> FLOATING_ISLANDS = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("floating_islands"));

    public boolean isAquifersEnabled() {
        return this.aquifersEnabled;
    }

    public boolean oreVeinsEnabled() {
        return this.oreVeinsEnabled;
    }

    public WorldgenRandom.Algorithm getRandomSource() {
        return this.useLegacyRandomSource ? WorldgenRandom.Algorithm.LEGACY : WorldgenRandom.Algorithm.XOROSHIRO;
    }

    public NoiseRouter createNoiseRouter(Registry<NormalNoise.NoiseParameters> registry, long l) {
        return NoiseRouterData.createNoiseRouter(this.noiseSettings, l, registry, this.getRandomSource(), this.noiseRouter);
    }

    private static void register(ResourceKey<NoiseGeneratorSettings> resourceKey, NoiseGeneratorSettings noiseGeneratorSettings) {
        BuiltinRegistries.register(BuiltinRegistries.NOISE_GENERATOR_SETTINGS, resourceKey.location(), noiseGeneratorSettings);
    }

    public static Holder<NoiseGeneratorSettings> bootstrap() {
        return (Holder)BuiltinRegistries.NOISE_GENERATOR_SETTINGS.holders().iterator().next();
    }

    private static NoiseGeneratorSettings end() {
        return new NoiseGeneratorSettings(NoiseSettings.END_NOISE_SETTINGS, Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), NoiseRouterData.end(NoiseSettings.END_NOISE_SETTINGS), SurfaceRuleData.end(), 0, true, false, false, true);
    }

    private static NoiseGeneratorSettings nether() {
        return new NoiseGeneratorSettings(NoiseSettings.NETHER_NOISE_SETTINGS, Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState(), NoiseRouterData.nether(NoiseSettings.NETHER_NOISE_SETTINGS), SurfaceRuleData.nether(), 32, false, false, false, true);
    }

    private static NoiseGeneratorSettings overworld(boolean bl, boolean bl2) {
        NoiseSettings noiseSettings = NoiseSettings.overworldNoiseSettings(bl);
        return new NoiseGeneratorSettings(noiseSettings, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), NoiseRouterData.overworld(noiseSettings, bl2), SurfaceRuleData.overworld(), 63, false, true, true, false);
    }

    private static NoiseGeneratorSettings caves() {
        return new NoiseGeneratorSettings(NoiseSettings.CAVES_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), NoiseRouterData.overworldWithoutCaves(NoiseSettings.CAVES_NOISE_SETTINGS), SurfaceRuleData.overworldLike(false, true, true), 32, false, false, false, true);
    }

    private static NoiseGeneratorSettings floatingIslands() {
        return new NoiseGeneratorSettings(NoiseSettings.FLOATING_ISLANDS_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), NoiseRouterData.overworldWithoutCaves(NoiseSettings.FLOATING_ISLANDS_NOISE_SETTINGS), SurfaceRuleData.overworldLike(false, false, false), -64, false, false, false, true);
    }

    static {
        NoiseGeneratorSettings.register(OVERWORLD, NoiseGeneratorSettings.overworld(false, false));
        NoiseGeneratorSettings.register(LARGE_BIOMES, NoiseGeneratorSettings.overworld(false, true));
        NoiseGeneratorSettings.register(AMPLIFIED, NoiseGeneratorSettings.overworld(true, false));
        NoiseGeneratorSettings.register(NETHER, NoiseGeneratorSettings.nether());
        NoiseGeneratorSettings.register(END, NoiseGeneratorSettings.end());
        NoiseGeneratorSettings.register(CAVES, NoiseGeneratorSettings.caves());
        NoiseGeneratorSettings.register(FLOATING_ISLANDS, NoiseGeneratorSettings.floatingIslands());
    }
}

