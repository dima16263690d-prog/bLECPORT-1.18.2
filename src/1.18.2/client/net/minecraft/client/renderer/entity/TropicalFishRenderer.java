/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;

public class TropicalFishRenderer
extends MobRenderer<TropicalFish, ColorableHierarchicalModel<TropicalFish>> {
    private final ColorableHierarchicalModel<TropicalFish> modelA = (ColorableHierarchicalModel)this.getModel();
    private final ColorableHierarchicalModel<TropicalFish> modelB;

    public TropicalFishRenderer(EntityRendererProvider.Context context) {
        super(context, new TropicalFishModelA(context.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL)), 0.15f);
        this.modelB = new TropicalFishModelB<TropicalFish>(context.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE));
        this.addLayer(new TropicalFishPatternLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(TropicalFish tropicalFish) {
        return tropicalFish.getBaseTextureLocation();
    }

    @Override
    public void render(TropicalFish tropicalFish, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        ColorableHierarchicalModel<TropicalFish> colorableHierarchicalModel;
        this.model = colorableHierarchicalModel = tropicalFish.getBaseVariant() == 0 ? this.modelA : this.modelB;
        float[] fArray = tropicalFish.getBaseColor();
        colorableHierarchicalModel.setColor(fArray[0], fArray[1], fArray[2]);
        super.render(tropicalFish, f, f2, poseStack, multiBufferSource, n);
        colorableHierarchicalModel.setColor(1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void setupRotations(TropicalFish tropicalFish, PoseStack poseStack, float f, float f2, float f3) {
        super.setupRotations(tropicalFish, poseStack, f, f2, f3);
        float f4 = 4.3f * Mth.sin(0.6f * f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(f4));
        if (!tropicalFish.isInWater()) {
            poseStack.translate(0.2f, 0.1f, 0.0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0f));
        }
    }
}

