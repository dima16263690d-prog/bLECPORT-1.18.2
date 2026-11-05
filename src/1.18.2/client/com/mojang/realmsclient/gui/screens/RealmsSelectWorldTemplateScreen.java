/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.TextRenderingUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class RealmsSelectWorldTemplateScreen
extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    static final ResourceLocation LINK_ICON = new ResourceLocation("realms", "textures/gui/realms/link_icons.png");
    static final ResourceLocation TRAILER_ICON = new ResourceLocation("realms", "textures/gui/realms/trailer_icons.png");
    static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
    static final Component PUBLISHER_LINK_TOOLTIP = new TranslatableComponent("mco.template.info.tooltip");
    static final Component TRAILER_LINK_TOOLTIP = new TranslatableComponent("mco.template.trailer.tooltip");
    private final Consumer<WorldTemplate> callback;
    WorldTemplateObjectSelectionList worldTemplateObjectSelectionList;
    int selectedTemplate = -1;
    private Button selectButton;
    private Button trailerButton;
    private Button publisherButton;
    @Nullable
    Component toolTip;
    @Nullable
    String currentLink;
    private final RealmsServer.WorldType worldType;
    int clicks;
    @Nullable
    private Component[] warning;
    private String warningURL;
    boolean displayWarning;
    private boolean hoverWarning;
    @Nullable
    List<TextRenderingUtils.Line> noTemplatesMessage;

    public RealmsSelectWorldTemplateScreen(Component component, Consumer<WorldTemplate> consumer, RealmsServer.WorldType worldType) {
        this(component, consumer, worldType, null);
    }

    public RealmsSelectWorldTemplateScreen(Component component, Consumer<WorldTemplate> consumer, RealmsServer.WorldType worldType, @Nullable WorldTemplatePaginatedList worldTemplatePaginatedList) {
        super(component);
        this.callback = consumer;
        this.worldType = worldType;
        if (worldTemplatePaginatedList == null) {
            this.worldTemplateObjectSelectionList = new WorldTemplateObjectSelectionList();
            this.fetchTemplatesAsync(new WorldTemplatePaginatedList(10));
        } else {
            this.worldTemplateObjectSelectionList = new WorldTemplateObjectSelectionList(Lists.newArrayList(worldTemplatePaginatedList.templates));
            this.fetchTemplatesAsync(worldTemplatePaginatedList);
        }
    }

    public void setWarning(Component ... componentArray) {
        this.warning = componentArray;
        this.displayWarning = true;
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (this.hoverWarning && this.warningURL != null) {
            Util.getPlatform().openUri("https://www.minecraft.net/realms/adventure-maps-in-1-9");
            return true;
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.worldTemplateObjectSelectionList = new WorldTemplateObjectSelectionList(this.worldTemplateObjectSelectionList.getTemplates());
        this.trailerButton = this.addRenderableWidget(new Button(this.width / 2 - 206, this.height - 32, 100, 20, new TranslatableComponent("mco.template.button.trailer"), button -> this.onTrailer()));
        this.selectButton = this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 32, 100, 20, new TranslatableComponent("mco.template.button.select"), button -> this.selectTemplate()));
        Component component = this.worldType == RealmsServer.WorldType.MINIGAME ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_BACK;
        Button button2 = new Button(this.width / 2 + 6, this.height - 32, 100, 20, component, button -> this.onClose());
        this.addRenderableWidget(button2);
        this.publisherButton = this.addRenderableWidget(new Button(this.width / 2 + 112, this.height - 32, 100, 20, new TranslatableComponent("mco.template.button.publisher"), button -> this.onPublish()));
        this.selectButton.active = false;
        this.trailerButton.visible = false;
        this.publisherButton.visible = false;
        this.addWidget(this.worldTemplateObjectSelectionList);
        this.magicalSpecialHackyFocus(this.worldTemplateObjectSelectionList);
    }

    @Override
    public Component getNarrationMessage() {
        ArrayList arrayList = Lists.newArrayListWithCapacity((int)2);
        if (this.title != null) {
            arrayList.add(this.title);
        }
        if (this.warning != null) {
            arrayList.addAll(Arrays.asList(this.warning));
        }
        return CommonComponents.joinLines(arrayList);
    }

    void updateButtonStates() {
        this.publisherButton.visible = this.shouldPublisherBeVisible();
        this.trailerButton.visible = this.shouldTrailerBeVisible();
        this.selectButton.active = this.shouldSelectButtonBeActive();
    }

    private boolean shouldSelectButtonBeActive() {
        return this.selectedTemplate != -1;
    }

    private boolean shouldPublisherBeVisible() {
        return this.selectedTemplate != -1 && !this.getSelectedTemplate().link.isEmpty();
    }

    private WorldTemplate getSelectedTemplate() {
        return this.worldTemplateObjectSelectionList.get(this.selectedTemplate);
    }

    private boolean shouldTrailerBeVisible() {
        return this.selectedTemplate != -1 && !this.getSelectedTemplate().trailer.isEmpty();
    }

    @Override
    public void tick() {
        super.tick();
        --this.clicks;
        if (this.clicks < 0) {
            this.clicks = 0;
        }
    }

    @Override
    public void onClose() {
        this.callback.accept(null);
    }

    void selectTemplate() {
        if (this.hasValidTemplate()) {
            this.callback.accept(this.getSelectedTemplate());
        }
    }

    private boolean hasValidTemplate() {
        return this.selectedTemplate >= 0 && this.selectedTemplate < this.worldTemplateObjectSelectionList.getItemCount();
    }

    private void onTrailer() {
        if (this.hasValidTemplate()) {
            WorldTemplate worldTemplate = this.getSelectedTemplate();
            if (!"".equals(worldTemplate.trailer)) {
                Util.getPlatform().openUri(worldTemplate.trailer);
            }
        }
    }

    private void onPublish() {
        if (this.hasValidTemplate()) {
            WorldTemplate worldTemplate = this.getSelectedTemplate();
            if (!"".equals(worldTemplate.link)) {
                Util.getPlatform().openUri(worldTemplate.link);
            }
        }
    }

    private void fetchTemplatesAsync(final WorldTemplatePaginatedList worldTemplatePaginatedList) {
        new Thread("realms-template-fetcher"){

            @Override
            public void run() {
                WorldTemplatePaginatedList worldTemplatePaginatedList2 = worldTemplatePaginatedList;
                RealmsClient realmsClient = RealmsClient.create();
                while (worldTemplatePaginatedList2 != null) {
                    Either<WorldTemplatePaginatedList, String> either = RealmsSelectWorldTemplateScreen.this.fetchTemplates(worldTemplatePaginatedList2, realmsClient);
                    worldTemplatePaginatedList2 = RealmsSelectWorldTemplateScreen.this.minecraft.submit(() -> {
                        if (either.right().isPresent()) {
                            LOGGER.error("Couldn't fetch templates: {}", either.right().get());
                            if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
                                RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(I18n.get("mco.template.select.failure", new Object[0]), new TextRenderingUtils.LineSegment[0]);
                            }
                            return null;
                        }
                        WorldTemplatePaginatedList worldTemplatePaginatedList2 = (WorldTemplatePaginatedList)either.left().get();
                        for (WorldTemplate object : worldTemplatePaginatedList2.templates) {
                            RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.addEntry(object);
                        }
                        if (worldTemplatePaginatedList2.templates.isEmpty()) {
                            if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
                                String string = I18n.get("mco.template.select.none", "%link");
                                TextRenderingUtils.LineSegment lineSegment = TextRenderingUtils.LineSegment.link(I18n.get("mco.template.select.none.linkTitle", new Object[0]), "https://aka.ms/MinecraftRealmsContentCreator");
                                RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(string, lineSegment);
                            }
                            return null;
                        }
                        return worldTemplatePaginatedList2;
                    }).join();
                }
            }
        }.start();
    }

    Either<WorldTemplatePaginatedList, String> fetchTemplates(WorldTemplatePaginatedList worldTemplatePaginatedList, RealmsClient realmsClient) {
        try {
            return Either.left((Object)realmsClient.fetchWorldTemplates(worldTemplatePaginatedList.page + 1, worldTemplatePaginatedList.size, this.worldType));
        }
        catch (RealmsServiceException realmsServiceException) {
            return Either.right((Object)realmsServiceException.getMessage());
        }
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.toolTip = null;
        this.currentLink = null;
        this.hoverWarning = false;
        this.renderBackground(poseStack);
        this.worldTemplateObjectSelectionList.render(poseStack, n, n2, f);
        if (this.noTemplatesMessage != null) {
            this.renderMultilineMessage(poseStack, n, n2, this.noTemplatesMessage);
        }
        RealmsSelectWorldTemplateScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 13, 0xFFFFFF);
        if (this.displayWarning) {
            int n3;
            int n4;
            Component[] componentArray = this.warning;
            for (n4 = 0; n4 < componentArray.length; ++n4) {
                int n5 = this.font.width(componentArray[n4]);
                n3 = this.width / 2 - n5 / 2;
                int n6 = RealmsSelectWorldTemplateScreen.row(-1 + n4);
                if (n < n3 || n > n3 + n5 || n2 < n6 || n2 > n6 + this.font.lineHeight) continue;
                this.hoverWarning = true;
            }
            for (n4 = 0; n4 < componentArray.length; ++n4) {
                Component component = componentArray[n4];
                n3 = 0xA0A0A0;
                if (this.warningURL != null) {
                    if (this.hoverWarning) {
                        n3 = 7107012;
                        component = component.copy().withStyle(ChatFormatting.STRIKETHROUGH);
                    } else {
                        n3 = 0x3366BB;
                    }
                }
                RealmsSelectWorldTemplateScreen.drawCenteredString(poseStack, this.font, component, this.width / 2, RealmsSelectWorldTemplateScreen.row(-1 + n4), n3);
            }
        }
        super.render(poseStack, n, n2, f);
        this.renderMousehoverTooltip(poseStack, this.toolTip, n, n2);
    }

    private void renderMultilineMessage(PoseStack poseStack, int n, int n2, List<TextRenderingUtils.Line> list) {
        for (int i = 0; i < list.size(); ++i) {
            TextRenderingUtils.Line line = list.get(i);
            int n3 = RealmsSelectWorldTemplateScreen.row(4 + i);
            int n4 = line.segments.stream().mapToInt(lineSegment -> this.font.width(lineSegment.renderedText())).sum();
            int n5 = this.width / 2 - n4 / 2;
            for (TextRenderingUtils.LineSegment lineSegment2 : line.segments) {
                int n6 = lineSegment2.isLink() ? 0x3366BB : 0xFFFFFF;
                int n7 = this.font.drawShadow(poseStack, lineSegment2.renderedText(), (float)n5, (float)n3, n6);
                if (lineSegment2.isLink() && n > n5 && n < n7 && n2 > n3 - 3 && n2 < n3 + 8) {
                    this.toolTip = new TextComponent(lineSegment2.getLinkUrl());
                    this.currentLink = lineSegment2.getLinkUrl();
                }
                n5 = n7;
            }
        }
    }

    protected void renderMousehoverTooltip(PoseStack poseStack, @Nullable Component component, int n, int n2) {
        if (component == null) {
            return;
        }
        int n3 = n + 12;
        int n4 = n2 - 12;
        int n5 = this.font.width(component);
        this.fillGradient(poseStack, n3 - 3, n4 - 3, n3 + n5 + 3, n4 + 8 + 3, -1073741824, -1073741824);
        this.font.drawShadow(poseStack, component, (float)n3, (float)n4, 0xFFFFFF);
    }

    class WorldTemplateObjectSelectionList
    extends RealmsObjectSelectionList<Entry> {
        public WorldTemplateObjectSelectionList() {
            this(Collections.emptyList());
        }

        public WorldTemplateObjectSelectionList(Iterable<WorldTemplate> iterable) {
            super(RealmsSelectWorldTemplateScreen.this.width, RealmsSelectWorldTemplateScreen.this.height, RealmsSelectWorldTemplateScreen.this.displayWarning ? RealmsSelectWorldTemplateScreen.row(1) : 32, RealmsSelectWorldTemplateScreen.this.height - 40, 46);
            iterable.forEach(this::addEntry);
        }

        public void addEntry(WorldTemplate worldTemplate) {
            this.addEntry(new Entry(worldTemplate));
        }

        @Override
        public boolean mouseClicked(double d, double d2, int n) {
            if (n == 0 && d2 >= (double)this.y0 && d2 <= (double)this.y1) {
                int n2 = this.width / 2 - 150;
                if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
                    Util.getPlatform().openUri(RealmsSelectWorldTemplateScreen.this.currentLink);
                }
                int n3 = (int)Math.floor(d2 - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
                int n4 = n3 / this.itemHeight;
                if (d >= (double)n2 && d < (double)this.getScrollbarPosition() && n4 >= 0 && n3 >= 0 && n4 < this.getItemCount()) {
                    this.selectItem(n4);
                    this.itemClicked(n3, n4, d, d2, this.width);
                    if (n4 >= RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.getItemCount()) {
                        return super.mouseClicked(d, d2, n);
                    }
                    RealmsSelectWorldTemplateScreen.this.clicks += 7;
                    if (RealmsSelectWorldTemplateScreen.this.clicks >= 10) {
                        RealmsSelectWorldTemplateScreen.this.selectTemplate();
                    }
                    return true;
                }
            }
            return super.mouseClicked(d, d2, n);
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.children().indexOf(entry);
            RealmsSelectWorldTemplateScreen.this.updateButtonStates();
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 46;
        }

        @Override
        public int getRowWidth() {
            return 300;
        }

        @Override
        public void renderBackground(PoseStack poseStack) {
            RealmsSelectWorldTemplateScreen.this.renderBackground(poseStack);
        }

        @Override
        public boolean isFocused() {
            return RealmsSelectWorldTemplateScreen.this.getFocused() == this;
        }

        public boolean isEmpty() {
            return this.getItemCount() == 0;
        }

        public WorldTemplate get(int n) {
            return ((Entry)this.children().get((int)n)).template;
        }

        public List<WorldTemplate> getTemplates() {
            return this.children().stream().map(entry -> entry.template).collect(Collectors.toList());
        }
    }

    class Entry
    extends ObjectSelectionList.Entry<Entry> {
        final WorldTemplate template;

        public Entry(WorldTemplate worldTemplate) {
            this.template = worldTemplate;
        }

        @Override
        public void render(PoseStack poseStack, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            this.renderWorldTemplateItem(poseStack, this.template, n3, n2, n6, n7);
        }

        private void renderWorldTemplateItem(PoseStack poseStack, WorldTemplate worldTemplate, int n, int n2, int n3, int n4) {
            int n5 = n + 45 + 20;
            RealmsSelectWorldTemplateScreen.this.font.draw(poseStack, worldTemplate.name, (float)n5, (float)(n2 + 2), 0xFFFFFF);
            RealmsSelectWorldTemplateScreen.this.font.draw(poseStack, worldTemplate.author, (float)n5, (float)(n2 + 15), 0x6C6C6C);
            RealmsSelectWorldTemplateScreen.this.font.draw(poseStack, worldTemplate.version, (float)(n5 + 227 - RealmsSelectWorldTemplateScreen.this.font.width(worldTemplate.version)), (float)(n2 + 1), 0x6C6C6C);
            if (!("".equals(worldTemplate.link) && "".equals(worldTemplate.trailer) && "".equals(worldTemplate.recommendedPlayers))) {
                this.drawIcons(poseStack, n5 - 1, n2 + 25, n3, n4, worldTemplate.link, worldTemplate.trailer, worldTemplate.recommendedPlayers);
            }
            this.drawImage(poseStack, n, n2 + 1, n3, n4, worldTemplate);
        }

        private void drawImage(PoseStack poseStack, int n, int n2, int n3, int n4, WorldTemplate worldTemplate) {
            RealmsTextureManager.bindWorldTemplate(worldTemplate.id, worldTemplate.image);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            GuiComponent.blit(poseStack, n + 1, n2 + 1, 0.0f, 0.0f, 38, 38, 38, 38);
            RenderSystem.setShaderTexture(0, SLOT_FRAME_LOCATION);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            GuiComponent.blit(poseStack, n, n2, 0.0f, 0.0f, 40, 40, 40, 40);
        }

        private void drawIcons(PoseStack poseStack, int n, int n2, int n3, int n4, String string, String string2, String string3) {
            if (!"".equals(string3)) {
                RealmsSelectWorldTemplateScreen.this.font.draw(poseStack, string3, (float)n, (float)(n2 + 4), 0x4C4C4C);
            }
            int n5 = "".equals(string3) ? 0 : RealmsSelectWorldTemplateScreen.this.font.width(string3) + 2;
            boolean bl = false;
            boolean bl2 = false;
            boolean bl3 = "".equals(string);
            if (n3 >= n + n5 && n3 <= n + n5 + 32 && n4 >= n2 && n4 <= n2 + 15 && n4 < RealmsSelectWorldTemplateScreen.this.height - 15 && n4 > 32) {
                if (n3 <= n + 15 + n5 && n3 > n5) {
                    if (bl3) {
                        bl2 = true;
                    } else {
                        bl = true;
                    }
                } else if (!bl3) {
                    bl2 = true;
                }
            }
            if (!bl3) {
                RenderSystem.setShaderTexture(0, LINK_ICON);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                float f = bl ? 15.0f : 0.0f;
                GuiComponent.blit(poseStack, n + n5, n2, f, 0.0f, 15, 15, 30, 15);
            }
            if (!"".equals(string2)) {
                RenderSystem.setShaderTexture(0, TRAILER_ICON);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                int n6 = n + n5 + (bl3 ? 0 : 17);
                float f = bl2 ? 15.0f : 0.0f;
                GuiComponent.blit(poseStack, n6, n2, f, 0.0f, 15, 15, 30, 15);
            }
            if (bl) {
                RealmsSelectWorldTemplateScreen.this.toolTip = PUBLISHER_LINK_TOOLTIP;
                RealmsSelectWorldTemplateScreen.this.currentLink = string;
            } else if (bl2 && !"".equals(string2)) {
                RealmsSelectWorldTemplateScreen.this.toolTip = TRAILER_LINK_TOOLTIP;
                RealmsSelectWorldTemplateScreen.this.currentLink = string2;
            }
        }

        @Override
        public Component getNarration() {
            Component component = CommonComponents.joinLines(new TextComponent(this.template.name), new TranslatableComponent("mco.template.select.narrate.authors", this.template.author), new TextComponent(this.template.recommendedPlayers), new TranslatableComponent("mco.template.select.narrate.version", this.template.version));
            return new TranslatableComponent("narrator.select", component);
        }
    }
}

