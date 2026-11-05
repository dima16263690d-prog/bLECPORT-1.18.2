/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;

public class ConstantHeight
extends HeightProvider {
    public static final ConstantHeight ZERO = new ConstantHeight(VerticalAnchor.absolute(0));
    public static final Codec<ConstantHeight> CODEC = Codec.either(VerticalAnchor.CODEC, (Codec)RecordCodecBuilder.create(instance -> instance.group((App)VerticalAnchor.CODEC.fieldOf("value").forGetter(constantHeight -> constantHeight.value)).apply((Applicative)instance, ConstantHeight::new))).xmap(either -> (ConstantHeight)either.map(ConstantHeight::of, constantHeight -> constantHeight), constantHeight -> Either.left((Object)constantHeight.value));
    private final VerticalAnchor value;

    public static ConstantHeight of(VerticalAnchor verticalAnchor) {
        return new ConstantHeight(verticalAnchor);
    }

    private ConstantHeight(VerticalAnchor verticalAnchor) {
        this.value = verticalAnchor;
    }

    public VerticalAnchor getValue() {
        return this.value;
    }

    @Override
    public int sample(Random random, WorldGenerationContext worldGenerationContext) {
        return this.value.resolveY(worldGenerationContext);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.CONSTANT;
    }

    public String toString() {
        return this.value.toString();
    }
}

