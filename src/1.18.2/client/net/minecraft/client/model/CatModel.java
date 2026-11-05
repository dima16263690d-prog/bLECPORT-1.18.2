/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.ModelUtils;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;

public class CatModel<T extends Cat>
extends OcelotModel<T> {
    private float lieDownAmount;
    private float lieDownAmountTail;
    private float relaxStateOneAmount;

    public CatModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void prepareMobModel(T t, float f, float f2, float f3) {
        this.lieDownAmount = ((Cat)t).getLieDownAmount(f3);
        this.lieDownAmountTail = ((Cat)t).getLieDownAmountTail(f3);
        this.relaxStateOneAmount = ((Cat)t).getRelaxStateOneAmount(f3);
        if (this.lieDownAmount <= 0.0f) {
            this.head.xRot = 0.0f;
            this.head.zRot = 0.0f;
            this.leftFrontLeg.xRot = 0.0f;
            this.leftFrontLeg.zRot = 0.0f;
            this.rightFrontLeg.xRot = 0.0f;
            this.rightFrontLeg.zRot = 0.0f;
            this.rightFrontLeg.x = -1.2f;
            this.leftHindLeg.xRot = 0.0f;
            this.rightHindLeg.xRot = 0.0f;
            this.rightHindLeg.zRot = 0.0f;
            this.rightHindLeg.x = -1.1f;
            this.rightHindLeg.y = 18.0f;
        }
        super.prepareMobModel(t, f, f2, f3);
        if (((TamableAnimal)t).isInSittingPose()) {
            this.body.xRot = 0.7853982f;
            this.body.y += -4.0f;
            this.body.z += 5.0f;
            this.head.y += -3.3f;
            this.head.z += 1.0f;
            this.tail1.y += 8.0f;
            this.tail1.z += -2.0f;
            this.tail2.y += 2.0f;
            this.tail2.z += -0.8f;
            this.tail1.xRot = 1.7278761f;
            this.tail2.xRot = 2.670354f;
            this.leftFrontLeg.xRot = -0.15707964f;
            this.leftFrontLeg.y = 16.1f;
            this.leftFrontLeg.z = -7.0f;
            this.rightFrontLeg.xRot = -0.15707964f;
            this.rightFrontLeg.y = 16.1f;
            this.rightFrontLeg.z = -7.0f;
            this.leftHindLeg.xRot = -1.5707964f;
            this.leftHindLeg.y = 21.0f;
            this.leftHindLeg.z = 1.0f;
            this.rightHindLeg.xRot = -1.5707964f;
            this.rightHindLeg.y = 21.0f;
            this.rightHindLeg.z = 1.0f;
            this.state = 3;
        }
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        super.setupAnim(t, f, f2, f3, f4, f5);
        if (this.lieDownAmount > 0.0f) {
            this.head.zRot = ModelUtils.rotlerpRad(this.head.zRot, -1.2707963f, this.lieDownAmount);
            this.head.yRot = ModelUtils.rotlerpRad(this.head.yRot, 1.2707963f, this.lieDownAmount);
            this.leftFrontLeg.xRot = -1.2707963f;
            this.rightFrontLeg.xRot = -0.47079635f;
            this.rightFrontLeg.zRot = -0.2f;
            this.rightFrontLeg.x = -0.2f;
            this.leftHindLeg.xRot = -0.4f;
            this.rightHindLeg.xRot = 0.5f;
            this.rightHindLeg.zRot = -0.5f;
            this.rightHindLeg.x = -0.3f;
            this.rightHindLeg.y = 20.0f;
            this.tail1.xRot = ModelUtils.rotlerpRad(this.tail1.xRot, 0.8f, this.lieDownAmountTail);
            this.tail2.xRot = ModelUtils.rotlerpRad(this.tail2.xRot, -0.4f, this.lieDownAmountTail);
        }
        if (this.relaxStateOneAmount > 0.0f) {
            this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, -0.58177644f, this.relaxStateOneAmount);
        }
    }
}

