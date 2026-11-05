/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Ticker
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.util.concurrent.MoreExecutors
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DataResult
 *  it.unimi.dsi.fastutil.Hash$Strategy
 *  javax.annotation.Nullable
 *  org.apache.commons.io.IOUtils
 *  org.slf4j.Logger
 */
package net.minecraft;

import com.google.common.base.Ticker;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.Hash;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.LambdaMetafactory;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CharPredicate;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class Util {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_MAX_THREADS = 255;
    private static final String MAX_THREADS_SYSTEM_PROPERTY = "max.bg.threads";
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
    private static final ExecutorService BOOTSTRAP_EXECUTOR = Util.makeExecutor("Bootstrap");
    private static final ExecutorService BACKGROUND_EXECUTOR = Util.makeExecutor("Main");
    private static final ExecutorService IO_POOL = Util.makeIoExecutor();
    public static LongSupplier timeSource = System::nanoTime;
    public static final Ticker TICKER = new Ticker(){

        public long read() {
            return timeSource.getAsLong();
        }
    };
    public static final UUID NIL_UUID = new UUID(0L, 0L);
    public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders().stream().filter(fileSystemProvider -> fileSystemProvider.getScheme().equalsIgnoreCase("jar")).findFirst().orElseThrow(() -> new IllegalStateException("No jar file system provider found"));
    private static Consumer<String> thePauser = string -> {};

    public static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <T extends Comparable<T>> String getPropertyName(Property<T> property, Object object) {
        return property.getName((Comparable)object);
    }

    public static String makeDescriptionId(String string, @Nullable ResourceLocation resourceLocation) {
        if (resourceLocation == null) {
            return string + ".unregistered_sadface";
        }
        return string + "." + resourceLocation.getNamespace() + "." + resourceLocation.getPath().replace('/', '.');
    }

    public static long getMillis() {
        return Util.getNanos() / 1000000L;
    }

    public static long getNanos() {
        return timeSource.getAsLong();
    }

    public static long getEpochMillis() {
        return Instant.now().toEpochMilli();
    }

    private static ExecutorService makeExecutor(String string) {
        int n = Mth.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, Util.getMaxThreads());
        Object object = n <= 0 ? MoreExecutors.newDirectExecutorService() : new ForkJoinPool(n, forkJoinPool -> {
            ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(forkJoinPool){

                @Override
                protected void onTermination(Throwable throwable) {
                    if (throwable != null) {
                        LOGGER.warn("{} died", (Object)this.getName(), (Object)throwable);
                    } else {
                        LOGGER.debug("{} shutdown", (Object)this.getName());
                    }
                    super.onTermination(throwable);
                }
            };
            forkJoinWorkerThread.setName("Worker-" + string + "-" + WORKER_COUNT.getAndIncrement());
            return forkJoinWorkerThread;
        }, Util::onThreadException, true);
        return object;
    }

    private static int getMaxThreads() {
        String string = System.getProperty(MAX_THREADS_SYSTEM_PROPERTY);
        if (string != null) {
            try {
                int n = Integer.parseInt(string);
                if (n >= 1 && n <= 255) {
                    return n;
                }
                LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{MAX_THREADS_SYSTEM_PROPERTY, string, 255});
            }
            catch (NumberFormatException numberFormatException) {
                LOGGER.error("Could not parse {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{MAX_THREADS_SYSTEM_PROPERTY, string, 255});
            }
        }
        return 255;
    }

    public static ExecutorService bootstrapExecutor() {
        return BOOTSTRAP_EXECUTOR;
    }

    public static ExecutorService backgroundExecutor() {
        return BACKGROUND_EXECUTOR;
    }

    public static ExecutorService ioPool() {
        return IO_POOL;
    }

    public static void shutdownExecutors() {
        Util.shutdownExecutor(BACKGROUND_EXECUTOR);
        Util.shutdownExecutor(IO_POOL);
    }

    private static void shutdownExecutor(ExecutorService executorService) {
        boolean bl;
        executorService.shutdown();
        try {
            bl = executorService.awaitTermination(3L, TimeUnit.SECONDS);
        }
        catch (InterruptedException interruptedException) {
            bl = false;
        }
        if (!bl) {
            executorService.shutdownNow();
        }
    }

    private static ExecutorService makeIoExecutor() {
        return Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("IO-Worker-" + WORKER_COUNT.getAndIncrement());
            thread.setUncaughtExceptionHandler(Util::onThreadException);
            return thread;
        });
    }

    public static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
        CompletableFuture completableFuture = new CompletableFuture();
        completableFuture.completeExceptionally(throwable);
        return completableFuture;
    }

    public static void throwAsRuntime(Throwable throwable) {
        throw throwable instanceof RuntimeException ? (RuntimeException)throwable : new RuntimeException(throwable);
    }

    private static void onThreadException(Thread thread, Throwable throwable) {
        Util.pauseInIde(throwable);
        if (throwable instanceof CompletionException) {
            throwable = throwable.getCause();
        }
        if (throwable instanceof ReportedException) {
            Bootstrap.realStdoutPrintln(((ReportedException)throwable).getReport().getFriendlyReport());
            System.exit(-1);
        }
        LOGGER.error(String.format("Caught exception in thread %s", thread), throwable);
    }

    @Nullable
    public static Type<?> fetchChoiceType(DSL.TypeReference typeReference, String string) {
        if (!SharedConstants.CHECK_DATA_FIXER_SCHEMA) {
            return null;
        }
        return Util.doFetchChoiceType(typeReference, string);
    }

    @Nullable
    private static Type<?> doFetchChoiceType(DSL.TypeReference typeReference, String string) {
        Type type;
        block2: {
            type = null;
            try {
                type = DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey((int)SharedConstants.getCurrentVersion().getWorldVersion())).getChoiceType(typeReference, string);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.error("No data fixer registered for {}", (Object)string);
                if (!SharedConstants.IS_RUNNING_IN_IDE) break block2;
                throw illegalArgumentException;
            }
        }
        return type;
    }

    public static Runnable wrapThreadWithTaskName(String string, Runnable runnable) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return () -> {
                Thread thread = Thread.currentThread();
                String string2 = thread.getName();
                thread.setName(string);
                try {
                    runnable.run();
                }
                finally {
                    thread.setName(string2);
                }
            };
        }
        return runnable;
    }

    public static <V> Supplier<V> wrapThreadWithTaskName(String string, Supplier<V> supplier) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return () -> {
                Thread thread = Thread.currentThread();
                String string2 = thread.getName();
                thread.setName(string);
                try {
                    Object t = supplier.get();
                    return t;
                }
                finally {
                    thread.setName(string2);
                }
            };
        }
        return supplier;
    }

    public static OS getPlatform() {
        String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (string.contains("win")) {
            return OS.WINDOWS;
        }
        if (string.contains("mac")) {
            return OS.OSX;
        }
        if (string.contains("solaris")) {
            return OS.SOLARIS;
        }
        if (string.contains("sunos")) {
            return OS.SOLARIS;
        }
        if (string.contains("linux")) {
            return OS.LINUX;
        }
        if (string.contains("unix")) {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }

    public static Stream<String> getVmArguments() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return runtimeMXBean.getInputArguments().stream().filter(string -> string.startsWith("-X"));
    }

    public static <T> T lastOf(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> T findNextInIterable(Iterable<T> iterable, @Nullable T t) {
        Iterator<T> iterator = iterable.iterator();
        T t2 = iterator.next();
        if (t != null) {
            T t3 = t2;
            while (true) {
                if (t3 == t) {
                    if (!iterator.hasNext()) break;
                    return iterator.next();
                }
                if (!iterator.hasNext()) continue;
                t3 = iterator.next();
            }
        }
        return t2;
    }

    public static <T> T findPreviousInIterable(Iterable<T> iterable, @Nullable T t) {
        Iterator<T> iterator = iterable.iterator();
        T t2 = null;
        while (iterator.hasNext()) {
            T t3 = iterator.next();
            if (t3 == t) {
                if (t2 != null) break;
                t2 = (T)(iterator.hasNext() ? Iterators.getLast(iterator) : t);
                break;
            }
            t2 = t3;
        }
        return t2;
    }

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T t, Consumer<T> consumer) {
        consumer.accept(t);
        return t;
    }

    public static <K> Hash.Strategy<K> identityStrategy() {
        return IdentityStrategy.INSTANCE;
    }

    public static <V> CompletableFuture<List<V>> sequence(List<? extends CompletableFuture<V>> list) {
        if (list.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }
        if (list.size() == 1) {
            return list.get(0).thenApply((Function<Object, List>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, of(java.lang.Object ), (Ljava/lang/Object;)Ljava/util/List;)());
        }
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
        return completableFuture.thenApply(void_ -> list.stream().map(CompletableFuture::join).toList());
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFast(List<? extends CompletableFuture<? extends V>> list) {
        ArrayList arrayList = Lists.newArrayListWithCapacity((int)list.size());
        CompletableFuture[] completableFutureArray = new CompletableFuture[list.size()];
        CompletableFuture completableFuture = new CompletableFuture();
        list.forEach(completableFuture2 -> {
            int n = arrayList.size();
            arrayList.add(null);
            completableFutureArray[n] = completableFuture2.whenComplete((object, throwable) -> {
                if (throwable != null) {
                    completableFuture.completeExceptionally((Throwable)throwable);
                } else {
                    arrayList.set(n, object);
                }
            });
        });
        return CompletableFuture.allOf(completableFutureArray).applyToEither((CompletionStage)completableFuture, void_ -> arrayList);
    }

    public static <T> Optional<T> ifElse(Optional<T> optional, Consumer<T> consumer, Runnable runnable) {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        } else {
            runnable.run();
        }
        return optional;
    }

    public static Runnable name(Runnable runnable, Supplier<String> supplier) {
        return runnable;
    }

    public static void logAndPauseIfInIde(String string) {
        LOGGER.error(string);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            Util.doPause(string);
        }
    }

    public static void logAndPauseIfInIde(String string, Throwable throwable) {
        LOGGER.error(string, throwable);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            Util.doPause(string);
        }
    }

    public static <T extends Throwable> T pauseInIde(T t) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", t);
            Util.doPause(t.getMessage());
        }
        return t;
    }

    public static void setPause(Consumer<String> consumer) {
        thePauser = consumer;
    }

    private static void doPause(String string) {
        boolean bl;
        Instant instant = Instant.now();
        LOGGER.warn("Did you remember to set a breakpoint here?");
        boolean bl2 = bl = Duration.between(instant, Instant.now()).toMillis() > 500L;
        if (!bl) {
            thePauser.accept(string);
        }
    }

    public static String describeError(Throwable throwable) {
        if (throwable.getCause() != null) {
            return Util.describeError(throwable.getCause());
        }
        if (throwable.getMessage() != null) {
            return throwable.getMessage();
        }
        return throwable.toString();
    }

    public static <T> T getRandom(T[] TArray, Random random) {
        return TArray[random.nextInt(TArray.length)];
    }

    public static int getRandom(int[] nArray, Random random) {
        return nArray[random.nextInt(nArray.length)];
    }

    public static <T> T getRandom(List<T> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }

    public static <T> Optional<T> getRandomSafe(List<T> list, Random random) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Util.getRandom(list, random));
    }

    private static BooleanSupplier createRenamer(final Path path, final Path path2) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.move(path, path2, new CopyOption[0]);
                    return true;
                }
                catch (IOException iOException) {
                    LOGGER.error("Failed to rename", (Throwable)iOException);
                    return false;
                }
            }

            public String toString() {
                return "rename " + path + " to " + path2;
            }
        };
    }

    private static BooleanSupplier createDeleter(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.deleteIfExists(path);
                    return true;
                }
                catch (IOException iOException) {
                    LOGGER.warn("Failed to delete", (Throwable)iOException);
                    return false;
                }
            }

            public String toString() {
                return "delete old " + path;
            }
        };
    }

    private static BooleanSupplier createFileDeletedCheck(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return !Files.exists(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + path + " is deleted";
            }
        };
    }

    private static BooleanSupplier createFileCreatedCheck(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return Files.isRegularFile(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + path + " is present";
            }
        };
    }

    private static boolean executeInSequence(BooleanSupplier ... booleanSupplierArray) {
        for (BooleanSupplier booleanSupplier : booleanSupplierArray) {
            if (booleanSupplier.getAsBoolean()) continue;
            LOGGER.warn("Failed to execute {}", (Object)booleanSupplier);
            return false;
        }
        return true;
    }

    private static boolean runWithRetries(int n, String string, BooleanSupplier ... booleanSupplierArray) {
        for (int i = 0; i < n; ++i) {
            if (Util.executeInSequence(booleanSupplierArray)) {
                return true;
            }
            LOGGER.error("Failed to {}, retrying {}/{}", new Object[]{string, i, n});
        }
        LOGGER.error("Failed to {}, aborting, progress might be lost", (Object)string);
        return false;
    }

    public static void safeReplaceFile(File file, File file2, File file3) {
        Util.safeReplaceFile(file.toPath(), file2.toPath(), file3.toPath());
    }

    public static void safeReplaceFile(Path path, Path path2, Path path3) {
        Util.safeReplaceOrMoveFile(path, path2, path3, false);
    }

    public static void safeReplaceOrMoveFile(File file, File file2, File file3, boolean bl) {
        Util.safeReplaceOrMoveFile(file.toPath(), file2.toPath(), file3.toPath(), bl);
    }

    public static void safeReplaceOrMoveFile(Path path, Path path2, Path path3, boolean bl) {
        int n = 10;
        if (Files.exists(path, new LinkOption[0]) && !Util.runWithRetries(10, "create backup " + path3, Util.createDeleter(path3), Util.createRenamer(path, path3), Util.createFileCreatedCheck(path3))) {
            return;
        }
        if (!Util.runWithRetries(10, "remove old " + path, Util.createDeleter(path), Util.createFileDeletedCheck(path))) {
            return;
        }
        if (!Util.runWithRetries(10, "replace " + path + " with " + path2, Util.createRenamer(path2, path), Util.createFileCreatedCheck(path)) && !bl) {
            Util.runWithRetries(10, "restore " + path + " from " + path3, Util.createRenamer(path3, path), Util.createFileCreatedCheck(path));
        }
    }

    public static int offsetByCodepoints(String string, int n, int n2) {
        int n3 = string.length();
        if (n2 >= 0) {
            for (int i = 0; n < n3 && i < n2; ++i) {
                if (!Character.isHighSurrogate(string.charAt(n++)) || n >= n3 || !Character.isLowSurrogate(string.charAt(n))) continue;
                ++n;
            }
        } else {
            for (int i = n2; n > 0 && i < 0; ++i) {
                if (!Character.isLowSurrogate(string.charAt(--n)) || n <= 0 || !Character.isHighSurrogate(string.charAt(n - 1))) continue;
                --n;
            }
        }
        return n;
    }

    public static Consumer<String> prefix(String string, Consumer<String> consumer) {
        return string2 -> consumer.accept(string + string2);
    }

    public static DataResult<int[]> fixedSize(IntStream intStream, int n) {
        int[] nArray = intStream.limit(n + 1).toArray();
        if (nArray.length != n) {
            String string = "Input is not a list of " + n + " ints";
            if (nArray.length >= n) {
                return DataResult.error((String)string, (Object)Arrays.copyOf(nArray, n));
            }
            return DataResult.error((String)string);
        }
        return DataResult.success((Object)nArray);
    }

    public static <T> DataResult<List<T>> fixedSize(List<T> list, int n) {
        if (list.size() != n) {
            String string = "Input is not a list of " + n + " elements";
            if (list.size() >= n) {
                return DataResult.error((String)string, list.subList(0, n));
            }
            return DataResult.error((String)string);
        }
        return DataResult.success(list);
    }

    public static void startTimerHackThread() {
        Thread thread = new Thread("Timer hack thread"){

            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(Integer.MAX_VALUE);
                    }
                }
                catch (InterruptedException interruptedException) {
                    LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                    return;
                }
            }
        };
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    public static void copyBetweenDirs(Path path, Path path2, Path path3) throws IOException {
        Path path4 = path.relativize(path3);
        Path path5 = path2.resolve(path4);
        Files.copy(path3, path5, new CopyOption[0]);
    }

    public static String sanitizeName(String string, CharPredicate charPredicate) {
        return string.toLowerCase(Locale.ROOT).chars().mapToObj(n -> charPredicate.test((char)n) ? Character.toString((char)n) : "_").collect(Collectors.joining());
    }

    public static <T, R> Function<T, R> memoize(final Function<T, R> function) {
        return new Function<T, R>(){
            private final Map<T, R> cache = Maps.newHashMap();

            @Override
            public R apply(T t) {
                return this.cache.computeIfAbsent(t, function);
            }

            public String toString() {
                return "memoize/1[function=" + function + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> biFunction) {
        return new BiFunction<T, U, R>(){
            private final Map<Pair<T, U>, R> cache = Maps.newHashMap();

            @Override
            public R apply(T t, U u) {
                return this.cache.computeIfAbsent(Pair.of(t, u), pair -> biFunction.apply(pair.getFirst(), pair.getSecond()));
            }

            public String toString() {
                return "memoize/2[function=" + biFunction + ", size=" + this.cache.size() + "]";
            }
        };
    }

    /*
     * Uses 'sealed' constructs - enablewith --sealed true
     */
    public static enum OS {
        LINUX("linux"),
        SOLARIS("solaris"),
        WINDOWS("windows"){

            @Override
            protected String[] getOpenUrlArguments(URL uRL) {
                return new String[]{"rundll32", "url.dll,FileProtocolHandler", uRL.toString()};
            }
        }
        ,
        OSX("mac"){

            @Override
            protected String[] getOpenUrlArguments(URL uRL) {
                return new String[]{"open", uRL.toString()};
            }
        }
        ,
        UNKNOWN("unknown");

        private final String telemetryName;

        OS(String string2) {
            this.telemetryName = string2;
        }

        public void openUrl(URL uRL) {
            try {
                Process process = AccessController.doPrivileged(() -> Runtime.getRuntime().exec(this.getOpenUrlArguments(uRL)));
                for (String string : IOUtils.readLines((InputStream)process.getErrorStream())) {
                    LOGGER.error(string);
                }
                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
            }
            catch (IOException | PrivilegedActionException exception) {
                LOGGER.error("Couldn't open url '{}'", (Object)uRL, (Object)exception);
            }
        }

        public void openUri(URI uRI) {
            try {
                this.openUrl(uRI.toURL());
            }
            catch (MalformedURLException malformedURLException) {
                LOGGER.error("Couldn't open uri '{}'", (Object)uRI, (Object)malformedURLException);
            }
        }

        public void openFile(File file) {
            try {
                this.openUrl(file.toURI().toURL());
            }
            catch (MalformedURLException malformedURLException) {
                LOGGER.error("Couldn't open file '{}'", (Object)file, (Object)malformedURLException);
            }
        }

        protected String[] getOpenUrlArguments(URL uRL) {
            String string = uRL.toString();
            if ("file".equals(uRL.getProtocol())) {
                string = string.replace("file:", "file://");
            }
            return new String[]{"xdg-open", string};
        }

        public void openUri(String string) {
            try {
                this.openUrl(new URI(string).toURL());
            }
            catch (IllegalArgumentException | MalformedURLException | URISyntaxException exception) {
                LOGGER.error("Couldn't open uri '{}'", (Object)string, (Object)exception);
            }
        }

        public String telemetryName() {
            return this.telemetryName;
        }
    }

    static enum IdentityStrategy implements Hash.Strategy<Object>
    {
        INSTANCE;


        public int hashCode(Object object) {
            return System.identityHashCode(object);
        }

        public boolean equals(Object object, Object object2) {
            return object == object2;
        }
    }
}

