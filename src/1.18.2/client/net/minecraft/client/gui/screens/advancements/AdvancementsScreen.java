/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;

public class AdvancementsScreen
extends Screen
implements ClientAdvancements.Listener {
    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
    private static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 18;
    public static final int WINDOW_INSIDE_WIDTH = 234;
    public static final int WINDOW_INSIDE_HEIGHT = 113;
    private static final int WINDOW_TITLE_X = 8;
    private static final int WINDOW_TITLE_Y = 6;
    public static final int BACKGROUND_TILE_WIDTH = 16;
    public static final int BACKGROUND_TILE_HEIGHT = 16;
    public static final int BACKGROUND_TILE_COUNT_X = 14;
    public static final int BACKGROUND_TILE_COUNT_Y = 7;
    private static final Component VERY_SAD_LABEL = new TranslatableComponent("advancements.sad_label");
    private static final Component NO_ADVANCEMENTS_LABEL = new TranslatableComponent("advancements.empty");
    private static final Component TITLE = new TranslatableComponent("gui.advancements");
    private final ClientAdvancements advancements;
    private final Map<Advancement, AdvancementTab> tabs = Maps.newLinkedHashMap();
    @Nullable
    private AdvancementTab selectedTab;
    private boolean isScrolling;

    public AdvancementsScreen(ClientAdvancements clientAdvancements) {
        super(NarratorChatListener.NO_TITLE);
        this.advancements = clientAdvancements;
    }

    @Override
    protected void init() {
        this.tabs.clear();
        this.selectedTab = null;
        this.advancements.setListener(this);
        if (this.selectedTab == null && !this.tabs.isEmpty()) {
            this.advancements.setSelectedTab(this.tabs.values().iterator().next().getAdvancement(), true);
        } else {
            this.advancements.setSelectedTab(this.selectedTab == null ? null : this.selectedTab.getAdvancement(), true);
        }
    }

    @Override
    public void removed() {
        this.advancements.setListener(null);
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        if (clientPacketListener != null) {
            clientPacketListener.send(ServerboundSeenAdvancementsPacket.closedScreen());
        }
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (n == 0) {
            int n2 = (this.width - 252) / 2;
            int n3 = (this.height - 140) / 2;
            for (AdvancementTab advancementTab : this.tabs.values()) {
                if (!advancementTab.isMouseOver(n2, n3, d, d2)) continue;
                this.advancements.setSelectedTab(advancementTab.getAdvancement(), true);
                break;
            }
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (this.minecraft.options.keyAdvancements.matches(n, n2)) {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        int n3 = (this.width - 252) / 2;
        int n4 = (this.height - 140) / 2;
        this.renderBackground(poseStack);
        this.renderInside(poseStack, n, n2, n3, n4);
        this.renderWindow(poseStack, n3, n4);
        this.renderTooltips(poseStack, n, n2, n3, n4);
    }

    @Override
    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        if (n != 0) {
            this.isScrolling = false;
            return false;
        }
        if (!this.isScrolling) {
            this.isScrolling = true;
        } else if (this.selectedTab != null) {
            this.selectedTab.scroll(d3, d4);
        }
        return true;
    }

    private void renderInside(PoseStack poseStack, int n, int n2, int n3, int n4) {
        AdvancementTab advancementTab = this.selectedTab;
        if (advancementTab == null) {
            AdvancementsScreen.fill(poseStack, n3 + 9, n4 + 18, n3 + 9 + 234, n4 + 18 + 113, -16777216);
            int n5 = n3 + 9 + 117;
            AdvancementsScreen.drawCenteredString(poseStack, this.font, NO_ADVANCEMENTS_LABEL, n5, n4 + 18 + 56 - this.font.lineHeight / 2, -1);
            AdvancementsScreen.drawCenteredString(poseStack, this.font, VERY_SAD_LABEL, n5, n4 + 18 + 113 - this.font.lineHeight, -1);
            return;
        }
        PoseStack poseStack2 = RenderSystem.getModelViewStack();
        poseStack2.pushPose();
        poseStack2.translate(n3 + 9, n4 + 18, 0.0);
        RenderSystem.applyModelViewMatrix();
        advancementTab.drawContents(poseStack);
        poseStack2.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
    }

    public void renderWindow(PoseStack poseStack, int n, int n2) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WINDOW_LOCATION);
        this.blit(poseStack, n, n2, 0, 0, 252, 140);
        if (this.tabs.size() > 1) {
            RenderSystem.setShaderTexture(0, TABS_LOCATION);
            for (AdvancementTab advancementTab : this.tabs.values()) {
                advancementTab.drawTab(poseStack, n, n2, advancementTab == this.selectedTab);
            }
            RenderSystem.defaultBlendFunc();
            for (AdvancementTab advancementTab : this.tabs.values()) {
                advancementTab.drawIcon(n, n2, this.itemRenderer);
            }
            RenderSystem.disableBlend();
        }
        this.font.draw(poseStack, TITLE, (float)(n + 8), (float)(n2 + 6), 0x404040);
    }

    private void renderTooltips(PoseStack poseStack, int n, int n2, int n3, int n4) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.selectedTab != null) {
            PoseStack poseStack2 = RenderSystem.getModelViewStack();
            poseStack2.pushPose();
            poseStack2.translate(n3 + 9, n4 + 18, 400.0);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableDepthTest();
            this.selectedTab.drawTooltips(poseStack, n - n3 - 9, n2 - n4 - 18, n3, n4);
            RenderSystem.disableDepthTest();
            poseStack2.popPose();
            RenderSystem.applyModelViewMatrix();
        }
        if (this.tabs.size() > 1) {
            for (AdvancementTab advancementTab : this.tabs.values()) {
                if (!advancementTab.isMouseOver(n3, n4, n, n2)) continue;
                this.renderTooltip(poseStack, advancementTab.getTitle(), n, n2);
            }
        }
    }

    @Override
    public void onAddAdvancementRoot(Advancement advancement) {
        AdvancementTab advancementTab = AdvancementTab.create(this.minecraft, this, this.tabs.size(), advancement);
        if (advancementTab == null) {
            return;
        }
        this.tabs.put(advancement, advancementTab);
    }

    @Override
    public void onRemoveAdvancementRoot(Advancement advancement) {
    }

    @Override
    public void onAddAdvancementTask(Advancement advancement) {
        AdvancementTab advancementTab = this.getTab(advancement);
        if (advancementTab != null) {
            advancementTab.addAdvancement(advancement);
        }
    }

    @Override
    public void onRemoveAdvancementTask(Advancement advancement) {
    }

    @Override
    public void onUpdateAdvancementProgress(Advancement advancement, AdvancementProgress advancementProgress) {
        AdvancementWidget advancementWidget = this.getAdvancementWidget(advancement);
        if (advancementWidget != null) {
            advancementWidget.setProgress(advancementProgress);
        }
    }

    @Override
    public void onSelectedTabChanged(@Nullable Advancement advancement) {
        this.selectedTab = this.tabs.get(advancement);
    }

    @Override
    public void onAdvancementsCleared() {
        this.tabs.clear();
        this.selectedTab = null;
    }

    @Nullable
    public AdvancementWidget getAdvancementWidget(Advancement advancement) {
        AdvancementTab advancementTab = this.getTab(advancement);
        return advancementTab == null ? null : advancementTab.getWidget(advancement);
    }

    @Nullable
    private AdvancementTab getTab(Advancement advancement) {
        while (advancement.getParent() != null) {
            advancement = advancement.getParent();
        }
        return this.tabs.get(advancement);
    }
}

