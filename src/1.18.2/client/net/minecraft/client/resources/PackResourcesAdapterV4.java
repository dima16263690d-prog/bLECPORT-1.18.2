/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 */
package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.world.level.block.state.properties.ChestType;

public class PackResourcesAdapterV4
implements PackResources {
    private static final Map<String, Pair<ChestType, ResourceLocation>> CHESTS = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("textures/entity/chest/normal_left.png", new Pair((Object)ChestType.LEFT, (Object)new ResourceLocation("textures/entity/chest/normal_double.png")));
        hashMap.put("textures/entity/chest/normal_right.png", new Pair((Object)ChestType.RIGHT, (Object)new ResourceLocation("textures/entity/chest/normal_double.png")));
        hashMap.put("textures/entity/chest/normal.png", new Pair((Object)ChestType.SINGLE, (Object)new ResourceLocation("textures/entity/chest/normal.png")));
        hashMap.put("textures/entity/chest/trapped_left.png", new Pair((Object)ChestType.LEFT, (Object)new ResourceLocation("textures/entity/chest/trapped_double.png")));
        hashMap.put("textures/entity/chest/trapped_right.png", new Pair((Object)ChestType.RIGHT, (Object)new ResourceLocation("textures/entity/chest/trapped_double.png")));
        hashMap.put("textures/entity/chest/trapped.png", new Pair((Object)ChestType.SINGLE, (Object)new ResourceLocation("textures/entity/chest/trapped.png")));
        hashMap.put("textures/entity/chest/christmas_left.png", new Pair((Object)ChestType.LEFT, (Object)new ResourceLocation("textures/entity/chest/christmas_double.png")));
        hashMap.put("textures/entity/chest/christmas_right.png", new Pair((Object)ChestType.RIGHT, (Object)new ResourceLocation("textures/entity/chest/christmas_double.png")));
        hashMap.put("textures/entity/chest/christmas.png", new Pair((Object)ChestType.SINGLE, (Object)new ResourceLocation("textures/entity/chest/christmas.png")));
        hashMap.put("textures/entity/chest/ender.png", new Pair((Object)ChestType.SINGLE, (Object)new ResourceLocation("textures/entity/chest/ender.png")));
    });
    private static final List<String> PATTERNS = Lists.newArrayList((Object[])new String[]{"base", "border", "bricks", "circle", "creeper", "cross", "curly_border", "diagonal_left", "diagonal_right", "diagonal_up_left", "diagonal_up_right", "flower", "globe", "gradient", "gradient_up", "half_horizontal", "half_horizontal_bottom", "half_vertical", "half_vertical_right", "mojang", "rhombus", "skull", "small_stripes", "square_bottom_left", "square_bottom_right", "square_top_left", "square_top_right", "straight_cross", "stripe_bottom", "stripe_center", "stripe_downleft", "stripe_downright", "stripe_left", "stripe_middle", "stripe_right", "stripe_top", "triangle_bottom", "triangle_top", "triangles_bottom", "triangles_top"});
    private static final Set<String> SHIELDS = PATTERNS.stream().map(string -> "textures/entity/shield/" + string + ".png").collect(Collectors.toSet());
    private static final Set<String> BANNERS = PATTERNS.stream().map(string -> "textures/entity/banner/" + string + ".png").collect(Collectors.toSet());
    public static final ResourceLocation SHIELD_BASE = new ResourceLocation("textures/entity/shield_base.png");
    public static final ResourceLocation BANNER_BASE = new ResourceLocation("textures/entity/banner_base.png");
    public static final int DEFAULT_CHEST_SIZE = 64;
    public static final int DEFAULT_SHIELD_SIZE = 64;
    public static final int DEFAULT_BANNER_SIZE = 64;
    public static final ResourceLocation OLD_IRON_GOLEM_LOCATION = new ResourceLocation("textures/entity/iron_golem.png");
    public static final String NEW_IRON_GOLEM_PATH = "textures/entity/iron_golem/iron_golem.png";
    private final PackResources pack;

    public PackResourcesAdapterV4(PackResources packResources) {
        this.pack = packResources;
    }

    @Override
    public InputStream getRootResource(String string) throws IOException {
        return this.pack.getRootResource(string);
    }

    @Override
    public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
        if (!"minecraft".equals(resourceLocation.getNamespace())) {
            return this.pack.hasResource(packType, resourceLocation);
        }
        String string = resourceLocation.getPath();
        if ("textures/misc/enchanted_item_glint.png".equals(string)) {
            return false;
        }
        if (NEW_IRON_GOLEM_PATH.equals(string)) {
            return this.pack.hasResource(packType, OLD_IRON_GOLEM_LOCATION);
        }
        if ("textures/entity/conduit/wind.png".equals(string) || "textures/entity/conduit/wind_vertical.png".equals(string)) {
            return false;
        }
        if (SHIELDS.contains(string)) {
            return this.pack.hasResource(packType, SHIELD_BASE) && this.pack.hasResource(packType, resourceLocation);
        }
        if (BANNERS.contains(string)) {
            return this.pack.hasResource(packType, BANNER_BASE) && this.pack.hasResource(packType, resourceLocation);
        }
        Pair<ChestType, ResourceLocation> pair = CHESTS.get(string);
        if (pair != null && this.pack.hasResource(packType, (ResourceLocation)pair.getSecond())) {
            return true;
        }
        return this.pack.hasResource(packType, resourceLocation);
    }

    @Override
    public InputStream getResource(PackType packType, ResourceLocation resourceLocation) throws IOException {
        if (!"minecraft".equals(resourceLocation.getNamespace())) {
            return this.pack.getResource(packType, resourceLocation);
        }
        String string = resourceLocation.getPath();
        if (NEW_IRON_GOLEM_PATH.equals(string)) {
            return this.pack.getResource(packType, OLD_IRON_GOLEM_LOCATION);
        }
        if (SHIELDS.contains(string)) {
            InputStream inputStream = PackResourcesAdapterV4.fixPattern(this.pack.getResource(packType, SHIELD_BASE), this.pack.getResource(packType, resourceLocation), 64, 2, 2, 12, 22);
            if (inputStream != null) {
                return inputStream;
            }
        } else if (BANNERS.contains(string)) {
            InputStream inputStream = PackResourcesAdapterV4.fixPattern(this.pack.getResource(packType, BANNER_BASE), this.pack.getResource(packType, resourceLocation), 64, 0, 0, 42, 41);
            if (inputStream != null) {
                return inputStream;
            }
        } else {
            if ("textures/entity/enderdragon/dragon.png".equals(string) || "textures/entity/enderdragon/dragon_exploding.png".equals(string)) {
                try (NativeImage nativeImage = NativeImage.read(this.pack.getResource(packType, resourceLocation));){
                    int n = nativeImage.getWidth() / 256;
                    for (int i = 88 * n; i < 200 * n; ++i) {
                        for (int j = 56 * n; j < 112 * n; ++j) {
                            nativeImage.setPixelRGBA(j, i, 0);
                        }
                    }
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(nativeImage.asByteArray());
                    return byteArrayInputStream;
                }
            }
            if ("textures/entity/conduit/closed_eye.png".equals(string) || "textures/entity/conduit/open_eye.png".equals(string)) {
                return PackResourcesAdapterV4.fixConduitEyeTexture(this.pack.getResource(packType, resourceLocation));
            }
            Pair<ChestType, ResourceLocation> pair = CHESTS.get(string);
            if (pair != null) {
                ChestType chestType = (ChestType)pair.getFirst();
                InputStream inputStream = this.pack.getResource(packType, (ResourceLocation)pair.getSecond());
                if (chestType == ChestType.SINGLE) {
                    return PackResourcesAdapterV4.fixSingleChest(inputStream);
                }
                if (chestType == ChestType.LEFT) {
                    return PackResourcesAdapterV4.fixLeftChest(inputStream);
                }
                if (chestType == ChestType.RIGHT) {
                    return PackResourcesAdapterV4.fixRightChest(inputStream);
                }
            }
        }
        return this.pack.getResource(packType, resourceLocation);
    }

    /*
     * Loose catch block
     */
    @Nullable
    public static InputStream fixPattern(InputStream inputStream, InputStream inputStream2, int n, int n2, int n3, int n4, int n5) throws IOException {
        try (NativeImage nativeImage = NativeImage.read(inputStream);
             NativeImage nativeImage2 = NativeImage.read(inputStream2);){
            block20: {
                int n6 = nativeImage.getWidth();
                int n7 = nativeImage.getHeight();
                if (n6 != nativeImage2.getWidth()) break block20;
                if (n7 != nativeImage2.getHeight()) break;
                NativeImage nativeImage3 = new NativeImage(n6, n7, true);
                try {
                    int n8 = n6 / n;
                    for (int i = n3 * n8; i < n5 * n8; ++i) {
                        for (int j = n2 * n8; j < n4 * n8; ++j) {
                            int n9 = NativeImage.getR(nativeImage2.getPixelRGBA(j, i));
                            int n10 = nativeImage.getPixelRGBA(j, i);
                            nativeImage3.setPixelRGBA(j, i, NativeImage.combine(n9, NativeImage.getB(n10), NativeImage.getG(n10), NativeImage.getR(n10)));
                        }
                    }
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(nativeImage3.asByteArray());
                    nativeImage3.close();
                    return byteArrayInputStream;
                }
                catch (Throwable throwable) {
                    try {
                        nativeImage3.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
            }
            {
                catch (Throwable throwable) {
                    throw throwable;
                }
            }
        }
    }

    public static InputStream fixConduitEyeTexture(InputStream inputStream) throws IOException {
        try (NativeImage nativeImage = NativeImage.read(inputStream);){
            int n = nativeImage.getWidth();
            int n2 = nativeImage.getHeight();
            NativeImage nativeImage2 = new NativeImage(2 * n, 2 * n2, true);
            try {
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 0, 0, 0, 0, n, n2, 1, false, false);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(nativeImage2.asByteArray());
                nativeImage2.close();
                return byteArrayInputStream;
            }
            catch (Throwable throwable) {
                try {
                    nativeImage2.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
        }
    }

    public static InputStream fixLeftChest(InputStream inputStream) throws IOException {
        try (NativeImage nativeImage = NativeImage.read(inputStream);){
            int n = nativeImage.getWidth();
            int n2 = nativeImage.getHeight();
            NativeImage nativeImage2 = new NativeImage(n / 2, n2, true);
            try {
                int n3 = n2 / 64;
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 29, 0, 29, 0, 15, 14, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 59, 0, 14, 0, 15, 14, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 29, 14, 43, 14, 15, 5, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 44, 14, 29, 14, 14, 5, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 58, 14, 14, 14, 15, 5, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 29, 19, 29, 19, 15, 14, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 59, 19, 14, 19, 15, 14, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 29, 33, 43, 33, 15, 10, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 44, 33, 29, 33, 14, 10, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 58, 33, 14, 33, 15, 10, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 2, 0, 2, 0, 1, 1, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 4, 0, 1, 0, 1, 1, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 2, 1, 3, 1, 1, 4, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 3, 1, 2, 1, 1, 4, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 4, 1, 1, 1, 1, 4, n3, true, true);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(nativeImage2.asByteArray());
                nativeImage2.close();
                return byteArrayInputStream;
            }
            catch (Throwable throwable) {
                try {
                    nativeImage2.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
        }
    }

    public static InputStream fixRightChest(InputStream inputStream) throws IOException {
        try (NativeImage nativeImage = NativeImage.read(inputStream);){
            int n = nativeImage.getWidth();
            int n2 = nativeImage.getHeight();
            NativeImage nativeImage2 = new NativeImage(n / 2, n2, true);
            try {
                int n3 = n2 / 64;
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 14, 0, 29, 0, 15, 14, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 44, 0, 14, 0, 15, 14, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 0, 14, 0, 14, 14, 5, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 14, 14, 43, 14, 15, 5, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 73, 14, 14, 14, 15, 5, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 14, 19, 29, 19, 15, 14, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 44, 19, 14, 19, 15, 14, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 0, 33, 0, 33, 14, 10, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 14, 33, 43, 33, 15, 10, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 73, 33, 14, 33, 15, 10, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 1, 0, 2, 0, 1, 1, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 3, 0, 1, 0, 1, 1, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 0, 1, 0, 1, 1, 4, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 1, 1, 3, 1, 1, 4, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 5, 1, 1, 1, 1, 4, n3, true, true);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(nativeImage2.asByteArray());
                nativeImage2.close();
                return byteArrayInputStream;
            }
            catch (Throwable throwable) {
                try {
                    nativeImage2.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
        }
    }

    public static InputStream fixSingleChest(InputStream inputStream) throws IOException {
        try (NativeImage nativeImage = NativeImage.read(inputStream);){
            int n = nativeImage.getWidth();
            int n2 = nativeImage.getHeight();
            NativeImage nativeImage2 = new NativeImage(n, n2, true);
            try {
                int n3 = n2 / 64;
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 14, 0, 28, 0, 14, 14, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 28, 0, 14, 0, 14, 14, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 0, 14, 0, 14, 14, 5, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 14, 14, 42, 14, 14, 5, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 28, 14, 28, 14, 14, 5, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 42, 14, 14, 14, 14, 5, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 14, 19, 28, 19, 14, 14, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 28, 19, 14, 19, 14, 14, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 0, 33, 0, 33, 14, 10, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 14, 33, 42, 33, 14, 10, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 28, 33, 28, 33, 14, 10, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 42, 33, 14, 33, 14, 10, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 1, 0, 3, 0, 2, 1, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 3, 0, 1, 0, 2, 1, n3, false, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 0, 1, 0, 1, 1, 4, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 1, 1, 4, 1, 2, 4, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 3, 1, 3, 1, 1, 4, n3, true, true);
                PackResourcesAdapterV4.copyRect(nativeImage, nativeImage2, 4, 1, 1, 1, 2, 4, n3, true, true);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(nativeImage2.asByteArray());
                nativeImage2.close();
                return byteArrayInputStream;
            }
            catch (Throwable throwable) {
                try {
                    nativeImage2.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
        }
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType packType, String string, String string2, int n, Predicate<String> predicate) {
        return this.pack.getResources(packType, string, string2, n, predicate);
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        return this.pack.getNamespaces(packType);
    }

    @Override
    @Nullable
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
        return this.pack.getMetadataSection(metadataSectionSerializer);
    }

    @Override
    public String getName() {
        return this.pack.getName();
    }

    @Override
    public void close() {
        this.pack.close();
    }

    private static void copyRect(NativeImage nativeImage, NativeImage nativeImage2, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, boolean bl2) {
        n6 *= n7;
        n5 *= n7;
        n3 *= n7;
        n4 *= n7;
        n *= n7;
        n2 *= n7;
        for (int i = 0; i < n6; ++i) {
            for (int j = 0; j < n5; ++j) {
                nativeImage2.setPixelRGBA(n3 + j, n4 + i, nativeImage.getPixelRGBA(n + (bl ? n5 - 1 - j : j), n2 + (bl2 ? n6 - 1 - i : i)));
            }
        }
    }
}

