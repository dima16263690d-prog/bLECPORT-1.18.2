/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.SpellcasterIllager;

public class EvokerRenderer<T extends SpellcasterIllager>
extends IllagerRenderer<T> {
    private static final ResourceLocation EVOKER_ILLAGER = new ResourceLocation("textures/entity/illager/evoker.png");

    public EvokerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel(context.bakeLayer(ModelLayers.EVOKER)), 0.5f);
        this.addLayer(new ItemInHandLayer<T, IllagerModel<T>>(this){

            @Override
            public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, T t, float f, float f2, float f3, float f4, float f5, float f6) {
                if (((SpellcasterIllager)t).isCastingSpell()) {
                    super.render(poseStack, multiBufferSource, n, t, f, f2, f3, f4, f5, f6);
                }
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(T t) {
        return EVOKER_ILLAGER;
    }
}

