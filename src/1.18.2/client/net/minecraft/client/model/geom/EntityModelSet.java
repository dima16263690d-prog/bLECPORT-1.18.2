/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.client.model.geom;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class EntityModelSet
implements ResourceManagerReloadListener {
    private Map<ModelLayerLocation, LayerDefinition> roots = ImmutableMap.of();

    public ModelPart bakeLayer(ModelLayerLocation modelLayerLocation) {
        LayerDefinition layerDefinition = this.roots.get(modelLayerLocation);
        if (layerDefinition == null) {
            throw new IllegalArgumentException("No model for layer " + modelLayerLocation);
        }
        return layerDefinition.bakeRoot();
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.roots = ImmutableMap.copyOf(LayerDefinitions.createRoots());
    }
}

