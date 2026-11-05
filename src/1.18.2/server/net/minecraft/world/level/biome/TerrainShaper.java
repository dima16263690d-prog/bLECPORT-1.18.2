/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jetbrains.annotations.VisibleForTesting
 */
package net.minecraft.world.level.biome;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.VisibleForTesting;

public record TerrainShaper(CubicSpline<Point> offsetSampler, CubicSpline<Point> factorSampler, CubicSpline<Point> jaggednessSampler) {
    private static final Codec<CubicSpline<Point>> SPLINE_CODEC = CubicSpline.codec(Coordinate.WIDE_CODEC);
    public static final Codec<CubicSpline<PointCustom>> SPLINE_CUSTOM_CODEC = CubicSpline.codec(CoordinateCustom.WIDE_CODEC);
    public static final Codec<TerrainShaper> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)SPLINE_CODEC.fieldOf("offset").forGetter(TerrainShaper::offsetSampler), (App)SPLINE_CODEC.fieldOf("factor").forGetter(TerrainShaper::factorSampler), (App)SPLINE_CODEC.fieldOf("jaggedness").forGetter(terrainShaper -> terrainShaper.jaggednessSampler)).apply((Applicative)instance, TerrainShaper::new));
    private static final float GLOBAL_OFFSET = -0.50375f;
    private static final ToFloatFunction<Float> NO_TRANSFORM = f -> f.floatValue();

    private static float getAmplifiedOffset(float f) {
        return f < 0.0f ? f : f * 2.0f;
    }

    private static float getAmplifiedFactor(float f) {
        return 1.25f - 6.25f / (f + 5.0f);
    }

    private static float getAmplifiedJaggedness(float f) {
        return f * 2.0f;
    }

    public static TerrainShaper overworld(boolean bl) {
        ToFloatFunction<Float> toFloatFunction = bl ? TerrainShaper::getAmplifiedOffset : NO_TRANSFORM;
        ToFloatFunction<Float> toFloatFunction2 = bl ? TerrainShaper::getAmplifiedFactor : NO_TRANSFORM;
        ToFloatFunction<Float> toFloatFunction3 = bl ? TerrainShaper::getAmplifiedJaggedness : NO_TRANSFORM;
        CubicSpline<Point> cubicSpline = TerrainShaper.buildErosionOffsetSpline(-0.15f, 0.0f, 0.0f, 0.1f, 0.0f, -0.03f, false, false, toFloatFunction);
        CubicSpline<Point> cubicSpline2 = TerrainShaper.buildErosionOffsetSpline(-0.1f, 0.03f, 0.1f, 0.1f, 0.01f, -0.03f, false, false, toFloatFunction);
        CubicSpline<Point> cubicSpline3 = TerrainShaper.buildErosionOffsetSpline(-0.1f, 0.03f, 0.1f, 0.7f, 0.01f, -0.03f, true, true, toFloatFunction);
        CubicSpline<Point> cubicSpline4 = TerrainShaper.buildErosionOffsetSpline(-0.05f, 0.03f, 0.1f, 1.0f, 0.01f, 0.01f, true, true, toFloatFunction);
        float f = -0.51f;
        float f2 = -0.4f;
        float f3 = 0.1f;
        float f4 = -0.15f;
        CubicSpline<Point> cubicSpline5 = CubicSpline.builder(Coordinate.CONTINENTS, toFloatFunction).addPoint(-1.1f, 0.044f, 0.0f).addPoint(-1.02f, -0.2222f, 0.0f).addPoint(-0.51f, -0.2222f, 0.0f).addPoint(-0.44f, -0.12f, 0.0f).addPoint(-0.18f, -0.12f, 0.0f).addPoint(-0.16f, cubicSpline, 0.0f).addPoint(-0.15f, cubicSpline, 0.0f).addPoint(-0.1f, cubicSpline2, 0.0f).addPoint(0.25f, cubicSpline3, 0.0f).addPoint(1.0f, cubicSpline4, 0.0f).build();
        CubicSpline<Point> cubicSpline6 = CubicSpline.builder(Coordinate.CONTINENTS, NO_TRANSFORM).addPoint(-0.19f, 3.95f, 0.0f).addPoint(-0.15f, TerrainShaper.getErosionFactor(6.25f, true, NO_TRANSFORM), 0.0f).addPoint(-0.1f, TerrainShaper.getErosionFactor(5.47f, true, toFloatFunction2), 0.0f).addPoint(0.03f, TerrainShaper.getErosionFactor(5.08f, true, toFloatFunction2), 0.0f).addPoint(0.06f, TerrainShaper.getErosionFactor(4.69f, false, toFloatFunction2), 0.0f).build();
        float f5 = 0.65f;
        CubicSpline<Point> cubicSpline7 = CubicSpline.builder(Coordinate.CONTINENTS, toFloatFunction3).addPoint(-0.11f, 0.0f, 0.0f).addPoint(0.03f, TerrainShaper.buildErosionJaggednessSpline(1.0f, 0.5f, 0.0f, 0.0f, toFloatFunction3), 0.0f).addPoint(0.65f, TerrainShaper.buildErosionJaggednessSpline(1.0f, 1.0f, 1.0f, 0.0f, toFloatFunction3), 0.0f).build();
        return new TerrainShaper(cubicSpline5, cubicSpline6, cubicSpline7);
    }

    private static CubicSpline<Point> buildErosionJaggednessSpline(float f, float f2, float f3, float f4, ToFloatFunction<Float> toFloatFunction) {
        float f5 = -0.5775f;
        CubicSpline<Point> cubicSpline = TerrainShaper.buildRidgeJaggednessSpline(f, f3, toFloatFunction);
        CubicSpline<Point> cubicSpline2 = TerrainShaper.buildRidgeJaggednessSpline(f2, f4, toFloatFunction);
        return CubicSpline.builder(Coordinate.EROSION, toFloatFunction).addPoint(-1.0f, cubicSpline, 0.0f).addPoint(-0.78f, cubicSpline2, 0.0f).addPoint(-0.5775f, cubicSpline2, 0.0f).addPoint(-0.375f, 0.0f, 0.0f).build();
    }

    private static CubicSpline<Point> buildRidgeJaggednessSpline(float f, float f2, ToFloatFunction<Float> toFloatFunction) {
        float f3 = TerrainShaper.peaksAndValleys(0.4f);
        float f4 = TerrainShaper.peaksAndValleys(0.56666666f);
        float f5 = (f3 + f4) / 2.0f;
        CubicSpline.Builder<Point> builder = CubicSpline.builder(Coordinate.RIDGES, toFloatFunction);
        builder.addPoint(f3, 0.0f, 0.0f);
        if (f2 > 0.0f) {
            builder.addPoint(f5, TerrainShaper.buildWeirdnessJaggednessSpline(f2, toFloatFunction), 0.0f);
        } else {
            builder.addPoint(f5, 0.0f, 0.0f);
        }
        if (f > 0.0f) {
            builder.addPoint(1.0f, TerrainShaper.buildWeirdnessJaggednessSpline(f, toFloatFunction), 0.0f);
        } else {
            builder.addPoint(1.0f, 0.0f, 0.0f);
        }
        return builder.build();
    }

    private static CubicSpline<Point> buildWeirdnessJaggednessSpline(float f, ToFloatFunction<Float> toFloatFunction) {
        float f2 = 0.63f * f;
        float f3 = 0.3f * f;
        return CubicSpline.builder(Coordinate.WEIRDNESS, toFloatFunction).addPoint(-0.01f, f2, 0.0f).addPoint(0.01f, f3, 0.0f).build();
    }

    private static CubicSpline<Point> getErosionFactor(float f, boolean bl, ToFloatFunction<Float> toFloatFunction) {
        CubicSpline<Point> cubicSpline = CubicSpline.builder(Coordinate.WEIRDNESS, toFloatFunction).addPoint(-0.2f, 6.3f, 0.0f).addPoint(0.2f, f, 0.0f).build();
        CubicSpline.Builder<Point> builder = CubicSpline.builder(Coordinate.EROSION, toFloatFunction).addPoint(-0.6f, cubicSpline, 0.0f).addPoint(-0.5f, CubicSpline.builder(Coordinate.WEIRDNESS, toFloatFunction).addPoint(-0.05f, 6.3f, 0.0f).addPoint(0.05f, 2.67f, 0.0f).build(), 0.0f).addPoint(-0.35f, cubicSpline, 0.0f).addPoint(-0.25f, cubicSpline, 0.0f).addPoint(-0.1f, CubicSpline.builder(Coordinate.WEIRDNESS, toFloatFunction).addPoint(-0.05f, 2.67f, 0.0f).addPoint(0.05f, 6.3f, 0.0f).build(), 0.0f).addPoint(0.03f, cubicSpline, 0.0f);
        if (bl) {
            CubicSpline<Point> cubicSpline2 = CubicSpline.builder(Coordinate.WEIRDNESS, toFloatFunction).addPoint(0.0f, f, 0.0f).addPoint(0.1f, 0.625f, 0.0f).build();
            CubicSpline<Point> cubicSpline3 = CubicSpline.builder(Coordinate.RIDGES, toFloatFunction).addPoint(-0.9f, f, 0.0f).addPoint(-0.69f, cubicSpline2, 0.0f).build();
            builder.addPoint(0.35f, f, 0.0f).addPoint(0.45f, cubicSpline3, 0.0f).addPoint(0.55f, cubicSpline3, 0.0f).addPoint(0.62f, f, 0.0f);
        } else {
            CubicSpline<Point> cubicSpline4 = CubicSpline.builder(Coordinate.RIDGES, toFloatFunction).addPoint(-0.7f, cubicSpline, 0.0f).addPoint(-0.15f, 1.37f, 0.0f).build();
            CubicSpline<Point> cubicSpline5 = CubicSpline.builder(Coordinate.RIDGES, toFloatFunction).addPoint(0.45f, cubicSpline, 0.0f).addPoint(0.7f, 1.56f, 0.0f).build();
            builder.addPoint(0.05f, cubicSpline5, 0.0f).addPoint(0.4f, cubicSpline5, 0.0f).addPoint(0.45f, cubicSpline4, 0.0f).addPoint(0.55f, cubicSpline4, 0.0f).addPoint(0.58f, f, 0.0f);
        }
        return builder.build();
    }

    private static float calculateSlope(float f, float f2, float f3, float f4) {
        return (f2 - f) / (f4 - f3);
    }

    private static CubicSpline<Point> buildMountainRidgeSplineWithPoints(float f, boolean bl, ToFloatFunction<Float> toFloatFunction) {
        CubicSpline.Builder<Point> builder = CubicSpline.builder(Coordinate.RIDGES, toFloatFunction);
        float f2 = -0.7f;
        float f3 = -1.0f;
        float f4 = TerrainShaper.mountainContinentalness(-1.0f, f, -0.7f);
        float f5 = 1.0f;
        float f6 = TerrainShaper.mountainContinentalness(1.0f, f, -0.7f);
        float f7 = TerrainShaper.calculateMountainRidgeZeroContinentalnessPoint(f);
        float f8 = -0.65f;
        if (-0.65f < f7 && f7 < 1.0f) {
            float f9 = TerrainShaper.mountainContinentalness(-0.65f, f, -0.7f);
            float f10 = -0.75f;
            float f11 = TerrainShaper.mountainContinentalness(-0.75f, f, -0.7f);
            float f12 = TerrainShaper.calculateSlope(f4, f11, -1.0f, -0.75f);
            builder.addPoint(-1.0f, f4, f12);
            builder.addPoint(-0.75f, f11, 0.0f);
            builder.addPoint(-0.65f, f9, 0.0f);
            float f13 = TerrainShaper.mountainContinentalness(f7, f, -0.7f);
            float f14 = TerrainShaper.calculateSlope(f13, f6, f7, 1.0f);
            float f15 = 0.01f;
            builder.addPoint(f7 - 0.01f, f13, 0.0f);
            builder.addPoint(f7, f13, f14);
            builder.addPoint(1.0f, f6, f14);
        } else {
            float f16 = TerrainShaper.calculateSlope(f4, f6, -1.0f, 1.0f);
            if (bl) {
                builder.addPoint(-1.0f, Math.max(0.2f, f4), 0.0f);
                builder.addPoint(0.0f, Mth.lerp(0.5f, f4, f6), f16);
            } else {
                builder.addPoint(-1.0f, f4, f16);
            }
            builder.addPoint(1.0f, f6, f16);
        }
        return builder.build();
    }

    private static float mountainContinentalness(float f, float f2, float f3) {
        float f4 = 1.17f;
        float f5 = 0.46082947f;
        float f6 = 1.0f - (1.0f - f2) * 0.5f;
        float f7 = 0.5f * (1.0f - f2);
        float f8 = (f + 1.17f) * 0.46082947f;
        float f9 = f8 * f6 - f7;
        if (f < f3) {
            return Math.max(f9, -0.2222f);
        }
        return Math.max(f9, 0.0f);
    }

    private static float calculateMountainRidgeZeroContinentalnessPoint(float f) {
        float f2 = 1.17f;
        float f3 = 0.46082947f;
        float f4 = 1.0f - (1.0f - f) * 0.5f;
        float f5 = 0.5f * (1.0f - f);
        return f5 / (0.46082947f * f4) - 1.17f;
    }

    private static CubicSpline<Point> buildErosionOffsetSpline(float f, float f2, float f3, float f4, float f5, float f6, boolean bl, boolean bl2, ToFloatFunction<Float> toFloatFunction) {
        float f7 = 0.6f;
        float f8 = 0.5f;
        float f9 = 0.5f;
        CubicSpline<Point> cubicSpline = TerrainShaper.buildMountainRidgeSplineWithPoints(Mth.lerp(f4, 0.6f, 1.5f), bl2, toFloatFunction);
        CubicSpline<Point> cubicSpline2 = TerrainShaper.buildMountainRidgeSplineWithPoints(Mth.lerp(f4, 0.6f, 1.0f), bl2, toFloatFunction);
        CubicSpline<Point> cubicSpline3 = TerrainShaper.buildMountainRidgeSplineWithPoints(f4, bl2, toFloatFunction);
        CubicSpline<Point> cubicSpline4 = TerrainShaper.ridgeSpline(f - 0.15f, 0.5f * f4, Mth.lerp(0.5f, 0.5f, 0.5f) * f4, 0.5f * f4, 0.6f * f4, 0.5f, toFloatFunction);
        CubicSpline<Point> cubicSpline5 = TerrainShaper.ridgeSpline(f, f5 * f4, f2 * f4, 0.5f * f4, 0.6f * f4, 0.5f, toFloatFunction);
        CubicSpline<Point> cubicSpline6 = TerrainShaper.ridgeSpline(f, f5, f5, f2, f3, 0.5f, toFloatFunction);
        CubicSpline<Point> cubicSpline7 = TerrainShaper.ridgeSpline(f, f5, f5, f2, f3, 0.5f, toFloatFunction);
        CubicSpline<Point> cubicSpline8 = CubicSpline.builder(Coordinate.RIDGES, toFloatFunction).addPoint(-1.0f, f, 0.0f).addPoint(-0.4f, cubicSpline6, 0.0f).addPoint(0.0f, f3 + 0.07f, 0.0f).build();
        CubicSpline<Point> cubicSpline9 = TerrainShaper.ridgeSpline(-0.02f, f6, f6, f2, f3, 0.0f, toFloatFunction);
        CubicSpline.Builder<Point> builder = CubicSpline.builder(Coordinate.EROSION, toFloatFunction).addPoint(-0.85f, cubicSpline, 0.0f).addPoint(-0.7f, cubicSpline2, 0.0f).addPoint(-0.4f, cubicSpline3, 0.0f).addPoint(-0.35f, cubicSpline4, 0.0f).addPoint(-0.1f, cubicSpline5, 0.0f).addPoint(0.2f, cubicSpline6, 0.0f);
        if (bl) {
            builder.addPoint(0.4f, cubicSpline7, 0.0f).addPoint(0.45f, cubicSpline8, 0.0f).addPoint(0.55f, cubicSpline8, 0.0f).addPoint(0.58f, cubicSpline7, 0.0f);
        }
        builder.addPoint(0.7f, cubicSpline9, 0.0f);
        return builder.build();
    }

    private static CubicSpline<Point> ridgeSpline(float f, float f2, float f3, float f4, float f5, float f6, ToFloatFunction<Float> toFloatFunction) {
        float f7 = Math.max(0.5f * (f2 - f), f6);
        float f8 = 5.0f * (f3 - f2);
        return CubicSpline.builder(Coordinate.RIDGES, toFloatFunction).addPoint(-1.0f, f, f7).addPoint(-0.4f, f2, Math.min(f7, f8)).addPoint(0.0f, f3, f8).addPoint(0.4f, f4, 2.0f * (f4 - f3)).addPoint(1.0f, f5, 0.7f * (f5 - f4)).build();
    }

    public void addDebugBiomesToVisualizeSplinePoints(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
        Float f;
        int n;
        Climate.Parameter parameter = Climate.Parameter.span(-1.0f, 1.0f);
        consumer.accept((Pair<Climate.ParameterPoint, ResourceKey<Biome>>)Pair.of((Object)((Object)Climate.parameters(parameter, parameter, parameter, parameter, Climate.Parameter.point(0.0f), parameter, 0.01f)), Biomes.PLAINS));
        CubicSpline.Multipoint multipoint = (CubicSpline.Multipoint)TerrainShaper.buildErosionOffsetSpline(-0.15f, 0.0f, 0.0f, 0.1f, 0.0f, -0.03f, false, false, NO_TRANSFORM);
        ResourceKey<Biome> resourceKey = Biomes.DESERT;
        float[] fArray = multipoint.locations();
        int n2 = fArray.length;
        for (n = 0; n < n2; ++n) {
            f = Float.valueOf(fArray[n]);
            consumer.accept((Pair<Climate.ParameterPoint, ResourceKey<Biome>>)Pair.of((Object)((Object)Climate.parameters(parameter, parameter, parameter, Climate.Parameter.point(f.floatValue()), Climate.Parameter.point(0.0f), parameter, 0.0f)), resourceKey));
            resourceKey = resourceKey == Biomes.DESERT ? Biomes.BADLANDS : Biomes.DESERT;
        }
        fArray = ((CubicSpline.Multipoint)this.offsetSampler).locations();
        n2 = fArray.length;
        for (n = 0; n < n2; ++n) {
            f = Float.valueOf(fArray[n]);
            consumer.accept((Pair<Climate.ParameterPoint, ResourceKey<Biome>>)Pair.of((Object)((Object)Climate.parameters(parameter, parameter, Climate.Parameter.point(f.floatValue()), parameter, Climate.Parameter.point(0.0f), parameter, 0.0f)), Biomes.SNOWY_TAIGA));
        }
    }

    public float offset(Point point) {
        return this.offsetSampler.apply(point) + -0.50375f;
    }

    public float factor(Point point) {
        return this.factorSampler.apply(point);
    }

    public float jaggedness(Point point) {
        return this.jaggednessSampler.apply(point);
    }

    public static Point makePoint(float f, float f2, float f3) {
        return new Point(f, f2, TerrainShaper.peaksAndValleys(f3), f3);
    }

    public static PointCustom makePoint(DensityFunction.FunctionContext functionContext) {
        return new PointCustom(functionContext);
    }

    public static float peaksAndValleys(float f) {
        return -(Math.abs(Math.abs(f) - 0.6666667f) - 0.33333334f) * 3.0f;
    }

    @VisibleForTesting
    protected static enum Coordinate implements StringRepresentable,
    ToFloatFunction<Point>
    {
        CONTINENTS(Point::continents, "continents"),
        EROSION(Point::erosion, "erosion"),
        WEIRDNESS(Point::weirdness, "weirdness"),
        RIDGES(Point::ridges, "ridges");

        private static final Map<String, Coordinate> BY_NAME;
        private static final Codec<Coordinate> CODEC;
        static final Codec<ToFloatFunction<Point>> WIDE_CODEC;
        private final ToFloatFunction<Point> reference;
        private final String name;

        private Coordinate(ToFloatFunction<Point> toFloatFunction, String string2) {
            this.reference = toFloatFunction;
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public String toString() {
            return this.name;
        }

        @Override
        public float apply(Point point) {
            return this.reference.apply(point);
        }

        @Override
        public /* synthetic */ float apply(Object object) {
            return this.apply((Point)((Object)object));
        }

        static {
            BY_NAME = Arrays.stream(Coordinate.values()).collect(Collectors.toMap(Coordinate::getSerializedName, coordinate -> coordinate));
            CODEC = StringRepresentable.fromEnum(Coordinate::values, BY_NAME::get);
            WIDE_CODEC = CODEC.flatComapMap(coordinate -> coordinate, toFloatFunction -> {
                DataResult dataResult;
                if (toFloatFunction instanceof Coordinate) {
                    Coordinate coordinate = (Coordinate)toFloatFunction;
                    dataResult = DataResult.success((Object)coordinate);
                } else {
                    dataResult = DataResult.error((String)("Not a coordinate resolver: " + toFloatFunction));
                }
                return dataResult;
            });
        }
    }

    public record Point(float continents, float erosion, float ridges, float weirdness) {
    }

    public record PointCustom(DensityFunction.FunctionContext context) {
    }

    public record CoordinateCustom(Holder<DensityFunction> function) implements ToFloatFunction<PointCustom>
    {
        static final Codec<ToFloatFunction<PointCustom>> WIDE_CODEC = DensityFunction.CODEC.flatComapMap(CoordinateCustom::new, toFloatFunction -> {
            DataResult dataResult;
            if (toFloatFunction instanceof CoordinateCustom) {
                CoordinateCustom coordinateCustom = (CoordinateCustom)toFloatFunction;
                dataResult = DataResult.success(coordinateCustom.function());
            } else {
                dataResult = DataResult.error((String)("Not a coordinate resolver: " + toFloatFunction));
            }
            return dataResult;
        });

        @Override
        public float apply(PointCustom pointCustom) {
            return (float)this.function.value().compute(pointCustom.context());
        }

        public CoordinateCustom mapAll(DensityFunction.Visitor visitor) {
            return new CoordinateCustom(new Holder.Direct<DensityFunction>(this.function.value().mapAll(visitor)));
        }

        @Override
        public /* synthetic */ float apply(Object object) {
            return this.apply((PointCustom)((Object)object));
        }
    }
}

