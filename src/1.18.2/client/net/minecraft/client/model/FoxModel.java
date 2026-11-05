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
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Fox;

public class FoxModel<T extends Fox>
extends AgeableListModel<T> {
    public final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;
    private static final int LEG_SIZE = 6;
    private static final float HEAD_HEIGHT = 16.5f;
    private static final float LEG_POS = 17.5f;
    private float legMotionPos;

    public FoxModel(ModelPart modelPart) {
        super(true, 8.0f, 3.35f);
        this.head = modelPart.getChild("head");
        this.body = modelPart.getChild("body");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(1, 5).addBox(-3.0f, -2.0f, -5.0f, 8.0f, 6.0f, 6.0f), PartPose.offset(-1.0f, 16.5f, -3.0f));
        partDefinition2.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(8, 1).addBox(-3.0f, -4.0f, -4.0f, 2.0f, 2.0f, 1.0f), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(15, 1).addBox(3.0f, -4.0f, -4.0f, 2.0f, 2.0f, 1.0f), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(6, 18).addBox(-1.0f, 2.01f, -8.0f, 4.0f, 2.0f, 3.0f), PartPose.ZERO);
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(24, 15).addBox(-3.0f, 3.999f, -3.5f, 6.0f, 11.0f, 6.0f), PartPose.offsetAndRotation(0.0f, 16.0f, -6.0f, 1.5707964f, 0.0f, 0.0f));
        CubeDeformation cubeDeformation = new CubeDeformation(0.001f);
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(4, 24).addBox(2.0f, 0.5f, -1.0f, 2.0f, 6.0f, 2.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(13, 24).addBox(2.0f, 0.5f, -1.0f, 2.0f, 6.0f, 2.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder2, PartPose.offset(-5.0f, 17.5f, 7.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(-1.0f, 17.5f, 7.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder2, PartPose.offset(-5.0f, 17.5f, 0.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(-1.0f, 17.5f, 0.0f));
        partDefinition3.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(30, 0).addBox(2.0f, 0.0f, -1.0f, 4.0f, 9.0f, 5.0f), PartPose.offsetAndRotation(-4.0f, 15.0f, -1.0f, -0.05235988f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 48, 32);
    }

    @Override
    public void prepareMobModel(T t, float f, float f2, float f3) {
        this.body.xRot = 1.5707964f;
        this.tail.xRot = -0.05235988f;
        this.rightHindLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * f2;
        this.leftHindLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * f2;
        this.rightFrontLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * f2;
        this.leftFrontLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * f2;
        this.head.setPos(-1.0f, 16.5f, -3.0f);
        this.head.yRot = 0.0f;
        this.head.zRot = ((Fox)t).getHeadRollAngle(f3);
        this.rightHindLeg.visible = true;
        this.leftHindLeg.visible = true;
        this.rightFrontLeg.visible = true;
        this.leftFrontLeg.visible = true;
        this.body.setPos(0.0f, 16.0f, -6.0f);
        this.body.zRot = 0.0f;
        this.rightHindLeg.setPos(-5.0f, 17.5f, 7.0f);
        this.leftHindLeg.setPos(-1.0f, 17.5f, 7.0f);
        if (((Fox)t).isCrouching()) {
            this.body.xRot = 1.6755161f;
            float f4 = ((Fox)t).getCrouchAmount(f3);
            this.body.setPos(0.0f, 16.0f + ((Fox)t).getCrouchAmount(f3), -6.0f);
            this.head.setPos(-1.0f, 16.5f + f4, -3.0f);
            this.head.yRot = 0.0f;
        } else if (((Fox)t).isSleeping()) {
            this.body.zRot = -1.5707964f;
            this.body.setPos(0.0f, 21.0f, -6.0f);
            this.tail.xRot = -2.6179938f;
            if (this.young) {
                this.tail.xRot = -2.1816616f;
                this.body.setPos(0.0f, 21.0f, -2.0f);
            }
            this.head.setPos(1.0f, 19.49f, -3.0f);
            this.head.xRot = 0.0f;
            this.head.yRot = -2.0943952f;
            this.head.zRot = 0.0f;
            this.rightHindLeg.visible = false;
            this.leftHindLeg.visible = false;
            this.rightFrontLeg.visible = false;
            this.leftFrontLeg.visible = false;
        } else if (((Fox)t).isSitting()) {
            this.body.xRot = 0.5235988f;
            this.body.setPos(0.0f, 9.0f, -3.0f);
            this.tail.xRot = 0.7853982f;
            this.tail.setPos(-4.0f, 15.0f, -2.0f);
            this.head.setPos(-1.0f, 10.0f, -0.25f);
            this.head.xRot = 0.0f;
            this.head.yRot = 0.0f;
            if (this.young) {
                this.head.setPos(-1.0f, 13.0f, -3.75f);
            }
            this.rightHindLeg.xRot = -1.3089969f;
            this.rightHindLeg.setPos(-5.0f, 21.5f, 6.75f);
            this.leftHindLeg.xRot = -1.3089969f;
            this.leftHindLeg.setPos(-1.0f, 21.5f, 6.75f);
            this.rightFrontLeg.xRot = -0.2617994f;
            this.leftFrontLeg.xRot = -0.2617994f;
        }
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
        float f6;
        if (!(((Fox)t).isSleeping() || ((Fox)t).isFaceplanted() || ((Fox)t).isCrouching())) {
            this.head.xRot = f5 * ((float)Math.PI / 180);
            this.head.yRot = f4 * ((float)Math.PI / 180);
        }
        if (((Fox)t).isSleeping()) {
            this.head.xRot = 0.0f;
            this.head.yRot = -2.0943952f;
            this.head.zRot = Mth.cos(f3 * 0.027f) / 22.0f;
        }
        if (((Fox)t).isCrouching()) {
            this.body.yRot = f6 = Mth.cos(f3) * 0.01f;
            this.rightHindLeg.zRot = f6;
            this.leftHindLeg.zRot = f6;
            this.rightFrontLeg.zRot = f6 / 2.0f;
            this.leftFrontLeg.zRot = f6 / 2.0f;
        }
        if (((Fox)t).isFaceplanted()) {
            f6 = 0.1f;
            this.legMotionPos += 0.67f;
            this.rightHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662f) * 0.1f;
            this.leftHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662f + (float)Math.PI) * 0.1f;
            this.rightFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662f + (float)Math.PI) * 0.1f;
            this.leftFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662f) * 0.1f;
        }
    }
}

