/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public abstract class AbstractWidget
extends GuiComponent
implements Widget,
GuiEventListener,
NarratableEntry {
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    protected int width;
    protected int height;
    public int x;
    public int y;
    private Component message;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0f;
    private boolean focused;

    public AbstractWidget(int n, int n2, int n3, int n4, Component component) {
        this.x = n;
        this.y = n2;
        this.width = n3;
        this.height = n4;
        this.message = component;
    }

    public int getHeight() {
        return this.height;
    }

    protected int getYImage(boolean bl) {
        int n = 1;
        if (!this.active) {
            n = 0;
        } else if (bl) {
            n = 2;
        }
        return n;
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        if (!this.visible) {
            return;
        }
        this.isHovered = n >= this.x && n2 >= this.y && n < this.x + this.width && n2 < this.y + this.height;
        this.renderButton(poseStack, n, n2, f);
    }

    protected MutableComponent createNarrationMessage() {
        return AbstractWidget.wrapDefaultNarrationMessage(this.getMessage());
    }

    public static MutableComponent wrapDefaultNarrationMessage(Component component) {
        return new TranslatableComponent("gui.narrate.button", component);
    }

    public void renderButton(PoseStack poseStack, int n, int n2, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        int n3 = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(poseStack, this.x, this.y, 0, 46 + n3 * 20, this.width / 2, this.height);
        this.blit(poseStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + n3 * 20, this.width / 2, this.height);
        this.renderBg(poseStack, minecraft, n, n2);
        int n4 = this.active ? 0xFFFFFF : 0xA0A0A0;
        AbstractWidget.drawCenteredString(poseStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, n4 | Mth.ceil(this.alpha * 255.0f) << 24);
    }

    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int n, int n2) {
    }

    public void onClick(double d, double d2) {
    }

    public void onRelease(double d, double d2) {
    }

    protected void onDrag(double d, double d2, double d3, double d4) {
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        boolean bl;
        if (!this.active || !this.visible) {
            return false;
        }
        if (this.isValidClickButton(n) && (bl = this.clicked(d, d2))) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onClick(d, d2);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double d, double d2, int n) {
        if (this.isValidClickButton(n)) {
            this.onRelease(d, d2);
            return true;
        }
        return false;
    }

    protected boolean isValidClickButton(int n) {
        return n == 0;
    }

    @Override
    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        if (this.isValidClickButton(n)) {
            this.onDrag(d, d2, d3, d4);
            return true;
        }
        return false;
    }

    protected boolean clicked(double d, double d2) {
        return this.active && this.visible && d >= (double)this.x && d2 >= (double)this.y && d < (double)(this.x + this.width) && d2 < (double)(this.y + this.height);
    }

    public boolean isHoveredOrFocused() {
        return this.isHovered || this.focused;
    }

    @Override
    public boolean changeFocus(boolean bl) {
        if (!this.active || !this.visible) {
            return false;
        }
        this.focused = !this.focused;
        this.onFocusedChanged(this.focused);
        return this.focused;
    }

    protected void onFocusedChanged(boolean bl) {
    }

    @Override
    public boolean isMouseOver(double d, double d2) {
        return this.active && this.visible && d >= (double)this.x && d2 >= (double)this.y && d < (double)(this.x + this.width) && d2 < (double)(this.y + this.height);
    }

    public void renderToolTip(PoseStack poseStack, int n, int n2) {
    }

    public void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int n) {
        this.width = n;
    }

    public void setAlpha(float f) {
        this.alpha = f;
    }

    public void setMessage(Component component) {
        this.message = component;
    }

    public Component getMessage() {
        return this.message;
    }

    public boolean isFocused() {
        return this.focused;
    }

    @Override
    public boolean isActive() {
        return this.visible && this.active;
    }

    protected void setFocused(boolean bl) {
        this.focused = bl;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.focused) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        }
        if (this.isHovered) {
            return NarratableEntry.NarrationPriority.HOVERED;
        }
        return NarratableEntry.NarrationPriority.NONE;
    }

    protected void defaultButtonNarrationText(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)new TranslatableComponent("narration.button.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)new TranslatableComponent("narration.button.usage.hovered"));
            }
        }
    }
}

