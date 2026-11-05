/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;

public class SquidRenderer<T extends Squid>
extends MobRenderer<T, SquidModel<T>> {
    private static final ResourceLocation SQUID_LOCATION = new ResourceLocation("textures/entity/squid/squid.png");

    public SquidRenderer(EntityRendererProvider.Context context, SquidModel<T> squidModel) {
        super(context, squidModel, 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(T t) {
        return SQUID_LOCATION;
    }

    @Override
    protected void setupRotations(T t, PoseStack poseStack, float f, float f2, float f3) {
        float f4 = Mth.lerp(f3, ((Squid)t).xBodyRotO, ((Squid)t).xBodyRot);
        float f5 = Mth.lerp(f3, ((Squid)t).zBodyRotO, ((Squid)t).zBodyRot);
        poseStack.translate(0.0, 0.5, 0.0);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f - f2));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(f4));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(f5));
        poseStack.translate(0.0, -1.2f, 0.0);
    }

    @Override
    protected float getBob(T t, float f) {
        return Mth.lerp(f, ((Squid)t).oldTentacleAngle, ((Squid)t).tentacleAngle);
    }
}

