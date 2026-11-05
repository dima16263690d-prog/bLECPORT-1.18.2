/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SheepFurLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;

public class SheepRenderer
extends MobRenderer<Sheep, SheepModel<Sheep>> {
    private static final ResourceLocation SHEEP_LOCATION = new ResourceLocation("textures/entity/sheep/sheep.png");

    public SheepRenderer(EntityRendererProvider.Context context) {
        super(context, new SheepModel(context.bakeLayer(ModelLayers.SHEEP)), 0.7f);
        this.addLayer(new SheepFurLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(Sheep sheep) {
        return SHEEP_LOCATION;
    }
}

