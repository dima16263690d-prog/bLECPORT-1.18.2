/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class OptionsList
extends ContainerObjectSelectionList<Entry> {
    public OptionsList(Minecraft minecraft, int n, int n2, int n3, int n4, int n5) {
        super(minecraft, n, n2, n3, n4, n5);
        this.centerListVertically = false;
    }

    public int addBig(Option option) {
        return this.addEntry(Entry.big(this.minecraft.options, this.width, option));
    }

    public void addSmall(Option option, @Nullable Option option2) {
        this.addEntry(Entry.small(this.minecraft.options, this.width, option, option2));
    }

    public void addSmall(Option[] optionArray) {
        for (int i = 0; i < optionArray.length; i += 2) {
            this.addSmall(optionArray[i], i < optionArray.length - 1 ? optionArray[i + 1] : null);
        }
    }

    @Override
    public int getRowWidth() {
        return 400;
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 32;
    }

    @Nullable
    public AbstractWidget findOption(Option option) {
        for (Entry entry : this.children()) {
            AbstractWidget abstractWidget = entry.options.get(option);
            if (abstractWidget == null) continue;
            return abstractWidget;
        }
        return null;
    }

    public Optional<AbstractWidget> getMouseOver(double d, double d2) {
        for (Entry entry : this.children()) {
            for (AbstractWidget abstractWidget : entry.children) {
                if (!abstractWidget.isMouseOver(d, d2)) continue;
                return Optional.of(abstractWidget);
            }
        }
        return Optional.empty();
    }

    protected static class Entry
    extends ContainerObjectSelectionList.Entry<Entry> {
        final Map<Option, AbstractWidget> options;
        final List<AbstractWidget> children;

        private Entry(Map<Option, AbstractWidget> map) {
            this.options = map;
            this.children = ImmutableList.copyOf(map.values());
        }

        public static Entry big(Options options, int n, Option option) {
            return new Entry((Map<Option, AbstractWidget>)ImmutableMap.of((Object)option, (Object)option.createButton(options, n / 2 - 155, 0, 310)));
        }

        public static Entry small(Options options, int n, Option option, @Nullable Option option2) {
            AbstractWidget abstractWidget = option.createButton(options, n / 2 - 155, 0, 150);
            if (option2 == null) {
                return new Entry((Map<Option, AbstractWidget>)ImmutableMap.of((Object)option, (Object)abstractWidget));
            }
            return new Entry((Map<Option, AbstractWidget>)ImmutableMap.of((Object)option, (Object)abstractWidget, (Object)option2, (Object)option2.createButton(options, n / 2 - 155 + 160, 0, 150)));
        }

        @Override
        public void render(PoseStack poseStack, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            this.children.forEach(abstractWidget -> {
                abstractWidget.y = n2;
                abstractWidget.render(poseStack, n6, n7, f);
            });
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }
    }
}

