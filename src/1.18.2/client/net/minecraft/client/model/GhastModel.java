/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import java.util.Random;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class GhastModel<T extends Entity>
extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart[] tentacles = new ModelPart[9];

    public GhastModel(ModelPart modelPart) {
        this.root = modelPart;
        for (int i = 0; i < this.tentacles.length; ++i) {
            this.tentacles[i] = modelPart.getChild(GhastModel.createTentacleName(i));
        }
    }

    private static String createTentacleName(int n) {
        return "tentacle" + n;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -8.0f, -8.0f, 16.0f, 16.0f, 16.0f), PartPose.offset(0.0f, 17.6f, 0.0f));
        Random random = new Random(1660L);
        for (int i = 0; i < 9; ++i) {
            float f = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5f + 0.25f) / 2.0f * 2.0f - 1.0f) * 5.0f;
            float f2 = ((float)(i / 3) / 2.0f * 2.0f - 1.0f) * 5.0f;
            int n = random.nextInt(7) + 8;
            partDefinition.addOrReplaceChild(GhastModel.createTentacleName(i), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, n, 2.0f), PartPose.offset(f, 24.6f, f2));
        }
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        for (int i = 0; i < this.tentacles.length; ++i) {
            this.tentacles[i].xRot = 0.2f * Mth.sin(f3 * 0.3f + (float)i) + 0.4f;
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

