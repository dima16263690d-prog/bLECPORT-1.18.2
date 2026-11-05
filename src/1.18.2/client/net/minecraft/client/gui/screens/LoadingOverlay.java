/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class LoadingOverlay
extends Overlay {
    static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojangstudios.png");
    private static final int LOGO_BACKGROUND_COLOR = FastColor.ARGB32.color(255, 239, 50, 61);
    private static final int LOGO_BACKGROUND_COLOR_DARK = FastColor.ARGB32.color(255, 0, 0, 0);
    private static final IntSupplier BRAND_BACKGROUND = () -> Minecraft.getInstance().options.darkMojangStudiosBackground ? LOGO_BACKGROUND_COLOR_DARK : LOGO_BACKGROUND_COLOR;
    private static final int LOGO_SCALE = 240;
    private static final float LOGO_QUARTER_FLOAT = 60.0f;
    private static final int LOGO_QUARTER = 60;
    private static final int LOGO_HALF = 120;
    private static final float LOGO_OVERLAP = 0.0625f;
    private static final float SMOOTHING = 0.95f;
    public static final long FADE_OUT_TIME = 1000L;
    public static final long FADE_IN_TIME = 500L;
    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> onFinish;
    private final boolean fadeIn;
    private float currentProgress;
    private long fadeOutStart = -1L;
    private long fadeInStart = -1L;

    public LoadingOverlay(Minecraft minecraft, ReloadInstance reloadInstance, Consumer<Optional<Throwable>> consumer, boolean bl) {
        this.minecraft = minecraft;
        this.reload = reloadInstance;
        this.onFinish = consumer;
        this.fadeIn = bl;
    }

    public static void registerTextures(Minecraft minecraft) {
        minecraft.getTextureManager().register(MOJANG_STUDIOS_LOGO_LOCATION, new LogoTexture());
    }

    private static int replaceAlpha(int n, int n2) {
        return n & 0xFFFFFF | n2 << 24;
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        float f2;
        int n3;
        float f3;
        int n4 = this.minecraft.getWindow().getGuiScaledWidth();
        int n5 = this.minecraft.getWindow().getGuiScaledHeight();
        long l = Util.getMillis();
        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = l;
        }
        float f4 = this.fadeOutStart > -1L ? (float)(l - this.fadeOutStart) / 1000.0f : -1.0f;
        float f5 = f3 = this.fadeInStart > -1L ? (float)(l - this.fadeInStart) / 500.0f : -1.0f;
        if (f4 >= 1.0f) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(poseStack, 0, 0, f);
            }
            n3 = Mth.ceil((1.0f - Mth.clamp(f4 - 1.0f, 0.0f, 1.0f)) * 255.0f);
            LoadingOverlay.fill(poseStack, 0, 0, n4, n5, LoadingOverlay.replaceAlpha(BRAND_BACKGROUND.getAsInt(), n3));
            f2 = 1.0f - Mth.clamp(f4 - 1.0f, 0.0f, 1.0f);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && f3 < 1.0f) {
                this.minecraft.screen.render(poseStack, n, n2, f);
            }
            n3 = Mth.ceil(Mth.clamp((double)f3, 0.15, 1.0) * 255.0);
            LoadingOverlay.fill(poseStack, 0, 0, n4, n5, LoadingOverlay.replaceAlpha(BRAND_BACKGROUND.getAsInt(), n3));
            f2 = Mth.clamp(f3, 0.0f, 1.0f);
        } else {
            n3 = BRAND_BACKGROUND.getAsInt();
            float f6 = (float)(n3 >> 16 & 0xFF) / 255.0f;
            float f7 = (float)(n3 >> 8 & 0xFF) / 255.0f;
            float f8 = (float)(n3 & 0xFF) / 255.0f;
            GlStateManager._clearColor(f6, f7, f8, 1.0f);
            GlStateManager._clear(16384, Minecraft.ON_OSX);
            f2 = 1.0f;
        }
        n3 = (int)((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.5);
        int n6 = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.5);
        double d = Math.min((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.75, (double)this.minecraft.getWindow().getGuiScaledHeight()) * 0.25;
        int n7 = (int)(d * 0.5);
        double d2 = d * 4.0;
        int n8 = (int)(d2 * 0.5);
        RenderSystem.setShaderTexture(0, MOJANG_STUDIOS_LOGO_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, f2);
        LoadingOverlay.blit(poseStack, n3 - n8, n6 - n7, n8, (int)d, -0.0625f, 0.0f, 120, 60, 120, 120);
        LoadingOverlay.blit(poseStack, n3, n6 - n7, n8, (int)d, 0.0625f, 60.0f, 120, 60, 120, 120);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        int n9 = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.8325);
        float f9 = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95f + f9 * 0.050000012f, 0.0f, 1.0f);
        if (f4 < 1.0f) {
            this.drawProgressBar(poseStack, n4 / 2 - n8, n9 - 5, n4 / 2 + n8, n9 + 5, 1.0f - Mth.clamp(f4, 0.0f, 1.0f));
        }
        if (f4 >= 2.0f) {
            this.minecraft.setOverlay(null);
        }
        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || f3 >= 2.0f)) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            }
            catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }
            this.fadeOutStart = Util.getMillis();
            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
            }
        }
    }

    private void drawProgressBar(PoseStack poseStack, int n, int n2, int n3, int n4, float f) {
        int n5 = Mth.ceil((float)(n3 - n - 2) * this.currentProgress);
        int n6 = Math.round(f * 255.0f);
        int n7 = FastColor.ARGB32.color(n6, 255, 255, 255);
        LoadingOverlay.fill(poseStack, n + 2, n2 + 2, n + n5, n4 - 2, n7);
        LoadingOverlay.fill(poseStack, n + 1, n2, n3 - 1, n2 + 1, n7);
        LoadingOverlay.fill(poseStack, n + 1, n4, n3 - 1, n4 - 1, n7);
        LoadingOverlay.fill(poseStack, n, n2, n + 1, n4, n7);
        LoadingOverlay.fill(poseStack, n3, n2, n3 - 1, n4, n7);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    static class LogoTexture
    extends SimpleTexture {
        public LogoTexture() {
            super(MOJANG_STUDIOS_LOGO_LOCATION);
        }

        @Override
        protected SimpleTexture.TextureImage getTextureImage(ResourceManager resourceManager) {
            SimpleTexture.TextureImage textureImage;
            block8: {
                Minecraft minecraft = Minecraft.getInstance();
                VanillaPackResources vanillaPackResources = minecraft.getClientPackSource().getVanillaPack();
                InputStream inputStream = vanillaPackResources.getResource(PackType.CLIENT_RESOURCES, MOJANG_STUDIOS_LOGO_LOCATION);
                try {
                    textureImage = new SimpleTexture.TextureImage(new TextureMetadataSection(true, true), NativeImage.read(inputStream));
                    if (inputStream == null) break block8;
                }
                catch (Throwable throwable) {
                    try {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (IOException iOException) {
                        return new SimpleTexture.TextureImage(iOException);
                    }
                }
                inputStream.close();
            }
            return textureImage;
        }
    }
}

