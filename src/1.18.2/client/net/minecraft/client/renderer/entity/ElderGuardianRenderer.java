/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;

public class ElderGuardianRenderer
extends GuardianRenderer {
    public static final ResourceLocation GUARDIAN_ELDER_LOCATION = new ResourceLocation("textures/entity/guardian_elder.png");

    public ElderGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, 1.2f, ModelLayers.ELDER_GUARDIAN);
    }

    @Override
    protected void scale(Guardian guardian, PoseStack poseStack, float f) {
        poseStack.scale(ElderGuardian.ELDER_SIZE_SCALE, ElderGuardian.ELDER_SIZE_SCALE, ElderGuardian.ELDER_SIZE_SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(Guardian guardian) {
        return GUARDIAN_ELDER_LOCATION;
    }
}

