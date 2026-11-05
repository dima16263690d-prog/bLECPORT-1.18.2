/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;

public class InventoryScreen
extends EffectRenderingInventoryScreen<InventoryMenu>
implements RecipeUpdateListener {
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    private float xMouse;
    private float yMouse;
    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean recipeBookComponentInitialized;
    private boolean widthTooNarrow;
    private boolean buttonClicked;

    public InventoryScreen(Player player) {
        super(player.inventoryMenu, player.getInventory(), new TranslatableComponent("container.crafting"));
        this.passEvents = true;
        this.titleLabelX = 97;
    }

    @Override
    public void containerTick() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player));
            return;
        }
        this.recipeBookComponent.tick();
    }

    @Override
    protected void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player));
            return;
        }
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, (RecipeBookMenu)this.menu);
        this.recipeBookComponentInitialized = true;
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.addRenderableWidget(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, button -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            ((ImageButton)button).setPosition(this.leftPos + 104, this.height / 2 - 22);
            this.buttonClicked = true;
        }));
        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int n, int n2) {
        this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 0x404040);
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderBackground(poseStack);
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(poseStack, f, n, n2);
            this.recipeBookComponent.render(poseStack, n, n2, f);
        } else {
            this.recipeBookComponent.render(poseStack, n, n2, f);
            super.render(poseStack, n, n2, f);
            this.recipeBookComponent.renderGhostRecipe(poseStack, this.leftPos, this.topPos, false, f);
        }
        this.renderTooltip(poseStack, n, n2);
        this.recipeBookComponent.renderTooltip(poseStack, this.leftPos, this.topPos, n, n2);
        this.xMouse = n;
        this.yMouse = n2;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int n, int n2) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
        int n3 = this.leftPos;
        int n4 = this.topPos;
        this.blit(poseStack, n3, n4, 0, 0, this.imageWidth, this.imageHeight);
        InventoryScreen.renderEntityInInventory(n3 + 51, n4 + 75, 30, (float)(n3 + 51) - this.xMouse, (float)(n4 + 75 - 50) - this.yMouse, this.minecraft.player);
    }

    public static void renderEntityInInventory(int n, int n2, int n3, float f, float f2, LivingEntity livingEntity) {
        float f3 = (float)Math.atan(f / 40.0f);
        float f4 = (float)Math.atan(f2 / 40.0f);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(n, n2, 1050.0);
        poseStack.scale(1.0f, 1.0f, -1.0f);
        RenderSystem.applyModelViewMatrix();
        PoseStack poseStack2 = new PoseStack();
        poseStack2.translate(0.0, 0.0, 1000.0);
        poseStack2.scale(n3, n3, n3);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0f);
        Quaternion quaternion2 = Vector3f.XP.rotationDegrees(f4 * 20.0f);
        quaternion.mul(quaternion2);
        poseStack2.mulPose(quaternion);
        float f5 = livingEntity.yBodyRot;
        float f6 = livingEntity.getYRot();
        float f7 = livingEntity.getXRot();
        float f8 = livingEntity.yHeadRotO;
        float f9 = livingEntity.yHeadRot;
        livingEntity.yBodyRot = 180.0f + f3 * 20.0f;
        livingEntity.setYRot(180.0f + f3 * 40.0f);
        livingEntity.setXRot(-f4 * 20.0f);
        livingEntity.yHeadRot = livingEntity.getYRot();
        livingEntity.yHeadRotO = livingEntity.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion2.conj();
        entityRenderDispatcher.overrideCameraOrientation(quaternion2);
        entityRenderDispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(livingEntity, 0.0, 0.0, 0.0, 0.0f, 1.0f, poseStack2, bufferSource, 0xF000F0));
        bufferSource.endBatch();
        entityRenderDispatcher.setRenderShadow(true);
        livingEntity.yBodyRot = f5;
        livingEntity.setYRot(f6);
        livingEntity.setXRot(f7);
        livingEntity.yHeadRotO = f8;
        livingEntity.yHeadRot = f9;
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    @Override
    protected boolean isHovering(int n, int n2, int n3, int n4, double d, double d2) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(n, n2, n3, n4, d, d2);
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (this.recipeBookComponent.mouseClicked(d, d2, n)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        }
        if (this.widthTooNarrow && this.recipeBookComponent.isVisible()) {
            return false;
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    public boolean mouseReleased(double d, double d2, int n) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        }
        return super.mouseReleased(d, d2, n);
    }

    @Override
    protected boolean hasClickedOutside(double d, double d2, int n, int n2, int n3) {
        boolean bl = d < (double)n || d2 < (double)n2 || d >= (double)(n + this.imageWidth) || d2 >= (double)(n2 + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(d, d2, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, n3) && bl;
    }

    @Override
    protected void slotClicked(Slot slot, int n, int n2, ClickType clickType) {
        super.slotClicked(slot, n, n2, clickType);
        this.recipeBookComponent.slotClicked(slot);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public void removed() {
        if (this.recipeBookComponentInitialized) {
            this.recipeBookComponent.removed();
        }
        super.removed();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
}

