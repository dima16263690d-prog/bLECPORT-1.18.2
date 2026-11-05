/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs.resources;

import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

@FunctionalInterface
public interface ResourceProvider {
    public Resource getResource(ResourceLocation var1) throws IOException;
}

