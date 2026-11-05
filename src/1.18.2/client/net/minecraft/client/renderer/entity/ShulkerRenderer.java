/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ShulkerHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ShulkerRenderer
extends MobRenderer<Shulker, ShulkerModel<Shulker>> {
    private static final ResourceLocation DEFAULT_TEXTURE_LOCATION = new ResourceLocation("textures/" + Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION.texture().getPath() + ".png");
    private static final ResourceLocation[] TEXTURE_LOCATION = (ResourceLocation[])Sheets.SHULKER_TEXTURE_LOCATION.stream().map(material -> new ResourceLocation("textures/" + material.texture().getPath() + ".png")).toArray(ResourceLocation[]::new);

    public ShulkerRenderer(EntityRendererProvider.Context context) {
        super(context, new ShulkerModel(context.bakeLayer(ModelLayers.SHULKER)), 0.0f);
        this.addLayer(new ShulkerHeadLayer(this));
    }

    @Override
    public Vec3 getRenderOffset(Shulker shulker, float f) {
        return shulker.getRenderPosition(f).orElse(super.getRenderOffset(shulker, f));
    }

    @Override
    public boolean shouldRender(Shulker shulker, Frustum frustum, double d, double d2, double d3) {
        if (super.shouldRender(shulker, frustum, d, d2, d3)) {
            return true;
        }
        return shulker.getRenderPosition(0.0f).filter(vec3 -> {
            EntityType<?> entityType = shulker.getType();
            float f = entityType.getHeight() / 2.0f;
            float f2 = entityType.getWidth() / 2.0f;
            Vec3 vec32 = Vec3.atBottomCenterOf(shulker.blockPosition());
            return frustum.isVisible(new AABB(vec3.x, vec3.y + (double)f, vec3.z, vec32.x, vec32.y + (double)f, vec32.z).inflate(f2, f, f2));
        }).isPresent();
    }

    @Override
    public ResourceLocation getTextureLocation(Shulker shulker) {
        return ShulkerRenderer.getTextureLocation(shulker.getColor());
    }

    public static ResourceLocation getTextureLocation(@Nullable DyeColor dyeColor) {
        if (dyeColor == null) {
            return DEFAULT_TEXTURE_LOCATION;
        }
        return TEXTURE_LOCATION[dyeColor.getId()];
    }

    @Override
    protected void setupRotations(Shulker shulker, PoseStack poseStack, float f, float f2, float f3) {
        super.setupRotations(shulker, poseStack, f, f2 + 180.0f, f3);
        poseStack.translate(0.0, 0.5, 0.0);
        poseStack.mulPose(shulker.getAttachFace().getOpposite().getRotation());
        poseStack.translate(0.0, -0.5, 0.0);
    }
}

