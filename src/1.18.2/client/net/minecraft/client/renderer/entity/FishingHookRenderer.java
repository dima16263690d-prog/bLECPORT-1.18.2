/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class FishingHookRenderer
extends EntityRenderer<FishingHook> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);
    private static final double VIEW_BOBBING_SCALE = 960.0;

    public FishingHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FishingHook fishingHook, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        double d;
        float f3;
        double d2;
        double d3;
        double d4;
        Player player = fishingHook.getPlayerOwner();
        if (player == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
        FishingHookRenderer.vertex(vertexConsumer, matrix4f, matrix3f, n, 0.0f, 0, 0, 1);
        FishingHookRenderer.vertex(vertexConsumer, matrix4f, matrix3f, n, 1.0f, 0, 1, 1);
        FishingHookRenderer.vertex(vertexConsumer, matrix4f, matrix3f, n, 1.0f, 1, 1, 0);
        FishingHookRenderer.vertex(vertexConsumer, matrix4f, matrix3f, n, 0.0f, 1, 0, 0);
        poseStack.popPose();
        int n2 = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        ItemStack itemStack = player.getMainHandItem();
        if (!itemStack.is(Items.FISHING_ROD)) {
            n2 = -n2;
        }
        float f4 = player.getAttackAnim(f2);
        float f5 = Mth.sin(Mth.sqrt(f4) * (float)Math.PI);
        float f6 = Mth.lerp(f2, player.yBodyRotO, player.yBodyRot) * ((float)Math.PI / 180);
        double d5 = Mth.sin(f6);
        double d6 = Mth.cos(f6);
        double d7 = (double)n2 * 0.35;
        double d8 = 0.8;
        if (this.entityRenderDispatcher.options != null && !this.entityRenderDispatcher.options.getCameraType().isFirstPerson() || player != Minecraft.getInstance().player) {
            d4 = Mth.lerp((double)f2, player.xo, player.getX()) - d6 * d7 - d5 * 0.8;
            d3 = player.yo + (double)player.getEyeHeight() + (player.getY() - player.yo) * (double)f2 - 0.45;
            d2 = Mth.lerp((double)f2, player.zo, player.getZ()) - d5 * d7 + d6 * 0.8;
            f3 = player.isCrouching() ? -0.1875f : 0.0f;
        } else {
            d = 960.0 / this.entityRenderDispatcher.options.fov;
            Vec3 vec3 = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float)n2 * 0.525f, -0.1f);
            vec3 = vec3.scale(d);
            vec3 = vec3.yRot(f5 * 0.5f);
            vec3 = vec3.xRot(-f5 * 0.7f);
            d4 = Mth.lerp((double)f2, player.xo, player.getX()) + vec3.x;
            d3 = Mth.lerp((double)f2, player.yo, player.getY()) + vec3.y;
            d2 = Mth.lerp((double)f2, player.zo, player.getZ()) + vec3.z;
            f3 = player.getEyeHeight();
        }
        d = Mth.lerp((double)f2, fishingHook.xo, fishingHook.getX());
        double d9 = Mth.lerp((double)f2, fishingHook.yo, fishingHook.getY()) + 0.25;
        double d10 = Mth.lerp((double)f2, fishingHook.zo, fishingHook.getZ());
        float f7 = (float)(d4 - d);
        float f8 = (float)(d3 - d9) + f3;
        float f9 = (float)(d2 - d10);
        VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.lineStrip());
        PoseStack.Pose pose2 = poseStack.last();
        int n3 = 16;
        for (int i = 0; i <= 16; ++i) {
            FishingHookRenderer.stringVertex(f7, f8, f9, vertexConsumer2, pose2, FishingHookRenderer.fraction(i, 16), FishingHookRenderer.fraction(i + 1, 16));
        }
        poseStack.popPose();
        super.render(fishingHook, f, f2, poseStack, multiBufferSource, n);
    }

    private static float fraction(int n, int n2) {
        return (float)n / (float)n2;
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, int n, float f, int n2, int n3, int n4) {
        vertexConsumer.vertex(matrix4f, f - 0.5f, (float)n2 - 0.5f, 0.0f).color(255, 255, 255, 255).uv(n3, n4).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(n).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
    }

    private static void stringVertex(float f, float f2, float f3, VertexConsumer vertexConsumer, PoseStack.Pose pose, float f4, float f5) {
        float f6 = f * f4;
        float f7 = f2 * (f4 * f4 + f4) * 0.5f + 0.25f;
        float f8 = f3 * f4;
        float f9 = f * f5 - f6;
        float f10 = f2 * (f5 * f5 + f5) * 0.5f + 0.25f - f7;
        float f11 = f3 * f5 - f8;
        float f12 = Mth.sqrt(f9 * f9 + f10 * f10 + f11 * f11);
        vertexConsumer.vertex(pose.pose(), f6, f7, f8).color(0, 0, 0, 255).normal(pose.normal(), f9 /= f12, f10 /= f12, f11 /= f12).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(FishingHook fishingHook) {
        return TEXTURE_LOCATION;
    }
}

