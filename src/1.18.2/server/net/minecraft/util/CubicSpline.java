/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.floats.FloatArrayList
 *  it.unimi.dsi.fastutil.floats.FloatList
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.VisibleForDebug;
import org.apache.commons.lang3.mutable.MutableObject;

public interface CubicSpline<C>
extends ToFloatFunction<C> {
    @VisibleForDebug
    public String parityString();

    public float min();

    public float max();

    public CubicSpline<C> mapAll(CoordinateVisitor<C> var1);

    public static <C> Codec<CubicSpline<C>> codec(Codec<ToFloatFunction<C>> codec) {
        record Point<C>(float location, CubicSpline<C> value, float derivative) {
        }
        MutableObject mutableObject = new MutableObject();
        Codec codec2 = RecordCodecBuilder.create(instance -> instance.group((App)Codec.FLOAT.fieldOf("location").forGetter(Point::location), (App)ExtraCodecs.lazyInitializedCodec(() -> ((MutableObject)mutableObject).getValue()).fieldOf("value").forGetter(Point::value), (App)Codec.FLOAT.fieldOf("derivative").forGetter(Point::derivative)).apply((Applicative)instance, (f, cubicSpline, f2) -> new Point((float)f, cubicSpline, (float)f2)));
        Codec codec3 = RecordCodecBuilder.create(instance -> instance.group((App)codec.fieldOf("coordinate").forGetter(Multipoint::coordinate), (App)ExtraCodecs.nonEmptyList(codec2.listOf()).fieldOf("points").forGetter(multipoint -> IntStream.range(0, multipoint.locations.length).mapToObj(n -> new Point(multipoint.locations()[n], multipoint.values().get(n), multipoint.derivatives()[n])).toList())).apply((Applicative)instance, (toFloatFunction, list) -> {
            float[] fArray = new float[list.size()];
            ImmutableList.Builder builder = ImmutableList.builder();
            float[] fArray2 = new float[list.size()];
            for (int i = 0; i < list.size(); ++i) {
                Point point = (Point)((Object)((Object)((Object)list.get(i))));
                fArray[i] = point.location();
                builder.add(point.value());
                fArray2[i] = point.derivative();
            }
            return new Multipoint(toFloatFunction, fArray, builder.build(), fArray2);
        }));
        mutableObject.setValue((Object)Codec.either((Codec)Codec.FLOAT, (Codec)codec3).xmap(either -> (CubicSpline)either.map(Constant::new, multipoint -> multipoint), cubicSpline -> {
            Either either;
            if (cubicSpline instanceof Constant) {
                Constant constant = (Constant)cubicSpline;
                either = Either.left((Object)Float.valueOf(constant.value()));
            } else {
                either = Either.right((Object)((Multipoint)cubicSpline));
            }
            return either;
        }));
        return (Codec)mutableObject.getValue();
    }

    public static <C> CubicSpline<C> constant(float f) {
        return new Constant(f);
    }

    public static <C> Builder<C> builder(ToFloatFunction<C> toFloatFunction) {
        return new Builder<C>(toFloatFunction);
    }

    public static <C> Builder<C> builder(ToFloatFunction<C> toFloatFunction, ToFloatFunction<Float> toFloatFunction2) {
        return new Builder<C>(toFloatFunction, toFloatFunction2);
    }

    @VisibleForDebug
    public record Constant<C>(float value) implements CubicSpline<C>
    {
        @Override
        public float apply(C c) {
            return this.value;
        }

        @Override
        public String parityString() {
            return String.format("k=%.3f", Float.valueOf(this.value));
        }

        @Override
        public float min() {
            return this.value;
        }

        @Override
        public float max() {
            return this.value;
        }

        @Override
        public CubicSpline<C> mapAll(CoordinateVisitor<C> coordinateVisitor) {
            return this;
        }
    }

    public static final class Builder<C> {
        private final ToFloatFunction<C> coordinate;
        private final ToFloatFunction<Float> valueTransformer;
        private final FloatList locations = new FloatArrayList();
        private final List<CubicSpline<C>> values = Lists.newArrayList();
        private final FloatList derivatives = new FloatArrayList();

        protected Builder(ToFloatFunction<C> toFloatFunction) {
            this(toFloatFunction, f -> f.floatValue());
        }

        protected Builder(ToFloatFunction<C> toFloatFunction, ToFloatFunction<Float> toFloatFunction2) {
            this.coordinate = toFloatFunction;
            this.valueTransformer = toFloatFunction2;
        }

        public Builder<C> addPoint(float f, float f2, float f3) {
            return this.addPoint(f, new Constant(this.valueTransformer.apply(Float.valueOf(f2))), f3);
        }

        public Builder<C> addPoint(float f, CubicSpline<C> cubicSpline, float f2) {
            if (!this.locations.isEmpty() && f <= this.locations.getFloat(this.locations.size() - 1)) {
                throw new IllegalArgumentException("Please register points in ascending order");
            }
            this.locations.add(f);
            this.values.add(cubicSpline);
            this.derivatives.add(f2);
            return this;
        }

        public CubicSpline<C> build() {
            if (this.locations.isEmpty()) {
                throw new IllegalStateException("No elements added");
            }
            return new Multipoint<C>(this.coordinate, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray());
        }
    }

    @VisibleForDebug
    public static final class Multipoint<C>
    extends Record
    implements CubicSpline<C> {
        private final ToFloatFunction<C> coordinate;
        final float[] locations;
        private final List<CubicSpline<C>> values;
        private final float[] derivatives;

        public Multipoint(ToFloatFunction<C> toFloatFunction, float[] fArray, List<CubicSpline<C>> list, float[] fArray2) {
            if (fArray.length != list.size() || fArray.length != fArray2.length) {
                throw new IllegalArgumentException("All lengths must be equal, got: " + fArray.length + " " + list.size() + " " + fArray2.length);
            }
            this.coordinate = toFloatFunction;
            this.locations = fArray;
            this.values = list;
            this.derivatives = fArray2;
        }

        @Override
        public float apply(C c) {
            float f = this.coordinate.apply(c);
            int n2 = Mth.binarySearch(0, this.locations.length, n -> f < this.locations[n]) - 1;
            int n3 = this.locations.length - 1;
            if (n2 < 0) {
                return this.values.get(0).apply(c) + this.derivatives[0] * (f - this.locations[0]);
            }
            if (n2 == n3) {
                return this.values.get(n3).apply(c) + this.derivatives[n3] * (f - this.locations[n3]);
            }
            float f2 = this.locations[n2];
            float f3 = this.locations[n2 + 1];
            float f4 = (f - f2) / (f3 - f2);
            ToFloatFunction toFloatFunction = this.values.get(n2);
            ToFloatFunction toFloatFunction2 = this.values.get(n2 + 1);
            float f5 = this.derivatives[n2];
            float f6 = this.derivatives[n2 + 1];
            float f7 = toFloatFunction.apply(c);
            float f8 = toFloatFunction2.apply(c);
            float f9 = f5 * (f3 - f2) - (f8 - f7);
            float f10 = -f6 * (f3 - f2) + (f8 - f7);
            float f11 = Mth.lerp(f4, f7, f8) + f4 * (1.0f - f4) * Mth.lerp(f4, f9, f10);
            return f11;
        }

        @Override
        @VisibleForTesting
        public String parityString() {
            return "Spline{coordinate=" + this.coordinate + ", locations=" + this.toString(this.locations) + ", derivatives=" + this.toString(this.derivatives) + ", values=" + this.values.stream().map(CubicSpline::parityString).collect(Collectors.joining(", ", "[", "]")) + "}";
        }

        private String toString(float[] fArray) {
            return "[" + IntStream.range(0, fArray.length).mapToDouble(n -> fArray[n]).mapToObj(d -> String.format(Locale.ROOT, "%.3f", d)).collect(Collectors.joining(", ")) + "]";
        }

        @Override
        public float min() {
            return (float)this.values().stream().mapToDouble(CubicSpline::min).min().orElseThrow();
        }

        @Override
        public float max() {
            return (float)this.values().stream().mapToDouble(CubicSpline::max).max().orElseThrow();
        }

        @Override
        public CubicSpline<C> mapAll(CoordinateVisitor<C> coordinateVisitor) {
            return new Multipoint<C>(coordinateVisitor.visit(this.coordinate), this.locations, this.values().stream().map(cubicSpline -> cubicSpline.mapAll(coordinateVisitor)).toList(), this.derivatives);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Multipoint.class, "coordinate;locations;values;derivatives", "coordinate", "locations", "values", "derivatives"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Multipoint.class, "coordinate;locations;values;derivatives", "coordinate", "locations", "values", "derivatives"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Multipoint.class, "coordinate;locations;values;derivatives", "coordinate", "locations", "values", "derivatives"}, this, object);
        }

        public ToFloatFunction<C> coordinate() {
            return this.coordinate;
        }

        public float[] locations() {
            return this.locations;
        }

        public List<CubicSpline<C>> values() {
            return this.values;
        }

        public float[] derivatives() {
            return this.derivatives;
        }
    }

    public static interface CoordinateVisitor<C> {
        public ToFloatFunction<C> visit(ToFloatFunction<C> var1);
    }
}

