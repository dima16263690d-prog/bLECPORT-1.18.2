/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

public class SheepFurLayer
extends RenderLayer<Sheep, SheepModel<Sheep>> {
    private static final ResourceLocation SHEEP_FUR_LOCATION = new ResourceLocation("textures/entity/sheep/sheep_fur.png");
    private final SheepFurModel<Sheep> model;

    public SheepFurLayer(RenderLayerParent<Sheep, SheepModel<Sheep>> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.model = new SheepFurModel(entityModelSet.bakeLayer(ModelLayers.SHEEP_FUR));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, Sheep sheep, float f, float f2, float f3, float f4, float f5, float f6) {
        float f7;
        float f8;
        float f9;
        if (sheep.isSheared()) {
            return;
        }
        if (sheep.isInvisible()) {
            Minecraft minecraft = Minecraft.getInstance();
            boolean bl = minecraft.shouldEntityAppearGlowing(sheep);
            if (bl) {
                ((SheepModel)this.getParentModel()).copyPropertiesTo(this.model);
                this.model.prepareMobModel(sheep, f, f2, f3);
                this.model.setupAnim(sheep, f, f2, f4, f5, f6);
                VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.outline(SHEEP_FUR_LOCATION));
                this.model.renderToBuffer(poseStack, vertexConsumer, n, LivingEntityRenderer.getOverlayCoords(sheep, 0.0f), 0.0f, 0.0f, 0.0f, 1.0f);
            }
            return;
        }
        if (sheep.hasCustomName() && "jeb_".equals(sheep.getName().getContents())) {
            int n2 = 25;
            int n3 = sheep.tickCount / 25 + sheep.getId();
            int n4 = DyeColor.values().length;
            int n5 = n3 % n4;
            int n6 = (n3 + 1) % n4;
            float f10 = ((float)(sheep.tickCount % 25) + f3) / 25.0f;
            float[] fArray = Sheep.getColorArray(DyeColor.byId(n5));
            float[] fArray2 = Sheep.getColorArray(DyeColor.byId(n6));
            f9 = fArray[0] * (1.0f - f10) + fArray2[0] * f10;
            f8 = fArray[1] * (1.0f - f10) + fArray2[1] * f10;
            f7 = fArray[2] * (1.0f - f10) + fArray2[2] * f10;
        } else {
            float[] fArray = Sheep.getColorArray(sheep.getColor());
            f9 = fArray[0];
            f8 = fArray[1];
            f7 = fArray[2];
        }
        SheepFurLayer.coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, SHEEP_FUR_LOCATION, poseStack, multiBufferSource, n, sheep, f, f2, f4, f5, f6, f3, f9, f8, f7);
    }
}

