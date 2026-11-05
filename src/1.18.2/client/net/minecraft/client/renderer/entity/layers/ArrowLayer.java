/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;

public class ArrowLayer<T extends LivingEntity, M extends PlayerModel<T>>
extends StuckInBodyLayer<T, M> {
    private final EntityRenderDispatcher dispatcher;

    public ArrowLayer(EntityRendererProvider.Context context, LivingEntityRenderer<T, M> livingEntityRenderer) {
        super(livingEntityRenderer);
        this.dispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    protected int numStuck(T t) {
        return ((LivingEntity)t).getArrowCount();
    }

    @Override
    protected void renderStuckItem(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, Entity entity, float f, float f2, float f3, float f4) {
        float f5 = Mth.sqrt(f * f + f3 * f3);
        Arrow arrow = new Arrow(entity.level, entity.getX(), entity.getY(), entity.getZ());
        arrow.setYRot((float)(Math.atan2(f, f3) * 57.2957763671875));
        arrow.setXRot((float)(Math.atan2(f2, f5) * 57.2957763671875));
        arrow.yRotO = arrow.getYRot();
        arrow.xRotO = arrow.getXRot();
        this.dispatcher.render(arrow, 0.0, 0.0, 0.0, 0.0f, f4, poseStack, multiBufferSource, n);
    }
}

