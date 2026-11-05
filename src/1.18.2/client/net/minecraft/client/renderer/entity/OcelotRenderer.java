/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Ocelot;

public class OcelotRenderer
extends MobRenderer<Ocelot, OcelotModel<Ocelot>> {
    private static final ResourceLocation CAT_OCELOT_LOCATION = new ResourceLocation("textures/entity/cat/ocelot.png");

    public OcelotRenderer(EntityRendererProvider.Context context) {
        super(context, new OcelotModel(context.bakeLayer(ModelLayers.OCELOT)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(Ocelot ocelot) {
        return CAT_OCELOT_LOCATION;
    }
}

