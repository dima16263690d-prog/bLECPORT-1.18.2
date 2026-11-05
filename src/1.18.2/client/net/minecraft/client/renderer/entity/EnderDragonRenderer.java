/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

public class EnderDragonRenderer
extends EntityRenderer<EnderDragon> {
    public static final ResourceLocation CRYSTAL_BEAM_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");
    private static final ResourceLocation DRAGON_EXPLODING_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");
    private static final ResourceLocation DRAGON_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon.png");
    private static final ResourceLocation DRAGON_EYES_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(DRAGON_LOCATION);
    private static final RenderType DECAL = RenderType.entityDecal(DRAGON_LOCATION);
    private static final RenderType EYES = RenderType.eyes(DRAGON_EYES_LOCATION);
    private static final RenderType BEAM = RenderType.entitySmoothCutout(CRYSTAL_BEAM_LOCATION);
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
    private final DragonModel model;

    public EnderDragonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        this.model = new DragonModel(context.bakeLayer(ModelLayers.ENDER_DRAGON));
    }

    @Override
    public void render(EnderDragon enderDragon, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        float f3 = (float)enderDragon.getLatencyPos(7, f2)[0];
        float f4 = (float)(enderDragon.getLatencyPos(5, f2)[1] - enderDragon.getLatencyPos(10, f2)[1]);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-f3));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(f4 * 10.0f));
        poseStack.translate(0.0, 0.0, 1.0);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0, -1.501f, 0.0);
        boolean bl = enderDragon.hurtTime > 0;
        this.model.prepareMobModel(enderDragon, 0.0f, 0.0f, f2);
        if (enderDragon.dragonDeathTime > 0) {
            float f5 = (float)enderDragon.dragonDeathTime / 200.0f;
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION));
            this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, f5);
            VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(DECAL);
            this.model.renderToBuffer(poseStack, vertexConsumer2, n, OverlayTexture.pack(0.0f, bl), 1.0f, 1.0f, 1.0f, 1.0f);
        } else {
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
            this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.pack(0.0f, bl), 1.0f, 1.0f, 1.0f, 1.0f);
        }
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(EYES);
        this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        if (enderDragon.dragonDeathTime > 0) {
            float f6 = ((float)enderDragon.dragonDeathTime + f2) / 200.0f;
            float f7 = Math.min(f6 > 0.8f ? (f6 - 0.8f) / 0.2f : 0.0f, 1.0f);
            Random random = new Random(432L);
            VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(RenderType.lightning());
            poseStack.pushPose();
            poseStack.translate(0.0, -1.0, -2.0);
            int n2 = 0;
            while ((float)n2 < (f6 + f6 * f6) / 2.0f * 60.0f) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0f));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0f));
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0f));
                poseStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0f));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0f));
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0f + f6 * 90.0f));
                float f8 = random.nextFloat() * 20.0f + 5.0f + f7 * 10.0f;
                float f9 = random.nextFloat() * 2.0f + 1.0f + f7 * 2.0f;
                Matrix4f matrix4f = poseStack.last().pose();
                int n3 = (int)(255.0f * (1.0f - f7));
                EnderDragonRenderer.vertex01(vertexConsumer3, matrix4f, n3);
                EnderDragonRenderer.vertex2(vertexConsumer3, matrix4f, f8, f9);
                EnderDragonRenderer.vertex3(vertexConsumer3, matrix4f, f8, f9);
                EnderDragonRenderer.vertex01(vertexConsumer3, matrix4f, n3);
                EnderDragonRenderer.vertex3(vertexConsumer3, matrix4f, f8, f9);
                EnderDragonRenderer.vertex4(vertexConsumer3, matrix4f, f8, f9);
                EnderDragonRenderer.vertex01(vertexConsumer3, matrix4f, n3);
                EnderDragonRenderer.vertex4(vertexConsumer3, matrix4f, f8, f9);
                EnderDragonRenderer.vertex2(vertexConsumer3, matrix4f, f8, f9);
                ++n2;
            }
            poseStack.popPose();
        }
        poseStack.popPose();
        if (enderDragon.nearestCrystal != null) {
            poseStack.pushPose();
            float f10 = (float)(enderDragon.nearestCrystal.getX() - Mth.lerp((double)f2, enderDragon.xo, enderDragon.getX()));
            float f11 = (float)(enderDragon.nearestCrystal.getY() - Mth.lerp((double)f2, enderDragon.yo, enderDragon.getY()));
            float f12 = (float)(enderDragon.nearestCrystal.getZ() - Mth.lerp((double)f2, enderDragon.zo, enderDragon.getZ()));
            EnderDragonRenderer.renderCrystalBeams(f10, f11 + EndCrystalRenderer.getY(enderDragon.nearestCrystal, f2), f12, f2, enderDragon.tickCount, poseStack, multiBufferSource, n);
            poseStack.popPose();
        }
        super.render(enderDragon, f, f2, poseStack, multiBufferSource, n);
    }

    private static void vertex01(VertexConsumer vertexConsumer, Matrix4f matrix4f, int n) {
        vertexConsumer.vertex(matrix4f, 0.0f, 0.0f, 0.0f).color(255, 255, 255, n).endVertex();
    }

    private static void vertex2(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float f2) {
        vertexConsumer.vertex(matrix4f, -HALF_SQRT_3 * f2, f, -0.5f * f2).color(255, 0, 255, 0).endVertex();
    }

    private static void vertex3(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float f2) {
        vertexConsumer.vertex(matrix4f, HALF_SQRT_3 * f2, f, -0.5f * f2).color(255, 0, 255, 0).endVertex();
    }

    private static void vertex4(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float f2) {
        vertexConsumer.vertex(matrix4f, 0.0f, f, 1.0f * f2).color(255, 0, 255, 0).endVertex();
    }

    public static void renderCrystalBeams(float f, float f2, float f3, float f4, int n, PoseStack poseStack, MultiBufferSource multiBufferSource, int n2) {
        float f5 = Mth.sqrt(f * f + f3 * f3);
        float f6 = Mth.sqrt(f * f + f2 * f2 + f3 * f3);
        poseStack.pushPose();
        poseStack.translate(0.0, 2.0, 0.0);
        poseStack.mulPose(Vector3f.YP.rotation((float)(-Math.atan2(f3, f)) - 1.5707964f));
        poseStack.mulPose(Vector3f.XP.rotation((float)(-Math.atan2(f5, f2)) - 1.5707964f));
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(BEAM);
        float f7 = 0.0f - ((float)n + f4) * 0.01f;
        float f8 = Mth.sqrt(f * f + f2 * f2 + f3 * f3) / 32.0f - ((float)n + f4) * 0.01f;
        int n3 = 8;
        float f9 = 0.0f;
        float f10 = 0.75f;
        float f11 = 0.0f;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        for (int i = 1; i <= 8; ++i) {
            float f12 = Mth.sin((float)i * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float f13 = Mth.cos((float)i * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float f14 = (float)i / 8.0f;
            vertexConsumer.vertex(matrix4f, f9 * 0.2f, f10 * 0.2f, 0.0f).color(0, 0, 0, 255).uv(f11, f7).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(n2).normal(matrix3f, 0.0f, -1.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, f9, f10, f6).color(255, 255, 255, 255).uv(f11, f8).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(n2).normal(matrix3f, 0.0f, -1.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, f12, f13, f6).color(255, 255, 255, 255).uv(f14, f8).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(n2).normal(matrix3f, 0.0f, -1.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, f12 * 0.2f, f13 * 0.2f, 0.0f).color(0, 0, 0, 255).uv(f14, f7).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(n2).normal(matrix3f, 0.0f, -1.0f, 0.0f).endVertex();
            f9 = f12;
            f10 = f13;
            f11 = f14;
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EnderDragon enderDragon) {
        return DRAGON_LOCATION;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = -16.0f;
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().addBox("upperlip", -6.0f, -1.0f, -24.0f, 12, 5, 16, 176, 44).addBox("upperhead", -8.0f, -8.0f, -10.0f, 16, 16, 16, 112, 30).mirror().addBox("scale", -5.0f, -12.0f, -4.0f, 2, 4, 6, 0, 0).addBox("nostril", -5.0f, -3.0f, -22.0f, 2, 2, 4, 112, 0).mirror().addBox("scale", 3.0f, -12.0f, -4.0f, 2, 4, 6, 0, 0).addBox("nostril", 3.0f, -3.0f, -22.0f, 2, 2, 4, 112, 0), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("jaw", CubeListBuilder.create().addBox("jaw", -6.0f, 0.0f, -16.0f, 12, 4, 16, 176, 65), PartPose.offset(0.0f, 4.0f, -8.0f));
        partDefinition.addOrReplaceChild("neck", CubeListBuilder.create().addBox("box", -5.0f, -5.0f, -5.0f, 10, 10, 10, 192, 104).addBox("scale", -1.0f, -9.0f, -3.0f, 2, 4, 6, 48, 0), PartPose.ZERO);
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().addBox("body", -12.0f, 0.0f, -16.0f, 24, 24, 64, 0, 0).addBox("scale", -1.0f, -6.0f, -10.0f, 2, 6, 12, 220, 53).addBox("scale", -1.0f, -6.0f, 10.0f, 2, 6, 12, 220, 53).addBox("scale", -1.0f, -6.0f, 30.0f, 2, 6, 12, 220, 53), PartPose.offset(0.0f, 4.0f, 8.0f));
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().mirror().addBox("bone", 0.0f, -4.0f, -4.0f, 56, 8, 8, 112, 88).addBox("skin", 0.0f, 0.0f, 2.0f, 56, 0, 56, -56, 88), PartPose.offset(12.0f, 5.0f, 2.0f));
        partDefinition3.addOrReplaceChild("left_wing_tip", CubeListBuilder.create().mirror().addBox("bone", 0.0f, -2.0f, -2.0f, 56, 4, 4, 112, 136).addBox("skin", 0.0f, 0.0f, 2.0f, 56, 0, 56, -56, 144), PartPose.offset(56.0f, 0.0f, 0.0f));
        PartDefinition partDefinition4 = partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().addBox("main", -4.0f, -4.0f, -4.0f, 8, 24, 8, 112, 104), PartPose.offset(12.0f, 20.0f, 2.0f));
        PartDefinition partDefinition5 = partDefinition4.addOrReplaceChild("left_front_leg_tip", CubeListBuilder.create().addBox("main", -3.0f, -1.0f, -3.0f, 6, 24, 6, 226, 138), PartPose.offset(0.0f, 20.0f, -1.0f));
        partDefinition5.addOrReplaceChild("left_front_foot", CubeListBuilder.create().addBox("main", -4.0f, 0.0f, -12.0f, 8, 4, 16, 144, 104), PartPose.offset(0.0f, 23.0f, 0.0f));
        PartDefinition partDefinition6 = partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().addBox("main", -8.0f, -4.0f, -8.0f, 16, 32, 16, 0, 0), PartPose.offset(16.0f, 16.0f, 42.0f));
        PartDefinition partDefinition7 = partDefinition6.addOrReplaceChild("left_hind_leg_tip", CubeListBuilder.create().addBox("main", -6.0f, -2.0f, 0.0f, 12, 32, 12, 196, 0), PartPose.offset(0.0f, 32.0f, -4.0f));
        partDefinition7.addOrReplaceChild("left_hind_foot", CubeListBuilder.create().addBox("main", -9.0f, 0.0f, -20.0f, 18, 6, 24, 112, 0), PartPose.offset(0.0f, 31.0f, 4.0f));
        PartDefinition partDefinition8 = partDefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().addBox("bone", -56.0f, -4.0f, -4.0f, 56, 8, 8, 112, 88).addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, -56, 88), PartPose.offset(-12.0f, 5.0f, 2.0f));
        partDefinition8.addOrReplaceChild("right_wing_tip", CubeListBuilder.create().addBox("bone", -56.0f, -2.0f, -2.0f, 56, 4, 4, 112, 136).addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, -56, 144), PartPose.offset(-56.0f, 0.0f, 0.0f));
        PartDefinition partDefinition9 = partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().addBox("main", -4.0f, -4.0f, -4.0f, 8, 24, 8, 112, 104), PartPose.offset(-12.0f, 20.0f, 2.0f));
        PartDefinition partDefinition10 = partDefinition9.addOrReplaceChild("right_front_leg_tip", CubeListBuilder.create().addBox("main", -3.0f, -1.0f, -3.0f, 6, 24, 6, 226, 138), PartPose.offset(0.0f, 20.0f, -1.0f));
        partDefinition10.addOrReplaceChild("right_front_foot", CubeListBuilder.create().addBox("main", -4.0f, 0.0f, -12.0f, 8, 4, 16, 144, 104), PartPose.offset(0.0f, 23.0f, 0.0f));
        PartDefinition partDefinition11 = partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().addBox("main", -8.0f, -4.0f, -8.0f, 16, 32, 16, 0, 0), PartPose.offset(-16.0f, 16.0f, 42.0f));
        PartDefinition partDefinition12 = partDefinition11.addOrReplaceChild("right_hind_leg_tip", CubeListBuilder.create().addBox("main", -6.0f, -2.0f, 0.0f, 12, 32, 12, 196, 0), PartPose.offset(0.0f, 32.0f, -4.0f));
        partDefinition12.addOrReplaceChild("right_hind_foot", CubeListBuilder.create().addBox("main", -9.0f, 0.0f, -20.0f, 18, 6, 24, 112, 0), PartPose.offset(0.0f, 31.0f, 4.0f));
        return LayerDefinition.create(meshDefinition, 256, 256);
    }

    public static class DragonModel
    extends EntityModel<EnderDragon> {
        private final ModelPart head;
        private final ModelPart neck;
        private final ModelPart jaw;
        private final ModelPart body;
        private final ModelPart leftWing;
        private final ModelPart leftWingTip;
        private final ModelPart leftFrontLeg;
        private final ModelPart leftFrontLegTip;
        private final ModelPart leftFrontFoot;
        private final ModelPart leftRearLeg;
        private final ModelPart leftRearLegTip;
        private final ModelPart leftRearFoot;
        private final ModelPart rightWing;
        private final ModelPart rightWingTip;
        private final ModelPart rightFrontLeg;
        private final ModelPart rightFrontLegTip;
        private final ModelPart rightFrontFoot;
        private final ModelPart rightRearLeg;
        private final ModelPart rightRearLegTip;
        private final ModelPart rightRearFoot;
        @Nullable
        private EnderDragon entity;
        private float a;

        public DragonModel(ModelPart modelPart) {
            this.head = modelPart.getChild("head");
            this.jaw = this.head.getChild("jaw");
            this.neck = modelPart.getChild("neck");
            this.body = modelPart.getChild("body");
            this.leftWing = modelPart.getChild("left_wing");
            this.leftWingTip = this.leftWing.getChild("left_wing_tip");
            this.leftFrontLeg = modelPart.getChild("left_front_leg");
            this.leftFrontLegTip = this.leftFrontLeg.getChild("left_front_leg_tip");
            this.leftFrontFoot = this.leftFrontLegTip.getChild("left_front_foot");
            this.leftRearLeg = modelPart.getChild("left_hind_leg");
            this.leftRearLegTip = this.leftRearLeg.getChild("left_hind_leg_tip");
            this.leftRearFoot = this.leftRearLegTip.getChild("left_hind_foot");
            this.rightWing = modelPart.getChild("right_wing");
            this.rightWingTip = this.rightWing.getChild("right_wing_tip");
            this.rightFrontLeg = modelPart.getChild("right_front_leg");
            this.rightFrontLegTip = this.rightFrontLeg.getChild("right_front_leg_tip");
            this.rightFrontFoot = this.rightFrontLegTip.getChild("right_front_foot");
            this.rightRearLeg = modelPart.getChild("right_hind_leg");
            this.rightRearLegTip = this.rightRearLeg.getChild("right_hind_leg_tip");
            this.rightRearFoot = this.rightRearLegTip.getChild("right_hind_foot");
        }

        @Override
        public void prepareMobModel(EnderDragon enderDragon, float f, float f2, float f3) {
            this.entity = enderDragon;
            this.a = f3;
        }

        @Override
        public void setupAnim(EnderDragon enderDragon, float f, float f2, float f3, float f4, float f5) {
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2, float f, float f2, float f3, float f4) {
            float f5;
            poseStack.pushPose();
            float f6 = Mth.lerp(this.a, this.entity.oFlapTime, this.entity.flapTime);
            this.jaw.xRot = (float)(Math.sin(f6 * ((float)Math.PI * 2)) + 1.0) * 0.2f;
            float f7 = (float)(Math.sin(f6 * ((float)Math.PI * 2) - 1.0f) + 1.0);
            f7 = (f7 * f7 + f7 * 2.0f) * 0.05f;
            poseStack.translate(0.0, f7 - 2.0f, -3.0);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(f7 * 2.0f));
            float f8 = 0.0f;
            float f9 = 20.0f;
            float f10 = -12.0f;
            float f11 = 1.5f;
            double[] dArray = this.entity.getLatencyPos(6, this.a);
            float f12 = Mth.rotWrap(this.entity.getLatencyPos(5, this.a)[0] - this.entity.getLatencyPos(10, this.a)[0]);
            float f13 = Mth.rotWrap(this.entity.getLatencyPos(5, this.a)[0] + (double)(f12 / 2.0f));
            float f14 = f6 * ((float)Math.PI * 2);
            for (int i = 0; i < 5; ++i) {
                double[] dArray2 = this.entity.getLatencyPos(5 - i, this.a);
                f5 = (float)Math.cos((float)i * 0.45f + f14) * 0.15f;
                this.neck.yRot = Mth.rotWrap(dArray2[0] - dArray[0]) * ((float)Math.PI / 180) * 1.5f;
                this.neck.xRot = f5 + this.entity.getHeadPartYOffset(i, dArray, dArray2) * ((float)Math.PI / 180) * 1.5f * 5.0f;
                this.neck.zRot = -Mth.rotWrap(dArray2[0] - (double)f13) * ((float)Math.PI / 180) * 1.5f;
                this.neck.y = f9;
                this.neck.z = f10;
                this.neck.x = f8;
                f9 += Mth.sin(this.neck.xRot) * 10.0f;
                f10 -= Mth.cos(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0f;
                f8 -= Mth.sin(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0f;
                this.neck.render(poseStack, vertexConsumer, n, n2, 1.0f, 1.0f, 1.0f, f4);
            }
            this.head.y = f9;
            this.head.z = f10;
            this.head.x = f8;
            double[] dArray3 = this.entity.getLatencyPos(0, this.a);
            this.head.yRot = Mth.rotWrap(dArray3[0] - dArray[0]) * ((float)Math.PI / 180);
            this.head.xRot = Mth.rotWrap(this.entity.getHeadPartYOffset(6, dArray, dArray3)) * ((float)Math.PI / 180) * 1.5f * 5.0f;
            this.head.zRot = -Mth.rotWrap(dArray3[0] - (double)f13) * ((float)Math.PI / 180);
            this.head.render(poseStack, vertexConsumer, n, n2, 1.0f, 1.0f, 1.0f, f4);
            poseStack.pushPose();
            poseStack.translate(0.0, 1.0, 0.0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-f12 * 1.5f));
            poseStack.translate(0.0, -1.0, 0.0);
            this.body.zRot = 0.0f;
            this.body.render(poseStack, vertexConsumer, n, n2, 1.0f, 1.0f, 1.0f, f4);
            float f15 = f6 * ((float)Math.PI * 2);
            this.leftWing.xRot = 0.125f - (float)Math.cos(f15) * 0.2f;
            this.leftWing.yRot = -0.25f;
            this.leftWing.zRot = -((float)(Math.sin(f15) + 0.125)) * 0.8f;
            this.leftWingTip.zRot = (float)(Math.sin(f15 + 2.0f) + 0.5) * 0.75f;
            this.rightWing.xRot = this.leftWing.xRot;
            this.rightWing.yRot = -this.leftWing.yRot;
            this.rightWing.zRot = -this.leftWing.zRot;
            this.rightWingTip.zRot = -this.leftWingTip.zRot;
            this.renderSide(poseStack, vertexConsumer, n, n2, f7, this.leftWing, this.leftFrontLeg, this.leftFrontLegTip, this.leftFrontFoot, this.leftRearLeg, this.leftRearLegTip, this.leftRearFoot, f4);
            this.renderSide(poseStack, vertexConsumer, n, n2, f7, this.rightWing, this.rightFrontLeg, this.rightFrontLegTip, this.rightFrontFoot, this.rightRearLeg, this.rightRearLegTip, this.rightRearFoot, f4);
            poseStack.popPose();
            f5 = -Mth.sin(f6 * ((float)Math.PI * 2)) * 0.0f;
            f14 = f6 * ((float)Math.PI * 2);
            f9 = 10.0f;
            f10 = 60.0f;
            f8 = 0.0f;
            dArray = this.entity.getLatencyPos(11, this.a);
            for (int i = 0; i < 12; ++i) {
                dArray3 = this.entity.getLatencyPos(12 + i, this.a);
                this.neck.yRot = (Mth.rotWrap(dArray3[0] - dArray[0]) * 1.5f + 180.0f) * ((float)Math.PI / 180);
                this.neck.xRot = (f5 += Mth.sin((float)i * 0.45f + f14) * 0.05f) + (float)(dArray3[1] - dArray[1]) * ((float)Math.PI / 180) * 1.5f * 5.0f;
                this.neck.zRot = Mth.rotWrap(dArray3[0] - (double)f13) * ((float)Math.PI / 180) * 1.5f;
                this.neck.y = f9;
                this.neck.z = f10;
                this.neck.x = f8;
                f9 += Mth.sin(this.neck.xRot) * 10.0f;
                f10 -= Mth.cos(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0f;
                f8 -= Mth.sin(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0f;
                this.neck.render(poseStack, vertexConsumer, n, n2, 1.0f, 1.0f, 1.0f, f4);
            }
            poseStack.popPose();
        }

        private void renderSide(PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2, float f, ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, ModelPart modelPart4, ModelPart modelPart5, ModelPart modelPart6, ModelPart modelPart7, float f2) {
            modelPart5.xRot = 1.0f + f * 0.1f;
            modelPart6.xRot = 0.5f + f * 0.1f;
            modelPart7.xRot = 0.75f + f * 0.1f;
            modelPart2.xRot = 1.3f + f * 0.1f;
            modelPart3.xRot = -0.5f - f * 0.1f;
            modelPart4.xRot = 0.75f + f * 0.1f;
            modelPart.render(poseStack, vertexConsumer, n, n2, 1.0f, 1.0f, 1.0f, f2);
            modelPart2.render(poseStack, vertexConsumer, n, n2, 1.0f, 1.0f, 1.0f, f2);
            modelPart5.render(poseStack, vertexConsumer, n, n2, 1.0f, 1.0f, 1.0f, f2);
        }
    }
}

