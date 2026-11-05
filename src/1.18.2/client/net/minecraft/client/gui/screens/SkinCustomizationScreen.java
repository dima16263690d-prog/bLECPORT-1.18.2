/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.PlayerModelPart;

public class SkinCustomizationScreen
extends OptionsSubScreen {
    public SkinCustomizationScreen(Screen screen, Options options) {
        super(screen, options, new TranslatableComponent("options.skinCustomisation.title"));
    }

    @Override
    protected void init() {
        int n = 0;
        for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
            this.addRenderableWidget(CycleButton.onOffBuilder(this.options.isModelPartEnabled(playerModelPart)).create(this.width / 2 - 155 + n % 2 * 160, this.height / 6 + 24 * (n >> 1), 150, 20, playerModelPart.getName(), (cycleButton, bl) -> this.options.toggleModelPart(playerModelPart, (boolean)bl)));
            ++n;
        }
        this.addRenderableWidget(Option.MAIN_HAND.createButton(this.options, this.width / 2 - 155 + n % 2 * 160, this.height / 6 + 24 * (n >> 1), 150));
        if (++n % 2 == 1) {
            ++n;
        }
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 24 * (n >> 1), 200, 20, CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen)));
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderBackground(poseStack);
        SkinCustomizationScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(poseStack, n, n2, f);
    }
}

