/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.util.concurrent.Runnables
 *  com.mojang.logging.LogUtils
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Vector3f;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.Realms32bitWarningScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;

public class TitleScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEMO_LEVEL_ID = "Demo_World";
    public static final Component COPYRIGHT_TEXT = new TextComponent("Copyright Mojang AB. Do not distribute!");
    public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
    private final boolean minceraftEasterEgg;
    @Nullable
    private String splash;
    private Button resetDemoButton;
    private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation MINECRAFT_EDITION = new ResourceLocation("textures/gui/title/edition.png");
    private Screen realmsNotificationsScreen;
    private final PanoramaRenderer panorama = new PanoramaRenderer(CUBE_MAP);
    private final boolean fading;
    private long fadeInStart;
    @Nullable
    private Warning32Bit warning32Bit;
    private RealmsClient realmsClient;
    private boolean realms32bitWarningShown = false;

    public TitleScreen() {
        this(false);
    }

    public TitleScreen(boolean bl) {
        super(new TranslatableComponent("narrator.screen.title"));
        this.fading = bl;
        this.minceraftEasterEgg = (double)new Random().nextFloat() < 1.0E-4;
        this.realmsClient = RealmsClient.create();
    }

    private boolean realmsNotificationsEnabled() {
        return this.minecraft.options.realmsNotifications && this.realmsNotificationsScreen != null;
    }

    @Override
    public void tick() {
        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.tick();
        }
        this.showRealms32BitWarningIfNeeded();
    }

    private void showRealms32BitWarningIfNeeded() {
        try {
            if (this.warning32Bit != null && !this.minecraft.options.skipRealms32bitWarning && !this.realms32bitWarningShown && this.warning32Bit.realmsSubscriptionFuture.getNow(false).booleanValue()) {
                this.realms32bitWarningShown = true;
                this.minecraft.setScreen(new Realms32bitWarningScreen(this));
            }
        }
        catch (CompletionException completionException) {
            LOGGER.warn("Failed to retrieve realms subscriptions", (Throwable)completionException);
            this.realms32bitWarningShown = true;
        }
    }

    public static CompletableFuture<Void> preloadResources(TextureManager textureManager, Executor executor) {
        return CompletableFuture.allOf(textureManager.preload(MINECRAFT_LOGO, executor), textureManager.preload(MINECRAFT_EDITION, executor), textureManager.preload(PANORAMA_OVERLAY, executor), CUBE_MAP.preload(textureManager, executor));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        if (this.splash == null) {
            this.splash = this.minecraft.getSplashManager().getSplash();
        }
        int n = this.font.width(COPYRIGHT_TEXT);
        int n2 = this.width - n - 2;
        int n3 = 24;
        int n4 = this.height / 4 + 48;
        if (this.minecraft.isDemo()) {
            this.createDemoMenuOptions(n4, 24);
        } else {
            this.createNormalMenuOptions(n4, 24);
        }
        this.addRenderableWidget(new ImageButton(this.width / 2 - 124, n4 + 72 + 12, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, button -> this.minecraft.setScreen(new LanguageSelectScreen((Screen)this, this.minecraft.options, this.minecraft.getLanguageManager())), new TranslatableComponent("narrator.button.language")));
        this.addRenderableWidget(new Button(this.width / 2 - 100, n4 + 72 + 12, 98, 20, new TranslatableComponent("menu.options"), button -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))));
        this.addRenderableWidget(new Button(this.width / 2 + 2, n4 + 72 + 12, 98, 20, new TranslatableComponent("menu.quit"), button -> this.minecraft.stop()));
        this.addRenderableWidget(new ImageButton(this.width / 2 + 104, n4 + 72 + 12, 20, 20, 0, 0, 20, ACCESSIBILITY_TEXTURE, 32, 64, button -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), new TranslatableComponent("narrator.button.accessibility")));
        this.addRenderableWidget(new PlainTextButton(n2, this.height - 10, n, 10, COPYRIGHT_TEXT, button -> this.minecraft.setScreen(new WinScreen(false, Runnables.doNothing())), this.font));
        this.minecraft.setConnectedToRealms(false);
        if (this.minecraft.options.realmsNotifications && this.realmsNotificationsScreen == null) {
            this.realmsNotificationsScreen = new RealmsNotificationsScreen();
        }
        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
        }
        if (!this.minecraft.is64Bit()) {
            CompletableFuture<Boolean> completableFuture = this.warning32Bit != null ? this.warning32Bit.realmsSubscriptionFuture : CompletableFuture.supplyAsync(this::hasRealmsSubscription, Util.backgroundExecutor());
            this.warning32Bit = new Warning32Bit(MultiLineLabel.create(this.font, (FormattedText)new TranslatableComponent("title.32bit.deprecation"), 350, 2), this.width / 2, n4 - 24, completableFuture);
        }
    }

    private boolean hasRealmsSubscription() {
        try {
            return this.realmsClient.listWorlds().servers.stream().anyMatch(realmsServer -> realmsServer.ownerUUID != null && !realmsServer.expired && realmsServer.ownerUUID.equals(this.minecraft.getUser().getUuid()));
        }
        catch (RealmsServiceException realmsServiceException) {
            return false;
        }
    }

    private void createNormalMenuOptions(int n, int n2) {
        this.addRenderableWidget(new Button(this.width / 2 - 100, n, 200, 20, new TranslatableComponent("menu.singleplayer"), button -> this.minecraft.setScreen(new SelectWorldScreen(this))));
        boolean bl = this.minecraft.allowsMultiplayer();
        Button.OnTooltip onTooltip = bl ? Button.NO_TOOLTIP : new Button.OnTooltip(){
            private final Component text = new TranslatableComponent("title.multiplayer.disabled");

            @Override
            public void onTooltip(Button button, PoseStack poseStack, int n, int n2) {
                if (!button.active) {
                    TitleScreen.this.renderTooltip(poseStack, TitleScreen.this.minecraft.font.split(this.text, Math.max(TitleScreen.this.width / 2 - 43, 170)), n, n2);
                }
            }

            @Override
            public void narrateTooltip(Consumer<Component> consumer) {
                consumer.accept(this.text);
            }
        };
        this.addRenderableWidget(new Button((int)(this.width / 2 - 100), (int)(n + n2 * 1), (int)200, (int)20, (Component)new TranslatableComponent((String)"menu.multiplayer"), (Button.OnPress)(Button.OnPress)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/client/gui/components/Button;)V, lambda$createNormalMenuOptions$7(net.minecraft.client.gui.components.Button ), (Lnet/minecraft/client/gui/components/Button;)V)((TitleScreen)this), (Button.OnTooltip)onTooltip)).active = bl;
        this.addRenderableWidget(new Button((int)(this.width / 2 - 100), (int)(n + n2 * 2), (int)200, (int)20, (Component)new TranslatableComponent((String)"menu.online"), (Button.OnPress)(Button.OnPress)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/client/gui/components/Button;)V, lambda$createNormalMenuOptions$8(net.minecraft.client.gui.components.Button ), (Lnet/minecraft/client/gui/components/Button;)V)((TitleScreen)this), (Button.OnTooltip)onTooltip)).active = bl;
    }

    private void createDemoMenuOptions(int n, int n2) {
        boolean bl = this.checkDemoWorldPresence();
        this.addRenderableWidget(new Button(this.width / 2 - 100, n, 200, 20, new TranslatableComponent("menu.playdemo"), button -> {
            if (bl) {
                this.minecraft.loadLevel(DEMO_LEVEL_ID);
            } else {
                RegistryAccess registryAccess = RegistryAccess.BUILTIN.get();
                this.minecraft.createLevel(DEMO_LEVEL_ID, MinecraftServer.DEMO_SETTINGS, registryAccess, WorldGenSettings.demoSettings(registryAccess));
            }
        }));
        this.resetDemoButton = this.addRenderableWidget(new Button(this.width / 2 - 100, n + n2 * 1, 200, 20, new TranslatableComponent("menu.resetdemo"), button -> {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(DEMO_LEVEL_ID);){
                LevelSummary levelSummary = levelStorageAccess.getSummary();
                if (levelSummary != null) {
                    this.minecraft.setScreen(new ConfirmScreen(this::confirmDemo, new TranslatableComponent("selectWorld.deleteQuestion"), new TranslatableComponent("selectWorld.deleteWarning", levelSummary.getLevelName()), new TranslatableComponent("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));
                }
            }
            catch (IOException iOException) {
                SystemToast.onWorldAccessFailure(this.minecraft, DEMO_LEVEL_ID);
                LOGGER.warn("Failed to access demo world", (Throwable)iOException);
            }
        }));
        this.resetDemoButton.active = bl;
    }

    private boolean checkDemoWorldPresence() {
        boolean bl;
        block8: {
            LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(DEMO_LEVEL_ID);
            try {
                boolean bl2 = bl = levelStorageAccess.getSummary() != null;
                if (levelStorageAccess == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (levelStorageAccess != null) {
                        try {
                            levelStorageAccess.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException iOException) {
                    SystemToast.onWorldAccessFailure(this.minecraft, DEMO_LEVEL_ID);
                    LOGGER.warn("Failed to read demo world data", (Throwable)iOException);
                    return false;
                }
            }
            levelStorageAccess.close();
        }
        return bl;
    }

    private void realmsButtonClicked() {
        this.minecraft.setScreen(new RealmsMainScreen(this));
    }

    @Override
    public void render(PoseStack poseStack, int n3, int n4, float f) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }
        float f2 = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0f : 1.0f;
        this.panorama.render(f, Mth.clamp(f2, 0.0f, 1.0f));
        int n5 = 274;
        int n6 = this.width / 2 - 137;
        int n7 = 30;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.fading ? (float)Mth.ceil(Mth.clamp(f2, 0.0f, 1.0f)) : 1.0f);
        TitleScreen.blit(poseStack, 0, 0, this.width, this.height, 0.0f, 0.0f, 16, 128, 16, 128);
        float f3 = this.fading ? Mth.clamp(f2 - 1.0f, 0.0f, 1.0f) : 1.0f;
        int n8 = Mth.ceil(f3 * 255.0f) << 24;
        if ((n8 & 0xFC000000) == 0) {
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, MINECRAFT_LOGO);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, f3);
        if (this.minceraftEasterEgg) {
            this.blitOutlineBlack(n6, 30, (n, n2) -> {
                this.blit(poseStack, n + 0, (int)n2, 0, 0, 99, 44);
                this.blit(poseStack, n + 99, (int)n2, 129, 0, 27, 44);
                this.blit(poseStack, n + 99 + 26, (int)n2, 126, 0, 3, 44);
                this.blit(poseStack, n + 99 + 26 + 3, (int)n2, 99, 0, 26, 44);
                this.blit(poseStack, n + 155, (int)n2, 0, 45, 155, 44);
            });
        } else {
            this.blitOutlineBlack(n6, 30, (n, n2) -> {
                this.blit(poseStack, n + 0, (int)n2, 0, 0, 155, 44);
                this.blit(poseStack, n + 155, (int)n2, 0, 45, 155, 44);
            });
        }
        RenderSystem.setShaderTexture(0, MINECRAFT_EDITION);
        TitleScreen.blit(poseStack, n6 + 88, 67, 0.0f, 0.0f, 98, 14, 128, 16);
        if (this.warning32Bit != null) {
            this.warning32Bit.label.renderBackgroundCentered(poseStack, this.warning32Bit.x, this.warning32Bit.y, this.font.lineHeight, 2, 0x55200000);
            this.warning32Bit.label.renderCentered(poseStack, this.warning32Bit.x, this.warning32Bit.y, this.font.lineHeight, 0xFFFFFF | n8);
        }
        if (this.splash != null) {
            poseStack.pushPose();
            poseStack.translate(this.width / 2 + 90, 70.0, 0.0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-20.0f));
            float f4 = 1.8f - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0f * ((float)Math.PI * 2)) * 0.1f);
            f4 = f4 * 100.0f / (float)(this.font.width(this.splash) + 32);
            poseStack.scale(f4, f4, f4);
            TitleScreen.drawCenteredString(poseStack, this.font, this.splash, 0, -8, 0xFFFF00 | n8);
            poseStack.popPose();
        }
        String string = "Minecraft " + SharedConstants.getCurrentVersion().getName();
        string = this.minecraft.isDemo() ? string + " Demo" : string + (String)("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            string = string + I18n.get("menu.modded", new Object[0]);
        }
        TitleScreen.drawString(poseStack, this.font, string, 2, this.height - 10, 0xFFFFFF | n8);
        for (GuiEventListener guiEventListener : this.children()) {
            if (!(guiEventListener instanceof AbstractWidget)) continue;
            ((AbstractWidget)guiEventListener).setAlpha(f3);
        }
        super.render(poseStack, n3, n4, f);
        if (this.realmsNotificationsEnabled() && f3 >= 1.0f) {
            this.realmsNotificationsScreen.render(poseStack, n3, n4, f);
        }
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (super.mouseClicked(d, d2, n)) {
            return true;
        }
        return this.realmsNotificationsEnabled() && this.realmsNotificationsScreen.mouseClicked(d, d2, n);
    }

    @Override
    public void removed() {
        if (this.realmsNotificationsScreen != null) {
            this.realmsNotificationsScreen.removed();
        }
    }

    private void confirmDemo(boolean bl) {
        if (bl) {
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(DEMO_LEVEL_ID);){
                levelStorageAccess.deleteLevel();
            }
            catch (IOException iOException) {
                SystemToast.onWorldDeleteFailure(this.minecraft, DEMO_LEVEL_ID);
                LOGGER.warn("Failed to delete demo world", (Throwable)iOException);
            }
        }
        this.minecraft.setScreen(this);
    }

    private /* synthetic */ void lambda$createNormalMenuOptions$8(Button button) {
        this.realmsButtonClicked();
    }

    private /* synthetic */ void lambda$createNormalMenuOptions$7(Button button) {
        Screen screen = this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this);
        this.minecraft.setScreen(screen);
    }

    static final class Warning32Bit
    extends Record {
        final MultiLineLabel label;
        final int x;
        final int y;
        final CompletableFuture<Boolean> realmsSubscriptionFuture;

        Warning32Bit(MultiLineLabel multiLineLabel, int n, int n2, CompletableFuture<Boolean> completableFuture) {
            this.label = multiLineLabel;
            this.x = n;
            this.y = n2;
            this.realmsSubscriptionFuture = completableFuture;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Warning32Bit.class, "label;x;y;realmsSubscriptionFuture", "label", "x", "y", "realmsSubscriptionFuture"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Warning32Bit.class, "label;x;y;realmsSubscriptionFuture", "label", "x", "y", "realmsSubscriptionFuture"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Warning32Bit.class, "label;x;y;realmsSubscriptionFuture", "label", "x", "y", "realmsSubscriptionFuture"}, this, object);
        }

        public MultiLineLabel label() {
            return this.label;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

        public CompletableFuture<Boolean> realmsSubscriptionFuture() {
            return this.realmsSubscriptionFuture;
        }
    }
}

