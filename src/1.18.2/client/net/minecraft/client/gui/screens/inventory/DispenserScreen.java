/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DispenserMenu;

public class DispenserScreen
extends AbstractContainerScreen<DispenserMenu> {
    private static final ResourceLocation CONTAINER_LOCATION = new ResourceLocation("textures/gui/container/dispenser.png");

    public DispenserScreen(DispenserMenu dispenserMenu, Inventory inventory, Component component) {
        super(dispenserMenu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderBackground(poseStack);
        super.render(poseStack, n, n2, f);
        this.renderTooltip(poseStack, n, n2);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int n, int n2) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, CONTAINER_LOCATION);
        int n3 = (this.width - this.imageWidth) / 2;
        int n4 = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, n3, n4, 0, 0, this.imageWidth, this.imageHeight);
    }
}

