/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class OptionsSubScreen
extends Screen {
    protected final Screen lastScreen;
    protected final Options options;

    public OptionsSubScreen(Screen screen, Options options, Component component) {
        super(component);
        this.lastScreen = screen;
        this.options = options;
    }

    @Override
    public void removed() {
        this.minecraft.options.save();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    public static List<FormattedCharSequence> tooltipAt(OptionsList optionsList, int n, int n2) {
        Optional<AbstractWidget> optional = optionsList.getMouseOver(n, n2);
        if (optional.isPresent() && optional.get() instanceof TooltipAccessor) {
            return ((TooltipAccessor)((Object)optional.get())).getTooltip();
        }
        return ImmutableList.of();
    }
}

