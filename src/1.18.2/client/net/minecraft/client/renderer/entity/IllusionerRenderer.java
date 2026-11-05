/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.phys.Vec3;

public class IllusionerRenderer
extends IllagerRenderer<Illusioner> {
    private static final ResourceLocation ILLUSIONER = new ResourceLocation("textures/entity/illager/illusioner.png");

    public IllusionerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel(context.bakeLayer(ModelLayers.ILLUSIONER)), 0.5f);
        this.addLayer(new ItemInHandLayer<Illusioner, IllagerModel<Illusioner>>((RenderLayerParent)this){

            @Override
            public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, Illusioner illusioner, float f, float f2, float f3, float f4, float f5, float f6) {
                if (illusioner.isCastingSpell() || illusioner.isAggressive()) {
                    super.render(poseStack, multiBufferSource, n, illusioner, f, f2, f3, f4, f5, f6);
                }
            }
        });
        ((IllagerModel)this.model).getHat().visible = true;
    }

    @Override
    public ResourceLocation getTextureLocation(Illusioner illusioner) {
        return ILLUSIONER;
    }

    @Override
    public void render(Illusioner illusioner, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        if (illusioner.isInvisible()) {
            Vec3[] vec3Array = illusioner.getIllusionOffsets(f2);
            float f3 = this.getBob(illusioner, f2);
            for (int i = 0; i < vec3Array.length; ++i) {
                poseStack.pushPose();
                poseStack.translate(vec3Array[i].x + (double)Mth.cos((float)i + f3 * 0.5f) * 0.025, vec3Array[i].y + (double)Mth.cos((float)i + f3 * 0.75f) * 0.0125, vec3Array[i].z + (double)Mth.cos((float)i + f3 * 0.7f) * 0.025);
                super.render(illusioner, f, f2, poseStack, multiBufferSource, n);
                poseStack.popPose();
            }
        } else {
            super.render(illusioner, f, f2, poseStack, multiBufferSource, n);
        }
    }

    @Override
    protected boolean isBodyVisible(Illusioner illusioner) {
        return true;
    }

    @Override
    protected /* synthetic */ boolean isBodyVisible(LivingEntity livingEntity) {
        return this.isBodyVisible((Illusioner)livingEntity);
    }
}

