/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.slf4j.Logger;

public class VanillaPackResources
implements PackResources,
ResourceProvider {
    @Nullable
    public static Path generatedDir;
    private static final Logger LOGGER;
    public static Class<?> clientObject;
    private static final Map<PackType, Path> ROOT_DIR_BY_TYPE;
    public final PackMetadataSection packMetadata;
    public final Set<String> namespaces;

    private static Path safeGetPath(URI uRI) throws IOException {
        try {
            return Paths.get(uRI);
        }
        catch (FileSystemNotFoundException fileSystemNotFoundException) {
        }
        catch (Throwable throwable) {
            LOGGER.warn("Unable to get path for: {}", (Object)uRI, (Object)throwable);
        }
        try {
            FileSystems.newFileSystem(uRI, Collections.emptyMap());
        }
        catch (FileSystemAlreadyExistsException fileSystemAlreadyExistsException) {
            // empty catch block
        }
        return Paths.get(uRI);
    }

    public VanillaPackResources(PackMetadataSection packMetadataSection, String ... stringArray) {
        this.packMetadata = packMetadataSection;
        this.namespaces = ImmutableSet.copyOf((Object[])stringArray);
    }

    @Override
    public InputStream getRootResource(String string) throws IOException {
        Path path;
        if (string.contains("/") || string.contains("\\")) {
            throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
        }
        if (generatedDir != null && Files.exists(path = generatedDir.resolve(string), new LinkOption[0])) {
            return Files.newInputStream(path, new OpenOption[0]);
        }
        return this.getResourceAsStream(string);
    }

    @Override
    public InputStream getResource(PackType packType, ResourceLocation resourceLocation) throws IOException {
        InputStream inputStream = this.getResourceAsStream(packType, resourceLocation);
        if (inputStream != null) {
            return inputStream;
        }
        throw new FileNotFoundException(resourceLocation.getPath());
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType packType, String string, String string2, int n, Predicate<String> predicate) {
        Object object;
        HashSet hashSet = Sets.newHashSet();
        if (generatedDir != null) {
            try {
                VanillaPackResources.getResources(hashSet, n, string, generatedDir.resolve(packType.getDirectory()), string2, predicate);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            if (packType == PackType.CLIENT_RESOURCES) {
                object = null;
                try {
                    object = clientObject.getClassLoader().getResources(packType.getDirectory() + "/");
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                while (object != null && object.hasMoreElements()) {
                    try {
                        URI uRI = ((URL)object.nextElement()).toURI();
                        if (!"file".equals(uRI.getScheme())) continue;
                        VanillaPackResources.getResources(hashSet, n, string, Paths.get(uRI), string2, predicate);
                    }
                    catch (IOException | URISyntaxException exception) {}
                }
            }
        }
        try {
            object = ROOT_DIR_BY_TYPE.get((Object)packType);
            if (object != null) {
                VanillaPackResources.getResources(hashSet, n, string, (Path)object, string2, predicate);
            } else {
                LOGGER.error("Can't access assets root for type: {}", (Object)packType);
            }
        }
        catch (FileNotFoundException | NoSuchFileException iOException) {
        }
        catch (IOException iOException) {
            LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)iOException);
        }
        return hashSet;
    }

    private static void getResources(Collection<ResourceLocation> collection, int n, String string, Path path3, String string2, Predicate<String> predicate) throws IOException {
        Path path4 = path3.resolve(string);
        try (Stream<Path> stream = Files.walk(path4.resolve(string2), n, new FileVisitOption[0]);){
            stream.filter(path -> !path.endsWith(".mcmeta") && Files.isRegularFile(path, new LinkOption[0]) && predicate.test(path.getFileName().toString())).map(path2 -> new ResourceLocation(string, path4.relativize((Path)path2).toString().replaceAll("\\\\", "/"))).forEach(collection::add);
        }
    }

    @Nullable
    protected InputStream getResourceAsStream(PackType packType, ResourceLocation resourceLocation) {
        Object object;
        String string = VanillaPackResources.createPath(packType, resourceLocation);
        if (generatedDir != null && Files.exists((Path)(object = generatedDir.resolve(packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath())), new LinkOption[0])) {
            try {
                return Files.newInputStream((Path)object, new OpenOption[0]);
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        try {
            object = VanillaPackResources.class.getResource(string);
            if (VanillaPackResources.isResourceUrlValid(string, (URL)object)) {
                return ((URL)object).openStream();
            }
        }
        catch (IOException iOException) {
            return VanillaPackResources.class.getResourceAsStream(string);
        }
        return null;
    }

    private static String createPath(PackType packType, ResourceLocation resourceLocation) {
        return "/" + packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath();
    }

    private static boolean isResourceUrlValid(String string, @Nullable URL uRL) throws IOException {
        return uRL != null && (uRL.getProtocol().equals("jar") || FolderPackResources.validatePath(new File(uRL.getFile()), string));
    }

    @Nullable
    protected InputStream getResourceAsStream(String string) {
        return VanillaPackResources.class.getResourceAsStream("/" + string);
    }

    @Override
    public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
        Object object;
        String string = VanillaPackResources.createPath(packType, resourceLocation);
        if (generatedDir != null && Files.exists((Path)(object = generatedDir.resolve(packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath())), new LinkOption[0])) {
            return true;
        }
        try {
            object = VanillaPackResources.class.getResource(string);
            return VanillaPackResources.isResourceUrlValid(string, (URL)object);
        }
        catch (IOException iOException) {
            return false;
        }
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        return this.namespaces;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    @Nullable
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
        try (InputStream inputStream = this.getRootResource("pack.mcmeta");){
            T t2;
            if (inputStream != null && (t2 = AbstractPackResources.getMetadataFromStream(metadataSectionSerializer, inputStream)) != null) {
                T t = t2;
                return t;
            }
        }
        catch (FileNotFoundException | RuntimeException exception) {
            // empty catch block
        }
        if (metadataSectionSerializer != PackMetadataSection.SERIALIZER) return null;
        return (T)this.packMetadata;
    }

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public void close() {
    }

    @Override
    public Resource getResource(final ResourceLocation resourceLocation) throws IOException {
        return new Resource(){
            @Nullable
            InputStream inputStream;

            @Override
            public void close() throws IOException {
                if (this.inputStream != null) {
                    this.inputStream.close();
                }
            }

            @Override
            public ResourceLocation getLocation() {
                return resourceLocation;
            }

            @Override
            public InputStream getInputStream() {
                try {
                    this.inputStream = VanillaPackResources.this.getResource(PackType.CLIENT_RESOURCES, resourceLocation);
                }
                catch (IOException iOException) {
                    throw new UncheckedIOException("Could not get client resource from vanilla pack", iOException);
                }
                return this.inputStream;
            }

            @Override
            public boolean hasMetadata() {
                return false;
            }

            @Override
            @Nullable
            public <T> T getMetadata(MetadataSectionSerializer<T> metadataSectionSerializer) {
                return null;
            }

            @Override
            public String getSourceName() {
                return resourceLocation.toString();
            }
        };
    }

    static {
        LOGGER = LogUtils.getLogger();
        ROOT_DIR_BY_TYPE = (Map)Util.make(() -> {
            Class<VanillaPackResources> clazz = VanillaPackResources.class;
            synchronized (VanillaPackResources.class) {
                ImmutableMap.Builder builder = ImmutableMap.builder();
                for (PackType packType : PackType.values()) {
                    String string = "/" + packType.getDirectory() + "/.mcassetsroot";
                    URL uRL = VanillaPackResources.class.getResource(string);
                    if (uRL == null) {
                        LOGGER.error("File {} does not exist in classpath", (Object)string);
                        continue;
                    }
                    try {
                        URI uRI = uRL.toURI();
                        String string2 = uRI.getScheme();
                        if (!"jar".equals(string2) && !"file".equals(string2)) {
                            LOGGER.warn("Assets URL '{}' uses unexpected schema", (Object)uRI);
                        }
                        Path path = VanillaPackResources.safeGetPath(uRI);
                        builder.put((Object)packType, (Object)path.getParent());
                    }
                    catch (Exception exception) {
                        LOGGER.error("Couldn't resolve path to vanilla assets", (Throwable)exception);
                    }
                }
                // ** MonitorExit[var0] (shouldn't be in output)
                return builder.build();
            }
        });
    }
}

