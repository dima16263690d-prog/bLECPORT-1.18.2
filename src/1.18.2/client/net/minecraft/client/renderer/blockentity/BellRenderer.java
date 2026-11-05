/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;

public class BellRenderer
implements BlockEntityRenderer<BellBlockEntity> {
    public static final Material BELL_RESOURCE_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/bell/bell_body"));
    private static final String BELL_BODY = "bell_body";
    private final ModelPart bellBody;

    public BellRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelPart = context.bakeLayer(ModelLayers.BELL);
        this.bellBody = modelPart.getChild(BELL_BODY);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(BELL_BODY, CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -6.0f, -3.0f, 6.0f, 7.0f, 6.0f), PartPose.offset(8.0f, 12.0f, 8.0f));
        partDefinition2.addOrReplaceChild("bell_base", CubeListBuilder.create().texOffs(0, 13).addBox(4.0f, 4.0f, 4.0f, 8.0f, 2.0f, 8.0f), PartPose.offset(-8.0f, -12.0f, -8.0f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void render(BellBlockEntity bellBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2) {
        float f2 = (float)bellBlockEntity.ticks + f;
        float f3 = 0.0f;
        float f4 = 0.0f;
        if (bellBlockEntity.shaking) {
            float f5 = Mth.sin(f2 / (float)Math.PI) / (4.0f + f2 / 3.0f);
            if (bellBlockEntity.clickDirection == Direction.NORTH) {
                f3 = -f5;
            } else if (bellBlockEntity.clickDirection == Direction.SOUTH) {
                f3 = f5;
            } else if (bellBlockEntity.clickDirection == Direction.EAST) {
                f4 = -f5;
            } else if (bellBlockEntity.clickDirection == Direction.WEST) {
                f4 = f5;
            }
        }
        this.bellBody.xRot = f3;
        this.bellBody.zRot = f4;
        VertexConsumer vertexConsumer = BELL_RESOURCE_LOCATION.buffer(multiBufferSource, RenderType::entitySolid);
        this.bellBody.render(poseStack, vertexConsumer, n, n2);
    }
}

