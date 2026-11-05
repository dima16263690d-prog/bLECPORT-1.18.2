/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.TropicalFish;

public class TropicalFishPatternLayer
extends RenderLayer<TropicalFish, ColorableHierarchicalModel<TropicalFish>> {
    private final TropicalFishModelA<TropicalFish> modelA;
    private final TropicalFishModelB<TropicalFish> modelB;

    public TropicalFishPatternLayer(RenderLayerParent<TropicalFish, ColorableHierarchicalModel<TropicalFish>> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.modelA = new TropicalFishModelA(entityModelSet.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL_PATTERN));
        this.modelB = new TropicalFishModelB(entityModelSet.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE_PATTERN));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, TropicalFish tropicalFish, float f, float f2, float f3, float f4, float f5, float f6) {
        ColorableHierarchicalModel colorableHierarchicalModel = tropicalFish.getBaseVariant() == 0 ? this.modelA : this.modelB;
        float[] fArray = tropicalFish.getPatternColor();
        TropicalFishPatternLayer.coloredCutoutModelCopyLayerRender(this.getParentModel(), colorableHierarchicalModel, tropicalFish.getPatternTextureLocation(), poseStack, multiBufferSource, n, tropicalFish, f, f2, f4, f5, f6, f3, fArray[0], fArray[1], fArray[2]);
    }
}

