/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;

public class ClientBundleTooltip
implements ClientTooltipComponent {
    public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
    private static final int MARGIN_Y = 4;
    private static final int BORDER_WIDTH = 1;
    private static final int TEX_SIZE = 128;
    private static final int SLOT_SIZE_X = 18;
    private static final int SLOT_SIZE_Y = 20;
    private final NonNullList<ItemStack> items;
    private final int weight;

    public ClientBundleTooltip(BundleTooltip bundleTooltip) {
        this.items = bundleTooltip.getItems();
        this.weight = bundleTooltip.getWeight();
    }

    @Override
    public int getHeight() {
        return this.gridSizeY() * 20 + 2 + 4;
    }

    @Override
    public int getWidth(Font font) {
        return this.gridSizeX() * 18 + 2;
    }

    @Override
    public void renderImage(Font font, int n, int n2, PoseStack poseStack, ItemRenderer itemRenderer, int n3) {
        int n4 = this.gridSizeX();
        int n5 = this.gridSizeY();
        boolean bl = this.weight >= 64;
        int n6 = 0;
        for (int i = 0; i < n5; ++i) {
            for (int j = 0; j < n4; ++j) {
                int n7 = n + j * 18 + 1;
                int n8 = n2 + i * 20 + 1;
                this.renderSlot(n7, n8, n6++, bl, font, poseStack, itemRenderer, n3);
            }
        }
        this.drawBorder(n, n2, n4, n5, poseStack, n3);
    }

    private void renderSlot(int n, int n2, int n3, boolean bl, Font font, PoseStack poseStack, ItemRenderer itemRenderer, int n4) {
        if (n3 >= this.items.size()) {
            this.blit(poseStack, n, n2, n4, bl ? Texture.BLOCKED_SLOT : Texture.SLOT);
            return;
        }
        ItemStack itemStack = this.items.get(n3);
        this.blit(poseStack, n, n2, n4, Texture.SLOT);
        itemRenderer.renderAndDecorateItem(itemStack, n + 1, n2 + 1, n3);
        itemRenderer.renderGuiItemDecorations(font, itemStack, n + 1, n2 + 1);
        if (n3 == 0) {
            AbstractContainerScreen.renderSlotHighlight(poseStack, n + 1, n2 + 1, n4);
        }
    }

    private void drawBorder(int n, int n2, int n3, int n4, PoseStack poseStack, int n5) {
        int n6;
        this.blit(poseStack, n, n2, n5, Texture.BORDER_CORNER_TOP);
        this.blit(poseStack, n + n3 * 18 + 1, n2, n5, Texture.BORDER_CORNER_TOP);
        for (n6 = 0; n6 < n3; ++n6) {
            this.blit(poseStack, n + 1 + n6 * 18, n2, n5, Texture.BORDER_HORIZONTAL_TOP);
            this.blit(poseStack, n + 1 + n6 * 18, n2 + n4 * 20, n5, Texture.BORDER_HORIZONTAL_BOTTOM);
        }
        for (n6 = 0; n6 < n4; ++n6) {
            this.blit(poseStack, n, n2 + n6 * 20 + 1, n5, Texture.BORDER_VERTICAL);
            this.blit(poseStack, n + n3 * 18 + 1, n2 + n6 * 20 + 1, n5, Texture.BORDER_VERTICAL);
        }
        this.blit(poseStack, n, n2 + n4 * 20, n5, Texture.BORDER_CORNER_BOTTOM);
        this.blit(poseStack, n + n3 * 18 + 1, n2 + n4 * 20, n5, Texture.BORDER_CORNER_BOTTOM);
    }

    private void blit(PoseStack poseStack, int n, int n2, int n3, Texture texture) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
        GuiComponent.blit(poseStack, n, n2, n3, texture.x, texture.y, texture.w, texture.h, 128, 128);
    }

    private int gridSizeX() {
        return Math.max(2, (int)Math.ceil(Math.sqrt((double)this.items.size() + 1.0)));
    }

    private int gridSizeY() {
        return (int)Math.ceil(((double)this.items.size() + 1.0) / (double)this.gridSizeX());
    }

    static enum Texture {
        SLOT(0, 0, 18, 20),
        BLOCKED_SLOT(0, 40, 18, 20),
        BORDER_VERTICAL(0, 18, 1, 20),
        BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
        BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
        BORDER_CORNER_TOP(0, 20, 1, 1),
        BORDER_CORNER_BOTTOM(0, 60, 1, 1);

        public final int x;
        public final int y;
        public final int w;
        public final int h;

        private Texture(int n2, int n3, int n4, int n5) {
            this.x = n2;
            this.y = n3;
            this.w = n4;
            this.h = n5;
        }
    }
}

