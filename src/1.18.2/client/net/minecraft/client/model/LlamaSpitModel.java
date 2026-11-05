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

public class LlamaSpitModel<T extends Entity>
extends HierarchicalModel<T> {
    private static final String MAIN = "main";
    private final ModelPart root;

    public LlamaSpitModel(ModelPart modelPart) {
        this.root = modelPart;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int n = 2;
        partDefinition.addOrReplaceChild(MAIN, CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, -4.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, 0.0f, -4.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(2.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, 2.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, 0.0f, 2.0f, 2.0f, 2.0f, 2.0f), PartPose.ZERO);
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

