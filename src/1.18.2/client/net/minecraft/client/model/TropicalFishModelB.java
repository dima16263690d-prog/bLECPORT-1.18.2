/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class TropicalFishModelB<T extends Entity>
extends ColorableHierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart tail;

    public TropicalFishModelB(ModelPart modelPart) {
        this.root = modelPart;
        this.tail = modelPart.getChild("tail");
    }

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int n = 19;
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 20).addBox(-1.0f, -3.0f, -3.0f, 2.0f, 6.0f, 6.0f, cubeDeformation), PartPose.offset(0.0f, 19.0f, 0.0f));
        partDefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(21, 16).addBox(0.0f, -3.0f, 0.0f, 0.0f, 6.0f, 5.0f, cubeDeformation), PartPose.offset(0.0f, 19.0f, 3.0f));
        partDefinition.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(2, 16).addBox(-2.0f, 0.0f, 0.0f, 2.0f, 2.0f, 0.0f, cubeDeformation), PartPose.offsetAndRotation(-1.0f, 20.0f, 0.0f, 0.0f, 0.7853982f, 0.0f));
        partDefinition.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(2, 12).addBox(0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 0.0f, cubeDeformation), PartPose.offsetAndRotation(1.0f, 20.0f, 0.0f, 0.0f, -0.7853982f, 0.0f));
        partDefinition.addOrReplaceChild("top_fin", CubeListBuilder.create().texOffs(20, 11).addBox(0.0f, -4.0f, 0.0f, 0.0f, 4.0f, 6.0f, cubeDeformation), PartPose.offset(0.0f, 16.0f, -3.0f));
        partDefinition.addOrReplaceChild("bottom_fin", CubeListBuilder.create().texOffs(20, 21).addBox(0.0f, 0.0f, 0.0f, 0.0f, 4.0f, 6.0f, cubeDeformation), PartPose.offset(0.0f, 22.0f, -3.0f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        float f6 = 1.0f;
        if (!((Entity)t).isInWater()) {
            f6 = 1.5f;
        }
        this.tail.yRot = -f6 * 0.45f * Mth.sin(0.6f * f3);
    }
}

