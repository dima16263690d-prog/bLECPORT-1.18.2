/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public abstract class AbstractHorseRenderer<T extends AbstractHorse, M extends HorseModel<T>>
extends MobRenderer<T, M> {
    private final float scale;

    public AbstractHorseRenderer(EntityRendererProvider.Context context, M m, float f) {
        super(context, m, 0.75f);
        this.scale = f;
    }

    @Override
    protected void scale(T t, PoseStack poseStack, float f) {
        poseStack.scale(this.scale, this.scale, this.scale);
        super.scale(t, poseStack, f);
    }
}

