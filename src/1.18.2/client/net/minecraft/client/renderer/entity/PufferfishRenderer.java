/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PufferfishBigModel;
import net.minecraft.client.model.PufferfishMidModel;
import net.minecraft.client.model.PufferfishSmallModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Pufferfish;

public class PufferfishRenderer
extends MobRenderer<Pufferfish, EntityModel<Pufferfish>> {
    private static final ResourceLocation PUFFER_LOCATION = new ResourceLocation("textures/entity/fish/pufferfish.png");
    private int puffStateO = 3;
    private final EntityModel<Pufferfish> small;
    private final EntityModel<Pufferfish> mid;
    private final EntityModel<Pufferfish> big = this.getModel();

    public PufferfishRenderer(EntityRendererProvider.Context context) {
        super(context, new PufferfishBigModel(context.bakeLayer(ModelLayers.PUFFERFISH_BIG)), 0.2f);
        this.mid = new PufferfishMidModel<Pufferfish>(context.bakeLayer(ModelLayers.PUFFERFISH_MEDIUM));
        this.small = new PufferfishSmallModel<Pufferfish>(context.bakeLayer(ModelLayers.PUFFERFISH_SMALL));
    }

    @Override
    public ResourceLocation getTextureLocation(Pufferfish pufferfish) {
        return PUFFER_LOCATION;
    }

    @Override
    public void render(Pufferfish pufferfish, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        int n2 = pufferfish.getPuffState();
        if (n2 != this.puffStateO) {
            this.model = n2 == 0 ? this.small : (n2 == 1 ? this.mid : this.big);
        }
        this.puffStateO = n2;
        this.shadowRadius = 0.1f + 0.1f * (float)n2;
        super.render(pufferfish, f, f2, poseStack, multiBufferSource, n);
    }

    @Override
    protected void setupRotations(Pufferfish pufferfish, PoseStack poseStack, float f, float f2, float f3) {
        poseStack.translate(0.0, Mth.cos(f * 0.05f) * 0.08f, 0.0);
        super.setupRotations(pufferfish, poseStack, f, f2, f3);
    }
}

