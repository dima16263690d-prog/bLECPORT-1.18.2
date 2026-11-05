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
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ElytraModel<T extends LivingEntity>
extends AgeableListModel<T> {
    private final ModelPart rightWing;
    private final ModelPart leftWing;

    public ElytraModel(ModelPart modelPart) {
        this.leftWing = modelPart.getChild("left_wing");
        this.rightWing = modelPart.getChild("right_wing");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeDeformation cubeDeformation = new CubeDeformation(1.0f);
        partDefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(22, 0).addBox(-10.0f, 0.0f, 0.0f, 10.0f, 20.0f, 2.0f, cubeDeformation), PartPose.offsetAndRotation(5.0f, 0.0f, 0.0f, 0.2617994f, 0.0f, -0.2617994f));
        partDefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(22, 0).mirror().addBox(0.0f, 0.0f, 0.0f, 10.0f, 20.0f, 2.0f, cubeDeformation), PartPose.offsetAndRotation(-5.0f, 0.0f, 0.0f, 0.2617994f, 0.0f, 0.2617994f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of((Object)this.leftWing, (Object)this.rightWing);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        float f6 = 0.2617994f;
        float f7 = -0.2617994f;
        float f8 = 0.0f;
        float f9 = 0.0f;
        if (((LivingEntity)t).isFallFlying()) {
            float f10 = 1.0f;
            Vec3 vec3 = ((Entity)t).getDeltaMovement();
            if (vec3.y < 0.0) {
                Vec3 vec32 = vec3.normalize();
                f10 = 1.0f - (float)Math.pow(-vec32.y, 1.5);
            }
            f6 = f10 * 0.34906584f + (1.0f - f10) * f6;
            f7 = f10 * -1.5707964f + (1.0f - f10) * f7;
        } else if (((Entity)t).isCrouching()) {
            f6 = 0.6981317f;
            f7 = -0.7853982f;
            f8 = 3.0f;
            f9 = 0.08726646f;
        }
        this.leftWing.y = f8;
        if (t instanceof AbstractClientPlayer) {
            AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)t;
            abstractClientPlayer.elytraRotX += (f6 - abstractClientPlayer.elytraRotX) * 0.1f;
            abstractClientPlayer.elytraRotY += (f9 - abstractClientPlayer.elytraRotY) * 0.1f;
            abstractClientPlayer.elytraRotZ += (f7 - abstractClientPlayer.elytraRotZ) * 0.1f;
            this.leftWing.xRot = abstractClientPlayer.elytraRotX;
            this.leftWing.yRot = abstractClientPlayer.elytraRotY;
            this.leftWing.zRot = abstractClientPlayer.elytraRotZ;
        } else {
            this.leftWing.xRot = f6;
            this.leftWing.zRot = f7;
            this.leftWing.yRot = f9;
        }
        this.rightWing.yRot = -this.leftWing.yRot;
        this.rightWing.y = this.leftWing.y;
        this.rightWing.xRot = this.leftWing.xRot;
        this.rightWing.zRot = -this.leftWing.zRot;
    }
}

