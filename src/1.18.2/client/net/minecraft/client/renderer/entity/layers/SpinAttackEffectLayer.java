/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class SpinAttackEffectLayer<T extends LivingEntity>
extends RenderLayer<T, PlayerModel<T>> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident_riptide.png");
    public static final String BOX = "box";
    private final ModelPart box;

    public SpinAttackEffectLayer(RenderLayerParent<T, PlayerModel<T>> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        ModelPart modelPart = entityModelSet.bakeLayer(ModelLayers.PLAYER_SPIN_ATTACK);
        this.box = modelPart.getChild(BOX);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(BOX, CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -16.0f, -8.0f, 16.0f, 32.0f, 16.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, T t, float f, float f2, float f3, float f4, float f5, float f6) {
        if (!((LivingEntity)t).isAutoSpinAttack()) {
            return;
        }
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        for (int i = 0; i < 3; ++i) {
            poseStack.pushPose();
            float f7 = f4 * (float)(-(45 + i * 5));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(f7));
            float f8 = 0.75f * (float)i;
            poseStack.scale(f8, f8, f8);
            poseStack.translate(0.0, -0.2f + 0.6f * (float)i, 0.0);
            this.box.render(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }
}

