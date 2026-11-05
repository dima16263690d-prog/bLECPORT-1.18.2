/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

public class MinecartModel<T extends Entity>
extends HierarchicalModel<T> {
    private final ModelPart root;

    public MinecartModel(ModelPart modelPart) {
        this.root = modelPart;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int n = 20;
        int n2 = 8;
        int n3 = 16;
        int n4 = 4;
        partDefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 10).addBox(-10.0f, -8.0f, -1.0f, 20.0f, 16.0f, 2.0f), PartPose.offsetAndRotation(0.0f, 4.0f, 0.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -9.0f, -1.0f, 16.0f, 8.0f, 2.0f), PartPose.offsetAndRotation(-9.0f, 4.0f, 0.0f, 0.0f, 4.712389f, 0.0f));
        partDefinition.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -9.0f, -1.0f, 16.0f, 8.0f, 2.0f), PartPose.offsetAndRotation(9.0f, 4.0f, 0.0f, 0.0f, 1.5707964f, 0.0f));
        partDefinition.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -9.0f, -1.0f, 16.0f, 8.0f, 2.0f), PartPose.offsetAndRotation(0.0f, 4.0f, -7.0f, 0.0f, (float)Math.PI, 0.0f));
        partDefinition.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -9.0f, -1.0f, 16.0f, 8.0f, 2.0f), PartPose.offset(0.0f, 4.0f, 7.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

