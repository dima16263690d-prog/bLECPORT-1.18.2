/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;

public class RealmsGenericErrorScreen
extends RealmsScreen {
    private final Screen nextScreen;
    private final Pair<Component, Component> lines;
    private MultiLineLabel line2Split = MultiLineLabel.EMPTY;

    public RealmsGenericErrorScreen(RealmsServiceException realmsServiceException, Screen screen) {
        super(NarratorChatListener.NO_TITLE);
        this.nextScreen = screen;
        this.lines = RealmsGenericErrorScreen.errorMessage(realmsServiceException);
    }

    public RealmsGenericErrorScreen(Component component, Screen screen) {
        super(NarratorChatListener.NO_TITLE);
        this.nextScreen = screen;
        this.lines = RealmsGenericErrorScreen.errorMessage(component);
    }

    public RealmsGenericErrorScreen(Component component, Component component2, Screen screen) {
        super(NarratorChatListener.NO_TITLE);
        this.nextScreen = screen;
        this.lines = RealmsGenericErrorScreen.errorMessage(component, component2);
    }

    private static Pair<Component, Component> errorMessage(RealmsServiceException realmsServiceException) {
        if (realmsServiceException.realmsError == null) {
            return Pair.of((Object)new TextComponent("An error occurred (" + realmsServiceException.httpResultCode + "):"), (Object)new TextComponent(realmsServiceException.rawResponse));
        }
        String string = "mco.errorMessage." + realmsServiceException.realmsError.getErrorCode();
        return Pair.of((Object)new TextComponent("Realms (" + realmsServiceException.realmsError + "):"), (Object)(I18n.exists(string) ? new TranslatableComponent(string) : Component.nullToEmpty(realmsServiceException.realmsError.getErrorMessage())));
    }

    private static Pair<Component, Component> errorMessage(Component component) {
        return Pair.of((Object)new TextComponent("An error occurred: "), (Object)component);
    }

    private static Pair<Component, Component> errorMessage(Component component, Component component2) {
        return Pair.of((Object)component, (Object)component2);
    }

    @Override
    public void init() {
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 52, 200, 20, new TextComponent("Ok"), button -> this.minecraft.setScreen(this.nextScreen)));
        this.line2Split = MultiLineLabel.create(this.font, (FormattedText)this.lines.getSecond(), this.width * 3 / 4);
    }

    @Override
    public Component getNarrationMessage() {
        return new TextComponent("").append((Component)this.lines.getFirst()).append(": ").append((Component)this.lines.getSecond());
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderBackground(poseStack);
        RealmsGenericErrorScreen.drawCenteredString(poseStack, this.font, (Component)this.lines.getFirst(), this.width / 2, 80, 0xFFFFFF);
        this.line2Split.renderCentered(poseStack, this.width / 2, 100, this.minecraft.font.lineHeight, 0xFF0000);
        super.render(poseStack, n, n2, f);
    }
}

