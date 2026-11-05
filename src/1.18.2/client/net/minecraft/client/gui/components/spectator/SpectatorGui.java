/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components.spectator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenuListener;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SpectatorGui
extends GuiComponent
implements SpectatorMenuListener {
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    public static final ResourceLocation SPECTATOR_LOCATION = new ResourceLocation("textures/gui/spectator_widgets.png");
    private static final long FADE_OUT_DELAY = 5000L;
    private static final long FADE_OUT_TIME = 2000L;
    private final Minecraft minecraft;
    private long lastSelectionTime;
    @Nullable
    private SpectatorMenu menu;

    public SpectatorGui(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void onHotbarSelected(int n) {
        this.lastSelectionTime = Util.getMillis();
        if (this.menu != null) {
            this.menu.selectSlot(n);
        } else {
            this.menu = new SpectatorMenu(this);
        }
    }

    private float getHotbarAlpha() {
        long l = this.lastSelectionTime - Util.getMillis() + 5000L;
        return Mth.clamp((float)l / 2000.0f, 0.0f, 1.0f);
    }

    public void renderHotbar(PoseStack poseStack) {
        if (this.menu == null) {
            return;
        }
        float f = this.getHotbarAlpha();
        if (f <= 0.0f) {
            this.menu.exit();
            return;
        }
        int n = this.minecraft.getWindow().getGuiScaledWidth() / 2;
        int n2 = this.getBlitOffset();
        this.setBlitOffset(-90);
        int n3 = Mth.floor((float)this.minecraft.getWindow().getGuiScaledHeight() - 22.0f * f);
        SpectatorPage spectatorPage = this.menu.getCurrentPage();
        this.renderPage(poseStack, f, n, n3, spectatorPage);
        this.setBlitOffset(n2);
    }

    protected void renderPage(PoseStack poseStack, float f, int n, int n2, SpectatorPage spectatorPage) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, f);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        this.blit(poseStack, n - 91, n2, 0, 0, 182, 22);
        if (spectatorPage.getSelectedSlot() >= 0) {
            this.blit(poseStack, n - 91 - 1 + spectatorPage.getSelectedSlot() * 20, n2 - 1, 0, 22, 24, 22);
        }
        for (int i = 0; i < 9; ++i) {
            this.renderSlot(poseStack, i, this.minecraft.getWindow().getGuiScaledWidth() / 2 - 90 + i * 20 + 2, n2 + 3, f, spectatorPage.getItem(i));
        }
        RenderSystem.disableBlend();
    }

    private void renderSlot(PoseStack poseStack, int n, int n2, float f, float f2, SpectatorMenuItem spectatorMenuItem) {
        RenderSystem.setShaderTexture(0, SPECTATOR_LOCATION);
        if (spectatorMenuItem != SpectatorMenu.EMPTY_SLOT) {
            int n3 = (int)(f2 * 255.0f);
            poseStack.pushPose();
            poseStack.translate(n2, f, 0.0);
            float f3 = spectatorMenuItem.isEnabled() ? 1.0f : 0.25f;
            RenderSystem.setShaderColor(f3, f3, f3, f2);
            spectatorMenuItem.renderIcon(poseStack, f3, n3);
            poseStack.popPose();
            if (n3 > 3 && spectatorMenuItem.isEnabled()) {
                Component component = this.minecraft.options.keyHotbarSlots[n].getTranslatedKeyMessage();
                this.minecraft.font.drawShadow(poseStack, component, (float)(n2 + 19 - 2 - this.minecraft.font.width(component)), f + 6.0f + 3.0f, 0xFFFFFF + (n3 << 24));
            }
        }
    }

    public void renderTooltip(PoseStack poseStack) {
        int n = (int)(this.getHotbarAlpha() * 255.0f);
        if (n > 3 && this.menu != null) {
            Component component;
            SpectatorMenuItem spectatorMenuItem = this.menu.getSelectedItem();
            Component component2 = component = spectatorMenuItem == SpectatorMenu.EMPTY_SLOT ? this.menu.getSelectedCategory().getPrompt() : spectatorMenuItem.getName();
            if (component != null) {
                int n2 = (this.minecraft.getWindow().getGuiScaledWidth() - this.minecraft.font.width(component)) / 2;
                int n3 = this.minecraft.getWindow().getGuiScaledHeight() - 35;
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                this.minecraft.font.drawShadow(poseStack, component, (float)n2, (float)n3, 0xFFFFFF + (n << 24));
                RenderSystem.disableBlend();
            }
        }
    }

    @Override
    public void onSpectatorMenuClosed(SpectatorMenu spectatorMenu) {
        this.menu = null;
        this.lastSelectionTime = 0L;
    }

    public boolean isMenuActive() {
        return this.menu != null;
    }

    public void onMouseScrolled(int n) {
        int n2;
        for (n2 = this.menu.getSelectedSlot() + n; !(n2 < 0 || n2 > 8 || this.menu.getItem(n2) != SpectatorMenu.EMPTY_SLOT && this.menu.getItem(n2).isEnabled()); n2 += n) {
        }
        if (n2 >= 0 && n2 <= 8) {
            this.menu.selectSlot(n2);
            this.lastSelectionTime = Util.getMillis();
        }
    }

    public void onMouseMiddleClick() {
        this.lastSelectionTime = Util.getMillis();
        if (this.isMenuActive()) {
            int n = this.menu.getSelectedSlot();
            if (n != -1) {
                this.menu.selectSlot(n);
            }
        } else {
            this.menu = new SpectatorMenu(this);
        }
    }
}

