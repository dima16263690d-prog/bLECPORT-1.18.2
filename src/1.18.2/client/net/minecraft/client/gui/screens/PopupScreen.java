/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;

public class PopupScreen
extends Screen {
    private static final int BUTTON_PADDING = 20;
    private static final int BUTTON_MARGIN = 5;
    private static final int BUTTON_HEIGHT = 20;
    private final Component narrationMessage;
    private final FormattedText message;
    private final ImmutableList<ButtonOption> buttonOptions;
    private MultiLineLabel messageLines = MultiLineLabel.EMPTY;
    private int contentTop;
    private int buttonWidth;

    protected PopupScreen(Component component, List<Component> list, ImmutableList<ButtonOption> immutableList) {
        super(component);
        this.message = FormattedText.composite(list);
        this.narrationMessage = CommonComponents.joinForNarration(component, ComponentUtils.formatList(list, TextComponent.EMPTY));
        this.buttonOptions = immutableList;
    }

    @Override
    public Component getNarrationMessage() {
        return this.narrationMessage;
    }

    @Override
    public void init() {
        for (ButtonOption buttonOption : this.buttonOptions) {
            this.buttonWidth = Math.max(this.buttonWidth, 20 + this.font.width(buttonOption.message) + 20);
        }
        int n = 5 + this.buttonWidth + 5;
        int n2 = n * this.buttonOptions.size();
        this.messageLines = MultiLineLabel.create(this.font, this.message, n2);
        int n3 = this.messageLines.getLineCount() * this.font.lineHeight;
        this.contentTop = (int)((double)this.height / 2.0 - (double)n3 / 2.0);
        int n4 = this.contentTop + n3 + this.font.lineHeight * 2;
        int n5 = (int)((double)this.width / 2.0 - (double)n2 / 2.0);
        for (ButtonOption buttonOption : this.buttonOptions) {
            this.addRenderableWidget(new Button(n5, n4, this.buttonWidth, 20, buttonOption.message, buttonOption.onPress));
            n5 += n;
        }
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderDirtBackground(0);
        PopupScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.contentTop - this.font.lineHeight * 2, -1);
        this.messageLines.renderCentered(poseStack, this.width / 2, this.contentTop);
        super.render(poseStack, n, n2, f);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    public static final class ButtonOption {
        final Component message;
        final Button.OnPress onPress;

        public ButtonOption(Component component, Button.OnPress onPress) {
            this.message = component;
            this.onPress = onPress;
        }
    }
}

