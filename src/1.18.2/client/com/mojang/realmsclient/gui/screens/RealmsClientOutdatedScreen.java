/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;

public class RealmsClientOutdatedScreen
extends RealmsScreen {
    private static final Component OUTDATED_TITLE = new TranslatableComponent("mco.client.outdated.title");
    private static final Component[] OUTDATED_MESSAGES = new Component[]{new TranslatableComponent("mco.client.outdated.msg.line1"), new TranslatableComponent("mco.client.outdated.msg.line2")};
    private static final Component INCOMPATIBLE_TITLE = new TranslatableComponent("mco.client.incompatible.title");
    private static final Component[] INCOMPATIBLE_MESSAGES = new Component[]{new TranslatableComponent("mco.client.incompatible.msg.line1"), new TranslatableComponent("mco.client.incompatible.msg.line2"), new TranslatableComponent("mco.client.incompatible.msg.line3")};
    private final Screen lastScreen;
    private final boolean outdated;

    public RealmsClientOutdatedScreen(Screen screen, boolean bl) {
        super(bl ? OUTDATED_TITLE : INCOMPATIBLE_TITLE);
        this.lastScreen = screen;
        this.outdated = bl;
    }

    @Override
    public void init() {
        this.addRenderableWidget(new Button(this.width / 2 - 100, RealmsClientOutdatedScreen.row(12), 200, 20, CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)));
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderBackground(poseStack);
        RealmsClientOutdatedScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, RealmsClientOutdatedScreen.row(3), 0xFF0000);
        Component[] componentArray = this.outdated ? INCOMPATIBLE_MESSAGES : OUTDATED_MESSAGES;
        for (int i = 0; i < componentArray.length; ++i) {
            RealmsClientOutdatedScreen.drawCenteredString(poseStack, this.font, componentArray[i], this.width / 2, RealmsClientOutdatedScreen.row(5) + i * 12, 0xFFFFFF);
        }
        super.render(poseStack, n, n2, f);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (n == 257 || n == 335 || n == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }
}

