/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import java.util.List;
import net.minecraft.client.Options;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.util.FormattedCharSequence;

public class SliderButton
extends AbstractOptionSliderButton
implements TooltipAccessor {
    private final ProgressOption option;
    private final List<FormattedCharSequence> tooltip;

    public SliderButton(Options options, int n, int n2, int n3, int n4, ProgressOption progressOption, List<FormattedCharSequence> list) {
        super(options, n, n2, n3, n4, (double)((float)progressOption.toPct(progressOption.get(options))));
        this.option = progressOption;
        this.tooltip = list;
        this.updateMessage();
    }

    @Override
    protected void applyValue() {
        this.option.set(this.options, this.option.toValue(this.value));
        this.options.save();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.option.getMessage(this.options));
    }

    @Override
    public List<FormattedCharSequence> getTooltip() {
        return this.tooltip;
    }
}

