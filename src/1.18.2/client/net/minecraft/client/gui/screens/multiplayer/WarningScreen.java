/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.multiplayer;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public abstract class WarningScreen
extends Screen {
    private final Component titleComponent;
    private final Component content;
    private final Component check;
    private final Component narration;
    protected final Screen previous;
    @Nullable
    protected Checkbox stopShowing;
    private MultiLineLabel message = MultiLineLabel.EMPTY;

    protected WarningScreen(Component component, Component component2, Component component3, Component component4, Screen screen) {
        super(NarratorChatListener.NO_TITLE);
        this.titleComponent = component;
        this.content = component2;
        this.check = component3;
        this.narration = component4;
        this.previous = screen;
    }

    protected abstract void initButtons(int var1);

    @Override
    protected void init() {
        super.init();
        this.message = MultiLineLabel.create(this.font, (FormattedText)this.content, this.width - 50);
        int n = (this.message.getLineCount() + 1) * this.font.lineHeight * 2;
        this.stopShowing = new Checkbox(this.width / 2 - 155 + 80, 76 + n, 150, 20, this.check, false);
        this.addRenderableWidget(this.stopShowing);
        this.initButtons(n);
    }

    @Override
    public Component getNarrationMessage() {
        return this.narration;
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderDirtBackground(0);
        WarningScreen.drawString(poseStack, this.font, this.titleComponent, 25, 30, 0xFFFFFF);
        this.message.renderLeftAligned(poseStack, 25, 70, this.font.lineHeight * 2, 0xFFFFFF);
        super.render(poseStack, n, n2, f);
    }
}

