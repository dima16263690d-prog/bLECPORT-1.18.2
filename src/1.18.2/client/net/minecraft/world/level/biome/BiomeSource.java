/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Graph;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;

public abstract class BiomeSource
implements BiomeResolver {
    public static final Codec<BiomeSource> CODEC;
    private final Set<Holder<Biome>> possibleBiomes;
    private final Supplier<List<StepFeatureData>> featuresPerStep;

    protected BiomeSource(Stream<Holder<Biome>> stream) {
        this(stream.distinct().toList());
    }

    protected BiomeSource(List<Holder<Biome>> list) {
        this.possibleBiomes = new ObjectLinkedOpenHashSet(list);
        this.featuresPerStep = Suppliers.memoize(() -> this.buildFeaturesPerStep(list, true));
    }

    private List<StepFeatureData> buildFeaturesPerStep(List<Holder<Biome>> list, boolean bl) {
        List<PlacedFeature> list2;
        Object object2;
        int arrayList;
        Object object3;
        Object object4;
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        MutableInt mutableInt = new MutableInt(0);
        record FeatureData(int featureIndex, int step, PlacedFeature feature) {
        }
        Comparator<FeatureData> comparator = Comparator.comparingInt(FeatureData::step).thenComparingInt(FeatureData::featureIndex);
        TreeMap<FeatureData, Set> treeMap = new TreeMap<FeatureData, Set>(comparator);
        int n2 = 0;
        for (Holder<Biome> object52 : list) {
            object4 = object52.value();
            object3 = Lists.newArrayList();
            Object i = ((Biome)object4).getGenerationSettings().features();
            n2 = Math.max(n2, i.size());
            for (arrayList = 0; arrayList < i.size(); ++arrayList) {
                for (Object n4 : (HolderSet)i.get(arrayList)) {
                    object2 = (PlacedFeature)((Object)n4.value());
                    object3.add(new FeatureData(object2IntOpenHashMap.computeIfAbsent(object2, object -> mutableInt.getAndIncrement()), arrayList, (PlacedFeature)((Object)object2)));
                }
            }
            for (arrayList = 0; arrayList < object3.size(); ++arrayList) {
                list2 = treeMap.computeIfAbsent((FeatureData)((Object)object3.get(arrayList)), featureData -> new TreeSet(comparator));
                if (arrayList >= object3.size() - 1) continue;
                list2.add((PlacedFeature)((Object)((FeatureData)((Object)object3.get(arrayList + 1)))));
            }
        }
        TreeSet<FeatureData> treeSet = new TreeSet<FeatureData>(comparator);
        TreeSet<FeatureData> treeSet2 = new TreeSet<FeatureData>(comparator);
        object4 = Lists.newArrayList();
        for (Object i : treeMap.keySet()) {
            if (!treeSet2.isEmpty()) {
                throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
            }
            if (treeSet.contains(i) || !Graph.depthFirstSearch(treeMap, treeSet, treeSet2, ((List)object4)::add, i)) continue;
            if (bl) {
                int n;
                ArrayList<Holder<Biome>> arrayList2 = new ArrayList<Holder<Biome>>(list);
                do {
                    Object n4;
                    n = arrayList2.size();
                    n4 = arrayList2.listIterator();
                    while (n4.hasNext()) {
                        object2 = (Holder)n4.next();
                        n4.remove();
                        try {
                            this.buildFeaturesPerStep(arrayList2, false);
                        }
                        catch (IllegalStateException j) {
                            continue;
                        }
                        n4.add(object2);
                    }
                } while (n != arrayList2.size());
                throw new IllegalStateException("Feature order cycle found, involved biomes: " + arrayList2);
            }
            throw new IllegalStateException("Feature order cycle found");
        }
        Collections.reverse(object4);
        object3 = ImmutableList.builder();
        for (int i = 0; i < n2; ++i) {
            arrayList = i;
            list2 = object4.stream().filter(featureData -> featureData.step() == n).map(FeatureData::feature).collect(Collectors.toList());
            int n = list2.size();
            object2 = new Object2IntOpenCustomHashMap(n, Util.identityStrategy());
            for (int j = 0; j < n; ++j) {
                object2.put((Object)((PlacedFeature)((Object)list2.get(j))), j);
            }
            object3.add((Object)new StepFeatureData(list2, (ToIntFunction<PlacedFeature>)object2));
        }
        return object3.build();
    }

    protected abstract Codec<? extends BiomeSource> codec();

    public abstract BiomeSource withSeed(long var1);

    public Set<Holder<Biome>> possibleBiomes() {
        return this.possibleBiomes;
    }

    public Set<Holder<Biome>> getBiomesWithin(int n, int n2, int n3, int n4, Climate.Sampler sampler) {
        int n5 = QuartPos.fromBlock(n - n4);
        int n6 = QuartPos.fromBlock(n2 - n4);
        int n7 = QuartPos.fromBlock(n3 - n4);
        int n8 = QuartPos.fromBlock(n + n4);
        int n9 = QuartPos.fromBlock(n2 + n4);
        int n10 = QuartPos.fromBlock(n3 + n4);
        int n11 = n8 - n5 + 1;
        int n12 = n9 - n6 + 1;
        int n13 = n10 - n7 + 1;
        HashSet hashSet = Sets.newHashSet();
        for (int i = 0; i < n13; ++i) {
            for (int j = 0; j < n11; ++j) {
                for (int k = 0; k < n12; ++k) {
                    int n14 = n5 + j;
                    int n15 = n6 + k;
                    int n16 = n7 + i;
                    hashSet.add(this.getNoiseBiome(n14, n15, n16, sampler));
                }
            }
        }
        return hashSet;
    }

    @Nullable
    public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int n, int n2, int n3, int n4, Predicate<Holder<Biome>> predicate, Random random, Climate.Sampler sampler) {
        return this.findBiomeHorizontal(n, n2, n3, n4, 1, predicate, random, false, sampler);
    }

    @Nullable
    public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int n, int n2, int n3, int n4, int n5, Predicate<Holder<Biome>> predicate, Random random, boolean bl, Climate.Sampler sampler) {
        int n6;
        int n7 = QuartPos.fromBlock(n);
        int n8 = QuartPos.fromBlock(n3);
        int n9 = QuartPos.fromBlock(n4);
        int n10 = QuartPos.fromBlock(n2);
        Pair pair = null;
        int n11 = 0;
        for (int i = n6 = bl ? 0 : n9; i <= n9; i += n5) {
            int n12;
            int n13 = n12 = SharedConstants.debugGenerateSquareTerrainWithoutNoise ? 0 : -i;
            while (n12 <= i) {
                boolean bl2 = Math.abs(n12) == i;
                for (int j = -i; j <= i; j += n5) {
                    int n14;
                    Holder<Biome> holder;
                    int n15;
                    if (bl) {
                        int n16 = n15 = Math.abs(j) == i ? 1 : 0;
                        if (n15 == 0 && !bl2) continue;
                    }
                    if (!predicate.test(holder = this.getNoiseBiome(n15 = n7 + j, n10, n14 = n8 + n12, sampler))) continue;
                    if (pair == null || random.nextInt(n11 + 1) == 0) {
                        BlockPos blockPos = new BlockPos(QuartPos.toBlock(n15), n2, QuartPos.toBlock(n14));
                        if (bl) {
                            return Pair.of((Object)blockPos, holder);
                        }
                        pair = Pair.of((Object)blockPos, holder);
                    }
                    ++n11;
                }
                n12 += n5;
            }
        }
        return pair;
    }

    @Override
    public abstract Holder<Biome> getNoiseBiome(int var1, int var2, int var3, Climate.Sampler var4);

    public void addDebugInfo(List<String> list, BlockPos blockPos, Climate.Sampler sampler) {
    }

    public List<StepFeatureData> featuresPerStep() {
        return this.featuresPerStep.get();
    }

    static {
        Registry.register(Registry.BIOME_SOURCE, "fixed", FixedBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "multi_noise", MultiNoiseBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "the_end", TheEndBiomeSource.CODEC);
        CODEC = Registry.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
    }

    public record StepFeatureData(List<PlacedFeature> features, ToIntFunction<PlacedFeature> indexMapping) {
    }
}

