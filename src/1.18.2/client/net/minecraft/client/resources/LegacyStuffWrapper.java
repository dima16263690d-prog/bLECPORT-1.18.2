/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class LegacyStuffWrapper {
    @Deprecated
    public static int[] getPixels(ResourceManager resourceManager, ResourceLocation resourceLocation) throws IOException {
        try (Resource resource = resourceManager.getResource(resourceLocation);){
            NativeImage nativeImage = NativeImage.read(resource.getInputStream());
            try {
                int[] nArray = nativeImage.makePixelArray();
                if (nativeImage != null) {
                    nativeImage.close();
                }
                return nArray;
            }
            catch (Throwable throwable) {
                if (nativeImage != null) {
                    try {
                        nativeImage.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }
}

