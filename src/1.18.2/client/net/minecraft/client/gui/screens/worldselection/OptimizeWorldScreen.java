/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.booleans.BooleanConsumer
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.WorldStem;
import net.minecraft.util.Mth;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class OptimizeWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Object2IntMap<ResourceKey<Level>> DIMENSION_COLORS = (Object2IntMap)Util.make(new Object2IntOpenCustomHashMap(Util.identityStrategy()), object2IntOpenCustomHashMap -> {
        object2IntOpenCustomHashMap.put(Level.OVERWORLD, -13408734);
        object2IntOpenCustomHashMap.put(Level.NETHER, -10075085);
        object2IntOpenCustomHashMap.put(Level.END, -8943531);
        object2IntOpenCustomHashMap.defaultReturnValue(-2236963);
    });
    private final BooleanConsumer callback;
    private final WorldUpgrader upgrader;

    @Nullable
    public static OptimizeWorldScreen create(Minecraft minecraft, BooleanConsumer booleanConsumer, DataFixer dataFixer, LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl) {
        WorldStem worldStem = minecraft.makeWorldStem(levelStorageAccess, false);
        try {
            WorldData worldData = worldStem.worldData();
            levelStorageAccess.saveDataTag(worldStem.registryAccess(), worldData);
            OptimizeWorldScreen optimizeWorldScreen = new OptimizeWorldScreen(booleanConsumer, dataFixer, levelStorageAccess, worldData.getLevelSettings(), bl, worldData.worldGenSettings());
            if (worldStem != null) {
                worldStem.close();
            }
            return optimizeWorldScreen;
        }
        catch (Throwable throwable) {
            try {
                if (worldStem != null) {
                    try {
                        worldStem.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to load datapacks, can't optimize world", (Throwable)exception);
                return null;
            }
        }
    }

    private OptimizeWorldScreen(BooleanConsumer booleanConsumer, DataFixer dataFixer, LevelStorageSource.LevelStorageAccess levelStorageAccess, LevelSettings levelSettings, boolean bl, WorldGenSettings worldGenSettings) {
        super(new TranslatableComponent("optimizeWorld.title", levelSettings.levelName()));
        this.callback = booleanConsumer;
        this.upgrader = new WorldUpgrader(levelStorageAccess, dataFixer, worldGenSettings, bl);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 150, 200, 20, CommonComponents.GUI_CANCEL, button -> {
            this.upgrader.cancel();
            this.callback.accept(false);
        }));
    }

    @Override
    public void tick() {
        if (this.upgrader.isFinished()) {
            this.callback.accept(true);
        }
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Override
    public void removed() {
        this.upgrader.cancel();
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderBackground(poseStack);
        OptimizeWorldScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        int n3 = this.width / 2 - 150;
        int n4 = this.width / 2 + 150;
        int n5 = this.height / 4 + 100;
        int n6 = n5 + 10;
        OptimizeWorldScreen.drawCenteredString(poseStack, this.font, this.upgrader.getStatus(), this.width / 2, n5 - this.font.lineHeight - 2, 0xA0A0A0);
        if (this.upgrader.getTotalChunks() > 0) {
            OptimizeWorldScreen.fill(poseStack, n3 - 1, n5 - 1, n4 + 1, n6 + 1, -16777216);
            OptimizeWorldScreen.drawString(poseStack, this.font, new TranslatableComponent("optimizeWorld.info.converted", this.upgrader.getConverted()), n3, 40, 0xA0A0A0);
            OptimizeWorldScreen.drawString(poseStack, this.font, new TranslatableComponent("optimizeWorld.info.skipped", this.upgrader.getSkipped()), n3, 40 + this.font.lineHeight + 3, 0xA0A0A0);
            OptimizeWorldScreen.drawString(poseStack, this.font, new TranslatableComponent("optimizeWorld.info.total", this.upgrader.getTotalChunks()), n3, 40 + (this.font.lineHeight + 3) * 2, 0xA0A0A0);
            int n7 = 0;
            for (ResourceKey resourceKey : this.upgrader.levels()) {
                int n8 = Mth.floor(this.upgrader.dimensionProgress(resourceKey) * (float)(n4 - n3));
                OptimizeWorldScreen.fill(poseStack, n3 + n7, n5, n3 + n7 + n8, n6, DIMENSION_COLORS.getInt((Object)resourceKey));
                n7 += n8;
            }
            int n9 = this.upgrader.getConverted() + this.upgrader.getSkipped();
            OptimizeWorldScreen.drawCenteredString(poseStack, this.font, n9 + " / " + this.upgrader.getTotalChunks(), this.width / 2, n5 + 2 * this.font.lineHeight + 2, 0xA0A0A0);
            OptimizeWorldScreen.drawCenteredString(poseStack, this.font, Mth.floor(this.upgrader.getProgress() * 100.0f) + "%", this.width / 2, n5 + (n6 - n5) / 2 - this.font.lineHeight / 2, 0xA0A0A0);
        }
        super.render(poseStack, n, n2, f);
    }
}

