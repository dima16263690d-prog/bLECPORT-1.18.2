/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import java.util.List;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateHolder;

public class BannerRenderer
implements BlockEntityRenderer<BannerBlockEntity> {
    private static final int BANNER_WIDTH = 20;
    private static final int BANNER_HEIGHT = 40;
    private static final int MAX_PATTERNS = 16;
    public static final String FLAG = "flag";
    private static final String POLE = "pole";
    private static final String BAR = "bar";
    private final ModelPart flag;
    private final ModelPart pole;
    private final ModelPart bar;

    public BannerRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelPart = context.bakeLayer(ModelLayers.BANNER);
        this.flag = modelPart.getChild(FLAG);
        this.pole = modelPart.getChild(POLE);
        this.bar = modelPart.getChild(BAR);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(FLAG, CubeListBuilder.create().texOffs(0, 0).addBox(-10.0f, 0.0f, -2.0f, 20.0f, 40.0f, 1.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(POLE, CubeListBuilder.create().texOffs(44, 0).addBox(-1.0f, -30.0f, -1.0f, 2.0f, 42.0f, 2.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(BAR, CubeListBuilder.create().texOffs(0, 42).addBox(-10.0f, -32.0f, -1.0f, 20.0f, 2.0f, 2.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void render(BannerBlockEntity bannerBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2) {
        Object object;
        long l;
        List<Pair<BannerPattern, DyeColor>> list = bannerBlockEntity.getPatterns();
        float f2 = 0.6666667f;
        boolean bl = bannerBlockEntity.getLevel() == null;
        poseStack.pushPose();
        if (bl) {
            l = 0L;
            poseStack.translate(0.5, 0.5, 0.5);
            this.pole.visible = true;
        } else {
            l = bannerBlockEntity.getLevel().getGameTime();
            object = bannerBlockEntity.getBlockState();
            if (((BlockBehaviour.BlockStateBase)object).getBlock() instanceof BannerBlock) {
                poseStack.translate(0.5, 0.5, 0.5);
                var13_12 = (float)(-((StateHolder)object).getValue(BannerBlock.ROTATION).intValue() * 360) / 16.0f;
                poseStack.mulPose(Vector3f.YP.rotationDegrees(var13_12));
                this.pole.visible = true;
            } else {
                poseStack.translate(0.5, -0.1666666716337204, 0.5);
                var13_12 = -((StateHolder)object).getValue(WallBannerBlock.FACING).toYRot();
                poseStack.mulPose(Vector3f.YP.rotationDegrees(var13_12));
                poseStack.translate(0.0, -0.3125, -0.4375);
                this.pole.visible = false;
            }
        }
        poseStack.pushPose();
        poseStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
        object = ModelBakery.BANNER_BASE.buffer(multiBufferSource, RenderType::entitySolid);
        this.pole.render(poseStack, (VertexConsumer)object, n, n2);
        this.bar.render(poseStack, (VertexConsumer)object, n, n2);
        BlockPos blockPos = bannerBlockEntity.getBlockPos();
        float f3 = ((float)Math.floorMod((long)(blockPos.getX() * 7 + blockPos.getY() * 9 + blockPos.getZ() * 13) + l, 100L) + f) / 100.0f;
        this.flag.xRot = (-0.0125f + 0.01f * Mth.cos((float)Math.PI * 2 * f3)) * (float)Math.PI;
        this.flag.y = -32.0f;
        BannerRenderer.renderPatterns(poseStack, multiBufferSource, n, n2, this.flag, ModelBakery.BANNER_BASE, true, list);
        poseStack.popPose();
        poseStack.popPose();
    }

    public static void renderPatterns(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, ModelPart modelPart, Material material, boolean bl, List<Pair<BannerPattern, DyeColor>> list) {
        BannerRenderer.renderPatterns(poseStack, multiBufferSource, n, n2, modelPart, material, bl, list, false);
    }

    public static void renderPatterns(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, ModelPart modelPart, Material material, boolean bl, List<Pair<BannerPattern, DyeColor>> list, boolean bl2) {
        modelPart.render(poseStack, material.buffer(multiBufferSource, RenderType::entitySolid, bl2), n, n2);
        for (int i = 0; i < 17 && i < list.size(); ++i) {
            Pair<BannerPattern, DyeColor> pair = list.get(i);
            float[] fArray = ((DyeColor)pair.getSecond()).getTextureDiffuseColors();
            BannerPattern bannerPattern = (BannerPattern)((Object)pair.getFirst());
            Material material2 = bl ? Sheets.getBannerMaterial(bannerPattern) : Sheets.getShieldMaterial(bannerPattern);
            modelPart.render(poseStack, material2.buffer(multiBufferSource, RenderType::entityNoOutline), n, n2, fArray[0], fArray[1], fArray[2], 1.0f);
        }
    }
}

