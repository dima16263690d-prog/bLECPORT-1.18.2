/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.booleans.BooleanConsumer
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ConfirmLinkScreen
extends ConfirmScreen {
    private static final Component COPY_BUTTON_TEXT = new TranslatableComponent("chat.copy");
    private static final Component WARNING_TEXT = new TranslatableComponent("chat.link.warning");
    private final String url;
    private final boolean showWarning;

    public ConfirmLinkScreen(BooleanConsumer booleanConsumer, String string, boolean bl) {
        super(booleanConsumer, new TranslatableComponent(bl ? "chat.link.confirmTrusted" : "chat.link.confirm"), new TextComponent(string));
        this.yesButton = bl ? new TranslatableComponent("chat.link.open") : CommonComponents.GUI_YES;
        this.noButton = bl ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO;
        this.showWarning = !bl;
        this.url = string;
    }

    @Override
    protected void addButtons(int n) {
        this.addRenderableWidget(new Button(this.width / 2 - 50 - 105, this.height / 6 + 96, 100, 20, this.yesButton, button -> this.callback.accept(true)));
        this.addRenderableWidget(new Button(this.width / 2 - 50, this.height / 6 + 96, 100, 20, COPY_BUTTON_TEXT, button -> {
            this.copyToClipboard();
            this.callback.accept(false);
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 50 + 105, this.height / 6 + 96, 100, 20, this.noButton, button -> this.callback.accept(false)));
    }

    public void copyToClipboard() {
        this.minecraft.keyboardHandler.setClipboard(this.url);
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        super.render(poseStack, n, n2, f);
        if (this.showWarning) {
            ConfirmLinkScreen.drawCenteredString(poseStack, this.font, WARNING_TEXT, this.width / 2, 110, 0xFFCCCC);
        }
    }
}

