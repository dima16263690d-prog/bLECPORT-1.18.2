/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Shulker;

public class ShulkerModel<T extends Shulker>
extends ListModel<T> {
    private static final String LID = "lid";
    private static final String BASE = "base";
    private final ModelPart base;
    private final ModelPart lid;
    private final ModelPart head;

    public ShulkerModel(ModelPart modelPart) {
        super(RenderType::entityCutoutNoCullZOffset);
        this.lid = modelPart.getChild(LID);
        this.base = modelPart.getChild(BASE);
        this.head = modelPart.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(LID, CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -16.0f, -8.0f, 16.0f, 12.0f, 16.0f), PartPose.offset(0.0f, 24.0f, 0.0f));
        partDefinition.addOrReplaceChild(BASE, CubeListBuilder.create().texOffs(0, 28).addBox(-8.0f, -8.0f, -8.0f, 16.0f, 8.0f, 16.0f), PartPose.offset(0.0f, 24.0f, 0.0f));
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 52).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 6.0f, 6.0f), PartPose.offset(0.0f, 12.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(T t, float f, float f2, float f3, float f4, float f5) {
        float f6 = f3 - (float)((Shulker)t).tickCount;
        float f7 = (0.5f + ((Shulker)t).getClientPeekAmount(f6)) * (float)Math.PI;
        float f8 = -1.0f + Mth.sin(f7);
        float f9 = 0.0f;
        if (f7 > (float)Math.PI) {
            f9 = Mth.sin(f3 * 0.1f) * 0.7f;
        }
        this.lid.setPos(0.0f, 16.0f + Mth.sin(f7) * 8.0f + f9, 0.0f);
        this.lid.yRot = ((Shulker)t).getClientPeekAmount(f6) > 0.3f ? f8 * f8 * f8 * f8 * (float)Math.PI * 0.125f : 0.0f;
        this.head.xRot = f5 * ((float)Math.PI / 180);
        this.head.yRot = (((Shulker)t).yHeadRot - 180.0f - ((Shulker)t).yBodyRot) * ((float)Math.PI / 180);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of((Object)this.base, (Object)this.lid);
    }

    public ModelPart getLid() {
        return this.lid;
    }

    public ModelPart getHead() {
        return this.head;
    }
}

