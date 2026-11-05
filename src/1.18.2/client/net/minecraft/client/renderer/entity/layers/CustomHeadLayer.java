/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;

public class CustomHeadLayer<T extends LivingEntity, M extends EntityModel<T>>
extends RenderLayer<T, M> {
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;
    private final Map<SkullBlock.Type, SkullModelBase> skullModels;

    public CustomHeadLayer(RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet) {
        this(renderLayerParent, entityModelSet, 1.0f, 1.0f, 1.0f);
    }

    public CustomHeadLayer(RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet, float f, float f2, float f3) {
        super(renderLayerParent);
        this.scaleX = f;
        this.scaleY = f2;
        this.scaleZ = f3;
        this.skullModels = SkullBlockRenderer.createSkullRenderers(entityModelSet);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, T t, float f, float f2, float f3, float f4, float f5, float f6) {
        float f7;
        boolean bl;
        ItemStack itemStack = ((LivingEntity)t).getItemBySlot(EquipmentSlot.HEAD);
        if (itemStack.isEmpty()) {
            return;
        }
        Item item = itemStack.getItem();
        poseStack.pushPose();
        poseStack.scale(this.scaleX, this.scaleY, this.scaleZ);
        boolean bl2 = bl = t instanceof Villager || t instanceof ZombieVillager;
        if (((LivingEntity)t).isBaby() && !(t instanceof Villager)) {
            f7 = 2.0f;
            float f8 = 1.4f;
            poseStack.translate(0.0, 0.03125, 0.0);
            poseStack.scale(0.7f, 0.7f, 0.7f);
            poseStack.translate(0.0, 1.0, 0.0);
        }
        ((HeadedModel)this.getParentModel()).getHead().translateAndRotate(poseStack);
        if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
            Object object;
            f7 = 1.1875f;
            poseStack.scale(1.1875f, -1.1875f, -1.1875f);
            if (bl) {
                poseStack.translate(0.0, 0.0625, 0.0);
            }
            GameProfile gameProfile = null;
            if (itemStack.hasTag() && ((CompoundTag)(object = itemStack.getTag())).contains("SkullOwner", 10)) {
                gameProfile = NbtUtils.readGameProfile(((CompoundTag)object).getCompound("SkullOwner"));
            }
            poseStack.translate(-0.5, 0.0, -0.5);
            object = ((AbstractSkullBlock)((BlockItem)item).getBlock()).getType();
            SkullModelBase skullModelBase = this.skullModels.get(object);
            RenderType renderType = SkullBlockRenderer.getRenderType((SkullBlock.Type)object, gameProfile);
            SkullBlockRenderer.renderSkull(null, 180.0f, f, poseStack, multiBufferSource, n, skullModelBase, renderType);
        } else if (!(item instanceof ArmorItem) || ((ArmorItem)item).getSlot() != EquipmentSlot.HEAD) {
            CustomHeadLayer.translateToHead(poseStack, bl);
            Minecraft.getInstance().getItemInHandRenderer().renderItem((LivingEntity)t, itemStack, ItemTransforms.TransformType.HEAD, false, poseStack, multiBufferSource, n);
        }
        poseStack.popPose();
    }

    public static void translateToHead(PoseStack poseStack, boolean bl) {
        float f = 0.625f;
        poseStack.translate(0.0, -0.25, 0.0);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        poseStack.scale(0.625f, -0.625f, -0.625f);
        if (bl) {
            poseStack.translate(0.0, 0.1875, 0.0);
        }
    }
}

