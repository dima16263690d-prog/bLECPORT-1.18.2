/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class HumanoidModel<T extends LivingEntity>
extends AgeableListModel<T>
implements ArmedModel,
HeadedModel {
    public static final float OVERLAY_SCALE = 0.25f;
    public static final float HAT_OVERLAY_SCALE = 0.5f;
    private static final float SPYGLASS_ARM_ROT_Y = 0.2617994f;
    private static final float SPYGLASS_ARM_ROT_X = 1.9198622f;
    private static final float SPYGLASS_ARM_CROUCH_ROT_X = 0.2617994f;
    public final ModelPart head;
    public final ModelPart hat;
    public final ModelPart body;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;
    public ArmPose leftArmPose = ArmPose.EMPTY;
    public ArmPose rightArmPose = ArmPose.EMPTY;
    public boolean crouching;
    public float swimAmount;

    public HumanoidModel(ModelPart modelPart) {
        this(modelPart, RenderType::entityCutoutNoCull);
    }

    public HumanoidModel(ModelPart modelPart, Function<ResourceLocation, RenderType> function) {
        super(function, true, 16.0f, 0.0f, 2.0f, 2.0f, 24.0f);
        this.head = modelPart.getChild("head");
        this.hat = modelPart.getChild("hat");
        this.body = modelPart.getChild("body");
        this.rightArm = modelPart.getChild("right_arm");
        this.leftArm = modelPart.getChild("left_arm");
        this.rightLeg = modelPart.getChild("right_leg");
        this.leftLeg = modelPart.getChild("left_leg");
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, float f) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation), PartPose.offset(0.0f, 0.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation.extend(0.5f)), PartPose.offset(0.0f, 0.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(0.0f, 0.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(-5.0f, 2.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(5.0f, 2.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(-1.9f, 12.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(1.9f, 12.0f + f, 0.0f));
        return meshDefinition;
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of((Object)this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of((Object)this.body, (Object)this.rightArm, (Object)this.leftArm, (Object)this.rightLeg, (Object)this.leftLeg, (Object)this.hat);
    }

    @Override
    public void prepareMobModel(T t, float f, float f2, float f3) {
        this.swimAmount = ((LivingEntity)t).getSwimAmount(f3);
        super.prepareMobModel(t, f, f2, f3);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        boolean bl;
        boolean bl2 = ((LivingEntity)t).getFallFlyingTicks() > 4;
        boolean bl3 = ((LivingEntity)t).isVisuallySwimming();
        this.head.yRot = f4 * ((float)Math.PI / 180);
        this.head.xRot = bl2 ? -0.7853982f : (this.swimAmount > 0.0f ? (bl3 ? this.rotlerpRad(this.swimAmount, this.head.xRot, -0.7853982f) : this.rotlerpRad(this.swimAmount, this.head.xRot, f5 * ((float)Math.PI / 180))) : f5 * ((float)Math.PI / 180));
        this.body.yRot = 0.0f;
        this.rightArm.z = 0.0f;
        this.rightArm.x = -5.0f;
        this.leftArm.z = 0.0f;
        this.leftArm.x = 5.0f;
        float f6 = 1.0f;
        if (bl2) {
            f6 = (float)((Entity)t).getDeltaMovement().lengthSqr();
            f6 /= 0.2f;
            f6 *= f6 * f6;
        }
        if (f6 < 1.0f) {
            f6 = 1.0f;
        }
        this.rightArm.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 2.0f * f2 * 0.5f / f6;
        this.leftArm.xRot = Mth.cos(f * 0.6662f) * 2.0f * f2 * 0.5f / f6;
        this.rightArm.zRot = 0.0f;
        this.leftArm.zRot = 0.0f;
        this.rightLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * f2 / f6;
        this.leftLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * f2 / f6;
        this.rightLeg.yRot = 0.0f;
        this.leftLeg.yRot = 0.0f;
        this.rightLeg.zRot = 0.0f;
        this.leftLeg.zRot = 0.0f;
        if (this.riding) {
            this.rightArm.xRot += -0.62831855f;
            this.leftArm.xRot += -0.62831855f;
            this.rightLeg.xRot = -1.4137167f;
            this.rightLeg.yRot = 0.31415927f;
            this.rightLeg.zRot = 0.07853982f;
            this.leftLeg.xRot = -1.4137167f;
            this.leftLeg.yRot = -0.31415927f;
            this.leftLeg.zRot = -0.07853982f;
        }
        this.rightArm.yRot = 0.0f;
        this.leftArm.yRot = 0.0f;
        boolean bl4 = bl = ((LivingEntity)t).getMainArm() == HumanoidArm.RIGHT;
        if (((LivingEntity)t).isUsingItem()) {
            boolean bl5 = var11_11 = ((LivingEntity)t).getUsedItemHand() == InteractionHand.MAIN_HAND;
            if (var11_11 == bl) {
                this.poseRightArm(t);
            } else {
                this.poseLeftArm(t);
            }
        } else {
            boolean bl6 = var11_11 = bl ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
            if (bl != var11_11) {
                this.poseLeftArm(t);
                this.poseRightArm(t);
            } else {
                this.poseRightArm(t);
                this.poseLeftArm(t);
            }
        }
        this.setupAttackAnimation(t, f3);
        if (this.crouching) {
            this.body.xRot = 0.5f;
            this.rightArm.xRot += 0.4f;
            this.leftArm.xRot += 0.4f;
            this.rightLeg.z = 4.0f;
            this.leftLeg.z = 4.0f;
            this.rightLeg.y = 12.2f;
            this.leftLeg.y = 12.2f;
            this.head.y = 4.2f;
            this.body.y = 3.2f;
            this.leftArm.y = 5.2f;
            this.rightArm.y = 5.2f;
        } else {
            this.body.xRot = 0.0f;
            this.rightLeg.z = 0.1f;
            this.leftLeg.z = 0.1f;
            this.rightLeg.y = 12.0f;
            this.leftLeg.y = 12.0f;
            this.head.y = 0.0f;
            this.body.y = 0.0f;
            this.leftArm.y = 2.0f;
            this.rightArm.y = 2.0f;
        }
        if (this.rightArmPose != ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(this.rightArm, f3, 1.0f);
        }
        if (this.leftArmPose != ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(this.leftArm, f3, -1.0f);
        }
        if (this.swimAmount > 0.0f) {
            float f7;
            float f8;
            float f9 = f % 26.0f;
            HumanoidArm humanoidArm = this.getAttackArm(t);
            float f10 = humanoidArm == HumanoidArm.RIGHT && this.attackTime > 0.0f ? 0.0f : this.swimAmount;
            float f11 = f8 = humanoidArm == HumanoidArm.LEFT && this.attackTime > 0.0f ? 0.0f : this.swimAmount;
            if (!((LivingEntity)t).isUsingItem()) {
                if (f9 < 14.0f) {
                    this.leftArm.xRot = this.rotlerpRad(f8, this.leftArm.xRot, 0.0f);
                    this.rightArm.xRot = Mth.lerp(f10, this.rightArm.xRot, 0.0f);
                    this.leftArm.yRot = this.rotlerpRad(f8, this.leftArm.yRot, (float)Math.PI);
                    this.rightArm.yRot = Mth.lerp(f10, this.rightArm.yRot, (float)Math.PI);
                    this.leftArm.zRot = this.rotlerpRad(f8, this.leftArm.zRot, (float)Math.PI + 1.8707964f * this.quadraticArmUpdate(f9) / this.quadraticArmUpdate(14.0f));
                    this.rightArm.zRot = Mth.lerp(f10, this.rightArm.zRot, (float)Math.PI - 1.8707964f * this.quadraticArmUpdate(f9) / this.quadraticArmUpdate(14.0f));
                } else if (f9 >= 14.0f && f9 < 22.0f) {
                    f7 = (f9 - 14.0f) / 8.0f;
                    this.leftArm.xRot = this.rotlerpRad(f8, this.leftArm.xRot, 1.5707964f * f7);
                    this.rightArm.xRot = Mth.lerp(f10, this.rightArm.xRot, 1.5707964f * f7);
                    this.leftArm.yRot = this.rotlerpRad(f8, this.leftArm.yRot, (float)Math.PI);
                    this.rightArm.yRot = Mth.lerp(f10, this.rightArm.yRot, (float)Math.PI);
                    this.leftArm.zRot = this.rotlerpRad(f8, this.leftArm.zRot, 5.012389f - 1.8707964f * f7);
                    this.rightArm.zRot = Mth.lerp(f10, this.rightArm.zRot, 1.2707963f + 1.8707964f * f7);
                } else if (f9 >= 22.0f && f9 < 26.0f) {
                    f7 = (f9 - 22.0f) / 4.0f;
                    this.leftArm.xRot = this.rotlerpRad(f8, this.leftArm.xRot, 1.5707964f - 1.5707964f * f7);
                    this.rightArm.xRot = Mth.lerp(f10, this.rightArm.xRot, 1.5707964f - 1.5707964f * f7);
                    this.leftArm.yRot = this.rotlerpRad(f8, this.leftArm.yRot, (float)Math.PI);
                    this.rightArm.yRot = Mth.lerp(f10, this.rightArm.yRot, (float)Math.PI);
                    this.leftArm.zRot = this.rotlerpRad(f8, this.leftArm.zRot, (float)Math.PI);
                    this.rightArm.zRot = Mth.lerp(f10, this.rightArm.zRot, (float)Math.PI);
                }
            }
            f7 = 0.3f;
            float f12 = 0.33333334f;
            this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3f * Mth.cos(f * 0.33333334f + (float)Math.PI));
            this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3f * Mth.cos(f * 0.33333334f));
        }
        this.hat.copyFrom(this.head);
    }

    private void poseRightArm(T t) {
        switch (this.rightArmPose) {
            case EMPTY: {
                this.rightArm.yRot = 0.0f;
                break;
            }
            case BLOCK: {
                this.rightArm.xRot = this.rightArm.xRot * 0.5f - 0.9424779f;
                this.rightArm.yRot = -0.5235988f;
                break;
            }
            case ITEM: {
                this.rightArm.xRot = this.rightArm.xRot * 0.5f - 0.31415927f;
                this.rightArm.yRot = 0.0f;
                break;
            }
            case THROW_SPEAR: {
                this.rightArm.xRot = this.rightArm.xRot * 0.5f - (float)Math.PI;
                this.rightArm.yRot = 0.0f;
                break;
            }
            case BOW_AND_ARROW: {
                this.rightArm.yRot = -0.1f + this.head.yRot;
                this.leftArm.yRot = 0.1f + this.head.yRot + 0.4f;
                this.rightArm.xRot = -1.5707964f + this.head.xRot;
                this.leftArm.xRot = -1.5707964f + this.head.xRot;
                break;
            }
            case CROSSBOW_CHARGE: {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, t, true);
                break;
            }
            case CROSSBOW_HOLD: {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
                break;
            }
            case SPYGLASS: {
                this.rightArm.xRot = Mth.clamp(this.head.xRot - 1.9198622f - (((Entity)t).isCrouching() ? 0.2617994f : 0.0f), -2.4f, 3.3f);
                this.rightArm.yRot = this.head.yRot - 0.2617994f;
            }
        }
    }

    private void poseLeftArm(T t) {
        switch (this.leftArmPose) {
            case EMPTY: {
                this.leftArm.yRot = 0.0f;
                break;
            }
            case BLOCK: {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - 0.9424779f;
                this.leftArm.yRot = 0.5235988f;
                break;
            }
            case ITEM: {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - 0.31415927f;
                this.leftArm.yRot = 0.0f;
                break;
            }
            case THROW_SPEAR: {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - (float)Math.PI;
                this.leftArm.yRot = 0.0f;
                break;
            }
            case BOW_AND_ARROW: {
                this.rightArm.yRot = -0.1f + this.head.yRot - 0.4f;
                this.leftArm.yRot = 0.1f + this.head.yRot;
                this.rightArm.xRot = -1.5707964f + this.head.xRot;
                this.leftArm.xRot = -1.5707964f + this.head.xRot;
                break;
            }
            case CROSSBOW_CHARGE: {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, t, false);
                break;
            }
            case CROSSBOW_HOLD: {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
                break;
            }
            case SPYGLASS: {
                this.leftArm.xRot = Mth.clamp(this.head.xRot - 1.9198622f - (((Entity)t).isCrouching() ? 0.2617994f : 0.0f), -2.4f, 3.3f);
                this.leftArm.yRot = this.head.yRot + 0.2617994f;
            }
        }
    }

    protected void setupAttackAnimation(T t, float f) {
        if (this.attackTime <= 0.0f) {
            return;
        }
        HumanoidArm humanoidArm = this.getAttackArm(t);
        ModelPart modelPart = this.getArm(humanoidArm);
        float f2 = this.attackTime;
        this.body.yRot = Mth.sin(Mth.sqrt(f2) * ((float)Math.PI * 2)) * 0.2f;
        if (humanoidArm == HumanoidArm.LEFT) {
            this.body.yRot *= -1.0f;
        }
        this.rightArm.z = Mth.sin(this.body.yRot) * 5.0f;
        this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0f;
        this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0f;
        this.leftArm.x = Mth.cos(this.body.yRot) * 5.0f;
        this.rightArm.yRot += this.body.yRot;
        this.leftArm.yRot += this.body.yRot;
        this.leftArm.xRot += this.body.yRot;
        f2 = 1.0f - this.attackTime;
        f2 *= f2;
        f2 *= f2;
        f2 = 1.0f - f2;
        float f3 = Mth.sin(f2 * (float)Math.PI);
        float f4 = Mth.sin(this.attackTime * (float)Math.PI) * -(this.head.xRot - 0.7f) * 0.75f;
        modelPart.xRot -= f3 * 1.2f + f4;
        modelPart.yRot += this.body.yRot * 2.0f;
        modelPart.zRot += Mth.sin(this.attackTime * (float)Math.PI) * -0.4f;
    }

    protected float rotlerpRad(float f, float f2, float f3) {
        float f4 = (f3 - f2) % ((float)Math.PI * 2);
        if (f4 < (float)(-Math.PI)) {
            f4 += (float)Math.PI * 2;
        }
        if (f4 >= (float)Math.PI) {
            f4 -= (float)Math.PI * 2;
        }
        return f2 + f * f4;
    }

    private float quadraticArmUpdate(float f) {
        return -65.0f * f + f * f;
    }

    @Override
    public void copyPropertiesTo(HumanoidModel<T> humanoidModel) {
        super.copyPropertiesTo(humanoidModel);
        humanoidModel.leftArmPose = this.leftArmPose;
        humanoidModel.rightArmPose = this.rightArmPose;
        humanoidModel.crouching = this.crouching;
        humanoidModel.head.copyFrom(this.head);
        humanoidModel.hat.copyFrom(this.hat);
        humanoidModel.body.copyFrom(this.body);
        humanoidModel.rightArm.copyFrom(this.rightArm);
        humanoidModel.leftArm.copyFrom(this.leftArm);
        humanoidModel.rightLeg.copyFrom(this.rightLeg);
        humanoidModel.leftLeg.copyFrom(this.leftLeg);
    }

    public void setAllVisible(boolean bl) {
        this.head.visible = bl;
        this.hat.visible = bl;
        this.body.visible = bl;
        this.rightArm.visible = bl;
        this.leftArm.visible = bl;
        this.rightLeg.visible = bl;
        this.leftLeg.visible = bl;
    }

    @Override
    public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
        this.getArm(humanoidArm).translateAndRotate(poseStack);
    }

    protected ModelPart getArm(HumanoidArm humanoidArm) {
        if (humanoidArm == HumanoidArm.LEFT) {
            return this.leftArm;
        }
        return this.rightArm;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    private HumanoidArm getAttackArm(T t) {
        HumanoidArm humanoidArm = ((LivingEntity)t).getMainArm();
        return ((LivingEntity)t).swingingArm == InteractionHand.MAIN_HAND ? humanoidArm : humanoidArm.getOpposite();
    }

    public static enum ArmPose {
        EMPTY(false),
        ITEM(false),
        BLOCK(false),
        BOW_AND_ARROW(true),
        THROW_SPEAR(false),
        CROSSBOW_CHARGE(true),
        CROSSBOW_HOLD(true),
        SPYGLASS(false);

        private final boolean twoHanded;

        private ArmPose(boolean bl) {
            this.twoHanded = bl;
        }

        public boolean isTwoHanded() {
            return this.twoHanded;
        }
    }
}

