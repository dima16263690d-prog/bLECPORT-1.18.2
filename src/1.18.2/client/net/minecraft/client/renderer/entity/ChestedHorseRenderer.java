/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.model.ChestedHorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;

public class ChestedHorseRenderer<T extends AbstractChestedHorse>
extends AbstractHorseRenderer<T, ChestedHorseModel<T>> {
    private static final Map<EntityType<?>, ResourceLocation> MAP = Maps.newHashMap((Map)ImmutableMap.of(EntityType.DONKEY, (Object)new ResourceLocation("textures/entity/horse/donkey.png"), EntityType.MULE, (Object)new ResourceLocation("textures/entity/horse/mule.png")));

    public ChestedHorseRenderer(EntityRendererProvider.Context context, float f, ModelLayerLocation modelLayerLocation) {
        super(context, new ChestedHorseModel(context.bakeLayer(modelLayerLocation)), f);
    }

    @Override
    public ResourceLocation getTextureLocation(T t) {
        return MAP.get(((Entity)t).getType());
    }
}

