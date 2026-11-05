/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

public class HuskRenderer
extends ZombieRenderer {
    private static final ResourceLocation HUSK_LOCATION = new ResourceLocation("textures/entity/zombie/husk.png");

    public HuskRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.HUSK, ModelLayers.HUSK_INNER_ARMOR, ModelLayers.HUSK_OUTER_ARMOR);
    }

    @Override
    protected void scale(Zombie zombie, PoseStack poseStack, float f) {
        float f2 = 1.0625f;
        poseStack.scale(1.0625f, 1.0625f, 1.0625f);
        super.scale(zombie, poseStack, f);
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie zombie) {
        return HUSK_LOCATION;
    }
}

