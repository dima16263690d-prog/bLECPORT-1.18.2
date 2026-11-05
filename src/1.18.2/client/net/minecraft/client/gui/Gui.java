/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Ordering
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringUtils
 */
package net.minecraft.client.gui;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.ChatListener;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.chat.OverlayChatListener;
import net.minecraft.client.gui.chat.StandardChatListener;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.apache.commons.lang3.StringUtils;

public class Gui
extends GuiComponent {
    private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation PUMPKIN_BLUR_LOCATION = new ResourceLocation("textures/misc/pumpkinblur.png");
    private static final ResourceLocation SPYGLASS_SCOPE_LOCATION = new ResourceLocation("textures/misc/spyglass_scope.png");
    private static final ResourceLocation POWDER_SNOW_OUTLINE_LOCATION = new ResourceLocation("textures/misc/powder_snow_outline.png");
    private static final Component DEMO_EXPIRED_TEXT = new TranslatableComponent("demo.demoExpired");
    private static final Component SAVING_TEXT = new TranslatableComponent("menu.savingLevel");
    private static final int COLOR_WHITE = 0xFFFFFF;
    private static final float MIN_CROSSHAIR_ATTACK_SPEED = 5.0f;
    private static final int NUM_HEARTS_PER_ROW = 10;
    private static final int LINE_HEIGHT = 10;
    private static final String SPACER = ": ";
    private static final float PORTAL_OVERLAY_ALPHA_MIN = 0.2f;
    private static final int HEART_SIZE = 9;
    private static final int HEART_SEPARATION = 8;
    private static final float AUTOSAVE_FADE_SPEED_FACTOR = 0.2f;
    private final Random random = new Random();
    private final Minecraft minecraft;
    private final ItemRenderer itemRenderer;
    private final ChatComponent chat;
    private int tickCount;
    @Nullable
    private Component overlayMessageString;
    private int overlayMessageTime;
    private boolean animateOverlayMessageColor;
    public float vignetteBrightness = 1.0f;
    private int toolHighlightTimer;
    private ItemStack lastToolHighlight = ItemStack.EMPTY;
    private final DebugScreenOverlay debugScreen;
    private final SubtitleOverlay subtitleOverlay;
    private final SpectatorGui spectatorGui;
    private final PlayerTabOverlay tabList;
    private final BossHealthOverlay bossOverlay;
    private int titleTime;
    @Nullable
    private Component title;
    @Nullable
    private Component subtitle;
    private int titleFadeInTime;
    private int titleStayTime;
    private int titleFadeOutTime;
    private int lastHealth;
    private int displayHealth;
    private long lastHealthTime;
    private long healthBlinkTime;
    private int screenWidth;
    private int screenHeight;
    private float autosaveIndicatorValue;
    private float lastAutosaveIndicatorValue;
    private final Map<ChatType, List<ChatListener>> chatListeners = Maps.newHashMap();
    private float scopeScale;

    public Gui(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.itemRenderer = minecraft.getItemRenderer();
        this.debugScreen = new DebugScreenOverlay(minecraft);
        this.spectatorGui = new SpectatorGui(minecraft);
        this.chat = new ChatComponent(minecraft);
        this.tabList = new PlayerTabOverlay(minecraft, this);
        this.bossOverlay = new BossHealthOverlay(minecraft);
        this.subtitleOverlay = new SubtitleOverlay(minecraft);
        for (ChatType chatType : ChatType.values()) {
            this.chatListeners.put(chatType, Lists.newArrayList());
        }
        NarratorChatListener narratorChatListener = NarratorChatListener.INSTANCE;
        this.chatListeners.get((Object)ChatType.CHAT).add(new StandardChatListener(minecraft));
        this.chatListeners.get((Object)ChatType.CHAT).add(narratorChatListener);
        this.chatListeners.get((Object)ChatType.SYSTEM).add(new StandardChatListener(minecraft));
        this.chatListeners.get((Object)ChatType.SYSTEM).add(narratorChatListener);
        this.chatListeners.get((Object)ChatType.GAME_INFO).add(new OverlayChatListener(minecraft));
        this.resetTitleTimes();
    }

    public void resetTitleTimes() {
        this.titleFadeInTime = 10;
        this.titleStayTime = 70;
        this.titleFadeOutTime = 20;
    }

    public void render(PoseStack poseStack, float f) {
        int n;
        float f2;
        this.screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        this.screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
        Font font = this.getFont();
        RenderSystem.enableBlend();
        if (Minecraft.useFancyGraphics()) {
            this.renderVignette(this.minecraft.getCameraEntity());
        } else {
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.defaultBlendFunc();
        }
        float f3 = this.minecraft.getDeltaFrameTime();
        this.scopeScale = Mth.lerp(0.5f * f3, this.scopeScale, 1.125f);
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            if (this.minecraft.player.isScoping()) {
                this.renderSpyglassOverlay(this.scopeScale);
            } else {
                this.scopeScale = 0.5f;
                ItemStack itemStack = this.minecraft.player.getInventory().getArmor(3);
                if (itemStack.is(Blocks.CARVED_PUMPKIN.asItem())) {
                    this.renderTextureOverlay(PUMPKIN_BLUR_LOCATION, 1.0f);
                }
            }
        }
        if (this.minecraft.player.getTicksFrozen() > 0) {
            this.renderTextureOverlay(POWDER_SNOW_OUTLINE_LOCATION, this.minecraft.player.getPercentFrozen());
        }
        if ((f2 = Mth.lerp(f, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime)) > 0.0f && !this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
            this.renderPortalOverlay(f2);
        }
        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            this.spectatorGui.renderHotbar(poseStack);
        } else if (!this.minecraft.options.hideGui) {
            this.renderHotbar(f, poseStack);
        }
        if (!this.minecraft.options.hideGui) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
            RenderSystem.enableBlend();
            this.renderCrosshair(poseStack);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.defaultBlendFunc();
            this.minecraft.getProfiler().push("bossHealth");
            this.bossOverlay.render(poseStack);
            this.minecraft.getProfiler().pop();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
            if (this.minecraft.gameMode.canHurtPlayer()) {
                this.renderPlayerHealth(poseStack);
            }
            this.renderVehicleHealth(poseStack);
            RenderSystem.disableBlend();
            int n2 = this.screenWidth / 2 - 91;
            if (this.minecraft.player.isRidingJumpable()) {
                this.renderJumpMeter(poseStack, n2);
            } else if (this.minecraft.gameMode.hasExperience()) {
                this.renderExperienceBar(poseStack, n2);
            }
            if (this.minecraft.options.heldItemTooltips && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                this.renderSelectedItemName(poseStack);
            } else if (this.minecraft.player.isSpectator()) {
                this.spectatorGui.renderTooltip(poseStack);
            }
        }
        if (this.minecraft.player.getSleepTimer() > 0) {
            this.minecraft.getProfiler().push("sleep");
            RenderSystem.disableDepthTest();
            float f4 = this.minecraft.player.getSleepTimer();
            float f5 = f4 / 100.0f;
            if (f5 > 1.0f) {
                f5 = 1.0f - (f4 - 100.0f) / 10.0f;
            }
            n = (int)(220.0f * f5) << 24 | 0x101020;
            Gui.fill(poseStack, 0, 0, this.screenWidth, this.screenHeight, n);
            RenderSystem.enableDepthTest();
            this.minecraft.getProfiler().pop();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        if (this.minecraft.isDemo()) {
            this.renderDemoOverlay(poseStack);
        }
        this.renderEffects(poseStack);
        if (this.minecraft.options.renderDebug) {
            this.debugScreen.render(poseStack);
        }
        if (!this.minecraft.options.hideGui) {
            Objective objective;
            int n3;
            int n4;
            if (this.overlayMessageString != null && this.overlayMessageTime > 0) {
                this.minecraft.getProfiler().push("overlayMessage");
                float f6 = (float)this.overlayMessageTime - f;
                int n5 = (int)(f6 * 255.0f / 20.0f);
                if (n5 > 255) {
                    n5 = 255;
                }
                if (n5 > 8) {
                    poseStack.pushPose();
                    poseStack.translate(this.screenWidth / 2, this.screenHeight - 68, 0.0);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    n = 0xFFFFFF;
                    if (this.animateOverlayMessageColor) {
                        n = Mth.hsvToRgb(f6 / 50.0f, 0.7f, 0.6f) & 0xFFFFFF;
                    }
                    n4 = n5 << 24 & 0xFF000000;
                    n3 = font.width(this.overlayMessageString);
                    this.drawBackdrop(poseStack, font, -4, n3, 0xFFFFFF | n4);
                    font.draw(poseStack, this.overlayMessageString, (float)(-n3 / 2), -4.0f, n | n4);
                    RenderSystem.disableBlend();
                    poseStack.popPose();
                }
                this.minecraft.getProfiler().pop();
            }
            if (this.title != null && this.titleTime > 0) {
                this.minecraft.getProfiler().push("titleAndSubtitle");
                float f7 = (float)this.titleTime - f;
                int n6 = 255;
                if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
                    float f8 = (float)(this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - f7;
                    n6 = (int)(f8 * 255.0f / (float)this.titleFadeInTime);
                }
                if (this.titleTime <= this.titleFadeOutTime) {
                    n6 = (int)(f7 * 255.0f / (float)this.titleFadeOutTime);
                }
                if ((n6 = Mth.clamp(n6, 0, 255)) > 8) {
                    poseStack.pushPose();
                    poseStack.translate(this.screenWidth / 2, this.screenHeight / 2, 0.0);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    poseStack.pushPose();
                    poseStack.scale(4.0f, 4.0f, 4.0f);
                    int n7 = n6 << 24 & 0xFF000000;
                    n4 = font.width(this.title);
                    this.drawBackdrop(poseStack, font, -10, n4, 0xFFFFFF | n7);
                    font.drawShadow(poseStack, this.title, (float)(-n4 / 2), -10.0f, 0xFFFFFF | n7);
                    poseStack.popPose();
                    if (this.subtitle != null) {
                        poseStack.pushPose();
                        poseStack.scale(2.0f, 2.0f, 2.0f);
                        n3 = font.width(this.subtitle);
                        this.drawBackdrop(poseStack, font, 5, n3, 0xFFFFFF | n7);
                        font.drawShadow(poseStack, this.subtitle, (float)(-n3 / 2), 5.0f, 0xFFFFFF | n7);
                        poseStack.popPose();
                    }
                    RenderSystem.disableBlend();
                    poseStack.popPose();
                }
                this.minecraft.getProfiler().pop();
            }
            this.subtitleOverlay.render(poseStack);
            Scoreboard scoreboard = this.minecraft.level.getScoreboard();
            Objective objective2 = null;
            PlayerTeam playerTeam = scoreboard.getPlayersTeam(this.minecraft.player.getScoreboardName());
            if (playerTeam != null && (n4 = playerTeam.getColor().getId()) >= 0) {
                objective2 = scoreboard.getDisplayObjective(3 + n4);
            }
            Objective objective3 = objective = objective2 != null ? objective2 : scoreboard.getDisplayObjective(1);
            if (objective != null) {
                this.displayScoreboardSidebar(poseStack, objective);
            }
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            poseStack.pushPose();
            poseStack.translate(0.0, this.screenHeight - 48, 0.0);
            this.minecraft.getProfiler().push("chat");
            this.chat.render(poseStack, this.tickCount);
            this.minecraft.getProfiler().pop();
            poseStack.popPose();
            objective = scoreboard.getDisplayObjective(0);
            if (this.minecraft.options.keyPlayerList.isDown() && (!this.minecraft.isLocalServer() || this.minecraft.player.connection.getOnlinePlayers().size() > 1 || objective != null)) {
                this.tabList.setVisible(true);
                this.tabList.render(poseStack, this.screenWidth, scoreboard, objective);
            } else {
                this.tabList.setVisible(false);
            }
            this.renderSavingIndicator(poseStack);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawBackdrop(PoseStack poseStack, Font font, int n, int n2, int n3) {
        int n4 = this.minecraft.options.getBackgroundColor(0.0f);
        if (n4 != 0) {
            int n5 = -n2 / 2;
            Gui.fill(poseStack, n5 - 2, n - 2, n5 + n2 + 2, n + font.lineHeight + 2, FastColor.ARGB32.multiply(n4, n3));
        }
    }

    private void renderCrosshair(PoseStack poseStack) {
        Options options = this.minecraft.options;
        if (!options.getCameraType().isFirstPerson()) {
            return;
        }
        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR && !this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
            return;
        }
        if (options.renderDebug && !options.hideGui && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo) {
            Camera camera = this.minecraft.gameRenderer.getMainCamera();
            PoseStack poseStack2 = RenderSystem.getModelViewStack();
            poseStack2.pushPose();
            poseStack2.translate(this.screenWidth / 2, this.screenHeight / 2, this.getBlitOffset());
            poseStack2.mulPose(Vector3f.XN.rotationDegrees(camera.getXRot()));
            poseStack2.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot()));
            poseStack2.scale(-1.0f, -1.0f, -1.0f);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.renderCrosshair(10);
            poseStack2.popPose();
            RenderSystem.applyModelViewMatrix();
        } else {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            int n = 15;
            this.blit(poseStack, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
            if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                float f = this.minecraft.player.getAttackStrengthScale(0.0f);
                boolean bl = false;
                if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= 1.0f) {
                    bl = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0f;
                    bl &= this.minecraft.crosshairPickEntity.isAlive();
                }
                int n2 = this.screenHeight / 2 - 7 + 16;
                int n3 = this.screenWidth / 2 - 8;
                if (bl) {
                    this.blit(poseStack, n3, n2, 68, 94, 16, 16);
                } else if (f < 1.0f) {
                    int n4 = (int)(f * 17.0f);
                    this.blit(poseStack, n3, n2, 36, 94, 16, 4);
                    this.blit(poseStack, n3, n2, 52, 94, n4, 4);
                }
            }
        }
    }

    private boolean canRenderCrosshairForSpectator(HitResult hitResult) {
        if (hitResult == null) {
            return false;
        }
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult)hitResult).getEntity() instanceof MenuProvider;
        }
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            ClientLevel clientLevel = this.minecraft.level;
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            return clientLevel.getBlockState(blockPos).getMenuProvider(clientLevel, blockPos) != null;
        }
        return false;
    }

    protected void renderEffects(PoseStack poseStack) {
        EffectRenderingInventoryScreen effectRenderingInventoryScreen;
        Screen screen;
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (collection.isEmpty() || (screen = this.minecraft.screen) instanceof EffectRenderingInventoryScreen && (effectRenderingInventoryScreen = (EffectRenderingInventoryScreen)screen).canSeeEffects()) {
            return;
        }
        RenderSystem.enableBlend();
        int n = 0;
        int n2 = 0;
        MobEffectTextureManager mobEffectTextureManager = this.minecraft.getMobEffectTextures();
        ArrayList arrayList = Lists.newArrayListWithExpectedSize((int)collection.size());
        RenderSystem.setShaderTexture(0, AbstractContainerScreen.INVENTORY_LOCATION);
        for (MobEffectInstance mobEffectInstance : Ordering.natural().reverse().sortedCopy(collection)) {
            MobEffect mobEffect = mobEffectInstance.getEffect();
            if (!mobEffectInstance.showIcon()) continue;
            int n3 = this.screenWidth;
            int n4 = 1;
            if (this.minecraft.isDemo()) {
                n4 += 15;
            }
            if (mobEffect.isBeneficial()) {
                n3 -= 25 * ++n;
            } else {
                n3 -= 25 * ++n2;
                n4 += 26;
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            float f = 1.0f;
            if (mobEffectInstance.isAmbient()) {
                this.blit(poseStack, n3, n4, 165, 166, 24, 24);
            } else {
                this.blit(poseStack, n3, n4, 141, 166, 24, 24);
                if (mobEffectInstance.getDuration() <= 200) {
                    int n5 = 10 - mobEffectInstance.getDuration() / 20;
                    f = Mth.clamp((float)mobEffectInstance.getDuration() / 10.0f / 5.0f * 0.5f, 0.0f, 0.5f) + Mth.cos((float)mobEffectInstance.getDuration() * (float)Math.PI / 5.0f) * Mth.clamp((float)n5 / 10.0f * 0.25f, 0.0f, 0.25f);
                }
            }
            TextureAtlasSprite textureAtlasSprite = mobEffectTextureManager.get(mobEffect);
            int n6 = n3;
            int n7 = n4;
            float f2 = f;
            arrayList.add(() -> {
                RenderSystem.setShaderTexture(0, textureAtlasSprite.atlas().location());
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, f2);
                Gui.blit(poseStack, n6 + 3, n7 + 3, this.getBlitOffset(), 18, 18, textureAtlasSprite);
            });
        }
        arrayList.forEach(Runnable::run);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderHotbar(float f, PoseStack poseStack) {
        float f2;
        int n;
        int n2;
        int n3;
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        ItemStack itemStack = player.getOffhandItem();
        HumanoidArm humanoidArm = player.getMainArm().getOpposite();
        int n4 = this.screenWidth / 2;
        int n5 = this.getBlitOffset();
        int n6 = 182;
        int n7 = 91;
        this.setBlitOffset(-90);
        this.blit(poseStack, n4 - 91, this.screenHeight - 22, 0, 0, 182, 22);
        this.blit(poseStack, n4 - 91 - 1 + player.getInventory().selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
        if (!itemStack.isEmpty()) {
            if (humanoidArm == HumanoidArm.LEFT) {
                this.blit(poseStack, n4 - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
            } else {
                this.blit(poseStack, n4 + 91, this.screenHeight - 23, 53, 22, 29, 24);
            }
        }
        this.setBlitOffset(n5);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int n8 = 1;
        for (n3 = 0; n3 < 9; ++n3) {
            n2 = n4 - 90 + n3 * 20 + 2;
            n = this.screenHeight - 16 - 3;
            this.renderSlot(n2, n, f, player, player.getInventory().items.get(n3), n8++);
        }
        if (!itemStack.isEmpty()) {
            n3 = this.screenHeight - 16 - 3;
            if (humanoidArm == HumanoidArm.LEFT) {
                this.renderSlot(n4 - 91 - 26, n3, f, player, itemStack, n8++);
            } else {
                this.renderSlot(n4 + 91 + 10, n3, f, player, itemStack, n8++);
            }
        }
        if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.HOTBAR && (f2 = this.minecraft.player.getAttackStrengthScale(0.0f)) < 1.0f) {
            n2 = this.screenHeight - 20;
            n = n4 + 91 + 6;
            if (humanoidArm == HumanoidArm.RIGHT) {
                n = n4 - 91 - 22;
            }
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            int n9 = (int)(f2 * 19.0f);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            this.blit(poseStack, n, n2, 0, 94, 18, 18);
            this.blit(poseStack, n, n2 + 18 - n9, 18, 112 - n9, 18, n9);
        }
        RenderSystem.disableBlend();
    }

    public void renderJumpMeter(PoseStack poseStack, int n) {
        this.minecraft.getProfiler().push("jumpBar");
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        float f = this.minecraft.player.getJumpRidingScale();
        int n2 = 182;
        int n3 = (int)(f * 183.0f);
        int n4 = this.screenHeight - 32 + 3;
        this.blit(poseStack, n, n4, 0, 84, 182, 5);
        if (n3 > 0) {
            this.blit(poseStack, n, n4, 0, 89, n3, 5);
        }
        this.minecraft.getProfiler().pop();
    }

    public void renderExperienceBar(PoseStack poseStack, int n) {
        int n2;
        int n3;
        this.minecraft.getProfiler().push("expBar");
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        int n4 = this.minecraft.player.getXpNeededForNextLevel();
        if (n4 > 0) {
            int n5 = 182;
            n3 = (int)(this.minecraft.player.experienceProgress * 183.0f);
            n2 = this.screenHeight - 32 + 3;
            this.blit(poseStack, n, n2, 0, 64, 182, 5);
            if (n3 > 0) {
                this.blit(poseStack, n, n2, 0, 69, n3, 5);
            }
        }
        this.minecraft.getProfiler().pop();
        if (this.minecraft.player.experienceLevel > 0) {
            this.minecraft.getProfiler().push("expLevel");
            String string = "" + this.minecraft.player.experienceLevel;
            n3 = (this.screenWidth - this.getFont().width(string)) / 2;
            n2 = this.screenHeight - 31 - 4;
            this.getFont().draw(poseStack, string, (float)(n3 + 1), (float)n2, 0);
            this.getFont().draw(poseStack, string, (float)(n3 - 1), (float)n2, 0);
            this.getFont().draw(poseStack, string, (float)n3, (float)(n2 + 1), 0);
            this.getFont().draw(poseStack, string, (float)n3, (float)(n2 - 1), 0);
            this.getFont().draw(poseStack, string, (float)n3, (float)n2, 8453920);
            this.minecraft.getProfiler().pop();
        }
    }

    public void renderSelectedItemName(PoseStack poseStack) {
        this.minecraft.getProfiler().push("selectedItemName");
        if (this.toolHighlightTimer > 0 && !this.lastToolHighlight.isEmpty()) {
            int n;
            MutableComponent mutableComponent = new TextComponent("").append(this.lastToolHighlight.getHoverName()).withStyle(this.lastToolHighlight.getRarity().color);
            if (this.lastToolHighlight.hasCustomHoverName()) {
                mutableComponent.withStyle(ChatFormatting.ITALIC);
            }
            int n2 = this.getFont().width(mutableComponent);
            int n3 = (this.screenWidth - n2) / 2;
            int n4 = this.screenHeight - 59;
            if (!this.minecraft.gameMode.canHurtPlayer()) {
                n4 += 14;
            }
            if ((n = (int)((float)this.toolHighlightTimer * 256.0f / 10.0f)) > 255) {
                n = 255;
            }
            if (n > 0) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Gui.fill(poseStack, n3 - 2, n4 - 2, n3 + n2 + 2, n4 + this.getFont().lineHeight + 2, this.minecraft.options.getBackgroundColor(0));
                this.getFont().drawShadow(poseStack, mutableComponent, (float)n3, (float)n4, 0xFFFFFF + (n << 24));
                RenderSystem.disableBlend();
            }
        }
        this.minecraft.getProfiler().pop();
    }

    public void renderDemoOverlay(PoseStack poseStack) {
        this.minecraft.getProfiler().push("demo");
        Component component = this.minecraft.level.getGameTime() >= 120500L ? DEMO_EXPIRED_TEXT : new TranslatableComponent("demo.remainingTime", StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime())));
        int n = this.getFont().width(component);
        this.getFont().drawShadow(poseStack, component, (float)(this.screenWidth - n - 10), 5.0f, 0xFFFFFF);
        this.minecraft.getProfiler().pop();
    }

    private void displayScoreboardSidebar(PoseStack poseStack, Objective objective) {
        int n;
        Scoreboard scoreboard = objective.getScoreboard();
        List<Object> list = scoreboard.getPlayerScores(objective);
        List list2 = list.stream().filter(score -> score.getOwner() != null && !score.getOwner().startsWith("#")).collect(Collectors.toList());
        list = list2.size() > 15 ? Lists.newArrayList((Iterable)Iterables.skip(list2, (int)(list.size() - 15))) : list2;
        ArrayList arrayList = Lists.newArrayListWithCapacity((int)list.size());
        Component component = objective.getDisplayName();
        int n2 = n = this.getFont().width(component);
        int n3 = this.getFont().width(SPACER);
        for (Score score2 : list) {
            PlayerTeam playerTeam = scoreboard.getPlayersTeam(score2.getOwner());
            MutableComponent mutableComponent = PlayerTeam.formatNameForTeam(playerTeam, new TextComponent(score2.getOwner()));
            arrayList.add(Pair.of((Object)score2, (Object)mutableComponent));
            n2 = Math.max(n2, this.getFont().width(mutableComponent) + n3 + this.getFont().width(Integer.toString(score2.getScore())));
        }
        int n4 = list.size() * this.getFont().lineHeight;
        int n5 = this.screenHeight / 2 + n4 / 3;
        int n6 = 3;
        int n7 = this.screenWidth - n2 - 3;
        int n8 = 0;
        int n9 = this.minecraft.options.getBackgroundColor(0.3f);
        int n10 = this.minecraft.options.getBackgroundColor(0.4f);
        for (Pair pair : arrayList) {
            Score score3 = (Score)pair.getFirst();
            Component component2 = (Component)pair.getSecond();
            String string = "" + ChatFormatting.RED + score3.getScore();
            int n11 = n7;
            int n12 = n5 - ++n8 * this.getFont().lineHeight;
            int n13 = this.screenWidth - 3 + 2;
            Gui.fill(poseStack, n11 - 2, n12, n13, n12 + this.getFont().lineHeight, n9);
            this.getFont().draw(poseStack, component2, (float)n11, (float)n12, -1);
            this.getFont().draw(poseStack, string, (float)(n13 - this.getFont().width(string)), (float)n12, -1);
            if (n8 != list.size()) continue;
            Gui.fill(poseStack, n11 - 2, n12 - this.getFont().lineHeight - 1, n13, n12 - 1, n10);
            Gui.fill(poseStack, n11 - 2, n12 - 1, n13, n12, n9);
            this.getFont().draw(poseStack, component, (float)(n11 + n2 / 2 - n / 2), (float)(n12 - this.getFont().lineHeight), -1);
        }
    }

    private Player getCameraPlayer() {
        if (!(this.minecraft.getCameraEntity() instanceof Player)) {
            return null;
        }
        return (Player)this.minecraft.getCameraEntity();
    }

    private LivingEntity getPlayerVehicleWithHealth() {
        Player player = this.getCameraPlayer();
        if (player != null) {
            Entity entity = player.getVehicle();
            if (entity == null) {
                return null;
            }
            if (entity instanceof LivingEntity) {
                return (LivingEntity)entity;
            }
        }
        return null;
    }

    private int getVehicleMaxHearts(LivingEntity livingEntity) {
        if (livingEntity == null || !livingEntity.showVehicleHealth()) {
            return 0;
        }
        float f = livingEntity.getMaxHealth();
        int n = (int)(f + 0.5f) / 2;
        if (n > 30) {
            n = 30;
        }
        return n;
    }

    private int getVisibleVehicleHeartRows(int n) {
        return (int)Math.ceil((double)n / 10.0);
    }

    private void renderPlayerHealth(PoseStack poseStack) {
        int n;
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }
        int n7 = Mth.ceil(player.getHealth());
        boolean bl = this.healthBlinkTime > (long)this.tickCount && (this.healthBlinkTime - (long)this.tickCount) / 3L % 2L == 1L;
        long l = Util.getMillis();
        if (n7 < this.lastHealth && player.invulnerableTime > 0) {
            this.lastHealthTime = l;
            this.healthBlinkTime = this.tickCount + 20;
        } else if (n7 > this.lastHealth && player.invulnerableTime > 0) {
            this.lastHealthTime = l;
            this.healthBlinkTime = this.tickCount + 10;
        }
        if (l - this.lastHealthTime > 1000L) {
            this.lastHealth = n7;
            this.displayHealth = n7;
            this.lastHealthTime = l;
        }
        this.lastHealth = n7;
        int n8 = this.displayHealth;
        this.random.setSeed(this.tickCount * 312871);
        FoodData foodData = player.getFoodData();
        int n9 = foodData.getFoodLevel();
        int n10 = this.screenWidth / 2 - 91;
        int n11 = this.screenWidth / 2 + 91;
        int n12 = this.screenHeight - 39;
        float f = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(n8, n7));
        int n13 = Mth.ceil(player.getAbsorptionAmount());
        int n14 = Mth.ceil((f + (float)n13) / 2.0f / 10.0f);
        int n15 = Math.max(10 - (n14 - 2), 3);
        int n16 = n12 - (n14 - 1) * n15 - 10;
        int n17 = n12 - 10;
        int n18 = player.getArmorValue();
        int n19 = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            n19 = this.tickCount % Mth.ceil(f + 5.0f);
        }
        this.minecraft.getProfiler().push("armor");
        for (int i = 0; i < 10; ++i) {
            if (n18 <= 0) continue;
            n6 = n10 + i * 8;
            if (i * 2 + 1 < n18) {
                this.blit(poseStack, n6, n16, 34, 9, 9, 9);
            }
            if (i * 2 + 1 == n18) {
                this.blit(poseStack, n6, n16, 25, 9, 9, 9);
            }
            if (i * 2 + 1 <= n18) continue;
            this.blit(poseStack, n6, n16, 16, 9, 9, 9);
        }
        this.minecraft.getProfiler().popPush("health");
        this.renderHearts(poseStack, player, n10, n12, n15, n19, f, n7, n8, n13, bl);
        LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
        n6 = this.getVehicleMaxHearts(livingEntity);
        if (n6 == 0) {
            this.minecraft.getProfiler().popPush("food");
            for (n5 = 0; n5 < 10; ++n5) {
                n4 = n12;
                n3 = 16;
                n2 = 0;
                if (player.hasEffect(MobEffects.HUNGER)) {
                    n3 += 36;
                    n2 = 13;
                }
                if (player.getFoodData().getSaturationLevel() <= 0.0f && this.tickCount % (n9 * 3 + 1) == 0) {
                    n4 += this.random.nextInt(3) - 1;
                }
                n = n11 - n5 * 8 - 9;
                this.blit(poseStack, n, n4, 16 + n2 * 9, 27, 9, 9);
                if (n5 * 2 + 1 < n9) {
                    this.blit(poseStack, n, n4, n3 + 36, 27, 9, 9);
                }
                if (n5 * 2 + 1 != n9) continue;
                this.blit(poseStack, n, n4, n3 + 45, 27, 9, 9);
            }
            n17 -= 10;
        }
        this.minecraft.getProfiler().popPush("air");
        n5 = player.getMaxAirSupply();
        n4 = Math.min(player.getAirSupply(), n5);
        if (player.isEyeInFluid(FluidTags.WATER) || n4 < n5) {
            n3 = this.getVisibleVehicleHeartRows(n6) - 1;
            n17 -= n3 * 10;
            n2 = Mth.ceil((double)(n4 - 2) * 10.0 / (double)n5);
            n = Mth.ceil((double)n4 * 10.0 / (double)n5) - n2;
            for (int i = 0; i < n2 + n; ++i) {
                if (i < n2) {
                    this.blit(poseStack, n11 - i * 8 - 9, n17, 16, 18, 9, 9);
                    continue;
                }
                this.blit(poseStack, n11 - i * 8 - 9, n17, 25, 18, 9, 9);
            }
        }
        this.minecraft.getProfiler().pop();
    }

    private void renderHearts(PoseStack poseStack, Player player, int n, int n2, int n3, int n4, float f, int n5, int n6, int n7, boolean bl) {
        HeartType heartType = HeartType.forPlayer(player);
        int n8 = 9 * (player.level.getLevelData().isHardcore() ? 5 : 0);
        int n9 = Mth.ceil((double)f / 2.0);
        int n10 = Mth.ceil((double)n7 / 2.0);
        int n11 = n9 * 2;
        for (int i = n9 + n10 - 1; i >= 0; --i) {
            int n12;
            boolean bl2;
            int n13 = i / 10;
            int n14 = i % 10;
            int n15 = n + n14 * 8;
            int n16 = n2 - n13 * n3;
            if (n5 + n7 <= 4) {
                n16 += this.random.nextInt(2);
            }
            if (i < n9 && i == n4) {
                n16 -= 2;
            }
            this.renderHeart(poseStack, HeartType.CONTAINER, n15, n16, n8, bl, false);
            int n17 = i * 2;
            boolean bl3 = bl2 = i >= n9;
            if (bl2 && (n12 = n17 - n11) < n7) {
                boolean bl4 = n12 + 1 == n7;
                this.renderHeart(poseStack, heartType == HeartType.WITHERED ? heartType : HeartType.ABSORBING, n15, n16, n8, false, bl4);
            }
            if (bl && n17 < n6) {
                n12 = n17 + 1 == n6 ? 1 : 0;
                this.renderHeart(poseStack, heartType, n15, n16, n8, true, n12 != 0);
            }
            if (n17 >= n5) continue;
            n12 = n17 + 1 == n5 ? 1 : 0;
            this.renderHeart(poseStack, heartType, n15, n16, n8, false, n12 != 0);
        }
    }

    private void renderHeart(PoseStack poseStack, HeartType heartType, int n, int n2, int n3, boolean bl, boolean bl2) {
        this.blit(poseStack, n, n2, heartType.getX(bl2, bl), n3, 9, 9);
    }

    private void renderVehicleHealth(PoseStack poseStack) {
        LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
        if (livingEntity == null) {
            return;
        }
        int n = this.getVehicleMaxHearts(livingEntity);
        if (n == 0) {
            return;
        }
        int n2 = (int)Math.ceil(livingEntity.getHealth());
        this.minecraft.getProfiler().popPush("mountHealth");
        int n3 = this.screenHeight - 39;
        int n4 = this.screenWidth / 2 + 91;
        int n5 = n3;
        int n6 = 0;
        boolean bl = false;
        while (n > 0) {
            int n7 = Math.min(n, 10);
            n -= n7;
            for (int i = 0; i < n7; ++i) {
                int n8 = 52;
                int n9 = 0;
                int n10 = n4 - i * 8 - 9;
                this.blit(poseStack, n10, n5, 52 + n9 * 9, 9, 9, 9);
                if (i * 2 + 1 + n6 < n2) {
                    this.blit(poseStack, n10, n5, 88, 9, 9, 9);
                }
                if (i * 2 + 1 + n6 != n2) continue;
                this.blit(poseStack, n10, n5, 97, 9, 9, 9);
            }
            n5 -= 10;
            n6 += 20;
        }
    }

    private void renderTextureOverlay(ResourceLocation resourceLocation, float f) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, f);
        RenderSystem.setShaderTexture(0, resourceLocation);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(0.0, this.screenHeight, -90.0).uv(0.0f, 1.0f).endVertex();
        bufferBuilder.vertex(this.screenWidth, this.screenHeight, -90.0).uv(1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(this.screenWidth, 0.0, -90.0).uv(1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(0.0, 0.0, -90.0).uv(0.0f, 0.0f).endVertex();
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderSpyglassOverlay(float f) {
        float f2;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SPYGLASS_SCOPE_LOCATION);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        float f3 = f2 = (float)Math.min(this.screenWidth, this.screenHeight);
        float f4 = Math.min((float)this.screenWidth / f2, (float)this.screenHeight / f3) * f;
        float f5 = f2 * f4;
        float f6 = f3 * f4;
        float f7 = ((float)this.screenWidth - f5) / 2.0f;
        float f8 = ((float)this.screenHeight - f6) / 2.0f;
        float f9 = f7 + f5;
        float f10 = f8 + f6;
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(f7, f10, -90.0).uv(0.0f, 1.0f).endVertex();
        bufferBuilder.vertex(f9, f10, -90.0).uv(1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(f9, f8, -90.0).uv(1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(f7, f8, -90.0).uv(0.0f, 0.0f).endVertex();
        tesselator.end();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableTexture();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(0.0, this.screenHeight, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(this.screenWidth, this.screenHeight, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(this.screenWidth, f10, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(0.0, f10, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(0.0, f8, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(this.screenWidth, f8, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(this.screenWidth, 0.0, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(0.0, 0.0, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(0.0, f10, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(f7, f10, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(f7, f8, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(0.0, f8, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(f9, f10, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(this.screenWidth, f10, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(this.screenWidth, f8, -90.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(f9, f8, -90.0).color(0, 0, 0, 255).endVertex();
        tesselator.end();
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void updateVignetteBrightness(Entity entity) {
        if (entity == null) {
            return;
        }
        float f = Mth.clamp(1.0f - entity.getBrightness(), 0.0f, 1.0f);
        this.vignetteBrightness += (f - this.vignetteBrightness) * 0.01f;
    }

    private void renderVignette(Entity entity) {
        WorldBorder worldBorder = this.minecraft.level.getWorldBorder();
        float f = (float)worldBorder.getDistanceToBorder(entity);
        double d = Math.min(worldBorder.getLerpSpeed() * (double)worldBorder.getWarningTime() * 1000.0, Math.abs(worldBorder.getLerpTarget() - worldBorder.getSize()));
        double d2 = Math.max((double)worldBorder.getWarningBlocks(), d);
        f = (double)f < d2 ? 1.0f - (float)((double)f / d2) : 0.0f;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        if (f > 0.0f) {
            f = Mth.clamp(f, 0.0f, 1.0f);
            RenderSystem.setShaderColor(0.0f, f, f, 1.0f);
        } else {
            float f2 = this.vignetteBrightness;
            f2 = Mth.clamp(f2, 0.0f, 1.0f);
            RenderSystem.setShaderColor(f2, f2, f2, 1.0f);
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, VIGNETTE_LOCATION);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(0.0, this.screenHeight, -90.0).uv(0.0f, 1.0f).endVertex();
        bufferBuilder.vertex(this.screenWidth, this.screenHeight, -90.0).uv(1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(this.screenWidth, 0.0, -90.0).uv(1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(0.0, 0.0, -90.0).uv(0.0f, 0.0f).endVertex();
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.defaultBlendFunc();
    }

    private void renderPortalOverlay(float f) {
        if (f < 1.0f) {
            f *= f;
            f *= f;
            f = f * 0.8f + 0.2f;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, f);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        TextureAtlasSprite textureAtlasSprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        float f2 = textureAtlasSprite.getU0();
        float f3 = textureAtlasSprite.getV0();
        float f4 = textureAtlasSprite.getU1();
        float f5 = textureAtlasSprite.getV1();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(0.0, this.screenHeight, -90.0).uv(f2, f5).endVertex();
        bufferBuilder.vertex(this.screenWidth, this.screenHeight, -90.0).uv(f4, f5).endVertex();
        bufferBuilder.vertex(this.screenWidth, 0.0, -90.0).uv(f4, f3).endVertex();
        bufferBuilder.vertex(0.0, 0.0, -90.0).uv(f2, f3).endVertex();
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderSlot(int n, int n2, float f, Player player, ItemStack itemStack, int n3) {
        if (itemStack.isEmpty()) {
            return;
        }
        PoseStack poseStack = RenderSystem.getModelViewStack();
        float f2 = (float)itemStack.getPopTime() - f;
        if (f2 > 0.0f) {
            float f3 = 1.0f + f2 / 5.0f;
            poseStack.pushPose();
            poseStack.translate(n + 8, n2 + 12, 0.0);
            poseStack.scale(1.0f / f3, (f3 + 1.0f) / 2.0f, 1.0f);
            poseStack.translate(-(n + 8), -(n2 + 12), 0.0);
            RenderSystem.applyModelViewMatrix();
        }
        this.itemRenderer.renderAndDecorateItem(player, itemStack, n, n2, n3);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        if (f2 > 0.0f) {
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
        this.itemRenderer.renderGuiItemDecorations(this.minecraft.font, itemStack, n, n2);
    }

    public void tick(boolean bl) {
        this.tickAutosaveIndicator();
        if (!bl) {
            this.tick();
        }
    }

    private void tick() {
        if (this.overlayMessageTime > 0) {
            --this.overlayMessageTime;
        }
        if (this.titleTime > 0) {
            --this.titleTime;
            if (this.titleTime <= 0) {
                this.title = null;
                this.subtitle = null;
            }
        }
        ++this.tickCount;
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null) {
            this.updateVignetteBrightness(entity);
        }
        if (this.minecraft.player != null) {
            ItemStack itemStack = this.minecraft.player.getInventory().getSelected();
            if (itemStack.isEmpty()) {
                this.toolHighlightTimer = 0;
            } else if (this.lastToolHighlight.isEmpty() || !itemStack.is(this.lastToolHighlight.getItem()) || !itemStack.getHoverName().equals(this.lastToolHighlight.getHoverName())) {
                this.toolHighlightTimer = 40;
            } else if (this.toolHighlightTimer > 0) {
                --this.toolHighlightTimer;
            }
            this.lastToolHighlight = itemStack;
        }
    }

    private void tickAutosaveIndicator() {
        IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
        boolean bl = integratedServer != null && integratedServer.isCurrentlySaving();
        this.lastAutosaveIndicatorValue = this.autosaveIndicatorValue;
        this.autosaveIndicatorValue = Mth.lerp(0.2f, this.autosaveIndicatorValue, bl ? 1.0f : 0.0f);
    }

    public void setNowPlaying(Component component) {
        this.setOverlayMessage(new TranslatableComponent("record.nowPlaying", component), true);
    }

    public void setOverlayMessage(Component component, boolean bl) {
        this.overlayMessageString = component;
        this.overlayMessageTime = 60;
        this.animateOverlayMessageColor = bl;
    }

    public void setTimes(int n, int n2, int n3) {
        if (n >= 0) {
            this.titleFadeInTime = n;
        }
        if (n2 >= 0) {
            this.titleStayTime = n2;
        }
        if (n3 >= 0) {
            this.titleFadeOutTime = n3;
        }
        if (this.titleTime > 0) {
            this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
        }
    }

    public void setSubtitle(Component component) {
        this.subtitle = component;
    }

    public void setTitle(Component component) {
        this.title = component;
        this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
    }

    public void clear() {
        this.title = null;
        this.subtitle = null;
        this.titleTime = 0;
    }

    public UUID guessChatUUID(Component component) {
        String string = StringDecomposer.getPlainText(component);
        String string2 = StringUtils.substringBetween((String)string, (String)"<", (String)">");
        if (string2 == null) {
            return Util.NIL_UUID;
        }
        return this.minecraft.getPlayerSocialManager().getDiscoveredUUID(string2);
    }

    public void handleChat(ChatType chatType, Component component, UUID uUID) {
        if (this.minecraft.isBlocked(uUID)) {
            return;
        }
        if (this.minecraft.options.hideMatchedNames && this.minecraft.isBlocked(this.guessChatUUID(component))) {
            return;
        }
        for (ChatListener chatListener : this.chatListeners.get((Object)chatType)) {
            chatListener.handle(chatType, component, uUID);
        }
    }

    public ChatComponent getChat() {
        return this.chat;
    }

    public int getGuiTicks() {
        return this.tickCount;
    }

    public Font getFont() {
        return this.minecraft.font;
    }

    public SpectatorGui getSpectatorGui() {
        return this.spectatorGui;
    }

    public PlayerTabOverlay getTabList() {
        return this.tabList;
    }

    public void onDisconnected() {
        this.tabList.reset();
        this.bossOverlay.reset();
        this.minecraft.getToasts().clear();
        this.minecraft.options.renderDebug = false;
        this.chat.clearMessages(true);
    }

    public BossHealthOverlay getBossOverlay() {
        return this.bossOverlay;
    }

    public void clearCache() {
        this.debugScreen.clearChunkCache();
    }

    private void renderSavingIndicator(PoseStack poseStack) {
        int n;
        if (this.minecraft.options.showAutosaveIndicator && (this.autosaveIndicatorValue > 0.0f || this.lastAutosaveIndicatorValue > 0.0f) && (n = Mth.floor(255.0f * Mth.clamp(Mth.lerp(this.minecraft.getFrameTime(), this.lastAutosaveIndicatorValue, this.autosaveIndicatorValue), 0.0f, 1.0f))) > 8) {
            Font font = this.getFont();
            int n2 = font.width(SAVING_TEXT);
            int n3 = 0xFFFFFF | n << 24 & 0xFF000000;
            font.drawShadow(poseStack, SAVING_TEXT, (float)(this.screenWidth - n2 - 10), (float)(this.screenHeight - 15), n3);
        }
    }

    static enum HeartType {
        CONTAINER(0, false),
        NORMAL(2, true),
        POISIONED(4, true),
        WITHERED(6, true),
        ABSORBING(8, false),
        FROZEN(9, false);

        private final int index;
        private final boolean canBlink;

        private HeartType(int n2, boolean bl) {
            this.index = n2;
            this.canBlink = bl;
        }

        public int getX(boolean bl, boolean bl2) {
            int n;
            if (this == CONTAINER) {
                n = bl2 ? 1 : 0;
            } else {
                int n2 = bl ? 1 : 0;
                int n3 = this.canBlink && bl2 ? 2 : 0;
                n = n2 + n3;
            }
            return 16 + (this.index * 2 + n) * 9;
        }

        static HeartType forPlayer(Player player) {
            HeartType heartType = player.hasEffect(MobEffects.POISON) ? POISIONED : (player.hasEffect(MobEffects.WITHER) ? WITHERED : (player.isFullyFrozen() ? FROZEN : NORMAL));
            return heartType;
        }
    }
}

