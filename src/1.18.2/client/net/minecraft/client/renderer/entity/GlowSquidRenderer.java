/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.GlowSquid;

public class GlowSquidRenderer
extends SquidRenderer<GlowSquid> {
    private static final ResourceLocation GLOW_SQUID_LOCATION = new ResourceLocation("textures/entity/squid/glow_squid.png");

    public GlowSquidRenderer(EntityRendererProvider.Context context, SquidModel<GlowSquid> squidModel) {
        super(context, squidModel);
    }

    @Override
    public ResourceLocation getTextureLocation(GlowSquid glowSquid) {
        return GLOW_SQUID_LOCATION;
    }

    @Override
    protected int getBlockLightLevel(GlowSquid glowSquid, BlockPos blockPos) {
        int n = (int)Mth.clampedLerp(0.0f, 15.0f, 1.0f - (float)glowSquid.getDarkTicksRemaining() / 10.0f);
        if (n == 15) {
            return 15;
        }
        return Math.max(n, super.getBlockLightLevel(glowSquid, blockPos));
    }
}

