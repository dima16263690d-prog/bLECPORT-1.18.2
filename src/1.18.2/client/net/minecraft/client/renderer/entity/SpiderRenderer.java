/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;

public class SpiderRenderer<T extends Spider>
extends MobRenderer<T, SpiderModel<T>> {
    private static final ResourceLocation SPIDER_LOCATION = new ResourceLocation("textures/entity/spider/spider.png");

    public SpiderRenderer(EntityRendererProvider.Context context) {
        this(context, ModelLayers.SPIDER);
    }

    public SpiderRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
        super(context, new SpiderModel(context.bakeLayer(modelLayerLocation)), 0.8f);
        this.addLayer(new SpiderEyesLayer(this));
    }

    @Override
    protected float getFlipDegrees(T t) {
        return 180.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(T t) {
        return SPIDER_LOCATION;
    }

    @Override
    protected /* synthetic */ float getFlipDegrees(LivingEntity livingEntity) {
        return this.getFlipDegrees((T)((Spider)livingEntity));
    }
}

