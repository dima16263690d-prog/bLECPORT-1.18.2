/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringUtils
 */
package net.minecraft.world.level.storage;

import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelVersion;
import org.apache.commons.lang3.StringUtils;

public class LevelSummary
implements Comparable<LevelSummary> {
    private final LevelSettings settings;
    private final LevelVersion levelVersion;
    private final String levelId;
    private final boolean requiresManualConversion;
    private final boolean locked;
    private final File icon;
    @Nullable
    private Component info;

    public LevelSummary(LevelSettings levelSettings, LevelVersion levelVersion, String string, boolean bl, boolean bl2, File file) {
        this.settings = levelSettings;
        this.levelVersion = levelVersion;
        this.levelId = string;
        this.locked = bl2;
        this.icon = file;
        this.requiresManualConversion = bl;
    }

    public String getLevelId() {
        return this.levelId;
    }

    public String getLevelName() {
        return StringUtils.isEmpty((CharSequence)this.settings.levelName()) ? this.levelId : this.settings.levelName();
    }

    public File getIcon() {
        return this.icon;
    }

    public boolean requiresManualConversion() {
        return this.requiresManualConversion;
    }

    public long getLastPlayed() {
        return this.levelVersion.lastPlayed();
    }

    @Override
    public int compareTo(LevelSummary levelSummary) {
        if (this.levelVersion.lastPlayed() < levelSummary.levelVersion.lastPlayed()) {
            return 1;
        }
        if (this.levelVersion.lastPlayed() > levelSummary.levelVersion.lastPlayed()) {
            return -1;
        }
        return this.levelId.compareTo(levelSummary.levelId);
    }

    public LevelSettings getSettings() {
        return this.settings;
    }

    public GameType getGameMode() {
        return this.settings.gameType();
    }

    public boolean isHardcore() {
        return this.settings.hardcore();
    }

    public boolean hasCheats() {
        return this.settings.allowCommands();
    }

    public MutableComponent getWorldVersionName() {
        if (StringUtil.isNullOrEmpty(this.levelVersion.minecraftVersionName())) {
            return new TranslatableComponent("selectWorld.versionUnknown");
        }
        return new TextComponent(this.levelVersion.minecraftVersionName());
    }

    public LevelVersion levelVersion() {
        return this.levelVersion;
    }

    public boolean markVersionInList() {
        return this.askToOpenWorld() || !SharedConstants.getCurrentVersion().isStable() && !this.levelVersion.snapshot() || this.backupStatus().shouldBackup();
    }

    public boolean askToOpenWorld() {
        return this.levelVersion.minecraftVersion().getVersion() > SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }

    public BackupStatus backupStatus() {
        WorldVersion worldVersion = SharedConstants.getCurrentVersion();
        int n = worldVersion.getDataVersion().getVersion();
        int n2 = this.levelVersion.minecraftVersion().getVersion();
        if (!worldVersion.isStable() && n2 < n) {
            return BackupStatus.UPGRADE_TO_SNAPSHOT;
        }
        if (n2 > n) {
            return BackupStatus.DOWNGRADE;
        }
        return BackupStatus.NONE;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public boolean isDisabled() {
        if (this.isLocked() || this.requiresManualConversion()) {
            return true;
        }
        return !this.isCompatible();
    }

    public boolean isCompatible() {
        return SharedConstants.getCurrentVersion().getDataVersion().isCompatible(this.levelVersion.minecraftVersion());
    }

    public Component getInfo() {
        if (this.info == null) {
            this.info = this.createInfo();
        }
        return this.info;
    }

    private Component createInfo() {
        TranslatableComponent translatableComponent;
        if (this.isLocked()) {
            return new TranslatableComponent("selectWorld.locked").withStyle(ChatFormatting.RED);
        }
        if (this.requiresManualConversion()) {
            return new TranslatableComponent("selectWorld.conversion").withStyle(ChatFormatting.RED);
        }
        if (!this.isCompatible()) {
            return new TranslatableComponent("selectWorld.incompatible_series").withStyle(ChatFormatting.RED);
        }
        MutableComponent mutableComponent = translatableComponent = this.isHardcore() ? new TextComponent("").append(new TranslatableComponent("gameMode.hardcore").withStyle(ChatFormatting.DARK_RED)) : new TranslatableComponent("gameMode." + this.getGameMode().getName());
        if (this.hasCheats()) {
            translatableComponent.append(", ").append(new TranslatableComponent("selectWorld.cheats"));
        }
        MutableComponent mutableComponent2 = this.getWorldVersionName();
        MutableComponent mutableComponent3 = new TextComponent(", ").append(new TranslatableComponent("selectWorld.version")).append(" ");
        if (this.markVersionInList()) {
            mutableComponent3.append(mutableComponent2.withStyle(this.askToOpenWorld() ? ChatFormatting.RED : ChatFormatting.ITALIC));
        } else {
            mutableComponent3.append(mutableComponent2);
        }
        translatableComponent.append(mutableComponent3);
        return translatableComponent;
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((LevelSummary)object);
    }

    public static enum BackupStatus {
        NONE(false, false, ""),
        DOWNGRADE(true, true, "downgrade"),
        UPGRADE_TO_SNAPSHOT(true, false, "snapshot");

        private final boolean shouldBackup;
        private final boolean severe;
        private final String translationKey;

        private BackupStatus(boolean bl, boolean bl2, String string2) {
            this.shouldBackup = bl;
            this.severe = bl2;
            this.translationKey = string2;
        }

        public boolean shouldBackup() {
            return this.shouldBackup;
        }

        public boolean isSevere() {
            return this.severe;
        }

        public String getTranslationKey() {
            return this.translationKey;
        }
    }
}

