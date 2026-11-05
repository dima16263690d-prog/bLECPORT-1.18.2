/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;

public class DrownedRenderer
extends AbstractZombieRenderer<Drowned, DrownedModel<Drowned>> {
    private static final ResourceLocation DROWNED_LOCATION = new ResourceLocation("textures/entity/zombie/drowned.png");

    public DrownedRenderer(EntityRendererProvider.Context context) {
        super(context, new DrownedModel(context.bakeLayer(ModelLayers.DROWNED)), new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_INNER_ARMOR)), new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_OUTER_ARMOR)));
        this.addLayer(new DrownedOuterLayer<Drowned>(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie zombie) {
        return DROWNED_LOCATION;
    }

    @Override
    protected void setupRotations(Drowned drowned, PoseStack poseStack, float f, float f2, float f3) {
        super.setupRotations(drowned, poseStack, f, f2, f3);
        float f4 = drowned.getSwimAmount(f3);
        if (f4 > 0.0f) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(f4, drowned.getXRot(), -10.0f - drowned.getXRot())));
        }
    }
}

