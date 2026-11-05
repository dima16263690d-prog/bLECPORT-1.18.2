/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsResetWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsUploadScreen;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;

public class RealmsSelectFileToUploadScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final Component WORLD_TEXT = new TranslatableComponent("selectWorld.world");
    static final Component HARDCORE_TEXT = new TranslatableComponent("mco.upload.hardcore").withStyle(ChatFormatting.DARK_RED);
    static final Component CHEATS_TEXT = new TranslatableComponent("selectWorld.cheats");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    private final RealmsResetWorldScreen lastScreen;
    private final long worldId;
    private final int slotId;
    Button uploadButton;
    List<LevelSummary> levelList = Lists.newArrayList();
    int selectedWorld = -1;
    WorldSelectionList worldSelectionList;
    private final Runnable callback;

    public RealmsSelectFileToUploadScreen(long l, int n, RealmsResetWorldScreen realmsResetWorldScreen, Runnable runnable) {
        super(new TranslatableComponent("mco.upload.select.world.title"));
        this.lastScreen = realmsResetWorldScreen;
        this.worldId = l;
        this.slotId = n;
        this.callback = runnable;
    }

    private void loadLevelList() throws Exception {
        this.levelList = this.minecraft.getLevelSource().getLevelList().stream().filter(levelSummary -> !levelSummary.requiresManualConversion() && !levelSummary.isLocked()).sorted((levelSummary, levelSummary2) -> {
            if (levelSummary.getLastPlayed() < levelSummary2.getLastPlayed()) {
                return 1;
            }
            if (levelSummary.getLastPlayed() > levelSummary2.getLastPlayed()) {
                return -1;
            }
            return levelSummary.getLevelId().compareTo(levelSummary2.getLevelId());
        }).collect(Collectors.toList());
        for (LevelSummary levelSummary3 : this.levelList) {
            this.worldSelectionList.addEntry(levelSummary3);
        }
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.worldSelectionList = new WorldSelectionList();
        try {
            this.loadLevelList();
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't load level list", (Throwable)exception);
            this.minecraft.setScreen(new RealmsGenericErrorScreen(new TextComponent("Unable to load worlds"), Component.nullToEmpty(exception.getMessage()), this.lastScreen));
            return;
        }
        this.addWidget(this.worldSelectionList);
        this.uploadButton = this.addRenderableWidget(new Button(this.width / 2 - 154, this.height - 32, 153, 20, new TranslatableComponent("mco.upload.button.name"), button -> this.upload()));
        this.uploadButton.active = this.selectedWorld >= 0 && this.selectedWorld < this.levelList.size();
        this.addRenderableWidget(new Button(this.width / 2 + 6, this.height - 32, 153, 20, CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)));
        this.addLabel(new RealmsLabel(new TranslatableComponent("mco.upload.select.world.subtitle"), this.width / 2, RealmsSelectFileToUploadScreen.row(-1), 0xA0A0A0));
        if (this.levelList.isEmpty()) {
            this.addLabel(new RealmsLabel(new TranslatableComponent("mco.upload.select.world.none"), this.width / 2, this.height / 2 - 20, 0xFFFFFF));
        }
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(this.getTitle(), this.createLabelNarration());
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void upload() {
        if (this.selectedWorld != -1 && !this.levelList.get(this.selectedWorld).isHardcore()) {
            LevelSummary levelSummary = this.levelList.get(this.selectedWorld);
            this.minecraft.setScreen(new RealmsUploadScreen(this.worldId, this.slotId, this.lastScreen, levelSummary, this.callback));
        }
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderBackground(poseStack);
        this.worldSelectionList.render(poseStack, n, n2, f);
        RealmsSelectFileToUploadScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 13, 0xFFFFFF);
        super.render(poseStack, n, n2, f);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (n == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    static Component gameModeName(LevelSummary levelSummary) {
        return levelSummary.getGameMode().getLongDisplayName();
    }

    static String formatLastPlayed(LevelSummary levelSummary) {
        return DATE_FORMAT.format(new Date(levelSummary.getLastPlayed()));
    }

    class WorldSelectionList
    extends RealmsObjectSelectionList<Entry> {
        public WorldSelectionList() {
            super(RealmsSelectFileToUploadScreen.this.width, RealmsSelectFileToUploadScreen.this.height, RealmsSelectFileToUploadScreen.row(0), RealmsSelectFileToUploadScreen.this.height - 40, 36);
        }

        public void addEntry(LevelSummary levelSummary) {
            this.addEntry(new Entry(levelSummary));
        }

        @Override
        public int getMaxPosition() {
            return RealmsSelectFileToUploadScreen.this.levelList.size() * 36;
        }

        @Override
        public boolean isFocused() {
            return RealmsSelectFileToUploadScreen.this.getFocused() == this;
        }

        @Override
        public void renderBackground(PoseStack poseStack) {
            RealmsSelectFileToUploadScreen.this.renderBackground(poseStack);
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            RealmsSelectFileToUploadScreen.this.selectedWorld = this.children().indexOf(entry);
            RealmsSelectFileToUploadScreen.this.uploadButton.active = RealmsSelectFileToUploadScreen.this.selectedWorld >= 0 && RealmsSelectFileToUploadScreen.this.selectedWorld < this.getItemCount() && !RealmsSelectFileToUploadScreen.this.levelList.get(RealmsSelectFileToUploadScreen.this.selectedWorld).isHardcore();
        }
    }

    class Entry
    extends ObjectSelectionList.Entry<Entry> {
        private final LevelSummary levelSummary;
        private final String name;
        private final String id;
        private final Component info;

        public Entry(LevelSummary levelSummary) {
            this.levelSummary = levelSummary;
            this.name = levelSummary.getLevelName();
            this.id = levelSummary.getLevelId() + " (" + RealmsSelectFileToUploadScreen.formatLastPlayed(levelSummary) + ")";
            Component component = levelSummary.isHardcore() ? HARDCORE_TEXT : RealmsSelectFileToUploadScreen.gameModeName(levelSummary);
            if (levelSummary.hasCheats()) {
                component = component.copy().append(", ").append(CHEATS_TEXT);
            }
            this.info = component;
        }

        @Override
        public void render(PoseStack poseStack, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            this.renderItem(poseStack, n, n3, n2);
        }

        @Override
        public boolean mouseClicked(double d, double d2, int n) {
            RealmsSelectFileToUploadScreen.this.worldSelectionList.selectItem(RealmsSelectFileToUploadScreen.this.levelList.indexOf(this.levelSummary));
            return true;
        }

        protected void renderItem(PoseStack poseStack, int n, int n2, int n3) {
            Object object = this.name.isEmpty() ? WORLD_TEXT + " " + (n + 1) : this.name;
            RealmsSelectFileToUploadScreen.this.font.draw(poseStack, (String)object, (float)(n2 + 2), (float)(n3 + 1), 0xFFFFFF);
            RealmsSelectFileToUploadScreen.this.font.draw(poseStack, this.id, (float)(n2 + 2), (float)(n3 + 12), 0x808080);
            RealmsSelectFileToUploadScreen.this.font.draw(poseStack, this.info, (float)(n2 + 2), (float)(n3 + 12 + 10), 0x808080);
        }

        @Override
        public Component getNarration() {
            Component component = CommonComponents.joinLines(new TextComponent(this.levelSummary.getLevelName()), new TextComponent(RealmsSelectFileToUploadScreen.formatLastPlayed(this.levelSummary)), RealmsSelectFileToUploadScreen.gameModeName(this.levelSummary));
            return new TranslatableComponent("narrator.select", component);
        }
    }
}

