/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure.pieces;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

@FunctionalInterface
public interface PieceGeneratorSupplier<C extends FeatureConfiguration> {
    public Optional<PieceGenerator<C>> createGenerator(Context<C> var1);

    public static <C extends FeatureConfiguration> PieceGeneratorSupplier<C> simple(Predicate<Context<C>> predicate, PieceGenerator<C> pieceGenerator) {
        Optional optional = Optional.of(pieceGenerator);
        return context -> predicate.test(context) ? optional : Optional.empty();
    }

    public static <C extends FeatureConfiguration> Predicate<Context<C>> checkForBiomeOnTop(Heightmap.Types types) {
        return context -> context.validBiomeOnTop(types);
    }

    public record Context<C extends FeatureConfiguration>(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long seed, ChunkPos chunkPos, C config, LevelHeightAccessor heightAccessor, Predicate<Holder<Biome>> validBiome, StructureManager structureManager, RegistryAccess registryAccess) {
        public boolean validBiomeOnTop(Heightmap.Types types) {
            int n = this.chunkPos.getMiddleBlockX();
            int n2 = this.chunkPos.getMiddleBlockZ();
            int n3 = this.chunkGenerator.getFirstOccupiedHeight(n, n2, types, this.heightAccessor);
            Holder<Biome> holder = this.chunkGenerator.getNoiseBiome(QuartPos.fromBlock(n), QuartPos.fromBlock(n3), QuartPos.fromBlock(n2));
            return this.validBiome.test(holder);
        }

        public int[] getCornerHeights(int n, int n2, int n3, int n4) {
            return new int[]{this.chunkGenerator.getFirstOccupiedHeight(n, n3, Heightmap.Types.WORLD_SURFACE_WG, this.heightAccessor), this.chunkGenerator.getFirstOccupiedHeight(n, n3 + n4, Heightmap.Types.WORLD_SURFACE_WG, this.heightAccessor), this.chunkGenerator.getFirstOccupiedHeight(n + n2, n3, Heightmap.Types.WORLD_SURFACE_WG, this.heightAccessor), this.chunkGenerator.getFirstOccupiedHeight(n + n2, n3 + n4, Heightmap.Types.WORLD_SURFACE_WG, this.heightAccessor)};
        }

        public int getLowestY(int n, int n2) {
            int n3 = this.chunkPos.getMinBlockX();
            int n4 = this.chunkPos.getMinBlockZ();
            int[] nArray = this.getCornerHeights(n3, n, n4, n2);
            return Math.min(Math.min(nArray[0], nArray[1]), Math.min(nArray[2], nArray[3]));
        }
    }
}

