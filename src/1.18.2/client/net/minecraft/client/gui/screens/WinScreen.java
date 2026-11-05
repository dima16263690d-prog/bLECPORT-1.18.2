/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  org.apache.commons.io.IOUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class WinScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation EDITION_LOCATION = new ResourceLocation("textures/gui/title/edition.png");
    private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
    private static final Component SECTION_HEADING = new TextComponent("============").withStyle(ChatFormatting.WHITE);
    private static final String NAME_PREFIX = "           ";
    private static final String OBFUSCATE_TOKEN = "" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + ChatFormatting.GREEN + ChatFormatting.AQUA;
    private static final int LOGO_WIDTH = 274;
    private static final float SPEEDUP_FACTOR = 5.0f;
    private static final float SPEEDUP_FACTOR_FAST = 15.0f;
    private final boolean poem;
    private final Runnable onFinished;
    private float scroll;
    private List<FormattedCharSequence> lines;
    private IntSet centeredLines;
    private int totalScrollLength;
    private boolean speedupActive;
    private final IntSet speedupModifiers = new IntOpenHashSet();
    private float scrollSpeed;
    private final float unmodifiedScrollSpeed;

    public WinScreen(boolean bl, Runnable runnable) {
        super(NarratorChatListener.NO_TITLE);
        this.poem = bl;
        this.onFinished = runnable;
        this.unmodifiedScrollSpeed = !bl ? 0.75f : 0.5f;
        this.scrollSpeed = this.unmodifiedScrollSpeed;
    }

    private float calculateScrollSpeed() {
        if (this.speedupActive) {
            return this.unmodifiedScrollSpeed * (5.0f + (float)this.speedupModifiers.size() * 15.0f);
        }
        return this.unmodifiedScrollSpeed;
    }

    @Override
    public void tick() {
        this.minecraft.getMusicManager().tick();
        this.minecraft.getSoundManager().tick(false);
        float f = this.totalScrollLength + this.height + this.height + 24;
        if (this.scroll > f) {
            this.respawn();
        }
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (n == 341 || n == 345) {
            this.speedupModifiers.add(n);
        } else if (n == 32) {
            this.speedupActive = true;
        }
        this.scrollSpeed = this.calculateScrollSpeed();
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public boolean keyReleased(int n, int n2, int n3) {
        if (n == 32) {
            this.speedupActive = false;
        } else if (n == 341 || n == 345) {
            this.speedupModifiers.remove(n);
        }
        this.scrollSpeed = this.calculateScrollSpeed();
        return super.keyReleased(n, n2, n3);
    }

    @Override
    public void onClose() {
        this.respawn();
    }

    private void respawn() {
        this.onFinished.run();
        this.minecraft.setScreen(null);
    }

    @Override
    protected void init() {
        if (this.lines != null) {
            return;
        }
        this.lines = Lists.newArrayList();
        this.centeredLines = new IntOpenHashSet();
        if (this.poem) {
            this.wrapCreditsIO("texts/end.txt", this::addPoemFile);
        }
        this.wrapCreditsIO("texts/credits.json", this::addCreditsFile);
        if (this.poem) {
            this.wrapCreditsIO("texts/postcredits.txt", this::addPoemFile);
        }
        this.totalScrollLength = this.lines.size() * 12;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void wrapCreditsIO(String string, CreditsReader creditsReader) {
        Resource resource = null;
        try {
            resource = this.minecraft.getResourceManager().getResource(new ResourceLocation(string));
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            creditsReader.read(inputStreamReader);
        }
        catch (Exception exception) {
            try {
                LOGGER.error("Couldn't load credits", (Throwable)exception);
            }
            catch (Throwable throwable) {
                IOUtils.closeQuietly(resource);
                throw throwable;
            }
            IOUtils.closeQuietly((Closeable)resource);
        }
        IOUtils.closeQuietly((Closeable)resource);
    }

    private void addPoemFile(InputStreamReader inputStreamReader) throws IOException {
        int n;
        Object object;
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        Random random = new Random(8124371L);
        while ((object = bufferedReader.readLine()) != null) {
            object = ((String)object).replaceAll("PLAYERNAME", this.minecraft.getUser().getName());
            while ((n = ((String)object).indexOf(OBFUSCATE_TOKEN)) != -1) {
                String string = ((String)object).substring(0, n);
                String string2 = ((String)object).substring(n + OBFUSCATE_TOKEN.length());
                object = string + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + "XXXXXXXX".substring(0, random.nextInt(4) + 3) + string2;
            }
            this.addPoemLines((String)object);
            this.addEmptyLine();
        }
        for (n = 0; n < 8; ++n) {
            this.addEmptyLine();
        }
    }

    private void addCreditsFile(InputStreamReader inputStreamReader) {
        JsonArray jsonArray = GsonHelper.parseArray(inputStreamReader);
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String string = jsonObject.get("section").getAsString();
            this.addCreditsLine(SECTION_HEADING, true);
            this.addCreditsLine(new TextComponent(string).withStyle(ChatFormatting.YELLOW), true);
            this.addCreditsLine(SECTION_HEADING, true);
            this.addEmptyLine();
            this.addEmptyLine();
            JsonArray jsonArray2 = jsonObject.getAsJsonArray("titles");
            for (JsonElement jsonElement2 : jsonArray2) {
                JsonObject jsonObject2 = jsonElement2.getAsJsonObject();
                String string2 = jsonObject2.get("title").getAsString();
                JsonArray jsonArray3 = jsonObject2.getAsJsonArray("names");
                this.addCreditsLine(new TextComponent(string2).withStyle(ChatFormatting.GRAY), false);
                for (JsonElement jsonElement3 : jsonArray3) {
                    String string3 = jsonElement3.getAsString();
                    this.addCreditsLine(new TextComponent(NAME_PREFIX).append(string3).withStyle(ChatFormatting.WHITE), false);
                }
                this.addEmptyLine();
                this.addEmptyLine();
            }
        }
    }

    private void addEmptyLine() {
        this.lines.add(FormattedCharSequence.EMPTY);
    }

    private void addPoemLines(String string) {
        this.lines.addAll(this.minecraft.font.split(new TextComponent(string), 274));
    }

    private void addCreditsLine(Component component, boolean bl) {
        if (bl) {
            this.centeredLines.add(this.lines.size());
        }
        this.lines.add(component.getVisualOrderText());
    }

    private void renderBg() {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
        int n = this.width;
        float f = -this.scroll * 0.5f;
        float f2 = (float)this.height - 0.5f * this.scroll;
        float f3 = 0.015625f;
        float f4 = this.scroll / this.unmodifiedScrollSpeed;
        float f5 = f4 * 0.02f;
        float f6 = (float)(this.totalScrollLength + this.height + this.height + 24) / this.unmodifiedScrollSpeed;
        float f7 = (f6 - 20.0f - f4) * 0.005f;
        if (f7 < f5) {
            f5 = f7;
        }
        if (f5 > 1.0f) {
            f5 = 1.0f;
        }
        f5 *= f5;
        f5 = f5 * 96.0f / 255.0f;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(0.0, this.height, this.getBlitOffset()).uv(0.0f, f * 0.015625f).color(f5, f5, f5, 1.0f).endVertex();
        bufferBuilder.vertex(n, this.height, this.getBlitOffset()).uv((float)n * 0.015625f, f * 0.015625f).color(f5, f5, f5, 1.0f).endVertex();
        bufferBuilder.vertex(n, 0.0, this.getBlitOffset()).uv((float)n * 0.015625f, f2 * 0.015625f).color(f5, f5, f5, 1.0f).endVertex();
        bufferBuilder.vertex(0.0, 0.0, this.getBlitOffset()).uv(0.0f, f2 * 0.015625f).color(f5, f5, f5, 1.0f).endVertex();
        tesselator.end();
    }

    @Override
    public void render(PoseStack poseStack, int n3, int n4, float f) {
        int n5;
        this.scroll += f * this.scrollSpeed;
        this.renderBg();
        int n6 = this.width / 2 - 137;
        int n7 = this.height + 50;
        float f2 = -this.scroll;
        poseStack.pushPose();
        poseStack.translate(0.0, f2, 0.0);
        RenderSystem.setShaderTexture(0, LOGO_LOCATION);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        this.blitOutlineBlack(n6, n7, (n, n2) -> {
            this.blit(poseStack, n + 0, (int)n2, 0, 0, 155, 44);
            this.blit(poseStack, n + 155, (int)n2, 0, 45, 155, 44);
        });
        RenderSystem.disableBlend();
        RenderSystem.setShaderTexture(0, EDITION_LOCATION);
        WinScreen.blit(poseStack, n6 + 88, n7 + 37, 0.0f, 0.0f, 98, 14, 128, 16);
        int n8 = n7 + 100;
        for (n5 = 0; n5 < this.lines.size(); ++n5) {
            float f3;
            if (n5 == this.lines.size() - 1 && (f3 = (float)n8 + f2 - (float)(this.height / 2 - 6)) < 0.0f) {
                poseStack.translate(0.0, -f3, 0.0);
            }
            if ((float)n8 + f2 + 12.0f + 8.0f > 0.0f && (float)n8 + f2 < (float)this.height) {
                FormattedCharSequence formattedCharSequence = this.lines.get(n5);
                if (this.centeredLines.contains(n5)) {
                    this.font.drawShadow(poseStack, formattedCharSequence, (float)(n6 + (274 - this.font.width(formattedCharSequence)) / 2), (float)n8, 0xFFFFFF);
                } else {
                    this.font.drawShadow(poseStack, formattedCharSequence, (float)n6, (float)n8, 0xFFFFFF);
                }
            }
            n8 += 12;
        }
        poseStack.popPose();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, VIGNETTE_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        n5 = this.width;
        int n9 = this.height;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(0.0, n9, this.getBlitOffset()).uv(0.0f, 1.0f).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(n5, n9, this.getBlitOffset()).uv(1.0f, 1.0f).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(n5, 0.0, this.getBlitOffset()).uv(1.0f, 0.0f).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(0.0, 0.0, this.getBlitOffset()).uv(0.0f, 0.0f).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
        super.render(poseStack, n3, n4, f);
    }

    @FunctionalInterface
    static interface CreditsReader {
        public void read(InputStreamReader var1) throws IOException;
    }
}

