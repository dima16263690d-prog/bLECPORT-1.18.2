/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfirmScreen;
import com.mojang.realmsclient.gui.screens.RealmsInviteScreen;
import com.mojang.realmsclient.util.RealmsTextureManager;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class RealmsPlayerScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation OP_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/op_icon.png");
    private static final ResourceLocation USER_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/user_icon.png");
    private static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_player_icon.png");
    private static final ResourceLocation OPTIONS_BACKGROUND = new ResourceLocation("minecraft", "textures/gui/options_background.png");
    private static final Component NORMAL_USER_TOOLTIP = new TranslatableComponent("mco.configure.world.invites.normal.tooltip");
    private static final Component OP_TOOLTIP = new TranslatableComponent("mco.configure.world.invites.ops.tooltip");
    private static final Component REMOVE_ENTRY_TOOLTIP = new TranslatableComponent("mco.configure.world.invites.remove.tooltip");
    private static final Component INVITED_LABEL = new TranslatableComponent("mco.configure.world.invited");
    @Nullable
    private Component toolTip;
    private final RealmsConfigureWorldScreen lastScreen;
    final RealmsServer serverData;
    private InvitedObjectSelectionList invitedObjectSelectionList;
    int column1X;
    int columnWidth;
    private int column2X;
    private Button removeButton;
    private Button opdeopButton;
    private int selectedInvitedIndex = -1;
    private String selectedInvited;
    int player = -1;
    private boolean stateChanged;
    UserAction hoveredUserAction = UserAction.NONE;

    public RealmsPlayerScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsServer realmsServer) {
        super(new TranslatableComponent("mco.configure.world.players.title"));
        this.lastScreen = realmsConfigureWorldScreen;
        this.serverData = realmsServer;
    }

    @Override
    public void init() {
        this.column1X = this.width / 2 - 160;
        this.columnWidth = 150;
        this.column2X = this.width / 2 + 12;
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.invitedObjectSelectionList = new InvitedObjectSelectionList();
        this.invitedObjectSelectionList.setLeftPos(this.column1X);
        this.addWidget(this.invitedObjectSelectionList);
        for (PlayerInfo playerInfo : this.serverData.players) {
            this.invitedObjectSelectionList.addEntry(playerInfo);
        }
        this.addRenderableWidget(new Button(this.column2X, RealmsPlayerScreen.row(1), this.columnWidth + 10, 20, new TranslatableComponent("mco.configure.world.buttons.invite"), button -> this.minecraft.setScreen(new RealmsInviteScreen(this.lastScreen, this, this.serverData))));
        this.removeButton = this.addRenderableWidget(new Button(this.column2X, RealmsPlayerScreen.row(7), this.columnWidth + 10, 20, new TranslatableComponent("mco.configure.world.invites.remove.tooltip"), button -> this.uninvite(this.player)));
        this.opdeopButton = this.addRenderableWidget(new Button(this.column2X, RealmsPlayerScreen.row(9), this.columnWidth + 10, 20, new TranslatableComponent("mco.configure.world.invites.ops.tooltip"), button -> {
            if (this.serverData.players.get(this.player).isOperator()) {
                this.deop(this.player);
            } else {
                this.op(this.player);
            }
        }));
        this.addRenderableWidget(new Button(this.column2X + this.columnWidth / 2 + 2, RealmsPlayerScreen.row(12), this.columnWidth / 2 + 10 - 2, 20, CommonComponents.GUI_BACK, button -> this.backButtonClicked()));
        this.updateButtonStates();
    }

    void updateButtonStates() {
        this.removeButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.player);
        this.opdeopButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.player);
    }

    private boolean shouldRemoveAndOpdeopButtonBeVisible(int n) {
        return n != -1;
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (n == 256) {
            this.backButtonClicked();
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    private void backButtonClicked() {
        if (this.stateChanged) {
            this.minecraft.setScreen(this.lastScreen.getNewScreen());
        } else {
            this.minecraft.setScreen(this.lastScreen);
        }
    }

    void op(int n) {
        this.updateButtonStates();
        RealmsClient realmsClient = RealmsClient.create();
        String string = this.serverData.players.get(n).getUuid();
        try {
            this.updateOps(realmsClient.op(this.serverData.id, string));
        }
        catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't op the user");
        }
    }

    void deop(int n) {
        this.updateButtonStates();
        RealmsClient realmsClient = RealmsClient.create();
        String string = this.serverData.players.get(n).getUuid();
        try {
            this.updateOps(realmsClient.deop(this.serverData.id, string));
        }
        catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't deop the user");
        }
    }

    private void updateOps(Ops ops) {
        for (PlayerInfo playerInfo : this.serverData.players) {
            playerInfo.setOperator(ops.ops.contains(playerInfo.getName()));
        }
    }

    void uninvite(int n) {
        this.updateButtonStates();
        if (n >= 0 && n < this.serverData.players.size()) {
            PlayerInfo playerInfo = this.serverData.players.get(n);
            this.selectedInvited = playerInfo.getUuid();
            this.selectedInvitedIndex = n;
            RealmsConfirmScreen realmsConfirmScreen = new RealmsConfirmScreen(bl -> {
                if (bl) {
                    RealmsClient realmsClient = RealmsClient.create();
                    try {
                        realmsClient.uninvite(this.serverData.id, this.selectedInvited);
                    }
                    catch (RealmsServiceException realmsServiceException) {
                        LOGGER.error("Couldn't uninvite user");
                    }
                    this.deleteFromInvitedList(this.selectedInvitedIndex);
                    this.player = -1;
                    this.updateButtonStates();
                }
                this.stateChanged = true;
                this.minecraft.setScreen(this);
            }, new TextComponent("Question"), new TranslatableComponent("mco.configure.world.uninvite.question").append(" '").append(playerInfo.getName()).append("' ?"));
            this.minecraft.setScreen(realmsConfirmScreen);
        }
    }

    private void deleteFromInvitedList(int n) {
        this.serverData.players.remove(n);
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.toolTip = null;
        this.hoveredUserAction = UserAction.NONE;
        this.renderBackground(poseStack);
        if (this.invitedObjectSelectionList != null) {
            this.invitedObjectSelectionList.render(poseStack, n, n2, f);
        }
        RealmsPlayerScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 17, 0xFFFFFF);
        int n3 = RealmsPlayerScreen.row(12) + 20;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, OPTIONS_BACKGROUND);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        float f2 = 32.0f;
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(0.0, this.height, 0.0).uv(0.0f, (float)(this.height - n3) / 32.0f + 0.0f).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(this.width, this.height, 0.0).uv((float)this.width / 32.0f, (float)(this.height - n3) / 32.0f + 0.0f).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(this.width, n3, 0.0).uv((float)this.width / 32.0f, 0.0f).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(0.0, n3, 0.0).uv(0.0f, 0.0f).color(64, 64, 64, 255).endVertex();
        tesselator.end();
        if (this.serverData != null && this.serverData.players != null) {
            this.font.draw(poseStack, new TextComponent("").append(INVITED_LABEL).append(" (").append(Integer.toString(this.serverData.players.size())).append(")"), (float)this.column1X, (float)RealmsPlayerScreen.row(0), 0xA0A0A0);
        } else {
            this.font.draw(poseStack, INVITED_LABEL, (float)this.column1X, (float)RealmsPlayerScreen.row(0), 0xA0A0A0);
        }
        super.render(poseStack, n, n2, f);
        if (this.serverData == null) {
            return;
        }
        this.renderMousehoverTooltip(poseStack, this.toolTip, n, n2);
    }

    protected void renderMousehoverTooltip(PoseStack poseStack, @Nullable Component component, int n, int n2) {
        if (component == null) {
            return;
        }
        int n3 = n + 12;
        int n4 = n2 - 12;
        int n5 = this.font.width(component);
        this.fillGradient(poseStack, n3 - 3, n4 - 3, n3 + n5 + 3, n4 + 8 + 3, -1073741824, -1073741824);
        this.font.drawShadow(poseStack, component, (float)n3, (float)n4, 0xFFFFFF);
    }

    void drawRemoveIcon(PoseStack poseStack, int n, int n2, int n3, int n4) {
        boolean bl = n3 >= n && n3 <= n + 9 && n4 >= n2 && n4 <= n2 + 9 && n4 < RealmsPlayerScreen.row(12) + 20 && n4 > RealmsPlayerScreen.row(1);
        RenderSystem.setShaderTexture(0, CROSS_ICON_LOCATION);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        float f = bl ? 7.0f : 0.0f;
        GuiComponent.blit(poseStack, n, n2, 0.0f, f, 8, 7, 8, 14);
        if (bl) {
            this.toolTip = REMOVE_ENTRY_TOOLTIP;
            this.hoveredUserAction = UserAction.REMOVE;
        }
    }

    void drawOpped(PoseStack poseStack, int n, int n2, int n3, int n4) {
        boolean bl = n3 >= n && n3 <= n + 9 && n4 >= n2 && n4 <= n2 + 9 && n4 < RealmsPlayerScreen.row(12) + 20 && n4 > RealmsPlayerScreen.row(1);
        RenderSystem.setShaderTexture(0, OP_ICON_LOCATION);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        float f = bl ? 8.0f : 0.0f;
        GuiComponent.blit(poseStack, n, n2, 0.0f, f, 8, 8, 8, 16);
        if (bl) {
            this.toolTip = OP_TOOLTIP;
            this.hoveredUserAction = UserAction.TOGGLE_OP;
        }
    }

    void drawNormal(PoseStack poseStack, int n, int n2, int n3, int n4) {
        boolean bl = n3 >= n && n3 <= n + 9 && n4 >= n2 && n4 <= n2 + 9 && n4 < RealmsPlayerScreen.row(12) + 20 && n4 > RealmsPlayerScreen.row(1);
        RenderSystem.setShaderTexture(0, USER_ICON_LOCATION);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        float f = bl ? 8.0f : 0.0f;
        GuiComponent.blit(poseStack, n, n2, 0.0f, f, 8, 8, 8, 16);
        if (bl) {
            this.toolTip = NORMAL_USER_TOOLTIP;
            this.hoveredUserAction = UserAction.TOGGLE_OP;
        }
    }

    static enum UserAction {
        TOGGLE_OP,
        REMOVE,
        NONE;

    }

    class InvitedObjectSelectionList
    extends RealmsObjectSelectionList<Entry> {
        public InvitedObjectSelectionList() {
            super(RealmsPlayerScreen.this.columnWidth + 10, RealmsPlayerScreen.row(12) + 20, RealmsPlayerScreen.row(1), RealmsPlayerScreen.row(12) + 20, 13);
        }

        public void addEntry(PlayerInfo playerInfo) {
            this.addEntry(new Entry(playerInfo));
        }

        @Override
        public int getRowWidth() {
            return (int)((double)this.width * 1.0);
        }

        @Override
        public boolean isFocused() {
            return RealmsPlayerScreen.this.getFocused() == this;
        }

        @Override
        public boolean mouseClicked(double d, double d2, int n) {
            if (n == 0 && d < (double)this.getScrollbarPosition() && d2 >= (double)this.y0 && d2 <= (double)this.y1) {
                int n2 = RealmsPlayerScreen.this.column1X;
                int n3 = RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth;
                int n4 = (int)Math.floor(d2 - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
                int n5 = n4 / this.itemHeight;
                if (d >= (double)n2 && d <= (double)n3 && n5 >= 0 && n4 >= 0 && n5 < this.getItemCount()) {
                    this.selectItem(n5);
                    this.itemClicked(n4, n5, d, d2, this.width);
                }
                return true;
            }
            return super.mouseClicked(d, d2, n);
        }

        @Override
        public void itemClicked(int n, int n2, double d, double d2, int n3) {
            if (n2 < 0 || n2 > RealmsPlayerScreen.this.serverData.players.size() || RealmsPlayerScreen.this.hoveredUserAction == UserAction.NONE) {
                return;
            }
            if (RealmsPlayerScreen.this.hoveredUserAction == UserAction.TOGGLE_OP) {
                if (RealmsPlayerScreen.this.serverData.players.get(n2).isOperator()) {
                    RealmsPlayerScreen.this.deop(n2);
                } else {
                    RealmsPlayerScreen.this.op(n2);
                }
            } else if (RealmsPlayerScreen.this.hoveredUserAction == UserAction.REMOVE) {
                RealmsPlayerScreen.this.uninvite(n2);
            }
        }

        @Override
        public void selectItem(int n) {
            super.selectItem(n);
            this.selectInviteListItem(n);
        }

        public void selectInviteListItem(int n) {
            RealmsPlayerScreen.this.player = n;
            RealmsPlayerScreen.this.updateButtonStates();
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            RealmsPlayerScreen.this.player = this.children().indexOf(entry);
            RealmsPlayerScreen.this.updateButtonStates();
        }

        @Override
        public void renderBackground(PoseStack poseStack) {
            RealmsPlayerScreen.this.renderBackground(poseStack);
        }

        @Override
        public int getScrollbarPosition() {
            return RealmsPlayerScreen.this.column1X + this.width - 5;
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 13;
        }
    }

    class Entry
    extends ObjectSelectionList.Entry<Entry> {
        private final PlayerInfo playerInfo;

        public Entry(PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;
        }

        @Override
        public void render(PoseStack poseStack, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            this.renderInvitedItem(poseStack, this.playerInfo, n3, n2, n6, n7);
        }

        private void renderInvitedItem(PoseStack poseStack, PlayerInfo playerInfo, int n, int n2, int n3, int n4) {
            int n5 = !playerInfo.getAccepted() ? 0xA0A0A0 : (playerInfo.getOnline() ? 0x7FFF7F : 0xFFFFFF);
            RealmsPlayerScreen.this.font.draw(poseStack, playerInfo.getName(), (float)(RealmsPlayerScreen.this.column1X + 3 + 12), (float)(n2 + 1), n5);
            if (playerInfo.isOperator()) {
                RealmsPlayerScreen.this.drawOpped(poseStack, RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 10, n2 + 1, n3, n4);
            } else {
                RealmsPlayerScreen.this.drawNormal(poseStack, RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 10, n2 + 1, n3, n4);
            }
            RealmsPlayerScreen.this.drawRemoveIcon(poseStack, RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 22, n2 + 2, n3, n4);
            RealmsTextureManager.withBoundFace(playerInfo.getUuid(), () -> {
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                GuiComponent.blit(poseStack, RealmsPlayerScreen.this.column1X + 2 + 2, n2 + 1, 8, 8, 8.0f, 8.0f, 8, 8, 64, 64);
                GuiComponent.blit(poseStack, RealmsPlayerScreen.this.column1X + 2 + 2, n2 + 1, 8, 8, 40.0f, 8.0f, 8, 8, 64, 64);
            });
        }

        @Override
        public Component getNarration() {
            return new TranslatableComponent("narrator.select", this.playerInfo.getName());
        }
    }
}

