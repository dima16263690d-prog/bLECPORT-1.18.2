/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

public class ControlsScreen
extends OptionsSubScreen {
    private static final int ROW_SPACING = 24;

    public ControlsScreen(Screen screen, Options options) {
        super(screen, options, new TranslatableComponent("controls.title"));
    }

    @Override
    protected void init() {
        super.init();
        int n = this.width / 2 - 155;
        int n2 = n + 160;
        int n3 = this.height / 6 - 12;
        this.addRenderableWidget(new Button(n, n3, 150, 20, new TranslatableComponent("options.mouse_settings"), button -> this.minecraft.setScreen(new MouseSettingsScreen(this, this.options))));
        this.addRenderableWidget(new Button(n2, n3, 150, 20, new TranslatableComponent("controls.keybinds"), button -> this.minecraft.setScreen(new KeyBindsScreen(this, this.options))));
        this.addRenderableWidget(Option.TOGGLE_CROUCH.createButton(this.options, n, n3 += 24, 150));
        this.addRenderableWidget(Option.TOGGLE_SPRINT.createButton(this.options, n2, n3, 150));
        this.addRenderableWidget(Option.AUTO_JUMP.createButton(this.options, n, n3 += 24, 150));
        this.addRenderableWidget(new Button(this.width / 2 - 100, n3 += 24, 200, 20, CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen)));
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderBackground(poseStack);
        ControlsScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(poseStack, n, n2, f);
    }
}

