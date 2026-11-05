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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class EvokerFangsModel<T extends Entity>
extends HierarchicalModel<T> {
    private static final String BASE = "base";
    private static final String UPPER_JAW = "upper_jaw";
    private static final String LOWER_JAW = "lower_jaw";
    private final ModelPart root;
    private final ModelPart base;
    private final ModelPart upperJaw;
    private final ModelPart lowerJaw;

    public EvokerFangsModel(ModelPart modelPart) {
        this.root = modelPart;
        this.base = modelPart.getChild(BASE);
        this.upperJaw = modelPart.getChild(UPPER_JAW);
        this.lowerJaw = modelPart.getChild(LOWER_JAW);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(BASE, CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 10.0f, 12.0f, 10.0f), PartPose.offset(-5.0f, 24.0f, -5.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(40, 0).addBox(0.0f, 0.0f, 0.0f, 4.0f, 14.0f, 8.0f);
        partDefinition.addOrReplaceChild(UPPER_JAW, cubeListBuilder, PartPose.offset(1.5f, 24.0f, -4.0f));
        partDefinition.addOrReplaceChild(LOWER_JAW, cubeListBuilder, PartPose.offsetAndRotation(-1.5f, 24.0f, 4.0f, 0.0f, (float)Math.PI, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        float f6 = f * 2.0f;
        if (f6 > 1.0f) {
            f6 = 1.0f;
        }
        f6 = 1.0f - f6 * f6 * f6;
        this.upperJaw.zRot = (float)Math.PI - f6 * 0.35f * (float)Math.PI;
        this.lowerJaw.zRot = (float)Math.PI + f6 * 0.35f * (float)Math.PI;
        float f7 = (f + Mth.sin(f * 2.7f)) * 0.6f * 12.0f;
        this.lowerJaw.y = this.upperJaw.y = 24.0f - f7;
        this.base.y = this.upperJaw.y;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

