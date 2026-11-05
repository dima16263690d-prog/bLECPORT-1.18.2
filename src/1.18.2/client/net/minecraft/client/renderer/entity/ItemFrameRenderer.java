/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

public class ItemFrameRenderer<T extends ItemFrame>
extends EntityRenderer<T> {
    private static final ModelResourceLocation FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=false");
    private static final ModelResourceLocation MAP_FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=true");
    private static final ModelResourceLocation GLOW_FRAME_LOCATION = new ModelResourceLocation("glow_item_frame", "map=false");
    private static final ModelResourceLocation GLOW_MAP_FRAME_LOCATION = new ModelResourceLocation("glow_item_frame", "map=true");
    public static final int GLOW_FRAME_BRIGHTNESS = 5;
    public static final int BRIGHT_MAP_LIGHT_ADJUSTMENT = 30;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final ItemRenderer itemRenderer;

    public ItemFrameRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    protected int getBlockLightLevel(T t, BlockPos blockPos) {
        if (((Entity)t).getType() == EntityType.GLOW_ITEM_FRAME) {
            return Math.max(5, super.getBlockLightLevel(t, blockPos));
        }
        return super.getBlockLightLevel(t, blockPos);
    }

    @Override
    public void render(T t, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        super.render(t, f, f2, poseStack, multiBufferSource, n);
        poseStack.pushPose();
        Direction direction = ((HangingEntity)t).getDirection();
        Vec3 vec3 = this.getRenderOffset(t, f2);
        poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
        double d = 0.46875;
        poseStack.translate((double)direction.getStepX() * 0.46875, (double)direction.getStepY() * 0.46875, (double)direction.getStepZ() * 0.46875);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(((Entity)t).getXRot()));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f - ((Entity)t).getYRot()));
        boolean bl = ((Entity)t).isInvisible();
        ItemStack itemStack = ((ItemFrame)t).getItem();
        if (!bl) {
            BlockRenderDispatcher blockRenderDispatcher = this.minecraft.getBlockRenderer();
            ModelManager modelManager = blockRenderDispatcher.getBlockModelShaper().getModelManager();
            ModelResourceLocation modelResourceLocation = this.getFrameModelResourceLoc(t, itemStack);
            poseStack.pushPose();
            poseStack.translate(-0.5, -0.5, -0.5);
            blockRenderDispatcher.getModelRenderer().renderModel(poseStack.last(), multiBufferSource.getBuffer(Sheets.solidBlockSheet()), null, modelManager.getModel(modelResourceLocation), 1.0f, 1.0f, 1.0f, n, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        if (!itemStack.isEmpty()) {
            boolean bl2 = itemStack.is(Items.FILLED_MAP);
            if (bl) {
                poseStack.translate(0.0, 0.0, 0.5);
            } else {
                poseStack.translate(0.0, 0.0, 0.4375);
            }
            int n2 = bl2 ? ((ItemFrame)t).getRotation() % 4 * 2 : ((ItemFrame)t).getRotation();
            poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)n2 * 360.0f / 8.0f));
            if (bl2) {
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0f));
                float f3 = 0.0078125f;
                poseStack.scale(0.0078125f, 0.0078125f, 0.0078125f);
                poseStack.translate(-64.0, -64.0, 0.0);
                Integer n3 = MapItem.getMapId(itemStack);
                MapItemSavedData mapItemSavedData = MapItem.getSavedData(n3, ((ItemFrame)t).level);
                poseStack.translate(0.0, 0.0, -1.0);
                if (mapItemSavedData != null) {
                    int n4 = this.getLightVal(t, 15728850, n);
                    this.minecraft.gameRenderer.getMapRenderer().render(poseStack, multiBufferSource, n3, mapItemSavedData, true, n4);
                }
            } else {
                int n5 = this.getLightVal(t, 0xF000F0, n);
                poseStack.scale(0.5f, 0.5f, 0.5f);
                this.itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.FIXED, n5, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, ((Entity)t).getId());
            }
        }
        poseStack.popPose();
    }

    private int getLightVal(T t, int n, int n2) {
        return ((Entity)t).getType() == EntityType.GLOW_ITEM_FRAME ? n : n2;
    }

    private ModelResourceLocation getFrameModelResourceLoc(T t, ItemStack itemStack) {
        boolean bl;
        boolean bl2 = bl = ((Entity)t).getType() == EntityType.GLOW_ITEM_FRAME;
        if (itemStack.is(Items.FILLED_MAP)) {
            return bl ? GLOW_MAP_FRAME_LOCATION : MAP_FRAME_LOCATION;
        }
        return bl ? GLOW_FRAME_LOCATION : FRAME_LOCATION;
    }

    @Override
    public Vec3 getRenderOffset(T t, float f) {
        return new Vec3((float)((HangingEntity)t).getDirection().getStepX() * 0.3f, -0.25, (float)((HangingEntity)t).getDirection().getStepZ() * 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(T t) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    protected boolean shouldShowName(T t) {
        if (!Minecraft.renderNames() || ((ItemFrame)t).getItem().isEmpty() || !((ItemFrame)t).getItem().hasCustomHoverName() || this.entityRenderDispatcher.crosshairPickEntity != t) {
            return false;
        }
        double d = this.entityRenderDispatcher.distanceToSqr((Entity)t);
        float f = ((Entity)t).isDiscrete() ? 32.0f : 64.0f;
        return d < (double)(f * f);
    }

    @Override
    protected void renderNameTag(T t, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        super.renderNameTag(t, ((ItemFrame)t).getItem().getHoverName(), poseStack, multiBufferSource, n);
    }
}

