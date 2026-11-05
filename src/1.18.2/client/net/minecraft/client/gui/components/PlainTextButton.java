/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

public class PlainTextButton
extends Button {
    private final Font font;
    private final Component message;
    private final Component underlinedMessage;

    public PlainTextButton(int n, int n2, int n3, int n4, Component component, Button.OnPress onPress, Font font) {
        super(n, n2, n3, n4, component, onPress);
        this.font = font;
        this.message = component;
        this.underlinedMessage = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withUnderlined(true));
    }

    @Override
    public void renderButton(PoseStack poseStack, int n, int n2, float f) {
        Component component = this.isHoveredOrFocused() ? this.underlinedMessage : this.message;
        PlainTextButton.drawString(poseStack, this.font, component, this.x, this.y, 0xFFFFFF | Mth.ceil(this.alpha * 255.0f) << 24);
    }
}

