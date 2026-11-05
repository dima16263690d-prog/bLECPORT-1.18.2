/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public abstract class MobRenderer<T extends Mob, M extends EntityModel<T>>
extends LivingEntityRenderer<T, M> {
    public static final int LEASH_RENDER_STEPS = 24;

    public MobRenderer(EntityRendererProvider.Context context, M m, float f) {
        super(context, m, f);
    }

    @Override
    protected boolean shouldShowName(T t) {
        return super.shouldShowName(t) && (((LivingEntity)t).shouldShowName() || ((Entity)t).hasCustomName() && t == this.entityRenderDispatcher.crosshairPickEntity);
    }

    @Override
    public boolean shouldRender(T t, Frustum frustum, double d, double d2, double d3) {
        if (super.shouldRender(t, frustum, d, d2, d3)) {
            return true;
        }
        Entity entity = ((Mob)t).getLeashHolder();
        if (entity != null) {
            return frustum.isVisible(entity.getBoundingBoxForCulling());
        }
        return false;
    }

    @Override
    public void render(T t, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        super.render(t, f, f2, poseStack, multiBufferSource, n);
        Entity entity = ((Mob)t).getLeashHolder();
        if (entity == null) {
            return;
        }
        this.renderLeash(t, f2, poseStack, multiBufferSource, entity);
    }

    private <E extends Entity> void renderLeash(T t, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, E e) {
        int n;
        poseStack.pushPose();
        Vec3 vec3 = e.getRopeHoldPosition(f);
        double d = (double)(Mth.lerp(f, ((Mob)t).yBodyRot, ((Mob)t).yBodyRotO) * ((float)Math.PI / 180)) + 1.5707963267948966;
        Vec3 vec32 = ((Entity)t).getLeashOffset();
        double d2 = Math.cos(d) * vec32.z + Math.sin(d) * vec32.x;
        double d3 = Math.sin(d) * vec32.z - Math.cos(d) * vec32.x;
        double d4 = Mth.lerp((double)f, ((Mob)t).xo, ((Entity)t).getX()) + d2;
        double d5 = Mth.lerp((double)f, ((Mob)t).yo, ((Entity)t).getY()) + vec32.y;
        double d6 = Mth.lerp((double)f, ((Mob)t).zo, ((Entity)t).getZ()) + d3;
        poseStack.translate(d2, vec32.y, d3);
        float f2 = (float)(vec3.x - d4);
        float f3 = (float)(vec3.y - d5);
        float f4 = (float)(vec3.z - d6);
        float f5 = 0.025f;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.last().pose();
        float f6 = Mth.fastInvSqrt(f2 * f2 + f4 * f4) * 0.025f / 2.0f;
        float f7 = f4 * f6;
        float f8 = f2 * f6;
        BlockPos blockPos = new BlockPos(((Entity)t).getEyePosition(f));
        BlockPos blockPos2 = new BlockPos(e.getEyePosition(f));
        int n2 = this.getBlockLightLevel(t, blockPos);
        int n3 = this.entityRenderDispatcher.getRenderer(e).getBlockLightLevel(e, blockPos2);
        int n4 = ((Mob)t).level.getBrightness(LightLayer.SKY, blockPos);
        int n5 = ((Mob)t).level.getBrightness(LightLayer.SKY, blockPos2);
        for (n = 0; n <= 24; ++n) {
            MobRenderer.addVertexPair(vertexConsumer, matrix4f, f2, f3, f4, n2, n3, n4, n5, 0.025f, 0.025f, f7, f8, n, false);
        }
        for (n = 24; n >= 0; --n) {
            MobRenderer.addVertexPair(vertexConsumer, matrix4f, f2, f3, f4, n2, n3, n4, n5, 0.025f, 0.0f, f7, f8, n, true);
        }
        poseStack.popPose();
    }

    private static void addVertexPair(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float f2, float f3, int n, int n2, int n3, int n4, float f4, float f5, float f6, float f7, int n5, boolean bl) {
        float f8 = (float)n5 / 24.0f;
        int n6 = (int)Mth.lerp(f8, n, n2);
        int n7 = (int)Mth.lerp(f8, n3, n4);
        int n8 = LightTexture.pack(n6, n7);
        float f9 = n5 % 2 == (bl ? 1 : 0) ? 0.7f : 1.0f;
        float f10 = 0.5f * f9;
        float f11 = 0.4f * f9;
        float f12 = 0.3f * f9;
        float f13 = f * f8;
        float f14 = f2 > 0.0f ? f2 * f8 * f8 : f2 - f2 * (1.0f - f8) * (1.0f - f8);
        float f15 = f3 * f8;
        vertexConsumer.vertex(matrix4f, f13 - f6, f14 + f5, f15 + f7).color(f10, f11, f12, 1.0f).uv2(n8).endVertex();
        vertexConsumer.vertex(matrix4f, f13 + f6, f14 + f4 - f5, f15 - f7).color(f10, f11, f12, 1.0f).uv2(n8).endVertex();
    }
}

