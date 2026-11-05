/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Queues
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.slf4j.Logger;

public class ChatComponent
extends GuiComponent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_CHAT_HISTORY = 100;
    private final Minecraft minecraft;
    private final List<String> recentChat = Lists.newArrayList();
    private final List<GuiMessage<Component>> allMessages = Lists.newArrayList();
    private final List<GuiMessage<FormattedCharSequence>> trimmedMessages = Lists.newArrayList();
    private final Deque<Component> chatQueue = Queues.newArrayDeque();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;
    private long lastMessage;

    public ChatComponent(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(PoseStack poseStack, int n) {
        int n2;
        int n3;
        int n4;
        int n5;
        if (this.isChatHidden()) {
            return;
        }
        this.processPendingMessages();
        int n6 = this.getLinesPerPage();
        int n7 = this.trimmedMessages.size();
        if (n7 <= 0) {
            return;
        }
        boolean bl = false;
        if (this.isChatFocused()) {
            bl = true;
        }
        float f = (float)this.getScale();
        int n8 = Mth.ceil((float)this.getWidth() / f);
        poseStack.pushPose();
        poseStack.translate(4.0, 8.0, 0.0);
        poseStack.scale(f, f, 1.0f);
        double d = this.minecraft.options.chatOpacity * (double)0.9f + (double)0.1f;
        double d2 = this.minecraft.options.textBackgroundOpacity;
        double d3 = 9.0 * (this.minecraft.options.chatLineSpacing + 1.0);
        double d4 = -8.0 * (this.minecraft.options.chatLineSpacing + 1.0) + 4.0 * this.minecraft.options.chatLineSpacing;
        int n9 = 0;
        for (n5 = 0; n5 + this.chatScrollbarPos < this.trimmedMessages.size() && n5 < n6; ++n5) {
            GuiMessage<FormattedCharSequence> guiMessage = this.trimmedMessages.get(n5 + this.chatScrollbarPos);
            if (guiMessage == null || (n4 = n - guiMessage.getAddedTime()) >= 200 && !bl) continue;
            double d5 = bl ? 1.0 : ChatComponent.getTimeFactor(n4);
            n3 = (int)(255.0 * d5 * d);
            n2 = (int)(255.0 * d5 * d2);
            ++n9;
            if (n3 <= 3) continue;
            boolean bl2 = false;
            double d6 = (double)(-n5) * d3;
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 50.0);
            ChatComponent.fill(poseStack, -4, (int)(d6 - d3), 0 + n8 + 4, (int)d6, n2 << 24);
            RenderSystem.enableBlend();
            poseStack.translate(0.0, 0.0, 50.0);
            this.minecraft.font.drawShadow(poseStack, guiMessage.getMessage(), 0.0f, (float)((int)(d6 + d4)), 0xFFFFFF + (n3 << 24));
            RenderSystem.disableBlend();
            poseStack.popPose();
        }
        if (!this.chatQueue.isEmpty()) {
            n5 = (int)(128.0 * d);
            int n10 = (int)(255.0 * d2);
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 50.0);
            ChatComponent.fill(poseStack, -2, 0, n8 + 4, 9, n10 << 24);
            RenderSystem.enableBlend();
            poseStack.translate(0.0, 0.0, 50.0);
            this.minecraft.font.drawShadow(poseStack, new TranslatableComponent("chat.queue", this.chatQueue.size()), 0.0f, 1.0f, 0xFFFFFF + (n5 << 24));
            poseStack.popPose();
            RenderSystem.disableBlend();
        }
        if (bl) {
            n5 = this.minecraft.font.lineHeight;
            int n11 = n7 * n5;
            n4 = n9 * n5;
            int n12 = this.chatScrollbarPos * n4 / n7;
            int n13 = n4 * n4 / n11;
            if (n11 != n4) {
                n3 = n12 > 0 ? 170 : 96;
                n2 = this.newMessageSinceScroll ? 0xCC3333 : 0x3333AA;
                poseStack.translate(-4.0, 0.0, 0.0);
                ChatComponent.fill(poseStack, 0, -n12, 2, -n12 - n13, n2 + (n3 << 24));
                ChatComponent.fill(poseStack, 2, -n12, 1, -n12 - n13, 0xCCCCCC + (n3 << 24));
            }
        }
        poseStack.popPose();
    }

    private boolean isChatHidden() {
        return this.minecraft.options.chatVisibility == ChatVisiblity.HIDDEN;
    }

    private static double getTimeFactor(int n) {
        double d = (double)n / 200.0;
        d = 1.0 - d;
        d *= 10.0;
        d = Mth.clamp(d, 0.0, 1.0);
        d *= d;
        return d;
    }

    public void clearMessages(boolean bl) {
        this.chatQueue.clear();
        this.trimmedMessages.clear();
        this.allMessages.clear();
        if (bl) {
            this.recentChat.clear();
        }
    }

    public void addMessage(Component component) {
        this.addMessage(component, 0);
    }

    private void addMessage(Component component, int n) {
        this.addMessage(component, n, this.minecraft.gui.getGuiTicks(), false);
        LOGGER.info("[CHAT] {}", (Object)component.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void addMessage(Component component, int n, int n2, boolean bl) {
        if (n != 0) {
            this.removeById(n);
        }
        int n3 = Mth.floor((double)this.getWidth() / this.getScale());
        List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(component, n3, this.minecraft.font);
        boolean bl2 = this.isChatFocused();
        for (FormattedCharSequence formattedCharSequence : list) {
            if (bl2 && this.chatScrollbarPos > 0) {
                this.newMessageSinceScroll = true;
                this.scrollChat(1);
            }
            this.trimmedMessages.add(0, new GuiMessage<FormattedCharSequence>(n2, formattedCharSequence, n));
        }
        while (this.trimmedMessages.size() > 100) {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }
        if (!bl) {
            this.allMessages.add(0, new GuiMessage<Component>(n2, component, n));
            while (this.allMessages.size() > 100) {
                this.allMessages.remove(this.allMessages.size() - 1);
            }
        }
    }

    public void rescaleChat() {
        this.trimmedMessages.clear();
        this.resetChatScroll();
        for (int i = this.allMessages.size() - 1; i >= 0; --i) {
            GuiMessage<Component> guiMessage = this.allMessages.get(i);
            this.addMessage(guiMessage.getMessage(), guiMessage.getId(), guiMessage.getAddedTime(), true);
        }
    }

    public List<String> getRecentChat() {
        return this.recentChat;
    }

    public void addRecentChat(String string) {
        if (this.recentChat.isEmpty() || !this.recentChat.get(this.recentChat.size() - 1).equals(string)) {
            this.recentChat.add(string);
        }
    }

    public void resetChatScroll() {
        this.chatScrollbarPos = 0;
        this.newMessageSinceScroll = false;
    }

    public void scrollChat(int n) {
        this.chatScrollbarPos += n;
        int n2 = this.trimmedMessages.size();
        if (this.chatScrollbarPos > n2 - this.getLinesPerPage()) {
            this.chatScrollbarPos = n2 - this.getLinesPerPage();
        }
        if (this.chatScrollbarPos <= 0) {
            this.chatScrollbarPos = 0;
            this.newMessageSinceScroll = false;
        }
    }

    public boolean handleChatQueueClicked(double d, double d2) {
        if (!this.isChatFocused() || this.minecraft.options.hideGui || this.isChatHidden() || this.chatQueue.isEmpty()) {
            return false;
        }
        double d3 = d - 2.0;
        double d4 = (double)this.minecraft.getWindow().getGuiScaledHeight() - d2 - 40.0;
        if (d3 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && d4 < 0.0 && d4 > (double)Mth.floor(-9.0 * this.getScale())) {
            this.addMessage(this.chatQueue.remove());
            this.lastMessage = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    @Nullable
    public Style getClickedComponentStyleAt(double d, double d2) {
        int n;
        if (!this.isChatFocused() || this.minecraft.options.hideGui || this.isChatHidden()) {
            return null;
        }
        double d3 = d - 2.0;
        double d4 = (double)this.minecraft.getWindow().getGuiScaledHeight() - d2 - 40.0;
        d3 = Mth.floor(d3 / this.getScale());
        d4 = Mth.floor(d4 / (this.getScale() * (this.minecraft.options.chatLineSpacing + 1.0)));
        if (d3 < 0.0 || d4 < 0.0) {
            return null;
        }
        int n2 = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
        if (d3 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && d4 < (double)(this.minecraft.font.lineHeight * n2 + n2) && (n = (int)(d4 / (double)this.minecraft.font.lineHeight + (double)this.chatScrollbarPos)) >= 0 && n < this.trimmedMessages.size()) {
            GuiMessage<FormattedCharSequence> guiMessage = this.trimmedMessages.get(n);
            return this.minecraft.font.getSplitter().componentStyleAtWidth(guiMessage.getMessage(), (int)d3);
        }
        return null;
    }

    private boolean isChatFocused() {
        return this.minecraft.screen instanceof ChatScreen;
    }

    private void removeById(int n) {
        this.trimmedMessages.removeIf(guiMessage -> guiMessage.getId() == n);
        this.allMessages.removeIf(guiMessage -> guiMessage.getId() == n);
    }

    public int getWidth() {
        return ChatComponent.getWidth(this.minecraft.options.chatWidth);
    }

    public int getHeight() {
        return ChatComponent.getHeight((this.isChatFocused() ? this.minecraft.options.chatHeightFocused : this.minecraft.options.chatHeightUnfocused) / (this.minecraft.options.chatLineSpacing + 1.0));
    }

    public double getScale() {
        return this.minecraft.options.chatScale;
    }

    public static int getWidth(double d) {
        int n = 320;
        int n2 = 40;
        return Mth.floor(d * 280.0 + 40.0);
    }

    public static int getHeight(double d) {
        int n = 180;
        int n2 = 20;
        return Mth.floor(d * 160.0 + 20.0);
    }

    public int getLinesPerPage() {
        return this.getHeight() / 9;
    }

    private long getChatRateMillis() {
        return (long)(this.minecraft.options.chatDelay * 1000.0);
    }

    private void processPendingMessages() {
        if (this.chatQueue.isEmpty()) {
            return;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastMessage >= this.getChatRateMillis()) {
            this.addMessage(this.chatQueue.remove());
            this.lastMessage = l;
        }
    }

    public void enqueueMessage(Component component) {
        if (this.minecraft.options.chatDelay <= 0.0) {
            this.addMessage(component);
        } else {
            long l = System.currentTimeMillis();
            if (l - this.lastMessage >= this.getChatRateMillis()) {
                this.addMessage(component);
                this.lastMessage = l;
            } else {
                this.chatQueue.add(component);
            }
        }
    }
}

