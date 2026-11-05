/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class SkullModel
extends SkullModelBase {
    private final ModelPart root;
    protected final ModelPart head;

    public SkullModel(ModelPart modelPart) {
        this.root = modelPart;
        this.head = modelPart.getChild("head");
    }

    public static MeshDefinition createHeadModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.ZERO);
        return meshDefinition;
    }

    public static LayerDefinition createHumanoidHeadLayer() {
        MeshDefinition meshDefinition = SkullModel.createHeadModel();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.getChild("head").addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.25f)), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public static LayerDefinition createMobHeadLayer() {
        MeshDefinition meshDefinition = SkullModel.createHeadModel();
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(float f, float f2, float f3) {
        this.head.yRot = f2 * ((float)Math.PI / 180);
        this.head.xRot = f3 * ((float)Math.PI / 180);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2, float f, float f2, float f3, float f4) {
        this.root.render(poseStack, vertexConsumer, n, n2, f, f2, f3, f4);
    }
}

