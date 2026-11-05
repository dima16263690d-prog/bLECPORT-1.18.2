/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.PolarBear;

public class PolarBearModel<T extends PolarBear>
extends QuadrupedModel<T> {
    public PolarBearModel(ModelPart modelPart) {
        super(modelPart, true, 16.0f, 4.0f, 2.25f, 2.0f, 24);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5f, -3.0f, -3.0f, 7.0f, 7.0f, 7.0f).texOffs(0, 44).addBox("mouth", -2.5f, 1.0f, -6.0f, 5.0f, 3.0f, 3.0f).texOffs(26, 0).addBox("right_ear", -4.5f, -4.0f, -1.0f, 2.0f, 2.0f, 1.0f).texOffs(26, 0).mirror().addBox("left_ear", 2.5f, -4.0f, -1.0f, 2.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 10.0f, -16.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 19).addBox(-5.0f, -13.0f, -7.0f, 14.0f, 14.0f, 11.0f).texOffs(39, 0).addBox(-4.0f, -25.0f, -7.0f, 12.0f, 12.0f, 10.0f), PartPose.offsetAndRotation(-2.0f, 9.0f, 12.0f, 1.5707964f, 0.0f, 0.0f));
        int n = 10;
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(50, 22).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 8.0f);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-4.5f, 14.0f, 6.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(4.5f, 14.0f, 6.0f));
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(50, 40).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 6.0f);
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder2, PartPose.offset(-3.5f, 14.0f, -8.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder2, PartPose.offset(3.5f, 14.0f, -8.0f));
        return LayerDefinition.create(meshDefinition, 128, 64);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        super.setupAnim(t, f, f2, f3, f4, f5);
        float f6 = f3 - (float)((PolarBear)t).tickCount;
        float f7 = ((PolarBear)t).getStandingAnimationScale(f6);
        f7 *= f7;
        float f8 = 1.0f - f7;
        this.body.xRot = 1.5707964f - f7 * (float)Math.PI * 0.35f;
        this.body.y = 9.0f * f8 + 11.0f * f7;
        this.rightFrontLeg.y = 14.0f * f8 - 6.0f * f7;
        this.rightFrontLeg.z = -8.0f * f8 - 4.0f * f7;
        this.rightFrontLeg.xRot -= f7 * (float)Math.PI * 0.45f;
        this.leftFrontLeg.y = this.rightFrontLeg.y;
        this.leftFrontLeg.z = this.rightFrontLeg.z;
        this.leftFrontLeg.xRot -= f7 * (float)Math.PI * 0.45f;
        if (this.young) {
            this.head.y = 10.0f * f8 - 9.0f * f7;
            this.head.z = -16.0f * f8 - 7.0f * f7;
        } else {
            this.head.y = 10.0f * f8 - 14.0f * f7;
            this.head.z = -16.0f * f8 - 3.0f * f7;
        }
        this.head.xRot += f7 * (float)Math.PI * 0.15f;
    }
}

