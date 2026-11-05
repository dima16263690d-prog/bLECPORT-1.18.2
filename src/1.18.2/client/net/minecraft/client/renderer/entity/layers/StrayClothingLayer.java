/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class StrayClothingLayer<T extends Mob, M extends EntityModel<T>>
extends RenderLayer<T, M> {
    private static final ResourceLocation STRAY_CLOTHES_LOCATION = new ResourceLocation("textures/entity/skeleton/stray_overlay.png");
    private final SkeletonModel<T> layerModel;

    public StrayClothingLayer(RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.layerModel = new SkeletonModel(entityModelSet.bakeLayer(ModelLayers.STRAY_OUTER_LAYER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, T t, float f, float f2, float f3, float f4, float f5, float f6) {
        StrayClothingLayer.coloredCutoutModelCopyLayerRender(this.getParentModel(), this.layerModel, STRAY_CLOTHES_LOCATION, poseStack, multiBufferSource, n, t, f, f2, f4, f5, f6, f3, 1.0f, 1.0f, 1.0f);
    }
}

