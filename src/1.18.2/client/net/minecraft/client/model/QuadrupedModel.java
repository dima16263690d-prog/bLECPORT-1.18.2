/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class QuadrupedModel<T extends Entity>
extends AgeableListModel<T> {
    protected final ModelPart head;
    protected final ModelPart body;
    protected final ModelPart rightHindLeg;
    protected final ModelPart leftHindLeg;
    protected final ModelPart rightFrontLeg;
    protected final ModelPart leftFrontLeg;

    protected QuadrupedModel(ModelPart modelPart, boolean bl, float f, float f2, float f3, float f4, int n) {
        super(bl, f, f2, f3, f4, n);
        this.head = modelPart.getChild("head");
        this.body = modelPart.getChild("body");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
    }

    public static MeshDefinition createBodyMesh(int n, CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f, cubeDeformation), PartPose.offset(0.0f, 18 - n, -6.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-5.0f, -10.0f, -7.0f, 10.0f, 16.0f, 8.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, 17 - n, 2.0f, 1.5707964f, 0.0f, 0.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, (float)n, 4.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-3.0f, 24 - n, 7.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(3.0f, 24 - n, 7.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-3.0f, 24 - n, -5.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(3.0f, 24 - n, -5.0f));
        return meshDefinition;
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of((Object)this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of((Object)this.body, (Object)this.rightHindLeg, (Object)this.leftHindLeg, (Object)this.rightFrontLeg, (Object)this.leftFrontLeg);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        this.head.xRot = f5 * ((float)Math.PI / 180);
        this.head.yRot = f4 * ((float)Math.PI / 180);
        this.rightHindLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * f2;
        this.leftHindLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * f2;
        this.rightFrontLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * f2;
        this.leftFrontLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * f2;
    }
}

