/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;

public class FireworkEntityRenderer
extends EntityRenderer<FireworkRocketEntity> {
    private final ItemRenderer itemRenderer;

    public FireworkEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FireworkRocketEntity fireworkRocketEntity, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        if (fireworkRocketEntity.isShotAtAngle()) {
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0f));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
        }
        this.itemRenderer.renderStatic(fireworkRocketEntity.getItem(), ItemTransforms.TransformType.GROUND, n, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, fireworkRocketEntity.getId());
        poseStack.popPose();
        super.render(fireworkRocketEntity, f, f2, poseStack, multiBufferSource, n);
    }

    @Override
    public ResourceLocation getTextureLocation(FireworkRocketEntity fireworkRocketEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

