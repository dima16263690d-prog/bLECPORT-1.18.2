/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class HumanoidMobRenderer<T extends Mob, M extends HumanoidModel<T>>
extends MobRenderer<T, M> {
    private static final ResourceLocation DEFAULT_LOCATION = new ResourceLocation("textures/entity/steve.png");

    public HumanoidMobRenderer(EntityRendererProvider.Context context, M m, float f) {
        this(context, m, f, 1.0f, 1.0f, 1.0f);
    }

    public HumanoidMobRenderer(EntityRendererProvider.Context context, M m, float f, float f2, float f3, float f4) {
        super(context, m, f);
        this.addLayer(new CustomHeadLayer(this, context.getModelSet(), f2, f3, f4));
        this.addLayer(new ElytraLayer(this, context.getModelSet()));
        this.addLayer(new ItemInHandLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(T t) {
        return DEFAULT_LOCATION;
    }
}

