/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class RandomizedIntStateProvider
extends BlockStateProvider {
    public static final Codec<RandomizedIntStateProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockStateProvider.CODEC.fieldOf("source").forGetter(randomizedIntStateProvider -> randomizedIntStateProvider.source), (App)Codec.STRING.fieldOf("property").forGetter(randomizedIntStateProvider -> randomizedIntStateProvider.propertyName), (App)IntProvider.CODEC.fieldOf("values").forGetter(randomizedIntStateProvider -> randomizedIntStateProvider.values)).apply((Applicative)instance, RandomizedIntStateProvider::new));
    private final BlockStateProvider source;
    private final String propertyName;
    @Nullable
    private IntegerProperty property;
    private final IntProvider values;

    public RandomizedIntStateProvider(BlockStateProvider blockStateProvider, IntegerProperty integerProperty, IntProvider intProvider) {
        this.source = blockStateProvider;
        this.property = integerProperty;
        this.propertyName = integerProperty.getName();
        this.values = intProvider;
        Collection<Integer> collection = integerProperty.getPossibleValues();
        for (int i = intProvider.getMinValue(); i <= intProvider.getMaxValue(); ++i) {
            if (collection.contains(i)) continue;
            throw new IllegalArgumentException("Property value out of range: " + integerProperty.getName() + ": " + i);
        }
    }

    public RandomizedIntStateProvider(BlockStateProvider blockStateProvider, String string, IntProvider intProvider) {
        this.source = blockStateProvider;
        this.propertyName = string;
        this.values = intProvider;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.RANDOMIZED_INT_STATE_PROVIDER;
    }

    @Override
    public BlockState getState(Random random, BlockPos blockPos) {
        BlockState blockState = this.source.getState(random, blockPos);
        if (this.property == null || !blockState.hasProperty(this.property)) {
            this.property = RandomizedIntStateProvider.findProperty(blockState, this.propertyName);
        }
        return (BlockState)blockState.setValue(this.property, this.values.sample(random));
    }

    private static IntegerProperty findProperty(BlockState blockState, String string) {
        Collection<Property<?>> collection = blockState.getProperties();
        Optional<IntegerProperty> optional = collection.stream().filter(property -> property.getName().equals(string)).filter(property -> property instanceof IntegerProperty).map(property -> (IntegerProperty)property).findAny();
        return optional.orElseThrow(() -> new IllegalArgumentException("Illegal property: " + string));
    }
}

