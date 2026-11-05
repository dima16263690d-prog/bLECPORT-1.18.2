/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.ModelUtils;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Panda;

public class PandaModel<T extends Panda>
extends QuadrupedModel<T> {
    private float sitAmount;
    private float lieOnBackAmount;
    private float rollAmount;

    public PandaModel(ModelPart modelPart) {
        super(modelPart, true, 23.0f, 4.8f, 2.7f, 3.0f, 49);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 6).addBox(-6.5f, -5.0f, -4.0f, 13.0f, 10.0f, 9.0f).texOffs(45, 16).addBox("nose", -3.5f, 0.0f, -6.0f, 7.0f, 5.0f, 2.0f).texOffs(52, 25).addBox("left_ear", 3.5f, -8.0f, -1.0f, 5.0f, 4.0f, 1.0f).texOffs(52, 25).addBox("right_ear", -8.5f, -8.0f, -1.0f, 5.0f, 4.0f, 1.0f), PartPose.offset(0.0f, 11.5f, -17.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 25).addBox(-9.5f, -13.0f, -6.5f, 19.0f, 26.0f, 13.0f), PartPose.offsetAndRotation(0.0f, 10.0f, 0.0f, 1.5707964f, 0.0f, 0.0f));
        int n = 9;
        int n2 = 6;
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(40, 0).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 9.0f, 6.0f);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-5.5f, 15.0f, 9.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(5.5f, 15.0f, 9.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-5.5f, 15.0f, -9.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(5.5f, 15.0f, -9.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void prepareMobModel(T t, float f, float f2, float f3) {
        super.prepareMobModel(t, f, f2, f3);
        this.sitAmount = ((Panda)t).getSitAmount(f3);
        this.lieOnBackAmount = ((Panda)t).getLieOnBackAmount(f3);
        this.rollAmount = ((AgeableMob)t).isBaby() ? 0.0f : ((Panda)t).getRollAmount(f3);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        super.setupAnim(t, f, f2, f3, f4, f5);
        boolean bl = ((Panda)t).getUnhappyCounter() > 0;
        boolean bl2 = ((Panda)t).isSneezing();
        int n = ((Panda)t).getSneezeCounter();
        boolean bl3 = ((Panda)t).isEating();
        boolean bl4 = ((Panda)t).isScared();
        if (bl) {
            this.head.yRot = 0.35f * Mth.sin(0.6f * f3);
            this.head.zRot = 0.35f * Mth.sin(0.6f * f3);
            this.rightFrontLeg.xRot = -0.75f * Mth.sin(0.3f * f3);
            this.leftFrontLeg.xRot = 0.75f * Mth.sin(0.3f * f3);
        } else {
            this.head.zRot = 0.0f;
        }
        if (bl2) {
            if (n < 15) {
                this.head.xRot = -0.7853982f * (float)n / 14.0f;
            } else if (n < 20) {
                float f6 = (n - 15) / 5;
                this.head.xRot = -0.7853982f + 0.7853982f * f6;
            }
        }
        if (this.sitAmount > 0.0f) {
            this.body.xRot = ModelUtils.rotlerpRad(this.body.xRot, 1.7407963f, this.sitAmount);
            this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, 1.5707964f, this.sitAmount);
            this.rightFrontLeg.zRot = -0.27079642f;
            this.leftFrontLeg.zRot = 0.27079642f;
            this.rightHindLeg.zRot = 0.5707964f;
            this.leftHindLeg.zRot = -0.5707964f;
            if (bl3) {
                this.head.xRot = 1.5707964f + 0.2f * Mth.sin(f3 * 0.6f);
                this.rightFrontLeg.xRot = -0.4f - 0.2f * Mth.sin(f3 * 0.6f);
                this.leftFrontLeg.xRot = -0.4f - 0.2f * Mth.sin(f3 * 0.6f);
            }
            if (bl4) {
                this.head.xRot = 2.1707964f;
                this.rightFrontLeg.xRot = -0.9f;
                this.leftFrontLeg.xRot = -0.9f;
            }
        } else {
            this.rightHindLeg.zRot = 0.0f;
            this.leftHindLeg.zRot = 0.0f;
            this.rightFrontLeg.zRot = 0.0f;
            this.leftFrontLeg.zRot = 0.0f;
        }
        if (this.lieOnBackAmount > 0.0f) {
            this.rightHindLeg.xRot = -0.6f * Mth.sin(f3 * 0.15f);
            this.leftHindLeg.xRot = 0.6f * Mth.sin(f3 * 0.15f);
            this.rightFrontLeg.xRot = 0.3f * Mth.sin(f3 * 0.25f);
            this.leftFrontLeg.xRot = -0.3f * Mth.sin(f3 * 0.25f);
            this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, 1.5707964f, this.lieOnBackAmount);
        }
        if (this.rollAmount > 0.0f) {
            this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, 2.0561945f, this.rollAmount);
            this.rightHindLeg.xRot = -0.5f * Mth.sin(f3 * 0.5f);
            this.leftHindLeg.xRot = 0.5f * Mth.sin(f3 * 0.5f);
            this.rightFrontLeg.xRot = 0.5f * Mth.sin(f3 * 0.5f);
            this.leftFrontLeg.xRot = -0.5f * Mth.sin(f3 * 0.5f);
        }
    }
}

