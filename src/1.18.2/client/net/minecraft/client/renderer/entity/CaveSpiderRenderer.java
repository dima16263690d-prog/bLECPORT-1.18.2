/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.CaveSpider;

public class CaveSpiderRenderer
extends SpiderRenderer<CaveSpider> {
    private static final ResourceLocation CAVE_SPIDER_LOCATION = new ResourceLocation("textures/entity/spider/cave_spider.png");
    private static final float SCALE = 0.7f;

    public CaveSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.CAVE_SPIDER);
        this.shadowRadius *= 0.7f;
    }

    @Override
    protected void scale(CaveSpider caveSpider, PoseStack poseStack, float f) {
        poseStack.scale(0.7f, 0.7f, 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(CaveSpider caveSpider) {
        return CAVE_SPIDER_LOCATION;
    }
}

