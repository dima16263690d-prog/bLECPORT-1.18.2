/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class SpiderModel<T extends Entity>
extends HierarchicalModel<T> {
    private static final String BODY_0 = "body0";
    private static final String BODY_1 = "body1";
    private static final String RIGHT_MIDDLE_FRONT_LEG = "right_middle_front_leg";
    private static final String LEFT_MIDDLE_FRONT_LEG = "left_middle_front_leg";
    private static final String RIGHT_MIDDLE_HIND_LEG = "right_middle_hind_leg";
    private static final String LEFT_MIDDLE_HIND_LEG = "left_middle_hind_leg";
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightMiddleHindLeg;
    private final ModelPart leftMiddleHindLeg;
    private final ModelPart rightMiddleFrontLeg;
    private final ModelPart leftMiddleFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public SpiderModel(ModelPart modelPart) {
        this.root = modelPart;
        this.head = modelPart.getChild("head");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightMiddleHindLeg = modelPart.getChild(RIGHT_MIDDLE_HIND_LEG);
        this.leftMiddleHindLeg = modelPart.getChild(LEFT_MIDDLE_HIND_LEG);
        this.rightMiddleFrontLeg = modelPart.getChild(RIGHT_MIDDLE_FRONT_LEG);
        this.leftMiddleFrontLeg = modelPart.getChild(LEFT_MIDDLE_FRONT_LEG);
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
    }

    public static LayerDefinition createSpiderBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int n = 15;
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 4).addBox(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 15.0f, -3.0f));
        partDefinition.addOrReplaceChild(BODY_0, CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f), PartPose.offset(0.0f, 15.0f, 0.0f));
        partDefinition.addOrReplaceChild(BODY_1, CubeListBuilder.create().texOffs(0, 12).addBox(-5.0f, -4.0f, -6.0f, 10.0f, 8.0f, 12.0f), PartPose.offset(0.0f, 15.0f, 9.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(18, 0).addBox(-15.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f);
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(18, 0).addBox(-1.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-4.0f, 15.0f, 2.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder2, PartPose.offset(4.0f, 15.0f, 2.0f));
        partDefinition.addOrReplaceChild(RIGHT_MIDDLE_HIND_LEG, cubeListBuilder, PartPose.offset(-4.0f, 15.0f, 1.0f));
        partDefinition.addOrReplaceChild(LEFT_MIDDLE_HIND_LEG, cubeListBuilder2, PartPose.offset(4.0f, 15.0f, 1.0f));
        partDefinition.addOrReplaceChild(RIGHT_MIDDLE_FRONT_LEG, cubeListBuilder, PartPose.offset(-4.0f, 15.0f, 0.0f));
        partDefinition.addOrReplaceChild(LEFT_MIDDLE_FRONT_LEG, cubeListBuilder2, PartPose.offset(4.0f, 15.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-4.0f, 15.0f, -1.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder2, PartPose.offset(4.0f, 15.0f, -1.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        this.head.yRot = f4 * ((float)Math.PI / 180);
        this.head.xRot = f5 * ((float)Math.PI / 180);
        float f6 = 0.7853982f;
        this.rightHindLeg.zRot = -0.7853982f;
        this.leftHindLeg.zRot = 0.7853982f;
        this.rightMiddleHindLeg.zRot = -0.58119464f;
        this.leftMiddleHindLeg.zRot = 0.58119464f;
        this.rightMiddleFrontLeg.zRot = -0.58119464f;
        this.leftMiddleFrontLeg.zRot = 0.58119464f;
        this.rightFrontLeg.zRot = -0.7853982f;
        this.leftFrontLeg.zRot = 0.7853982f;
        float f7 = -0.0f;
        float f8 = 0.3926991f;
        this.rightHindLeg.yRot = 0.7853982f;
        this.leftHindLeg.yRot = -0.7853982f;
        this.rightMiddleHindLeg.yRot = 0.3926991f;
        this.leftMiddleHindLeg.yRot = -0.3926991f;
        this.rightMiddleFrontLeg.yRot = -0.3926991f;
        this.leftMiddleFrontLeg.yRot = 0.3926991f;
        this.rightFrontLeg.yRot = -0.7853982f;
        this.leftFrontLeg.yRot = 0.7853982f;
        float f9 = -(Mth.cos(f * 0.6662f * 2.0f + 0.0f) * 0.4f) * f2;
        float f10 = -(Mth.cos(f * 0.6662f * 2.0f + (float)Math.PI) * 0.4f) * f2;
        float f11 = -(Mth.cos(f * 0.6662f * 2.0f + 1.5707964f) * 0.4f) * f2;
        float f12 = -(Mth.cos(f * 0.6662f * 2.0f + 4.712389f) * 0.4f) * f2;
        float f13 = Math.abs(Mth.sin(f * 0.6662f + 0.0f) * 0.4f) * f2;
        float f14 = Math.abs(Mth.sin(f * 0.6662f + (float)Math.PI) * 0.4f) * f2;
        float f15 = Math.abs(Mth.sin(f * 0.6662f + 1.5707964f) * 0.4f) * f2;
        float f16 = Math.abs(Mth.sin(f * 0.6662f + 4.712389f) * 0.4f) * f2;
        this.rightHindLeg.yRot += f9;
        this.leftHindLeg.yRot += -f9;
        this.rightMiddleHindLeg.yRot += f10;
        this.leftMiddleHindLeg.yRot += -f10;
        this.rightMiddleFrontLeg.yRot += f11;
        this.leftMiddleFrontLeg.yRot += -f11;
        this.rightFrontLeg.yRot += f12;
        this.leftFrontLeg.yRot += -f12;
        this.rightHindLeg.zRot += f13;
        this.leftHindLeg.zRot += -f13;
        this.rightMiddleHindLeg.zRot += f14;
        this.leftMiddleHindLeg.zRot += -f14;
        this.rightMiddleFrontLeg.zRot += f15;
        this.leftMiddleFrontLeg.zRot += -f15;
        this.rightFrontLeg.zRot += f16;
        this.leftFrontLeg.zRot += -f16;
    }
}

