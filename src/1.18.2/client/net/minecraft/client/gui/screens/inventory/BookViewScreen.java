/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;

public class BookViewScreen
extends Screen {
    public static final int PAGE_INDICATOR_TEXT_Y_OFFSET = 16;
    public static final int PAGE_TEXT_X_OFFSET = 36;
    public static final int PAGE_TEXT_Y_OFFSET = 30;
    public static final BookAccess EMPTY_ACCESS = new BookAccess(){

        @Override
        public int getPageCount() {
            return 0;
        }

        @Override
        public FormattedText getPageRaw(int n) {
            return FormattedText.EMPTY;
        }
    };
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("textures/gui/book.png");
    protected static final int TEXT_WIDTH = 114;
    protected static final int TEXT_HEIGHT = 128;
    protected static final int IMAGE_WIDTH = 192;
    protected static final int IMAGE_HEIGHT = 192;
    private BookAccess bookAccess;
    private int currentPage;
    private List<FormattedCharSequence> cachedPageComponents = Collections.emptyList();
    private int cachedPage = -1;
    private Component pageMsg = TextComponent.EMPTY;
    private PageButton forwardButton;
    private PageButton backButton;
    private final boolean playTurnSound;

    public BookViewScreen(BookAccess bookAccess) {
        this(bookAccess, true);
    }

    public BookViewScreen() {
        this(EMPTY_ACCESS, false);
    }

    private BookViewScreen(BookAccess bookAccess, boolean bl) {
        super(NarratorChatListener.NO_TITLE);
        this.bookAccess = bookAccess;
        this.playTurnSound = bl;
    }

    public void setBookAccess(BookAccess bookAccess) {
        this.bookAccess = bookAccess;
        this.currentPage = Mth.clamp(this.currentPage, 0, bookAccess.getPageCount());
        this.updateButtonVisibility();
        this.cachedPage = -1;
    }

    public boolean setPage(int n) {
        int n2 = Mth.clamp(n, 0, this.bookAccess.getPageCount() - 1);
        if (n2 != this.currentPage) {
            this.currentPage = n2;
            this.updateButtonVisibility();
            this.cachedPage = -1;
            return true;
        }
        return false;
    }

    protected boolean forcePage(int n) {
        return this.setPage(n);
    }

    @Override
    protected void init() {
        this.createMenuControls();
        this.createPageControlButtons();
    }

    protected void createMenuControls() {
        this.addRenderableWidget(new Button(this.width / 2 - 100, 196, 200, 20, CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(null)));
    }

    protected void createPageControlButtons() {
        int n = (this.width - 192) / 2;
        int n2 = 2;
        this.forwardButton = this.addRenderableWidget(new PageButton(n + 116, 159, true, button -> this.pageForward(), this.playTurnSound));
        this.backButton = this.addRenderableWidget(new PageButton(n + 43, 159, false, button -> this.pageBack(), this.playTurnSound));
        this.updateButtonVisibility();
    }

    private int getNumPages() {
        return this.bookAccess.getPageCount();
    }

    protected void pageBack() {
        if (this.currentPage > 0) {
            --this.currentPage;
        }
        this.updateButtonVisibility();
    }

    protected void pageForward() {
        if (this.currentPage < this.getNumPages() - 1) {
            ++this.currentPage;
        }
        this.updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        this.forwardButton.visible = this.currentPage < this.getNumPages() - 1;
        this.backButton.visible = this.currentPage > 0;
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (super.keyPressed(n, n2, n3)) {
            return true;
        }
        switch (n) {
            case 266: {
                this.backButton.onPress();
                return true;
            }
            case 267: {
                this.forwardButton.onPress();
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderBackground(poseStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BOOK_LOCATION);
        int n3 = (this.width - 192) / 2;
        int n4 = 2;
        this.blit(poseStack, n3, 2, 0, 0, 192, 192);
        if (this.cachedPage != this.currentPage) {
            FormattedText formattedText = this.bookAccess.getPage(this.currentPage);
            this.cachedPageComponents = this.font.split(formattedText, 114);
            this.pageMsg = new TranslatableComponent("book.pageIndicator", this.currentPage + 1, Math.max(this.getNumPages(), 1));
        }
        this.cachedPage = this.currentPage;
        int n5 = this.font.width(this.pageMsg);
        this.font.draw(poseStack, this.pageMsg, (float)(n3 - n5 + 192 - 44), 18.0f, 0);
        int n6 = Math.min(128 / this.font.lineHeight, this.cachedPageComponents.size());
        for (int i = 0; i < n6; ++i) {
            FormattedCharSequence formattedCharSequence = this.cachedPageComponents.get(i);
            this.font.draw(poseStack, formattedCharSequence, (float)(n3 + 36), (float)(32 + i * this.font.lineHeight), 0);
        }
        Style style = this.getClickedComponentStyleAt(n, n2);
        if (style != null) {
            this.renderComponentHoverEffect(poseStack, style, n, n2);
        }
        super.render(poseStack, n, n2, f);
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        Style style;
        if (n == 0 && (style = this.getClickedComponentStyleAt(d, d2)) != null && this.handleComponentClicked(style)) {
            return true;
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    public boolean handleComponentClicked(Style style) {
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null) {
            return false;
        }
        if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
            String string = clickEvent.getValue();
            try {
                int n = Integer.parseInt(string) - 1;
                return this.forcePage(n);
            }
            catch (Exception exception) {
                return false;
            }
        }
        boolean bl = super.handleComponentClicked(style);
        if (bl && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
            this.closeScreen();
        }
        return bl;
    }

    protected void closeScreen() {
        this.minecraft.setScreen(null);
    }

    @Nullable
    public Style getClickedComponentStyleAt(double d, double d2) {
        if (this.cachedPageComponents.isEmpty()) {
            return null;
        }
        int n = Mth.floor(d - (double)((this.width - 192) / 2) - 36.0);
        int n2 = Mth.floor(d2 - 2.0 - 30.0);
        if (n < 0 || n2 < 0) {
            return null;
        }
        int n3 = Math.min(128 / this.font.lineHeight, this.cachedPageComponents.size());
        if (n <= 114 && n2 < this.minecraft.font.lineHeight * n3 + n3) {
            int n4 = n2 / this.minecraft.font.lineHeight;
            if (n4 >= 0 && n4 < this.cachedPageComponents.size()) {
                FormattedCharSequence formattedCharSequence = this.cachedPageComponents.get(n4);
                return this.minecraft.font.getSplitter().componentStyleAtWidth(formattedCharSequence, n);
            }
            return null;
        }
        return null;
    }

    static List<String> loadPages(CompoundTag compoundTag) {
        ImmutableList.Builder builder = ImmutableList.builder();
        BookViewScreen.loadPages(compoundTag, arg_0 -> ((ImmutableList.Builder)builder).add(arg_0));
        return builder.build();
    }

    public static void loadPages(CompoundTag compoundTag, Consumer<String> consumer) {
        IntFunction<String> intFunction;
        ListTag listTag = compoundTag.getList("pages", 8).copy();
        if (Minecraft.getInstance().isTextFilteringEnabled() && compoundTag.contains("filtered_pages", 10)) {
            CompoundTag compoundTag2 = compoundTag.getCompound("filtered_pages");
            intFunction = n -> {
                String string = String.valueOf(n);
                return compoundTag2.contains(string) ? compoundTag2.getString(string) : listTag.getString(n);
            };
        } else {
            intFunction = listTag::getString;
        }
        for (int i = 0; i < listTag.size(); ++i) {
            consumer.accept(intFunction.apply(i));
        }
    }

    public static interface BookAccess {
        public int getPageCount();

        public FormattedText getPageRaw(int var1);

        default public FormattedText getPage(int n) {
            if (n >= 0 && n < this.getPageCount()) {
                return this.getPageRaw(n);
            }
            return FormattedText.EMPTY;
        }

        public static BookAccess fromItem(ItemStack itemStack) {
            if (itemStack.is(Items.WRITTEN_BOOK)) {
                return new WrittenBookAccess(itemStack);
            }
            if (itemStack.is(Items.WRITABLE_BOOK)) {
                return new WritableBookAccess(itemStack);
            }
            return EMPTY_ACCESS;
        }
    }

    public static class WritableBookAccess
    implements BookAccess {
        private final List<String> pages;

        public WritableBookAccess(ItemStack itemStack) {
            this.pages = WritableBookAccess.readPages(itemStack);
        }

        private static List<String> readPages(ItemStack itemStack) {
            CompoundTag compoundTag = itemStack.getTag();
            return compoundTag != null ? BookViewScreen.loadPages(compoundTag) : ImmutableList.of();
        }

        @Override
        public int getPageCount() {
            return this.pages.size();
        }

        @Override
        public FormattedText getPageRaw(int n) {
            return FormattedText.of(this.pages.get(n));
        }
    }

    public static class WrittenBookAccess
    implements BookAccess {
        private final List<String> pages;

        public WrittenBookAccess(ItemStack itemStack) {
            this.pages = WrittenBookAccess.readPages(itemStack);
        }

        private static List<String> readPages(ItemStack itemStack) {
            CompoundTag compoundTag = itemStack.getTag();
            if (compoundTag != null && WrittenBookItem.makeSureTagIsValid(compoundTag)) {
                return BookViewScreen.loadPages(compoundTag);
            }
            return ImmutableList.of((Object)Component.Serializer.toJson(new TranslatableComponent("book.invalid.tag").withStyle(ChatFormatting.DARK_RED)));
        }

        @Override
        public int getPageCount() {
            return this.pages.size();
        }

        @Override
        public FormattedText getPageRaw(int n) {
            String string = this.pages.get(n);
            try {
                MutableComponent mutableComponent = Component.Serializer.fromJson(string);
                if (mutableComponent != null) {
                    return mutableComponent;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return FormattedText.of(string);
        }
    }
}

