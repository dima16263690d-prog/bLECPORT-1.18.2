/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PlayerItemInHandLayer<T extends Player, M extends EntityModel<T> & HeadedModel>
extends ItemInHandLayer<T, M> {
    private static final float X_ROT_MIN = -0.5235988f;
    private static final float X_ROT_MAX = 1.5707964f;

    public PlayerItemInHandLayer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    protected void renderArmWithItem(LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType transformType, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        if (itemStack.is(Items.SPYGLASS) && livingEntity.getUseItem() == itemStack && livingEntity.swingTime == 0) {
            this.renderArmWithSpyglass(livingEntity, itemStack, humanoidArm, poseStack, multiBufferSource, n);
        } else {
            super.renderArmWithItem(livingEntity, itemStack, transformType, humanoidArm, poseStack, multiBufferSource, n);
        }
    }

    private void renderArmWithSpyglass(LivingEntity livingEntity, ItemStack itemStack, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        ModelPart modelPart = ((HeadedModel)this.getParentModel()).getHead();
        float f = modelPart.xRot;
        modelPart.xRot = Mth.clamp(modelPart.xRot, -0.5235988f, 1.5707964f);
        modelPart.translateAndRotate(poseStack);
        modelPart.xRot = f;
        CustomHeadLayer.translateToHead(poseStack, false);
        boolean bl = humanoidArm == HumanoidArm.LEFT;
        poseStack.translate((bl ? -2.5f : 2.5f) / 16.0f, -0.0625, 0.0);
        Minecraft.getInstance().getItemInHandRenderer().renderItem(livingEntity, itemStack, ItemTransforms.TransformType.HEAD, false, poseStack, multiBufferSource, n);
        poseStack.popPose();
    }
}

