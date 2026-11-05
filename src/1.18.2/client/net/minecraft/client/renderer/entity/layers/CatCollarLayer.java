/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;

public class CatCollarLayer
extends RenderLayer<Cat, CatModel<Cat>> {
    private static final ResourceLocation CAT_COLLAR_LOCATION = new ResourceLocation("textures/entity/cat/cat_collar.png");
    private final CatModel<Cat> catModel;

    public CatCollarLayer(RenderLayerParent<Cat, CatModel<Cat>> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.catModel = new CatModel(entityModelSet.bakeLayer(ModelLayers.CAT_COLLAR));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, Cat cat, float f, float f2, float f3, float f4, float f5, float f6) {
        if (!cat.isTame()) {
            return;
        }
        float[] fArray = cat.getCollarColor().getTextureDiffuseColors();
        CatCollarLayer.coloredCutoutModelCopyLayerRender(this.getParentModel(), this.catModel, CAT_COLLAR_LOCATION, poseStack, multiBufferSource, n, cat, f, f2, f4, f5, f6, f3, fArray[0], fArray[1], fArray[2]);
    }
}

