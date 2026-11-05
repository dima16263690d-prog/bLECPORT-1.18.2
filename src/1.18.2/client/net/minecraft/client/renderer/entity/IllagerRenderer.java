/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.monster.AbstractIllager;

public abstract class IllagerRenderer<T extends AbstractIllager>
extends MobRenderer<T, IllagerModel<T>> {
    protected IllagerRenderer(EntityRendererProvider.Context context, IllagerModel<T> illagerModel, float f) {
        super(context, illagerModel, f);
        this.addLayer(new CustomHeadLayer(this, context.getModelSet()));
    }

    @Override
    protected void scale(T t, PoseStack poseStack, float f) {
        float f2 = 0.9375f;
        poseStack.scale(0.9375f, 0.9375f, 0.9375f);
    }
}

