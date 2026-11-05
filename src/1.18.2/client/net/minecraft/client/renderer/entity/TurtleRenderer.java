/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.TurtleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Turtle;

public class TurtleRenderer
extends MobRenderer<Turtle, TurtleModel<Turtle>> {
    private static final ResourceLocation TURTLE_LOCATION = new ResourceLocation("textures/entity/turtle/big_sea_turtle.png");

    public TurtleRenderer(EntityRendererProvider.Context context) {
        super(context, new TurtleModel(context.bakeLayer(ModelLayers.TURTLE)), 0.7f);
    }

    @Override
    public void render(Turtle turtle, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        if (turtle.isBaby()) {
            this.shadowRadius *= 0.5f;
        }
        super.render(turtle, f, f2, poseStack, multiBufferSource, n);
    }

    @Override
    public ResourceLocation getTextureLocation(Turtle turtle) {
        return TURTLE_LOCATION;
    }
}

