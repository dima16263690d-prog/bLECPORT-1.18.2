/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;

public class WitherBossModel<T extends WitherBoss>
extends HierarchicalModel<T> {
    private static final String RIBCAGE = "ribcage";
    private static final String CENTER_HEAD = "center_head";
    private static final String RIGHT_HEAD = "right_head";
    private static final String LEFT_HEAD = "left_head";
    private static final float RIBCAGE_X_ROT_OFFSET = 0.065f;
    private static final float TAIL_X_ROT_OFFSET = 0.265f;
    private final ModelPart root;
    private final ModelPart centerHead;
    private final ModelPart rightHead;
    private final ModelPart leftHead;
    private final ModelPart ribcage;
    private final ModelPart tail;

    public WitherBossModel(ModelPart modelPart) {
        this.root = modelPart;
        this.ribcage = modelPart.getChild(RIBCAGE);
        this.tail = modelPart.getChild("tail");
        this.centerHead = modelPart.getChild(CENTER_HEAD);
        this.rightHead = modelPart.getChild(RIGHT_HEAD);
        this.leftHead = modelPart.getChild(LEFT_HEAD);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("shoulders", CubeListBuilder.create().texOffs(0, 16).addBox(-10.0f, 3.9f, -0.5f, 20.0f, 3.0f, 3.0f, cubeDeformation), PartPose.ZERO);
        float f = 0.20420352f;
        partDefinition.addOrReplaceChild(RIBCAGE, CubeListBuilder.create().texOffs(0, 22).addBox(0.0f, 0.0f, 0.0f, 3.0f, 10.0f, 3.0f, cubeDeformation).texOffs(24, 22).addBox(-4.0f, 1.5f, 0.5f, 11.0f, 2.0f, 2.0f, cubeDeformation).texOffs(24, 22).addBox(-4.0f, 4.0f, 0.5f, 11.0f, 2.0f, 2.0f, cubeDeformation).texOffs(24, 22).addBox(-4.0f, 6.5f, 0.5f, 11.0f, 2.0f, 2.0f, cubeDeformation), PartPose.offsetAndRotation(-2.0f, 6.9f, -0.5f, 0.20420352f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(12, 22).addBox(0.0f, 0.0f, 0.0f, 3.0f, 6.0f, 3.0f, cubeDeformation), PartPose.offsetAndRotation(-2.0f, 6.9f + Mth.cos(0.20420352f) * 10.0f, -0.5f + Mth.sin(0.20420352f) * 10.0f, 0.83252203f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(CENTER_HEAD, CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation), PartPose.ZERO);
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -4.0f, -4.0f, 6.0f, 6.0f, 6.0f, cubeDeformation);
        partDefinition.addOrReplaceChild(RIGHT_HEAD, cubeListBuilder, PartPose.offset(-8.0f, 4.0f, 0.0f));
        partDefinition.addOrReplaceChild(LEFT_HEAD, cubeListBuilder, PartPose.offset(10.0f, 4.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        float f6 = Mth.cos(f3 * 0.1f);
        this.ribcage.xRot = (0.065f + 0.05f * f6) * (float)Math.PI;
        this.tail.setPos(-2.0f, 6.9f + Mth.cos(this.ribcage.xRot) * 10.0f, -0.5f + Mth.sin(this.ribcage.xRot) * 10.0f);
        this.tail.xRot = (0.265f + 0.1f * f6) * (float)Math.PI;
        this.centerHead.yRot = f4 * ((float)Math.PI / 180);
        this.centerHead.xRot = f5 * ((float)Math.PI / 180);
    }

    @Override
    public void prepareMobModel(T t, float f, float f2, float f3) {
        WitherBossModel.setupHeadRotation(t, this.rightHead, 0);
        WitherBossModel.setupHeadRotation(t, this.leftHead, 1);
    }

    private static <T extends WitherBoss> void setupHeadRotation(T t, ModelPart modelPart, int n) {
        modelPart.yRot = (t.getHeadYRot(n) - t.yBodyRot) * ((float)Math.PI / 180);
        modelPart.xRot = t.getHeadXRot(n) * ((float)Math.PI / 180);
    }
}

