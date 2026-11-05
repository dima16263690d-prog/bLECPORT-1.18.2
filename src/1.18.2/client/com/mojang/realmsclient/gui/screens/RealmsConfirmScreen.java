/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.booleans.BooleanConsumer
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

public class RealmsConfirmScreen
extends RealmsScreen {
    protected BooleanConsumer callback;
    private final Component title1;
    private final Component title2;

    public RealmsConfirmScreen(BooleanConsumer booleanConsumer, Component component, Component component2) {
        super(NarratorChatListener.NO_TITLE);
        this.callback = booleanConsumer;
        this.title1 = component;
        this.title2 = component2;
    }

    @Override
    public void init() {
        this.addRenderableWidget(new Button(this.width / 2 - 105, RealmsConfirmScreen.row(9), 100, 20, CommonComponents.GUI_YES, button -> this.callback.accept(true)));
        this.addRenderableWidget(new Button(this.width / 2 + 5, RealmsConfirmScreen.row(9), 100, 20, CommonComponents.GUI_NO, button -> this.callback.accept(false)));
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderBackground(poseStack);
        RealmsConfirmScreen.drawCenteredString(poseStack, this.font, this.title1, this.width / 2, RealmsConfirmScreen.row(3), 0xFFFFFF);
        RealmsConfirmScreen.drawCenteredString(poseStack, this.font, this.title2, this.width / 2, RealmsConfirmScreen.row(5), 0xFFFFFF);
        super.render(poseStack, n, n2, f);
    }
}

