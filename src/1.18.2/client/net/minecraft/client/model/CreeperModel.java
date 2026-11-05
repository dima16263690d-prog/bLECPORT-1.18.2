/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class CreeperModel<T extends Entity>
extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private static final int Y_OFFSET = 6;

    public CreeperModel(ModelPart modelPart) {
        this.root = modelPart;
        this.head = modelPart.getChild("head");
        this.leftHindLeg = modelPart.getChild("right_hind_leg");
        this.rightHindLeg = modelPart.getChild("left_hind_leg");
        this.leftFrontLeg = modelPart.getChild("right_front_leg");
        this.rightFrontLeg = modelPart.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation), PartPose.offset(0.0f, 6.0f, 0.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(0.0f, 6.0f, 0.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 6.0f, 4.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-2.0f, 18.0f, 4.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(2.0f, 18.0f, 4.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-2.0f, 18.0f, -4.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(2.0f, 18.0f, -4.0f));
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
        this.rightHindLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * f2;
        this.leftHindLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * f2;
        this.rightFrontLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * f2;
        this.leftFrontLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * f2;
    }
}

