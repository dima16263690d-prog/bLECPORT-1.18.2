/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;

public class LlamaModel<T extends AbstractChestedHorse>
extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightChest;
    private final ModelPart leftChest;

    public LlamaModel(ModelPart modelPart) {
        this.head = modelPart.getChild("head");
        this.body = modelPart.getChild("body");
        this.rightChest = modelPart.getChild("right_chest");
        this.leftChest = modelPart.getChild("left_chest");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -14.0f, -10.0f, 4.0f, 4.0f, 9.0f, cubeDeformation).texOffs(0, 14).addBox("neck", -4.0f, -16.0f, -6.0f, 8.0f, 18.0f, 6.0f, cubeDeformation).texOffs(17, 0).addBox("ear", -4.0f, -19.0f, -4.0f, 3.0f, 3.0f, 2.0f, cubeDeformation).texOffs(17, 0).addBox("ear", 1.0f, -19.0f, -4.0f, 3.0f, 3.0f, 2.0f, cubeDeformation), PartPose.offset(0.0f, 7.0f, -6.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(29, 0).addBox(-6.0f, -10.0f, -7.0f, 12.0f, 18.0f, 10.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, 5.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_chest", CubeListBuilder.create().texOffs(45, 28).addBox(-3.0f, 0.0f, 0.0f, 8.0f, 8.0f, 3.0f, cubeDeformation), PartPose.offsetAndRotation(-8.5f, 3.0f, 3.0f, 0.0f, 1.5707964f, 0.0f));
        partDefinition.addOrReplaceChild("left_chest", CubeListBuilder.create().texOffs(45, 41).addBox(-3.0f, 0.0f, 0.0f, 8.0f, 8.0f, 3.0f, cubeDeformation), PartPose.offsetAndRotation(5.5f, 3.0f, 3.0f, 0.0f, 1.5707964f, 0.0f));
        int n = 4;
        int n2 = 14;
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(29, 29).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 14.0f, 4.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-3.5f, 10.0f, 6.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(3.5f, 10.0f, 6.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-3.5f, 10.0f, -5.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(3.5f, 10.0f, -5.0f));
        return LayerDefinition.create(meshDefinition, 128, 64);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        boolean bl;
        this.head.xRot = f5 * ((float)Math.PI / 180);
        this.head.yRot = f4 * ((float)Math.PI / 180);
        this.rightHindLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * f2;
        this.leftHindLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * f2;
        this.rightFrontLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * f2;
        this.leftFrontLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * f2;
        this.rightChest.visible = bl = !((AgeableMob)t).isBaby() && ((AbstractChestedHorse)t).hasChest();
        this.leftChest.visible = bl;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2, float f, float f2, float f3, float f4) {
        if (this.young) {
            float f5 = 2.0f;
            poseStack.pushPose();
            float f6 = 0.7f;
            poseStack.scale(0.71428573f, 0.64935064f, 0.7936508f);
            poseStack.translate(0.0, 1.3125, 0.22f);
            this.head.render(poseStack, vertexConsumer, n, n2, f, f2, f3, f4);
            poseStack.popPose();
            poseStack.pushPose();
            float f7 = 1.1f;
            poseStack.scale(0.625f, 0.45454544f, 0.45454544f);
            poseStack.translate(0.0, 2.0625, 0.0);
            this.body.render(poseStack, vertexConsumer, n, n2, f, f2, f3, f4);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.scale(0.45454544f, 0.41322312f, 0.45454544f);
            poseStack.translate(0.0, 2.0625, 0.0);
            ImmutableList.of((Object)this.rightHindLeg, (Object)this.leftHindLeg, (Object)this.rightFrontLeg, (Object)this.leftFrontLeg, (Object)this.rightChest, (Object)this.leftChest).forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, n, n2, f, f2, f3, f4));
            poseStack.popPose();
        } else {
            ImmutableList.of((Object)this.head, (Object)this.body, (Object)this.rightHindLeg, (Object)this.leftHindLeg, (Object)this.rightFrontLeg, (Object)this.leftFrontLeg, (Object)this.rightChest, (Object)this.leftChest).forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, n, n2, f, f2, f3, f4));
        }
    }
}

