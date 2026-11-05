/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.realms;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.Component;

public class RealmsLabel
implements Widget {
    private final Component text;
    private final int x;
    private final int y;
    private final int color;

    public RealmsLabel(Component component, int n, int n2, int n3) {
        this.text = component;
        this.x = n;
        this.y = n2;
        this.color = n3;
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        GuiComponent.drawCenteredString(poseStack, Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
    }

    public Component getText() {
        return this.text;
    }
}

