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
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class HorseModel<T extends AbstractHorse>
extends AgeableListModel<T> {
    private static final float DEG_125 = 2.1816616f;
    private static final float DEG_60 = 1.0471976f;
    private static final float DEG_45 = 0.7853982f;
    private static final float DEG_30 = 0.5235988f;
    private static final float DEG_15 = 0.2617994f;
    protected static final String HEAD_PARTS = "head_parts";
    private static final String LEFT_HIND_BABY_LEG = "left_hind_baby_leg";
    private static final String RIGHT_HIND_BABY_LEG = "right_hind_baby_leg";
    private static final String LEFT_FRONT_BABY_LEG = "left_front_baby_leg";
    private static final String RIGHT_FRONT_BABY_LEG = "right_front_baby_leg";
    private static final String SADDLE = "saddle";
    private static final String LEFT_SADDLE_MOUTH = "left_saddle_mouth";
    private static final String LEFT_SADDLE_LINE = "left_saddle_line";
    private static final String RIGHT_SADDLE_MOUTH = "right_saddle_mouth";
    private static final String RIGHT_SADDLE_LINE = "right_saddle_line";
    private static final String HEAD_SADDLE = "head_saddle";
    private static final String MOUTH_SADDLE_WRAP = "mouth_saddle_wrap";
    protected final ModelPart body;
    protected final ModelPart headParts;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightHindBabyLeg;
    private final ModelPart leftHindBabyLeg;
    private final ModelPart rightFrontBabyLeg;
    private final ModelPart leftFrontBabyLeg;
    private final ModelPart tail;
    private final ModelPart[] saddleParts;
    private final ModelPart[] ridingParts;

    public HorseModel(ModelPart modelPart) {
        super(true, 16.2f, 1.36f, 2.7272f, 2.0f, 20.0f);
        this.body = modelPart.getChild("body");
        this.headParts = modelPart.getChild(HEAD_PARTS);
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.rightHindBabyLeg = modelPart.getChild(RIGHT_HIND_BABY_LEG);
        this.leftHindBabyLeg = modelPart.getChild(LEFT_HIND_BABY_LEG);
        this.rightFrontBabyLeg = modelPart.getChild(RIGHT_FRONT_BABY_LEG);
        this.leftFrontBabyLeg = modelPart.getChild(LEFT_FRONT_BABY_LEG);
        this.tail = this.body.getChild("tail");
        ModelPart modelPart2 = this.body.getChild(SADDLE);
        ModelPart modelPart3 = this.headParts.getChild(LEFT_SADDLE_MOUTH);
        ModelPart modelPart4 = this.headParts.getChild(RIGHT_SADDLE_MOUTH);
        ModelPart modelPart5 = this.headParts.getChild(LEFT_SADDLE_LINE);
        ModelPart modelPart6 = this.headParts.getChild(RIGHT_SADDLE_LINE);
        ModelPart modelPart7 = this.headParts.getChild(HEAD_SADDLE);
        ModelPart modelPart8 = this.headParts.getChild(MOUTH_SADDLE_WRAP);
        this.saddleParts = new ModelPart[]{modelPart2, modelPart3, modelPart4, modelPart7, modelPart8};
        this.ridingParts = new ModelPart[]{modelPart5, modelPart6};
    }

    public static MeshDefinition createBodyMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 32).addBox(-5.0f, -8.0f, -17.0f, 10.0f, 10.0f, 22.0f, new CubeDeformation(0.05f)), PartPose.offset(0.0f, 11.0f, 5.0f));
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild(HEAD_PARTS, CubeListBuilder.create().texOffs(0, 35).addBox(-2.05f, -6.0f, -2.0f, 4.0f, 12.0f, 7.0f), PartPose.offsetAndRotation(0.0f, 4.0f, -12.0f, 0.5235988f, 0.0f, 0.0f));
        PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0f, -11.0f, -2.0f, 6.0f, 5.0f, 7.0f, cubeDeformation), PartPose.ZERO);
        partDefinition3.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(56, 36).addBox(-1.0f, -11.0f, 5.01f, 2.0f, 16.0f, 2.0f, cubeDeformation), PartPose.ZERO);
        partDefinition3.addOrReplaceChild("upper_mouth", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0f, -11.0f, -7.0f, 4.0f, 5.0f, 5.0f, cubeDeformation), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, cubeDeformation), PartPose.offset(4.0f, 14.0f, 7.0f));
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, cubeDeformation), PartPose.offset(-4.0f, 14.0f, 7.0f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, cubeDeformation), PartPose.offset(4.0f, 14.0f, -12.0f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, cubeDeformation), PartPose.offset(-4.0f, 14.0f, -12.0f));
        CubeDeformation cubeDeformation2 = cubeDeformation.extend(0.0f, 5.5f, 0.0f);
        partDefinition.addOrReplaceChild(LEFT_HIND_BABY_LEG, CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, cubeDeformation2), PartPose.offset(4.0f, 14.0f, 7.0f));
        partDefinition.addOrReplaceChild(RIGHT_HIND_BABY_LEG, CubeListBuilder.create().texOffs(48, 21).addBox(-1.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, cubeDeformation2), PartPose.offset(-4.0f, 14.0f, 7.0f));
        partDefinition.addOrReplaceChild(LEFT_FRONT_BABY_LEG, CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, cubeDeformation2), PartPose.offset(4.0f, 14.0f, -12.0f));
        partDefinition.addOrReplaceChild(RIGHT_FRONT_BABY_LEG, CubeListBuilder.create().texOffs(48, 21).addBox(-1.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, cubeDeformation2), PartPose.offset(-4.0f, 14.0f, -12.0f));
        partDefinition2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(42, 36).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 14.0f, 4.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, -5.0f, 2.0f, 0.5235988f, 0.0f, 0.0f));
        partDefinition2.addOrReplaceChild(SADDLE, CubeListBuilder.create().texOffs(26, 0).addBox(-5.0f, -8.0f, -9.0f, 10.0f, 9.0f, 9.0f, new CubeDeformation(0.5f)), PartPose.ZERO);
        partDefinition3.addOrReplaceChild(LEFT_SADDLE_MOUTH, CubeListBuilder.create().texOffs(29, 5).addBox(2.0f, -9.0f, -6.0f, 1.0f, 2.0f, 2.0f, cubeDeformation), PartPose.ZERO);
        partDefinition3.addOrReplaceChild(RIGHT_SADDLE_MOUTH, CubeListBuilder.create().texOffs(29, 5).addBox(-3.0f, -9.0f, -6.0f, 1.0f, 2.0f, 2.0f, cubeDeformation), PartPose.ZERO);
        partDefinition3.addOrReplaceChild(LEFT_SADDLE_LINE, CubeListBuilder.create().texOffs(32, 2).addBox(3.1f, -6.0f, -8.0f, 0.0f, 3.0f, 16.0f, cubeDeformation), PartPose.rotation(-0.5235988f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild(RIGHT_SADDLE_LINE, CubeListBuilder.create().texOffs(32, 2).addBox(-3.1f, -6.0f, -8.0f, 0.0f, 3.0f, 16.0f, cubeDeformation), PartPose.rotation(-0.5235988f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild(HEAD_SADDLE, CubeListBuilder.create().texOffs(1, 1).addBox(-3.0f, -11.0f, -1.9f, 6.0f, 5.0f, 6.0f, new CubeDeformation(0.2f)), PartPose.ZERO);
        partDefinition3.addOrReplaceChild(MOUTH_SADDLE_WRAP, CubeListBuilder.create().texOffs(19, 0).addBox(-2.0f, -11.0f, -4.0f, 4.0f, 5.0f, 2.0f, new CubeDeformation(0.2f)), PartPose.ZERO);
        partDefinition4.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(19, 16).addBox(0.55f, -13.0f, 4.0f, 2.0f, 3.0f, 1.0f, new CubeDeformation(-0.001f)), PartPose.ZERO);
        partDefinition4.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(19, 16).addBox(-2.55f, -13.0f, 4.0f, 2.0f, 3.0f, 1.0f, new CubeDeformation(-0.001f)), PartPose.ZERO);
        return meshDefinition;
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        boolean bl = ((AbstractHorse)t).isSaddled();
        boolean bl2 = ((Entity)t).isVehicle();
        for (ModelPart modelPart : this.saddleParts) {
            modelPart.visible = bl;
        }
        for (ModelPart modelPart : this.ridingParts) {
            modelPart.visible = bl2 && bl;
        }
        this.body.y = 11.0f;
    }

    @Override
    public Iterable<ModelPart> headParts() {
        return ImmutableList.of((Object)this.headParts);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of((Object)this.body, (Object)this.rightHindLeg, (Object)this.leftHindLeg, (Object)this.rightFrontLeg, (Object)this.leftFrontLeg, (Object)this.rightHindBabyLeg, (Object)this.leftHindBabyLeg, (Object)this.rightFrontBabyLeg, (Object)this.leftFrontBabyLeg);
    }

    @Override
    public void prepareMobModel(T t, float f, float f2, float f3) {
        super.prepareMobModel(t, f, f2, f3);
        float f4 = Mth.rotlerp(((AbstractHorse)t).yBodyRotO, ((AbstractHorse)t).yBodyRot, f3);
        float f5 = Mth.rotlerp(((AbstractHorse)t).yHeadRotO, ((AbstractHorse)t).yHeadRot, f3);
        float f6 = Mth.lerp(f3, ((AbstractHorse)t).xRotO, ((Entity)t).getXRot());
        float f7 = f5 - f4;
        float f8 = f6 * ((float)Math.PI / 180);
        if (f7 > 20.0f) {
            f7 = 20.0f;
        }
        if (f7 < -20.0f) {
            f7 = -20.0f;
        }
        if (f2 > 0.2f) {
            f8 += Mth.cos(f * 0.4f) * 0.15f * f2;
        }
        float f9 = ((AbstractHorse)t).getEatAnim(f3);
        float f10 = ((AbstractHorse)t).getStandAnim(f3);
        float f11 = 1.0f - f10;
        float f12 = ((AbstractHorse)t).getMouthAnim(f3);
        boolean bl = ((AbstractHorse)t).tailCounter != 0;
        float f13 = (float)((AbstractHorse)t).tickCount + f3;
        this.headParts.y = 4.0f;
        this.headParts.z = -12.0f;
        this.body.xRot = 0.0f;
        this.headParts.xRot = 0.5235988f + f8;
        this.headParts.yRot = f7 * ((float)Math.PI / 180);
        float f14 = ((Entity)t).isInWater() ? 0.2f : 1.0f;
        float f15 = Mth.cos(f14 * f * 0.6662f + (float)Math.PI);
        float f16 = f15 * 0.8f * f2;
        float f17 = (1.0f - Math.max(f10, f9)) * (0.5235988f + f8 + f12 * Mth.sin(f13) * 0.05f);
        this.headParts.xRot = f10 * (0.2617994f + f8) + f9 * (2.1816616f + Mth.sin(f13) * 0.05f) + f17;
        this.headParts.yRot = f10 * f7 * ((float)Math.PI / 180) + (1.0f - Math.max(f10, f9)) * this.headParts.yRot;
        this.headParts.y = f10 * -4.0f + f9 * 11.0f + (1.0f - Math.max(f10, f9)) * this.headParts.y;
        this.headParts.z = f10 * -4.0f + f9 * -12.0f + (1.0f - Math.max(f10, f9)) * this.headParts.z;
        this.body.xRot = f10 * -0.7853982f + f11 * this.body.xRot;
        float f18 = 0.2617994f * f10;
        float f19 = Mth.cos(f13 * 0.6f + (float)Math.PI);
        this.leftFrontLeg.y = 2.0f * f10 + 14.0f * f11;
        this.leftFrontLeg.z = -6.0f * f10 - 10.0f * f11;
        this.rightFrontLeg.y = this.leftFrontLeg.y;
        this.rightFrontLeg.z = this.leftFrontLeg.z;
        float f20 = (-1.0471976f + f19) * f10 + f16 * f11;
        float f21 = (-1.0471976f - f19) * f10 - f16 * f11;
        this.leftHindLeg.xRot = f18 - f15 * 0.5f * f2 * f11;
        this.rightHindLeg.xRot = f18 + f15 * 0.5f * f2 * f11;
        this.leftFrontLeg.xRot = f20;
        this.rightFrontLeg.xRot = f21;
        this.tail.xRot = 0.5235988f + f2 * 0.75f;
        this.tail.y = -5.0f + f2;
        this.tail.z = 2.0f + f2 * 2.0f;
        this.tail.yRot = bl ? Mth.cos(f13 * 0.7f) : 0.0f;
        this.rightHindBabyLeg.y = this.rightHindLeg.y;
        this.rightHindBabyLeg.z = this.rightHindLeg.z;
        this.rightHindBabyLeg.xRot = this.rightHindLeg.xRot;
        this.leftHindBabyLeg.y = this.leftHindLeg.y;
        this.leftHindBabyLeg.z = this.leftHindLeg.z;
        this.leftHindBabyLeg.xRot = this.leftHindLeg.xRot;
        this.rightFrontBabyLeg.y = this.rightFrontLeg.y;
        this.rightFrontBabyLeg.z = this.rightFrontLeg.z;
        this.rightFrontBabyLeg.xRot = this.rightFrontLeg.xRot;
        this.leftFrontBabyLeg.y = this.leftFrontLeg.y;
        this.leftFrontBabyLeg.z = this.leftFrontLeg.z;
        this.leftFrontBabyLeg.xRot = this.leftFrontLeg.xRot;
        boolean bl2 = ((AgeableMob)t).isBaby();
        this.rightHindLeg.visible = !bl2;
        this.leftHindLeg.visible = !bl2;
        this.rightFrontLeg.visible = !bl2;
        this.leftFrontLeg.visible = !bl2;
        this.rightHindBabyLeg.visible = bl2;
        this.leftHindBabyLeg.visible = bl2;
        this.rightFrontBabyLeg.visible = bl2;
        this.leftFrontBabyLeg.visible = bl2;
        this.body.y = bl2 ? 10.8f : 0.0f;
    }
}

