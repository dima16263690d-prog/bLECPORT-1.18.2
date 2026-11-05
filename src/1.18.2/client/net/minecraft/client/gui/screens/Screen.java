/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import com.mojang.math.Matrix4f;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.ScreenNarrationCollector;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.slf4j.Logger;

public abstract class Screen
extends AbstractContainerEventHandler
implements Widget {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> ALLOWED_PROTOCOLS = Sets.newHashSet((Object[])new String[]{"http", "https"});
    private static final int EXTRA_SPACE_AFTER_FIRST_TOOLTIP_LINE = 2;
    private static final Component USAGE_NARRATION = new TranslatableComponent("narrator.screen.usage");
    protected final Component title;
    private final List<GuiEventListener> children = Lists.newArrayList();
    private final List<NarratableEntry> narratables = Lists.newArrayList();
    @Nullable
    protected Minecraft minecraft;
    protected ItemRenderer itemRenderer;
    public int width;
    public int height;
    private final List<Widget> renderables = Lists.newArrayList();
    public boolean passEvents;
    protected Font font;
    @Nullable
    private URI clickedLink;
    private static final long NARRATE_SUPPRESS_AFTER_INIT_TIME;
    private static final long NARRATE_DELAY_NARRATOR_ENABLED;
    private static final long NARRATE_DELAY_MOUSE_MOVE = 750L;
    private static final long NARRATE_DELAY_MOUSE_ACTION = 200L;
    private static final long NARRATE_DELAY_KEYBOARD_ACTION = 200L;
    private final ScreenNarrationCollector narrationState = new ScreenNarrationCollector();
    private long narrationSuppressTime = Long.MIN_VALUE;
    private long nextNarrationTime = Long.MAX_VALUE;
    @Nullable
    private NarratableEntry lastNarratable;

    protected Screen(Component component) {
        this.title = component;
    }

    public Component getTitle() {
        return this.title;
    }

    public Component getNarrationMessage() {
        return this.getTitle();
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        for (Widget widget : this.renderables) {
            widget.render(poseStack, n, n2, f);
        }
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (n == 256 && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        }
        if (n == 258) {
            boolean bl;
            boolean bl2 = bl = !Screen.hasShiftDown();
            if (!this.changeFocus(bl)) {
                this.changeFocus(bl);
            }
            return false;
        }
        return super.keyPressed(n, n2, n3);
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    public void onClose() {
        this.minecraft.setScreen(null);
    }

    protected <T extends GuiEventListener & Widget> T addRenderableWidget(T t) {
        this.renderables.add(t);
        return this.addWidget(t);
    }

    protected <T extends Widget> T addRenderableOnly(T t) {
        this.renderables.add(t);
        return t;
    }

    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T t) {
        this.children.add(t);
        this.narratables.add(t);
        return t;
    }

    protected void removeWidget(GuiEventListener guiEventListener) {
        if (guiEventListener instanceof Widget) {
            this.renderables.remove((Widget)((Object)guiEventListener));
        }
        if (guiEventListener instanceof NarratableEntry) {
            this.narratables.remove((NarratableEntry)((Object)guiEventListener));
        }
        this.children.remove(guiEventListener);
    }

    protected void clearWidgets() {
        this.renderables.clear();
        this.children.clear();
        this.narratables.clear();
    }

    protected void renderTooltip(PoseStack poseStack, ItemStack itemStack, int n, int n2) {
        this.renderTooltip(poseStack, this.getTooltipFromItem(itemStack), itemStack.getTooltipImage(), n, n2);
    }

    public void renderTooltip(PoseStack poseStack, List<Component> list, Optional<TooltipComponent> optional, int n, int n2) {
        List<ClientTooltipComponent> list2 = list.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        optional.ifPresent(tooltipComponent -> list2.add(1, ClientTooltipComponent.create(tooltipComponent)));
        this.renderTooltipInternal(poseStack, list2, n, n2);
    }

    public List<Component> getTooltipFromItem(ItemStack itemStack) {
        return itemStack.getTooltipLines(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    public void renderTooltip(PoseStack poseStack, Component component, int n, int n2) {
        this.renderTooltip(poseStack, Arrays.asList(component.getVisualOrderText()), n, n2);
    }

    public void renderComponentTooltip(PoseStack poseStack, List<Component> list, int n, int n2) {
        this.renderTooltip(poseStack, Lists.transform(list, Component::getVisualOrderText), n, n2);
    }

    public void renderTooltip(PoseStack poseStack, List<? extends FormattedCharSequence> list, int n, int n2) {
        this.renderTooltipInternal(poseStack, list.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), n, n2);
    }

    private void renderTooltipInternal(PoseStack poseStack, List<ClientTooltipComponent> list, int n, int n2) {
        ClientTooltipComponent clientTooltipComponent;
        int n3;
        int n4;
        if (list.isEmpty()) {
            return;
        }
        int n5 = 0;
        int n6 = list.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent clientTooltipComponent2 : list) {
            n4 = clientTooltipComponent2.getWidth(this.font);
            if (n4 > n5) {
                n5 = n4;
            }
            n6 += clientTooltipComponent2.getHeight();
        }
        int n7 = n + 12;
        int n8 = n2 - 12;
        n4 = n5;
        int n9 = n6;
        if (n7 + n5 > this.width) {
            n7 -= 28 + n5;
        }
        if (n8 + n9 + 6 > this.height) {
            n8 = this.height - n9 - 6;
        }
        poseStack.pushPose();
        int n10 = -267386864;
        int n11 = 0x505000FF;
        int n12 = 1344798847;
        int n13 = 400;
        float f = this.itemRenderer.blitOffset;
        this.itemRenderer.blitOffset = 400.0f;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix4f = poseStack.last().pose();
        Screen.fillGradient(matrix4f, bufferBuilder, n7 - 3, n8 - 4, n7 + n4 + 3, n8 - 3, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, n7 - 3, n8 + n9 + 3, n7 + n4 + 3, n8 + n9 + 4, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, n7 - 3, n8 - 3, n7 + n4 + 3, n8 + n9 + 3, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, n7 - 4, n8 - 3, n7 - 3, n8 + n9 + 3, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, n7 + n4 + 3, n8 - 3, n7 + n4 + 4, n8 + n9 + 3, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, n7 - 3, n8 - 3 + 1, n7 - 3 + 1, n8 + n9 + 3 - 1, 400, 0x505000FF, 1344798847);
        Screen.fillGradient(matrix4f, bufferBuilder, n7 + n4 + 2, n8 - 3 + 1, n7 + n4 + 3, n8 + n9 + 3 - 1, 400, 0x505000FF, 1344798847);
        Screen.fillGradient(matrix4f, bufferBuilder, n7 - 3, n8 - 3, n7 + n4 + 3, n8 - 3 + 1, 400, 0x505000FF, 0x505000FF);
        Screen.fillGradient(matrix4f, bufferBuilder, n7 - 3, n8 + n9 + 2, n7 + n4 + 3, n8 + n9 + 3, 400, 1344798847, 1344798847);
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        poseStack.translate(0.0, 0.0, 400.0);
        int n14 = n8;
        for (n3 = 0; n3 < list.size(); ++n3) {
            clientTooltipComponent = list.get(n3);
            clientTooltipComponent.renderText(this.font, n7, n14, matrix4f, bufferSource);
            n14 += clientTooltipComponent.getHeight() + (n3 == 0 ? 2 : 0);
        }
        bufferSource.endBatch();
        poseStack.popPose();
        n14 = n8;
        for (n3 = 0; n3 < list.size(); ++n3) {
            clientTooltipComponent = list.get(n3);
            clientTooltipComponent.renderImage(this.font, n7, n14, poseStack, this.itemRenderer, 400);
            n14 += clientTooltipComponent.getHeight() + (n3 == 0 ? 2 : 0);
        }
        this.itemRenderer.blitOffset = f;
    }

    protected void renderComponentHoverEffect(PoseStack poseStack, @Nullable Style style, int n, int n2) {
        if (style == null || style.getHoverEvent() == null) {
            return;
        }
        HoverEvent hoverEvent = style.getHoverEvent();
        HoverEvent.ItemStackInfo itemStackInfo = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
        if (itemStackInfo != null) {
            this.renderTooltip(poseStack, itemStackInfo.getItemStack(), n, n2);
        } else {
            HoverEvent.EntityTooltipInfo entityTooltipInfo = hoverEvent.getValue(HoverEvent.Action.SHOW_ENTITY);
            if (entityTooltipInfo != null) {
                if (this.minecraft.options.advancedItemTooltips) {
                    this.renderComponentTooltip(poseStack, entityTooltipInfo.getTooltipLines(), n, n2);
                }
            } else {
                Component component = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
                if (component != null) {
                    this.renderTooltip(poseStack, this.minecraft.font.split(component, Math.max(this.width / 2, 200)), n, n2);
                }
            }
        }
    }

    protected void insertText(String string, boolean bl) {
    }

    public boolean handleComponentClicked(@Nullable Style style) {
        if (style == null) {
            return false;
        }
        ClickEvent clickEvent = style.getClickEvent();
        if (Screen.hasShiftDown()) {
            if (style.getInsertion() != null) {
                this.insertText(style.getInsertion(), false);
            }
        } else if (clickEvent != null) {
            block21: {
                if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL) {
                    if (!this.minecraft.options.chatLinks) {
                        return false;
                    }
                    try {
                        URI uRI = new URI(clickEvent.getValue());
                        String string = uRI.getScheme();
                        if (string == null) {
                            throw new URISyntaxException(clickEvent.getValue(), "Missing protocol");
                        }
                        if (!ALLOWED_PROTOCOLS.contains(string.toLowerCase(Locale.ROOT))) {
                            throw new URISyntaxException(clickEvent.getValue(), "Unsupported protocol: " + string.toLowerCase(Locale.ROOT));
                        }
                        if (this.minecraft.options.chatLinksPrompt) {
                            this.clickedLink = uRI;
                            this.minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, clickEvent.getValue(), false));
                            break block21;
                        }
                        this.openLink(uRI);
                    }
                    catch (URISyntaxException uRISyntaxException) {
                        LOGGER.error("Can't open url for {}", (Object)clickEvent, (Object)uRISyntaxException);
                    }
                } else if (clickEvent.getAction() == ClickEvent.Action.OPEN_FILE) {
                    URI uRI = new File(clickEvent.getValue()).toURI();
                    this.openLink(uRI);
                } else if (clickEvent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
                    this.insertText(clickEvent.getValue(), true);
                } else if (clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                    this.sendMessage(clickEvent.getValue(), false);
                } else if (clickEvent.getAction() == ClickEvent.Action.COPY_TO_CLIPBOARD) {
                    this.minecraft.keyboardHandler.setClipboard(clickEvent.getValue());
                } else {
                    LOGGER.error("Don't know how to handle {}", (Object)clickEvent);
                }
            }
            return true;
        }
        return false;
    }

    public void sendMessage(String string) {
        this.sendMessage(string, true);
    }

    public void sendMessage(String string, boolean bl) {
        if (bl) {
            this.minecraft.gui.getChat().addRecentChat(string);
        }
        this.minecraft.player.chat(string);
    }

    public final void init(Minecraft minecraft, int n, int n2) {
        this.minecraft = minecraft;
        this.itemRenderer = minecraft.getItemRenderer();
        this.font = minecraft.font;
        this.width = n;
        this.height = n2;
        this.clearWidgets();
        this.setFocused(null);
        this.init();
        this.triggerImmediateNarration(false);
        this.suppressNarration(NARRATE_SUPPRESS_AFTER_INIT_TIME);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    protected void init() {
    }

    public void tick() {
    }

    public void removed() {
    }

    public void renderBackground(PoseStack poseStack) {
        this.renderBackground(poseStack, 0);
    }

    public void renderBackground(PoseStack poseStack, int n) {
        if (this.minecraft.level != null) {
            this.fillGradient(poseStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.renderDirtBackground(n);
        }
    }

    public void renderDirtBackground(int n) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        float f = 32.0f;
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(0.0, this.height, 0.0).uv(0.0f, (float)this.height / 32.0f + (float)n).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(this.width, this.height, 0.0).uv((float)this.width / 32.0f, (float)this.height / 32.0f + (float)n).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(this.width, 0.0, 0.0).uv((float)this.width / 32.0f, n).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(0.0, 0.0, 0.0).uv(0.0f, n).color(64, 64, 64, 255).endVertex();
        tesselator.end();
    }

    public boolean isPauseScreen() {
        return true;
    }

    private void confirmLink(boolean bl) {
        if (bl) {
            this.openLink(this.clickedLink);
        }
        this.clickedLink = null;
        this.minecraft.setScreen(this);
    }

    private void openLink(URI uRI) {
        Util.getPlatform().openUri(uRI);
    }

    public static boolean hasControlDown() {
        if (Minecraft.ON_OSX) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 343) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 347);
        }
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 341) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 345);
    }

    public static boolean hasShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
    }

    public static boolean hasAltDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 342) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 346);
    }

    public static boolean isCut(int n) {
        return n == 88 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public static boolean isPaste(int n) {
        return n == 86 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public static boolean isCopy(int n) {
        return n == 67 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public static boolean isSelectAll(int n) {
        return n == 65 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public void resize(Minecraft minecraft, int n, int n2) {
        this.init(minecraft, n, n2);
    }

    public static void wrapScreenError(Runnable runnable, String string, String string2) {
        try {
            runnable.run();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, string);
            CrashReportCategory crashReportCategory = crashReport.addCategory("Affected screen");
            crashReportCategory.setDetail("Screen name", () -> string2);
            throw new ReportedException(crashReport);
        }
    }

    protected boolean isValidCharacterForName(String string, char c, int n) {
        int n2 = string.indexOf(58);
        int n3 = string.indexOf(47);
        if (c == ':') {
            return (n3 == -1 || n <= n3) && n2 == -1;
        }
        if (c == '/') {
            return n > n2;
        }
        return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.';
    }

    @Override
    public boolean isMouseOver(double d, double d2) {
        return true;
    }

    public void onFilesDrop(List<Path> list) {
    }

    private void scheduleNarration(long l, boolean bl) {
        this.nextNarrationTime = Util.getMillis() + l;
        if (bl) {
            this.narrationSuppressTime = Long.MIN_VALUE;
        }
    }

    private void suppressNarration(long l) {
        this.narrationSuppressTime = Util.getMillis() + l;
    }

    public void afterMouseMove() {
        this.scheduleNarration(750L, false);
    }

    public void afterMouseAction() {
        this.scheduleNarration(200L, true);
    }

    public void afterKeyboardAction() {
        this.scheduleNarration(200L, true);
    }

    private boolean shouldRunNarration() {
        return NarratorChatListener.INSTANCE.isActive();
    }

    public void handleDelayedNarration() {
        long l;
        if (this.shouldRunNarration() && (l = Util.getMillis()) > this.nextNarrationTime && l > this.narrationSuppressTime) {
            this.runNarration(true);
            this.nextNarrationTime = Long.MAX_VALUE;
        }
    }

    protected void triggerImmediateNarration(boolean bl) {
        if (this.shouldRunNarration()) {
            this.runNarration(bl);
        }
    }

    private void runNarration(boolean bl) {
        this.narrationState.update(this::updateNarrationState);
        String string = this.narrationState.collectNarrationText(!bl);
        if (!string.isEmpty()) {
            NarratorChatListener.INSTANCE.sayNow(string);
        }
    }

    protected void updateNarrationState(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getNarrationMessage());
        narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
        this.updateNarratedWidget(narrationElementOutput);
    }

    protected void updateNarratedWidget(NarrationElementOutput narrationElementOutput) {
        ImmutableList immutableList = (ImmutableList)this.narratables.stream().filter(NarratableEntry::isActive).collect(ImmutableList.toImmutableList());
        NarratableSearchResult narratableSearchResult = Screen.findNarratableWidget((List<? extends NarratableEntry>)immutableList, this.lastNarratable);
        if (narratableSearchResult != null) {
            if (narratableSearchResult.priority.isTerminal()) {
                this.lastNarratable = narratableSearchResult.entry;
            }
            if (immutableList.size() > 1) {
                narrationElementOutput.add(NarratedElementType.POSITION, (Component)new TranslatableComponent("narrator.position.screen", narratableSearchResult.index + 1, immutableList.size()));
                if (narratableSearchResult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
                    narrationElementOutput.add(NarratedElementType.USAGE, (Component)new TranslatableComponent("narration.component_list.usage"));
                }
            }
            narratableSearchResult.entry.updateNarration(narrationElementOutput.nest());
        }
    }

    @Nullable
    public static NarratableSearchResult findNarratableWidget(List<? extends NarratableEntry> list, @Nullable NarratableEntry narratableEntry) {
        NarratableSearchResult narratableSearchResult = null;
        NarratableSearchResult narratableSearchResult2 = null;
        int n = list.size();
        for (int i = 0; i < n; ++i) {
            NarratableEntry narratableEntry2 = list.get(i);
            NarratableEntry.NarrationPriority narrationPriority = narratableEntry2.narrationPriority();
            if (narrationPriority.isTerminal()) {
                if (narratableEntry2 == narratableEntry) {
                    narratableSearchResult2 = new NarratableSearchResult(narratableEntry2, i, narrationPriority);
                    continue;
                }
                return new NarratableSearchResult(narratableEntry2, i, narrationPriority);
            }
            if (narrationPriority.compareTo(narratableSearchResult != null ? narratableSearchResult.priority : NarratableEntry.NarrationPriority.NONE) <= 0) continue;
            narratableSearchResult = new NarratableSearchResult(narratableEntry2, i, narrationPriority);
        }
        return narratableSearchResult != null ? narratableSearchResult : narratableSearchResult2;
    }

    public void narrationEnabled() {
        this.scheduleNarration(NARRATE_DELAY_NARRATOR_ENABLED, false);
    }

    protected static void hideWidgets(AbstractWidget ... abstractWidgetArray) {
        for (AbstractWidget abstractWidget : abstractWidgetArray) {
            abstractWidget.visible = false;
        }
    }

    static {
        NARRATE_DELAY_NARRATOR_ENABLED = NARRATE_SUPPRESS_AFTER_INIT_TIME = TimeUnit.SECONDS.toMillis(2L);
    }

    public static class NarratableSearchResult {
        public final NarratableEntry entry;
        public final int index;
        public final NarratableEntry.NarrationPriority priority;

        public NarratableSearchResult(NarratableEntry narratableEntry, int n, NarratableEntry.NarrationPriority narrationPriority) {
            this.entry = narratableEntry;
            this.index = n;
            this.priority = narrationPriority;
        }
    }
}

