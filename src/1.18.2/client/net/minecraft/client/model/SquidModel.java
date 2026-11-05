/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import java.util.Arrays;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

public class SquidModel<T extends Entity>
extends HierarchicalModel<T> {
    private final ModelPart[] tentacles = new ModelPart[8];
    private final ModelPart root;

    public SquidModel(ModelPart modelPart) {
        this.root = modelPart;
        Arrays.setAll(this.tentacles, n -> modelPart.getChild(SquidModel.createTentacleName(n)));
    }

    private static String createTentacleName(int n) {
        return "tentacle" + n;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int n = -16;
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -8.0f, -6.0f, 12.0f, 16.0f, 12.0f), PartPose.offset(0.0f, 8.0f, 0.0f));
        int n2 = 8;
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(48, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 18.0f, 2.0f);
        for (int i = 0; i < 8; ++i) {
            double d = (double)i * Math.PI * 2.0 / 8.0;
            float f = (float)Math.cos(d) * 5.0f;
            float f2 = 15.0f;
            float f3 = (float)Math.sin(d) * 5.0f;
            d = (double)i * Math.PI * -2.0 / 8.0 + 1.5707963267948966;
            float f4 = (float)d;
            partDefinition.addOrReplaceChild(SquidModel.createTentacleName(i), cubeListBuilder, PartPose.offsetAndRotation(f, 15.0f, f3, 0.0f, f4, 0.0f));
        }
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        for (ModelPart modelPart : this.tentacles) {
            modelPart.xRot = f3;
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

