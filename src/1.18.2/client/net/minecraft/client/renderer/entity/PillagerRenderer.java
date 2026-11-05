/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Pillager;

public class PillagerRenderer
extends IllagerRenderer<Pillager> {
    private static final ResourceLocation PILLAGER = new ResourceLocation("textures/entity/illager/pillager.png");

    public PillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel(context.bakeLayer(ModelLayers.PILLAGER)), 0.5f);
        this.addLayer(new ItemInHandLayer<Pillager, IllagerModel<Pillager>>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Pillager pillager) {
        return PILLAGER;
    }
}

