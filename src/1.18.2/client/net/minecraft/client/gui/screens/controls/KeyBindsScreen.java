/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

public class KeyBindsScreen
extends OptionsSubScreen {
    @Nullable
    public KeyMapping selectedKey;
    public long lastKeySelection;
    private KeyBindsList keyBindsList;
    private Button resetButton;

    public KeyBindsScreen(Screen screen, Options options) {
        super(screen, options, new TranslatableComponent("controls.keybinds.title"));
    }

    @Override
    protected void init() {
        this.keyBindsList = new KeyBindsList(this, this.minecraft);
        this.addWidget(this.keyBindsList);
        this.resetButton = this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslatableComponent("controls.resetAll"), button -> {
            for (KeyMapping keyMapping : this.options.keyMappings) {
                keyMapping.setKey(keyMapping.getDefaultKey());
            }
            KeyMapping.resetMapping();
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height - 29, 150, 20, CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen)));
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (this.selectedKey != null) {
            this.options.setKey(this.selectedKey, InputConstants.Type.MOUSE.getOrCreate(n));
            this.selectedKey = null;
            KeyMapping.resetMapping();
            return true;
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (this.selectedKey != null) {
            if (n == 256) {
                this.options.setKey(this.selectedKey, InputConstants.UNKNOWN);
            } else {
                this.options.setKey(this.selectedKey, InputConstants.getKey(n, n2));
            }
            this.selectedKey = null;
            this.lastKeySelection = Util.getMillis();
            KeyMapping.resetMapping();
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderBackground(poseStack);
        this.keyBindsList.render(poseStack, n, n2, f);
        KeyBindsScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        boolean bl = false;
        for (KeyMapping keyMapping : this.options.keyMappings) {
            if (keyMapping.isDefault()) continue;
            bl = true;
            break;
        }
        this.resetButton.active = bl;
        super.render(poseStack, n, n2, f);
    }
}

