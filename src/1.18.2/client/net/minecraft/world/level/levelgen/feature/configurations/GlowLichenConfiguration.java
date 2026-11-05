/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class GlowLichenConfiguration
implements FeatureConfiguration {
    public static final Codec<GlowLichenConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.intRange((int)1, (int)64).fieldOf("search_range").orElse((Object)10).forGetter(glowLichenConfiguration -> glowLichenConfiguration.searchRange), (App)Codec.BOOL.fieldOf("can_place_on_floor").orElse((Object)false).forGetter(glowLichenConfiguration -> glowLichenConfiguration.canPlaceOnFloor), (App)Codec.BOOL.fieldOf("can_place_on_ceiling").orElse((Object)false).forGetter(glowLichenConfiguration -> glowLichenConfiguration.canPlaceOnCeiling), (App)Codec.BOOL.fieldOf("can_place_on_wall").orElse((Object)false).forGetter(glowLichenConfiguration -> glowLichenConfiguration.canPlaceOnWall), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("chance_of_spreading").orElse((Object)Float.valueOf(0.5f)).forGetter(glowLichenConfiguration -> Float.valueOf(glowLichenConfiguration.chanceOfSpreading)), (App)RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("can_be_placed_on").forGetter(glowLichenConfiguration -> glowLichenConfiguration.canBePlacedOn)).apply((Applicative)instance, GlowLichenConfiguration::new));
    public final int searchRange;
    public final boolean canPlaceOnFloor;
    public final boolean canPlaceOnCeiling;
    public final boolean canPlaceOnWall;
    public final float chanceOfSpreading;
    public final HolderSet<Block> canBePlacedOn;
    public final List<Direction> validDirections;

    public GlowLichenConfiguration(int n, boolean bl, boolean bl2, boolean bl3, float f, HolderSet<Block> holderSet) {
        this.searchRange = n;
        this.canPlaceOnFloor = bl;
        this.canPlaceOnCeiling = bl2;
        this.canPlaceOnWall = bl3;
        this.chanceOfSpreading = f;
        this.canBePlacedOn = holderSet;
        ArrayList arrayList = Lists.newArrayList();
        if (bl2) {
            arrayList.add(Direction.UP);
        }
        if (bl) {
            arrayList.add(Direction.DOWN);
        }
        if (bl3) {
            Direction.Plane.HORIZONTAL.forEach(arrayList::add);
        }
        this.validDirections = Collections.unmodifiableList(arrayList);
    }
}

