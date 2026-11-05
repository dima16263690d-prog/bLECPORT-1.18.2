/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Vector3f;
import java.util.Map;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

public class AxolotlModel<T extends Axolotl>
extends AgeableListModel<T> {
    public static final float SWIMMING_LEG_XROT = 1.8849558f;
    private final ModelPart tail;
    private final ModelPart leftHindLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart topGills;
    private final ModelPart leftGills;
    private final ModelPart rightGills;

    public AxolotlModel(ModelPart modelPart) {
        super(true, 8.0f, 3.35f);
        this.body = modelPart.getChild("body");
        this.head = this.body.getChild("head");
        this.rightHindLeg = this.body.getChild("right_hind_leg");
        this.leftHindLeg = this.body.getChild("left_hind_leg");
        this.rightFrontLeg = this.body.getChild("right_front_leg");
        this.leftFrontLeg = this.body.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
        this.topGills = this.head.getChild("top_gills");
        this.leftGills = this.head.getChild("left_gills");
        this.rightGills = this.head.getChild("right_gills");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 11).addBox(-4.0f, -2.0f, -9.0f, 8.0f, 4.0f, 10.0f).texOffs(2, 17).addBox(0.0f, -3.0f, -8.0f, 0.0f, 5.0f, 9.0f), PartPose.offset(0.0f, 20.0f, 5.0f));
        CubeDeformation cubeDeformation = new CubeDeformation(0.001f);
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 1).addBox(-4.0f, -3.0f, -5.0f, 8.0f, 5.0f, 5.0f, cubeDeformation), PartPose.offset(0.0f, 0.0f, -9.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(3, 37).addBox(-4.0f, -3.0f, 0.0f, 8.0f, 3.0f, 0.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(0, 40).addBox(-3.0f, -5.0f, 0.0f, 3.0f, 7.0f, 0.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder3 = CubeListBuilder.create().texOffs(11, 40).addBox(0.0f, -5.0f, 0.0f, 3.0f, 7.0f, 0.0f, cubeDeformation);
        partDefinition3.addOrReplaceChild("top_gills", cubeListBuilder, PartPose.offset(0.0f, -3.0f, -1.0f));
        partDefinition3.addOrReplaceChild("left_gills", cubeListBuilder2, PartPose.offset(-4.0f, 0.0f, -1.0f));
        partDefinition3.addOrReplaceChild("right_gills", cubeListBuilder3, PartPose.offset(4.0f, 0.0f, -1.0f));
        CubeListBuilder cubeListBuilder4 = CubeListBuilder.create().texOffs(2, 13).addBox(-1.0f, 0.0f, 0.0f, 3.0f, 5.0f, 0.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder5 = CubeListBuilder.create().texOffs(2, 13).addBox(-2.0f, 0.0f, 0.0f, 3.0f, 5.0f, 0.0f, cubeDeformation);
        partDefinition2.addOrReplaceChild("right_hind_leg", cubeListBuilder5, PartPose.offset(-3.5f, 1.0f, -1.0f));
        partDefinition2.addOrReplaceChild("left_hind_leg", cubeListBuilder4, PartPose.offset(3.5f, 1.0f, -1.0f));
        partDefinition2.addOrReplaceChild("right_front_leg", cubeListBuilder5, PartPose.offset(-3.5f, 1.0f, -8.0f));
        partDefinition2.addOrReplaceChild("left_front_leg", cubeListBuilder4, PartPose.offset(3.5f, 1.0f, -8.0f));
        partDefinition2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(2, 19).addBox(0.0f, -3.0f, 0.0f, 0.0f, 5.0f, 12.0f), PartPose.offset(0.0f, 0.0f, 1.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of((Object)this.body);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        boolean bl;
        this.setupInitialAnimationValues(t, f4, f5);
        if (((Axolotl)t).isPlayingDead()) {
            this.setupPlayDeadAnimation(f4);
            this.saveAnimationValues(t);
            return;
        }
        boolean bl2 = bl = ((Entity)t).getDeltaMovement().horizontalDistanceSqr() > 1.0E-7 || ((Entity)t).getXRot() != ((Axolotl)t).xRotO || ((Entity)t).getYRot() != ((Axolotl)t).yRotO || ((Axolotl)t).xOld != ((Entity)t).getX() || ((Axolotl)t).zOld != ((Entity)t).getZ();
        if (((Entity)t).isInWaterOrBubble()) {
            if (bl) {
                this.setupSwimmingAnimation(f3, f5);
            } else {
                this.setupWaterHoveringAnimation(f3);
            }
            this.saveAnimationValues(t);
            return;
        }
        if (((Entity)t).isOnGround()) {
            if (bl) {
                this.setupGroundCrawlingAnimation(f3, f4);
            } else {
                this.setupLayStillOnGroundAnimation(f3, f4);
            }
        }
        this.saveAnimationValues(t);
    }

    private void saveAnimationValues(T t) {
        Map<String, Vector3f> map = ((Axolotl)t).getModelRotationValues();
        map.put("body", this.getRotationVector(this.body));
        map.put("head", this.getRotationVector(this.head));
        map.put("right_hind_leg", this.getRotationVector(this.rightHindLeg));
        map.put("left_hind_leg", this.getRotationVector(this.leftHindLeg));
        map.put("right_front_leg", this.getRotationVector(this.rightFrontLeg));
        map.put("left_front_leg", this.getRotationVector(this.leftFrontLeg));
        map.put("tail", this.getRotationVector(this.tail));
        map.put("top_gills", this.getRotationVector(this.topGills));
        map.put("left_gills", this.getRotationVector(this.leftGills));
        map.put("right_gills", this.getRotationVector(this.rightGills));
    }

    private Vector3f getRotationVector(ModelPart modelPart) {
        return new Vector3f(modelPart.xRot, modelPart.yRot, modelPart.zRot);
    }

    private void setRotationFromVector(ModelPart modelPart, Vector3f vector3f) {
        modelPart.setRotation(vector3f.x(), vector3f.y(), vector3f.z());
    }

    private void setupInitialAnimationValues(T t, float f, float f2) {
        this.body.x = 0.0f;
        this.head.y = 0.0f;
        this.body.y = 20.0f;
        Map<String, Vector3f> map = ((Axolotl)t).getModelRotationValues();
        if (map.isEmpty()) {
            this.body.setRotation(f2 * ((float)Math.PI / 180), f * ((float)Math.PI / 180), 0.0f);
            this.head.setRotation(0.0f, 0.0f, 0.0f);
            this.leftHindLeg.setRotation(0.0f, 0.0f, 0.0f);
            this.rightHindLeg.setRotation(0.0f, 0.0f, 0.0f);
            this.leftFrontLeg.setRotation(0.0f, 0.0f, 0.0f);
            this.rightFrontLeg.setRotation(0.0f, 0.0f, 0.0f);
            this.leftGills.setRotation(0.0f, 0.0f, 0.0f);
            this.rightGills.setRotation(0.0f, 0.0f, 0.0f);
            this.topGills.setRotation(0.0f, 0.0f, 0.0f);
            this.tail.setRotation(0.0f, 0.0f, 0.0f);
        } else {
            this.setRotationFromVector(this.body, map.get("body"));
            this.setRotationFromVector(this.head, map.get("head"));
            this.setRotationFromVector(this.leftHindLeg, map.get("left_hind_leg"));
            this.setRotationFromVector(this.rightHindLeg, map.get("right_hind_leg"));
            this.setRotationFromVector(this.leftFrontLeg, map.get("left_front_leg"));
            this.setRotationFromVector(this.rightFrontLeg, map.get("right_front_leg"));
            this.setRotationFromVector(this.leftGills, map.get("left_gills"));
            this.setRotationFromVector(this.rightGills, map.get("right_gills"));
            this.setRotationFromVector(this.topGills, map.get("top_gills"));
            this.setRotationFromVector(this.tail, map.get("tail"));
        }
    }

    private float lerpTo(float f, float f2) {
        return this.lerpTo(0.05f, f, f2);
    }

    private float lerpTo(float f, float f2, float f3) {
        return Mth.rotLerp(f, f2, f3);
    }

    private void lerpPart(ModelPart modelPart, float f, float f2, float f3) {
        modelPart.setRotation(this.lerpTo(modelPart.xRot, f), this.lerpTo(modelPart.yRot, f2), this.lerpTo(modelPart.zRot, f3));
    }

    private void setupLayStillOnGroundAnimation(float f, float f2) {
        float f3 = f * 0.09f;
        float f4 = Mth.sin(f3);
        float f5 = Mth.cos(f3);
        float f6 = f4 * f4 - 2.0f * f4;
        float f7 = f5 * f5 - 3.0f * f4;
        this.head.xRot = this.lerpTo(this.head.xRot, -0.09f * f6);
        this.head.yRot = this.lerpTo(this.head.yRot, 0.0f);
        this.head.zRot = this.lerpTo(this.head.zRot, -0.2f);
        this.tail.yRot = this.lerpTo(this.tail.yRot, -0.1f + 0.1f * f6);
        this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.6f + 0.05f * f7);
        this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -this.topGills.xRot);
        this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
        this.lerpPart(this.leftHindLeg, 1.1f, 1.0f, 0.0f);
        this.lerpPart(this.leftFrontLeg, 0.8f, 2.3f, -0.5f);
        this.applyMirrorLegRotations();
        this.body.xRot = this.lerpTo(0.2f, this.body.xRot, 0.0f);
        this.body.yRot = this.lerpTo(this.body.yRot, f2 * ((float)Math.PI / 180));
        this.body.zRot = this.lerpTo(this.body.zRot, 0.0f);
    }

    private void setupGroundCrawlingAnimation(float f, float f2) {
        float f3 = f * 0.11f;
        float f4 = Mth.cos(f3);
        float f5 = (f4 * f4 - 2.0f * f4) / 5.0f;
        float f6 = 0.7f * f4;
        this.head.xRot = this.lerpTo(this.head.xRot, 0.0f);
        this.head.yRot = this.lerpTo(this.head.yRot, 0.09f * f4);
        this.head.zRot = this.lerpTo(this.head.zRot, 0.0f);
        this.tail.yRot = this.lerpTo(this.tail.yRot, this.head.yRot);
        this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.6f - 0.08f * (f4 * f4 + 2.0f * Mth.sin(f3)));
        this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -this.topGills.xRot);
        this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
        this.lerpPart(this.leftHindLeg, 0.9424779f, 1.5f - f5, -0.1f);
        this.lerpPart(this.leftFrontLeg, 1.0995574f, 1.5707964f - f6, 0.0f);
        this.lerpPart(this.rightHindLeg, this.leftHindLeg.xRot, -1.0f - f5, 0.0f);
        this.lerpPart(this.rightFrontLeg, this.leftFrontLeg.xRot, -1.5707964f - f6, 0.0f);
        this.body.xRot = this.lerpTo(0.2f, this.body.xRot, 0.0f);
        this.body.yRot = this.lerpTo(this.body.yRot, f2 * ((float)Math.PI / 180));
        this.body.zRot = this.lerpTo(this.body.zRot, 0.0f);
    }

    private void setupWaterHoveringAnimation(float f) {
        float f2 = f * 0.075f;
        float f3 = Mth.cos(f2);
        float f4 = Mth.sin(f2) * 0.15f;
        this.body.xRot = this.lerpTo(this.body.xRot, -0.15f + 0.075f * f3);
        this.body.y -= f4;
        this.head.xRot = this.lerpTo(this.head.xRot, -this.body.xRot);
        this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.2f * f3);
        this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -0.3f * f3 - 0.19f);
        this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
        this.lerpPart(this.leftHindLeg, 2.3561945f - f3 * 0.11f, 0.47123894f, 1.7278761f);
        this.lerpPart(this.leftFrontLeg, 0.7853982f - f3 * 0.2f, 2.042035f, 0.0f);
        this.applyMirrorLegRotations();
        this.tail.yRot = this.lerpTo(this.tail.yRot, 0.5f * f3);
        this.head.yRot = this.lerpTo(this.head.yRot, 0.0f);
        this.head.zRot = this.lerpTo(this.head.zRot, 0.0f);
    }

    private void setupSwimmingAnimation(float f, float f2) {
        float f3 = f * 0.33f;
        float f4 = Mth.sin(f3);
        float f5 = Mth.cos(f3);
        float f6 = 0.13f * f4;
        this.body.xRot = this.lerpTo(0.1f, this.body.xRot, f2 * ((float)Math.PI / 180) + f6);
        this.head.xRot = -f6 * 1.8f;
        this.body.y -= 0.45f * f5;
        this.topGills.xRot = this.lerpTo(this.topGills.xRot, -0.5f * f4 - 0.8f);
        this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, 0.3f * f4 + 0.9f);
        this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
        this.tail.yRot = this.lerpTo(this.tail.yRot, 0.3f * Mth.cos(f3 * 0.9f));
        this.lerpPart(this.leftHindLeg, 1.8849558f, -0.4f * f4, 1.5707964f);
        this.lerpPart(this.leftFrontLeg, 1.8849558f, -0.2f * f5 - 0.1f, 1.5707964f);
        this.applyMirrorLegRotations();
        this.head.yRot = this.lerpTo(this.head.yRot, 0.0f);
        this.head.zRot = this.lerpTo(this.head.zRot, 0.0f);
    }

    private void setupPlayDeadAnimation(float f) {
        this.lerpPart(this.leftHindLeg, 1.4137167f, 1.0995574f, 0.7853982f);
        this.lerpPart(this.leftFrontLeg, 0.7853982f, 2.042035f, 0.0f);
        this.body.xRot = this.lerpTo(this.body.xRot, -0.15f);
        this.body.zRot = this.lerpTo(this.body.zRot, 0.35f);
        this.applyMirrorLegRotations();
        this.body.yRot = this.lerpTo(this.body.yRot, f * ((float)Math.PI / 180));
        this.head.xRot = this.lerpTo(this.head.xRot, 0.0f);
        this.head.yRot = this.lerpTo(this.head.yRot, 0.0f);
        this.head.zRot = this.lerpTo(this.head.zRot, 0.0f);
        this.tail.yRot = this.lerpTo(this.tail.yRot, 0.0f);
        this.lerpPart(this.topGills, 0.0f, 0.0f, 0.0f);
        this.lerpPart(this.leftGills, 0.0f, 0.0f, 0.0f);
        this.lerpPart(this.rightGills, 0.0f, 0.0f, 0.0f);
    }

    private void applyMirrorLegRotations() {
        this.lerpPart(this.rightHindLeg, this.leftHindLeg.xRot, -this.leftHindLeg.yRot, -this.leftHindLeg.zRot);
        this.lerpPart(this.rightFrontLeg, this.leftFrontLeg.xRot, -this.leftFrontLeg.yRot, -this.leftFrontLeg.zRot);
    }
}

