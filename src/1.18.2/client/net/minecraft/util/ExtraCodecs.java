/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Codec$ResultFunction
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.util;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.HolderSet;
import org.apache.commons.lang3.mutable.MutableObject;

public class ExtraCodecs {
    public static final Codec<Integer> NON_NEGATIVE_INT = ExtraCodecs.intRangeWithMessage(0, Integer.MAX_VALUE, n -> "Value must be non-negative: " + n);
    public static final Codec<Integer> POSITIVE_INT = ExtraCodecs.intRangeWithMessage(1, Integer.MAX_VALUE, n -> "Value must be positive: " + n);
    public static final Codec<Float> POSITIVE_FLOAT = ExtraCodecs.floatRangeMinExclusiveWithMessage(0.0f, Float.MAX_VALUE, f -> "Value must be positive: " + f);

    public static <F, S> Codec<Either<F, S>> xor(Codec<F> codec, Codec<S> codec2) {
        return new XorCodec<F, S>(codec, codec2);
    }

    public static <P, I> Codec<I> intervalCodec(Codec<P> codec, String string, String string2, BiFunction<P, P, DataResult<I>> biFunction, Function<I, P> function, Function<I, P> function2) {
        Codec codec2 = Codec.list(codec).comapFlatMap(list2 -> Util.fixedSize(list2, 2).flatMap(list -> {
            Object e = list.get(0);
            Object e2 = list.get(1);
            return (DataResult)biFunction.apply(e, e2);
        }), object -> ImmutableList.of(function.apply(object), function2.apply(object)));
        Codec codec3 = RecordCodecBuilder.create(instance -> instance.group((App)codec.fieldOf(string).forGetter(Pair::getFirst), (App)codec.fieldOf(string2).forGetter(Pair::getSecond)).apply((Applicative)instance, Pair::of)).comapFlatMap(pair -> (DataResult)biFunction.apply(pair.getFirst(), pair.getSecond()), object -> Pair.of(function.apply(object), function2.apply(object)));
        Codec codec4 = new EitherCodec(codec2, codec3).xmap(either -> either.map(object -> object, object -> object), Either::left);
        return Codec.either(codec, (Codec)codec4).comapFlatMap(either -> (DataResult)either.map(object -> (DataResult)biFunction.apply(object, object), DataResult::success), object -> {
            Object r;
            Object r2 = function.apply(object);
            if (Objects.equals(r2, r = function2.apply(object))) {
                return Either.left(r2);
            }
            return Either.right((Object)object);
        });
    }

    public static <A> Codec.ResultFunction<A> orElsePartial(final A a) {
        return new Codec.ResultFunction<A>(){

            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> dynamicOps, T t, DataResult<Pair<A, T>> dataResult) {
                MutableObject mutableObject = new MutableObject();
                Optional optional = dataResult.resultOrPartial(arg_0 -> ((MutableObject)mutableObject).setValue(arg_0));
                if (optional.isPresent()) {
                    return dataResult;
                }
                return DataResult.error((String)("(" + (String)mutableObject.getValue() + " -> using default)"), (Object)Pair.of((Object)a, t));
            }

            public <T> DataResult<T> coApply(DynamicOps<T> dynamicOps, A a2, DataResult<T> dataResult) {
                return dataResult;
            }

            public String toString() {
                return "OrElsePartial[" + a + "]";
            }
        };
    }

    public static <E> Codec<E> idResolverCodec(ToIntFunction<E> toIntFunction, IntFunction<E> intFunction, int n2) {
        return Codec.INT.flatXmap(n -> Optional.ofNullable(intFunction.apply((int)n)).map(DataResult::success).orElseGet(() -> DataResult.error((String)("Unknown element id: " + n))), object -> {
            int n2 = toIntFunction.applyAsInt(object);
            return n2 == n2 ? DataResult.error((String)("Element with unknown id: " + object)) : DataResult.success((Object)n2);
        });
    }

    public static <E> Codec<E> stringResolverCodec(Function<E, String> function, Function<String, E> function2) {
        return Codec.STRING.flatXmap(string -> Optional.ofNullable(function2.apply((String)string)).map(DataResult::success).orElseGet(() -> DataResult.error((String)("Unknown element name:" + string))), object -> Optional.ofNullable((String)function.apply(object)).map(DataResult::success).orElseGet(() -> DataResult.error((String)("Element with unknown name: " + object))));
    }

    public static <E> Codec<E> orCompressed(final Codec<E> codec, final Codec<E> codec2) {
        return new Codec<E>(){

            public <T> DataResult<T> encode(E e, DynamicOps<T> dynamicOps, T t) {
                if (dynamicOps.compressMaps()) {
                    return codec2.encode(e, dynamicOps, t);
                }
                return codec.encode(e, dynamicOps, t);
            }

            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> dynamicOps, T t) {
                if (dynamicOps.compressMaps()) {
                    return codec2.decode(dynamicOps, t);
                }
                return codec.decode(dynamicOps, t);
            }

            public String toString() {
                return codec + " orCompressed " + codec2;
            }
        };
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> codec, final Function<E, Lifecycle> function, final Function<E, Lifecycle> function2) {
        return codec.mapResult(new Codec.ResultFunction<E>(){

            public <T> DataResult<Pair<E, T>> apply(DynamicOps<T> dynamicOps, T t, DataResult<Pair<E, T>> dataResult) {
                return dataResult.result().map(pair -> dataResult.setLifecycle((Lifecycle)function.apply(pair.getFirst()))).orElse(dataResult);
            }

            public <T> DataResult<T> coApply(DynamicOps<T> dynamicOps, E e, DataResult<T> dataResult) {
                return dataResult.setLifecycle((Lifecycle)function2.apply(e));
            }

            public String toString() {
                return "WithLifecycle[" + function + " " + function2 + "]";
            }
        });
    }

    private static <N extends Number> Function<N, DataResult<N>> checkRangeWithMessage(N n, N n2, Function<N, String> function) {
        return number3 -> {
            if (((Comparable)((Object)number3)).compareTo(n) >= 0 && ((Comparable)((Object)number3)).compareTo(n2) <= 0) {
                return DataResult.success((Object)number3);
            }
            return DataResult.error((String)((String)function.apply(number3)));
        };
    }

    private static Codec<Integer> intRangeWithMessage(int n, int n2, Function<Integer, String> function) {
        Function<Integer, DataResult<Integer>> function2 = ExtraCodecs.checkRangeWithMessage(n, n2, function);
        return Codec.INT.flatXmap(function2, function2);
    }

    private static <N extends Number> Function<N, DataResult<N>> checkRangeMinExclusiveWithMessage(N n, N n2, Function<N, String> function) {
        return number3 -> {
            if (((Comparable)((Object)number3)).compareTo(n) > 0 && ((Comparable)((Object)number3)).compareTo(n2) <= 0) {
                return DataResult.success((Object)number3);
            }
            return DataResult.error((String)((String)function.apply(number3)));
        };
    }

    private static Codec<Float> floatRangeMinExclusiveWithMessage(float f, float f2, Function<Float, String> function) {
        Function<Float, DataResult<Float>> function2 = ExtraCodecs.checkRangeMinExclusiveWithMessage(Float.valueOf(f), Float.valueOf(f2), function);
        return Codec.FLOAT.flatXmap(function2, function2);
    }

    public static <T> Function<List<T>, DataResult<List<T>>> nonEmptyListCheck() {
        return list -> {
            if (list.isEmpty()) {
                return DataResult.error((String)"List must have contents");
            }
            return DataResult.success((Object)list);
        };
    }

    public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> codec) {
        return codec.flatXmap(ExtraCodecs.nonEmptyListCheck(), ExtraCodecs.nonEmptyListCheck());
    }

    public static <T> Function<HolderSet<T>, DataResult<HolderSet<T>>> nonEmptyHolderSetCheck() {
        return holderSet -> {
            if (holderSet.unwrap().right().filter(List::isEmpty).isPresent()) {
                return DataResult.error((String)"List must have contents");
            }
            return DataResult.success((Object)holderSet);
        };
    }

    public static <T> Codec<HolderSet<T>> nonEmptyHolderSet(Codec<HolderSet<T>> codec) {
        return codec.flatXmap(ExtraCodecs.nonEmptyHolderSetCheck(), ExtraCodecs.nonEmptyHolderSetCheck());
    }

    public static <A> Codec<A> lazyInitializedCodec(Supplier<Codec<A>> supplier) {
        return new LazyInitializedCodec<A>(supplier);
    }

    public static <E> MapCodec<E> retrieveContext(Function<DynamicOps<?>, DataResult<E>> function) {
        class ContextRetrievalCodec
        extends MapCodec<E> {
            final /* synthetic */ Function val$getter;

            ContextRetrievalCodec(Function function) {
                this.val$getter = function;
            }

            public <T> RecordBuilder<T> encode(E e, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
                return recordBuilder;
            }

            public <T> DataResult<E> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
                return (DataResult)this.val$getter.apply(dynamicOps);
            }

            public String toString() {
                return "ContextRetrievalCodec[" + this.val$getter + "]";
            }

            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return Stream.empty();
            }
        }
        return new ContextRetrievalCodec(function);
    }

    public static <E, L extends Collection<E>, T> Function<L, DataResult<L>> ensureHomogenous(Function<E, T> function) {
        return collection -> {
            Iterator iterator = collection.iterator();
            if (iterator.hasNext()) {
                Object r = function.apply(iterator.next());
                while (iterator.hasNext()) {
                    Object e = iterator.next();
                    Object r2 = function.apply(e);
                    if (r2 == r) continue;
                    return DataResult.error((String)("Mixed type list: element " + e + " had type " + r2 + ", but list is of type " + r));
                }
            }
            return DataResult.success((Object)collection, (Lifecycle)Lifecycle.stable());
        };
    }

    static final class XorCodec<F, S>
    implements Codec<Either<F, S>> {
        private final Codec<F> first;
        private final Codec<S> second;

        public XorCodec(Codec<F> codec, Codec<S> codec2) {
            this.first = codec;
            this.second = codec2;
        }

        public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> dynamicOps, T t) {
            DataResult dataResult = this.first.decode(dynamicOps, t).map(pair -> pair.mapFirst(Either::left));
            DataResult dataResult2 = this.second.decode(dynamicOps, t).map(pair -> pair.mapFirst(Either::right));
            Optional optional = dataResult.result();
            Optional optional2 = dataResult2.result();
            if (optional.isPresent() && optional2.isPresent()) {
                return DataResult.error((String)("Both alternatives read successfully, can not pick the correct one; first: " + optional.get() + " second: " + optional2.get()), (Object)((Pair)optional.get()));
            }
            return optional.isPresent() ? dataResult : dataResult2;
        }

        public <T> DataResult<T> encode(Either<F, S> either, DynamicOps<T> dynamicOps, T t) {
            return (DataResult)either.map(object2 -> this.first.encode(object2, dynamicOps, t), object2 -> this.second.encode(object2, dynamicOps, t));
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            XorCodec xorCodec = (XorCodec)object;
            return Objects.equals(this.first, xorCodec.first) && Objects.equals(this.second, xorCodec.second);
        }

        public int hashCode() {
            return Objects.hash(this.first, this.second);
        }

        public String toString() {
            return "XorCodec[" + this.first + ", " + this.second + "]";
        }

        public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
            return this.encode((Either)object, dynamicOps, object2);
        }
    }

    static final class EitherCodec<F, S>
    implements Codec<Either<F, S>> {
        private final Codec<F> first;
        private final Codec<S> second;

        public EitherCodec(Codec<F> codec, Codec<S> codec2) {
            this.first = codec;
            this.second = codec2;
        }

        public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> dynamicOps, T t) {
            DataResult dataResult = this.first.decode(dynamicOps, t).map(pair -> pair.mapFirst(Either::left));
            if (!dataResult.error().isPresent()) {
                return dataResult;
            }
            DataResult dataResult2 = this.second.decode(dynamicOps, t).map(pair -> pair.mapFirst(Either::right));
            if (!dataResult2.error().isPresent()) {
                return dataResult2;
            }
            return dataResult.apply2((pair, pair2) -> pair2, dataResult2);
        }

        public <T> DataResult<T> encode(Either<F, S> either, DynamicOps<T> dynamicOps, T t) {
            return (DataResult)either.map(object2 -> this.first.encode(object2, dynamicOps, t), object2 -> this.second.encode(object2, dynamicOps, t));
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            EitherCodec eitherCodec = (EitherCodec)object;
            return Objects.equals(this.first, eitherCodec.first) && Objects.equals(this.second, eitherCodec.second);
        }

        public int hashCode() {
            return Objects.hash(this.first, this.second);
        }

        public String toString() {
            return "EitherCodec[" + this.first + ", " + this.second + "]";
        }

        public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
            return this.encode((Either)object, dynamicOps, object2);
        }
    }

    record LazyInitializedCodec<A>(Supplier<Codec<A>> delegate) implements Codec<A>
    {
        LazyInitializedCodec(Supplier<Codec<A>> supplier) {
            this.delegate = supplier = Suppliers.memoize(() -> supplier.get());
        }

        public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> dynamicOps, T t) {
            return this.delegate.get().decode(dynamicOps, t);
        }

        public <T> DataResult<T> encode(A a, DynamicOps<T> dynamicOps, T t) {
            return this.delegate.get().encode(a, dynamicOps, t);
        }
    }
}

