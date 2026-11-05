/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import java.util.List;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

public class RealmsSlotOptionsScreen
extends RealmsScreen {
    private static final int DEFAULT_DIFFICULTY = 2;
    public static final List<Difficulty> DIFFICULTIES = ImmutableList.of((Object)((Object)Difficulty.PEACEFUL), (Object)((Object)Difficulty.EASY), (Object)((Object)Difficulty.NORMAL), (Object)((Object)Difficulty.HARD));
    private static final int DEFAULT_GAME_MODE = 0;
    public static final List<GameType> GAME_MODES = ImmutableList.of((Object)((Object)GameType.SURVIVAL), (Object)((Object)GameType.CREATIVE), (Object)((Object)GameType.ADVENTURE));
    private static final Component NAME_LABEL = new TranslatableComponent("mco.configure.world.edit.slot.name");
    static final Component SPAWN_PROTECTION_TEXT = new TranslatableComponent("mco.configure.world.spawnProtection");
    private EditBox nameEdit;
    protected final RealmsConfigureWorldScreen parent;
    private int column1X;
    private int columnWidth;
    private final RealmsWorldOptions options;
    private final RealmsServer.WorldType worldType;
    private final int activeSlot;
    private Difficulty difficulty;
    private GameType gameMode;
    private boolean pvp;
    private boolean spawnNPCs;
    private boolean spawnAnimals;
    private boolean spawnMonsters;
    int spawnProtection;
    private boolean commandBlocks;
    private boolean forceGameMode;
    SettingsSlider spawnProtectionButton;

    public RealmsSlotOptionsScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsWorldOptions realmsWorldOptions, RealmsServer.WorldType worldType, int n) {
        super(new TranslatableComponent("mco.configure.world.buttons.options"));
        this.parent = realmsConfigureWorldScreen;
        this.options = realmsWorldOptions;
        this.worldType = worldType;
        this.activeSlot = n;
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (n == 256) {
            this.minecraft.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    private static <T> T findByIndex(List<T> list, int n, int n2) {
        try {
            return list.get(n);
        }
        catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            return list.get(n2);
        }
    }

    private static <T> int findIndex(List<T> list, T t, int n) {
        int n2 = list.indexOf(t);
        return n2 == -1 ? n : n2;
    }

    @Override
    public void init() {
        Object object;
        this.columnWidth = 170;
        this.column1X = this.width / 2 - this.columnWidth;
        int n = this.width / 2 + 10;
        this.difficulty = RealmsSlotOptionsScreen.findByIndex(DIFFICULTIES, this.options.difficulty, 2);
        this.gameMode = RealmsSlotOptionsScreen.findByIndex(GAME_MODES, this.options.gameMode, 0);
        if (this.worldType == RealmsServer.WorldType.NORMAL) {
            this.pvp = this.options.pvp;
            this.spawnProtection = this.options.spawnProtection;
            this.forceGameMode = this.options.forceGameMode;
            this.spawnAnimals = this.options.spawnAnimals;
            this.spawnMonsters = this.options.spawnMonsters;
            this.spawnNPCs = this.options.spawnNPCs;
            this.commandBlocks = this.options.commandBlocks;
        } else {
            object = this.worldType == RealmsServer.WorldType.ADVENTUREMAP ? new TranslatableComponent("mco.configure.world.edit.subscreen.adventuremap") : (this.worldType == RealmsServer.WorldType.INSPIRATION ? new TranslatableComponent("mco.configure.world.edit.subscreen.inspiration") : new TranslatableComponent("mco.configure.world.edit.subscreen.experience"));
            this.addLabel(new RealmsLabel((Component)object, this.width / 2, 26, 0xFF0000));
            this.pvp = true;
            this.spawnProtection = 0;
            this.forceGameMode = false;
            this.spawnAnimals = true;
            this.spawnMonsters = true;
            this.spawnNPCs = true;
            this.commandBlocks = true;
        }
        this.nameEdit = new EditBox(this.minecraft.font, this.column1X + 2, RealmsSlotOptionsScreen.row(1), this.columnWidth - 4, 20, null, new TranslatableComponent("mco.configure.world.edit.slot.name"));
        this.nameEdit.setMaxLength(10);
        this.nameEdit.setValue(this.options.getSlotName(this.activeSlot));
        this.magicalSpecialHackyFocus(this.nameEdit);
        object = this.addRenderableWidget(CycleButton.onOffBuilder(this.pvp).create(n, RealmsSlotOptionsScreen.row(1), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.pvp"), (cycleButton, bl) -> {
            this.pvp = bl;
        }));
        this.addRenderableWidget(CycleButton.builder(GameType::getShortDisplayName).withValues(GAME_MODES).withInitialValue(this.gameMode).create(this.column1X, RealmsSlotOptionsScreen.row(3), this.columnWidth, 20, new TranslatableComponent("selectWorld.gameMode"), (cycleButton, gameType) -> {
            this.gameMode = gameType;
        }));
        CycleButton<Boolean> cycleButton3 = this.addRenderableWidget(CycleButton.onOffBuilder(this.spawnAnimals).create(n, RealmsSlotOptionsScreen.row(3), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.spawnAnimals"), (cycleButton, bl) -> {
            this.spawnAnimals = bl;
        }));
        CycleButton<Boolean> cycleButton4 = CycleButton.onOffBuilder(this.difficulty != Difficulty.PEACEFUL && this.spawnMonsters).create(n, RealmsSlotOptionsScreen.row(5), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.spawnMonsters"), (cycleButton, bl) -> {
            this.spawnMonsters = bl;
        });
        this.addRenderableWidget(CycleButton.builder(Difficulty::getDisplayName).withValues(DIFFICULTIES).withInitialValue(this.difficulty).create(this.column1X, RealmsSlotOptionsScreen.row(5), this.columnWidth, 20, new TranslatableComponent("options.difficulty"), (cycleButton2, difficulty) -> {
            this.difficulty = difficulty;
            if (this.worldType == RealmsServer.WorldType.NORMAL) {
                boolean bl;
                cycleButton.active = bl = this.difficulty != Difficulty.PEACEFUL;
                cycleButton4.setValue(bl && this.spawnMonsters);
            }
        }));
        this.addRenderableWidget(cycleButton4);
        this.spawnProtectionButton = this.addRenderableWidget(new SettingsSlider(this.column1X, RealmsSlotOptionsScreen.row(7), this.columnWidth, this.spawnProtection, 0.0f, 16.0f));
        CycleButton<Boolean> cycleButton5 = this.addRenderableWidget(CycleButton.onOffBuilder(this.spawnNPCs).create(n, RealmsSlotOptionsScreen.row(7), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.spawnNPCs"), (cycleButton, bl) -> {
            this.spawnNPCs = bl;
        }));
        CycleButton<Boolean> cycleButton6 = this.addRenderableWidget(CycleButton.onOffBuilder(this.forceGameMode).create(this.column1X, RealmsSlotOptionsScreen.row(9), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.forceGameMode"), (cycleButton, bl) -> {
            this.forceGameMode = bl;
        }));
        CycleButton<Boolean> cycleButton7 = this.addRenderableWidget(CycleButton.onOffBuilder(this.commandBlocks).create(n, RealmsSlotOptionsScreen.row(9), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.commandBlocks"), (cycleButton, bl) -> {
            this.commandBlocks = bl;
        }));
        if (this.worldType != RealmsServer.WorldType.NORMAL) {
            ((CycleButton)object).active = false;
            cycleButton3.active = false;
            cycleButton5.active = false;
            cycleButton4.active = false;
            this.spawnProtectionButton.active = false;
            cycleButton7.active = false;
            cycleButton6.active = false;
        }
        if (this.difficulty == Difficulty.PEACEFUL) {
            cycleButton4.active = false;
        }
        this.addRenderableWidget(new Button(this.column1X, RealmsSlotOptionsScreen.row(13), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.buttons.done"), button -> this.saveSettings()));
        this.addRenderableWidget(new Button(n, RealmsSlotOptionsScreen.row(13), this.columnWidth, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)));
        this.addWidget(this.nameEdit);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(this.getTitle(), this.createLabelNarration());
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderBackground(poseStack);
        RealmsSlotOptionsScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 17, 0xFFFFFF);
        this.font.draw(poseStack, NAME_LABEL, (float)(this.column1X + this.columnWidth / 2 - this.font.width(NAME_LABEL) / 2), (float)(RealmsSlotOptionsScreen.row(0) - 5), 0xFFFFFF);
        this.nameEdit.render(poseStack, n, n2, f);
        super.render(poseStack, n, n2, f);
    }

    private String getSlotName() {
        if (this.nameEdit.getValue().equals(this.options.getDefaultSlotName(this.activeSlot))) {
            return "";
        }
        return this.nameEdit.getValue();
    }

    private void saveSettings() {
        int n = RealmsSlotOptionsScreen.findIndex(DIFFICULTIES, this.difficulty, 2);
        int n2 = RealmsSlotOptionsScreen.findIndex(GAME_MODES, this.gameMode, 0);
        if (this.worldType == RealmsServer.WorldType.ADVENTUREMAP || this.worldType == RealmsServer.WorldType.EXPERIENCE || this.worldType == RealmsServer.WorldType.INSPIRATION) {
            this.parent.saveSlotSettings(new RealmsWorldOptions(this.options.pvp, this.options.spawnAnimals, this.options.spawnMonsters, this.options.spawnNPCs, this.options.spawnProtection, this.options.commandBlocks, n, n2, this.options.forceGameMode, this.getSlotName()));
        } else {
            this.parent.saveSlotSettings(new RealmsWorldOptions(this.pvp, this.spawnAnimals, this.spawnMonsters, this.spawnNPCs, this.spawnProtection, this.commandBlocks, n, n2, this.forceGameMode, this.getSlotName()));
        }
    }

    class SettingsSlider
    extends AbstractSliderButton {
        private final double minValue;
        private final double maxValue;

        public SettingsSlider(int n, int n2, int n3, int n4, float f, float f2) {
            super(n, n2, n3, 20, TextComponent.EMPTY, 0.0);
            this.minValue = f;
            this.maxValue = f2;
            this.value = (Mth.clamp((float)n4, f, f2) - f) / (f2 - f);
            this.updateMessage();
        }

        @Override
        public void applyValue() {
            if (!RealmsSlotOptionsScreen.this.spawnProtectionButton.active) {
                return;
            }
            RealmsSlotOptionsScreen.this.spawnProtection = (int)Mth.lerp(Mth.clamp(this.value, 0.0, 1.0), this.minValue, this.maxValue);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(CommonComponents.optionNameValue(SPAWN_PROTECTION_TEXT, RealmsSlotOptionsScreen.this.spawnProtection == 0 ? CommonComponents.OPTION_OFF : new TextComponent(String.valueOf(RealmsSlotOptionsScreen.this.spawnProtection))));
        }

        @Override
        public void onClick(double d, double d2) {
        }

        @Override
        public void onRelease(double d, double d2) {
        }
    }
}

