/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class CartographyTableScreen
extends AbstractContainerScreen<CartographyTableMenu> {
    private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/cartography_table.png");

    public CartographyTableScreen(CartographyTableMenu cartographyTableMenu, Inventory inventory, Component component) {
        super(cartographyTableMenu, inventory, component);
        this.titleLabelY -= 2;
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        super.render(poseStack, n, n2, f);
        this.renderTooltip(poseStack, n, n2);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int n, int n2) {
        MapItemSavedData mapItemSavedData;
        Integer n3;
        this.renderBackground(poseStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BG_LOCATION);
        int n4 = this.leftPos;
        int n5 = this.topPos;
        this.blit(poseStack, n4, n5, 0, 0, this.imageWidth, this.imageHeight);
        ItemStack itemStack = ((CartographyTableMenu)this.menu).getSlot(1).getItem();
        boolean bl = itemStack.is(Items.MAP);
        boolean bl2 = itemStack.is(Items.PAPER);
        boolean bl3 = itemStack.is(Items.GLASS_PANE);
        ItemStack itemStack2 = ((CartographyTableMenu)this.menu).getSlot(0).getItem();
        boolean bl4 = false;
        if (itemStack2.is(Items.FILLED_MAP)) {
            n3 = MapItem.getMapId(itemStack2);
            mapItemSavedData = MapItem.getSavedData(n3, (Level)this.minecraft.level);
            if (mapItemSavedData != null) {
                if (mapItemSavedData.locked) {
                    bl4 = true;
                    if (bl2 || bl3) {
                        this.blit(poseStack, n4 + 35, n5 + 31, this.imageWidth + 50, 132, 28, 21);
                    }
                }
                if (bl2 && mapItemSavedData.scale >= 4) {
                    bl4 = true;
                    this.blit(poseStack, n4 + 35, n5 + 31, this.imageWidth + 50, 132, 28, 21);
                }
            }
        } else {
            n3 = null;
            mapItemSavedData = null;
        }
        this.renderResultingMap(poseStack, n3, mapItemSavedData, bl, bl2, bl3, bl4);
    }

    private void renderResultingMap(PoseStack poseStack, @Nullable Integer n, @Nullable MapItemSavedData mapItemSavedData, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        int n2 = this.leftPos;
        int n3 = this.topPos;
        if (bl2 && !bl4) {
            this.blit(poseStack, n2 + 67, n3 + 13, this.imageWidth, 66, 66, 66);
            this.renderMap(poseStack, n, mapItemSavedData, n2 + 85, n3 + 31, 0.226f);
        } else if (bl) {
            this.blit(poseStack, n2 + 67 + 16, n3 + 13, this.imageWidth, 132, 50, 66);
            this.renderMap(poseStack, n, mapItemSavedData, n2 + 86, n3 + 16, 0.34f);
            RenderSystem.setShaderTexture(0, BG_LOCATION);
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 1.0);
            this.blit(poseStack, n2 + 67, n3 + 13 + 16, this.imageWidth, 132, 50, 66);
            this.renderMap(poseStack, n, mapItemSavedData, n2 + 70, n3 + 32, 0.34f);
            poseStack.popPose();
        } else if (bl3) {
            this.blit(poseStack, n2 + 67, n3 + 13, this.imageWidth, 0, 66, 66);
            this.renderMap(poseStack, n, mapItemSavedData, n2 + 71, n3 + 17, 0.45f);
            RenderSystem.setShaderTexture(0, BG_LOCATION);
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 1.0);
            this.blit(poseStack, n2 + 66, n3 + 12, 0, this.imageHeight, 66, 66);
            poseStack.popPose();
        } else {
            this.blit(poseStack, n2 + 67, n3 + 13, this.imageWidth, 0, 66, 66);
            this.renderMap(poseStack, n, mapItemSavedData, n2 + 71, n3 + 17, 0.45f);
        }
    }

    private void renderMap(PoseStack poseStack, @Nullable Integer n, @Nullable MapItemSavedData mapItemSavedData, int n2, int n3, float f) {
        if (n != null && mapItemSavedData != null) {
            poseStack.pushPose();
            poseStack.translate(n2, n3, 1.0);
            poseStack.scale(f, f, 1.0f);
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            this.minecraft.gameRenderer.getMapRenderer().render(poseStack, bufferSource, n, mapItemSavedData, true, 0xF000F0);
            bufferSource.endBatch();
            poseStack.popPose();
        }
    }
}

