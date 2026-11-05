/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;

public class WitchRenderer
extends MobRenderer<Witch, WitchModel<Witch>> {
    private static final ResourceLocation WITCH_LOCATION = new ResourceLocation("textures/entity/witch.png");

    public WitchRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchModel(context.bakeLayer(ModelLayers.WITCH)), 0.5f);
        this.addLayer(new WitchItemLayer<Witch>(this));
    }

    @Override
    public void render(Witch witch, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        ((WitchModel)this.model).setHoldingItem(!witch.getMainHandItem().isEmpty());
        super.render(witch, f, f2, poseStack, multiBufferSource, n);
    }

    @Override
    public ResourceLocation getTextureLocation(Witch witch) {
        return WITCH_LOCATION;
    }

    @Override
    protected void scale(Witch witch, PoseStack poseStack, float f) {
        float f2 = 0.9375f;
        poseStack.scale(0.9375f, 0.9375f, 0.9375f);
    }
}

