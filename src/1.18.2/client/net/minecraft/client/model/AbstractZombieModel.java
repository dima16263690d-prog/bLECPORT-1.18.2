/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Monster;

public abstract class AbstractZombieModel<T extends Monster>
extends HumanoidModel<T> {
    protected AbstractZombieModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        super.setupAnim(t, f, f2, f3, f4, f5);
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, this.isAggressive(t), this.attackTime, f3);
    }

    public abstract boolean isAggressive(T var1);
}

