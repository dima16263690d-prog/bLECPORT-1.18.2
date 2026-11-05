/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.gui.screens.RealmsSlotOptionsScreen;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;

public class RealmsBackupInfoScreen
extends RealmsScreen {
    private static final Component TEXT_UNKNOWN = new TextComponent("UNKNOWN");
    private final Screen lastScreen;
    final Backup backup;
    private BackupInfoList backupInfoList;

    public RealmsBackupInfoScreen(Screen screen, Backup backup) {
        super(new TextComponent("Changes from last backup"));
        this.lastScreen = screen;
        this.backup = backup;
    }

    @Override
    public void tick() {
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20, CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)));
        this.backupInfoList = new BackupInfoList(this.minecraft);
        this.addWidget(this.backupInfoList);
        this.magicalSpecialHackyFocus(this.backupInfoList);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (n == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderBackground(poseStack);
        this.backupInfoList.render(poseStack, n, n2, f);
        RealmsBackupInfoScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        super.render(poseStack, n, n2, f);
    }

    Component checkForSpecificMetadata(String string, String string2) {
        String string3 = string.toLowerCase(Locale.ROOT);
        if (string3.contains("game") && string3.contains("mode")) {
            return this.gameModeMetadata(string2);
        }
        if (string3.contains("game") && string3.contains("difficulty")) {
            return this.gameDifficultyMetadata(string2);
        }
        return new TextComponent(string2);
    }

    private Component gameDifficultyMetadata(String string) {
        try {
            return RealmsSlotOptionsScreen.DIFFICULTIES.get(Integer.parseInt(string)).getDisplayName();
        }
        catch (Exception exception) {
            return TEXT_UNKNOWN;
        }
    }

    private Component gameModeMetadata(String string) {
        try {
            return RealmsSlotOptionsScreen.GAME_MODES.get(Integer.parseInt(string)).getShortDisplayName();
        }
        catch (Exception exception) {
            return TEXT_UNKNOWN;
        }
    }

    class BackupInfoList
    extends ObjectSelectionList<BackupInfoListEntry> {
        public BackupInfoList(Minecraft minecraft) {
            super(minecraft, RealmsBackupInfoScreen.this.width, RealmsBackupInfoScreen.this.height, 32, RealmsBackupInfoScreen.this.height - 64, 36);
            this.setRenderSelection(false);
            if (RealmsBackupInfoScreen.this.backup.changeList != null) {
                RealmsBackupInfoScreen.this.backup.changeList.forEach((string, string2) -> this.addEntry(new BackupInfoListEntry((String)string, (String)string2)));
            }
        }
    }

    class BackupInfoListEntry
    extends ObjectSelectionList.Entry<BackupInfoListEntry> {
        private final String key;
        private final String value;

        public BackupInfoListEntry(String string, String string2) {
            this.key = string;
            this.value = string2;
        }

        @Override
        public void render(PoseStack poseStack, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            Font font = ((RealmsBackupInfoScreen)RealmsBackupInfoScreen.this).minecraft.font;
            GuiComponent.drawString(poseStack, font, this.key, n3, n2, 0xA0A0A0);
            GuiComponent.drawString(poseStack, font, RealmsBackupInfoScreen.this.checkForSpecificMetadata(this.key, this.value), n3, n2 + 12, 0xFFFFFF);
        }

        @Override
        public Component getNarration() {
            return new TranslatableComponent("narrator.select", this.key + " " + this.value);
        }
    }
}

