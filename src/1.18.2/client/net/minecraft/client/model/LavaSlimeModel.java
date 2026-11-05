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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;

public class LavaSlimeModel<T extends Slime>
extends HierarchicalModel<T> {
    private static final int SEGMENT_COUNT = 8;
    private final ModelPart root;
    private final ModelPart[] bodyCubes = new ModelPart[8];

    public LavaSlimeModel(ModelPart modelPart) {
        this.root = modelPart;
        Arrays.setAll(this.bodyCubes, n -> modelPart.getChild(LavaSlimeModel.getSegmentName(n)));
    }

    private static String getSegmentName(int n) {
        return "cube" + n;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        for (int i = 0; i < 8; ++i) {
            int n = 0;
            int n2 = i;
            if (i == 2) {
                n = 24;
                n2 = 10;
            } else if (i == 3) {
                n = 24;
                n2 = 19;
            }
            partDefinition.addOrReplaceChild(LavaSlimeModel.getSegmentName(i), CubeListBuilder.create().texOffs(n, n2).addBox(-4.0f, 16 + i, -4.0f, 8.0f, 1.0f, 8.0f), PartPose.ZERO);
        }
        partDefinition.addOrReplaceChild("inside_cube", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 18.0f, -2.0f, 4.0f, 4.0f, 4.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
    }

    @Override
    public void prepareMobModel(T t, float f, float f2, float f3) {
        float f4 = Mth.lerp(f3, ((Slime)t).oSquish, ((Slime)t).squish);
        if (f4 < 0.0f) {
            f4 = 0.0f;
        }
        for (int i = 0; i < this.bodyCubes.length; ++i) {
            this.bodyCubes[i].y = (float)(-(4 - i)) * f4 * 1.7f;
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

