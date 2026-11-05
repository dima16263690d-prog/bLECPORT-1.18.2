/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

public class ContainerScreen
extends AbstractContainerScreen<ChestMenu>
implements MenuAccess<ChestMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");
    private final int containerRows;

    public ContainerScreen(ChestMenu chestMenu, Inventory inventory, Component component) {
        super(chestMenu, inventory, component);
        this.passEvents = false;
        int n = 222;
        int n2 = 114;
        this.containerRows = chestMenu.getRowCount();
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
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
        RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND);
        int n3 = (this.width - this.imageWidth) / 2;
        int n4 = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, n3, n4, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        this.blit(poseStack, n3, n4 + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }
}

