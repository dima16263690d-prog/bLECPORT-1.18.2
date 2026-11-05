/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  javax.annotation.Nullable
 */
package net.minecraft.client;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.blaze3d.platform.InputConstants;
import java.text.MessageFormat;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.Option;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class KeyboardHandler {
    public static final int DEBUG_CRASH_TIME = 10000;
    private final Minecraft minecraft;
    private boolean sendRepeatsToGui;
    private final ClipboardManager clipboardManager = new ClipboardManager();
    private long debugCrashKeyTime = -1L;
    private long debugCrashKeyReportedTime = -1L;
    private long debugCrashKeyReportedCount = -1L;
    private boolean handledDebugKey;

    public KeyboardHandler(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    private boolean handleChunkDebugKeys(int n) {
        switch (n) {
            case 69: {
                this.minecraft.chunkPath = !this.minecraft.chunkPath;
                this.debugFeedback("ChunkPath: {0}", this.minecraft.chunkPath ? "shown" : "hidden");
                return true;
            }
            case 76: {
                this.minecraft.smartCull = !this.minecraft.smartCull;
                this.debugFeedback("SmartCull: {0}", this.minecraft.smartCull ? "enabled" : "disabled");
                return true;
            }
            case 85: {
                if (Screen.hasShiftDown()) {
                    this.minecraft.levelRenderer.killFrustum();
                    this.debugFeedback("Killed frustum", new Object[0]);
                } else {
                    this.minecraft.levelRenderer.captureFrustum();
                    this.debugFeedback("Captured frustum", new Object[0]);
                }
                return true;
            }
            case 86: {
                this.minecraft.chunkVisibility = !this.minecraft.chunkVisibility;
                this.debugFeedback("ChunkVisibility: {0}", this.minecraft.chunkVisibility ? "enabled" : "disabled");
                return true;
            }
            case 87: {
                this.minecraft.wireframe = !this.minecraft.wireframe;
                this.debugFeedback("WireFrame: {0}", this.minecraft.wireframe ? "enabled" : "disabled");
                return true;
            }
        }
        return false;
    }

    private void debugComponent(ChatFormatting chatFormatting, Component component) {
        this.minecraft.gui.getChat().addMessage(new TextComponent("").append(new TranslatableComponent("debug.prefix").withStyle(chatFormatting, ChatFormatting.BOLD)).append(" ").append(component));
    }

    private void debugFeedbackComponent(Component component) {
        this.debugComponent(ChatFormatting.YELLOW, component);
    }

    private void debugFeedbackTranslated(String string, Object ... objectArray) {
        this.debugFeedbackComponent(new TranslatableComponent(string, objectArray));
    }

    private void debugWarningTranslated(String string, Object ... objectArray) {
        this.debugComponent(ChatFormatting.RED, new TranslatableComponent(string, objectArray));
    }

    private void debugFeedback(String string, Object ... objectArray) {
        this.debugFeedbackComponent(new TextComponent(MessageFormat.format(string, objectArray)));
    }

    private boolean handleDebugKeys(int n) {
        if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
            return true;
        }
        switch (n) {
            case 65: {
                this.minecraft.levelRenderer.allChanged();
                this.debugFeedbackTranslated("debug.reload_chunks.message", new Object[0]);
                return true;
            }
            case 66: {
                boolean bl = !this.minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes();
                this.minecraft.getEntityRenderDispatcher().setRenderHitBoxes(bl);
                this.debugFeedbackTranslated(bl ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off", new Object[0]);
                return true;
            }
            case 68: {
                if (this.minecraft.gui != null) {
                    this.minecraft.gui.getChat().clearMessages(false);
                }
                return true;
            }
            case 70: {
                Option.RENDER_DISTANCE.set(this.minecraft.options, Mth.clamp((double)(this.minecraft.options.renderDistance + (Screen.hasShiftDown() ? -1 : 1)), Option.RENDER_DISTANCE.getMinValue(), Option.RENDER_DISTANCE.getMaxValue()));
                this.debugFeedbackTranslated("debug.cycle_renderdistance.message", this.minecraft.options.renderDistance);
                return true;
            }
            case 71: {
                boolean bl = this.minecraft.debugRenderer.switchRenderChunkborder();
                this.debugFeedbackTranslated(bl ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off", new Object[0]);
                return true;
            }
            case 72: {
                this.minecraft.options.advancedItemTooltips = !this.minecraft.options.advancedItemTooltips;
                this.debugFeedbackTranslated(this.minecraft.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off", new Object[0]);
                this.minecraft.options.save();
                return true;
            }
            case 73: {
                if (!this.minecraft.player.isReducedDebugInfo()) {
                    this.copyRecreateCommand(this.minecraft.player.hasPermissions(2), !Screen.hasShiftDown());
                }
                return true;
            }
            case 78: {
                if (!this.minecraft.player.hasPermissions(2)) {
                    this.debugFeedbackTranslated("debug.creative_spectator.error", new Object[0]);
                } else if (!this.minecraft.player.isSpectator()) {
                    this.minecraft.player.chat("/gamemode spectator");
                } else {
                    this.minecraft.player.chat("/gamemode " + ((GameType)((Object)MoreObjects.firstNonNull((Object)((Object)this.minecraft.gameMode.getPreviousPlayerMode()), (Object)((Object)GameType.CREATIVE)))).getName());
                }
                return true;
            }
            case 293: {
                if (!this.minecraft.player.hasPermissions(2)) {
                    this.debugFeedbackTranslated("debug.gamemodes.error", new Object[0]);
                } else {
                    this.minecraft.setScreen(new GameModeSwitcherScreen());
                }
                return true;
            }
            case 80: {
                this.minecraft.options.pauseOnLostFocus = !this.minecraft.options.pauseOnLostFocus;
                this.minecraft.options.save();
                this.debugFeedbackTranslated(this.minecraft.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off", new Object[0]);
                return true;
            }
            case 81: {
                this.debugFeedbackTranslated("debug.help.message", new Object[0]);
                ChatComponent chatComponent = this.minecraft.gui.getChat();
                chatComponent.addMessage(new TranslatableComponent("debug.reload_chunks.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.show_hitboxes.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.copy_location.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.clear_chat.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.cycle_renderdistance.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.chunk_boundaries.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.advanced_tooltips.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.inspect.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.profiling.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.creative_spectator.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.pause_focus.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.help.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.reload_resourcepacks.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.pause.help"));
                chatComponent.addMessage(new TranslatableComponent("debug.gamemodes.help"));
                return true;
            }
            case 84: {
                this.debugFeedbackTranslated("debug.reload_resourcepacks.message", new Object[0]);
                this.minecraft.reloadResourcePacks();
                return true;
            }
            case 76: {
                if (this.minecraft.debugClientMetricsStart(this::debugFeedbackComponent)) {
                    this.debugFeedbackTranslated("debug.profiling.start", 10);
                }
                return true;
            }
            case 67: {
                if (this.minecraft.player.isReducedDebugInfo()) {
                    return false;
                }
                ClientPacketListener clientPacketListener = this.minecraft.player.connection;
                if (clientPacketListener == null) {
                    return false;
                }
                this.debugFeedbackTranslated("debug.copy_location.message", new Object[0]);
                this.setClipboard(String.format(Locale.ROOT, "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f", this.minecraft.player.level.dimension().location(), this.minecraft.player.getX(), this.minecraft.player.getY(), this.minecraft.player.getZ(), Float.valueOf(this.minecraft.player.getYRot()), Float.valueOf(this.minecraft.player.getXRot())));
                return true;
            }
        }
        return false;
    }

    private void copyRecreateCommand(boolean bl, boolean bl2) {
        HitResult hitResult = this.minecraft.hitResult;
        if (hitResult == null) {
            return;
        }
        switch (hitResult.getType()) {
            case BLOCK: {
                BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
                BlockState blockState = this.minecraft.player.level.getBlockState(blockPos);
                if (bl) {
                    if (bl2) {
                        this.minecraft.player.connection.getDebugQueryHandler().queryBlockEntityTag(blockPos, compoundTag -> {
                            this.copyCreateBlockCommand(blockState, blockPos, (CompoundTag)compoundTag);
                            this.debugFeedbackTranslated("debug.inspect.server.block", new Object[0]);
                        });
                        break;
                    }
                    BlockEntity blockEntity = this.minecraft.player.level.getBlockEntity(blockPos);
                    CompoundTag compoundTag2 = blockEntity != null ? blockEntity.saveWithoutMetadata() : null;
                    this.copyCreateBlockCommand(blockState, blockPos, compoundTag2);
                    this.debugFeedbackTranslated("debug.inspect.client.block", new Object[0]);
                    break;
                }
                this.copyCreateBlockCommand(blockState, blockPos, null);
                this.debugFeedbackTranslated("debug.inspect.client.block", new Object[0]);
                break;
            }
            case ENTITY: {
                Entity entity = ((EntityHitResult)hitResult).getEntity();
                ResourceLocation resourceLocation = Registry.ENTITY_TYPE.getKey(entity.getType());
                if (bl) {
                    if (bl2) {
                        this.minecraft.player.connection.getDebugQueryHandler().queryEntityTag(entity.getId(), compoundTag -> {
                            this.copyCreateEntityCommand(resourceLocation, entity.position(), (CompoundTag)compoundTag);
                            this.debugFeedbackTranslated("debug.inspect.server.entity", new Object[0]);
                        });
                        break;
                    }
                    CompoundTag compoundTag3 = entity.saveWithoutId(new CompoundTag());
                    this.copyCreateEntityCommand(resourceLocation, entity.position(), compoundTag3);
                    this.debugFeedbackTranslated("debug.inspect.client.entity", new Object[0]);
                    break;
                }
                this.copyCreateEntityCommand(resourceLocation, entity.position(), null);
                this.debugFeedbackTranslated("debug.inspect.client.entity", new Object[0]);
                break;
            }
        }
    }

    private void copyCreateBlockCommand(BlockState blockState, BlockPos blockPos, @Nullable CompoundTag compoundTag) {
        StringBuilder stringBuilder = new StringBuilder(BlockStateParser.serialize(blockState));
        if (compoundTag != null) {
            stringBuilder.append(compoundTag);
        }
        String string = String.format(Locale.ROOT, "/setblock %d %d %d %s", blockPos.getX(), blockPos.getY(), blockPos.getZ(), stringBuilder);
        this.setClipboard(string);
    }

    private void copyCreateEntityCommand(ResourceLocation resourceLocation, Vec3 vec3, @Nullable CompoundTag compoundTag) {
        String string;
        if (compoundTag != null) {
            compoundTag.remove("UUID");
            compoundTag.remove("Pos");
            compoundTag.remove("Dimension");
            String string2 = NbtUtils.toPrettyComponent(compoundTag).getString();
            string = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", resourceLocation.toString(), vec3.x, vec3.y, vec3.z, string2);
        } else {
            string = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", resourceLocation.toString(), vec3.x, vec3.y, vec3.z);
        }
        this.setClipboard(string);
    }

    public void keyPress(long l, int n, int n2, int n3, int n4) {
        boolean bl;
        if (l != this.minecraft.getWindow().getWindow()) {
            return;
        }
        if (this.debugCrashKeyTime > 0L) {
            if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) || !InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292)) {
                this.debugCrashKeyTime = -1L;
            }
        } else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292)) {
            this.handledDebugKey = true;
            this.debugCrashKeyTime = Util.getMillis();
            this.debugCrashKeyReportedTime = Util.getMillis();
            this.debugCrashKeyReportedCount = 0L;
        }
        Screen screen = this.minecraft.screen;
        if (!(n3 != 1 || this.minecraft.screen instanceof KeyBindsScreen && ((KeyBindsScreen)screen).lastKeySelection > Util.getMillis() - 20L)) {
            if (this.minecraft.options.keyFullscreen.matches(n, n2)) {
                this.minecraft.getWindow().toggleFullScreen();
                this.minecraft.options.fullscreen = this.minecraft.getWindow().isFullscreen();
                this.minecraft.options.save();
                return;
            }
            if (this.minecraft.options.keyScreenshot.matches(n, n2)) {
                if (Screen.hasControlDown()) {
                    // empty if block
                }
                Screenshot.grab(this.minecraft.gameDirectory, this.minecraft.getMainRenderTarget(), component -> this.minecraft.execute(() -> this.minecraft.gui.getChat().addMessage((Component)component)));
                return;
            }
        }
        if (NarratorChatListener.INSTANCE.isActive()) {
            boolean bl2;
            boolean bl3 = bl2 = screen == null || !(screen.getFocused() instanceof EditBox) || !((EditBox)screen.getFocused()).canConsumeInput();
            if (n3 != 0 && n == 66 && Screen.hasControlDown() && bl2) {
                bl = this.minecraft.options.narratorStatus == NarratorStatus.OFF;
                this.minecraft.options.narratorStatus = NarratorStatus.byId(this.minecraft.options.narratorStatus.getId() + 1);
                NarratorChatListener.INSTANCE.updateNarratorStatus(this.minecraft.options.narratorStatus);
                if (screen instanceof SimpleOptionsSubScreen) {
                    ((SimpleOptionsSubScreen)screen).updateNarratorButton();
                }
                if (bl && screen != null) {
                    screen.narrationEnabled();
                }
            }
        }
        if (screen != null) {
            boolean[] blArray = new boolean[]{false};
            Screen.wrapScreenError(() -> {
                if (n3 == 1 || n3 == 2 && this.sendRepeatsToGui) {
                    screen.afterKeyboardAction();
                    blArray[0] = screen.keyPressed(n, n2, n4);
                } else if (n3 == 0) {
                    blArray[0] = screen.keyReleased(n, n2, n4);
                }
            }, "keyPressed event handler", screen.getClass().getCanonicalName());
            if (blArray[0]) {
                return;
            }
        }
        if (this.minecraft.screen == null || this.minecraft.screen.passEvents) {
            InputConstants.Key key = InputConstants.getKey(n, n2);
            if (n3 == 0) {
                KeyMapping.set(key, false);
                if (n == 292) {
                    if (this.handledDebugKey) {
                        this.handledDebugKey = false;
                    } else {
                        this.minecraft.options.renderDebug = !this.minecraft.options.renderDebug;
                        this.minecraft.options.renderDebugCharts = this.minecraft.options.renderDebug && Screen.hasShiftDown();
                        this.minecraft.options.renderFpsChart = this.minecraft.options.renderDebug && Screen.hasAltDown();
                    }
                }
            } else {
                if (n == 293 && this.minecraft.gameRenderer != null) {
                    this.minecraft.gameRenderer.togglePostEffect();
                }
                bl = false;
                if (this.minecraft.screen == null) {
                    if (n == 256) {
                        boolean bl4 = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292);
                        this.minecraft.pauseGame(bl4);
                    }
                    bl = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292) && this.handleDebugKeys(n);
                    this.handledDebugKey |= bl;
                    if (n == 290) {
                        boolean bl5 = this.minecraft.options.hideGui = !this.minecraft.options.hideGui;
                    }
                }
                if (bl) {
                    KeyMapping.set(key, false);
                } else {
                    KeyMapping.set(key, true);
                    KeyMapping.click(key);
                }
                if (this.minecraft.options.renderDebugCharts && n >= 48 && n <= 57) {
                    this.minecraft.debugFpsMeterKeyPress(n - 48);
                }
            }
        }
    }

    private void charTyped(long l, int n, int n2) {
        if (l != this.minecraft.getWindow().getWindow()) {
            return;
        }
        Screen screen = this.minecraft.screen;
        if (screen == null || this.minecraft.getOverlay() != null) {
            return;
        }
        if (Character.charCount(n) == 1) {
            Screen.wrapScreenError(() -> screen.charTyped((char)n, n2), "charTyped event handler", screen.getClass().getCanonicalName());
        } else {
            for (char c : Character.toChars(n)) {
                Screen.wrapScreenError(() -> screen.charTyped(c, n2), "charTyped event handler", screen.getClass().getCanonicalName());
            }
        }
    }

    public void setSendRepeatsToGui(boolean bl) {
        this.sendRepeatsToGui = bl;
    }

    public void setup(long l2) {
        InputConstants.setupKeyboardCallbacks(l2, (l, n, n2, n3, n4) -> this.minecraft.execute(() -> this.keyPress(l, n, n2, n3, n4)), (l, n, n2) -> this.minecraft.execute(() -> this.charTyped(l, n, n2)));
    }

    public String getClipboard() {
        return this.clipboardManager.getClipboard(this.minecraft.getWindow().getWindow(), (n, l) -> {
            if (n != 65545) {
                this.minecraft.getWindow().defaultErrorCallback(n, l);
            }
        });
    }

    public void setClipboard(String string) {
        if (!string.isEmpty()) {
            this.clipboardManager.setClipboard(this.minecraft.getWindow().getWindow(), string);
        }
    }

    public void tick() {
        if (this.debugCrashKeyTime > 0L) {
            long l = Util.getMillis();
            long l2 = 10000L - (l - this.debugCrashKeyTime);
            long l3 = l - this.debugCrashKeyReportedTime;
            if (l2 < 0L) {
                if (Screen.hasControlDown()) {
                    Blaze3D.youJustLostTheGame();
                }
                String string = "Manually triggered debug crash";
                CrashReport crashReport = new CrashReport("Manually triggered debug crash", new Throwable("Manually triggered debug crash"));
                CrashReportCategory crashReportCategory = crashReport.addCategory("Manual crash details");
                NativeModuleLister.addCrashSection(crashReportCategory);
                throw new ReportedException(crashReport);
            }
            if (l3 >= 1000L) {
                if (this.debugCrashKeyReportedCount == 0L) {
                    this.debugFeedbackTranslated("debug.crash.message", new Object[0]);
                } else {
                    this.debugWarningTranslated("debug.crash.warning", Mth.ceil((float)l2 / 1000.0f));
                }
                this.debugCrashKeyReportedTime = l;
                ++this.debugCrashKeyReportedCount;
            }
        }
    }
}

